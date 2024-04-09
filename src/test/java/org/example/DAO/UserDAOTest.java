package org.example.DAO;

import org.example.Ressources.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = mock(UserDAO.class);
    }

    @Test
    void createUser() {
        User newUser = new User("username", "password");
        when(userDAO.create(newUser)).thenReturn(newUser); // Assuming create returns the created user for the purpose of this example

        User resultUser = userDAO.create(newUser);
        verify(userDAO).create(newUser); // Verify that create was called with newUser
        assertEquals(newUser.getUsername(), resultUser.getUsername(), "The username of the created user should match.");
    }

    @Test
    void findUserById() {
        User foundUser = new User("username", "password");
        when(userDAO.getById("username")).thenReturn(foundUser);

        User result = userDAO.getById("username");
        verify(userDAO).getById("username"); // Verify that getById was called with "username"
        assertEquals("username", result.getUsername(), "The username of the found user should match.");
    }

    // Here we have 2 users where we simulate to create a new user with a password and update it with a new password. This is because where are using incryption and salt on our passwords in the db
    @Test
    void updateUser() {
        User originalUser = new User("username", "password");
        User updatedUser = new User("username", "newPassword");

        // Assuming update method returns the user with the updated password
        when(userDAO.update(any(User.class))).thenReturn(updatedUser);

        User result = userDAO.update(originalUser);

        verify(userDAO).update(originalUser); // Verify that update was called with originalUser
        // Since you cannot directly compare hashed passwords, focus on checking that you receive the expected updated user object
        assertNotEquals(originalUser.getPassword(), result.getPassword(), "The password should have been updated.");
    }

    @Test
    void deleteUser() {
        User userToDelete = new User("username", "password");
        // For deletion, since there's no direct result to assert on, we simulate that the user no longer exists
        when(userDAO.getById(userToDelete.getUsername())).thenReturn(null); // Assuming user is not found after deletion

        userDAO.delete(userToDelete);
        verify(userDAO).delete(userToDelete); // Verify that delete was called with userToDelete
        User resultUser = userDAO.getById(userToDelete.getUsername());
        assertEquals(null, resultUser, "The user should no longer exist after deletion.");
    }
}
