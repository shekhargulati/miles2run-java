package org.miles2run.business.services;

import org.miles2run.business.domain.GoalUnit;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.domain.SocialProvider;
import org.miles2run.business.vo.ProfileSocialConnectionDetails;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 04/03/14.
 */
@Stateless
public class ProfileService {

    @Inject
    EntityManager entityManager;
    @Inject
    Logger logger;

    public Profile save(Profile profile) {
        entityManager.persist(profile);
        return profile;
    }

    public Profile findProfile(String username) {
        try {
            return entityManager.createQuery("select p from Profile p where p.username =:username", Profile.class).setParameter("username", username).getSingleResult();
        } catch (NoResultException e) {
            logger.fine(String.format("No user found with username: %s", username));
            return null;
        }
    }

    public Profile findProfileByUsername(String username) {
        TypedQuery<Profile> query = entityManager.createNamedQuery("Profile.findByUsername", Profile.class);
        query.setParameter("username", username);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            logger.fine(String.format("No user found with username: %s", username));
            return null;
        }
    }

    public Profile findProfileByEmail(String email) {
        TypedQuery<Profile> query = entityManager.createNamedQuery("Profile.findByEmail", Profile.class);
        query.setParameter("email", email);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            logger.fine(String.format("No user found with email: %s", email));
            return null;
        }
    }

    public ProfileSocialConnectionDetails findProfileWithSocialConnections(String username) {
        Query query = entityManager.createNamedQuery("Profile.findProfileWithSocialNetworks").setParameter("username", username);
        List result = query.getResultList();
        if (result == null || result.isEmpty()) {
            return null;
        }
        ProfileSocialConnectionDetails profileSocialConnectionDetails = new ProfileSocialConnectionDetails();
        for (Object object : result) {
            if (object instanceof Object[]) {
                Object[] row = (Object[]) object;
                profileSocialConnectionDetails.setId((Long) row[0]);
                profileSocialConnectionDetails.setUsername((String) row[1]);
                profileSocialConnectionDetails.setGoal((Long) row[2]);
                profileSocialConnectionDetails.setGoalUnit((GoalUnit) row[3]);
                profileSocialConnectionDetails.getProviders().add(((SocialProvider) row[4]).getProvider());
            }
        }
        return profileSocialConnectionDetails;
    }

    public Profile findFullProfileByUsername(String username) {
        TypedQuery<Profile> query = entityManager.createNamedQuery("Profile.findFullProfileByUsername", Profile.class);
        query.setParameter("username", username);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            logger.fine(String.format("No user found with username: %s", username));
            return null;
        }
    }

    public void update(Profile profile) {
        Query updateQuery = entityManager.createQuery("UPDATE Profile p SET p.fullname =:fullname, p.bio =:bio,p.goal =:goal,p.goalUnit =:goalUnit,p.city =:city, p.country =:country, p.gender =:gender WHERE p.username =:username");
        updateQuery.setParameter("fullname", profile.getFullname());
        updateQuery.setParameter("bio", profile.getBio());
        updateQuery.setParameter("goal", profile.getGoal());
        updateQuery.setParameter("goalUnit", profile.getGoalUnit());
        updateQuery.setParameter("city", profile.getCity());
        updateQuery.setParameter("country", profile.getCountry());
        updateQuery.setParameter("gender", profile.getGender());
        updateQuery.setParameter("username", profile.getUsername());
        updateQuery.executeUpdate();
    }
}
