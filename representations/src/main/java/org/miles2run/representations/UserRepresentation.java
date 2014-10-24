package org.miles2run.representations;

import org.miles2run.domain.entities.Gender;
import org.miles2run.domain.entities.Profile;
import org.miles2run.domain.entities.Role;

import java.util.Date;

public class UserRepresentation {

    private String bio;
    private String username;
    private String fullname;
    private String pic;
    private String city;
    private String country;
    private Gender gender;
    private Date createdAt;
    private Role role;

    protected UserRepresentation() {
    }

    private UserRepresentation(String bio, String username, String fullname, String pic, String city, String country, Gender gender, Date createdAt, Role role) {
        this.bio = bio;
        this.username = username;
        this.fullname = fullname;
        this.pic = pic;
        this.city = city;
        this.country = country;
        this.gender = gender;
        this.createdAt = createdAt;
        this.role = role;
    }

    public static UserRepresentation from(Profile profile) {
        return new UserRepresentation(profile.getBio(), profile.getUsername(), profile.getFullname(), profile.getProfilePic(), profile.getCity(), profile.getCountry(), profile.getGender(), profile.getCreatedAt(), profile.getRole());
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

    public Gender getGender() {
        return gender;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Role getRole() {
        return role;
    }
}
