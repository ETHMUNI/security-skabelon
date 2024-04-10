package org.example.Routes;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.Config.ApplicationConfig;
import org.example.Config.HibernateConfig;
import org.example.Main;
import org.example.Ressources.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;


// integration test
class RoutesTest {

    @BeforeAll
    static void setUpAll() {
        RestAssured.baseURI = "http://localhost:7001/api";
        Main.startServer(7001);

    }

    @AfterAll
    static void tearDownAll() {
        Main.closeServer();
    }


    @Test
    @DisplayName("Test Post Request")
    void registerUserSuccessfully() {
        User u = new User("test1", "test123");
        // Given: Givet at serveren kører
        RestAssured.
         given()
                .contentType("application/json")
                .body(u)
                // When: sender jeg en POST til "/auth/register" med user data
                .when()
                .post("/auth/register")
                // Then: får jeg status kode 200 tilbage
                .then()
                .statusCode(200)
                .body("username", equalTo("test1"))
                .body("password", equalTo("test123"));
    }

    @Test
    void loginUserSuccessfully() {
        // Given: Givet at serveren kører og en bruger med test1 er registeret
        Response response = given()
                .contentType("application/json")
                .body("{\"username\":\"test1\", \"password\":\"test123\"}")
                // When: sender jeg en POST til "/login" med de rigtige oplysninger
                .when()
                .post("/auth/login")
                // Then får jeg status kode 200
                .then()
                .extract().response();
        // og får svaret:
        assertEquals(200, response.statusCode());
        assertEquals("User logged in successfully", response.jsonPath().getString("message"));
    }


}
