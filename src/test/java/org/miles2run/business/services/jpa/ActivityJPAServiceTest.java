package org.miles2run.business.services.jpa;

import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.miles2run.business.domain.jpa.*;
import org.miles2run.business.producers.EntityManagerProducer;
import org.miles2run.business.vo.*;
import org.miles2run.jaxrs.forms.ProfileForm;
import org.miles2run.shared.repositories.ProfileRepository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.Date;

/**
 * Created by shekhargulati on 10/08/14.
 */
@RunWith(Arquillian.class)
public class ActivityJPAServiceTest {

    @Inject
    private GoalJPAService goalJPAService;
    @Inject
    private ProfileRepository profileRepository;
    @Inject
    private EntityManager entityManager;
    @Inject
    private UserTransaction userTransaction;
    @Inject
    private ActivityJPAService activityJPAService;

    @Deployment
    public static Archive<?> deployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class).
                addClass(BaseEntity.class).
                addClass(Profile.class).
                addClass(SocialConnection.class).
                addClass(ProfileRepository.class).
                addClass(ProfileDetails.class).
                addClass(ProfileSocialConnectionDetails.class).
                addClass(SocialProvider.class).
                addClass(Role.class).
                addClass(Gender.class).
                addClass(ProfileForm.class).
                addClass(ProfileGroupDetails.class).
                addClass(Goal.class).
                addClass(GoalUnit.class).
                addClass(GoalType.class).
                addClass(Profile.class).
                addClass(EntityManagerProducer.class).
                addClass(GoalJPAService.class).
                addClass(ProfileRepository.class).
                addClass(Activity.class).
                addClass(ActivityJPAService.class).
                addClass(ActivityDetails.class).
                addClass(ActivityCountAndDistanceTuple.class).
                addClass(Progress.class).
                addClass(CommunityRun.class).
                addClass(Share.class).
                addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("org.jadira.usertype:usertype.core").withTransitivity().asFile()).
                addAsResource("META-INF/test_persistence.xml", "META-INF/persistence.xml").
                addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.printf("WebArchive %s", webArchive.toString(true));
        return webArchive;
    }

    @Before
    public void setUp() throws Exception {
        userTransaction.begin();
        entityManager.createQuery("DELETE from Activity a").executeUpdate();
        entityManager.createQuery("DELETE from Goal g").executeUpdate();
        entityManager.createQuery("DELETE from Profile p").executeUpdate();
        userTransaction.commit();
    }

    @Test
    public void calculateTotalActivitiesAndDistanceCoveredByUser_5ActivitiesEachWith20AsDistanceCovered_TotalActivities5AndTotalDistance100() throws Exception {
        Profile profile = createProfile();
        Goal goal = createGoal(profile);
        for (int i = 0; i < 5; i++) {
            Activity activity = new Activity(new Date(), 20d, GoalUnit.MI);
            activity.setGoal(goal);
            activity.setPostedBy(profile);
            activityJPAService.save(activity);
        }
        ActivityCountAndDistanceTuple tuple = activityJPAService.calculateTotalActivitiesAndDistanceCoveredByUser(profile);
        Assert.assertThat(tuple.getActivityCount(), CoreMatchers.is(CoreMatchers.equalTo(5L)));
        Assert.assertThat(tuple.getDistanceCovered(), CoreMatchers.is(CoreMatchers.equalTo(100d)));
    }

    Goal createGoal(Profile profile) {
        Goal goal = new Goal();
        goal.setDistance(100);
        goal.setGoalUnit(GoalUnit.MI);
        goal.setPurpose("Run 100 miles");
        goal.setGoalType(GoalType.DISTANCE_GOAL);
        return goalJPAService.save(goal, profile);
    }

    private Profile createProfile() {
        Profile profile = Profile.createProfile("test@test.com", "test_user", "Test User", "city", "country", Gender.MALE);
        return profileRepository.save(profile);
    }

    @Test
    public void calculateTotalActivitiesAndDistanceCoveredByUser_NoActivityForAGoal_TotalActivities0TotalDistance0() throws Exception {
        Profile profile = createProfile();
        ActivityCountAndDistanceTuple tuple = activityJPAService.calculateTotalActivitiesAndDistanceCoveredByUser(profile);
        System.out.printf("ActivityCountAndDistanceTuple %s", tuple);
        Assert.assertThat(tuple.getActivityCount(), CoreMatchers.is(CoreMatchers.equalTo(0L)));
        Assert.assertThat(tuple.getDistanceCovered(), CoreMatchers.is(CoreMatchers.equalTo(0d)));
    }

}
