package org.miles2run.business.domain.redis;

/**
 * Created by shekhargulati on 23/07/14.
 */
public class CommunityRunCounter {

    private final Long runners;
    private final Long countries;
    private final Long cities;
    private final Long totalDistance;
    private final Long totalDuration;

    public CommunityRunCounter(Long runners, Long countries, Long cities, Long totalDistance, Long totalDuration) {
        this.runners = runners;
        this.countries = countries;
        this.cities = cities;
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
    }

    public Long getRunners() {
        return runners;
    }

    public Long getCountries() {
        return countries;
    }

    public Long getCities() {
        return cities;
    }

    public Long getTotalDistance() {
        return totalDistance;
    }

    public Long getTotalDuration() {
        return totalDuration;
    }
}
