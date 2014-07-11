package org.miles2run.business.services;

import org.miles2run.business.domain.jpa.CommunityRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

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

    public void addRunnerToCommunityRun(final String slug, final String username) {
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                pipeline.sadd(String.format("%s-runners", slug), username);
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
}
