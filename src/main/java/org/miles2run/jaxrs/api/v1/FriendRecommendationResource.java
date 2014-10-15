package org.miles2run.jaxrs.api.v1;

import org.miles2run.business.recommender.FriendRecommender;
import org.miles2run.business.services.jpa.ProfileService;
import org.miles2run.business.vo.ProfileDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Collections;
import java.util.List;

@Path("/api/v1/profiles/{username}/suggestions")
public class FriendRecommendationResource {

    private final Logger logger = LoggerFactory.getLogger(FriendRecommendationResource.class);

    @Inject
    private FriendRecommender friendRecommender;
    @Inject
    private ProfileService profileService;

    @GET
    @Produces("application/json")
    public List<ProfileDetails> recommendFriends(@NotNull @PathParam("username") String username) {
        List<String> recommendations = friendRecommender.recommend(username);
        if (recommendations.isEmpty()) {
            logger.debug("No friend recommendation for {}", username);
            return Collections.emptyList();
        }
        logger.info("Recommended friends for {} are {}", username, recommendations);
        return profileService.findAllProfiles(recommendations);
    }

}
