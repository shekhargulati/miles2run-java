package org.miles2run.rest.api.community_runs;

import org.apache.commons.lang3.StringUtils;
import org.jug.filters.InjectPrincipal;
import org.jug.filters.LoggedIn;
import org.miles2run.core.cache.CityCoordinatesCache;
import org.miles2run.core.repositories.jpa.CommunityRunRepository;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.CommunityRunStatsRepository;
import org.miles2run.core.utils.SlugUtils;
import org.miles2run.core.vo.ProfileGroupDetails;
import org.miles2run.domain.entities.CommunityRun;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;
import org.miles2run.domain.entities.Role;
import org.miles2run.representations.CommunityRunDetails;
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
import java.util.stream.Collectors;

@Path("community_runs")
public class CommunityRunResource {

    private Logger logger = LoggerFactory.getLogger(CommunityRunResource.class);

    @Inject
    private CommunityRunStatsRepository communityRunStatsRepository;
    @Inject
    private CommunityRunRepository communityRunRepository;
    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileRepository profileRepository;
    @Inject
    private GoalRepository goalRepository;
    @Inject
    private CityCoordinatesCache cityCache;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @LoggedIn
    public Response createCommunityRun(@Valid CommunityRun communityRun) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findProfileByUsername(loggedInUser);
        if (profile.getRole() == Role.ADMIN || profile.getRole() == Role.ORGANIZER) {
            communityRun.setSlug(SlugUtils.toSlug(communityRun.getName()));
            Long id = communityRunRepository.save(communityRun);
            communityRunStatsRepository.addCommunityRunToSet(communityRun.getSlug());
            return Response.status(Response.Status.CREATED).entity(id).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();

    }

    @GET
    @Produces("application/json")
    @InjectPrincipal
    public List<CommunityRunDetails> allCommunityRuns(@QueryParam("name") String name, @QueryParam("include_stats") boolean includeStats, @QueryParam("include_participation_detail") boolean includeParticipationDetail, @QueryParam("max") int max, @QueryParam("page") int page) {
        max = max > 20 ? 20 : max;
        page = page == 0 ? 1 : page;
        if (StringUtils.isNotBlank(name)) {
            return toCommunityRunDetailsList(communityRunRepository.findAllActiveCommunityRunsWithNameLike(name, page, max));
        }
        List<CommunityRun> activeCommunityRuns = communityRunRepository.findAllActiveCommunityRuns(max, page);
        List<CommunityRunDetails> communityRunDetailsList = new ArrayList<>();
        for (CommunityRun activeCommunityRun : activeCommunityRuns) {
            String slug = activeCommunityRun.getSlug();
            CommunityRunDetails communityRunDetails = CommunityRunDetails.fromCommunityRun(activeCommunityRun);
            if (includeStats) {
                communityRunDetails.addStats(communityRunStatsRepository.getCurrentStatsForCommunityRun(slug));
            }
            if (includeParticipationDetail && securityContext.getUserPrincipal() != null) {
                String principal = securityContext.getUserPrincipal().getName();
                if (communityRunStatsRepository.isUserAlreadyPartOfRun(slug, principal)) {
                    boolean participating = true;
                    communityRunDetails.addParticipationDetails(participating);
                }
            }
            communityRunDetailsList.add(communityRunDetails);
        }
        return communityRunDetailsList;
    }

    private List<CommunityRunDetails> toCommunityRunDetailsList(List<CommunityRun> allActiveCommunityRunsWithNameLike) {
        List<CommunityRunDetails> communityRunDetailsList = new ArrayList<>();
        for (CommunityRun communityRun : allActiveCommunityRunsWithNameLike) {
            communityRunDetailsList.add(CommunityRunDetails.fromCommunityRun(communityRun));
        }
        return communityRunDetailsList;
    }

    @Path("/{slug}")
    @GET
    @Produces("application/json")
    public CommunityRun findCommunityRun(@NotNull @PathParam("slug") String slug) {
        return communityRunRepository.findBySlug(slug);
    }

    @Path("/{slug}/profiles_group_city")
    @GET
    @Produces("application/json")
    public List<ProfileGroupDetails> groupAllUsersInACommunityRunByCity(@NotNull @PathParam("slug") String slug) {
        List<ProfileGroupDetails> groups = communityRunRepository.groupAllUserInACommunityRunByCity(slug);
        List<ProfileGroupDetails> groupsWithLngLat = groups.stream().map(group -> ProfileGroupDetails.withLatLng(group, cityCache.findLatLng(group.getCity(), group.getCountry()))).collect(Collectors.toList());
        return groupsWithLngLat;
    }

    // TODO : CODE COPIED FROM CommunityRunView
    @Path("/{slug}/join")
    @POST
    @Produces("application/json")
    @LoggedIn
    public Response joinCommunityRun(@NotNull @PathParam("slug") final String slug) {
        if (!communityRunStatsRepository.communityRunExists(slug)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String principal = securityContext.getUserPrincipal().getName();
        if (communityRunStatsRepository.isUserAlreadyPartOfRun(slug, principal)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("You are already part of this community run").build();
        }
        Profile profile = profileRepository.findProfile(principal);
        logger.info("Adding profile {} to community run {}", principal, slug);
        CommunityRun communityRun = communityRunRepository.addRunnerToCommunityRun(slug, profile);

        Goal goal = Goal.newCommunityRunGoal(communityRun);
        logger.info("Creating a CommunityRun goal for profile {}", principal);
        Goal savedGoal = goalRepository.save(goal, profile);
        logger.info("Created a new goal with id {}", savedGoal.getId());

        communityRunStatsRepository.addGoalToCommunityRun(slug, savedGoal.getId());
        communityRunStatsRepository.addRunnerToCommunityRun(slug, profile);
        return Response.status(Response.Status.CREATED).entity(savedGoal).build();
    }

    @Path("/{slug}/leave")
    @POST
    @Produces("application/json")
    @LoggedIn
    public Response leaveCommunityRun(@NotNull @PathParam("slug") final String slug) {
        if (!communityRunStatsRepository.communityRunExists(slug)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String principal = securityContext.getUserPrincipal().getName();
        if (!communityRunStatsRepository.isUserAlreadyPartOfRun(slug, principal)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("You are not part of this community run.").build();
        }
        Profile profile = profileRepository.findProfile(principal);
        logger.info("User {} leaving community run {}", principal, slug);
        communityRunRepository.leaveCommunityRun(slug, profile);
        goalRepository.archiveGoalWithCommunityRun(communityRunRepository.find(slug), profile);
        communityRunStatsRepository.removeRunnerFromCommunityRun(slug, principal);
        return Response.status(Response.Status.OK).build();
    }

}
