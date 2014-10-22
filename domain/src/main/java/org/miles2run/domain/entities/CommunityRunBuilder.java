package org.miles2run.domain.entities;

public class CommunityRunBuilder {
    private String name;
    private String slug;
    private String description;
    private Duration duration;
    private String website;
    private String twitterHandle;
    private String bannerImg;

    public CommunityRunBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public CommunityRunBuilder setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public CommunityRunBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public CommunityRunBuilder setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public CommunityRunBuilder setWebsite(String website) {
        this.website = website;
        return this;
    }

    public CommunityRunBuilder setTwitterHandle(String twitterHandle) {
        this.twitterHandle = twitterHandle;
        return this;
    }

    public CommunityRunBuilder setBannerImg(String bannerImg) {
        this.bannerImg = bannerImg;
        return this;
    }

    public CommunityRun createCommunityRun() {
        return CommunityRun.createCommunityRun(name, slug, description, duration, website, twitterHandle, bannerImg);
    }
}