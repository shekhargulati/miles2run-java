package org.miles2run.core.repositories.jpa;

import org.hamcrest.collection.IsCollectionWithSize;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.miles2run.core.producers.EntityManagerProducer;
import org.miles2run.core.test_helpers.TestHelpers;
import org.miles2run.domain.entities.DistanceGoal;
import org.miles2run.domain.entities.DurationGoal;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.miles2run.core.test_helpers.TestHelpers.*;

@Transactional(TransactionMode.ROLLBACK)
@RunWith(Arquillian.class)
public class GoalRepositoryTest {

    @Inject
    private GoalRepository goalRepository;
    @Inject
    private EntityManager entityManager;

    @Deployment
    public static Archive<?> deployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(domainDeployment())
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("joda-time:joda-time").withoutTransitivity().asFile())
                .addClasses(GoalRepository.class, TestHelpers.class, EntityManagerProducer.class)
                .addAsResource("META-INF/test_persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(webArchive.toString(true));
        return webArchive;
    }

    @Test
    public void goalRepositoryShouldBeNotNull() throws Exception {
        Assert.assertNotNull(goalRepository);
    }

    @Test
    @UsingDataSet({"profile.yml"})
    @ShouldMatchDataSet(value = {"distance_goal.json"}, excludeColumns = {"id", "startDate"})
    public void save_DistanceBasedGoal_SuccessfullySaved() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        DistanceGoal distanceGoal = createDistanceGoal(profile);
        DistanceGoal saved = goalRepository.save(distanceGoal);
        Assert.assertNotNull(saved.getId());
    }

    @Test
    @UsingDataSet({"profile.yml"})
    @ShouldMatchDataSet(value = {"duration_goal.json"}, excludeColumns = {"id", "startDate", "endDate"})
    public void save_DurationBasedGoal_SuccessfullySaved() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        DurationGoal durationGoal = createDuration(profile);
        DurationGoal saved = goalRepository.save(durationGoal);
        Assert.assertNotNull(saved.getId());
    }

    @UsingDataSet({"profile.yml", "distance_goal.json"})
    @Test
    @ShouldMatchDataSet(value = {"distance_goal_updated.json"}, excludeColumns = {"id"})
    public void update_UpdateDistance_SuccessfullyUpdated() throws Exception {
        DistanceGoal distanceGoal = goalRepository.find(DistanceGoal.class, 100L);
        distanceGoal.setPurpose("Run 200 miles");
        goalRepository.update(distanceGoal);
    }

    @Test
    @UsingDataSet({"profile.yml", "distance_goals.json"})
    @ShouldMatchDataSet({"distance_goals.json"})
    public void findAll_DistanceGoals_Found() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        List<Goal> goals = goalRepository.findAll(profile, false);
        assertThat(goals, IsCollectionWithSize.hasSize(2));
    }

    @Test
    @UsingDataSet({"profile.yml", "goals.json"})
    @ShouldMatchDataSet({"goals.json"})
    public void findAll_DistanceAndDurationGoals_AllFound() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);

        List<Goal> goals = goalRepository.findAll(profile, false);
        assertThat(goals, IsCollectionWithSize.hasSize(4));
    }


    @Test
    @UsingDataSet({"profile.yml", "goals.json"})
    public void shouldFindLatestCreatedGoal() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        Goal goal = goalRepository.findLatestCreatedGoal(profile);
        assertNotNull(goal);
        assertThat(goal.getId(), equalTo(1001L));
    }

    @Test
    @UsingDataSet({"profile.yml", "goals.json"})
    public void countOfActiveGoalCreatedByUser() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        long count = goalRepository.countOfActiveGoalCreatedByUser(profile);
        assertThat(count, is(equalTo(4L)));

    }
}