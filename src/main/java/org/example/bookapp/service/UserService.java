package org.example.bookapp.service;

import org.example.bookapp.exception.DatabaseOperationException;
import org.example.bookapp.exception.InvalidUserException;
import org.example.bookapp.exception.UsernameAlreadyExistsException;
import org.example.bookapp.model.Role;
import org.example.bookapp.model.User;
import org.example.bookapp.repository.UserRepository;

import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String username, String password) {

        if (username == null || username.isBlank()) {
            throw new InvalidUserException("Username cannot be null or blank");
        }

        if (password == null || password.isBlank()) {
            throw new InvalidUserException("Password cannot be null or blank");
        }

        try {
            if (userRepository.existsByUsername(username)) {
                throw new UsernameAlreadyExistsException();
            }

            String encodedPassword = passwordEncoder.encode(password);

            User user = new User(username, encodedPassword, Role.USER);

            return userRepository.save(user);

        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to register user", e);
        }
    }

}
