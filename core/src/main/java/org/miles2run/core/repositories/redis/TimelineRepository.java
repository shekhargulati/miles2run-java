package org.miles2run.core.repositories.redis;

import org.miles2run.core.repositories.mongo.UserProfileRepository;
import org.miles2run.domain.documents.UserProfile;
import org.miles2run.domain.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@ApplicationScoped
public class TimelineRepository {

    private static final long TIMELINE_SIZE = 1000;
    private final Logger logger = LoggerFactory.getLogger(TimelineRepository.class);

    @Inject
    JedisExecution jedisExecution;
    @Inject
    private UserProfileRepository userProfileRepository;

    /**
     * Get User's home timeline activity Ids.
     *
     * @param username user whose home timeline activity ids need to be retrieved.
     * @param page
     * @param count
     * @return a set of activity ids or empty set if no activity exists.
     */
    public Set<String> getHomeTimelineIds(final String username, final long page, final long count) {

        return jedisExecution.execute(new JedisOperation<Set<String>>() {
            @Override
            public Set<String> perform(Jedis jedis) {
                String homeTimelineKey = String.format(RedisKeyNames.HOME_S_TIMELINE, username);
                return jedis.zrevrange(homeTimelineKey, (page - 1) * count, page * (count - 1));
            }
        });
    }

    /**
     * Get User's Profile timeline activity Ids.
     *
     * @param username user whose profile timeline activity ids need to be retrieved.
     * @param page
     * @param count
     * @return a set of activity ids or empty set if no activity exists.
     */
    public Set<String> getUserProfileTimelineIds(final String username, final long page, final long count) {
        return jedisExecution.execute(new JedisOperation<Set<String>>() {
            @Override
            public Set<String> perform(Jedis jedis) {
                String profileTimelineKey = String.format(RedisKeyNames.PROFILE_S_TIMELINE, username);
                return jedis.zrevrange(profileTimelineKey, (page - 1) * count, page * (count - 1));
            }
        });
    }

