package com.example.demo.controller;
import com.example.demo.entity.RegistryItem;
import com.example.demo.repository.RegistryItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/registry")
public class RegistryController {
    private final RegistryItemRepository repository;
    public RegistryController(RegistryItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Page<RegistryItem> getMyRegistry(Pageable pageable) {
        Long mockUserId = 1L; 
        return repository.findByUserId(mockUserId, pageable);
    }

    @GetMapping("/{itemId}")
    public RegistryItem getRegistryItem(@PathVariable Long itemId) {
        Long mockUserId = 1L;
        return registryService.getItem(itemId, mockUserId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistryItem addItem(@RequestBody RegistryItem newItem) {
        Long mockUserId = 1L;
        return registryService.createItem(mockUserId, newItem);
    }

    @PutMapping("/{itemId}")
    public RegistryItem replaceItem(@PathVariable Long itemId, @RequestBody RegistryItem replacement) {
        Long mockUserId = 1L;
        return registryService.replaceItem(itemId, mockUserId, replacement);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long itemId) {
        Long mockUserId = 1L;
        registryService.deleteItem(itemId, mockUserId);
    }
}