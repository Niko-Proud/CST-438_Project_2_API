package com.example.demo.controller;
import com.example.demo.model.RegistryItem;
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
        return repository.findById(itemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }
}