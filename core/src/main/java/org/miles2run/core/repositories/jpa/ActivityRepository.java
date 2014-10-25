package org.miles2run.core.repositories.jpa;

import org.miles2run.core.repositories.jpa.vo.ActivityCountAndDistanceTuple;
import org.miles2run.core.repositories.jpa.vo.Progress;
import org.miles2run.domain.entities.Activity;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ActivityRepository {

    @Inject
    private EntityManager entityManager;

    public Activity save(Activity activity) {
        entityManager.persist(activity);
        return activity;
    }

    public List<Activity> findAll(@NotNull final Profile postedBy, final int start, final int max) {
        final TypedQuery<Activity> query = findAllActivitiesQuery(postedBy)
                .setFirstResult(start)
                .setMaxResults(max);
        return Collections.unmodifiableList(query.getResultList());
    }

    private TypedQuery<Activity> findAllActivitiesQuery(final Profile postedBy) {
        final String findAllActivitiesQuery = "SELECT a FROM Activity a WHERE a.postedBy =:postedBy ORDER BY a.activityDate DESC";
        return entityManager.createQuery(findAllActivitiesQuery, Activity.class).setParameter("postedBy", postedBy);
    }

    public Activity update(final Activity activity) {
        Activity merged = entityManager.merge(activity);
        entityManager.persist(merged);
        return merged;
    }

    public List<Activity> findAll(@NotNull final Profile postedBy) {
        final TypedQuery<Activity> query = findAllActivitiesQuery(postedBy);
        List<Activity> activities = query.getResultList();
        return Collections.unmodifiableList(activities);
    }

    public Activity get(@NotNull final Long id) {
        final String activityByIdQuery = "SELECT a from Activity a where a.id =:id";
        final TypedQuery<Activity> query = entityManager
                .createQuery(activityByIdQuery, Activity.class)
                .setParameter("id", id);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void delete(@NotNull final Long id) {
        Activity activity = this.read(id);
        if (activity != null) {
            entityManager.remove(activity);
        }

    }

    public Activity read(@NotNull final Long id) {
        return entityManager.find(Activity.class, id);
    }

    public Progress calculateUserProgressForGoal(@NotNull final Profile profile, @NotNull final Goal goal) {
        long count = goalActivityCount(profile, goal);
        if (count == 0) {
            return new Progress(goal);
        }
        String goalProgressQuery = "SELECT new org.miles2run.core.repositories.jpa.vo.Progress(a.goal,SUM(a.distanceCovered),COUNT(a), SUM(a.duration)) from Activity a WHERE a.postedBy =:postedBy and a.goal =:goal";
        TypedQuery<Progress> query = entityManager.createQuery(goalProgressQuery, Progress.class).setParameter("postedBy", profile).setParameter("goal", goal);
        return query.getSingleResult();
    }

    public long goalActivityCount(@NotNull final Profile profile, @NotNull final Goal goal) {
        String query = "SELECT COUNT(a) FROM Activity a WHERE a.postedBy =:profile and a.goal =:goal";
        return entityManager.createQuery(query, Long.class).setParameter("profile", profile).setParameter("goal", goal).getSingleResult();
    }

    public long userActivityCount(@NotNull final Profile profile) {
        final String activityCountForUserQuery = "SELECT COUNT(a) FROM Activity a WHERE a.postedBy =:profile";
        return entityManager.createQuery(activityCountForUserQuery, Long.class)
                .setParameter("profile", profile)
                .getSingleResult();
    }

    public Activity findByProfileAndId(@NotNull final Profile profile, @NotNull final Long activityId) {
        final String findActivityWithProfileAndIdQuery = "SELECT a from Activity a where a.id =:activityId and a.postedBy =:profile";
        final TypedQuery<Activity> query = entityManager
                .createQuery(findActivityWithProfileAndIdQuery, Activity.class)
                .setParameter("activityId", activityId)
                .setParameter("profile", profile);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ActivityCountAndDistanceTuple calculateTotalActivitiesAndDistanceCoveredByUser(@NotNull final Profile profile) {
        final String totalActivitiesAndDistanceForUserQuery = "SELECT new org.miles2run.core.repositories.jpa.vo.ActivityCountAndDistanceTuple(COUNT(a),SUM(a.distanceCovered)) FROM Activity a where a.postedBy =:profile";
        return entityManager
                .createQuery(totalActivitiesAndDistanceForUserQuery, ActivityCountAndDistanceTuple.class)
                .setParameter("profile", profile)
                .getSingleResult();
    }

    public List<Activity> findAllActivitiesWithIds(final List<Long> activityIds) {
        final String query = "SELECT a from Activity a where a.id IN :activityIds order by a.activityDate desc";
        List<Activity> activities = entityManager.createQuery(query, Activity.class)
                .setParameter("activityIds", activityIds)
                .getResultList();
        return Collections.unmodifiableList(activities);
    }
}
