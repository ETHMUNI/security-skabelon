package org.example.DAO;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.example.Config.HibernateConfig;
import org.example.Ressources.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {

    private static EntityManagerFactory emf;
    private static UserDAO userDAO;
    @BeforeAll
    public static void setUp() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        userDAO = new UserDAO(emf);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void create() {
        User user = new User("test1", "test123");
        User createdUser = userDAO.create(user);
        assertNotNull(createdUser);
        assertEquals(user.getUsername(), createdUser.getUsername());
    }

    @Test
    void getById() {
        User user = new User("test1", "test123");
        userDAO.create(user);
        User retrievedUser = userDAO.getById(user.getUsername());
        assertNotNull(retrievedUser);
        assertEquals(user.getUsername(), retrievedUser.getUsername());
    }

    @Test
    void update() {
        User user = new User("test1", "test123");
        userDAO.create(user);
        user.setPassword("newPassword");
        User updatedUser = userDAO.update(user);
        assertNotNull(updatedUser);
        assertEquals("newPassword", updatedUser.getPassword());
    }

    @Test
    void delete() {
        User user = new User("test1", "test123");
        userDAO.create(user);
        userDAO.delete(user);
        assertThrows(EntityNotFoundException.class, () -> userDAO.getById(user.getUsername()));
    }
}