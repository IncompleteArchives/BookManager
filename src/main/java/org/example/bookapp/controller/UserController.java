package org.example.bookapp.controller;

import org.example.bookapp.dto.RegisterUserRequest;
import org.example.bookapp.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody RegisterUserRequest request) {

        userService.register(request.getUsername(), request.getPassword());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

}
