package org.example.bookapp.integration;

import org.example.bookapp.exception.DatabaseOperationException;
import org.example.bookapp.model.Author;
import org.example.bookapp.model.Book;
import org.example.bookapp.service.AuthorService;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class AuthorServiceIntegrationTest {

    private static AnnotationConfigApplicationContext context;
    private static AuthorService authorService;
    private static JdbcTemplate jdbcTemplate;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("bookdb_test")
                    .withUsername("test")
                    .withPassword("test");

    @BeforeAll
    static void configureDataSource() throws SQLException {

        PostgresTestConfig.dbUrl = postgres.getJdbcUrl();
        PostgresTestConfig.dbUsername = postgres.getUsername();
        PostgresTestConfig.dbPassword = postgres.getPassword();
        PostgresTestConfig.dbDriver = "org.postgresql.Driver";

        initializeDatabase();

        context = new AnnotationConfigApplicationContext(PostgresTestConfig.class);
        authorService = context.getBean(AuthorService.class);
        jdbcTemplate = context.getBean(JdbcTemplate.class);

    }

    @BeforeEach
    void setUp() {

        jdbcTemplate.execute("TRUNCATE TABLE books, authors RESTART IDENTITY CASCADE");
    }

    @AfterAll
    static void tearDownClass() {

        context.close();
    }

    private static void initializeDatabase() throws SQLException {

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(
                        PostgresTestConfig.dbUrl,
                        PostgresTestConfig.dbUsername,
                        PostgresTestConfig.dbPassword);

        dataSource.setDriverClassName(PostgresTestConfig.dbDriver);

        ClassPathResource resource = new ClassPathResource("schema.sql");

        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, resource);
        }
    }

    @Test
    void database_shouldBeInitialized() {

        Integer authorsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM authors",
                Integer.class
        );

        Integer booksCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM books",
                Integer.class
        );

        assertThat(authorsCount).isEqualTo(0);
        assertThat(booksCount).isEqualTo(0);
    }

    @Test
    void addAuthorWithBooks_shouldSaveAuthorAndBooks() {
        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        Book book1 = new Book("Book1", 1999);
        Book book2 = new Book("Book2", 1992);
        List<Book> books = List.of(book1, book2);

        Integer authorId = authorService.addAuthorWithBooks(author, books);

        assertThat(authorId).isNotNull();

        Integer authorCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM authors WHERE id = ?",
                Integer.class,
                authorId
        );

        assertThat(authorCount).isEqualTo(1);

        Integer booksCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM books WHERE author_id = ?",
                Integer.class,
                authorId
        );

        assertThat(booksCount).isEqualTo(2);
    }

    @Test
    void addAuthorWithBooks_shouldRollback_whenSecondBookFails() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        Book book1 = new Book("Book1", 1999);
        Book book2 = new Book("B".repeat(256), 1992);
        List<Book> books = List.of(book1, book2);

        assertThatThrownBy(() ->
                authorService.addAuthorWithBooks(author, books))
                .isInstanceOf(DatabaseOperationException.class);

        Integer authorCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM authors WHERE first_name = ?",
                Integer.class,
                "Audrey"
        );

        assertThat(authorCount).isZero();

        Integer bookCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM books WHERE name = ?",
                Integer.class,
                "Book1"
        );

        assertThat(bookCount).isZero();
    }

}