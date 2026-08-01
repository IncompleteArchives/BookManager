package org.example.bookapp.model;

import org.example.bookapp.exception.InvalidAuthorException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Author {

    private Integer id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate birthDate;
    private List<Book> books = new ArrayList<>();

    public Author() {
    }

    public Author(Integer id, String firstName, String middleName, String lastName, String gender, LocalDate birthDate) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public Author(String firstName, String middleName, String lastName, String gender, LocalDate birthDate) {

        if (firstName == null || firstName.isBlank()) {
            throw new InvalidAuthorException("First name cannot be null or blank");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new InvalidAuthorException("Last name cannot be null or blank");
        }

        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new InvalidAuthorException("BirthDate cannot be in the future");
        }

        if (gender != null
                && !gender.equalsIgnoreCase("M")
                && !gender.equalsIgnoreCase("F")) {
            throw new InvalidAuthorException("Gender must be 'M' or 'F'");
        }

        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
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

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %d, Full name: %s %s %s, Gender: %s, Birth date: %s",
                id,
                firstName,
                middleName,
                lastName,
                gender,
                birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        );
    }
}
