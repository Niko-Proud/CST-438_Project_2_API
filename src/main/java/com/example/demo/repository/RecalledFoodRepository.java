package com.example.demo.repository;
import com.example.demo.entity.RecalledFood;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface RecalledFoodRepository extends JpaRepository<RecalledFood, Long> {
    Page<RecalledFood> findByProductContainingIgnoreCase(String product, Pageable pageable);
    Page<RecalledFood> findByContaminantContainingIgnoreCase(String contaminant, Pageable pageable);
    Page<RecalledFood> findByBatch(String batch, Pageable pageable);
    Page<RecalledFood> findByRecallDate(LocalDate recallDate, Pageable pageable);
}