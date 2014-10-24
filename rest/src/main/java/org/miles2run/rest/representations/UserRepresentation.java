package org.miles2run.rest.representations;

import org.miles2run.domain.entities.Profile;

public class UserRepresentation {

    private String bio;
    private String username;
    private String fullname;
    private String pic;
    private String city;
    private String country;

    protected UserRepresentation() {
    }

    private UserRepresentation(String bio, String username, String fullname, String pic, String city, String country) {
        this.bio = bio;
        this.username = username;
        this.fullname = fullname;
        this.pic = pic;
        this.city = city;
        this.country = country;
    }

    public static UserRepresentation from(Profile profile) {
        return new UserRepresentation(profile.getBio(), profile.getUsername(), profile.getFullname(), profile.getProfilePic(), profile.getCity(), profile.getCountry());
    }

    public String getBio() {
        return bio;
    }

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public String getPic() {
        return pic;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }
}
