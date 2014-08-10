package org.miles2run.jaxrs.api.v1;

import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.ActivityService;
import org.miles2run.business.services.jpa.ProfileService;
import org.miles2run.business.vo.Progress;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by shekhargulati on 15/03/14.
 */
@Path("/api/v1/profiles/{username}/goals/{goalId}/progress")
public class ProgressResource {

    @Inject
    private ActivityService activityService;
    @Inject
    private ProfileService profileService;
    @Context
    private SecurityContext securityContext;

    @GET
    @Produces("application/json")
    public Response progress(@NotNull @PathParam("username") String username, @NotNull @PathParam("goalId") Long goalId) {
        Profile loggedInUser = profileService.findProfile(username);
        Progress progress = activityService.calculateUserProgressForGoal(loggedInUser, goalId);
        return Response.status(Response.Status.OK).entity(progress).build();
    }

}
