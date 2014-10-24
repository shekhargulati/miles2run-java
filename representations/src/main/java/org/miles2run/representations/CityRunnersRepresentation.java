package org.miles2run.representations;

public class CityRunnersRepresentation {

    private final long count;
    private final String city;
    private final String country;
    private final double[] latLng;

    public CityRunnersRepresentation(long count, String city, String country, double[] latLng) {
        this.count = count;
        this.city = city;
        this.country = country;
        this.latLng = latLng;
    }

    public long getCount() {
        return count;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public double[] getLatLng() {
        return latLng;
    }
}
