package org.miles2run.jaxrs.vo;

import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.redis.CommunityRunStats;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by shekhargulati on 23/07/14.
 */
public class CommunityRunDetails {

    private final String name;

    private final String bannerImg;

    private final String slug;

    private final String description;

    private final Date startDate;

    private final Date endDate;

    private final String website;

    private final String twitterHandle;

    private final Set<String> hashtags = new HashSet<>();

    private boolean loggedInUserParticipating = false;

    private CommunityRunStats stats;

    private CommunityRunDetails(CommunityRun communityRun) {
        this.name = communityRun.getName();
        this.bannerImg = communityRun.getBannerImg();
        this.slug = communityRun.getSlug();
        this.description = communityRun.getDescription();
        this.startDate = communityRun.getStartDate();
        this.endDate = communityRun.getEndDate();
        this.website = communityRun.getWebsite();
        this.twitterHandle = communityRun.getTwitterHandle();
        this.hashtags.addAll(communityRun.getHashtags());
    }

    public static CommunityRunDetails fromCommunityRun(CommunityRun communityRun) {
        return new CommunityRunDetails(communityRun);
    }

    public CommunityRunDetails addParticipationDetails(boolean participating) {
        this.loggedInUserParticipating = participating;
        return this;
    }

    public CommunityRunDetails addStats(CommunityRunStats stats) {
        this.stats = stats;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getBannerImg() {
        return bannerImg;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getWebsite() {
        return website;
    }

    public String getTwitterHandle() {
        return twitterHandle;
    }

    public Set<String> getHashtags() {
        return hashtags;
    }

    public boolean isLoggedInUserParticipating() {
        return loggedInUserParticipating;
    }

    public CommunityRunStats getStats() {
        return stats;
    }
}
