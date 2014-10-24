package org.miles2run.rest.representations;

import org.miles2run.domain.entities.Activity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TimelineRepresentation {

    private final List<ActivityRepresentation> activities;
    private final long activityCount;

    protected TimelineRepresentation(long activityCount, List<ActivityRepresentation> activities) {
        this.activities = activities;
        this.activityCount = activityCount;
    }

    public static TimelineRepresentation empty() {
        return new TimelineRepresentation(0L, Collections.emptyList());
    }

    public static TimelineRepresentation with(Long activityCount, List<Activity> activities) {
        List<ActivityRepresentation> activityRepresentations = activities.stream().map(ActivityRepresentation::from).collect(Collectors.toList());
        return new TimelineRepresentation(activityCount, activityRepresentations);
    }

    public List<ActivityRepresentation> getActivities() {
        return Collections.unmodifiableList(activities);
    }

    public long getActivityCount() {
        return activityCount;
    }
}
