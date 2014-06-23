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
    private final long distance;
    private final GoalUnit goalUnit;
    private final boolean archived;
    private long percentageCompleted;

    public GoalDetails(Goal distance, long percentageCompleted) {
        this.id = distance.getId();
        this.purpose = distance.getPurpose();
        this.targetDate = distance.getTargetDate();
        this.distance = distance.getDistance() / distance.getGoalUnit().getConversion();
        this.goalUnit = distance.getGoalUnit();
        this.archived = distance.isArchived();
        this.percentageCompleted = percentageCompleted;
    }

    public GoalDetails(Goal distance) {
        this.id = distance.getId();
        this.purpose = distance.getPurpose();
        this.targetDate = distance.getTargetDate();
        this.distance = distance.getDistance() / distance.getGoalUnit().getConversion();
        this.goalUnit = distance.getGoalUnit();
        this.archived = distance.isArchived();
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

    public long getDistance() {
        return distance;
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
