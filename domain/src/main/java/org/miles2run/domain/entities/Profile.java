package org.miles2run.domain.entities;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.miles2run.domain.bean_validation.ImageUrl;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profile", indexes = {
        @Index(unique = true, columnList = "username"),
        @Index(unique = true, columnList = "email")
})
@Access(AccessType.FIELD)
public class Profile extends BaseEntity {

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "profile")
    private final List<SocialConnection> socialConnections = new ArrayList<>();
    @NotBlank
    @Column(unique = true)
    @Email
    private String email;
    @NotBlank
    @Column(unique = true, updatable = false)
    @Size(max = 20)
    private String username;
    @NotBlank
    @Size(max = 50)
    private String fullname;
    @Size(max = 500)
    private String bio;
    @NotBlank
    private String city;
    @NotBlank
    private String country;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @ImageUrl
    private String profilePic;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Transient
    private String miniProfilePic;

    @Transient
    private String biggerProfilePic;

    public Profile() {
    }

    public Profile(final String email, final String username, final String bio, final String city, final String country, final String fullname, final Gender gender, final String profilePic) {
        this.email = email;
        this.username = username.toLowerCase();
        this.bio = bio;
        this.city = city;
        this.country = country;
        this.fullname = fullname;
        this.gender = gender;
        this.profilePic = profilePic;
    }

    public Profile(Profile p) {
        this.username = p.username;
        this.bio = p.bio;
        this.city = p.city;
        this.country = p.country;
        this.fullname = p.fullname;
        this.profilePic = p.profilePic;
        this.gender = p.gender;
        this.createdAt = p.createdAt;
        this.role = p.role;
    }

    private Profile(String email, String username, String fullname, String city, String country, Gender gender) {
        this.email = email;
        this.username = username;
        this.fullname = fullname;
        this.city = city;
        this.country = country;
        this.gender = gender;
    }

    private Profile(String fullname, String bio, String city, String country, Gender gender) {
        this.fullname = fullname;
        this.bio = bio;
        this.city = city;
        this.country = country;
        this.gender = gender;
    }

    public static Profile createProfile(String email, String username, String fullname, String city, String country, Gender gender) {
        return new Profile(email, username, fullname, city, country, gender);
    }

    public static Profile createProfileForUpdate(String fullname, String bio, String city, String country, Gender gender) {
        return new Profile(fullname, bio, city, country, gender);
    }

    public String getEmail() {
        return email;
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

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public Gender getGender() {
        return gender;
    }

    public List<SocialConnection> getSocialConnections() {
        return socialConnections;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public Role getRole() {
        return role;
    }

    public String getMiniProfilePic() {
        return getImageWithSize("mini");
    }

    private String getImageWithSize(String size) {
        if (this.profilePic != null) {
            int index = this.profilePic.lastIndexOf(".");
            String imgPrefix = this.profilePic.substring(0, index);
            String picExtension = this.profilePic.substring(index);
            return new StringBuilder(imgPrefix).append("_").append(size).append(picExtension).toString();
        }
        return this.profilePic;
    }

    public String getBiggerProfilePic() {
        return getImageWithSize("bigger");
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullname + '\'' +
                ", bio='" + bio + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", profilePic='" + profilePic + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;

        Profile profile = (Profile) o;

        if (!email.equals(profile.email)) return false;
        if (!username.equals(profile.username)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = email.hashCode();
        result = 31 * result + username.hashCode();
        return result;
    }
}
