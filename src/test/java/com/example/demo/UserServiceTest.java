package com.example.demo.service;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void getUser_ThrowsNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.getUser(99L);
        });
        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    public void updateUserRole_UpdatesAndSaves_WhenUserExists() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setAdmin(false);
        User updates = new User();
        updates.setAdmin(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUserRole(1L, updates);
        assertTrue(result.isAdmin());
        verify(userRepository).save(existingUser);
    }

    @Test
    public void deleteUser_ThrowsNotFound_WhenUserDoesNotExist() {
        when(userRepository.existsById(99L)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.deleteUser(99L);
        });

        assertEquals(404, exception.getStatusCode().value());
        verify(userRepository, never()).deleteById(anyLong());
    }
}