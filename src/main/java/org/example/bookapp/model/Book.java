package org.example.bookapp.model;

import jakarta.persistence.*;
import org.example.bookapp.exception.InvalidBookException;

import java.time.Year;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    public Book() {
    }

    public Book(String name, Integer publicationYear) {

        if (name == null || name.isBlank()) {
            throw new InvalidBookException("book name cannot be null or blank");
        }

        int currentYear = Year.now().getValue();
        if (publicationYear == null || publicationYear <= 0 || publicationYear > currentYear) {
            throw new InvalidBookException("publication year must be between 1 and " + currentYear);
        }

        this.name = name;
        this.publicationYear = publicationYear;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public Author getAuthor() {
        return author;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %d, Name: %s, Publication year: %d, Author ID: %d",
                id,
                name,
                publicationYear,
                author != null ? author.getId() : null
        );
    }
}
