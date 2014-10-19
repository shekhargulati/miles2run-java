package org.miles2run.representations;

import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.GoalType;

import java.util.Date;

public class DurationGoalDetails implements GoalDetails {

    private final Long id;
    private final String purpose;
    private final boolean archived;
    private final Date startDate;
    private final Date endDate;
    private final GoalType goalType;

    public DurationGoalDetails(Goal goal) {
        this.id = goal.getId();
        this.purpose = goal.getPurpose();
        this.archived = goal.isArchived();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.goalType = goal.getGoalType();
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
}
