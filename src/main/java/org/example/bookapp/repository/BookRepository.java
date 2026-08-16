package org.example.bookapp.repository;

import org.example.bookapp.model.Book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findByNameContaining(String name);

    @Query("""
            SELECT b
            FROM Book b
            WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))
                AND b.author.id = :authorId
            """)
    List<Book> findByNameContainingAndAuthor_Id(@Param("name") String name,
                                                @Param("authorId") Integer authorId);

}