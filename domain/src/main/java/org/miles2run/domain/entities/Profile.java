package org.miles2run.domain.entities;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.miles2run.domain.bean_validation.ImageUrl;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "profile", indexes = {
        @Index(unique = true, columnList = "username"),
        @Index(unique = true, columnList = "email")
})
@NamedEntityGraphs(value = {
        @NamedEntityGraph(
                name = "Profile.OnlyUsername",
                attributeNodes = {
                        @NamedAttributeNode("username")
                }
        ),
        @NamedEntityGraph(
                name = "Profile.WithConnections",
                attributeNodes = {
                        @NamedAttributeNode("username"),
                        @NamedAttributeNode("socialConnections")
                }
        )
})
@Access(AccessType.FIELD)
public class Profile extends BaseEntity {


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialConnection> socialConnections;

    @NotBlank
    @Column(unique = true)
    @Email
    @Basic(fetch = FetchType.LAZY)
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

    protected Profile() {
    }

    private Profile(String email, String username, String fullname, String city, String country, Gender gender) {
        this.email = email;
        this.username = username;
        this.fullname = fullname;
        this.city = city;
        this.country = country;
        this.gender = gender;
    }

    static Profile createProfile(String email, String username, String fullname, String city, String country, Gender gender) {
        return new Profile(email, username, fullname, city, country, gender);
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

    public String getProfilePic() {
        return profilePic;
    }

    public Role getRole() {
        return role;
    }

    public List<SocialConnection> getSocialConnections() {
        if (socialConnections == null) {
            socialConnections = new ArrayList<>();
        }
        return Collections.unmodifiableList(socialConnections);
    }

    public Profile addSocialConnection(SocialConnection socialConnection) {
        if (socialConnections == null) {
            socialConnections = new ArrayList<>();
        }
        socialConnections.add(socialConnection);
        socialConnection.setProfile(this);
        return this;
    }

    public Profile removeSocialConnection(SocialConnection socialConnection) {
        if (socialConnections.remove(socialConnection)) {
            socialConnection.setProfile(null);
        }
        return this;
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

    @Override
    public String toString() {
        return "Profile{" +
                "id='" + id + '\'' +
                "email='" + email + '\'' +
                ", fullname='" + fullname + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
