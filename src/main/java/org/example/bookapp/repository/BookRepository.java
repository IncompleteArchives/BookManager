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
                    resultSet.getInt("publication_year"),
                    resultSet.getObject("author_id", Integer.class)
            );

    @Autowired
    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void add(Book book) {
        jdbcTemplate.update("INSERT INTO books(name, publication_year, author_id) VALUES (?, ?, ?)",
                book.getName(), book.getPublicationYear(), book.getAuthorId());
    }

    public List<Book> findAll() {
        return jdbcTemplate.query("SELECT * FROM books ORDER BY id ASC", BOOK_ROW_MAPPER);
    }

    public List<Book> findByNameContains(String name) {
        return jdbcTemplate.query("SELECT * FROM books WHERE name ILIKE ?", BOOK_ROW_MAPPER, "%" + name + "%");
    }

    public List<Book> findByAuthorId(Integer authorId) {
        return jdbcTemplate.query("SELECT * FROM books WHERE author_id = ? ORDER BY publication_year", BOOK_ROW_MAPPER, authorId);
    }

    public Optional<Book> findById(Integer id) {
        return jdbcTemplate.query("SELECT * FROM books WHERE id = ?", BOOK_ROW_MAPPER, id)
                .stream()
                .findFirst();
    }

    public int updateAuthor(Integer bookId, Integer authorId) {
        return jdbcTemplate.update("UPDATE books SET author_id = ? WHERE id = ?", authorId, bookId);
    }
}
