package org.miles2run.core.repositories.jpa;

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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.miles2run.core.producers.EntityManagerProducer;
import org.miles2run.core.test_helpers.TestHelpers;
import org.miles2run.domain.entities.Profile;
import org.miles2run.domain.entities.SocialConnection;

import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import java.util.HashMap;
import java.util.Map;

import static org.miles2run.core.test_helpers.TestHelpers.*;

@RunWith(Arquillian.class)
public class ProfileRepositoryTest {

    @Inject
    private ProfileRepository profileRepository;

    @Inject
    private EntityManager entityManager;

    @Deployment
    public static Archive<?> deployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(domainDeployment())
                .addClasses(ProfileRepository.class, TestHelpers.class, EntityManagerProducer.class)
                .addAsResource("META-INF/test_persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(webArchive.toString(true));
        return webArchive;
    }

    @Test
    public void profileRepositoryInstanceIsNotNull() throws Exception {
        Assert.assertNotNull(profileRepository);
    }

    @Test
    @ShouldMatchDataSet(value = {"profile.yml"}, excludeColumns = {"id"})
    public void save_NewProfile_ShouldBeCreated() throws Exception {
        Profile profile = createProfile();
        Profile saved = profileRepository.save(profile);
        Assert.assertNotNull(saved);
        Assert.assertNotNull(saved.getId());
    }

    @Test(expected = Exception.class)
    public void save_ExistingEmail_ThrowsException() throws Exception {
        Profile profile = createProfile();
        profileRepository.save(profile);
        profileRepository.save(profile);
    }

    @Test
    @UsingDataSet({"profile.yml"})
    @ShouldMatchDataSet(value = {"profile.yml"}, excludeColumns = {"id"})
    public void get_ExistingProfile_Found() throws Exception {
        Profile profile = profileRepository.get(1000L);
        Assert.assertNotNull(profile);
    }

    @Test
    @UsingDataSet({"profile.yml"})
    @ShouldMatchDataSet(value = {"profile.yml"}, excludeColumns = {"id"})
    public void findByUsername_ExistingProfile_Found() throws Exception {
        Profile profile = profileRepository.findByUsername("test_user");
        Assert.assertNotNull(profile);
    }

    @Test
    @UsingDataSet({"profile.yml"})
    @ShouldMatchDataSet(value = {"profile.yml"}, excludeColumns = {"id"})
    public void findByEmail_ExistingProfile_Found() throws Exception {
        Profile profile = profileRepository.findByEmail("test_user@test.com");
        Assert.assertNotNull(profile);
    }

    @Test
    @UsingDataSet({"profile.yml"})
    @ShouldMatchDataSet(value = {"profile.yml", "social_connection.yml"}, excludeColumns = {"id"})
    public void shouldAddSocialConnectionToProfile() throws Exception {
        Profile profile = profileRepository.get(1000L);
        profile.addSocialConnection(createSocialConnection());
        profileRepository.save(profile);
    }

    @Transactional(TransactionMode.DISABLED)
    @Test
    @ShouldMatchDataSet(value = {"profile.yml", "social_connection.yml"}, excludeColumns = {"id"})
    public void save_NewProfileWithSocialConnection_ShouldPersist() throws Exception {
        Profile profile = createProfile();
        profile.addSocialConnection(createSocialConnection());
        Profile saved = profileRepository.save(profile);
        Profile persistedProfile = profileRepository.get(saved.getId());
        for (SocialConnection socialConnection : persistedProfile.getSocialConnections()) {
            Assert.assertNotNull(socialConnection.getId());
        }
    }

    @Transactional(TransactionMode.DISABLED)
    @Test
    @UsingDataSet({"profile.yml", "social_connections.yml"})
    public void testEntityGraphs() throws Exception {
        EntityGraph entityGraph = entityManager.getEntityGraph("Profile.WithConnections");
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", entityGraph);
        Profile profile = entityManager.find(Profile.class, 1000L, hints);
        PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        Assert.assertTrue("Username should be loaded", persistenceUnitUtil.isLoaded(profile, "username"));
        Assert.assertTrue("id should be loaded", persistenceUnitUtil.isLoaded(profile, "id"));
//        Assert.assertFalse("email should not be loaded", persistenceUnitUtil.isLoaded(profile, "email"));
        boolean socialconnectionsLoaded = persistenceUnitUtil.isLoaded(profile, "socialConnections");
        System.out.println("SocialConnection loaded.. " + socialconnectionsLoaded);
        Assert.assertTrue("social connection should be loaded", socialconnectionsLoaded);

    }
}