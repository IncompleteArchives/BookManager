package org.example.bookapp.service;

import org.example.bookapp.exception.AuthorHasBooksException;
import org.example.bookapp.exception.AuthorNotFoundException;
import org.example.bookapp.exception.DatabaseOperationException;
import org.example.bookapp.model.Author;
import org.example.bookapp.model.Book;
import org.example.bookapp.repository.AuthorRepository;
import org.example.bookapp.repository.BookRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {

    private final AuthorRepository repository;
    private final BookRepository bookRepository;

    @Autowired
    public AuthorService(AuthorRepository repository, BookRepository bookRepository) {
        this.repository = repository;
        this.bookRepository = bookRepository;
    }

    public Optional<Author> findById(Integer id) {

        try {
            Optional<Author> author = repository.findById(id);

            if (author.isEmpty()) return Optional.empty();

            List<Book> books = bookRepository.findByAuthorId(id);
            author.get().setBooks(books);

            return author;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to retrieve author", e);
        }
    }

    public void deleteAuthor(Integer id) {

        try {
            if (repository.findById(id).isEmpty()) throw new AuthorNotFoundException();
            if (!bookRepository.findByAuthorId(id).isEmpty()) throw new AuthorHasBooksException();

            repository.delete(id);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to delete author", e);
        }

    }

    @Transactional
    public void addAuthorWithBooks(Author author, List<Book> books) {

        try {
            Integer authorId = repository.add(author);

            for (Book book : books) {
                book.setAuthorId(authorId);
                bookRepository.add(book);
            }
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to insert author with books", e);
        }

    }

    public Integer addAuthor(Author author) {

        try {
            return repository.add(author);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to insert author", e);
        }
    }
}