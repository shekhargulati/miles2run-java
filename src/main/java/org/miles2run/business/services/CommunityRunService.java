package org.miles2run.business.services;

import org.miles2run.business.domain.jpa.CommunityRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
