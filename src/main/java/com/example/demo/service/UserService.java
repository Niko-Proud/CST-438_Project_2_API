package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
 * Codex also assisted with the first-login Google subject linking and the
 * authenticated self-delete flow added for the deployed API.
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
     * Finds the database user represented by a verified Google token, creating
     * the profile on first login when necessary. Only the stable token subject
     * is stored; the bearer token itself is never persisted.
     */
    public User getOrCreateFromAuthentication(Authentication authentication) {
        String googleSubject = getGoogleSubject(authentication);
        String username = getPreferredUsername(authentication, googleSubject);

        return userRepository.findByGoogleSub(googleSubject)
                .orElseGet(() -> userRepository.findByUsername(username)
                        .map(existingUser -> linkGoogleSubject(existingUser, googleSubject))
                        .orElseGet(() -> createGoogleUser(username, googleSubject)));
    }

    /** Deletes the authenticated user's account and cascaded registry records. */
    public void deleteCurrentUser(Authentication authentication) {
        String googleSubject = getGoogleSubject(authentication);
        String username = getPreferredUsername(authentication, googleSubject);

        User user = userRepository.findByGoogleSub(googleSubject)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
        userRepository.delete(user);
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

    private User linkGoogleSubject(User user, String googleSubject) {
        if (!StringUtils.hasText(user.getGoogleSub())) {
            user.setGoogleSub(googleSubject);
            return userRepository.save(user);
        }
        return user;
    }

    private User createGoogleUser(String username, String googleSubject) {
        User user = new User();
        user.setUsername(username);
        user.setGoogleSub(googleSubject);
        user.setAdmin(false);
        return userRepository.save(user);
    }

    private String getGoogleSubject(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt && StringUtils.hasText(jwt.getSubject())) {
            return jwt.getSubject();
        }
        if (StringUtils.hasText(authentication.getName())) {
            return authentication.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token subject is missing");
    }

    private String getPreferredUsername(Authentication authentication, String fallback) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            if (StringUtils.hasText(email)) {
                return email;
            }
        }
        return fallback;
    }
}
