package org.miles2run.business.domain.jpa;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.miles2run.business.bean_validation.ImageUrl;
import org.miles2run.jaxrs.forms.ProfileForm;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by shekhargulati on 04/03/14.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Profile.findByUsername", query = "select new Profile(p) from Profile p where p.username =:username"),
        @NamedQuery(name = "Profile.findByEmail", query = "select new Profile(p) from Profile p where p.email =:email"),
        @NamedQuery(name = "Profile.findProfileWithSocialNetworks", query = "select p.id,p.username, s.provider from Profile p JOIN p.socialConnections s where p.username =:username"),
        @NamedQuery(name = "Profile.findFullProfileByUsername", query = "select new Profile(p) from Profile p where p.username =:username")
})
@Table(name = "profile", indexes = {
        @Index(unique = true, columnList = "username"),
        @Index(unique = true, columnList = "email")
})
@Access(AccessType.FIELD)
public class Profile implements Serializable {

    @Id
    @TableGenerator(name = "profile_generator", table = "id_gen", allocationSize = 100)
    @GeneratedValue(generator = "profile_generator")
    private Long id;

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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "profile")
    private final List<SocialConnection> socialConnections = new ArrayList<>();

    @Temporal(TemporalType.DATE)
    @NotNull
    @Column(updatable = false)
    private Date registeredOn = new Date();

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

    public Profile(Profile p) {
        this.username = p.username;
        this.bio = p.bio;
        this.city = p.city;
        this.country = p.country;
        this.fullname = p.fullname;
        this.profilePic = p.profilePic;
        this.gender = p.gender;
        this.registeredOn = new Date(((java.sql.Date) p.registeredOn).getTime());
    }

    public Profile(ProfileForm profileForm) {
        this.email = profileForm.getEmail();
        this.username = profileForm.getUsername().toLowerCase();
        this.bio = profileForm.getBio();
        this.city = profileForm.getCity();
        this.country = profileForm.getCountry();
        this.fullname = profileForm.getFullname();
        this.gender = profileForm.getGender();
        this.profilePic = profileForm.getProfilePic();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullName) {
        this.fullname = fullName;
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


    public List<SocialConnection> getSocialConnections() {
        return socialConnections;
    }

    public Date getRegisteredOn() {
        return registeredOn;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getMiniProfilePic() {
        return getImageWithSize("mini");
    }

    public String getBiggerProfilePic() {
        return getImageWithSize("bigger");
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }


    public Role getRole() {
        return role;
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
                ", registeredOn=" + registeredOn +
                '}';
    }
}
