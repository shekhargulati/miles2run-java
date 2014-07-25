package org.miles2run.business.services;

import org.miles2run.business.domain.jpa.Activity;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.domain.redis.CommunityRunCounter;
import org.miles2run.business.vo.ProfileDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by shekhargulati on 10/07/14.
 */
@Stateless
public class CommunityRunService {

    private Logger logger = LoggerFactory.getLogger(CommunityRunService.class);

    @Inject
    private EntityManager entityManager;

    @Inject
    JedisExecutionService jedisExecutionService;

    public Long save(CommunityRun communityRun) {
        entityManager.persist(communityRun);
        return communityRun.getId();
    }

    public List<CommunityRun> findAllActiveRaces() {
        TypedQuery<CommunityRun> query = entityManager.createNamedQuery("CommunityRun.findAllActiveRaces", CommunityRun.class);
        return query.getResultList();
    }

    public CommunityRun findBySlug(String slug) {
        TypedQuery<CommunityRun> query = entityManager.createNamedQuery("CommunityRun.findBySlug", CommunityRun.class);
        query.setParameter("slug", slug);
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

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

    public CommunityRunCounter currentStats(final CommunityRun communityRun) {
        return jedisExecutionService.execute(new JedisOperation<CommunityRunCounter>() {
            @Override
            public CommunityRunCounter perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                String slug = communityRun.getSlug();
                Response<String> totalDistanceCoveredResponse = pipeline.get(String.format("%s-total_distance_covered", slug));
                Response<String> totalDurationResponse = pipeline.get(String.format("%s-total_duration", slug));
                Response<Long> countriesCountResponse = pipeline.scard(String.format("%s-countries", slug));
                Response<Long> citiesCountResponse = pipeline.scard(String.format("%s-cities", slug));
                Response<Long> runnersCountResponse = pipeline.scard(String.format("%s-runners", slug));
                pipeline.sync();
                Long totalDistance = totalDistanceCoveredResponse.get() == null ? Long.valueOf(0L) : Long.valueOf(totalDistanceCoveredResponse.get());
                Long totalDuration = totalDurationResponse.get() == null ? Long.valueOf(0L) : Long.valueOf(totalDurationResponse.get());
                return new CommunityRunCounter(runnersCountResponse.get(), countriesCountResponse.get(), citiesCountResponse.get(), totalDistance, totalDuration);
            }
        });
    }

    public List<CommunityRun> findAllActiveRacesWithNameLike(String name) {
        return entityManager.createNamedQuery("CommunityRun.findAllActivieRunsByNameLike", CommunityRun.class).setParameter("name", "%" + name + "%").getResultList();
    }
}
