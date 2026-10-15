package com.example.demo.controller;
import com.example.demo.entity.RecalledFood;
import com.example.demo.service.RecallService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
public class RecallController {
    private final RecallService recallService;

    public RecallController(RecallService recallService) {
        this.recallService = recallService;
    }

    @GetMapping("/recalls")
    public Page<RecalledFood> getRecalls(
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String contaminant,
            @RequestParam(required = false) String batch,
            @RequestParam(required = false) LocalDate recallDate, Pageable pageable) {
        return recallService.getFilteredRecalls(product, contaminant, batch, recallDate, pageable);
    }

    @GetMapping("/{recallId}")
    public RecalledFood getRecallById(@PathVariable Long recallId) {
        return recallService.getRecall(recallId);
    }

    @PostMapping("/registry/{registryItemId}")
    @ResponseStatus(HttpStatus.CREATED)
    public RecalledFood addRecallToRegistry(@PathVariable Long registryItemId, @RequestBody RecalledFood recall) {
        Long mockUserId = 1L;
        return recallService.addRecallToRegistry(registryItemId, mockUserId, recall);
    }

    @DeleteMapping("/{recallId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecall(@PathVariable Long recallId) {
        Long mockUserId = 1L;
        recallService.deleteRecall(recallId, mockUserId);
    }
}
