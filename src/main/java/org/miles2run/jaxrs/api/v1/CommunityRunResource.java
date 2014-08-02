package org.miles2run.jaxrs.api.v1;

import org.apache.commons.lang3.StringUtils;
import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.domain.jpa.Role;
import org.miles2run.business.services.ProfileMongoService;
import org.miles2run.business.services.jpa.CommunityRunJPAService;
import org.miles2run.business.services.redis.CommunityRunRedisService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.utils.SlugUtils;
import org.miles2run.business.vo.ProfileGroupDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shekhargulati on 10/07/14.
 */
@Path("/api/v1/community_runs")
public class CommunityRunResource {

    private Logger logger = LoggerFactory.getLogger(CommunityRunResource.class);

    @Inject
    private CommunityRunRedisService communityRunRedisService;
    @Inject
    private CommunityRunJPAService communityRunJPAService;
    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileService profileService;
    @Inject
    private ProfileMongoService profileMongoService;


    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @LoggedIn
    public Response createCommunityRun(@Valid CommunityRun communityRun) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfileByUsername(loggedInUser);
        if (profile.getRole() == Role.ADMIN || profile.getRole() == Role.ORGANIZER) {
            communityRun.setSlug(SlugUtils.toSlug(communityRun.getName()));
            Long id = communityRunJPAService.save(communityRun);
            communityRunRedisService.addCommunityRunToSet(communityRun.getSlug());
            return Response.status(Response.Status.CREATED).entity(id).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();

    }


    @GET
    @Produces("application/json")
    public List<CommunityRun> allCommunityRuns(@QueryParam("name") String name) {
        if (StringUtils.isNotBlank(name)) {
            return communityRunJPAService.findAllActiveRacesWithNameLike(name);
        }
        return communityRunJPAService.findAllActiveRaces();
    }

    @Path("/{slug}")
    @GET
    @Produces("application/json")
    public CommunityRun findCommunityRun(@NotNull @PathParam("slug") String slug) {
        return communityRunJPAService.findBySlug(slug);
    }

    @Path("/{slug}/profiles_group_city")
    @GET
    @Produces("application/json")
    public List<ProfileGroupDetails> groupAllUsersInACommunityRunByCity(@NotNull @PathParam("slug") String slug) {
        List<ProfileGroupDetails> profileGroupDetailsList = communityRunJPAService.groupAllUserInACommunityRunByCity(slug);
        List<ProfileGroupDetails> profileGroupDetailsListWithLatLng = new ArrayList<>();
        for (ProfileGroupDetails profileGroupDetails : profileGroupDetailsList) {
            profileGroupDetailsListWithLatLng.add(ProfileGroupDetails.newWitLatLng(profileGroupDetails, profileMongoService.findLatLngForACity(profileGroupDetails.getCity())));
        }
        return profileGroupDetailsListWithLatLng;
    }

}
