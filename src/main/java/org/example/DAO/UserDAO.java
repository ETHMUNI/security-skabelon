package org.example.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.example.Ressources.Role;
import org.example.Ressources.User;

import static org.example.Config.HibernateConfig.getEntityManagerFactory;

public class UserDAO implements ISecurityDAO {

    private static UserDAO instance;
    private EntityManagerFactory emf;

    private UserDAO(EntityManagerFactory _emf) {
        this.emf = _emf;
    }

    public static UserDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            instance = new UserDAO(_emf);
        }
        return instance;
    }
   @Override
    public User createUser(String username, String password) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User user = new User(username, password);
        Role userRole = em.find(Role.class, "user");
        if (userRole == null) {
            userRole = new Role("user");
            em.persist(userRole);
        }
        user.addRole(userRole);
        em.persist(user);
        em.getTransaction().commit();
        em.close();
        return user;
    }

    @Override
    public Role createRole(String role) {
        //return null;

        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public User addRoleToUser(String username, String role) {
        //return null;

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public User verifyUser(String username, String password) throws EntityNotFoundException {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, username);
        if (user == null)
            throw new EntityNotFoundException("No user found with username: " + username);
        if (!user.verifyUser(password))
            throw new EntityNotFoundException("Wrong password");
        return user;
    }

    public void updatePassword(String username, String newPassword) {
        EntityManager em = this.emf.createEntityManager();  // Use the emf passed to the constructor
        em.getTransaction().begin();
        User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getSingleResult();

        user.updatePassword(newPassword);

        em.getTransaction().commit();
        em.close();
    }


}
