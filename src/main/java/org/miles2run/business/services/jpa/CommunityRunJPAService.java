package org.miles2run.business.services.jpa;

import org.miles2run.business.domain.jpa.Activity;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.domain.redis.CommunityRunCounter;
import org.miles2run.business.services.JedisExecutionService;
import org.miles2run.business.services.JedisOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by shekhargulati on 10/07/14.
 */
@Stateless
public class CommunityRunJPAService {

    private Logger logger = LoggerFactory.getLogger(CommunityRunJPAService.class);

    @Inject
    private EntityManager entityManager;

    public Long save(CommunityRun communityRun) {
        entityManager.persist(communityRun);
        return communityRun.getId();
    }

    public List<CommunityRun> findAllActiveRaces() {
        TypedQuery<CommunityRun> query = entityManager.createNamedQuery("CommunityRun.findAllActiveRaces", CommunityRun.class);
        return query.getResultList();
    }

    public CommunityRun findBySlug(@NotNull String slug) {
        TypedQuery<CommunityRun> query = entityManager.createNamedQuery("CommunityRun.findBySlug", CommunityRun.class);
        query.setParameter("slug", slug);
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<CommunityRun> findAllActiveRacesWithNameLike(@NotNull String name) {
        name = name.toLowerCase();
        return entityManager.createNamedQuery("CommunityRun.findAllActivieRunsByNameLike", CommunityRun.class).setParameter("name", "%" + name + "%").getResultList();
    }
}
