package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.domain.jpa.Role;
import org.miles2run.business.services.CommunityRunService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.utils.SlugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

/**
 * Created by shekhargulati on 10/07/14.
 */
@Path("/api/v1/community_runs")
public class CommunityRunResource {

    private Logger logger = LoggerFactory.getLogger(CommunityRunResource.class);

    @Inject
    private CommunityRunService communityRunService;
    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileService profileService;


    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @LoggedIn
    public Response createCommunityRun(@Valid CommunityRun communityRun) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfileByUsername(loggedInUser);
        if (profile.getRole() == Role.ADMIN || profile.getRole() == Role.ORGANIZER) {
            communityRun.setSlug(SlugUtils.toSlug(communityRun.getName()));
            Long id = communityRunService.save(communityRun);
            return Response.status(Response.Status.CREATED).entity(id).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();

    }


    @GET
    @Produces("application/json")
    public List<CommunityRun> allCommunityRuns() {
        return communityRunService.findAllActiveRaces();
    }

    @Path("/{slug}")
    @GET
    @Produces("application/json")
    public CommunityRun findCommunityRun(@NotNull @PathParam("slug") String slug) {
        return communityRunService.findBySlug(slug);
    }


}
