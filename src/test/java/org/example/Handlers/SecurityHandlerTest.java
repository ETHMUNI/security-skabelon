package org.example.Handlers;

import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.example.Config.ApplicationConfig;
import org.example.Config.HibernateConfig;
import org.example.Routes.Routes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.example.Ressources.User;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SecurityHandlerTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    static void setUpAll() {
        emf = HibernateConfig.getEntityManagerFactoryConfigForTesting();

        RestAssured.baseURI = "http://localhost:7000/api";
        ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
        applicationConfig.initiateServer()
                .startServer(7000)
                .setExceptionHandling()
                .setRoute(Routes.getSecuredRoutes(emf))
                .checkSecurityRoles();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            User newUser = new User();
            newUser.setUsername("existinguser");
            newUser.setPassword("password");  // Consider hashing the password if your system uses hashed passwords
            em.persist(newUser);
            em.getTransaction().commit();
        }
    }

    @AfterAll
    static void tearDown() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("delete from User").executeUpdate();
            em.getTransaction().commit();
        }
    }

    private static String tokenForTestUser(String username, String password) {
        String json = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

        return given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/api/auth/login")
                .then()
                .extract()
                .path("token");
    }

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
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("username", equalTo("newuser"))
                .body("token", notNullValue());
    }

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
                .post("/api/auth/login")
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
                .post("/api/auth/resetPassword")
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
                .post("/api/auth/logout")
                .then()
                .statusCode(200)
                .body("msg", equalTo("Logout successful. Discard token..."));
    }
}
