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
    public List<Map<String, Object>> getDataForDistanceCovered(@QueryParam("interval") String interval) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        switch (interval) {
            case "day":
                return getMapDataForDay();
            case "month":
                return getMapDataForMonth();
            case "year":
                return getMapDataForYear();
            default:
                return getMapDataForDay();
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

    private List<Map<String, Object>> getMapDataForMonth() {
        List<Map<String, Object>> chartData = new ArrayList<>();
        chartData.add(newEntryForMonth("2014-01", 40));
        chartData.add(newEntryForMonth("2014-02", 15));
        chartData.add(newEntryForMonth("2014-03", 90));
        chartData.add(newEntryForMonth("2014-04", 80));
        chartData.add(newEntryForMonth("2014-05", 70));
        chartData.add(newEntryForMonth("2014-06", 60));
        chartData.add(newEntryForMonth("2014-07", 50));
        chartData.add(newEntryForMonth("2014-08", 35));
        chartData.add(newEntryForMonth("2014-09", 100));
        return chartData;
    }

    private Map<String, Object> newEntryForMonth(String month, long distance) {
        Map<String, Object> map = new HashMap<>();
        map.put("month", month);
        map.put("distance", distance);
        return map;
    }

    private List<Map<String, Object>> getMapDataForDay() {
        List<Map<String, Object>> chartData = new ArrayList<>();
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

    private Map<String, Object> getData(int year, int month, int date, long distance) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("day", toTimeInMillesconds(year, month, date));
        data.put("distance", distance);
        return data;
    }

    private long toTimeInMillesconds(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);
        return calendar.getTimeInMillis();
    }
}
