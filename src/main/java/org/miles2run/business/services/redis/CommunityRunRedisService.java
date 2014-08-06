package org.miles2run.business.services.redis;

import org.miles2run.business.domain.jpa.Activity;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.domain.redis.CommunityRunStats;
import org.miles2run.business.services.JedisExecutionService;
import org.miles2run.business.services.JedisOperation;
import org.miles2run.business.services.RedisKeyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by shekhargulati on 10/07/14.
 */
@ApplicationScoped
public class CommunityRunRedisService {

    private Logger logger = LoggerFactory.getLogger(CommunityRunRedisService.class);

    @Inject
    JedisExecutionService jedisExecutionService;

    public void addGoalToCommunityRun(final String slug, final Long goalId) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                jedis.sadd(String.format("%s-goals", slug), String.valueOf(goalId));
                return null;
            }
        });
    }

    public void addRunnerToCommunityRun(final String slug, final Profile profile) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                String username = profile.getUsername();
                String country = profile.getCountry();
                String city = profile.getCity();
                Pipeline pipeline = jedis.pipelined();
                pipeline.sadd(String.format("%s-all-runners", slug), username);
                pipeline.sadd(String.format("%s-runners", slug), username);
                pipeline.sadd(String.format("%s-cities", slug), city);
                pipeline.sadd(String.format("%s-countries", slug), country);
                pipeline.sadd(String.format("%s-community_runs", username), slug);
                pipeline.sync();
                return null;
            }
        });
    }

    public boolean isUserAlreadyPartOfRun(final String slug, final String username) {
        return jedisExecutionService.execute(new JedisOperation<Boolean>() {
            @Override
            public Boolean perform(Jedis jedis) {
                return jedis.sismember(String.format("%s-runners", slug), username);
            }
        });
    }

    public void updateCommunityRunStats(final String username, final Goal goal, final Activity activity) {
        final CommunityRun communityRun = goal.getCommunityRun();

        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                String slug = communityRun.getSlug();
                pipeline.incrBy(String.format("%s-activity_count", slug), 1);
                pipeline.incrByFloat(String.format("%s-total_distance_covered", slug), activity.getDistanceCovered());
                pipeline.incrBy(String.format("%s-total_duration", slug), activity.getDuration());
                pipeline.sync();
                return null;
            }
        });
    }

    public void updateCommunityRunDistanceAndDurationStats(final String slug, final double updatedDistanceCovered, final long updatedDuration) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                pipeline.incrByFloat(String.format("%s-total_distance_covered", slug), updatedDistanceCovered);
                pipeline.incrBy(String.format("%s-total_duration", slug), updatedDuration);
                pipeline.sync();
                return null;
            }
        });
    }

    public CommunityRunStats getCurrentStatsForCommunityRun(final String slug) {
        return jedisExecutionService.execute(new JedisOperation<CommunityRunStats>() {
            @Override
            public CommunityRunStats perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                Response<String> totalDistanceCoveredResponse = pipeline.get(String.format("%s-total_distance_covered", slug));
                Response<String> totalDurationResponse = pipeline.get(String.format("%s-total_duration", slug));
                Response<Long> countriesCountResponse = pipeline.scard(String.format("%s-countries", slug));
                Response<Long> citiesCountResponse = pipeline.scard(String.format("%s-cities", slug));
                Response<Long> runnersCountResponse = pipeline.scard(String.format("%s-all-runners", slug));
                pipeline.sync();
                Long totalDistance = totalDistanceCoveredResponse.get() == null ? Long.valueOf(0L) : Long.valueOf(totalDistanceCoveredResponse.get());
                Long totalDuration = totalDurationResponse.get() == null ? Long.valueOf(0L) : Long.valueOf(totalDurationResponse.get());
                return new CommunityRunStats(runnersCountResponse.get(), countriesCountResponse.get(), citiesCountResponse.get(), totalDistance, totalDuration);
            }
        });
    }

    public void addCommunityRunToSet(final String slug) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                jedis.sadd(RedisKeyNames.COMMUNITY_RUNS, slug);
                return null;
            }
        });
    }

    public boolean communityRunExists(final String slug) {
        return jedisExecutionService.execute(new JedisOperation<Boolean>() {
            @Override
            public Boolean perform(Jedis jedis) {
                return jedis.sismember(RedisKeyNames.COMMUNITY_RUNS,slug);
            }
        });

    }

    public void removeRunnerFromCommunityRun(final String slug, final String username) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                pipeline.srem(String.format("%s-runners", slug), username);
                pipeline.srem(String.format("%s-community_runs", username), slug);
                pipeline.sync();
                return null;
            }
        });
    }
}
