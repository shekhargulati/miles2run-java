package org.miles2run.jaxrs.api.v1;

import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.ActivityJPAService;
import org.miles2run.business.services.jpa.GoalJPAService;
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
    private ActivityJPAService activityJPAService;
    @Inject
    private ProfileService profileService;
    @Context
    private SecurityContext securityContext;
    @Inject
    private GoalJPAService goalJPAService;

    @GET
    @Produces("application/json")
    public Response progress(@NotNull @PathParam("username") String username, @NotNull @PathParam("goalId") Long goalId) {
        Profile profile = profileService.findProfile(username);
        Goal goal = goalJPAService.find(goalId);
        Progress progress = activityJPAService.calculateUserProgressForGoal(profile, goal);
        return Response.status(Response.Status.OK).entity(progress).build();
    }

}
