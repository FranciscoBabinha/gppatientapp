package com.controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONObject;
import com.dao.InputDataDAO;
import java.util.List;

@Path("/saveData")
public class SaveDataService {

    private InputDataDAO inputDataDAO = new InputDataDAO();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response receiveString(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            String receivedText = jsonObject.optString("text", "").trim();

            if (receivedText.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Error: Empty input").build();
            }

            boolean isInserted = inputDataDAO.insertInputText(receivedText);

            if (isInserted) {
                return Response.ok("Data successfully stored in the database.").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Database insert failed.").build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllEntries() {
        List<String> entries = inputDataDAO.getAllEntries();
        return Response.ok(entries != null ? entries : "[]").build();  // Ensure JSON response is not null
    }
}