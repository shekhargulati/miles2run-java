package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.services.TimelineService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

/**
 * Created by shekhargulati on 06/06/14.
 */
@Path("/api/v1/dashboard")
public class DashboardResource {

    @Context
    private SecurityContext securityContext;
    @Inject
    private TimelineService timelineService;
    @Inject
    private ProfileService profileService;


    @GET
    @LoggedIn
    @Produces("application/json")
    @Path("/charts/distance")
    public List<Map<String, Object>> getDataForDistanceCovered(@QueryParam("interval") String interval) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        switch (interval) {
            case "day":
                return timelineService.distanceCoveredOverTime(profile, interval, 15);
            case "month":
                return timelineService.distanceCoveredOverTime(profile, interval, 6);
            default:
                return timelineService.distanceCoveredOverTime(profile, interval, 15);
        }
    }

    private List<Map<String, Object>> getMapDataForYear() {
        List<Map<String, Object>> chartData = new ArrayList<>();
        chartData.add(newEntryForYear("2010", 400));
        chartData.add(newEntryForYear("2011", 150));
        chartData.add(newEntryForYear("2012", 600));
        chartData.add(newEntryForYear("2013", 800));
        chartData.add(newEntryForYear("2014", 700));
        return chartData;
    }

    private Map<String, Object> newEntryForYear(String year, long distance) {
        Map<String, Object> map = new HashMap<>();
        map.put("year", year);
        map.put("distance", distance);
        return map;
    }


}
