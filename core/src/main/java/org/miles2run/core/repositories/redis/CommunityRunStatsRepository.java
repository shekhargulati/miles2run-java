/*
package org.miles2run.core.repositories.redis;

import org.miles2run.domain.entities.Activity;
import org.miles2run.domain.entities.CommunityRun;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;
import org.miles2run.domain.kv_aggregates.CommunityRunStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CommunityRunStatsRepository {

    private final Logger logger = LoggerFactory.getLogger(CommunityRunStatsRepository.class);

    @Inject
    JedisExecution jedisExecution;

    public void addGoalToCommunityRun(final String slug, final Long goalId) {
        jedisExecution.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                jedis.sadd(String.format(RedisKeyNames.CR_GOALS_SET, slug), String.valueOf(goalId));
                return null;
            }
        });
    }

    public void addRunnerToCommunityRun(final String slug, final Profile profile) {
        jedisExecution.execute(new JedisOperation<Void>() {
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
        return jedisExecution.execute(new JedisOperation<Boolean>() {
            @Override
            public Boolean perform(Jedis jedis) {
                return jedis.sismember(String.format("%s-runners", slug), username);
            }
        });
    }

    public void updateCommunityRunStats(final String username, final Goal goal, final Activity activity) {
        final CommunityRun communityRun = goal.getCommunityRun();

        jedisExecution.execute(new JedisOperation<Void>() {
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
        jedisExecution.execute(new JedisOperation<Void>() {
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
        return jedisExecution.execute(new JedisOperation<CommunityRunStats>() {
            @Override
            public CommunityRunStats perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                Response<String> totalDistanceCoveredResponse = pipeline.get(String.format("%s-total_distance_covered", slug));
                Response<String> totalDurationResponse = pipeline.get(String.format("%s-total_duration", slug));
                Response<Long> countriesCountResponse = pipeline.scard(String.format("%s-countries", slug));
                Response<Long> citiesCountResponse = pipeline.scard(String.format("%s-cities", slug));
                Response<Long> runnersCountResponse = pipeline.scard(String.format("%s-all-runners", slug));
                pipeline.sync();
                Double totalDistance = totalDistanceCoveredResponse.get() == null ? Double.valueOf(0) : Double.valueOf(totalDistanceCoveredResponse.get());
                Long totalDuration = totalDurationResponse.get() == null ? Long.valueOf(0L) : Long.valueOf(totalDurationResponse.get());
                return new CommunityRunStats(runnersCountResponse.get(), countriesCountResponse.get(), citiesCountResponse.get(), totalDistance, totalDuration);
            }
        });
    }

    public void addCommunityRunToSet(final String slug) {
        jedisExecution.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                jedis.sadd(RedisKeyNames.COMMUNITY_RUNS, slug);
                return null;
            }
        });
    }

    public boolean communityRunExists(final String slug) {
        return jedisExecution.execute(new JedisOperation<Boolean>() {
            @Override
            public Boolean perform(Jedis jedis) {
                return jedis.sismember(RedisKeyNames.COMMUNITY_RUNS, slug);
            }
        });

    }

    public void removeRunnerFromCommunityRun(final String slug, final String username) {
        jedisExecution.execute(new JedisOperation<Void>() {
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
*/
