package com.example.demo.controller;
import com.example.demo.entity.RecalledFood;
import com.example.demo.repository.RecalledFoodRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
public class RecallController {
    private final RecalledFoodRepository repository;

    public RecallController(RecalledFoodRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/recalls")
    public Page<RecalledFood> getRecalls(
        @RequestParam(required = false) String product,
        @RequestParam(required = false) String contaminant,
        @RequestParam(required = false) String batch,
        @RequestParam(required = false) LocalDate recallDate, Pageable pageable) {

        if (product != null) {
            return repository.findByProductContainingIgnoreCase(product, pageable);
        } 
        else if (contaminant != null) {
            return repository.findByContaminantContainingIgnoreCase(contaminant, pageable);
        } 
        else if (batch != null) {
            return repository.findByBatch(batch, pageable);
        } 
        else if (recallDate != null) {
            return repository.findByRecallDate(recallDate, pageable);
        }

        return repository.findAll(pageable);
    }

    @GetMapping("/recalls/{recallId}")
    public RecalledFood getRecallById(@PathVariable Long recallId) {
        return repository.findById(recallId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recall not found"));
    }
}
