package org.example.bookapp.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.OptimisticLockException;
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
    private static EntityManagerFactory entityManagerFactory;

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
        entityManagerFactory = context.getBean(EntityManagerFactory.class);

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

    @Test
    void updateAuthor_shouldThrowOptimisticLockException_whenVersionIsOutdated() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        Integer authorId = authorService.addAuthor(author).getId();

        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();

        EntityTransaction tx1 = em1.getTransaction();
        EntityTransaction tx2 = em2.getTransaction();

        try {
            tx1.begin();

            Author author1 = em1.find(Author.class, authorId);

            tx2.begin();

            Author author2 = em2.find(Author.class, authorId);

            assertThat(author1.getVersion())
                    .isEqualTo(author2.getVersion());

            author1.setFirstName("First transaction");
            author2.setFirstName("Second transaction");

            tx1.commit();

            assertThatThrownBy(() -> {
                em2.flush();
                tx2.commit();
            }).isInstanceOf(OptimisticLockException.class);

        } finally {

            if (tx1.isActive()) {
                tx1.rollback();
            }

            if (tx2.isActive()) {
                tx2.rollback();
            }

            em1.close();
            em2.close();
        }
    }

}