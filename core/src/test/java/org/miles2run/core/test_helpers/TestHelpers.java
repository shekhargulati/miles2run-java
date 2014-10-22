package org.miles2run.core.test_helpers;

import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence10.PersistenceDescriptor;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.miles2run.domain.entities.*;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public abstract class TestHelpers {

    public static PersistenceDescriptor persistenceDescriptor() {
        return Descriptors.create(PersistenceDescriptor.class)
                .createPersistenceUnit()
                .name("test")
                .getOrCreateProperties()
                .createProperty()
                .name("hibernate.hbm2ddl.auto")
                .value("create-drop").up()
                .createProperty()
                .name("hibernate.show_sql")
                .value("true").up().up()
                .jtaDataSource("java:jboss/datasources/ExampleDS").up();
    }

    public static File[] domainDeployment() {
        return Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.miles2run:domain")
                .withTransitivity()
                .asFile();
    }

    public static SocialConnection createSocialConnection() {
        return new SocialConnectionBuilder().setConnectionId("test_connection")
                .setAccessSecret("access_secret")
                .setAccessToken("access_token")
                .setHandle("test_user")
                .setProvider(SocialProvider.TWITTER)
                .createSocialConnection();
    }

    public static Profile createProfile() {
        return new ProfileBuilder().setUsername("test_user").setEmail("test_user@test.com").setFullname("Test User")
                .setCity("test_city").setCountry("test_country").setGender(Gender.MALE).createProfile();
    }

    public static DistanceGoal createDistanceGoal(Profile profile) {
        Date startDate = toDate(2014, 10, 22);
        return new DistanceGoalBuilder().setArchived(false)
                .setDistance(100)
                .setGoalUnit(GoalUnit.MI)
                .setDuration(new Duration(startDate, null))
                .setPurpose("100 miles run")
                .setProfile(profile)
                .createDistanceGoal();
    }

    public static Date toDate(int year, int month, int day) {
        return toDate(year, month, day, 0, 0, 0);
    }

    public static Date toDate(int year, int month, int day, int hour, int min, int sec) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, min, sec);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static DurationGoal createDuration(Profile profile) {
        Date startDate = toDate(2014, 10, 22);
        Date endDate = toDate(2014, 11, 21);
        return new DurationGoalBuilder().setArchived(false)
                .setGoalUnit(GoalUnit.MI)
                .setDuration(new Duration(startDate, endDate))
                .setPurpose("Run 30 days")
                .setProfile(profile)
                .setDays(30)
                .createDurationGoal();
    }
}
