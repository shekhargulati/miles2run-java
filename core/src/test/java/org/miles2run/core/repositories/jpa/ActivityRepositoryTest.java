package org.miles2run.core.repositories.jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.miles2run.core.producers.EntityManagerProducer;
import org.miles2run.core.test_helpers.TestHelpers;
import org.miles2run.core.vo.ActivityCountAndDistanceTuple;
import org.miles2run.domain.entities.Activity;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.GoalUnit;
import org.miles2run.domain.entities.Profile;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.miles2run.core.test_helpers.TestHelpers.createActivity;
import static org.miles2run.core.test_helpers.TestHelpers.domainDeployment;

@RunWith(Arquillian.class)
public class ActivityRepositoryTest {

    @Inject
    private ActivityRepository activityRepository;
    @Inject
    private EntityManager entityManager;

    @Deployment
    public static Archive<?> deployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class).addAsLibraries(domainDeployment())
                .addClasses(ActivityRepository.class, TestHelpers.class, EntityManagerProducer.class, ActivityCountAndDistanceTuple.class)
                .addAsResource("META-INF/test_persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(webArchive.toString(true));
        return webArchive;
    }

    @Test
    public void activityRepositoryShouldBeNotNull() throws Exception {
        assertNotNull(activityRepository);
    }

    @Test
    @UsingDataSet({"profile.yml", "distance_goal.json"})
    @ShouldMatchDataSet(value = {"activity.json"}, excludeColumns = {"id", "activityDate"})
    public void shouldSaveActivity() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        Goal goal = entityManager.find(Goal.class, 100L);
        Activity activity = createActivity(profile, goal);
        Activity saved = activityRepository.save(activity);
        assertNotNull(saved.getId());
    }

    @Test
    @UsingDataSet({"profile.yml", "distance_goal.json", "activities.json"})
    @ShouldMatchDataSet(value = {"activities.json"}, excludeColumns = {"id"})
    public void findAll_ThreeActivitiesStoredInDatabase_3ActivitiesFound() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        List<Activity> activities = activityRepository.findAll(profile);
        assertThat(activities, hasSize(3));
    }

    @Test
    @UsingDataSet({"profiles.yml", "distance_goals_diff_users.json", "activities_diff_users.json"})
    public void findAll_ThreeActivitiesStoredInDatabase_2ActivitiesForUserFound() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1001L);
        List<Activity> activities = activityRepository.findAll(profile);
        assertThat(activities, hasSize(2));
    }

    @Test
    @UsingDataSet({"profile.yml", "distance_goal.json", "activity.json"})
    @ShouldMatchDataSet(value = {"activity_updated.json"}, excludeColumns = {"id"})
    public void shouldUpdateActivity() throws Exception {
        Activity activity = activityRepository.get(10000L);
        activity.setDistanceCovered(4.5);
        activity.setGoalUnit(GoalUnit.KM);
        activityRepository.update(activity);
    }

    @Test
    @UsingDataSet({"profile.yml", "distance_goal.json", "activities.json"})
    @ShouldMatchDataSet(value = {"activities.json"}, excludeColumns = {"id"})
    public void count_TotalActivitiesForAUser() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        long count = activityRepository.count(profile);
        assertThat(count, is(equalTo(3L)));
    }

    @Test
    @UsingDataSet({"profiles.yml", "distance_goals_diff_users.json", "activities_diff_users.json"})
    public void count_ThreeActivitiesStoredInDatabase_2ActivitiesForUserFound() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1001L);
        long count = activityRepository.count(profile);
        assertThat(count, is(equalTo(2L)));
    }

    @Test
    @UsingDataSet({"profile.yml", "distance_goal.json", "activity.json"})
    @ShouldMatchDataSet(value = {"activity.json"}, excludeColumns = {"id"})
    public void shouldFindByProfileAndActivityId() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        Activity activity = activityRepository.findByProfileAndId(profile, 10000L);
        assertNotNull(activity);
    }

    @Test
    @UsingDataSet({"profile.yml", "distance_goal.json", "activities.json"})
    public void shouldCalculateTotalActivitiesAndDistanceCoveredByUser() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        ActivityCountAndDistanceTuple tuple = activityRepository.calculateTotalActivitiesAndDistanceCoveredByUser(profile);
        assertThat(tuple.getActivityCount(), is(equalTo(3L)));
        assertThat(tuple.getDistanceCovered(), is(equalTo(10.5)));
    }

    @Test
    @UsingDataSet({"profile.yml", "distance_goal.json", "activities.json"})
    public void shouldFindAllActivitiesWithIds() throws Exception {
        List<Activity> activities = activityRepository.findAllActivitiesWithIds(Arrays.asList(10000L, 10001L, 10002L));
        assertThat(activities, hasSize(3));

    }
}