package org.example.bookapp.dto;

import org.example.bookapp.model.Author;

import java.time.LocalDate;
import java.util.List;

public class AuthorResponse {

    private Integer id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate birthDate;
    private List<BookResponse> books;

    public AuthorResponse() {
    }

    public AuthorResponse(Author author) {
        this.id = author.getId();
        this.firstName = author.getFirstName();
        this.middleName = author.getMiddleName();
        this.lastName = author.getLastName();
        this.gender = author.getGender();
        this.birthDate = author.getBirthDate();

        this.books = author.getBooks()
                .stream()
                .map(BookResponse::new)
                .toList();
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public List<BookResponse> getBooks() {
        return books;
    }

}