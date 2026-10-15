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
        return repository.findById(itemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistryItem addItem(@RequestBody RegistryItem newItem) {
        return repository.save(newItem);
    }

    @PutMapping("/{itemId}")
    public RegistryItem replaceItem(@PathVariable Long itemId, @RequestBody RegistryItem replacement) {
        RegistryItem existing = repository.findById(itemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        existing.setProduct(replacement.getProduct());
        existing.setBatch(replacement.getBatch());
        return repository.save(existing);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long itemId) {
        if (!repository.existsById(itemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
        }
        repository.deleteById(itemId);
    }
}