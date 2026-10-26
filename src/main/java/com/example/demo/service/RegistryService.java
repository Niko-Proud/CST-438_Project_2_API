package com.example.demo.service;

import com.example.demo.entity.RegistryItem;
import com.example.demo.entity.User;
import com.example.demo.repository.RegistryItemRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Application rules for a user's food registry.
 *
 * The controller supplies the subject from the verified bearer token. This
 * service resolves that subject to a database user and never accepts an owner
 * ID from request JSON. Spring Data binds repository arguments as SQL
 * parameters, so these lookups do not concatenate user input into SQL.
 * User ownership lookup accepts the Google subject first and retains the
 * legacy username fallback for records created before OAuth linking.
 * Codex assisted with this identity-lookup adjustment and its documentation.
 *
 * Service-layer guidance:
 * https://docs.spring.io/spring-framework/reference/core/beans/classpath-scanning.html
 */
@Service
public class RegistryService {
    private static final String ITEM_NOT_FOUND = "Item not found";

    private final RegistryItemRepository registryItemRepository;
    private final UserRepository userRepository;

    public RegistryService(RegistryItemRepository registryItemRepository, UserRepository userRepository) {
        this.registryItemRepository = registryItemRepository;
        this.userRepository = userRepository;
    }

    public Page<RegistryItem> getMyRegistry(String username, Pageable pageable) {
        User owner = findUser(username);
        return registryItemRepository.findByUser_Id(owner.getId(), pageable);
    }

    public RegistryItem createItem(String username, RegistryItem newItem) {
        validateItem(newItem);
        newItem.setUser(findUser(username));
        return registryItemRepository.save(newItem);
    }

    public RegistryItem getItem(String username, Long itemId) {
        return ownedItem(username, itemId);
    }

    public RegistryItem replaceItem(String username, Long itemId, RegistryItem replacement) {
        validateItem(replacement);
        RegistryItem existing = ownedItem(username, itemId);
        existing.setProduct(replacement.getProduct());
        existing.setBatch(replacement.getBatch());
        return registryItemRepository.save(existing);
    }

    public void deleteItem(String username, Long itemId) {
        registryItemRepository.delete(ownedItem(username, itemId));
    }

    private User findUser(String username) {
        return userRepository.findByGoogleSub(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private RegistryItem ownedItem(String username, Long itemId) {
        User owner = findUser(username);
        return registryItemRepository.findByIdAndUser_Id(itemId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND));
    }

    private void validateItem(RegistryItem item) {
        if (item == null || item.getProduct() == null
                || item.getProduct().isBlank() || item.getProduct().length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product must contain between 1 and 200 characters");
        }
        if (item.getBatch() != null && item.getBatch().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Batch must contain no more than 100 characters");
        }
    }
}
