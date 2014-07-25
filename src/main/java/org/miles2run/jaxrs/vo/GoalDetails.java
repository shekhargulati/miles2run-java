package org.miles2run.jaxrs.vo;

import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.GoalUnit;

import java.util.Date;

/**
 * Created by shekhargulati on 16/06/14.
 */
public class GoalDetails {

    private final Long id;
    private final String purpose;
    private final Date endDate;
    private final long distance;
    private final GoalUnit goalUnit;
    private final boolean archived;
    private double percentageCompleted;

    public GoalDetails(Goal goal, double percentageCompleted) {
        this.id = goal.getId();
        this.purpose = goal.getPurpose();
        this.endDate = goal.getEndDate();
        this.distance = goal.getDistance() / goal.getGoalUnit().getConversion();
        this.goalUnit = goal.getGoalUnit();
        this.archived = goal.isArchived();
        this.percentageCompleted = percentageCompleted;
    }

    public GoalDetails(Goal goal) {
        this.id = goal.getId();
        this.purpose = goal.getPurpose();
        this.endDate = goal.getEndDate();
        this.distance = goal.getDistance() / goal.getGoalUnit().getConversion();
        this.goalUnit = goal.getGoalUnit();
        this.archived = goal.isArchived();
    }

    public Long getId() {
        return id;
    }

    public String getPurpose() {
        return purpose;
    }

    public Date getEndDate() {
        return endDate;
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
}
