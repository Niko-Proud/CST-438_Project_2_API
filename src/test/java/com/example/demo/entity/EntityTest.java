package com.example.demo.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for entity state used by the controllers and repositories.
 * These tests validate Java-side state without requiring PostgreSQL.
 */
class EntityTest {
    @Test
    void userStoresIdentityAndAdminState() {
        User user = new User();

        user.setId(1L);
        user.setUsername("user@example.com");
        user.setAdmin(true);

        assertEquals(1L, user.getId());
        assertEquals("user@example.com", user.getUsername());
        assertTrue(user.isAdmin());
    }

    @Test
    void registryItemStoresProductOwnerAndRecallCollection() {
        User user = new User();
        RegistryItem item = new RegistryItem();
        ArrayList<RecalledFood> recalls = new ArrayList<>();

        item.setId(2L);
        item.setUser(user);
        item.setProduct("Cereal");
        item.setBatch("B-100");
        item.setRecalledFoods(recalls);

        assertEquals(2L, item.getId());
        assertEquals(user, item.getUser());
        assertEquals("Cereal", item.getProduct());
        assertEquals("B-100", item.getBatch());
        assertEquals(recalls, item.getRecalledFoods());
    }

    @Test
    void recalledFoodStoresRecallDetails() {
        RecalledFood recall = new RecalledFood();
        LocalDate date = LocalDate.of(2026, 10, 18);

        recall.setId(3L);
        recall.setProduct("Cereal");
        recall.setContaminant("Undeclared allergen");
        recall.setRecallDate(date);
        recall.setManuDate(date.minusDays(10));
        recall.setBatch("B-100");

        assertEquals(3L, recall.getId());
        assertEquals("Cereal", recall.getProduct());
        assertEquals("Undeclared allergen", recall.getContaminant());
        assertEquals(date, recall.getRecallDate());
        assertEquals(date.minusDays(10), recall.getManuDate());
        assertEquals("B-100", recall.getBatch());
    }
}
