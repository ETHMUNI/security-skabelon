package org.example.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;
import org.example.DTO.UserDTO;
import org.example.Exceptions.ApiException;
import org.example.Handlers.ISecurityHandler;
import org.example.Handlers.SecurityHandler;

import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationConfig {
    ObjectMapper om = new ObjectMapper();
    ISecurityHandler securityHandler = new SecurityHandler();
    private Javalin app;
    private static ApplicationConfig instance;
    private ApplicationConfig(){}
    public static ApplicationConfig getInstance(){
        if(instance == null){
            instance = new ApplicationConfig();
        }
        return instance;
    }
    public ApplicationConfig initiateServer(){
        app = Javalin.create(config->{
            config.http.defaultContentType = "application/json";
            config.routing.contextPath = "/api";
        });

        return instance;
    }

    public ApplicationConfig checkSecurityRoles() {
        // Check roles on the user (ctx.attribute("username") and compare with permittedRoles using securityController.authorize()
        app.updateConfig(config -> {

            config.accessManager((handler, ctx, permittedRoles) -> {
                // permitted roles are defined in the last arg to routes: get("/", ctx -> ctx.result("Hello World"), Role.ANYONE);

                Set<String> allowedRoles = permittedRoles.stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
                if(allowedRoles.contains("ANYONE") || ctx.method().toString().equals("OPTIONS")) {
                    // Allow requests from anyone and OPTIONS requests (preflight in CORS)
                    handler.handle(ctx);
                    return;
                }

                UserDTO user = ctx.attribute("user");
                System.out.println("USER IN CHECK_SEC_ROLES: "+user);
                if(user == null)
                    ctx.status(HttpStatus.FORBIDDEN)
                            .json(om.createObjectNode()
                                    .put("msg","Not authorized. No username were added from the token"));

                if (securityHandler.authorize(user, allowedRoles))
                    handler.handle(ctx);
                else
                    throw new ApiException(HttpStatus.FORBIDDEN.getCode(), "Unauthorized with roles: "+allowedRoles);
            });
        });
        return instance;
    }
    public ApplicationConfig startServer(int port){
        app.start(port);
        return instance;
    }
    public ApplicationConfig setRoute(EndpointGroup route){
        app.routes(route);
        return instance;
    }

    public ApplicationConfig setExceptionHandling(){
        app.exception(Exception.class, (e,ctx)->{
            ObjectNode node = om.createObjectNode().put("errrorMessage",e.getMessage());
            ctx.status(500).json(node);
        });
        return instance;
    }
    public void stopServer(){
        app.stop();
    }
}
