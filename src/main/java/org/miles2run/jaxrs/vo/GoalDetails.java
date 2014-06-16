package org.miles2run.jaxrs.vo;

import org.miles2run.business.domain.Goal;
import org.miles2run.business.domain.GoalUnit;

import java.util.Date;

/**
 * Created by shekhargulati on 16/06/14.
 */
public class GoalDetails {

    private final Long id;
    private final String purpose;
    private final Date targetDate;
    private final long goal;
    private final GoalUnit goalUnit;
    private final boolean archived;
    private final long percentageCompleted;

    public GoalDetails(Goal goal, long percentageCompleted) {
        this.id = goal.getId();
        this.purpose = goal.getPurpose();
        this.targetDate = goal.getTargetDate();
        this.goal = goal.getGoal() / goal.getGoalUnit().getConversion();
        this.goalUnit = goal.getGoalUnit();
        this.archived = goal.isArchived();
        this.percentageCompleted = percentageCompleted;
    }

    public Long getId() {
        return id;
    }

    public String getPurpose() {
        return purpose;
    }

    public Date getTargetDate() {
        return targetDate;
    }

    public long getGoal() {
        return goal;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public boolean isArchived() {
        return archived;
    }

    public long getPercentageCompleted() {
        return percentageCompleted;
    }
}
