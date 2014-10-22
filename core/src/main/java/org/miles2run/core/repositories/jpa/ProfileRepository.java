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
import java.util.List;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ProfileRepository {

    private final Logger logger = LoggerFactory.getLogger(ProfileRepository.class);

    @Inject
    private EntityManager entityManager;

    public Profile save(final Profile profile) {
//        Profile merge = entityManager.merge(profile);
        entityManager.persist(profile);
        return profile;
    }

    public Profile get(final long id) {
//        EntityGraph<Profile> entityGraph = entityManager.createEntityGraph(Profile.class);
//        entityGraph.addAttributeNodes("username");
//        Map<String, Object> hints = new HashMap<>();
//        hints.put("javax.persistence.fetchgraph", entityGraph);
        return entityManager.find(Profile.class, id);
    }

    public Profile findByUsername(final String username) {
        EntityGraph<Profile> profileEntityGraph = getProfileEntityGraph();
        try {
            final String findByUsernameQuery = "select p from Profile p where p.username =:username";
            return entityManager.createQuery(findByUsernameQuery, Profile.class)
                    .setParameter("username", username)
                    .setHint("javax.persistence.fetchgraph", profileEntityGraph)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.warn("No user found with username: {}", username);
            return null;
        }
    }

    private EntityGraph<Profile> getProfileEntityGraph() {
        EntityGraph<Profile> profileEntityGraph = entityManager.createEntityGraph(Profile.class);
        profileEntityGraph.addAttributeNodes("username", "email", "city", "country", "gender", "bio");
        return profileEntityGraph;
    }

    public Profile findByEmail(final String email) {
        try {
            final String findByEmailQuery = "select p from Profile p where p.email =:email";
            return entityManager.createQuery(findByEmailQuery, Profile.class)
                    .setParameter("email", email)
                    .setHint("javax.persistence.fetchgraph", getProfileEntityGraph())
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.warn("No user found with email: {}", email);
            return null;
        }
    }

    public List<Profile> findProfiles(final List<String> usernames) {
        return entityManager.createQuery("SELECT p from Profile p WHERE p.username IN :usernames", Profile.class).setParameter("usernames", usernames).getResultList();
    }

    public List<Profile> findProfilesWithFullnameLike(final String name) {
        return entityManager.createQuery("SELECT p from Profile p WHERE p.fullname LIKE :name", Profile.class).setParameter("name", "%" + name + "%").getResultList();
    }
}
