package org.example.Routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.security.RouteRole;
import org.example.Handlers.SecurityHandler;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {
    private static SecurityHandler securityHandler;


    public static EndpointGroup getSecurityRoutes(EntityManagerFactory emf) {
        securityHandler = SecurityHandler.getInstance(emf); // getting the singleton instance

        return ()->{
            path("/auth", ()->{
                post("/login", securityHandler.login(), Role.ANYONE);
                post("/register", securityHandler.register(), Role.ANYONE);
                before(securityHandler.authenticate());
                post("/reset-password", securityHandler.resetPassword(), Role.USER, Role.ADMIN);
                post("/logout", securityHandler.logout(), Role.USER, Role.ADMIN);
            });
        };
    }

    // Similarly, update the getSecuredRoutes method
    public static EndpointGroup getSecuredRoutes(EntityManagerFactory emf){
        securityHandler = SecurityHandler.getInstance(emf); // getting the singleton instance

        return ()->{
            path("/protected", ()->{
                before(securityHandler.authenticate());
                get("/user_demo", (ctx)->ctx.json(new ObjectMapper().createObjectNode().put("msg", "Hello from USER Protected")), Role.USER);
                get("/admin_demo", (ctx)->ctx.json(new ObjectMapper().createObjectNode().put("msg", "Hello from ADMIN Protected")), Role.ADMIN);
            });
        };
    }

    public enum Role implements RouteRole { ANYONE, USER, ADMIN }
}
