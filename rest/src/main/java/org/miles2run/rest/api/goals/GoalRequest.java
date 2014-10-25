package org.miles2run.rest.api.goals;

import org.miles2run.domain.entities.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class GoalRequest {

    @NotNull
    private String purpose;

    private Date startDate;

    private Date endDate;

    private long distance = 0;

    @Enumerated(EnumType.STRING)
    private GoalUnit goalUnit = GoalUnit.MI;

    @Enumerated(EnumType.STRING)
    @NotNull
    private GoalType goalType;

    private boolean archived = false;

    private int days;

    public Goal toGoal(Profile profile) {
        switch (goalType) {
            case DURATION_GOAL:
                return new DurationGoalBuilder().setArchived(archived).setDays(days).setPurpose(purpose).setDuration(new Duration(startDate, endDate)).setGoalUnit(goalUnit).setProfile(profile).createDurationGoal();
            case DISTANCE_GOAL:
                return new DistanceGoalBuilder().setArchived(archived).setProfile(profile).setDuration(new Duration(new Date(), endDate)).setDistance(distance).setArchived(archived).setPurpose(purpose).setGoalUnit(goalUnit).createDistanceGoal();
        }
        return null;
    }

    public String getPurpose() {
        return purpose;
    }

    public Date getStartDate() {
        return startDate;
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

    public GoalType getGoalType() {
        return goalType;
    }

    public boolean isArchived() {
        return archived;
    }
}
