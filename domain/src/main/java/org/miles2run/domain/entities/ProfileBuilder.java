package org.miles2run.domain.entities;

public class ProfileBuilder {
    private String email;
    private String username;
    private String fullname;
    private String city;
    private String country;
    private Gender gender;

    public ProfileBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public ProfileBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public ProfileBuilder setFullname(String fullname) {
        this.fullname = fullname;
        return this;
    }

    public ProfileBuilder setCity(String city) {
        this.city = city;
        return this;
    }

    public ProfileBuilder setCountry(String country) {
        this.country = country;
        return this;
    }

    public ProfileBuilder setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public Profile createProfile() {
        return Profile.createProfile(email, username, fullname, city, country, gender);
    }
}