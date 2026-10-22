package com.example.demo.controller;
import com.example.demo.entity.RecalledFood;
import com.example.demo.service.RecallService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
/**
 * Read-only recall endpoints. All recall data is available only to an
 * authenticated caller; filtering values are passed to Spring Data methods,
 * which bind them as query parameters rather than concatenating SQL.
 *
 * API contract: docs/openapi.yaml
 */
public class RecallController {
    private final RecallService recallService;

    public RecallController(RecallService recallService) {
        this.recallService = recallService;
    }

    @GetMapping("/recalls")
    /** Returns a page of recalls, optionally filtered by one supported field. */
    public Page<RecalledFood> getRecalls(
        @RequestParam(required = false) String product,
        @RequestParam(required = false) String contaminant,
        @RequestParam(required = false) String batch,
        @RequestParam(required = false) LocalDate recallDate, Pageable pageable,
        Authentication authentication) {
        return recallService.getFilteredRecalls(
                product, contaminant, batch, recallDate, pageable);
    }

    @GetMapping("/recalls/{recallId}")
    /** Returns one recall or the contract's not-found response. */
    public RecalledFood getRecallById(@PathVariable Long recallId, Authentication authentication) {
        return recallService.getRecall(recallId);
    }
}
