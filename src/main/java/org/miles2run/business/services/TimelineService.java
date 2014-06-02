package org.miles2run.business.services;

import org.miles2run.business.domain.UserProfile;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 02/06/14.
 */
@ApplicationScoped
public class TimelineService {

    @Inject
    private JedisExecutionService jedisExecutionService;
    @Inject
    private Logger logger;
    @Inject
    private ProfileMongoService profileMongoService;

    public String postActivityToTimeline(final long userId, final String message, final String username) {
        logger.info("Storing activity in redis");
        final String activityId = storeActivity(userId, message, username);
        logger.info("Activity created with id : " + activityId);
        if (activityId == null) {
            return null;
        }
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
            return null;
        }
        logger.info("Updating user profile:timeline with new activity..");
        jedisExecutionService.execute(new JedisOperation<Object>() {
            @Override
            public <T> T perform(Jedis jedis) {
                jedis.zadd("profile:timeline:" + username, posted, activityId);
                return null;
            }
        });
        logger.info("Updated user profile:timeline with new activity..");
        logger.info("Posting new activity to all the followers ...");
        postActivityToFollowersTimeline(username, activityId, posted);
        logger.info("Posted new activity to all the followers ...");
        return activityId;
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
//                    pipeline.zremrangeByRank("home:timeline:" + follower, 0, 100);
                }
                pipeline.sync();
                return null;
            }
        });
    }


    private String storeActivity(final long userId, final String message, final String username) {

        return jedisExecutionService.execute(new JedisOperation<String>() {
            @Override
            public String perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                pipeline.incr("activity:id:");
                Response<String> activityIdResponse = pipeline.get("activity:id:");
                pipeline.sync();
                if (activityIdResponse == null) {
                    return null;
                }
                String id = activityIdResponse.get();
                logger.info(String.format("Activity id for Redis is %s ", id));
                Map<String, String> data = new HashMap<>();
                data.put("id", id);
                data.put("message", message);
                data.put("username", username);
                data.put("userId", String.valueOf(userId));
                data.put("posted", String.valueOf(new Date().getTime()));
                pipeline.hmset("activity:" + id, data);
                pipeline.sync();
                return id;
            }
        });

    }
}
