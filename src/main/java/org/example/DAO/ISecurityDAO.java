package org.example.DAO;

import org.example.Ressources.Role;
import org.example.Ressources.User;

public interface ISecurityDAO {
    User createUser(String username, String password);

    Role createRole(String role);

    User addRoleToUser(String username, String role);

}
