package org.example.bookapp.model;

public class Book {

    private Integer id;
    private String name;
    private Integer publicationYear;
    private Integer authorId;

    public Book() {
    }

    public Book(Integer id,
                String name,
                Integer publicationYear,
                Integer authorId) {
        this.id = id;
        this.name = name;
        this.publicationYear = publicationYear;
        this.authorId = authorId;
    }

    public Book(String name, Integer publicationYear) {
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

    public Integer getAuthorId() {
        return authorId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %d, Name: %s, Publication year: %d, Author ID: %d",
                id,
                name,
                publicationYear,
                authorId
        );
    }
}
