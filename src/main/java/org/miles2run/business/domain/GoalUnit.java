package org.miles2run.business.domain;

/**
 * Created by shekhargulati on 07/03/14.
 */
public enum GoalUnit {
    KMS("KMS", 1000), MILES("MILES", 1609);

    private final String unit;
    private final long conversion;


    GoalUnit(String unit, long conversion) {
        this.unit = unit;
        this.conversion = conversion;
    }

    public String getUnit() {
        return unit;
    }

    public static GoalUnit fromStringToGoalUnit(String goalUnit) {
        GoalUnit[] values = GoalUnit.values();
        for (GoalUnit goal : values) {
            if (goal.unit.equals(goalUnit)) {
                return goal;
            }
        }
        return null;
    }

    public long getConversion() {
        return conversion;
    }

    @Override
    public String toString() {
        return this.unit != null ? this.unit.toLowerCase() : null;
    }
}
