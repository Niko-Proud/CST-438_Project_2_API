package com.example.demo.service;
import com.example.demo.entity.RecalledFood;
import com.example.demo.entity.RegistryItem;
import com.example.demo.repository.RecalledFoodRepository;
import com.example.demo.repository.RegistryItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
public class RecallService {
    private final RecalledFoodRepository recalledFoodRepository;
    private final RegistryItemRepository registryItemRepository;

    public RecallService(RecalledFoodRepository recalledFoodRepository, RegistryItemRepository registryItemRepository) {
        this.recalledFoodRepository = recalledFoodRepository;
        this.registryItemRepository = registryItemRepository;
    }

    public Page<RecalledFood> listRecalls(Pageable pageable) {
        return recalledFoodRepository.findAll(pageable);
    }

    public RecalledFood getRecall(Long recallId) {
        return recalledFoodRepository.findById(recallId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recall not found"));
    }

    public RecalledFood addRecallToRegistry(Long registryItemId, Long requesterId, RecalledFood recall) {
        RegistryItem item = registryItemRepository.findById(registryItemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registry item not found"));

        if (!item.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this registry item");
        }
        recall.setRegistryItem(item);
        return recalledFoodRepository.save(recall);
    }

    public void deleteRecall(Long recallId, Long requesterId) {
        RecalledFood existingRecall = getRecall(recallId);
        if (!existingRecall.getRegistryItem().getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this recall");
        }
        recalledFoodRepository.delete(existingRecall);
    }

    public Page<RecalledFood> getFilteredRecalls(String product, String contaminant, String batch, LocalDate recallDate, Pageable pageable) {
        if (product != null) {
            return recalledFoodRepository.findByProductContainingIgnoreCase(product, pageable);
        }
        else if (contaminant != null) {
            return recalledFoodRepository.findByContaminantContainingIgnoreCase(contaminant, pageable);
        }
        else if (batch != null) {
            return recalledFoodRepository.findByBatch(batch, pageable);
        }
        else if (recallDate != null) {
            return recalledFoodRepository.findByRecallDate(recallDate, pageable);
        }
        return recalledFoodRepository.findAll(pageable);
    }
}
