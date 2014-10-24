package org.miles2run.rest.representations;

import org.miles2run.domain.entities.CommunityRun;
import org.miles2run.domain.kv_aggregates.CommunityRunAggregate;

import java.util.Date;

public class CommunityRunRepresentation {

    private final String name;

    private final String bannerImg;

    private final String slug;

    private final String description;

    private final Date startDate;

    private final Date endDate;

    private final String website;

    private final String twitterHandle;

    private boolean loggedInUserParticipating = false;

    private CommunityRunAggregate stats;


    public CommunityRunRepresentation(String name, String bannerImg, String slug, String description, Date startDate, Date endDate, String website, String twitterHandle) {
        this.name = name;
        this.bannerImg = bannerImg;
        this.slug = slug;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.website = website;
        this.twitterHandle = twitterHandle;
    }

    public static CommunityRunRepresentation from(CommunityRun communityRun) {
        return new CommunityRunRepresentation(communityRun.getName(), communityRun.getBannerImg(), communityRun.getSlug(), communityRun.getDescription(), communityRun.getDuration().getStartDate(), communityRun.getDuration().getEndDate(), communityRun.getWebsite(), communityRun.getTwitterHandle());
    }

    public void addStats(CommunityRunAggregate currentStatsForCommunityRun) {
        this.stats = currentStatsForCommunityRun;
    }

    public void addParticipationDetails(boolean participating) {
        this.loggedInUserParticipating = participating;
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

    public boolean isLoggedInUserParticipating() {
        return loggedInUserParticipating;
    }

    public CommunityRunAggregate getStats() {
        return stats;
    }
}
