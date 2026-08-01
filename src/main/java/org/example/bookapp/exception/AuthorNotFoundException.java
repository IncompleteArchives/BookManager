package org.example.bookapp.exception;

public class AuthorNotFoundException extends AuthorException {

    public AuthorNotFoundException() {
        super("author not found");
    }

}
