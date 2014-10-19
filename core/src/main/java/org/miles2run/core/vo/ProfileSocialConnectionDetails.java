package org.miles2run.core.vo;

import org.miles2run.domain.entities.GoalUnit;

import java.util.ArrayList;
import java.util.List;

public class ProfileSocialConnectionDetails {
    private final List<String> providers = new ArrayList<>();
    private Long id;
    private String username;
    private long goal;
    private GoalUnit goalUnit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getProviders() {
        return providers;
    }

    public long getGoal() {
        return goal;
    }

    public void setGoal(long goal) {
        this.goal = goal;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public void setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
    }
}
