package org.example;


import jakarta.persistence.EntityManagerFactory;
import org.example.Config.ApplicationConfig;
import org.example.Config.HibernateConfig;
import org.example.Routes.Routes;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory(false);

        ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
        applicationConfig
                .initiateServer()
                .startServer(7000)
                .setExceptionHandling()
                .setRoute(Routes.getSecurityRoutes(emf))
                .setRoute(Routes.getSecuredRoutes(emf))
                .checkSecurityRoles();
    }
}
