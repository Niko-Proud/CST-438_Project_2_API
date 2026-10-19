package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import com.example.demo.entity.RecalledFood;
import com.example.demo.entity.RegistryItem;
import com.example.demo.entity.User;
import com.example.demo.repository.RecalledFoodRepository;
import com.example.demo.repository.RegistryItemRepository;
import com.example.demo.repository.UserRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration smoke test for the complete Spring application context.
 *
 * Testcontainers starts a temporary PostgreSQL database for this test class.
 * Spring Boot's @ServiceConnection supplies the container's connection details
 * to JPA and Flyway, so V1__initial_schema.sql is applied before the context
 * is checked. The container is removed when the test lifecycle finishes.
 *
 * Documentation:
 * https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
 */
@Testcontainers
@SpringBootTest
class DemoApplicationTests {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RegistryItemRepository registryItemRepository;

	@Autowired
	private RecalledFoodRepository recalledFoodRepository;

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres =
			new PostgreSQLContainer("postgres:17-alpine");

	@Test
	void contextLoads() {
	}

	/**
	 * Confirms that Flyway created the schema that the JPA entities use and that
	 * the three related database records can be persisted and read back.
	 */
	@Test
	void migrationSupportsUserRegistryItemAndRecallPersistence() {
		User user = new User();
		user.setUsername("integration-test@example.com");
		user.setAdmin(false);
		User savedUser = userRepository.saveAndFlush(user);

		RegistryItem item = new RegistryItem();
		item.setUser(savedUser);
		item.setProduct("Organic Spinach");
		item.setBatch("TEST-BATCH");
		RegistryItem savedItem = registryItemRepository.saveAndFlush(item);

		RecalledFood recall = new RecalledFood();
		recall.setProduct("Organic Spinach");
		recall.setContaminant("Salmonella");
		recall.setRecallDate(LocalDate.of(2026, 10, 2));
		recall.setManuDate(LocalDate.of(2026, 9, 18));
		recall.setBatch("TEST-BATCH");
		recalledFoodRepository.saveAndFlush(recall);

		assertTrue(userRepository.findById(savedUser.getId()).isPresent());
		assertEquals("Organic Spinach", registryItemRepository
				.findById(savedItem.getId()).orElseThrow().getProduct());
		assertEquals("Salmonella", recalledFoodRepository
				.findById(recall.getId()).orElseThrow().getContaminant());
	}

}
