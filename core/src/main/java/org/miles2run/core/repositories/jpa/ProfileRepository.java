package org.miles2run.core.repositories.jpa;

import org.miles2run.domain.entities.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;
import java.util.List;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ProfileRepository {

    public static final String FIND_BY_EMAIL_QUERY = "select p from Profile p where p.email =:email";
    public static final String FIND_PROFILES_BY_USERNAMES = "SELECT p from Profile p WHERE p.username IN :usernames";
    public static final String FIND_BY_FULLNAME_LIKE = "SELECT p from Profile p WHERE lower(p.fullname) LIKE :name";
    private static final String FIND_BY_USERNAME_QUERY = "select p from Profile p where p.username =:username";
    private final Logger logger = LoggerFactory.getLogger(ProfileRepository.class);
    @Inject
    private EntityManager entityManager;

    public Profile save(final Profile profile) {
//        Profile merge = entityManager.merge(profile);
        entityManager.persist(profile);
        return profile;
    }

    public Profile get(final long id) {
        return entityManager.find(Profile.class, id);
    }

    public Profile findByUsername(@NotNull final String username) {
        try {
            return entityManager.createQuery(FIND_BY_USERNAME_QUERY, Profile.class)
                    .setParameter("username", username)
                    .setHint("javax.persistence.fetchgraph", getProfileEntityGraph())
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.warn("No user found with username: {}", username);
            return null;
        }
    }

    private EntityGraph<Profile> getProfileEntityGraph() {
        EntityGraph<Profile> profileEntityGraph = entityManager.createEntityGraph(Profile.class);
        profileEntityGraph.addAttributeNodes("username", "city", "country", "gender", "bio");
        return profileEntityGraph;
    }

    public Profile findByEmail(@NotNull final String email) {
        try {
            return entityManager.createQuery(FIND_BY_EMAIL_QUERY, Profile.class)
                    .setParameter("email", email)
                    .setHint("javax.persistence.fetchgraph", getProfileEntityGraph())
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.warn("No user found with email: {}", email);
            return null;
        }
    }

    public List<Profile> findProfiles(final List<String> usernames) {
        return entityManager.createQuery(FIND_PROFILES_BY_USERNAMES, Profile.class).setParameter("usernames", usernames).getResultList();
    }

    public List<Profile> findProfilesWithFullnameLike(@NotNull final String name) {
        String nameInLowercase = name.toLowerCase();
        return entityManager.createQuery(FIND_BY_FULLNAME_LIKE, Profile.class).setParameter("name", "%" + nameInLowercase + "%").getResultList();
    }

    public Profile findWithSocialConnections(@NotNull final String username) {
        try {
            EntityGraph<Profile> entityGraph = entityManager.createEntityGraph(Profile.class);
            entityGraph.addAttributeNodes("username");
            entityGraph.addSubgraph("socialConnections").addAttributeNodes("provider");
            return entityManager.createQuery(FIND_BY_USERNAME_QUERY, Profile.class)
                    .setParameter("username", username)
                    .setHint("javax.persistence.fetchgraph", entityGraph)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
