package org.miles2run.jaxrs.api.v1;

import org.miles2run.business.services.mongo.FriendRecommender;
import org.miles2run.business.services.jpa.ProfileService;
import org.miles2run.business.vo.ProfileDetails;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 20/03/14.
 */
@Path("/api/v1/profiles/{username}/suggestions")
public class ProfileSuggestionResource {

    @Inject
    private FriendRecommender friendRecommender;
    @Inject
    private ProfileService profileService;
    @Inject
    private Logger logger;

    @GET
    @Produces("application/json")
    public List<ProfileDetails> recommendFriends(@PathParam("username") String username) {
        logger.info("Recommending friends to " + username);
        List<String> recommendFriends = friendRecommender.recommend(username);
        if (recommendFriends.isEmpty()) {
            return Collections.emptyList();
        }
        logger.info("Recommended Friends .. " + recommendFriends);
        return profileService.findAllProfiles(recommendFriends);
    }


}
