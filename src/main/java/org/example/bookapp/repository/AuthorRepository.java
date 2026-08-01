package org.example.bookapp.repository;

import org.example.bookapp.model.Author;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AuthorRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<Author> AUTHOR_ROW_MAPPER =
            (resultSet, rowNum) -> new Author(
                    resultSet.getInt("id"),
                    resultSet.getString("first_name"),
                    resultSet.getString("middle_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("gender"),
                    resultSet.getDate("birth_date") == null ? null : resultSet.getDate("birth_date").toLocalDate()
            );

    @Autowired
    public AuthorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer add(Author author) {
        return jdbcTemplate.queryForObject(
                """
                        INSERT INTO authors
                        (first_name, middle_name, last_name, gender, birth_date)
                        VALUES (?, ?, ?, ?, ?)
                        RETURNING id
                        """,
                Integer.class,
                author.getFirstName(),
                author.getMiddleName(),
                author.getLastName(),
                author.getGender(),
                author.getBirthDate()
        );
    }

    public void delete(Integer id) {
        jdbcTemplate.update("DELETE FROM authors WHERE id = ?", id);
    }

    public Optional<Author> findById(Integer id) {
        return jdbcTemplate.query("SELECT * FROM authors WHERE id = ?", AUTHOR_ROW_MAPPER, id)
                .stream()
                .findFirst();
    }

}
