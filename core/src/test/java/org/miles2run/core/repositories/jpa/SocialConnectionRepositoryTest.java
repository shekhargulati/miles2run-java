package org.miles2run.core.repositories.jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.miles2run.core.producers.EntityManagerProducer;
import org.miles2run.core.test_helpers.TestHelpers;
import org.miles2run.domain.entities.SocialConnection;

import javax.inject.Inject;

import static org.miles2run.core.test_helpers.TestHelpers.createSocialConnection;
import static org.miles2run.core.test_helpers.TestHelpers.domainDeployment;

@RunWith(Arquillian.class)
public class SocialConnectionRepositoryTest {

    @Inject
    private SocialConnectionRepository socialConnectionRepository;

    @Deployment
    public static Archive<?> deployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class).
                addAsLibraries(domainDeployment())
                .addClasses(SocialConnectionRepository.class, EntityManagerProducer.class, TestHelpers.class)
                .addAsResource("META-INF/test_persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(webArchive.toString(true));
        return webArchive;
    }


    @Test
    public void socialConnectionRepositoryDependencyShouldBeInjected() throws Exception {
        Assert.assertNotNull(socialConnectionRepository);
    }

    @Test
    @ShouldMatchDataSet(value = {"social_connection.yml"}, excludeColumns = {"*id"})
    public void shouldBeAbleToCreateSocialConnection() throws Exception {
        SocialConnection socialConnection = createSocialConnection();
        SocialConnection saved = socialConnectionRepository.save(socialConnection);
        Assert.assertNotNull(saved.getId());
    }

    @Test(expected = Exception.class)
    public void save_DuplicateConnectionId_ThrowsException() throws Exception {
        SocialConnection socialConnection = createSocialConnection();
        socialConnectionRepository.save(socialConnection);
        socialConnectionRepository.save(socialConnection);
    }

    @Test
    @ShouldMatchDataSet(value = {"social_connection_updated.yml"}, excludeColumns = {"*id"})
    public void update_AccessToken_SocialConnectionUpdated() throws Exception {
        SocialConnection socialConnection = createSocialConnection();
        SocialConnection saved = socialConnectionRepository.save(socialConnection);
        saved.setAccessToken("updated_access_token");
        saved.setAccessSecret("updated_access_secret");
        socialConnectionRepository.save(saved);
    }

    @Test
    @UsingDataSet({"social_connection.yml"})
    @ShouldMatchDataSet({"social_connection.yml"})
    public void findByConnectionId_ValidConnectionId_SocialConnectionFound() throws Exception {
        SocialConnection connection = socialConnectionRepository.findByConnectionId("test_connection");
        Assert.assertNotNull(connection);
    }

    @Test
    public void findByConnectionId_ConnectionIdInvalid_ReturnsNull() throws Exception {
        SocialConnection connection = socialConnectionRepository.findByConnectionId("test_connection");
        Assert.assertNull(connection);
    }
}