package com.example.demo.controller;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/users")
/**
 * User profile and administrator endpoints.
 *
 * Authentication is enforced by SecurityConfig before these methods run.
 * Administrator status is read from the database rather than accepted from
 * the request body, so a regular user cannot promote themselves.
 *
 * API contract: docs/openapi.yaml
 */
public class UserController {
    private static final String USER_NOT_FOUND = "User not found";
    private final UserRepository repository;
    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/me")
    /** Uses the verified JWT subject to retrieve the caller's database record. */
    public User getCurrentUser(Authentication authentication) {
        return repository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }

    @GetMapping
    /** Lists users only after confirming that the caller is an administrator. */
    public Page<User> listUsers(Pageable pageable, Authentication authentication) {
        requireAdmin(authentication);
        return repository.findAll(pageable);
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable Long userId, Authentication authentication) {
        requireAdmin(authentication);
        return repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }

    @PatchMapping("/{userId}")
    public User updateUserRole(@PathVariable Long userId, @RequestBody User updates, Authentication authentication) {
        requireAdmin(authentication);
        User user = repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
        user.setAdmin(updates.isAdmin());
        return repository.save(user);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId, Authentication authentication) {
        requireAdmin(authentication);
        if (!repository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
        }
        repository.deleteById(userId);
    }

    private void requireAdmin(Authentication authentication) {
        boolean admin = repository.findByUsername(authentication.getName())
                .map(User::isAdmin)
                .orElse(false);
        if (!admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
        }
    }
}
