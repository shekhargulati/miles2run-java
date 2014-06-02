package org.miles2run.business.services;

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
}
