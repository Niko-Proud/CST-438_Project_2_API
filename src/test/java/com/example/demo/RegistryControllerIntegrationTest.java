package com.example.demo;
import com.example.demo.entity.RegistryItem;
import com.example.demo.entity.User;
import com.example.demo.repository.RegistryItemRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class RegistryControllerIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistryItemRepository registryItemRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private RegistryItem testItem;

    @BeforeEach
    public void setUp() {
        registryItemRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser@example.com");
        testUser.setAdmin(false);
        testUser = userRepository.save(testUser);

        testItem = new RegistryItem();
        testItem.setProduct("Canned Beans");
        testItem.setBatch("BATCH-001");
        testItem.setUser(testUser);
        testItem = registryItemRepository.save(testItem);
    }

    @Test
    public void createItem_Returns201() throws Exception {
        String requestBody = "{ \"product\": \"Almond Milk\", \"batch\": \"LOT-999\" }";

        assertThatRequest(post("/api/v1/registry")
                .with(jwt().jwt(token -> token.subject(testUser.getUsername())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.product").value("Almond Milk"));
    }

    @Test
    public void getItem_ReturnsItem_WhenExists() throws Exception {
        assertThatRequest(get("/api/v1/registry/" + testItem.getId())
                .with(jwt().jwt(token -> token.subject(testUser.getUsername()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value("Canned Beans"));
    }

    @Test
    public void getItem_Returns404_WhenDoesNotExist() throws Exception {
        assertThatRequest(get("/api/v1/registry/99999")
                .with(jwt().jwt(token -> token.subject(testUser.getUsername()))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void replaceItem_Returns200() throws Exception {
        String updatedBody = "{ \"product\": \"Updated Beans\", \"batch\": \"BATCH-002\" }";
        assertThatRequest(put("/api/v1/registry/" + testItem.getId())
                .with(jwt().jwt(token -> token.subject(testUser.getUsername())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value("Updated Beans"))
                .andExpect(jsonPath("$.batch").value("BATCH-002"));
    }

    @Test
    public void deleteItem_Returns204() throws Exception {
        assertThatRequest(delete("/api/v1/registry/" + testItem.getId())
                .with(jwt().jwt(token -> token.subject(testUser.getUsername()))))
                .andExpect(status().isNoContent());
    }

    private org.springframework.test.web.servlet.ResultActions assertThatRequest(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        return mockMvc.perform(request);
    }
}
