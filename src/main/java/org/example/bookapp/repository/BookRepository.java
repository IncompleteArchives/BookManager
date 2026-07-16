package org.example.bookapp.repository;

import org.example.bookapp.model.Book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<Book> BOOK_ROW_MAPPER =
            (resultSet, rowNum) -> new Book(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("author"),
                    resultSet.getInt("publication_year")
            );

    @Autowired
    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void add(Book book) {
        jdbcTemplate.update("INSERT INTO books(name, author, publication_year) VALUES (?, ?, ?)",
                book.getName(), book.getAuthor(), book.getPublicationYear());
    }

    public List<Book> findAll() {
        return jdbcTemplate.query("SELECT * FROM books ORDER BY id ASC", BOOK_ROW_MAPPER);
    }

    public Optional<Book> findByName(String name) {
        return jdbcTemplate.query("SELECT * FROM books WHERE name = ?", BOOK_ROW_MAPPER, name)
                .stream()
                .findFirst();
    }
}
