package org.example.Handlers;

import com.nimbusds.jose.JOSEException;
import io.javalin.http.Handler;
import org.example.DTO.UserDTO;
import org.example.Exceptions.NotAuthorizedException;

import java.text.ParseException;
import java.util.Set;

public interface ISecurityHandler {

    Handler register(); // creates a new User
    Handler login(); // logs in a user and returns a token
    Handler resetPassword(); // resets the users password
    Handler logout(); // logging user out and discarding the token

    String createToken(UserDTO user); // creates a token based on the UserDTO, sercret and expiration time
    Handler authenticate(); // checks if the token is valid and adds the user (with roles) to the context
    UserDTO verifyToken(String token);
    boolean tokenIsValid(String token, String secret) throws ParseException, JOSEException, NotAuthorizedException;
    boolean tokenNotExpired(String token) throws ParseException, NotAuthorizedException;
    UserDTO getUserWithRolesFromToken(String token) throws ParseException;
    int timeToExpire(String token) throws ParseException, NotAuthorizedException;
}
