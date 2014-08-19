package org.miles2run.jaxrs.vo;

import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.GoalType;
import org.miles2run.business.domain.jpa.GoalUnit;

import java.util.Date;

/**
 * Created by shekhargulati on 09/07/14.
 */
public class DistanceGoalDetails {

    private final Long id;
    private final String purpose;
    private final long distance;
    private final GoalUnit goalUnit;
    private final boolean archived;
    private final double percentageCompleted;
    private final Date endDate;
    private final GoalType goalType;

    public DistanceGoalDetails(Goal goal, double percentageCompleted) {
        this.id = goal.getId();
        this.purpose = goal.getPurpose();
        this.distance = goal.getDistance() / goal.getGoalUnit().getConversion();
        this.goalUnit = goal.getGoalUnit();
        this.archived = goal.isArchived();
        this.endDate = goal.getEndDate();
        this.percentageCompleted = percentageCompleted;
        this.goalType = goal.getGoalType();

    }

    public Long getId() {
        return id;
    }

    public String getPurpose() {
        return purpose;
    }

    public long getDistance() {
        return distance;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public boolean isArchived() {
        return archived;
    }

    public double getPercentageCompleted() {
        return percentageCompleted;
    }

    public Date getEndDate() {
        return endDate;
    }

    public GoalType getGoalType() {
        return goalType;
    }
}


