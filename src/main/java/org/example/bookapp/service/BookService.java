package org.example.bookapp.service;

import org.example.bookapp.exception.InvalidBookException;
import org.example.bookapp.model.Book;
import org.example.bookapp.repository.BookRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    @Autowired
    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public void addBook(String name, String author, Integer publicationYear) {

        if (name == null || name.isBlank()) {
            throw new InvalidBookException("book name cannot be null or blank");
        }

        int currentYear = Year.now().getValue();
        if (publicationYear == null || publicationYear <= 0 || publicationYear > currentYear) {
            throw new InvalidBookException("publication year must be between 1 and " + currentYear);
        }

        Book book = new Book();
        book.setName(name);
        book.setAuthor(author);
        book.setPublicationYear(publicationYear);

        repository.add(book);

    }

    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    public Optional<Book> findBook(String name) {
        return repository.findByName(name);
    }

}
