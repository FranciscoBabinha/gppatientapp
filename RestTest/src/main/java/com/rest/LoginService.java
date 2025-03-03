package com.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/login")  // Maps to /HelloWorld/rest/login
public class LoginService {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)  // Sends an HTML page response
    public Response authenticateUser(@FormParam("username") String username,
                                     @FormParam("password") String password) {

        // Accept only if both username and password are null or empty
        if ((username == null || username.isEmpty()) && (password == null || password.isEmpty())) {
            String successPage = "<html><head><title>Login Success</title></head>"
                               + "<body><h1>Welcome!</h1><p>You have successfully logged in.</p></body></html>";
            return Response.ok(successPage).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Invalid credentials").build();
        }
    }
}