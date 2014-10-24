package org.miles2run.rest.representations;

import org.miles2run.domain.entities.CommunityRunGoal;
import org.miles2run.domain.entities.DistanceGoal;
import org.miles2run.domain.entities.DurationGoal;
import org.miles2run.domain.entities.Goal;

public class GoalRepresentationFactory {
    public static GoalRepresentation toGoalType(Goal goal, double distanceCovered) {
        if (goal instanceof DistanceGoal) {
            return new DistanceGoalRepresentation((DistanceGoal) goal, distanceCovered);
        } else if (goal instanceof DurationGoal) {
            return new DurationGoalRepresentation((DurationGoal) goal);
        }
        return new CommunityRunGoalRepresentation((CommunityRunGoal) goal);
    }
}
