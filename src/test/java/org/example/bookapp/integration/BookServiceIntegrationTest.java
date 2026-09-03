package org.example.bookapp.integration;

import org.example.bookapp.exception.BookNotAvailableException;
import org.example.bookapp.model.Author;
import org.example.bookapp.service.BookService;

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
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class BookServiceIntegrationTest {

    private static AnnotationConfigApplicationContext context;
    private static BookService bookService;
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
        bookService = context.getBean(BookService.class);
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
                        PostgresTestConfig.dbPassword
                );

        dataSource.setDriverClassName(PostgresTestConfig.dbDriver);

        ClassPathResource resource = new ClassPathResource("schema.sql");

        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, resource);
        }
    }

    @Test
    void borrowBook_shouldAllowOnlyOneConcurrentBorrow() throws Exception {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        jdbcTemplate.update("""
                        INSERT INTO authors
                            (first_name, middle_name, last_name, gender, birth_date, version)
                        VALUES
                            (?, ?, ?, ?, ?, ?)
                        """,
                author.getFirstName(),
                author.getMiddleName(),
                author.getLastName(),
                author.getGender(),
                author.getBirthDate(),
                0
        );

        Integer authorId = jdbcTemplate.queryForObject(
                "SELECT id FROM authors LIMIT 1",
                Integer.class
        );

        jdbcTemplate.update("""
                        INSERT INTO books
                            (name, publication_year, author_id, available_copies)
                        VALUES
                            (?, ?, ?, ?)
                        """,
                "Book1",
                1999,
                authorId,
                1
        );

        Integer bookId = jdbcTemplate.queryForObject(
                "SELECT id FROM books LIMIT 1",
                Integer.class
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {

            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            Callable<Boolean> borrowAttempt = () -> {

                ready.countDown();

                boolean started = start.await(10, TimeUnit.SECONDS);

                if (!started)
                    throw new AssertionError("Timed out waiting for concurrent borrow start");

                try {
                    bookService.borrowBook(bookId);
                    return true;

                } catch (BookNotAvailableException e) {
                    return false;
                }
            };

            Future<Boolean> firstAttempt =  executor.submit(borrowAttempt);
            Future<Boolean> secondAttempt = executor.submit(borrowAttempt);

            boolean bothReady = ready.await(10, TimeUnit.SECONDS);

            assertThat(bothReady).as("Both borrow attempts must reach the ready barrier").isTrue();

            start.countDown();

            boolean firstResult = firstAttempt.get();
            boolean secondResult = secondAttempt.get();

            assertThat(firstResult).isNotEqualTo(secondResult);

            Integer availableCopies = jdbcTemplate.queryForObject(
                    "SELECT available_copies FROM books WHERE id = ?",
                    Integer.class,
                    bookId
            );

            assertThat(availableCopies).isZero();

        } finally {
            executor.shutdownNow();

            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

    }

}