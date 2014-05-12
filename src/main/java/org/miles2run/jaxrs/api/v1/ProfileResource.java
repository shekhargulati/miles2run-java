package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.vo.ProfileDetails;
import org.miles2run.business.vo.ProfileSocialConnectionDetails;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

/**
 * Created by shekhargulati on 12/03/14.
 */
@Path("/api/v1/profiles")
public class ProfileResource {

    @Context
    private HttpServletRequest request;

    @Inject
    private ProfileService profileService;
    @Context
    private SecurityContext securityContext;

    @Path("/me")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Response currentLoggedInUser() {
        String username = securityContext.getUserPrincipal().getName();
        ProfileSocialConnectionDetails profileWithSocialConnections = profileService.findProfileWithSocialConnections(username);
        return Response.ok(profileWithSocialConnections).build();
    }

    @GET
    @Produces("application/json")
    public List<ProfileDetails> profiles(@QueryParam("name") String name) {
        return profileService.findProfileWithFullnameLike(name);
    }


}

