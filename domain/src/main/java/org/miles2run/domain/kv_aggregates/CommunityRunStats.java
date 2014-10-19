package org.miles2run.domain.kv_aggregates;

public class CommunityRunStats {

    private final Long runners;
    private final Long countries;
    private final Long cities;
    private final Double totalDistance;
    private final Long totalDuration;

    public CommunityRunStats(Long runners, Long countries, Long cities, Double totalDistance, Long totalDuration) {
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

    public Double getTotalDistance() {
        return totalDistance;
    }

    public Long getTotalDuration() {
        return totalDuration;
    }
}
