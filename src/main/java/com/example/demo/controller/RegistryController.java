package com.example.demo.controller;

import com.example.demo.entity.RegistryItem;
import com.example.demo.service.RegistryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registry")
public class RegistryController {
    private final RegistryService registryService;

    public RegistryController(RegistryService registryService) {
        this.registryService = registryService;
    }

    @GetMapping
    public Page<RegistryItem> getMyRegistry(Pageable pageable, Authentication authentication) {
        return registryService.getMyRegistry(authentication.getName(), pageable);
    }

    @GetMapping("/{itemId}")
    public RegistryItem getRegistryItem(@PathVariable Long itemId, Authentication authentication) {
        return registryService.getItem(authentication.getName(), itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistryItem addItem(@RequestBody RegistryItem newItem, Authentication authentication) {
        return registryService.createItem(authentication.getName(), newItem);
    }

    @PutMapping("/{itemId}")
    public RegistryItem replaceItem(@PathVariable Long itemId, @RequestBody RegistryItem replacement,
            Authentication authentication) {
        return registryService.replaceItem(authentication.getName(), itemId, replacement);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long itemId, Authentication authentication) {
        registryService.deleteItem(authentication.getName(), itemId);
    }
}
