package com.example.demo.service;
import com.example.demo.model.RegistryItem;
import com.example.demo.model.User;
import com.example.demo.repository.RegistryItemRepository;
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
public class RegistryServiceTest {
    @Mock
    private RegistryItemRepository registryItemRepository;

    @InjectMocks
    private RegistryService registryService;

    @Test
    public void deleteItem_ThrowsForbidden_WhenUserDoesNotOwnItem() {
        User owner = new User();
        owner.setId(1L);
        
        RegistryItem item = new RegistryItem();
        item.setId(10L);
        item.setUser(owner);
        Long maliciousRequesterId = 2L;

        when(registryItemRepository.findById(10L)).thenReturn(Optional.of(item));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            registryService.deleteRegistryItem(10L, maliciousRequesterId);
        });
        
        assertEquals(403, exception.getStatusCode().value());
        verify(registryItemRepository, never()).deleteById(anyLong());
    }

    @Test
    public void addItem_ThrowsBadRequest_WhenProductIsEmpty() {
        User user = new User();
        user.setId(1L);

        RegistryItem blankItem = new RegistryItem();
        blankItem.setProduct(""); 
        blankItem.setUser(user);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            registryService.addRegistryItem(blankItem);
        });

        assertEquals(400, exception.getStatusCode().value());
        verify(registryItemRepository, never()).save(any(RegistryItem.class));
    }

    @Test
    public void getItem_ReturnsItem_WhenUserIsOwner() {
        User owner = new User();
        owner.setId(1L);
        
        RegistryItem item = new RegistryItem();
        item.setId(15L);
        item.setProduct("Organic Spinach");
        item.setUser(owner);

        when(registryItemRepository.findById(15L)).thenReturn(Optional.of(item));
        RegistryItem result = registryService.getRegistryItem(15L, 1L);

        assertNotNull(result);
        assertEquals("Organic Spinach", result.getProduct());
    }
}
