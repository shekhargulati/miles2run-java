package org.miles2run.rest.representations;

import org.miles2run.domain.entities.CommunityRunGoal;
import org.miles2run.domain.entities.GoalType;

import java.util.Date;

public class CommunityRunGoalRepresentation implements GoalRepresentation {

    private final Long id;
    private final String purpose;
    private final boolean archived;
    private final Date startDate;
    private final Date endDate;
    private final GoalType goalType;
    private final Date communityRunStartDate;
    private final Date communityRunEndDate;

    public CommunityRunGoalRepresentation(CommunityRunGoal goal) {
        this.id = goal.getId();
        this.purpose = goal.getPurpose();
        this.archived = goal.isArchived();
        this.startDate = goal.getDuration().getStartDate();
        this.endDate = goal.getDuration().getEndDate();
        this.goalType = GoalType.COMMUNITY_RUN_GOAL;
        this.communityRunStartDate = goal.getCommunityRun().getDuration().getStartDate();
        this.communityRunEndDate = goal.getCommunityRun().getDuration().getEndDate();
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
