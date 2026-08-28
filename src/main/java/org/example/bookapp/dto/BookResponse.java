package org.example.bookapp.dto;

import org.example.bookapp.model.Book;

public class BookResponse {

    private Integer id;
    private String name;
    private Integer publicationYear;
    private Integer authorId;

    public BookResponse() {
    }

    public BookResponse(Book book) {
        this.id = book.getId();
        this.name = book.getName();
        this.publicationYear = book.getPublicationYear();

        this.authorId = book.getAuthor() != null
                ? book.getAuthor().getId()
                : null;
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

}