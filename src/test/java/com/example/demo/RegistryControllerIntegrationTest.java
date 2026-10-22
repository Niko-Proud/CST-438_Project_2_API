package com.example.demo;
import com.example.demo.entity.RegistryItem;
import com.example.demo.entity.User;
import com.example.demo.repository.RegistryItemRepository;
import com.example.demo.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistryControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private RegistryItemRepository registryItemRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private RegistryItem testItem;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
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
    public void createItem_Returns201() {
        String requestBody = "{ \"product\": \"Almond Milk\", \"batch\": \"LOT-999\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/registry")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("product", equalTo("Almond Milk"));
    }

    @Test
    public void getItem_ReturnsItem_WhenExists() {
        given()
                .when()
                .get("/api/v1/registry/" + testItem.getId())
                .then()
                .statusCode(200)
                .body("product", equalTo("Canned Beans"));
    }

    @Test
    public void getItem_Returns404_WhenDoesNotExist() {
        given()
                .when()
                .get("/api/v1/registry/99999")
                .then()
                .statusCode(404);
    }

    @Test
    public void replaceItem_Returns200() {
        String updatedBody = "{ \"product\": \"Updated Beans\", \"batch\": \"BATCH-002\" }";
        given()
                .contentType(ContentType.JSON)
                .body(updatedBody)
                .when()
                .put("/api/v1/registry/" + testItem.getId())
                .then()
                .statusCode(200)
                .body("product", equalTo("Updated Beans"))
                .body("batch", equalTo("BATCH-002"));
    }

    @Test
    public void deleteItem_Returns204() {
        given()
                .when()
                .delete("/api/v1/registry/" + testItem.getId())
                .then()
                .statusCode(204);
    }
}
