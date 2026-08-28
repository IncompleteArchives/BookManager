package org.example.bookapp.controller;

import org.example.bookapp.dto.AuthorRequest;
import org.example.bookapp.dto.AuthorResponse;
import org.example.bookapp.model.Author;
import org.example.bookapp.service.AuthorService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(
            @RequestBody AuthorRequest request) {

        Author author = new Author(
                request.getFirstName(),
                request.getMiddleName(),
                request.getLastName(),
                request.getGender(),
                request.getBirthDate()
        );

        Author savedAuthor = authorService.addAuthor(author);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthorResponse(savedAuthor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(
            @PathVariable(name = "id") Integer id) {

        return authorService.findById(id)
                .map(author -> ResponseEntity.ok(new AuthorResponse(author)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(
            @PathVariable(name = "id") Integer id) {

        authorService.deleteAuthor(id);

        return ResponseEntity.noContent().build();
    }

}