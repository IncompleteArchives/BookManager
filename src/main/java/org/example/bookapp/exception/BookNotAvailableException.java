package org.example.bookapp.exception;

public class BookNotAvailableException extends BookException {

    public BookNotAvailableException() {
        super("No available copies");
    }

}
