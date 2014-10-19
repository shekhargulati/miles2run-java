package org.miles2run.core.repositories.jpa;

import org.miles2run.core.exceptions.NoRecordExistsException;
import org.miles2run.core.vo.ActivityCountAndDistanceTuple;
import org.miles2run.core.vo.ActivityDetails;
import org.miles2run.core.vo.Progress;
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

    public Long save(Activity activity) {
        entityManager.persist(activity);
        return activity.getId();
    }

    public List<ActivityDetails> findAll(@NotNull final Profile postedBy, final int start, final int max) {
        final TypedQuery<ActivityDetails> query = findAllActivitiesQuery(postedBy)
                .setFirstResult(start)
                .setMaxResults(max);
        return Collections.unmodifiableList(query.getResultList());
    }

    private TypedQuery<ActivityDetails> findAllActivitiesQuery(final Profile postedBy) {
        final String findAllActivitiesQuery = "SELECT NEW org.miles2run.core.vo.ActivityDetails(a.id,a.status,a.distanceCovered,a.goalUnit,a.activityDate,a.postedBy.fullname,a.duration,a.postedBy.username,a.postedBy.profilePic,a.createdAt,a.goal.id) FROM Activity a WHERE a.postedBy =:postedBy ORDER BY a.activityDate DESC";
        return entityManager.createQuery(findAllActivitiesQuery, ActivityDetails.class).setParameter("postedBy", postedBy);
    }

    public List<ActivityDetails> findAll(@NotNull final Profile postedBy) {
        final TypedQuery<ActivityDetails> query = findAllActivitiesQuery(postedBy);
        List<ActivityDetails> activities = query.getResultList();
        if (activities == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(activities);
    }

    public ActivityDetails update(final ActivityDetails activityDetails, final Activity activity) {
        Activity existingActivity = this.read(activityDetails.getId());
        existingActivity.setStatus(activity.getStatus());
        existingActivity.setDistanceCovered(activity.getDistanceCovered());
        existingActivity.setActivityDate(activity.getActivityDate());
        existingActivity.setGoalUnit(activity.getGoalUnit());
        existingActivity.setDuration(activity.getDuration());
        entityManager.persist(existingActivity);
        return this.findById(existingActivity.getId());
    }

    public ActivityDetails findById(@NotNull final Long id) {
        final String activityByIdQuery = "SELECT new org.miles2run.core.vo.ActivityDetails(a.id,a.status,a.distanceCovered,a.goalUnit,a.activityDate,a.postedBy.fullname,a.duration,a.postedBy.username,a.postedBy.profilePic,a.createdAt,a.goal.id) from Activity a where a.id =:id";
        final TypedQuery<ActivityDetails> query = entityManager
                .createQuery(activityByIdQuery, ActivityDetails.class)
                .setParameter("id", id);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new NoRecordExistsException(String.format("No activity exists for id %d", id));
        }
    }

    public Activity read(@NotNull final Long id) {
        return entityManager.find(Activity.class, id);
    }

    public void delete(@NotNull final Long id) {
        Activity activity = this.read(id);
        if (activity != null) {
            entityManager.remove(activity);
        }

    }

    public Progress calculateUserProgressForGoal(@NotNull final Profile profile, @NotNull final Goal goal) {
        String activityCountForUserGoalQuery = "SELECT COUNT(a) FROM Activity a WHERE a.postedBy =:profile and a.goal =:goal";
        long count = entityManager.createQuery(activityCountForUserGoalQuery, Long.class).setParameter("profile", profile).setParameter("goal", goal).getSingleResult();
        if (count == 0) {
            return new Progress(goal);
        }
        String goalProgressQuery = "SELECT new org.miles2run.core.vo.Progress(a.goal.distance,a.goal.goalUnit,SUM(a.distanceCovered),COUNT(a), SUM(a.duration) ,a.goal.goalType) from Activity a WHERE a.postedBy =:postedBy and a.goal =:goal";
        TypedQuery<Progress> query = entityManager.createQuery(goalProgressQuery, Progress.class).setParameter("postedBy", profile).setParameter("goal", goal);
        return query.getSingleResult();
    }

    public List<Activity> findActivitiesWithTimeStamp(@NotNull final Profile profile) {
        return entityManager.createQuery("SELECT NEW Activity(a.activityDate,a.distanceCovered,a.goalUnit) from Activity a WHERE a.postedBy =:profile", Activity.class).setParameter("profile", profile).getResultList();
    }

    public long count(@NotNull final Profile profile) {
        final String activityCountForUserQuery = "SELECT COUNT(a) FROM Activity a WHERE a.postedBy =:profile";
        return entityManager.createQuery(activityCountForUserQuery, Long.class)
                .setParameter("profile", profile)
                .getSingleResult();
    }

    public ActivityDetails findByUsernameAndId(@NotNull final Profile profile, @NotNull final Long activityId) {
        final String findActivityWithProfileAndIdQuery = "SELECT new org.miles2run.core.vo.ActivityDetails(a.id,a.status,a.distanceCovered,a.goalUnit,a.activityDate,a.postedBy.fullname,a.duration,a.postedBy.username,a.postedBy.profilePic,a.createdAt,a.goal.id) from Activity a where a.id =:activityId and a.postedBy =:profile";
        final TypedQuery<ActivityDetails> query = entityManager
                .createQuery(findActivityWithProfileAndIdQuery, ActivityDetails.class)
                .setParameter("activityId", activityId)
                .setParameter("profile", profile);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new NoRecordExistsException(String.format("No activity exists for username %s and activityId %d", profile.getUsername(), activityId));
        }
    }

    public ActivityCountAndDistanceTuple calculateTotalActivitiesAndDistanceCoveredByUser(@NotNull final Profile profile) {
        final String totalActivitiesAndDistanceForUserQuery = "SELECT new org.miles2run.core.vo.ActivityCountAndDistanceTuple(COUNT(a),SUM(a.distanceCovered)) FROM Activity a where a.postedBy =:profile";
        return entityManager
                .createQuery(totalActivitiesAndDistanceForUserQuery, ActivityCountAndDistanceTuple.class)
                .setParameter("profile", profile)
                .getSingleResult();
    }

    public List<ActivityDetails> findAllActivitiesWithIds(final List<Long> activityIds) {
        final String query = "SELECT new org.miles2run.core.vo.ActivityDetails(a.id,a.status,a.distanceCovered,a.goalUnit,a.activityDate,a.postedBy.fullname,a.duration,a.postedBy.username,a.postedBy.profilePic,a.createdAt,a.goal.id) from Activity a where a.id IN :activityIds order by a.activityDate desc";
        List<ActivityDetails> activities = entityManager.createQuery(query, ActivityDetails.class)
                .setParameter("activityIds", activityIds)
                .getResultList();
        return Collections.unmodifiableList(activities);
    }
}