    /**
     * Update user home timeline with activities from the new follower.
     *
     * @param username     user whose home timeline needs to be updated.
     * @param userToFollow a new follower whose activities need to be added
     */
    public void updateTimelineWithFollowingTimeline(final String username, final String userToFollow) {
        jedisExecution.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                String followerTimelineKey = String.format(RedisKeyNames.PROFILE_S_TIMELINE, userToFollow);
                Set<Tuple> followerActivitiesWithTimestamp = jedis.zrevrangeWithScores(followerTimelineKey, 0, TIMELINE_SIZE - 1);
                if (!followerActivitiesWithTimestamp.isEmpty()) {
                    Pipeline pipeline = jedis.pipelined();
                    String userHomeTimeline = String.format(RedisKeyNames.HOME_S_TIMELINE, username);
                    pipeline.zadd(userHomeTimeline, followerActivitiesWithTimestamp.stream().collect(toMap(Tuple::getScore, Tuple::getElement)));
                    pipeline.zremrangeByRank(userHomeTimeline, 0, -(TIMELINE_SIZE - 1));
                    pipeline.sync();
                }
                return null;
            }
        });
    }

    /**
     * Remove unfollower activities from the user's home timeline.
     *
     * @param username       User who unfollowed a user
     * @param userToUnfollow unfollowed user
     * @return 1 if activities were removed from the user's home timeline else it returns 0.
     */
    public Long removeFollowingTimeline(final String username, final String userToUnfollow) {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                String profileTimeline = String.format(RedisKeyNames.PROFILE_S_TIMELINE, userToUnfollow);
                Set<String> unfollowerActivities = jedis.zrevrange(profileTimeline, 0, TIMELINE_SIZE - 1);
                if (unfollowerActivities.isEmpty()) {
                    return Long.valueOf(0);
                }
                String homeTimeline = String.format(RedisKeyNames.HOME_S_TIMELINE, username);
                return jedis.zrem(homeTimeline, unfollowerActivities.stream().toArray(size -> new String[size]));
            }
        });
    }

    public void updateActivity(final Activity updatedActivity, final Profile profile, Goal goal) {
        deleteActivityFromTimeline(profile.getUsername(), updatedActivity.getId(), goal);
        postActivityToTimeline(updatedActivity, profile, goal);
    }

    public void deleteActivityFromTimeline(final String username, final Long activityId, final Goal goal) {
        jedisExecution.execute(new JedisOperation<Void>() {
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
                UserProfile userProfile = userProfileRepository.find(username);
                logger.info("Deleting activity from all the followers timeline");
                final List<String> followers = userProfile.getFollowers();
                logger.info("Followers for {} are {}", username, followers);
                followers.forEach(follower -> {
                    String homeTimelineKeyForFollower = String.format(RedisKeyNames.HOME_S_TIMELINE, follower);
                    pipeline.zrem(homeTimelineKeyForFollower, String.valueOf(activityId));
                });
                pipeline.sync();
                return null;
            }
        });
    }

    public void postActivityToTimeline(final Activity activity, final Profile profile, final Goal goal) {
        final String username = profile.getUsername();
        final String activityId = String.valueOf(activity.getId());
        logger.info("Storing activity in redis");
        storeActivity(activity, profile, goal);
        logger.info("Activity created with id : " + activityId);
        logger.info("Getting posted time from Redis..");
        final Long posted = jedisExecution.execute(new JedisOperation<Long>() {
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
        jedisExecution.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                pipeline.zadd(String.format(RedisKeyNames.PROFILE_S_TIMELINE, username), posted, activityId);
                pipeline.zadd(String.format(RedisKeyNames.HOME_S_TIMELINE, username), posted, activityId);
                if (goal instanceof CommunityRunGoal) {
                    CommunityRun communityRun = ((CommunityRunGoal) goal).getCommunityRun();
                    pipeline.zadd(String.format(RedisKeyNames.COMMUNITY_RUN_TIMELINE, communityRun.getSlug()), posted, activityId);
                }
                pipeline.zadd(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, username, goal.getId()), posted, activityId);
                pipeline.zadd(String.format(RedisKeyNames.PROFILE_S_TIMELINE_LATEST, username), activity.getActivityDate().getTime(), activityId);
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

    private String storeActivity(final Activity activity, final Profile profile, final Goal goal) {

        return jedisExecution.execute(new JedisOperation<String>() {
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

    private void postActivityToFollowersTimeline(final String username, final String activityId, final Long posted) {
        UserProfile userProfile = userProfileRepository.find(username);
        final List<String> followers = userProfile.getFollowers();
        logger.info(String.format("Followers for %s are %s", username, followers));
        jedisExecution.execute(new JedisOperation<Object>() {
            @Override
            public <T> T perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                for (String follower : followers) {
                    String homeTimelineKey = String.format(RedisKeyNames.HOME_S_TIMELINE, follower);
                    pipeline.zadd(homeTimelineKey, posted, activityId);
                    pipeline.zremrangeByRank(homeTimelineKey, 0, -(TIMELINE_SIZE - 1));
                }
                pipeline.sync();
                return null;
            }
        });
    }

    /**
     * Count of user timeline activities. If user has not posted any activity then 0 is returned.
     *
     * @param username
     * @return user timeline activity count
     */
    public Long userTimelineActivityCount(final String username) {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.zcard(String.format(RedisKeyNames.PROFILE_S_TIMELINE, username));
            }
        });
    }

    /**
     * Count of home timeline activities. If user and the users user is following have not posted any activity then 0 is returned.
     *
     * @param username
     * @return home timeline activity count
     */
    public Long homeTimelineActivityCount(final String username) {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.zcard(String.format(RedisKeyNames.HOME_S_TIMELINE, username));
            }
        });
    }

    /**
     * Returns a set of activity ids posted by user for a specific goal. The activity ids are sorted by activity date in descending order.
     * If no activity posted for a goal, then an empty Set is returned.
     *
     * @param username
     * @param goalId
     * @param page
     * @param count
     * @return Set with activity ids
     */
    public Set<String> getGoalTimelineIds(final String username, final Long goalId, final int page, final int count) {
        return jedisExecution.execute(new JedisOperation<Set<String>>() {
            @Override
            public Set<String> perform(Jedis jedis) {
                final String goalTimelineKey = String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, username, goalId);
                return jedis.zrevrange(goalTimelineKey, (page - 1) * count, page * (count - 1));
            }
        });
    }

    /**
     * Count of goal timeline activities. If no activity posted to a goal then 0 is returned.
     *
     * @param username
     * @param goalId
     * @return goal timeline activity count
     */
    public Long goalTimelineActivityCount(final String username, final Long goalId) {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                String goalTimelineKey = String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, username, goalId);
                return jedis.zcard(goalTimelineKey);
            }
        });
    }


}