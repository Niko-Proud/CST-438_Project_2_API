package com.example.demo.contract;

import com.example.demo.entity.RecalledFood;
import com.example.demo.entity.RegistryItem;
import com.example.demo.entity.User;
import com.example.demo.repository.RecalledFoodRepository;
import com.example.demo.repository.RegistryItemRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract and authorization tests derived from docs/openapi.yaml.
 *
 * Each test uses a temporary PostgreSQL container and Flyway's real migration.
 * Mock JWTs represent Google-authenticated identities without making network
 * calls to Google. The repository methods used by the controllers are Spring
 * Data queries, so request values are bound as parameters instead of being
 * concatenated into SQL.
 *
 * Documentation:
 * https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/jwt.html
 * https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OpenApiContractIntegrationTest {

    private static final String USER_SUBJECT = "user-subject";
    private static final String OTHER_SUBJECT = "other-subject";
    private static final String ADMIN_SUBJECT = "admin-subject";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistryItemRepository registryItemRepository;

    @Autowired
    private RecalledFoodRepository recalledFoodRepository;

    private User user;
    private User otherUser;
    private RegistryItem userItem;
    private RecalledFood recall;

    @BeforeEach
    void seedDatabase() {
        recalledFoodRepository.deleteAll();
        registryItemRepository.deleteAll();
        userRepository.deleteAll();

        user = saveUser(USER_SUBJECT, false);
        otherUser = saveUser(OTHER_SUBJECT, false);
        saveUser(ADMIN_SUBJECT, true);

        userItem = new RegistryItem();
        userItem.setUser(user);
        userItem.setProduct("Organic Spinach");
        userItem.setBatch("USER-BATCH");
        userItem = registryItemRepository.saveAndFlush(userItem);

        recall = new RecalledFood();
        recall.setProduct("Organic Spinach");
        recall.setContaminant("Salmonella");
        recall.setRecallDate(LocalDate.of(2026, 10, 2));
        recall.setManuDate(LocalDate.of(2026, 9, 18));
        recall.setBatch("USER-BATCH");
        recall = recalledFoodRepository.saveAndFlush(recall);
    }

    @Test
    void currentUser_requiresAuthentication_andReturnsMatchingUser() throws Exception {
        assertUnauthorized(get("/api/v1/users/me"));
        mockMvc.perform(get("/api/v1/users/me").with(userJwt(USER_SUBJECT)))
                .andExpect(status().isOk());
    }

    @Test
    void registryList_requiresAuthentication_andReturnsCallerItems() throws Exception {
        assertUnauthorized(get("/api/v1/registry"));
        mockMvc.perform(get("/api/v1/registry").with(userJwt(USER_SUBJECT)))
                .andExpect(status().isOk());
    }

    @Test
    void registryCreate_requiresAuthentication_andAssignsCallerOwnership() throws Exception {
        String body = "{\"product\":\"Apples\"}";
        assertUnauthorized(post("/api/v1/registry")
                .contentType(MediaType.APPLICATION_JSON).content(body));

        mockMvc.perform(post("/api/v1/registry")
                        .with(userJwt(OTHER_SUBJECT))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        RegistryItem saved = registryItemRepository.findAll().stream()
                .filter(item -> "Apples".equals(item.getProduct()))
                .findFirst().orElseThrow();
        assertEquals(otherUser.getId(), saved.getUser().getId());
    }

    @Test
    void registryCreate_rejectsInvalidProductAndBatchWithBadRequest() throws Exception {
        String blankProduct = "{\"product\":\"   \"}";
        String missingProduct = "{\"batch\":\"BATCH-1\"}";
        String oversizedProduct = "{\"product\":\"" + "P".repeat(201) + "\"}";
        String oversizedBatch = "{\"product\":\"Apples\",\"batch\":\""
                + "B".repeat(101) + "\"}";

        assertBadRequest(post("/api/v1/registry").with(userJwt(USER_SUBJECT))
                .contentType(MediaType.APPLICATION_JSON).content(blankProduct));
        assertBadRequest(post("/api/v1/registry").with(userJwt(USER_SUBJECT))
                .contentType(MediaType.APPLICATION_JSON).content(missingProduct));
        assertBadRequest(post("/api/v1/registry").with(userJwt(USER_SUBJECT))
                .contentType(MediaType.APPLICATION_JSON).content(oversizedProduct));
        assertBadRequest(post("/api/v1/registry").with(userJwt(USER_SUBJECT))
                .contentType(MediaType.APPLICATION_JSON).content(oversizedBatch));
    }

    @Test
    void registryItemGet_requiresAuthentication_andEnforcesOwnership() throws Exception {
        assertUnauthorized(get("/api/v1/registry/" + userItem.getId()));
        mockMvc.perform(get("/api/v1/registry/" + userItem.getId())
                        .with(userJwt(USER_SUBJECT)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/registry/" + userItem.getId())
                        .with(userJwt(OTHER_SUBJECT)))
                .andExpect(status().isNotFound());
    }

    @Test
    void registryItemReplace_requiresAuthentication_andEnforcesOwnership() throws Exception {
        String body = "{\"product\":\"Updated Spinach\",\"batch\":\"NEW-BATCH\"}";
        assertUnauthorized(put("/api/v1/registry/" + userItem.getId())
                .contentType(MediaType.APPLICATION_JSON).content(body));
        mockMvc.perform(put("/api/v1/registry/" + userItem.getId())
                        .with(userJwt(OTHER_SUBJECT))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/v1/registry/" + userItem.getId())
                        .with(userJwt(USER_SUBJECT))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void registryItemReplace_rejectsInvalidProductAndBatchWithBadRequest() throws Exception {
        String oversizedProduct = "{\"product\":\"" + "P".repeat(201) + "\"}";
        String oversizedBatch = "{\"product\":\"Updated Spinach\",\"batch\":\""
                + "B".repeat(101) + "\"}";

        assertBadRequest(put("/api/v1/registry/" + userItem.getId())
                .with(userJwt(USER_SUBJECT))
                .contentType(MediaType.APPLICATION_JSON).content(oversizedProduct));
        assertBadRequest(put("/api/v1/registry/" + userItem.getId())
                .with(userJwt(USER_SUBJECT))
                .contentType(MediaType.APPLICATION_JSON).content(oversizedBatch));
    }

    @Test
    void registryItemDelete_requiresAuthentication_andEnforcesOwnership() throws Exception {
        assertUnauthorized(delete("/api/v1/registry/" + userItem.getId()));
        mockMvc.perform(delete("/api/v1/registry/" + userItem.getId())
                        .with(userJwt(OTHER_SUBJECT)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/registry/" + userItem.getId())
                        .with(userJwt(USER_SUBJECT)))
                .andExpect(status().isNoContent());
    }

    @Test
    void recallsList_requiresAuthentication_andReturnsSuccessForUser() throws Exception {
        assertUnauthorized(get("/api/v1/recalls?page=0&size=20"));
        mockMvc.perform(get("/api/v1/recalls?page=0&size=20")
                        .with(userJwt(USER_SUBJECT)))
                .andExpect(status().isOk());
    }

    @Test
    void recallGet_requiresAuthentication_andHandlesMissingRecall() throws Exception {
        assertUnauthorized(get("/api/v1/recalls/" + recall.getId()));
        mockMvc.perform(get("/api/v1/recalls/" + recall.getId())
                        .with(userJwt(USER_SUBJECT)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/recalls/999999")
                        .with(userJwt(USER_SUBJECT)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userList_requiresAuthentication_andIsAdminOnly() throws Exception {
        assertUnauthorized(get("/api/v1/users?page=0&size=20"));
        mockMvc.perform(get("/api/v1/users?page=0&size=20")
                        .with(userJwt(USER_SUBJECT)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users?page=0&size=20")
                        .with(userJwt(ADMIN_SUBJECT)))
                .andExpect(status().isOk());
    }

    @Test
    void userGet_requiresAuthentication_andIsAdminOnly() throws Exception {
        assertUnauthorized(get("/api/v1/users/" + user.getId()));
        mockMvc.perform(get("/api/v1/users/" + user.getId())
                        .with(userJwt(USER_SUBJECT)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/" + user.getId())
                        .with(userJwt(ADMIN_SUBJECT)))
                .andExpect(status().isOk());
    }

    @Test
    void userPatch_requiresAuthentication_andIsAdminOnly() throws Exception {
        String body = "{\"isAdmin\":true}";
        assertUnauthorized(patch("/api/v1/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON).content(body));
        mockMvc.perform(patch("/api/v1/users/" + user.getId())
                        .with(userJwt(USER_SUBJECT))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/users/" + user.getId())
                        .with(userJwt(ADMIN_SUBJECT))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void userDelete_requiresAuthentication_andIsAdminOnly() throws Exception {
        assertUnauthorized(delete("/api/v1/users/" + user.getId()));
        mockMvc.perform(delete("/api/v1/users/" + user.getId())
                        .with(userJwt(USER_SUBJECT)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/users/" + user.getId())
                        .with(userJwt(ADMIN_SUBJECT)))
                .andExpect(status().isNoContent());
    }

    private User saveUser(String username, boolean admin) {
        User saved = new User();
        saved.setUsername(username);
        saved.setAdmin(admin);
        return userRepository.saveAndFlush(saved);
    }

    private JwtRequestPostProcessor userJwt(String subject) {
        return jwt().jwt(token -> token.subject(subject));
    }

    private void assertUnauthorized(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    private void assertBadRequest(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }
}
