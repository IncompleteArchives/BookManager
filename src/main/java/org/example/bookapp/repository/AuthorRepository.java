package org.example.bookapp.repository;

import org.example.bookapp.model.Author;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {

    @Query("""
        SELECT a
        FROM Author a
        LEFT JOIN FETCH a.books
        WHERE a.id = :id
        """)
    Optional<Author> findByIdWithBooks(@Param("id") Integer id);

}