package org.example.bookapp.model;

import jakarta.persistence.*;
import org.example.bookapp.exception.InvalidAuthorException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Long version;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @OneToMany(
            mappedBy = "author",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Book> books = new ArrayList<>();

    public Author() {
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

    public void setId(Integer id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
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

    public void addBook(Book book) {
        books.add(book);
        book.setAuthor(this);
    }

    public void removeBook(Book book) {
        books.remove(book);
        book.setAuthor(null);
    }

    public Long getVersion() {
        return version;
    }

}
