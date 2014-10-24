package org.miles2run.representations;

import org.miles2run.domain.entities.DistanceGoal;
import org.miles2run.domain.entities.Duration;
import org.miles2run.domain.entities.GoalType;
import org.miles2run.domain.entities.GoalUnit;

import java.util.Date;

public class DistanceGoalRepresentation implements GoalRepresentation {

    private final Long id;
    private final String purpose;
    private final long distance;
    private final GoalUnit goalUnit;
    private final boolean archived;
    private final double percentageCompleted;
    private final GoalType goalType;
    private Date endDate;

    public DistanceGoalRepresentation(DistanceGoal goal, double distanceCovered) {
        this.id = goal.getId();
        this.purpose = goal.getPurpose();
        this.distance = goal.getDistance() / goal.getGoalUnit().getConversion();
        this.goalUnit = goal.getGoalUnit();
        this.archived = goal.isArchived();
        Duration duration = goal.getDuration();
        if (duration != null) {
            this.endDate = duration.getEndDate();
        }
        this.percentageCompleted = percentageGoalCompleted(goal.getDistance(), distanceCovered);
        this.goalType = GoalType.DISTANCE_GOAL;
    }

    private static double percentageGoalCompleted(long goal, double distanceCovered) {
        double percentageCompleted = (distanceCovered * 100 / goal);
        return percentageCompleted > 100 ? 100 : percentageCompleted;
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
