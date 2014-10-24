package org.miles2run.rest.api.users;

import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.mongo.UserProfileRepository;
import org.miles2run.domain.documents.UserProfile;
import org.miles2run.domain.entities.Profile;
import org.miles2run.representations.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("/users")
public class UserResource {

    private final Logger logger = LoggerFactory.getLogger(UserResource.class);

    @Inject
    private ProfileRepository profileRepository;
    @Context
    private SecurityContext securityContext;
    @Inject
    private UserProfileRepository userProfileRepository;

/*    @Path("/me")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Response currentLoggedInUser() {
        String username = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findWithSocialConnections(username);
        return Response.ok(UserRepresentation.from(profile)).build();
    }*/

    @GET
    @Produces("application/json")
    public List<UserRepresentation> profiles(@QueryParam("name") String name) {
        List<Profile> profiles = profileRepository.findProfilesWithFullnameLike(name);
        return profiles.stream().map(UserRepresentation::from).collect(Collectors.toList());
    }

    @Path("/me/following")
    @GET
    @Produces("application/json")
    @LoggedIn
    public List<UserRepresentation> followings() {
        String username = securityContext.getUserPrincipal().getName();
        UserProfile userProfile = userProfileRepository.find(username);
        return getFollowing(username, userProfile);
    }

    private List<UserRepresentation> getFollowing(String username, UserProfile userProfile) {
        List<String> following = userProfile.getFollowing();
        logger.info(String.format("User %s following %s", username, following));
        return getProfileRepresentations(following);
    }

    private List<UserRepresentation> getProfileRepresentations(List<String> usernames) {
        if (usernames.isEmpty()) {
            return Collections.emptyList();
        }
        List<Profile> profiles = profileRepository.findProfiles(usernames);
        return profiles.stream().map(UserRepresentation::from).collect(Collectors.toList());
    }

    @Path("/{username}/following")
    @GET
    @Produces("application/json")
    public List<UserRepresentation> followingForProfile(@PathParam("username") String username) {
        UserProfile userProfile = userProfileRepository.find(username);
        return getFollowing(username, userProfile);
    }

    @Path("/{username}/followers")
    @GET
    @Produces("application/json")
    public List<UserRepresentation> followersForProfile(@PathParam("username") String username) {
        UserProfile userProfile = userProfileRepository.find(username);
        List<String> followers = userProfile.getFollowers();
        logger.info(String.format("User %s followers %s", username, followers));
        return getProfileRepresentations(followers);
    }

}

