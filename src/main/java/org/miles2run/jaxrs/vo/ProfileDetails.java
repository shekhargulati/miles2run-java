package org.miles2run.jaxrs.vo;

import org.miles2run.business.domain.jpa.GoalUnit;
import org.miles2run.jaxrs.forms.ProfileForm;

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
    private GoalUnit goalUnit;
    private long goal;
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

    public ProfileDetails(ProfileForm profileForm) {
        this.username = profileForm.getUsername();
        this.fullname = profileForm.getFullname();
        this.bio = profileForm.getBio();
        this.connectionId = profileForm.getConnectionId();
        this.profilePic = profileForm.getProfilePic();
        this.city = profileForm.getCity();
        this.country = profileForm.getCountry();
        this.email = profileForm.getEmail();
        if (profileForm.getGender() != null) {
            this.gender = profileForm.getGender().getGender();
        }
        this.goal = profileForm.getGoal();
        this.goalUnit = profileForm.getGoalUnit();
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

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public long getGoal() {
        return goal;
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

