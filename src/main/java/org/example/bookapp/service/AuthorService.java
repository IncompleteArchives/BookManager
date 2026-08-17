package org.example.bookapp.service;

import org.example.bookapp.exception.AuthorHasBooksException;
import org.example.bookapp.exception.AuthorNotFoundException;
import org.example.bookapp.exception.DatabaseOperationException;
import org.example.bookapp.model.Author;
import org.example.bookapp.model.Book;
import org.example.bookapp.repository.AuthorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {

    private final AuthorRepository repository;

    @Autowired
    public AuthorService(AuthorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Optional<Author> findById(Integer id) {
        try {
            return repository.findByIdWithBooks(id);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to retrieve author", e);
        }
    }

    @Transactional
    public void deleteAuthor(Integer id) {
        try {
            Author author = repository.findById(id).orElseThrow(AuthorNotFoundException::new);

            if (!author.getBooks().isEmpty()) throw new AuthorHasBooksException();

            repository.delete(author);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to delete author", e);
        }
    }

    @Transactional
    public Integer addAuthorWithBooks(Author author, List<Book> books) {
        try {
            for (Book book : books) {
                author.addBook(book);
            }

            return repository.save(author).getId();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to insert author with books", e);
        }
    }

    public Integer addAuthor(Author author) {
        try {
            return repository.save(author).getId();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to insert author", e);
        }
    }

    @Transactional
    public void updateAuthor(Integer id, Author updatedAuthor) {
        try {
            Author author = repository.findById(id).orElseThrow(AuthorNotFoundException::new);

            author.setFirstName(updatedAuthor.getFirstName());
            author.setMiddleName(updatedAuthor.getMiddleName());
            author.setLastName(updatedAuthor.getLastName());
            author.setGender(updatedAuthor.getGender());
            author.setBirthDate(updatedAuthor.getBirthDate());
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to update author", e);
        }
    }

}