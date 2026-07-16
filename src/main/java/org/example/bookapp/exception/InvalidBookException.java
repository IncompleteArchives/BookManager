package org.example.bookapp.exception;

public class InvalidBookException extends RuntimeException {

    public InvalidBookException(String message) {
        super(message);
    }

}
