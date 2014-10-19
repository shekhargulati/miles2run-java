package org.miles2run.core.vo;

public class CityAndCountry {
    private String city;
    private String country;

    public CityAndCountry() {
    }

    public CityAndCountry(String city, String country) {
        this.city = city;
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }
}
