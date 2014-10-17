package org.miles2run.jaxrs.vo;

import org.miles2run.business.domain.jpa.Goal;

public abstract class GoalDetailsFactory {

    public static GoalDetails toGoalType(Goal goal, double totalDistanceCoveredForGoal) {
        switch (goal.getGoalType()) {
            case DURATION_GOAL:
                return new DurationGoalDetails(goal);
            case DISTANCE_GOAL:
                return new DistanceGoalDetails(goal, totalDistanceCoveredForGoal);
            case COMMUNITY_RUN_GOAL:
                return new CommunityRunGoalDetails(goal);
            default:
                return null;
        }
    }

}
