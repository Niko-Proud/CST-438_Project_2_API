package com.example.demo.service;
import com.example.demo.entity.RegistryItem;
import com.example.demo.entity.User;
import com.example.demo.repository.RegistryItemRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistryService {
    private final RegistryItemRepository registryItemRepository;
    private final UserRepository userRepository;

    public RegistryService(RegistryItemRepository registryItemRepository, UserRepository userRepository) {
        this.registryItemRepository = registryItemRepository;
        this.userRepository = userRepository;
    }

    public RegistryItem createItem(Long userId, RegistryItem newItem) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        newItem.setUser(owner);
        return registryItemRepository.save(newItem);
    }

    public RegistryItem getItem(Long itemId, Long requesterId) {
        RegistryItem item = registryItemRepository.findById(itemId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        
        if (!item.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this item");
        }
        return item;
    }

    public RegistryItem replaceItem(Long itemId, Long requesterId, RegistryItem replacement) {
        RegistryItem existing = getItem(itemId, requesterId);
        existing.setProduct(replacement.getProduct());
        existing.setBatch(replacement.getBatch());
        return registryItemRepository.save(existing);
    }

    public void deleteItem(Long itemId, Long requesterId) {
        RegistryItem existing = getItem(itemId, requesterId);
        registryItemRepository.delete(existing);
    }
}
