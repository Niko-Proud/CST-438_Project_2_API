package com.example.demo.repository;
import com.example.demo.entity.RegistryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistryItemRepository extends JpaRepository<RegistryItem, Long> {
    Page<RegistryItem> findByUserId(Long userId, Pageable pageable);
}