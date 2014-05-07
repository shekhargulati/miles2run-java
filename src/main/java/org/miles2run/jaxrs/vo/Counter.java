package org.miles2run.jaxrs.vo;

/**
 * Created by shekhargulati on 07/05/14.
 */
public class Counter {

    private final Long developers;
    private final Long countries;
    private final Long totalDistanceCovered;

    public Counter(Long developers, Long countries, Long totalDistanceCovered) {
        this.developers = developers;
        this.countries = countries;
        this.totalDistanceCovered = totalDistanceCovered;
    }

    public Long getDevelopers() {
        return developers;
    }

    public Long getCountries() {
        return countries;
    }

    public Long getTotalDistanceCovered() {
        return totalDistanceCovered;
    }
}
