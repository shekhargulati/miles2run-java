package org.miles2run.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("ping")
public class PingResource {

    @GET
    @Produces("application/json")
    public Response ping() {
        return Response.status(Response.Status.OK).entity("pong").build();
    }
}
