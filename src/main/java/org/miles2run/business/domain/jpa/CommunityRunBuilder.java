package org.miles2run.business.domain.jpa;

import java.util.Date;

public class CommunityRunBuilder {
    private String name;
    private String bannerImg;
    private String slug;
    private String description;
    private Date startDate;
    private Date endDate;
    private String website;
    private String twitterHandle;

    public CommunityRunBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public CommunityRunBuilder setBannerImg(String bannerImg) {
        this.bannerImg = bannerImg;
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

    public CommunityRunBuilder setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public CommunityRunBuilder setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public CommunityRun createCommunityRun() {
        return new CommunityRun(name, bannerImg, slug, description, startDate, endDate, website, twitterHandle);
    }
}