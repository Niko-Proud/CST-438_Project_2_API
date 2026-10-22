package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.ThreadLocalRandom;

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
    private final JwtDecoder jwtDecoder;

    public UserService(UserRepository userRepository, JwtDecoder jwtDecoder) {
        this.userRepository = userRepository;
        this.jwtDecoder = jwtDecoder;
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
     * Finds an existing Google-linked account or creates a normie.
     * New usernames combine Google's given name with a four-digit number.
     * Existing usernames and admin privileges remain unchanged.
     */
    @Transactional
    public User connectGoogleAccount(String googleSub, String givenName) {
        if (googleSub == null || googleSub.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Google account identifier is missing"
            );
        }

        return userRepository.findByGoogleSub(googleSub)
                .orElseGet(() -> {
                    String firstName =
                            givenName == null || givenName.isBlank()
                                    ? "User"
                                    : givenName.strip();

                    // Bound the attempts so username selection cannot loop forever.
                    for (int attempt = 0; attempt < 25; attempt++) {
                        int number = ThreadLocalRandom.current().nextInt(10000);
                        String username = firstName
                                + String.format(java.util.Locale.ROOT, "%04d", number);

                        if (userRepository.findByUsername(username).isPresent()) {
                            continue;
                        }

                        User user = new User();
                        user.setGoogleSub(googleSub);
                        user.setUsername(username);
                        user.setAdmin(false);

                        return userRepository.save(user);
                    }

                    throw new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "Could not assign a username. Please try again."
                    );
                });
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
