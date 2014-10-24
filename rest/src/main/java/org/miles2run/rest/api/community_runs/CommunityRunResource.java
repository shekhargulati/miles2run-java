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
import org.miles2run.domain.entities.*;
import org.miles2run.rest.representations.CityRunnersRepresentation;
import org.miles2run.rest.representations.CommunityRunRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
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
        Profile profile = profileRepository.findByUsername(loggedInUser);
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
    public List<CommunityRunRepresentation> findAll(@QueryParam("name") String name, @QueryParam("include_stats") boolean includeStats, @QueryParam("include_participation_detail") boolean includeParticipationDetail, @QueryParam("max") int max, @QueryParam("page") int page) {
        String username = securityContext.getUserPrincipal().getName();
        page = page == 0 ? 1 : page;
        max = max > 20 ? 20 : max;
        if (StringUtils.isNotBlank(name)) {
            return communityRunsWithNameLike(name, page, max);
        }
        return communityRuns(includeStats, includeParticipationDetail, page, max, username);
    }

    private List<CommunityRunRepresentation> communityRunsWithNameLike(String name, int page, int max) {
        List<CommunityRun> communityRuns = communityRunRepository.findAllActiveCommunityRunsWithNameLike(name, page, max);
        return toCommunityRunRepresentations(communityRuns);
    }

    private List<CommunityRunRepresentation> communityRuns(boolean includeStats, boolean includeParticipationDetail, int page, int max, String username) {
        List<CommunityRun> communityRuns = communityRunRepository.findAllActiveCommunityRuns(page, max);
        return communityRuns.stream().map(activeCommunityRun -> {
            CommunityRunRepresentation representation = toCommunityRunRepresentation(activeCommunityRun);
            String slug = activeCommunityRun.getSlug();
            if (includeStats) {
                representation.addStats(communityRunStatsRepository.getCurrentStatsForCommunityRun(slug));
            }
            if (includeParticipationDetail && username != null) {
                if (communityRunStatsRepository.isUserAlreadyPartOfRun(slug, username)) {
                    representation.addParticipationDetails(true);
                }
            }
            return representation;
        }).collect(Collectors.toList());
    }

    private List<CommunityRunRepresentation> toCommunityRunRepresentations(List<CommunityRun> communityRuns) {
        return communityRuns.stream().map(CommunityRunRepresentation::from).collect(Collectors.toList());
    }

    private CommunityRunRepresentation toCommunityRunRepresentation(CommunityRun activeCommunityRun) {
        return CommunityRunRepresentation.from(activeCommunityRun);
    }

    @Path("/{slug}")
    @GET
    @Produces("application/json")
    public CommunityRunRepresentation findCommunityRun(@NotNull @PathParam("slug") String slug) {
        return CommunityRunRepresentation.from(communityRunRepository.findBySlug(slug));
    }

    @Path("/{slug}/runners_by_city")
    @GET
    @Produces("application/json")
    public List<CityRunnersRepresentation> groupRunnersByCity(@NotNull @PathParam("slug") String slug) {
        Set<Profile> runners = communityRunRepository.allRunners(slug);
        Map<CountryCityTuple, Set<Profile>> runnersByCity = groupByCity(runners);
        return toCityRunnersRepresentations(runnersByCity);
    }

    private Map<CountryCityTuple, Set<Profile>> groupByCity(Set<Profile> runners) {
        Map<CountryCityTuple, Set<Profile>> runnersByCity = new HashMap<>();
        for (Profile runner : runners) {
            String city = runner.getCity();
            CountryCityTuple tuple = new CountryCityTuple(runner.getCountry(), city);
            Set<Profile> runnersForCity = runnersByCity.get(tuple);
            if (runnersForCity == null) {
                runnersForCity = new HashSet<>();
                runnersForCity.add(runner);
                runnersByCity.put(tuple, runnersForCity);
            } else {
                runnersForCity.add(runner);
            }
        }
        return runnersByCity;
    }

    private List<CityRunnersRepresentation> toCityRunnersRepresentations(Map<CountryCityTuple, Set<Profile>> runnersByCity) {
        Set<Map.Entry<CountryCityTuple, Set<Profile>>> entries = runnersByCity.entrySet();
        return entries.stream().map(this::toCityRunnersRepresentation).collect(Collectors.toList());
    }

    private CityRunnersRepresentation toCityRunnersRepresentation(Map.Entry<CountryCityTuple, Set<Profile>> entry) {
        CountryCityTuple tuple = entry.getKey();
        Set<Profile> cityRunners = entry.getValue();
        double[] latLng = cityCache.findLatLng(tuple.getCity(), tuple.getCountry());
        return new CityRunnersRepresentation(cityRunners.size(), tuple.getCity(), tuple.getCountry(), latLng);
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
        Profile profile = profileRepository.findByUsername(principal);
        logger.info("Adding profile {} to community run {}", principal, slug);
        CommunityRun communityRun = communityRunRepository.addRunnerToCommunityRun(slug, profile);

        CommunityRunGoal goal = new CommunityRunGoalBuilder()
                .setCommunityRun(communityRun)
                .setDuration(new Duration(new Date(), communityRun.getDuration().getEndDate()))
                .setPurpose(communityRun.getName())
                .setProfile(profile)
                .createCommunityRunGoal();
        logger.info("Creating a CommunityRun goal for profile {}", principal);
        CommunityRunGoal savedGoal = goalRepository.save(goal);
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
        Profile profile = profileRepository.findByUsername(principal);
        logger.info("User {} leaving community run {}", principal, slug);
        communityRunRepository.leaveCommunityRun(slug, profile);
        goalRepository.archiveGoalWithCommunityRun(communityRunRepository.find(slug), profile);
        communityRunStatsRepository.removeRunnerFromCommunityRun(slug, principal);
        return Response.status(Response.Status.OK).build();
    }

}
