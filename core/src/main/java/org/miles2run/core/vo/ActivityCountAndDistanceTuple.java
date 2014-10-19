package org.miles2run.core.vo;

public class ActivityCountAndDistanceTuple {
    private final Long activityCount;
    private final Double distanceCovered;

    public ActivityCountAndDistanceTuple(Long activityCount, Double distanceCovered) {
        this.activityCount = activityCount;
        this.distanceCovered = distanceCovered;
    }

    public Long getActivityCount() {
        return activityCount == null ? 0L : activityCount;
    }

    public Double getDistanceCovered() {
        return distanceCovered == null ? Double.valueOf(0) : distanceCovered;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActivityCountAndDistanceTuple{");
        sb.append("activityCount=").append(activityCount);
        sb.append(", distanceCovered=").append(distanceCovered);
        sb.append('}');
        return sb.toString();
    }
}
