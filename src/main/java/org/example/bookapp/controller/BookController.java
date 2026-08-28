package org.example.bookapp.controller;

import org.example.bookapp.dto.BookRequest;
import org.example.bookapp.dto.BookResponse;
import org.example.bookapp.dto.TransferBookRequest;
import org.example.bookapp.model.Book;
import org.example.bookapp.service.BookService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @RequestBody BookRequest request) {

        Book savedBook = bookService.addBook(
                request.getName(),
                request.getPublicationYear(),
                request.getAuthorId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new BookResponse(savedBook));
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> findBooksByName(
            @RequestParam(name = "name") String name) {

        List<BookResponse> books = bookService.findByNameContains(name)
                .stream()
                .map(BookResponse::new)
                .toList();

        return ResponseEntity.ok(books);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> transferBook(
            @PathVariable(name = "id") Integer bookId,
            @RequestBody TransferBookRequest request) {

        bookService.transferBook(bookId, request.getAuthorId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable(name = "id") Integer id) {

        bookService.deleteBook(id);

        return ResponseEntity.noContent().build();
    }

}