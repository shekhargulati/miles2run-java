package org.miles2run.core.test_helpers;

import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence10.PersistenceDescriptor;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.miles2run.domain.entities.*;

import java.io.File;

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
}
