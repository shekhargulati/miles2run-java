package org.miles2run.business.services;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.miles2run.business.domain.Activity;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.domain.UserProfile;
import org.miles2run.business.vo.ActivityDetails;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 02/06/14.
 */
@ApplicationScoped
public class TimelineService {

    private static final long HOME_TIMELINE_SIZE = 1000;

    @Inject
    private JedisExecutionService jedisExecutionService;
    @Inject
    private Logger logger;
    @Inject
    private ProfileMongoService profileMongoService;

    public List<ActivityDetails> getHomeTimeline(final String username, final long page, final long count) {
        return jedisExecutionService.execute(new JedisOperation<List<ActivityDetails>>() {
            @Override
            public List<ActivityDetails> perform(Jedis jedis) {
                Set<String> activityIds = jedis.zrevrange("home:timeline:" + username, (page - 1) * count, page * (count - 1));
                Pipeline pipeline = jedis.pipelined();
                List<Response<Map<String, String>>> result = new ArrayList<>();
                for (String activityId : activityIds) {
                    Response<Map<String, String>> response = pipeline.hgetAll("activity:" + activityId);
                    result.add(response);
                }
                pipeline.sync();
                List<ActivityDetails> homeTimeline = new ArrayList<>();
                for (Response<Map<String, String>> response : result) {
                    Map<String, String> hash = response.get();
                    homeTimeline.add(new ActivityDetails(hash));
                }
                return homeTimeline;
            }
        });
    }

