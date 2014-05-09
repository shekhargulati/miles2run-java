package org.miles2run.jaxrs.api.v1;

import org.miles2run.business.services.CounterService;
import org.miles2run.jaxrs.vo.Counter;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Created by shekhargulati on 17/03/14.
 */
@Path("/api/v1/counters")
public class CounterResource {

    @Inject
    private CounterService counterService;


    @GET
    @Produces("application/json")
    public Counter appCounter() {
        Long runCounter = counterService.getRunCounter();
        Long countryCounter = counterService.getCountryCounter();
        Long developerCounter = counterService.getDeveloperCounter();
        return new Counter(developerCounter, countryCounter, runCounter);
    }
}
