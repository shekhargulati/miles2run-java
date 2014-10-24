package org.miles2run.views.views;

public class UserViewRepresentation {

    private final String username;
    private final String fullname;
    private final String bio;
    private final String connectionId;
    private final String profilePic;
    private final String city;
    private final String country;
    private String email;
    private String gender;

    private UserViewRepresentation(String username, String fullname, String bio, String connectionId, String profilePic, String city, String country) {
        this.username = username;
        this.fullname = fullname;
        this.bio = bio;
        this.connectionId = connectionId;
        this.profilePic = profilePic;
        this.city = city;
        this.country = country;
    }

    public static UserViewRepresentation createUserRepresentation(String username, String fullname, String bio, String connectionId, String profilePic, String city, String country) {
        return new UserViewRepresentation(username, fullname, bio, connectionId, profilePic, city, country);
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

    public UserViewRepresentation addEmail(String email) {
        this.email = email;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public UserViewRepresentation setGender(String gender) {
        this.gender = gender;
        return this;
    }
}
