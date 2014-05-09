package org.miles2run.jaxrs.vo;

/**
 * Created by shekhargulati on 06/03/14.
 */
public class ProfileDetails {

    private final String username;
    private final String fullname;
    private final String bio;
    private final String connectionId;
    private final String profilePic;
    private final String city;
    private final String country;
    private String email;
    private String gender;

    public ProfileDetails(String username, String fullname, String bio, String connectionId, String profilePic, String city, String country) {
        this.username = username;
        this.fullname = fullname;
        this.bio = bio;
        this.connectionId = connectionId;
        this.profilePic = profilePic;
        this.city = city;
        this.country = country;
    }

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public String getBio() {
        return bio;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "ProfileDetails{" +
                "username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", bio='" + bio + '\'' +
                ", connectionId='" + connectionId + '\'' +
                ", profilePic='" + profilePic + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}

