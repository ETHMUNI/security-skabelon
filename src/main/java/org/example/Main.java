package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import org.example.Config.ApplicationConfig;
import org.example.Config.HibernateConfig;

import static org.example.Routes.Routes.*;

public class Main {
    public static void main(String[] args) {
        Main.startServer(7000);
    }

    public static void startServer(int port) {
        ObjectMapper om = new ObjectMapper();
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
        applicationConfig
                .initiateServer()
                .startServer(port)
                .setExceptionHandling()
                .setRoute(getSecurityRoutes())
                .setRoute(getSecuredRoutes())
                .checkSecurityRoles();
    }

    public static void closeServer() {
        ApplicationConfig.getInstance().stopServer();
    }
}