package org.miles2run.business.domain;

/**
 * Created by shekhargulati on 07/05/14.
 */
public class Counter {

    private final long runners;
    private final long countries;
    private final double distance;
    private final long cities;
    private final long seconds;

    public Counter(long runners, long countries, double distance, long cities, long seconds) {
        this.runners = runners;
        this.countries = countries;
        this.distance = distance;
        this.cities = cities;
        this.seconds = seconds;
    }

    public long getRunners() {
        return runners;
    }

    public long getCountries() {
        return countries;
    }

    public double getDistance() {
        return distance;
    }

    public long getCities() {
        return cities;
    }

    public long getSeconds() {
        return seconds;
    }
}
