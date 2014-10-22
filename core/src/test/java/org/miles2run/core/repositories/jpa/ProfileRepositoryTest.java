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
import org.miles2run.domain.entities.SocialProvider;

import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
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
        EntityGraph entityGraph = entityManager.createEntityGraph(Profile.class);
        entityGraph.addAttributeNodes("username");
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", entityGraph);
        Profile profile = entityManager.find(Profile.class, 1000L, hints);
        assertEntityGraph(profile);
    }

    private void assertEntityGraph(Profile profile) {
        PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        Assert.assertTrue("Username should be loaded", persistenceUnitUtil.isLoaded(profile, "username"));
        Assert.assertTrue("id should be loaded", persistenceUnitUtil.isLoaded(profile, "id"));
        Assert.assertFalse("social connection should not be loaded", persistenceUnitUtil.isLoaded(profile, "socialConnections"));
    }

    @Transactional(TransactionMode.DISABLED)
    @Test
    @UsingDataSet({"profile.yml", "social_connections.yml"})
    public void findByEmail_EntityGraph_NotLoadSocialConnections() throws Exception {
        Profile profile = profileRepository.findByEmail("test_user@test.com");
        assertEntityGraph(profile);
    }

    @Test
    @UsingDataSet({"profile.yml"})
    public void findByUsername_InvalidUsername_NoProfileFound() throws Exception {
        Profile profile = profileRepository.findByUsername("invalid_user");
        Assert.assertNull(profile);
    }

    @Test
    @UsingDataSet({"profile.yml"})
    public void findByEmail_InvalidEmail_NoProfileFound() throws Exception {
        Profile profile = profileRepository.findByEmail("invalid_user@email.com");
        Assert.assertNull(profile);
    }

    @Test
    @UsingDataSet({"profiles.yml"})
    @ShouldMatchDataSet({"profiles.yml"})
    public void findProfiles_ListOfUsernames_ReturnListOfProfiles() throws Exception {
        List<Profile> profiles = profileRepository.findProfiles(Arrays.asList("test_user_1", "test_user_2", "test_user_3"));
        Assert.assertThat(profiles, hasSize(3));
    }

    @Transactional(TransactionMode.DISABLED)
    @Test
    @UsingDataSet({"profiles.yml", "profiles_social_connections.yml"})
    public void findProfiles_ListOfUsername_ReturnListOfProfilesWithoutSocialConnections() throws Exception {
        List<Profile> profiles = profileRepository.findProfiles(Arrays.asList("test_user_1", "test_user_2", "test_user_3"));
        Assert.assertThat(profiles, hasSize(3));
        profiles.forEach(this::assertEntityGraph);
    }

    @Test
    @UsingDataSet({"profiles.yml"})
    public void findProfiles_2ValidNameAndOneInvalid_Return2Profiles() throws Exception {
        List<Profile> profiles = profileRepository.findProfiles(Arrays.asList("test_user_1", "test_user_2", "test_user_invalid"));
        Assert.assertThat(profiles, hasSize(2));
    }


    @Test
    public void findProfiles_UsernameNotExists_ReturnEmptyList() throws Exception {
        List<Profile> profiles = profileRepository.findProfiles(Arrays.asList("test_user_invalid_1", "test_user_invalid_2", "test_user_invalid_3"));
        Assert.assertThat(profiles, hasSize(0));
    }

    @Test
    @UsingDataSet({"profiles.yml"})
    public void findProfilesWithFullnameLike_ValidFullnameInitialsInLowercase_ReturnsValidProfiles() throws Exception {
        List<Profile> profiles = profileRepository.findProfilesWithFullnameLike("test");
        Assert.assertThat(profiles, hasSize(3));
    }

    @Test
    @UsingDataSet({"profiles.yml"})
    public void findProfilesWithFullnameLike_ValidFullnameInitialsInUppercase_ReturnsValidProfiles() throws Exception {
        List<Profile> profiles = profileRepository.findProfilesWithFullnameLike("TEST");
        Assert.assertThat(profiles, hasSize(3));
    }

    @Test
    @UsingDataSet({"profiles.yml"})
    public void findProfilesWithFullnameLike_ValidFullnameInitialsInMixedCase_ReturnsValidProfiles() throws Exception {
        List<Profile> profiles = profileRepository.findProfilesWithFullnameLike("tESt");
        Assert.assertThat(profiles, hasSize(3));
    }

    @Test
    public void findProfiles_FullnameInitialInvalid_ReturnEmptyList() throws Exception {
        List<Profile> profiles = profileRepository.findProfilesWithFullnameLike("invalid");
        Assert.assertThat(profiles, hasSize(0));
    }

    @Transactional(TransactionMode.DISABLED)
    @Test
    @UsingDataSet({"profile.yml", "social_connections.yml"})
    public void shouldFindProfileWithSocialConnections() throws Exception {
        Profile profile = profileRepository.findWithSocialConnections("test_user");
        Assert.assertThat(profile.getSocialConnections(), hasSize(2));
        for (SocialConnection socialConnection : profile.getSocialConnections()) {
            Assert.assertEquals(SocialProvider.TWITTER, socialConnection.getProvider());
        }
    }

    @Test
    @UsingDataSet({"profile.yml"})
    public void shouldUpdateAnExistingProfile() throws Exception {
        Profile profile = entityManager.find(Profile.class, 1000L);
        profile.setFullname("Updated User");
        profileRepository.update(profile);
    }
}