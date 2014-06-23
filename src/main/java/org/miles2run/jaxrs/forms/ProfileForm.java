package org.miles2run.jaxrs.forms;

import org.hibernate.validator.constraints.NotBlank;
import org.miles2run.business.domain.Gender;
import org.miles2run.business.domain.GoalUnit;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.FormParam;

/**
 * Created by shekhargulati on 10/03/14.
 */
public class ProfileForm {

    @FormParam("email")
    @NotBlank
    private String email;

    @FormParam("username")
    @NotBlank
    private String username;

    @FormParam("fullname")
    @NotBlank
    private String fullname;

    @FormParam("bio")
    @NotBlank
    @Size(max = 200)
    private String bio;

    @FormParam("city")
    @NotBlank
    private String city;

    @FormParam("country")
    @NotBlank
    private String country;

    @FormParam("gender")
    private Gender gender;

    @FormParam("goal")
    @NotNull
    private long goal;

    @NotNull
    @FormParam("goalUnit")
    private GoalUnit goalUnit;

    @FormParam("profilePic")
    private String profilePic;

    @FormParam("connectionId")
    private String connectionId;

    public ProfileForm() {
        this.goalUnit = GoalUnit.KM;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public long getGoal() {
        return goal;
    }

    public void setGoal(long goal) {
        this.goal = goal;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public void setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public String toString() {
        return "ProfileForm{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", bio='" + bio + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", gender='" + gender + '\'' +
                ", goal=" + goal +
                ", goalUnit='" + goalUnit + '\'' +
                ", profilePic='" + profilePic + '\'' +
                ", connectionId='" + connectionId + '\'' +
                '}';
    }
}
