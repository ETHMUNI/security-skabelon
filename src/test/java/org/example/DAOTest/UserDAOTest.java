package org.example.DAOTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.example.Config.HibernateConfig;
import org.example.DAO.UserDAO;
import org.example.Ressources.Role;
import org.example.Ressources.User;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private static EntityManagerFactory emf;
    private static UserDAO userDAO;

    @BeforeAll
    static void setUpAll() {
        emf = HibernateConfig.getEntityManagerFactoryConfigForTesting();
        userDAO = UserDAO.getInstance(emf);
    }

    @BeforeEach
    void setUp() {

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.createQuery("DELETE FROM Role").executeUpdate();
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new Role("USER"));
        em.persist(new Role("ADMIN"));
        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    static void tearDownAll() {
        emf.close();
    }

    @Test
    void createUser() {
        // create user
        String username = "testUser";
        String password = "pass123";
        User user = userDAO.createUser(username, password);
        assertNotNull(user, "user should be successfully created");
        assertEquals(username, user.getUsername(), "usernames should match");
    }

    @Test
    void verifyUser() {
        // create user
        String username = "testUser";
        String password = "pass123";
        userDAO.createUser(username, password);

        // verify user
        User verifiedUser = userDAO.verifyUser(username, password);
        assertNotNull(verifiedUser, "user should be found and verified");
        assertEquals(username, verifiedUser.getUsername(), "verified username should match");
    }
    @Test
    void updatePassword() {

        // setting up conditions with a user in the database
        String originalPassword = "pass123";
        String newPassword = "newPass123";
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User testUser = new User("testUser", originalPassword); // sassword is hashed in the user constructor
        em.persist(testUser);
        em.getTransaction().commit();
        em.close();

        // update the user's password using the DAO
        userDAO.updatePassword(testUser.getUsername(), newPassword);

        // verify that the password was updated correctly
        EntityManager verifyEm = emf.createEntityManager();
        User updatedUser = verifyEm.find(User.class, testUser.getUsername());
        assertNotNull(updatedUser, "The updated user should not be null.");
        assertTrue(updatedUser.verifyUser(newPassword), "The new password should be verified successfully.");
        verifyEm.close();
    }

}
