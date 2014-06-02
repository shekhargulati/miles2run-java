package org.miles2run.business.services;

import org.miles2run.business.domain.Activity;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.vo.ActivityDetails;
import org.miles2run.business.vo.Progress;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * Created by shekhargulati on 04/03/14.
 */
@Stateless
public class ActivityService {

    @Inject
    private EntityManager entityManager;
    @Inject
    private ProfileService profileService;

    public Activity save(Activity activity, Profile profile) {
        activity.setPostedBy(profile);
        entityManager.persist(activity);
        return activity;
    }

    public ActivityDetails readById(@NotNull Long id) {
        TypedQuery<ActivityDetails> query = entityManager.createNamedQuery("Activity.findById", ActivityDetails.class).setParameter("id", id);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Activity read(@NotNull Long id) {
        return entityManager.find(Activity.class, id);
    }


    public List<ActivityDetails> findAll(Profile postedBy, int start, int max) {
        TypedQuery<ActivityDetails> query = entityManager.createNamedQuery("Activity.findAll", ActivityDetails.class);
        query.setFirstResult(start);
        query.setMaxResults(max);
        query.setParameter("postedBy", postedBy);
        return query.getResultList();
    }

    public List<ActivityDetails> findAll(String username) {
        Profile profile = profileService.findProfile(username);
        TypedQuery<ActivityDetails> query = entityManager.createNamedQuery("Activity.findAll", ActivityDetails.class);
        query.setParameter("postedBy", profile);
        List<ActivityDetails> activities = query.getResultList();
        if (activities == null) {
            return Collections.emptyList();
        }
        return activities;
    }

    public ActivityDetails update(Activity existingActivity, Activity activity) {
        existingActivity = this.read(existingActivity.getId());
        existingActivity.setStatus(activity.getStatus());
        existingActivity.setDistanceCovered(activity.getDistanceCovered());
        existingActivity.setActivityDate(activity.getActivityDate());
        existingActivity.setGoalUnit(activity.getGoalUnit());
        existingActivity.setDuration(activity.getDuration());
        entityManager.persist(existingActivity);
        return this.readById(existingActivity.getId());
    }

    public void delete(@NotNull Long id) {
        Activity activity = this.read(id);
        if (activity != null) {
            entityManager.remove(activity);
        }

    }

    public Progress findTotalDistanceCovered(Profile profile) {
        long count = entityManager.createNamedQuery("Activity.countByProfile", Long.class).setParameter("profile", profile).getSingleResult();
        if (count == 0) {
            return null;
        }
        TypedQuery<Progress> query = entityManager.createQuery("SELECT new org.miles2run.business.vo.Progress(a.postedBy.goal,a.postedBy.goalUnit, SUM(a.distanceCovered),COUNT(a)) from Activity a WHERE a.postedBy =:postedBy", Progress.class).setParameter("postedBy", profile);
        return query.getSingleResult();
    }

    public List<Activity> findActivitiesWithTimeStamp(Profile profile) {
        return entityManager.createQuery("SELECT NEW Activity(a.activityDate,a.distanceCovered,a.goalUnit) from Activity a WHERE a.postedBy =:profile", Activity.class).setParameter("profile", profile).getResultList();
    }

    public Progress findTotalDistanceCovered(String username) {
        Profile profile = profileService.findProfile(username);
        long count = entityManager.createNamedQuery("Activity.countByProfile", Long.class).setParameter("profile", profile).getSingleResult();
        if (count == 0) {
            return new Progress();
        }
        TypedQuery<Progress> query = entityManager.createQuery("SELECT new org.miles2run.business.vo.Progress(a.postedBy.goal,a.postedBy.goalUnit, SUM(a.distanceCovered),COUNT(a)) from Activity a WHERE a.postedBy =:postedBy", Progress.class).setParameter("postedBy", profile);
        return query.getSingleResult();
    }


    public long count(String username) {
        Profile profile = profileService.findProfile(username);
        long count = entityManager.createNamedQuery("Activity.countByProfile", Long.class).setParameter("profile", profile).getSingleResult();
        return count;
    }
}