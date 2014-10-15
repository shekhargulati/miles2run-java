package org.miles2run.business.services.jpa;

import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.vo.ProfileGroupDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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

    public CommunityRun findById(Long id) {
        return entityManager.find(CommunityRun.class, id);
    }

    public List<CommunityRun> findAllActiveCommunityRuns() {
        return findAllActiveCommunityRuns(20, 1);
    }

    public List<CommunityRun> findAllActiveCommunityRuns(@Max(value = 20) int max, @Min(value = 1) int page) {
        return entityManager.
                createNamedQuery("CommunityRun.findAllActiveRaces", CommunityRun.class).
                setMaxResults(max).
                setFirstResult((page - 1) * max).
                getResultList();
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

    public List<Profile> findAllRunners(@NotNull String slug) {
        TypedQuery<CommunityRun> query = entityManager.createNamedQuery("CommunityRun.findBySlugWithProfiles", CommunityRun.class);
        query.setParameter("slug", slug);
        try {
            CommunityRun communityRun = query.getSingleResult();
            List<Profile> profiles = communityRun.getProfiles();
            profiles.size();
            return profiles;
        } catch (Exception e) {
            return null;
        }
    }

    public List<CommunityRun> findAllActiveCommunityRunsWithNameLike(@NotNull String name) {
        return findAllActiveCommunityRunsWithNameLike(name, 20, 1);
    }

    public List<CommunityRun> findAllActiveCommunityRunsWithNameLike(@NotNull String name, @Max(value = 20) int max, @Min(value = 1) int page) {
        name = name.toLowerCase();
        return entityManager.createNamedQuery("CommunityRun.findAllActiviRunsByNameLike", CommunityRun.class)
                .setParameter("name", "%" + name + "%")
                .setMaxResults(max)
                .setFirstResult((page - 1) * max)
                .getResultList();
    }

    public List<ProfileGroupDetails> groupAllUserInACommunityRunByCity(@NotNull String slug) {
        CommunityRun communityRun = this.find(slug);
        if (communityRun == null) {
            return Collections.emptyList();
        }
        List<Profile> runners = communityRun.getProfiles();
        if (runners.isEmpty()) {
            return Collections.emptyList();
        }
        // TODO: Possible Performance Bottleneck
        List<ProfileGroupDetails> profileGroups = entityManager.createQuery("SELECT new org.miles2run.business.vo.ProfileGroupDetails(COUNT(p),p.city,p.country) FROM Profile p where p in :profiles GROUP BY p.city", ProfileGroupDetails.class).setParameter("profiles", runners).getResultList();
        logger.info("ProfileGroups: {}", profileGroups);
        return profileGroups;
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

    // TODO: IS THIS THE RIGHT  WAY TO HANDLE CONCURRENCY?
    public CommunityRun addRunnerToCommunityRun(final String slug, final Profile profile) {
        CommunityRun communityRun = this.findBySlugWithPessimisticWriteLock(slug);
        communityRun.getProfiles().add(profile);
        entityManager.merge(communityRun);
        entityManager.flush();
        return communityRun;
    }

    public CommunityRun findBySlugWithPessimisticWriteLock(final String slug) {
        CommunityRun communityRun = entityManager.createNamedQuery("CommunityRun.findBySlugWithProfiles", CommunityRun.class).
                setParameter("slug", slug).
                setLockMode(LockModeType.PESSIMISTIC_WRITE).
                getSingleResult();
        return communityRun;
    }

    public void leaveCommunityRun(final String slug, final Profile profile) {
        CommunityRun communityRun = this.findBySlugWithPessimisticWriteLock(slug);
        communityRun.getProfiles().remove(profile);
        entityManager.merge(communityRun);
        entityManager.flush();
    }
}
