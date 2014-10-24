package org.miles2run.core.repositories.jpa;

import org.miles2run.core.exceptions.NoRecordExistsException;
import org.miles2run.domain.entities.CommunityRun;
import org.miles2run.domain.entities.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class CommunityRunRepository {

    private Logger logger = LoggerFactory.getLogger(CommunityRunRepository.class);

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
        return findAllActiveCommunityRuns(1, 20);
    }

    public List<CommunityRun> findAllActiveCommunityRuns(@Min(value = 1) final int page, @Max(value = 20) final int max) {
        final String allActiveRunsQuery = "SELECT cr FROM CommunityRun cr WHERE cr.active IS TRUE";
        List<CommunityRun> runs = entityManager.
                createQuery(allActiveRunsQuery, CommunityRun.class).
                setMaxResults(max).
                setFirstResult((page - 1) * max).
                getResultList();
        return Collections.unmodifiableList(runs);
    }

    public CommunityRun findBySlug(@NotNull final String slug) {
        final String communityRunBySlugQuery = "SELECT cr FROM CommunityRun cr WHERE cr.slug =:slug";
        TypedQuery<CommunityRun> query = entityManager.createQuery(communityRunBySlugQuery, CommunityRun.class)
                .setParameter("slug", slug);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Set<Profile> findAllRunners(@NotNull final String slug) {
        final String communityRunBySlugQuery = "SELECT cr FROM CommunityRun cr WHERE cr.slug =:slug";
        TypedQuery<CommunityRun> query = entityManager.createQuery(communityRunBySlugQuery, CommunityRun.class)
                .setParameter("slug", slug);
        try {
            CommunityRun communityRun = query.getSingleResult();
            Set<Profile> profiles = communityRun.getRunners();
            profiles.size();
            return profiles;
        } catch (NoResultException e) {
            throw new NoRecordExistsException(String.format("No community run exists for slug %s", slug));
        }
    }

    public List<CommunityRun> findAllActiveCommunityRunsWithNameLike(@NotNull final String name) {
        return findAllActiveCommunityRunsWithNameLike(name, 1, 20);
    }

    public List<CommunityRun> findAllActiveCommunityRunsWithNameLike(@NotNull String name, @Min(value = 1) int page, @Max(value = 20) int max) {
        name = name.toLowerCase();
        String runNameLikeQuery = "SELECT cr from CommunityRun cr WHERE LOWER(cr.name) LIKE :name and cr.active IS TRUE";
        List<CommunityRun> runs = entityManager.createQuery(runNameLikeQuery, CommunityRun.class)
                .setParameter("name", "%" + name + "%")
                .setMaxResults(max)
                .setFirstResult((page - 1) * max)
                .getResultList();
        return Collections.unmodifiableList(runs);
    }

    public Set<Profile> allRunners(@NotNull final String slug) {
        CommunityRun communityRun = this.find(slug);
        if (communityRun == null) {
            return emptySet();
        }
        Set<Profile> runners = communityRun.getRunners();
        if (runners.isEmpty()) {
            return emptySet();
        }
        return unmodifiableSet(runners);
    }

    public CommunityRun find(@NotNull final String slug) {
        final String runBySlugQuery = "SELECT cr FROM CommunityRun cr WHERE cr.slug =:slug";
        TypedQuery<CommunityRun> query = entityManager.createQuery(runBySlugQuery, CommunityRun.class)
                .setParameter("slug", slug);
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            throw new NoRecordExistsException(String.format("No community run exists for slug %s", slug));
        }
    }

    // TODO: IS THIS THE RIGHT  WAY TO HANDLE CONCURRENCY?
    public CommunityRun addRunnerToCommunityRun(final String slug, final Profile profile) {
        CommunityRun communityRun = this.findBySlugWithPessimisticWriteLock(slug);
        communityRun.getRunners().add(profile);
        entityManager.merge(communityRun);
        entityManager.flush();
        return communityRun;
    }

    public CommunityRun findBySlugWithPessimisticWriteLock(final String slug) {
        final String runBySlugQuery = "SELECT cr FROM CommunityRun cr WHERE cr.slug =:slug";

        CommunityRun communityRun = entityManager.createQuery(runBySlugQuery, CommunityRun.class).
                setParameter("slug", slug).
                setLockMode(LockModeType.PESSIMISTIC_WRITE).
                getSingleResult();
        return communityRun;
    }

    public void leaveCommunityRun(final String slug, final Profile profile) {
        CommunityRun communityRun = this.findBySlugWithPessimisticWriteLock(slug);
        communityRun.getRunners().remove(profile);
        entityManager.merge(communityRun);
        entityManager.flush();
    }
}
