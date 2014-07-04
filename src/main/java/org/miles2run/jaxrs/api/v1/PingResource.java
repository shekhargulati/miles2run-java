package org.miles2run.jaxrs.api.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Created by shekhargulati on 04/07/14.
 */
@Path("/api/v1/ping")
public class PingResource {

    @GET
    @Produces("application/json")
    public Response ping() {
        return Response.status(Response.Status.OK).entity("pong").build();
    }
}
