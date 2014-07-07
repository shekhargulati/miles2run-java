package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.mongo.UserProfile;
import org.miles2run.business.services.ProfileMongoService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.vo.ProfileDetails;
import org.miles2run.business.vo.ProfileSocialConnectionDetails;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

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
    @Inject
    private ProfileMongoService profileMongoService;
    @Inject
    private Logger logger;

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

    @Path("/me/following")
    @GET
    @Produces("application/json")
    @LoggedIn
    public List<ProfileDetails> followings() {
        String username = securityContext.getUserPrincipal().getName();
        UserProfile userProfile = profileMongoService.findProfile(username);
        List<String> following = userProfile.getFollowing();
        logger.info(String.format("User %s is following %s", username, following));
        if (following.isEmpty()) {
            return Collections.emptyList();
        }
        return profileService.findAllProfiles(following);
    }

    @Path("/{username}/following")
    @GET
    @Produces("application/json")
    public List<ProfileDetails> followingForProfile(@PathParam("username") String username) {
        UserProfile userProfile = profileMongoService.findProfile(username);
        List<String> following = userProfile.getFollowing();
        logger.info(String.format("User %s is following %s", username, following));
        if (following.isEmpty()) {
            return Collections.emptyList();
        }
        return profileService.findAllProfiles(following);
    }

    @Path("/{username}/followers")
    @GET
    @Produces("application/json")
    public List<ProfileDetails> followersForProfile(@PathParam("username") String username) {
        UserProfile userProfile = profileMongoService.findProfile(username);
        List<String> followers = userProfile.getFollowers();
        logger.info(String.format("User %s is followers %s", username, followers));
        if (followers.isEmpty()) {
            return Collections.emptyList();
        }
        return profileService.findAllProfiles(followers);
    }

}

