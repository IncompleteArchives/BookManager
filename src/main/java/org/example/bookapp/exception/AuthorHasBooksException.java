package org.example.bookapp.exception;

public class AuthorHasBooksException extends AuthorException {

    public AuthorHasBooksException() {
        super("author has books");
    }

}