    public void postActivityToTimeline(final Activity activity, final Profile profile) {
        final String username = profile.getUsername();
        final String activityId = String.valueOf(activity.getId());
        logger.info("Storing activity in redis");
        storeActivity(activity, profile);
        logger.info("Activity created with id : " + activityId);
        logger.info("Getting posted time from Redis..");
        final Long posted = jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                String postedVal = jedis.hget("activity:" + activityId, "posted");
                if (postedVal == null) {
                    return null;
                }
                return Long.valueOf(postedVal);
            }
        });
        logger.info("Activity posted at.." + posted);
        if (posted == null) {
            return;
        }
        logger.info(String.format("Updating %s profile:timeline with new activity..", username));
        jedisExecutionService.execute(new JedisOperation<Object>() {
            @Override
            public <T> T perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                pipeline.zadd("profile:timeline:" + username, posted, activityId);
                pipeline.zadd("home:timeline:" + username, posted, activityId);
                pipeline.sync();
                return null;
            }
        });
        logger.info("Updated user profile:timeline with new activity..");
        logger.info("Posting new activity to all the followers ...");
        postActivityToFollowersTimeline(username, activityId, posted);
        logger.info("Posted new activity to all the followers ...");
    }

    private void postActivityToFollowersTimeline(final String username, final String activityId, final Long posted) {
        UserProfile userProfile = profileMongoService.findProfile(username);
        final List<String> followers = userProfile.getFollowers();
        logger.info(String.format("Followers for %s are %s", username, followers));
        jedisExecutionService.execute(new JedisOperation<Object>() {
            @Override
            public <T> T perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                for (String follower : followers) {
                    pipeline.zadd("home:timeline:" + follower, posted, activityId);
                    pipeline.zremrangeByRank("home:timeline:" + follower, 0, -(HOME_TIMELINE_SIZE - 1));
                }
                pipeline.sync();
                return null;
            }
        });
    }


    private String storeActivity(final Activity activity, final Profile profile) {

        return jedisExecutionService.execute(new JedisOperation<String>() {
            @Override
            public String perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                String id = String.valueOf(activity.getId());
                logger.info(String.format("Activity id for Redis is %s ", id));
                Map<String, String> data = new HashMap<>();
                data.put("id", id);
                data.put("username", profile.getUsername());
                data.put("userId", String.valueOf(profile.getId()));
                data.put("posted", String.valueOf(activity.getActivityDate().getTime()));
                data.put("fullname", profile.getFullname());
                data.put("distanceCovered", String.valueOf(activity.getDistanceCovered()));
                data.put("goalUnit", activity.getGoalUnit().getUnit());
                data.put("profilePic", profile.getProfilePic());
                data.put("status", activity.getStatus() == null ? "" : activity.getStatus());
                data.put("duration", String.valueOf(activity.getDuration()));
                pipeline.hmset("activity:" + id, data);
                pipeline.sync();
                return id;
            }
        });

    }

    public void updateTimelineWithFollowingTimeline(final String username, final String userToFollow) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                Set<Tuple> activitiesWithScore = jedis.zrevrangeWithScores("profile:timeline:" + userToFollow, 0, HOME_TIMELINE_SIZE - 1);
                if (activitiesWithScore != null && !activitiesWithScore.isEmpty()) {
                    Pipeline pipeline = jedis.pipelined();
                    pipeline.zadd("home:timeline:" + username, toMap(activitiesWithScore));
                    pipeline.zremrangeByRank("home:timeline:" + username, 0, -(HOME_TIMELINE_SIZE - 1));
                    pipeline.sync();
                }
                return null;
            }
        });
    }

    private Map<Double, String> toMap(Set<Tuple> activitiesWithScore) {
        Map<Double, String> map = new HashMap<>();
        for (Tuple tuple : activitiesWithScore) {
            map.put(tuple.getScore(), tuple.getElement());
        }
        return map;
    }

    public void removeFollowingTimeline(final String username, final String userToUnfollow) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                Set<String> activities = jedis.zrevrange("profile:timeline:" + userToUnfollow, 0, HOME_TIMELINE_SIZE - 1);
                if (activities != null && !activities.isEmpty()) {
                    String[] members = new ArrayList<String>(activities).toArray(new String[0]);
                    jedis.zrem("home:timeline:" + username, members);
                }
                return null;
            }
        });
    }

    public void updateActivity(final ActivityDetails updatedActivity, final Profile profile) {
        deleteActivityFromTimeline(profile.getUsername(), updatedActivity.getId());
        Activity activity = new Activity(updatedActivity);
        activity.setDistanceCovered(activity.getDistanceCovered() * activity.getGoalUnit().getConversion());
        postActivityToTimeline(activity, profile);
    }

    public void deleteActivityFromTimeline(final String username, final Long activityId) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                String key = "activity:" + activityId;
                if (!jedis.hget(key, "username").equals(username)) {
                    return null;
                }
                Pipeline pipeline = jedis.pipelined();
                pipeline.del(key);
                pipeline.zrem("home:timeline:" + username, String.valueOf(activityId));
                pipeline.zrem("profile:timeline:" + username, String.valueOf(activityId));
                UserProfile userProfile = profileMongoService.findProfile(username);
                logger.info("Deleting activity from all the followers timeline");
                final List<String> followers = userProfile.getFollowers();
                logger.info(String.format("Followers for %s are %s", username, followers));
                for (String follower : followers) {
                    pipeline.zrem("home:timeline:" + follower, String.valueOf(activityId));
                }
                pipeline.sync();
                return null;
            }
        });
    }

    public List<ActivityDetails> getProfileTimeline(final String username, final int page, final int count) {
        return jedisExecutionService.execute(new JedisOperation<List<ActivityDetails>>() {
            @Override
            public List<ActivityDetails> perform(Jedis jedis) {
                Set<String> activityIds = jedis.zrevrange("profile:timeline:" + username, (page - 1) * count, page * (count - 1));
                Pipeline pipeline = jedis.pipelined();
                List<Response<Map<String, String>>> result = new ArrayList<>();
                for (String activityId : activityIds) {
                    Response<Map<String, String>> response = pipeline.hgetAll("activity:" + activityId);
                    result.add(response);
                }
                pipeline.sync();
                List<ActivityDetails> profileTimeline = new ArrayList<>();
                for (Response<Map<String, String>> response : result) {
                    Map<String, String> hash = response.get();
                    profileTimeline.add(new ActivityDetails(hash));
                }
                return profileTimeline;
            }
        });
    }

    public List<Map<String, Object>> distanceCoveredOverTime(final Profile profile, final String interval, final int n) {
        switch (interval) {
            case "day":
                return getDistanceCoveredInLastNDays(profile, interval, n);
            case "month":
                return getDistanceCoveredInLastNMonths(profile, interval, n);
            default:
                return getDistanceCoveredInLastNDays(profile, interval, n);
        }
    }

    private List<Map<String, Object>> getDistanceCoveredInLastNMonths(final Profile profile, final String interval, final int nMonths) {
        return jedisExecutionService.execute(new JedisOperation<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> perform(Jedis jedis) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -nMonths);
                Date nMonthsBack = calendar.getTime();
                Set<Tuple> activityIdsInNDaysWithScores = jedis.zrangeByScoreWithScores("profile:timeline:" + profile.getUsername(), nMonthsBack.getTime(), today.getTime());
                List<Response<Map<String, String>>> result = new ArrayList<>();
                Map<String, Long> monthDistanceHash = new HashMap<>();
                for (Tuple activityIdTuple : activityIdsInNDaysWithScores) {
                    String activityId = activityIdTuple.getElement();
                    double activityTimestamp = activityIdTuple.getScore();
                    String distanceCovered = jedis.hget(String.format("activity:%s", activityId), "distanceCovered");
                    Date activityDate = new Date(Double.valueOf(activityTimestamp).longValue());
                    String key = formatDateToYearAndMonth(activityDate);
                    if (monthDistanceHash.containsKey(key)) {
                        Long value = monthDistanceHash.get(key);
                        monthDistanceHash.put(key, value + Long.valueOf(distanceCovered));
                    } else {
                        monthDistanceHash.put(key, Long.valueOf(distanceCovered));
                    }

                }
                List<Map<String, Object>> chartData = new ArrayList<>();
                Set<Map.Entry<String, Long>> entries = monthDistanceHash.entrySet();
                for (Map.Entry<String, Long> entry : entries) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(interval, entry.getKey());
                    data.put("distance", entry.getValue() / profile.getGoalUnit().getConversion());
                    chartData.add(data);
                }
                return chartData;
            }
        });
    }

    private List<Map<String, Object>> getDistanceCoveredInLastNDays(final Profile profile, final String interval, final int n) {
        return jedisExecutionService.execute(new JedisOperation<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> perform(Jedis jedis) {
                Date today = new Date();
                Date nDaysBack = new Date(today.getTime() - n * 24 * 3600 * 1000);
                Set<Tuple> activityIdsInNDaysWithScores = jedis.zrangeByScoreWithScores("profile:timeline:" + profile.getUsername(), nDaysBack.getTime(), today.getTime());
                List<Response<Map<String, String>>> result = new ArrayList<>();
                List<Map<String, Object>> chartData = new ArrayList<>();
                for (Tuple activityIdTuple : activityIdsInNDaysWithScores) {
                    String activityId = activityIdTuple.getElement();
                    double activityDate = activityIdTuple.getScore();
                    String distanceCovered = jedis.hget(String.format("activity:%s", activityId), "distanceCovered");
                    Map<String, Object> data = new HashMap<>();
                    data.put(interval, activityDate);
                    data.put("distance", Long.valueOf(distanceCovered) / profile.getGoalUnit().getConversion());
                    chartData.add(data);
                }
                return chartData;
            }
        });
    }

    private String formatDate(Date activityDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(activityDate);
    }

    private String formatDateToYearAndMonth(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        return dateFormat.format(date);
    }

    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -6);
        Date nMonthsBack = calendar.getTime();
        System.out.println(nMonthsBack.toString());
    }

    public List<Map<String, Object>> paceOverTime(Profile profile, String interval, int n) {
        switch (interval) {
            case "day":
                return getPaceInLastNDays(profile, interval, n);
            case "month":
                return getPaceInLastNMonths(profile, interval, n);
            default:
                return getDistanceCoveredInLastNDays(profile, interval, n);
        }
    }

    private List<Map<String, Object>> getPaceInLastNMonths(final Profile profile, final String interval, final int nMonths) {
        return jedisExecutionService.execute(new JedisOperation<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> perform(Jedis jedis) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -nMonths);
                Date nMonthsBack = calendar.getTime();
                Set<Tuple> activityIdsInNDaysWithScores = jedis.zrangeByScoreWithScores("profile:timeline:" + profile.getUsername(), nMonthsBack.getTime(), today.getTime());
                List<Response<Map<String, String>>> result = new ArrayList<>();
                Map<String, Double> monthPaceHash = new HashMap<>();
                for (Tuple activityIdTuple : activityIdsInNDaysWithScores) {
                    String activityId = activityIdTuple.getElement();
                    double activityTimestamp = activityIdTuple.getScore();
                    List<String> values = jedis.hmget(String.format("activity:%s", activityId), "distanceCovered", "duration");
                    Date activityDate = new Date(Double.valueOf(activityTimestamp).longValue());
                    String key = formatDateToYearAndMonth(activityDate);
                    if (monthPaceHash.containsKey(key)) {
                        Double value = monthPaceHash.get(key);
                        Double durationInSeconds = Double.valueOf(Long.valueOf(values.get(1)));
                        double durationInMinutes = durationInSeconds / 60;
                        long distance = Long.valueOf(values.get(0)) / profile.getGoalUnit().getConversion();
                        double pace = durationInMinutes / distance;
                        monthPaceHash.put(key, (value + pace) / 2);
                    } else {
                        Double durationInSeconds = Double.valueOf(Long.valueOf(values.get(1)));
                        double durationInMinutes = durationInSeconds / 60;
                        long distance = Long.valueOf(values.get(0)) / profile.getGoalUnit().getConversion();
                        double pace = durationInMinutes / distance;
                        monthPaceHash.put(key, pace);
                    }

                }
                List<Map<String, Object>> chartData = new ArrayList<>();
                Set<Map.Entry<String, Double>> entries = monthPaceHash.entrySet();
                for (Map.Entry<String, Double> entry : entries) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(interval, entry.getKey());
                    data.put("pace", entry.getValue());
                    chartData.add(data);
                }
                return chartData;
            }
        });
    }

    private List<Map<String, Object>> getPaceInLastNDays(final Profile profile, final String interval, final int n) {
        return jedisExecutionService.execute(new JedisOperation<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> perform(Jedis jedis) {
                Date today = new Date();
                Date nDaysBack = new Date(today.getTime() - n * 24 * 3600 * 1000);
                Set<Tuple> activityIdsInNDaysWithScores = jedis.zrangeByScoreWithScores("profile:timeline:" + profile.getUsername(), nDaysBack.getTime(), today.getTime());
                List<Response<Map<String, String>>> result = new ArrayList<>();
                List<Map<String, Object>> chartData = new ArrayList<>();
                for (Tuple activityIdTuple : activityIdsInNDaysWithScores) {
                    String activityId = activityIdTuple.getElement();
                    double activityDate = activityIdTuple.getScore();
                    List<String> values = jedis.hmget(String.format("activity:%s", activityId), "distanceCovered", "duration");
                    Map<String, Object> data = new HashMap<>();
                    data.put(interval, activityDate);
                    Double durationInSeconds = Double.valueOf(Long.valueOf(values.get(1)));
                    double durationInMinutes = durationInSeconds / 60;
                    long distance = Long.valueOf(values.get(0)) / profile.getGoalUnit().getConversion();
                    double pace = durationInMinutes / distance;
                    data.put("pace", pace);
                    chartData.add(data);
                }
                return chartData;
            }
        });
    }

    public Long totalItems(final String loggedInUser) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.zcard(String.format("home:timeline:%s", loggedInUser));
            }
        });
    }
}


