package org.miles2run.core.repositories.redis;

public interface RedisKeyNames {

    public static final String PROFILE_S_TIMELINE = "profile:%s:timeline";
    public static final String HOME_S_TIMELINE = "home:%s:timeline";
    public static final String PROFILE_S_GOAL_S_TIMELINE = "profile:%s:goal:%s:timeline";
    public static final String PROFILE_S_TIMELINE_LATEST = "profile:%s:timeline:latest";
    public static final String ACTIVITY_S = "activity:%s";
    public static final String COMMUNITY_RUN_TIMELINE = "community_run:%s:timeline";
    public static final String COMMUNITY_RUNS = "community_runs";
    public static final String CR_GOALS_SET = "%s-goals";
    public static final String COUNTRY_SET_KEY = "countries";
    public static final String RUNNER_COUNTER = "runners";
    public static final String DISTANCE_COUNTER = "distance";
    public static final String CITY_SET_KEY = "cities";
    public static final String SECONDS_COUNTER = "hours";
}
