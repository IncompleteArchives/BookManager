package org.example.bookapp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import org.example.bookapp.config.SecurityConfig;
import org.example.bookapp.config.WebConfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class SecurityIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("bookdb_test")
                    .withUsername("test")
                    .withPassword("test");

    private static AnnotationConfigApplicationContext context;
    private static AnnotationConfigWebApplicationContext webContext;
    private static JdbcTemplate jdbcTemplate;
    private static Tomcat tomcat;

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:18080/bookapp";

    @BeforeAll
    static void setUp() throws Exception {

        PostgresTestConfig.dbUrl = postgres.getJdbcUrl();
        PostgresTestConfig.dbUsername = postgres.getUsername();
        PostgresTestConfig.dbPassword = postgres.getPassword();
        PostgresTestConfig.dbDriver = "org.postgresql.Driver";

        initializeDatabase();

        context = new AnnotationConfigApplicationContext(SecurityTestConfig.class);

        jdbcTemplate = context.getBean(JdbcTemplate.class);

        startTomcat();
    }

    @BeforeEach
    void cleanDatabase() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE books, authors, users
                RESTART IDENTITY CASCADE
                """);
    }

    @AfterAll
    static void tearDown() throws Exception {

        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }

        if (webContext != null) {
            webContext.close();
        }

        if (context != null) {
            context.close();
        }
    }

    @Test
    void register_shouldCreateUserWithUserRole() throws Exception {

        HttpResponse<String> response = post(
                "/api/users/register",
                """
                        {
                            "username": "user1",
                            "password": "secret123"
                        }
                        """
        );

        assertThat(response.statusCode()).isEqualTo(201);

        String role = jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE username = ?",
                String.class,
                "user1"
        );

        assertThat(role).isEqualTo("USER");
    }

    @Test
    void register_shouldStorePasswordAsBCrypt() throws Exception {

        registerUser();

        String password = jdbcTemplate.queryForObject(
                "SELECT password FROM users WHERE username = ?",
                String.class,
                "user1"
        );

        PasswordEncoder encoder = context.getBean(PasswordEncoder.class);

        assertThat(password).startsWith("$2");
        assertThat(encoder.matches("secret123", password)).isTrue();
    }

    @Test
    void register_shouldReturn409_whenUsernameExists() throws Exception {

        registerUser();

        HttpResponse<String> response = post(
                "/api/users/register",
                """
                        {
                            "username": "user1",
                            "password": "anotherPassword"
                        }
                        """
        );

        assertThat(response.statusCode()).isEqualTo(409);
    }

    @Test
    void protectedEndpoint_shouldReturn401_withoutAuthentication() throws Exception {

        HttpResponse<String> response =
                post("/api/books/1/borrow");

        assertThat(response.statusCode()).isEqualTo(401);

        assertJsonError(
                response,
                401,
                "Unauthorized"
        );
    }

    @Test
    void protectedEndpoint_shouldReturn401_withWrongPassword() throws Exception {

        registerUser();

        HttpResponse<String> response =
                post(
                        "/api/books/1/borrow",
                        "user1",
                        "wrongPassword"
                );

        assertThat(response.statusCode()).isEqualTo(401);

        assertJsonError(
                response,
                401,
                "Unauthorized"
        );
    }

    @Test
    void user_shouldReceive403_whenDeletingBook() throws Exception {

        registerUser();

        Integer bookId = createBook();

        HttpResponse<String> response =
                delete(
                        "/api/books/" + bookId,
                        "user1",
                        "secret123"
                );

        assertThat(response.statusCode()).isEqualTo(403);

        assertJsonError(
                response,
                403,
                "Forbidden"
        );
    }

    @Test
    void admin_shouldBeAllowedToDeleteBook() throws Exception {

        createUser(
                "admin",
                "admin123",
                "ADMIN"
        );

        Integer bookId = createBook();

        HttpResponse<String> response =
                delete(
                        "/api/books/" + bookId,
                        "admin",
                        "admin123"
                );

        assertThat(response.statusCode()).isEqualTo(204);
    }


    private static void registerUser() throws Exception {

        HttpResponse<String> response = post(
                "/api/users/register",
                """
                        {
                            "username": "user1",
                            "password": "secret123"
                        }
                        """
        );

        assertThat(response.statusCode()).isEqualTo(201);
    }

    private static void createUser(
            String username,
            String password,
            String role) {

        PasswordEncoder encoder =
                context.getBean(PasswordEncoder.class);

        jdbcTemplate.update(
                """
                        INSERT INTO users (username, password, role)
                        VALUES (?, ?, ?)
                        """,
                username,
                encoder.encode(password),
                role
        );
    }

    private static Integer createBook() {

        Integer authorId = jdbcTemplate.queryForObject(
                """
                        INSERT INTO authors (first_name, last_name)
                        VALUES (?, ?)
                        RETURNING id
                        """,
                Integer.class,
                "Audrey",
                "Barker"
        );

        return jdbcTemplate.queryForObject(
                """
                        INSERT INTO books
                            (name, publication_year, author_id, available_copies)
                        VALUES (?, ?, ?, ?)
                        RETURNING id
                        """,
                Integer.class,
                "Test Book",
                2025,
                authorId,
                1
        );
    }

    private static HttpResponse<String> post(
            String path) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + path))
                        .POST(
                                HttpRequest.BodyPublishers.noBody()
                        )
                        .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private static HttpResponse<String> post(
            String path,
            String body) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + path))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private static HttpResponse<String> post(
            String path,
            String username,
            String password) throws Exception {

        HttpRequest request =
                authenticatedRequest(
                        path,
                        username,
                        password
                )
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private static HttpResponse<String> delete(
            String path,
            String username,
            String password) throws Exception {

        HttpRequest request =
                authenticatedRequest(
                        path,
                        username,
                        password
                )
                        .DELETE()
                        .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private static HttpRequest.Builder authenticatedRequest(
            String path,
            String username,
            String password) {

        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header(
                        "Authorization",
                        "Basic " + encoded
                );
    }

    private static void assertJsonError(
            HttpResponse<String> response,
            int status,
            String error) throws Exception {

        JsonNode json = objectMapper.readTree(response.body());

        assertThat(json.get("status").asInt()).isEqualTo(status);
        assertThat(json.get("error").asText()).isEqualTo(error);
    }

    private static void initializeDatabase() throws Exception {

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(
                        PostgresTestConfig.dbUrl,
                        PostgresTestConfig.dbUsername,
                        PostgresTestConfig.dbPassword
                );

        dataSource.setDriverClassName(
                PostgresTestConfig.dbDriver
        );

        try (var connection = dataSource.getConnection()) {

            org.springframework.jdbc.datasource.init.ScriptUtils
                    .executeSqlScript(
                            connection,
                            new org.springframework.core.io.ClassPathResource("schema.sql")
                    );
        }
    }

    private static void startTomcat() throws Exception {

        tomcat = new Tomcat();
        tomcat.setPort(18080);
        tomcat.getConnector();

        webContext = new AnnotationConfigWebApplicationContext();

        webContext.register(
                SecurityTestConfig.class,
                WebConfig.class,
                SecurityConfig.class
        );

        Context tomcatContext =
                tomcat.addContext(
                        "/bookapp",
                        new File(System.getProperty("java.io.tmpdir"))
                                .getAbsolutePath()
                );

        webContext.setServletContext(tomcatContext.getServletContext());

        webContext.refresh();

        DelegatingFilterProxy securityFilter =
                new DelegatingFilterProxy(
                        "springSecurityFilterChain",
                        webContext
                );

        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("springSecurityFilterChain");
        filterDef.setFilter(securityFilter);

        tomcatContext.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("springSecurityFilterChain");
        filterMap.addURLPattern("/*");

        tomcatContext.addFilterMap(filterMap);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(webContext);

        Tomcat.addServlet(
                tomcatContext,
                "dispatcher",
                dispatcherServlet
        );

        tomcatContext.addServletMappingDecoded(
                "/",
                "dispatcher"
        );

        tomcat.start();
    }

}