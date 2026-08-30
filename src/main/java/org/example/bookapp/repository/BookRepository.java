package org.example.bookapp.repository;

import jakarta.persistence.LockModeType;

import org.example.bookapp.model.Book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Integer id);

}