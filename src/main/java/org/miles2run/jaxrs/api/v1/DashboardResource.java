package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.jaxrs.filters.InjectProfile;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

/**
 * Created by shekhargulati on 06/06/14.
 */
@Path("/api/v1/dashboard")
public class DashboardResource {

    @Context
    private SecurityContext securityContext;

    @GET
    @LoggedIn
    @Produces("application/json")
    @Path("/charts/distance")
    public List<Map<String, Long>> getDataForDistanceCovered(@QueryParam("interval") String interval) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        List<Map<String, Long>> chartData = new ArrayList<>();
        chartData.add(getData(2014, 5, 1, 10));
        chartData.add(getData(2014, 5, 2, 10));
        chartData.add(getData(2014, 5, 3, 5));
        chartData.add(getData(2014, 5, 4, 7));
        chartData.add(getData(2014, 5, 5, 12));
        chartData.add(getData(2014, 5, 6, 10));
        chartData.add(getData(2014, 5, 7, 8));
        chartData.add(getData(2014, 5, 8, 6));
        chartData.add(getData(2014, 5, 9, 5));
        chartData.add(getData(2014, 5, 10, 2));
        chartData.add(getData(2014, 5, 11, 3));
        chartData.add(getData(2014, 5, 12, 11));
        return chartData;
    }

    private Map<String, Long> getData(int year, int month, int date, long distance) {
        Map<String, Long> data = new LinkedHashMap<>();
        data.put("activityDate", toTimeInMillesconds(year, month, date));
        data.put("distance", distance);
        return data;
    }

    private long toTimeInMillesconds(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);
        return calendar.getTimeInMillis();
    }
}
