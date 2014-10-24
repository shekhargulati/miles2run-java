package org.miles2run.rest.api.counters;

import org.miles2run.core.repositories.redis.CounterStatsRepository;
import org.miles2run.domain.kv_aggregates.CounterAggregate;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("counters")
public class CounterResource {

    @Inject
    private CounterStatsRepository counterStatsRepository;

    @GET
    @Produces("application/json")
    public CounterAggregate appCounter() {
        return counterStatsRepository.currentCounter();
    }
}
