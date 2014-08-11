package org.miles2run.business.services.jpa;

import org.miles2run.business.domain.jpa.Activity;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.vo.ActivityCountAndDistanceTuple;
import org.miles2run.business.vo.ActivityDetails;
import org.miles2run.business.vo.Progress;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * Created by shekhargulati on 04/03/14.
 */
@Stateless
public class ActivityJPAService {

    @Inject
    private EntityManager entityManager;

    public Long save(Activity activity) {
        entityManager.persist(activity);
        return activity.getId();
    }

    public ActivityDetails findById(@NotNull Long id) {
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


    public List<ActivityDetails> findAll(@NotNull final Profile postedBy, int start, int max) {
        TypedQuery<ActivityDetails> query = entityManager.createNamedQuery("Activity.findAll", ActivityDetails.class);
        query.setFirstResult(start);
        query.setMaxResults(max);
        query.setParameter("postedBy", postedBy);
        return query.getResultList();
    }

    public List<ActivityDetails> findAll(@NotNull final Profile profile) {
        TypedQuery<ActivityDetails> query = entityManager.createNamedQuery("Activity.findAll", ActivityDetails.class);
        query.setParameter("postedBy", profile);
        List<ActivityDetails> activities = query.getResultList();
        if (activities == null) {
            return Collections.emptyList();
        }
        return activities;
    }

    public ActivityDetails update(ActivityDetails activityDetails, Activity activity) {
        Activity existingActivity = this.read(activityDetails.getId());
        existingActivity.setStatus(activity.getStatus());
        existingActivity.setDistanceCovered(activity.getDistanceCovered());
        existingActivity.setActivityDate(activity.getActivityDate());
        existingActivity.setGoalUnit(activity.getGoalUnit());
        existingActivity.setDuration(activity.getDuration());
        entityManager.persist(existingActivity);
        return this.findById(existingActivity.getId());
    }

    public void delete(@NotNull Long id) {
        Activity activity = this.read(id);
        if (activity != null) {
            entityManager.remove(activity);
        }

    }

    public Progress calculateUserProgressForGoal(@NotNull Profile profile, @NotNull Goal goal) {
        long count = entityManager.createNamedQuery("Activity.countByProfileAndGoal", Long.class).setParameter("profile", profile).setParameter("goal", goal).getSingleResult();
        if (count == 0) {
            return new Progress(goal);
        }
        TypedQuery<Progress> query = entityManager.createNamedQuery("Activity.userGoalProgress", Progress.class).setParameter("postedBy", profile).setParameter("goal", goal);
        return query.getSingleResult();
    }

    public List<Activity> findActivitiesWithTimeStamp(@NotNull Profile profile) {
        return entityManager.createQuery("SELECT NEW Activity(a.activityDate,a.distanceCovered,a.goalUnit) from Activity a WHERE a.postedBy =:profile", Activity.class).setParameter("profile", profile).getResultList();
    }


    public long count(@NotNull Profile profile) {
        long count = entityManager.createNamedQuery("Activity.countByProfile", Long.class).setParameter("profile", profile).getSingleResult();
        return count;
    }

    public ActivityDetails findByUsernameAndId(@NotNull Profile profile, @NotNull Long activityId) {
        TypedQuery<ActivityDetails> query = entityManager.createNamedQuery("Activity.findByUsernameAndId", ActivityDetails.class);
        query.setParameter("activityId", activityId);
        query.setParameter("profile", profile);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public ActivityCountAndDistanceTuple calculateTotalActivitiesAndDistanceCoveredByUser(@NotNull Profile profile) {
        return entityManager.createQuery("SELECT new org.miles2run.business.vo.ActivityCountAndDistanceTuple(COUNT(a),SUM(a.distanceCovered)) FROM Activity a where a.postedBy =:profile", ActivityCountAndDistanceTuple.class).setParameter("profile", profile).getSingleResult();
    }


}