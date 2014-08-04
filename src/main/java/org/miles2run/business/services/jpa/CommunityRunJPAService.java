package org.miles2run.business.services.jpa;

import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.vo.ProfileGroupDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.Collections;
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

    public List<CommunityRun> findAllActiveCommunityRuns() {
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

    public CommunityRun find(@NotNull String slug) {
        TypedQuery<CommunityRun> query = entityManager.createNamedQuery("CommunityRun.findBySlugWithProfiles", CommunityRun.class);
        query.setParameter("slug", slug);
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<CommunityRun> findAllActiveCommunityRunsWithNameLike(@NotNull String name) {
        name = name.toLowerCase();
        return entityManager.createNamedQuery("CommunityRun.findAllActiviRunsByNameLike", CommunityRun.class).setParameter("name", "%" + name + "%").getResultList();
    }

    public List<ProfileGroupDetails> groupAllUserInACommunityRunByCity(@NotNull String slug) {
        CommunityRun communityRun = this.find(slug);
        if(communityRun == null){
            return Collections.emptyList();
        }
        List<Profile> runners = communityRun.getProfiles();
        if(runners.isEmpty()){
            return Collections.emptyList();
        }
        // TODO: Possible Performance Bottleneck
        List<ProfileGroupDetails> profileGroups = entityManager.createQuery("SELECT new org.miles2run.business.vo.ProfileGroupDetails(COUNT(p),p.city,p.country) FROM Profile p where p in :profiles GROUP BY p.city", ProfileGroupDetails.class).setParameter("profiles", runners).getResultList();
        logger.info("ProfileGroups: {}", profileGroups);
        return profileGroups;
    }

    public CommunityRun addRunnerToCommunityRun(String slug, Profile profile) {
        CommunityRun communityRun = this.find(slug);
        communityRun.getProfiles().add(profile);
        return communityRun;
    }
}
