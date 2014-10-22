package org.miles2run.domain.entities;

public enum GoalType {

    DISTANCE_GOAL(Constants.DISTANCE_GOAL), DURATION_GOAL(Constants.DURATION_GOAL), COMMUNITY_RUN_GOAL(Constants.COMMUNITY_RUN);

    private final String type;

    GoalType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static interface Constants {
        public static final String DISTANCE_GOAL = "DISTANCE_GOAL";
        public static final String DURATION_GOAL = "DURATION_GOAL";
        public static final String COMMUNITY_RUN = "COMMUNITY_RUN_GOAL";

    }

}
