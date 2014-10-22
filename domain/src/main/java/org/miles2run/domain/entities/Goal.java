package org.miles2run.domain.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "goal_type")
@Table(name = "goal")
public abstract class Goal extends BaseEntity {

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private final Set<Activity> activities = new HashSet<>();

    @NotNull
    private String purpose;

    @NotNull
    @Embedded
    private Duration duration;

    @Enumerated(EnumType.STRING)
    private GoalUnit goalUnit = GoalUnit.MI;

    private boolean archived = false;

    @ManyToOne
    private Profile profile;

    protected Goal() {
    }

    protected Goal(String purpose, Duration duration, GoalUnit goalUnit, boolean archived, Profile profile) {
        this.purpose = purpose;
        this.duration = duration;
        this.goalUnit = goalUnit;
        this.archived = archived;
        this.profile = profile;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public void setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Set<Activity> getActivities() {
        return Collections.unmodifiableSet(activities);
    }

    public Goal addActivity(Activity activity) {
        activities.add(activity);
        return this;
    }

    public Goal removeActivity(Activity activity) {
        if (activities.remove(activity)) {
            activity.setGoal(null);
        }
        return this;
    }
}
