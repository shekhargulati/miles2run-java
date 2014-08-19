package org.miles2run.jaxrs.vo;

import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.GoalType;

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
    private final GoalType goalType;
    private final Date communityRunStartDate;
    private final Date communityRunEndDate;

    public CommunityRunGoalDetails(Goal goal) {
        this.id = goal.getId();
        this.purpose = goal.getPurpose();
        this.archived = goal.isArchived();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.goalType = goal.getGoalType();
        this.communityRunStartDate = goal.getCommunityRun().getStartDate();
        this.communityRunEndDate = goal.getCommunityRun().getEndDate();
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

    public GoalType getGoalType() {
        return goalType;
    }

    public Date getCommunityRunStartDate() {
        return communityRunStartDate;
    }

    public Date getCommunityRunEndDate() {
        return communityRunEndDate;
    }
}
