package org.miles2run.jaxrs.vo;

import org.miles2run.business.domain.jpa.Goal;

import java.util.Date;

/**
 * Created by shekhargulati on 23/07/14.
 */
public class CommunityRunGoalDetails {

    private final Long id;
    private final String purpose;
    private final boolean archived;
    private final Date startDate;
    private final Date endDate;

    public CommunityRunGoalDetails(Goal goal) {
        this.id = goal.getId();
        this.purpose = goal.getPurpose();
        this.archived = goal.isArchived();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
    }

    public Long getId() {
        return id;
    }

    public String getPurpose() {
        return purpose;
    }

    public boolean isArchived() {
        return archived;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
