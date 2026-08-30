package org.example.bookapp.exception;

public class BookNotFoundException extends BookException {

    public BookNotFoundException() {
        super("book not found");
    }

}