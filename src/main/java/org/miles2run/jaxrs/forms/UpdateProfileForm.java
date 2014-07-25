package org.miles2run.jaxrs.forms;

import org.miles2run.business.domain.jpa.Gender;

import javax.ws.rs.FormParam;

/**
 * Created by shekhargulati on 10/03/14.
 */
public class UpdateProfileForm {

    @FormParam("fullname")
    private String fullname;

    @FormParam("bio")
    private String bio;

    @FormParam("city")
    private String city;

    @FormParam("country")
    private String country;

    @FormParam("gender")
    private Gender gender;


    public UpdateProfileForm() {
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


    @Override
    public String toString() {
        return "ProfileForm{" +
                ", fullname='" + fullname + '\'' +
                ", bio='" + bio + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
