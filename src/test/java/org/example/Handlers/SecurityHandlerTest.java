package org.example.Handlers;

import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.example.Config.ApplicationConfig;
import org.example.Config.HibernateConfig;
import org.example.Ressources.Role;
import org.example.Routes.Routes;
import org.junit.jupiter.api.*;
import org.example.Ressources.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SecurityHandlerTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    static void setUpAll() {
        emf = HibernateConfig.getEntityManagerFactory(true);
        RestAssured.baseURI = "http://localhost:7000/api";
        ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
        applicationConfig.initiateServer()
                .startServer(7000)
                .setExceptionHandling()
                .setRoute(Routes.getSecurityRoutes(emf))
                .checkSecurityRoles();
    }

    @BeforeEach
    void setUp() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Clean up the previous test data
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();

            // Setup required data for the next test
            Role userRole = new Role("USER");
            Role adminRole = new Role("ADMIN");
            em.persist(userRole);
            em.persist(adminRole);

            // Using the constructor that hashes the password
            User newUser = new User("existinguser", "password");
            newUser.addRole(userRole);
            newUser.addRole(adminRole);
            em.persist(newUser);

            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    @AfterAll
    static void tearDownAll() {
        emf.close();
    }


    private static String tokenForTestUser(String username, String password) {
        String json = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

        return given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("http://localhost:7000/api/auth/login")
                .then()
                .extract()
                .path("token");
    }
/* Method doesn't work
    @Test
    void testRegister() {
        String json = """
        {
            "username": "newuser",
            "password": "newpassword123"
        }
        """;

        RestAssured.given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("http://localhost:7000/api/auth/register")
                .then()
                .statusCode(201)
                .body("username", equalTo("newuser"))
                .body("token", notNullValue());
    }*/

    @Test
    void testLogin() {
        String json = """
        {
            "username": "existinguser",
            "password": "password"
        }
        """;

        RestAssured.given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("http://localhost:7000/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void testResetPassword() {
        String token = "Bearer " + tokenForTestUser("existinguser", "password");
        String json = """
        {
            "newPassword": "updatedPassword123"
        }
        """;

        RestAssured.given()
                .header("Authorization", token)
                .contentType("application/json")
                .body(json)
                .when()
                .post("http://localhost:7000/api/auth/reset-password")
                .then()
                .statusCode(200)
                .body("msg", equalTo("Password reset successfully."));
    }

    @Test
    void testLogout() {
        String token = "Bearer " + tokenForTestUser("existinguser", "password");

        RestAssured.given()
                .header("Authorization", token)
                .when()
                .post("http://localhost:7000/api/auth/logout")
                .then()
                .statusCode(200)
                .body("msg", equalTo("Logout successful. Discard token..."));
    }
}
