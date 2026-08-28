package org.example.bookapp.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException() {
        super("book not found");
    }

}