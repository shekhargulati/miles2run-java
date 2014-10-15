package org.miles2run.business.services.redis;

import org.joda.time.DateTime;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.domain.mongo.UserProfile;
import org.miles2run.business.services.mongo.ProfileMongoService;
import org.miles2run.business.vo.ActivityDetails;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.ParseException;
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

    public Set<String> getHomeTimelineIds(final String username, final long page, final long count) {
        return jedisExecutionService.execute(new JedisOperation<Set<String>>() {
            @Override
            public Set<String> perform(Jedis jedis) {
                String key = String.format(RedisKeyNames.HOME_S_TIMELINE, username);
                Set<String> activityIds = jedis.zrevrange(key, (page - 1) * count, page * (count - 1));
                return activityIds;
            }
        });
    }

    public Set<String> getProfileTimelineIds(final String username, final long page, final long count) {
        return jedisExecutionService.execute(new JedisOperation<Set<String>>() {
            @Override
            public Set<String> perform(Jedis jedis) {
                String profileTimelineKey = String.format(RedisKeyNames.PROFILE_S_TIMELINE, username);
                Set<String> activityIds = jedis.zrevrange(profileTimelineKey, (page - 1) * count, page * (count - 1));
                return activityIds;
            }
        });
    }

    public List<ActivityDetails> getHomeTimeline(final String username, final long page, final long count) {
        return jedisExecutionService.execute(new JedisOperation<List<ActivityDetails>>() {
            @Override
            public List<ActivityDetails> perform(Jedis jedis) {
                String key = String.format(RedisKeyNames.HOME_S_TIMELINE, username);
                Set<String> activityIds = jedis.zrevrange(key, (page - 1) * count, page * (count - 1));
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

    public void updateTimelineWithFollowingTimeline(final String username, final String userToFollow) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                String profileTimelineKey = String.format("profile:%s:timeline", userToFollow);
                Set<Tuple> activitiesWithScore = jedis.zrevrangeWithScores(profileTimelineKey, 0, HOME_TIMELINE_SIZE - 1);
                if (activitiesWithScore != null && !activitiesWithScore.isEmpty()) {
                    Pipeline pipeline = jedis.pipelined();
                    String key = String.format("home:%s:timeline", username);
                    pipeline.zadd(key, toMap(activitiesWithScore));
                    pipeline.zremrangeByRank(key, 0, -(HOME_TIMELINE_SIZE - 1));
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
                String profileTimelineKey = String.format("profile:%s:timeline", userToUnfollow);
                Set<String> activities = jedis.zrevrange(profileTimelineKey, 0, HOME_TIMELINE_SIZE - 1);
                if (activities != null && !activities.isEmpty()) {
                    String[] members = new ArrayList<>(activities).toArray(new String[0]);
                    String key = String.format("home:%s:timeline", username);
                    jedis.zrem(key, members);
                }
                return null;
            }
        });
    }

    public void updateActivity(final ActivityDetails updatedActivity, final Profile profile, Goal goal) {
        deleteActivityFromTimeline(profile.getUsername(), updatedActivity.getId(), goal);
        postActivityToTimeline(updatedActivity, profile, goal);
    }

    public void postActivityToTimeline(final ActivityDetails activity, final Profile profile, final Goal goal) {
        final String username = profile.getUsername();
        final String activityId = String.valueOf(activity.getId());
        logger.info("Storing activity in redis");
        storeActivity(activity, profile, goal);
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
        logger.info(String.format("Updating %s timelines with new activity..", username));
        jedisExecutionService.execute(new JedisOperation<Object>() {
            @Override
            public <T> T perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                pipeline.zadd(String.format(RedisKeyNames.PROFILE_S_TIMELINE, username), posted, activityId);
                pipeline.zadd(String.format(RedisKeyNames.HOME_S_TIMELINE, username), posted, activityId);
                CommunityRun communityRun = goal.getCommunityRun();
                if (communityRun != null) {
                    pipeline.zadd(String.format(RedisKeyNames.COMMUNITY_RUN_TIMELINE, communityRun.getSlug()), posted, activityId);
                }
                pipeline.zadd(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, username, goal.getId()), posted, activityId);
                pipeline.zadd(String.format(RedisKeyNames.PROFILE_S_TIMELINE_LATEST, username), activity.getPostedAt().getTime(), activityId);
                pipeline.zremrangeByRank(String.format(RedisKeyNames.PROFILE_S_TIMELINE_LATEST, username), 0, -2);
                pipeline.sync();
                return null;
            }
        });
        logger.info("Updated user timeline with new activity..");
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
                    String key = String.format("home:%s:timeline", follower);
                    pipeline.zadd(key, posted, activityId);
                    pipeline.zremrangeByRank(key, 0, -(HOME_TIMELINE_SIZE - 1));
                }
                pipeline.sync();
                return null;
            }
        });
    }

    private String storeActivity(final ActivityDetails activity, final Profile profile, final Goal goal) {

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
                String posted = String.valueOf(activity.getActivityDate().getTime());
                data.put("posted", posted);
                data.put("activityDate", activity.getActivityDate().toString());
                data.put("fullname", profile.getFullname());
                data.put("distanceCovered", String.valueOf(activity.getDistanceCovered()));
                data.put("goalUnit", goal.getGoalUnit().getUnit());
                data.put("goalId", String.valueOf(goal.getId()));
                data.put("profilePic", profile.getProfilePic());
                data.put("status", activity.getStatus() == null ? "" : activity.getStatus());
                data.put("duration", String.valueOf(activity.getDuration()));
                pipeline.hmset("activity:" + id, data);
                pipeline.sync();
                return id;
            }
        });

    }

    public void deleteActivityFromTimeline(final String username, final Long activityId, final Goal goal) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                String key = "activity:" + activityId;
                if (!jedis.hget(key, "username").equals(username)) {
                    return null;
                }
                Pipeline pipeline = jedis.pipelined();
                pipeline.del(key);
                String homeTimelineKey = String.format(RedisKeyNames.HOME_S_TIMELINE, username);
                String profileTimelineKey = String.format(RedisKeyNames.PROFILE_S_TIMELINE, username);
                pipeline.zrem(homeTimelineKey, String.valueOf(activityId));
                pipeline.zrem(profileTimelineKey, String.valueOf(activityId));
                pipeline.zrem(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, username, goal.getId()), String.valueOf(activityId));
                pipeline.zrem(String.format(RedisKeyNames.PROFILE_S_TIMELINE_LATEST, username), String.valueOf(activityId));
                UserProfile userProfile = profileMongoService.findProfile(username);
                logger.info("Deleting activity from all the followers timeline");
                final List<String> followers = userProfile.getFollowers();
                logger.info(String.format("Followers for %s are %s", username, followers));
                for (String follower : followers) {
                    String homeTimelineKeyForFollower = String.format("home:%s:timeline", follower);
                    pipeline.zrem(homeTimelineKeyForFollower, String.valueOf(activityId));
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
                String profileTimelineKey = String.format(RedisKeyNames.PROFILE_S_TIMELINE, username);
                Set<String> activityIds = jedis.zrevrange(profileTimelineKey, (page - 1) * count, page * (count - 1));
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

    public List<Map<String, Object>> distanceAndPaceOverLastNMonths(final Profile profile, final Goal goal, final String interval, final int nMonths) {
        return jedisExecutionService.execute(new JedisOperation<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> perform(Jedis jedis) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -nMonths);
                Date nMonthsBack = calendar.getTime();
                Set<Tuple> activityIdsInNDaysWithScores = jedis.zrangeByScoreWithScores(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, profile.getUsername(), goal.getId()), nMonthsBack.getTime(), today.getTime());
                Map<String, Long> monthDistanceHash = new HashMap<>();
                Map<String, Double> monthPaceHash = new HashMap<>();
                for (Tuple activityIdTuple : activityIdsInNDaysWithScores) {
                    String activityId = activityIdTuple.getElement();
                    double activityTimestamp = activityIdTuple.getScore();
                    List<String> values = jedis.hmget(String.format("activity:%s", activityId), "distanceCovered", "duration");
                    Date activityDate = new Date(Double.valueOf(activityTimestamp).longValue());
                    logger.info(String.format("Activity Date : %s", activityDate));
                    String key = formatDateToYearAndMonth(activityDate);
                    logger.info(String.format("DateToYearAndMonth : %s", key));
                    long distance = Long.valueOf(values.get(0)) / goal.getGoalUnit().getConversion();
                    if (monthDistanceHash.containsKey(key)) {
                        Long value = monthDistanceHash.get(key);
                        monthDistanceHash.put(key, value + distance);
                        Double durationInSeconds = Double.valueOf(Long.valueOf(values.get(1)));
                        double durationInMinutes = durationInSeconds / 60;
                        double pace = durationInMinutes / distance;
                        monthPaceHash.put(key, (value + pace) / 2);
                    } else {
                        monthDistanceHash.put(key, distance);
                        Double durationInSeconds = Double.valueOf(Long.valueOf(values.get(1)));
                        double durationInMinutes = durationInSeconds / 60;
                        double pace = durationInMinutes / distance;
                        monthPaceHash.put(key, pace);
                    }
                }
                List<Map<String, Object>> chartData = new ArrayList<>();
                Set<Map.Entry<String, Long>> entries = monthDistanceHash.entrySet();
                for (Map.Entry<String, Long> entry : entries) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(interval, entry.getKey());
                    data.put("distance", entry.getValue() / goal.getGoalUnit().getConversion());
                    data.put("pace", monthPaceHash.get(entry.getKey()));
                    chartData.add(data);
                }
                return chartData;
            }
        });
    }

    private String formatDateToYearAndMonth(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMMM");
        return dateFormat.format(date);
    }

    public List<Map<String, Object>> distanceAndPaceOverLastNDays(final Profile profile, final Goal goal, final String interval, final int n) {
        return jedisExecutionService.execute(new JedisOperation<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> perform(Jedis jedis) {
                Date today = new Date();
                DateTime dateTime = new DateTime(today);
                dateTime = dateTime.minusDays(n);
                Date nDaysBack = dateTime.toDate();
                Set<Tuple> activityIdsInNDaysWithScores = jedis.zrangeByScoreWithScores(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, profile.getUsername(), goal.getId()), nDaysBack.getTime(), today.getTime());
                List<Response<Map<String, String>>> result = new ArrayList<>();
                List<Map<String, Object>> chartData = new ArrayList<>();
                for (Tuple activityIdTuple : activityIdsInNDaysWithScores) {
                    String activityId = activityIdTuple.getElement();
                    double activityDate = activityIdTuple.getScore();
                    List<String> values = jedis.hmget(String.format("activity:%s", activityId), "distanceCovered", "duration");
                    Map<String, Object> data = new HashMap<>();
                    data.put(interval, activityDate);
                    long distance = Long.valueOf(values.get(0)) / goal.getGoalUnit().getConversion();
                    data.put("distance", distance);
                    Double durationInSeconds = Double.valueOf(Long.valueOf(values.get(1)));
                    double durationInMinutes = durationInSeconds / 60;
                    double pace = durationInMinutes / distance;
                    data.put("pace", pace);
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

    public Long totalItems(final String loggedInUser) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.zcard(String.format(RedisKeyNames.HOME_S_TIMELINE, loggedInUser));
            }
        });
    }

    public Set<String> getGoalTimelineIds(final String loggedInUser, final Goal goal, final int page, final int count) {
        return jedisExecutionService.execute(new JedisOperation<Set<String>>() {
            @Override
            public Set<String> perform(Jedis jedis) {
                Set<String> activityIds = jedis.zrevrange(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, loggedInUser, goal.getId()), (page - 1) * count, page * (count - 1));
                return activityIds;
            }
        });
    }

    public List<ActivityDetails> getGoalTimeline(final String loggedInUser, final Goal goal, final int page, final int count) {
        return jedisExecutionService.execute(new JedisOperation<List<ActivityDetails>>() {
            @Override
            public List<ActivityDetails> perform(Jedis jedis) {
                Set<String> activityIds = jedis.zrevrange(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, loggedInUser, goal.getId()), (page - 1) * count, page * (count - 1));
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

    public Long totalActivitiesForGoal(final String loggedInUser, final Goal goal) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.zcard(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, loggedInUser, goal.getId()));
            }
        });
    }

    public List<Object[]> distanceAndPaceOverNMonths(final Profile profile, final Goal goal, final String interval, final int nMonths) {
        return jedisExecutionService.execute(new JedisOperation<List<Object[]>>() {
            @Override
            public List<Object[]> perform(Jedis jedis) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -nMonths);
                Date nMonthsBack = calendar.getTime();
                Set<Tuple> activityIdsInNDaysWithScores = jedis.zrangeByScoreWithScores(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, profile.getUsername(), goal.getId()), nMonthsBack.getTime(), today.getTime());
                Map<String, Double> monthDistanceHash = new HashMap<>();
                Map<String, Double> monthPaceHash = new HashMap<>();
                for (Tuple activityIdTuple : activityIdsInNDaysWithScores) {
                    String activityId = activityIdTuple.getElement();
                    double activityTimestamp = activityIdTuple.getScore();
                    List<String> values = jedis.hmget(String.format("activity:%s", activityId), "distanceCovered", "duration");
                    Date activityDate = new Date(Double.valueOf(activityTimestamp).longValue());
                    logger.info(String.format("Activity Date : %s", activityDate));
                    String key = formatDateToYearAndMonth(activityDate);
                    logger.info(String.format("DateToYearAndMonth : %s", key));
                    double distance = Double.valueOf(values.get(0)) / goal.getGoalUnit().getConversion();
                    if (monthDistanceHash.containsKey(key)) {
                        Double value = monthDistanceHash.get(key);
                        monthDistanceHash.put(key, value + distance);
                        Double durationInSeconds = Double.valueOf(Long.valueOf(values.get(1)));
                        double durationInMinutes = durationInSeconds / 60;
                        double pace = durationInMinutes / distance;
                        Double currentAvgPace = monthPaceHash.get(key);
                        monthPaceHash.put(key, (currentAvgPace + pace) / 2);
                    } else {
                        monthDistanceHash.put(key, distance);
                        Double durationInSeconds = Double.valueOf(Long.valueOf(values.get(1)));
                        double durationInMinutes = durationInSeconds / 60;
                        double pace = durationInMinutes / distance;
                        monthPaceHash.put(key, pace);
                    }
                }
                List<Object[]> chartData = new ArrayList<>();
                Set<Map.Entry<String, Double>> entries = monthDistanceHash.entrySet();
                for (Map.Entry<String, Double> entry : entries) {
                    chartData.add(new Object[]{entry.getKey(), entry.getValue(), monthPaceHash.get(entry.getKey())});
                }
                Collections.reverse(chartData);
                return chartData;
            }
        });
    }

    public List<Object[]> distanceAndActivityCountOverNMonths(final Profile profile, final Goal goal, final int months) {
        return jedisExecutionService.execute(new JedisOperation<List<Object[]>>() {
            @Override
            public List<Object[]> perform(Jedis jedis) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -months);
                Date nMonthsBack = calendar.getTime();
                Set<Tuple> activityIdsInNDaysWithScores = jedis.zrangeByScoreWithScores(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, profile.getUsername(), goal.getId()), nMonthsBack.getTime(), today.getTime());
                Map<String, Double> monthDistanceHash = new HashMap<>();
                Map<String, Long> monthActivityCountHash = new HashMap<>();
                for (Tuple activityIdTuple : activityIdsInNDaysWithScores) {
                    String activityId = activityIdTuple.getElement();
                    double activityTimestamp = activityIdTuple.getScore();
                    List<String> values = jedis.hmget(String.format("activity:%s", activityId), "distanceCovered", "duration");
                    Date activityDate = new Date(Double.valueOf(activityTimestamp).longValue());
                    logger.info(String.format("Activity Date : %s", activityDate));
                    String key = formatDateToYearAndMonth(activityDate);
                    logger.info(String.format("DateToYearAndMonth : %s", key));
                    double distance = Double.valueOf(values.get(0)) / goal.getGoalUnit().getConversion();
                    if (monthDistanceHash.containsKey(key)) {
                        Double distanceTillNow = monthDistanceHash.get(key);
                        monthDistanceHash.put(key, distanceTillNow + distance);
                        Long activityCountTillNow = monthActivityCountHash.get(key);
                        monthActivityCountHash.put(key, activityCountTillNow + 1L);
                    } else {
                        monthDistanceHash.put(key, distance);
                        monthActivityCountHash.put(key, Long.valueOf(1));
                    }
                }
                List<Object[]> chartData = new ArrayList<>();
                Set<Map.Entry<String, Double>> entries = monthDistanceHash.entrySet();
                for (Map.Entry<String, Double> entry : entries) {
                    chartData.add(new Object[]{entry.getKey(), entry.getValue(), monthActivityCountHash.get(entry.getKey())});
                }
                Collections.sort(chartData, new Comparator<Object[]>() {
                    @Override
                    public int compare(Object[] m1, Object[] m2) {
                        String month1 = (String) m1[0];
                        String month2 = (String) m2[0];
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMMM");
                        try {
                            Date date1 = dateFormat.parse(month1);
                            Date date2 = dateFormat.parse(month2);
                            return date1.compareTo(date2);
                        } catch (ParseException e) {
                            return 0;
                        }
                    }
                });
                return chartData;
            }
        });
    }

}


