package org.miles2run.core.vo;

public class ProfileGroupDetails {

    private final long count;
    private final String city;
    private final String country;
    private double[] latLng;

    public ProfileGroupDetails(long count, String city, String country) {
        this.count = count;
        this.city = city;
        this.country = country;
    }

    private ProfileGroupDetails(long count, String city, String country, double[] latLng) {
        this.count = count;
        this.city = city;
        this.country = country;
        this.latLng = latLng;
    }

    public static ProfileGroupDetails withLatLng(ProfileGroupDetails profileGroupDetails, double[] latLng) {
        return new ProfileGroupDetails(profileGroupDetails.getCount(), profileGroupDetails.getCity(), profileGroupDetails.getCountry(), latLng);
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProfileGroupDetails{");
        sb.append("count=").append(count);
        sb.append(", city='").append(city).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
