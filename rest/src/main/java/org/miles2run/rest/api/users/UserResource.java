package org.miles2run.rest.api.users;

import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.mongo.UserProfileRepository;
import org.miles2run.core.vo.ProfileDetails;
import org.miles2run.core.vo.ProfileSocialConnectionDetails;
import org.miles2run.domain.documents.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.List;

@Path("/users")
public class UserResource {

    private final Logger logger = LoggerFactory.getLogger(UserResource.class);

    @Inject
    private ProfileRepository profileRepository;
    @Context
    private SecurityContext securityContext;
    @Inject
    private UserProfileRepository userProfileRepository;

    @Path("/me")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Response currentLoggedInUser() {
        String username = securityContext.getUserPrincipal().getName();
        ProfileSocialConnectionDetails profileWithSocialConnections = profileRepository.findProfileWithSocialConnections(username);
        return Response.ok(profileWithSocialConnections).build();
    }

    @GET
    @Produces("application/json")
    public List<ProfileDetails> profiles(@QueryParam("name") String name) {
        return profileRepository.findProfileWithFullnameLike(name);
    }

    @Path("/me/following")
    @GET
    @Produces("application/json")
    @LoggedIn
    public List<ProfileDetails> followings() {
        String username = securityContext.getUserPrincipal().getName();
        UserProfile userProfile = userProfileRepository.find(username);
        List<String> following = userProfile.getFollowing();
        logger.info(String.format("User %s is following %s", username, following));
        if (following.isEmpty()) {
            return Collections.emptyList();
        }
        return profileRepository.findAllProfiles(following);
    }

    @Path("/{username}/following")
    @GET
    @Produces("application/json")
    public List<ProfileDetails> followingForProfile(@PathParam("username") String username) {
        UserProfile userProfile = userProfileRepository.find(username);
        List<String> following = userProfile.getFollowing();
        logger.info(String.format("User %s is following %s", username, following));
        if (following.isEmpty()) {
            return Collections.emptyList();
        }
        return profileRepository.findAllProfiles(following);
    }

    @Path("/{username}/followers")
    @GET
    @Produces("application/json")
    public List<ProfileDetails> followersForProfile(@PathParam("username") String username) {
        UserProfile userProfile = userProfileRepository.find(username);
        List<String> followers = userProfile.getFollowers();
        logger.info(String.format("User %s is followers %s", username, followers));
        if (followers.isEmpty()) {
            return Collections.emptyList();
        }
        return profileRepository.findAllProfiles(followers);
    }

}

