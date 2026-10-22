package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Encapsulates user lookup and user-record operations that can be tested
 * without starting the web layer.
 *
 * The controller remains responsible for authentication and administrator
 * authorization. This service is responsible for the repository operation
 * and the HTTP-visible not-found behavior defined by the API.
 *
 * Spring service documentation:
 * https://docs.spring.io/spring-framework/reference/core/beans/classpath-scanning.html
 *
 * Codex was used to generate this file from the existing UserServiceTest
 * and to document the repository behavior it exercises. The generated work
 * covers the service implementation, error handling, and supporting comments.
 */
@Service
public class UserService {
    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns a user by database ID.
     *
     * @param userId the database identifier to look up
     * @return the matching user
     * @throws ResponseStatusException with HTTP 404 when no user exists
     */
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }

    /**
     * Applies the administrator flag from an update request to an existing user.
     *
     * @param userId the user whose role is being changed
     * @param updates the requested role values
     * @return the saved user
     * @throws ResponseStatusException with HTTP 404 when no user exists
     */
    public User updateUserRole(Long userId, User updates) {
        User user = getUser(userId);
        user.setAdmin(updates.isAdmin());
        return userRepository.save(user);
    }
  
    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Deletes a user only when the requested record exists.
     *
     * @param userId the database identifier to delete
     * @throws ResponseStatusException with HTTP 404 when no user exists
     */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }
}
