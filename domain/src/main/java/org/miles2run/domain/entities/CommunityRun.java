package org.miles2run.domain.entities;

import org.hibernate.validator.constraints.URL;
import org.miles2run.domain.bean_validation.DateRangeCheck;
import org.miles2run.domain.bean_validation.ImageUrl;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
@Table(name = "community_run", indexes = {
        @Index(columnList = "name", unique = true),
        @Index(columnList = "slug", unique = true)
})
@DateRangeCheck
public class CommunityRun extends BaseEntity {

    @NotNull
    @Size(max = 10)
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "hashtags", joinColumns = {@JoinColumn(name = "cr_id")})
    private final Set<String> hashtags = new HashSet<>();

    @OneToMany(orphanRemoval = true, mappedBy = "communityRun", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final Set<CommunityRunGoal> goals = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    private final Set<Profile> runners = new HashSet<>();

    @NotNull
    private String name;

    @NotNull
    private String slug;

    @NotNull
    @Size(max = 4000)
    private String description;

    @NotNull
    @Embedded
    private Duration duration;

    @URL
    private String website;

    @NotNull
    private String twitterHandle;

    @NotNull
    @ImageUrl
    private String bannerImg;

    private boolean active = true;


    protected CommunityRun() {
    }

    private CommunityRun(String name, String slug, String description, Duration duration, String website, String twitterHandle, String bannerImg) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.duration = duration;
        this.website = website;
        this.twitterHandle = twitterHandle;
        this.bannerImg = bannerImg;
    }

    static CommunityRun createCommunityRun(String name, String slug, String description, Duration duration, String website, String twitterHandle, String bannerImg) {
        return new CommunityRun(name, slug, description, duration, website, twitterHandle, bannerImg);
    }

    public Set<String> getHashtags() {
        return Collections.unmodifiableSet(hashtags);
    }

    public CommunityRun addHashtag(String hashtag) {
        hashtags.add(hashtag);
        return this;
    }

    public CommunityRun addHashtags(Set<String> hashtags) {
        hashtags.addAll(hashtags);
        return this;
    }

    public CommunityRun removeHashtags(String... tags) {
        hashtags.removeAll(Arrays.asList(tags));
        return this;
    }

    public Set<Profile> getRunners() {
        return Collections.unmodifiableSet(runners);
    }

    public CommunityRun addRunner(Profile runner) {
        runners.add(runner);
        return this;
    }

    public CommunityRun removeRunner(Profile runner) {
        runners.remove(runner);
        return this;
    }

    public Set<CommunityRunGoal> getGoals(){
        return Collections.unmodifiableSet(goals);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getTwitterHandle() {
        return twitterHandle;
    }

    public void setTwitterHandle(String twitterHandle) {
        this.twitterHandle = twitterHandle;
    }

    public String getBannerImg() {
        return bannerImg;
    }

    public void setBannerImg(String bannerImg) {
        this.bannerImg = bannerImg;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
