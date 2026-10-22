package com.example.demo.repository;
import com.example.demo.entity.RegistryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistryItemRepository extends JpaRepository<RegistryItem, Long> {
    Page<RegistryItem> findByUserId(Long userId, Pageable pageable);

    // Spring Data binds these values as query parameters; no SQL is assembled
    // from request strings, preventing injection through ownership lookups.
    Optional<RegistryItem> findByIdAndUserUsername(Long id, String username);
}
