package org.miles2run.core.repositories.jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.miles2run.core.exceptions.NoRecordExistsException;
import org.miles2run.core.producers.EntityManagerProducer;
import org.miles2run.core.test_helpers.TestHelpers;
import org.miles2run.domain.entities.CommunityRun;
import org.miles2run.domain.entities.CommunityRunBuilder;
import org.miles2run.domain.entities.Duration;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.miles2run.core.test_helpers.TestHelpers.domainDeployment;

@RunWith(Arquillian.class)
public class CommunityRunRepositoryTest {

    @Inject
    private CommunityRunRepository communityRunRepository;
    @Inject
    private EntityManager entityManager;
    @Inject
    private UserTransaction userTransaction;

    @Deployment
    public static Archive<?> deployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class).addAsLibraries(domainDeployment())
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("joda-time:joda-time", "org.jadira.usertype:usertype.core").withTransitivity().asFile())
                .addClasses(CommunityRunRepository.class, TestHelpers.class, EntityManagerProducer.class, NoRecordExistsException.class)
                .addAsResource("META-INF/test_persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(webArchive.toString(true));
        return webArchive;
    }

    @Before
    public void setUp() throws Exception {
        userTransaction.begin();
        entityManager.createQuery("DELETE from CommunityRun cr").executeUpdate();
        userTransaction.commit();
    }


    @Test
    public void communityRunRepositoryShouldBeNotNull() throws Exception {
        assertNotNull(communityRunRepository);
    }

    @Test
    public void shouldSaveCommunityRunWhenDateIsCorrect() throws Exception {
        CommunityRun communityRun = createCommunityRun("JavaOne 2014", "javaone-2014");
        Long communityRunId = communityRunRepository.save(communityRun);
        assertNotNull(communityRunId);
    }

    private CommunityRun createCommunityRun(String name, String slug) {
        return new CommunityRunBuilder().
                setName(name).
                setBannerImg("http://example.com/javaone.png").
                setDescription("biggest Java conference").
                setSlug(slug).
                setDuration(new Duration(new Date(), new DateTime().plusDays(5).toDate())).
                setTwitterHandle("javaoneconf").
                setWebsite("https://www.oracle.com/javaone/index.html").
                createCommunityRun();
    }

    @Test(expected = Exception.class)
    public void shouldNotSaveACommunityRunWithSameName() throws Exception {
        CommunityRun communityRun = createCommunityRun("JavaOne", "javaone");
        communityRunRepository.save(communityRun);

        CommunityRun anotherCommunityRunWithSameName = createCommunityRun("JavaOne", "javaone-1");
        communityRunRepository.save(anotherCommunityRunWithSameName);
    }

    @Test(expected = Exception.class)
    public void shouldNotSaveACommunityRunWithSameSlug() throws Exception {
        CommunityRun communityRun = createCommunityRun("JavaOne 1", "javaone");
        communityRunRepository.save(communityRun);

        CommunityRun anotherCommunityRunWithSameName = createCommunityRun("JavaOne 2", "javaone");
        communityRunRepository.save(anotherCommunityRunWithSameName);
    }

    @Test(expected = Exception.class)
    public void shouldGiveErrorWhenStartDateIsGreaterThanEndDate() {
        CommunityRun communityRun = new CommunityRunBuilder().
                setName("JavaOne 2014").
                setBannerImg("http://example.com/javaone.png").
                setDescription("biggest Java conference").
                setSlug("javaone-2014").
                setDuration(new Duration(new Date(), new DateTime().minusDays(5).toDate())).
                setTwitterHandle("javaoneconf").
                setWebsite("https://www.oracle.com/javaone/index.html").
                createCommunityRun();
        communityRunRepository.save(communityRun);
    }

    @Test
    public void shouldFindAllActiveRuns() throws Exception {
        createCommunityRuns(10);
        List<CommunityRun> allActiveRaces = communityRunRepository.findAllActiveCommunityRuns();
        Assert.assertEquals(10, allActiveRaces.size());
    }

    private void createCommunityRuns(int n) {
        for (int i = 0; i < n; i++) {
            CommunityRun communityRun = new CommunityRunBuilder().
                    setName("JavaOne 201" + i).
                    setBannerImg("http://example.com/javaone.png").
                    setDescription("biggest Java conference").
                    setSlug("javaone-201" + i).
                    setDuration(new Duration(new Date(), new DateTime().plusDays(5).toDate())).
                    setTwitterHandle("javaoneconf").
                    setWebsite("https://www.oracle.com/javaone/index.html").
                    createCommunityRun();

            communityRunRepository.save(communityRun);
        }
    }

    @Test
    public void shouldFindBySlug() throws Exception {
        createCommunityRuns(10);
        CommunityRun communityRun = communityRunRepository.findBySlug("javaone-2014");
        assertNotNull(communityRun);
    }

    @Test
    public void shouldReturnNullWhenCommunityRunDoesNotExistForSlug() throws Exception {
        createCommunityRuns(10);
        CommunityRun communityRun = communityRunRepository.findBySlug("xxx");
        Assert.assertNull(communityRun);
    }

    @Test
    public void shouldReturn10CommunityRunsWithNameJavaOne() throws Exception {
        createCommunityRuns(10);
        List<CommunityRun> communityRuns = communityRunRepository.findAllActiveCommunityRunsWithNameLike("JavaOne");
        Assert.assertEquals(10, communityRuns.size());

    }

    @Test
    public void shouldReturn10CommunityRunsWhenNameCaseIsDifferent() throws Exception {
        createCommunityRuns(10);
        List<CommunityRun> communityRuns = communityRunRepository.findAllActiveCommunityRunsWithNameLike("JaVa");
        Assert.assertEquals(10, communityRuns.size());

    }

    @Test
    public void shouldReturn0CommunityRunsWhenNoNameMatches() throws Exception {
        createCommunityRuns(10);
        List<CommunityRun> communityRuns = communityRunRepository.findAllActiveCommunityRunsWithNameLike("xxx");
        Assert.assertEquals(0, communityRuns.size());
    }

    @Test
    public void shouldThrowExceptionWhenFindingWithCommunityRaceNameNull() throws Exception {
        createCommunityRuns(1);
        try {
            communityRunRepository.findAllActiveCommunityRunsWithNameLike(null);
            Assert.fail("Should fail when name is null");
        } catch (Exception e) {
            Assert.assertEquals("javax.validation.ConstraintViolationException", e.getCause().getClass().getName());
        }
    }

    @Test
    public void shouldRespectContraintsOnFindAllActiveCommunityRuns() throws Exception {
        createCommunityRuns(1);
        try {
            communityRunRepository.findAllActiveCommunityRuns(100, 0);
        } catch (Exception e) {
            Assert.assertEquals("javax.validation.ConstraintViolationException", e.getCause().getClass().getName());
        }
    }

    @Test
    public void shouldRespectContraintsOnFindAllActiveCommunityRunsWithNameLike() throws Exception {
        createCommunityRuns(1);
        try {
            communityRunRepository.findAllActiveCommunityRunsWithNameLike("javaone", 100, 0);
        } catch (Exception e) {
            Assert.assertEquals("javax.validation.ConstraintViolationException", e.getCause().getClass().getName());
        }
    }

    @Test
    public void testFindAllActiveCommunityRuns() throws Exception {
        int max = 2;
        int expectedPages = 5;
        int actualPages = 0;
        createCommunityRuns(10);
        for (int page = 1; page <= expectedPages; page++) {
            List<CommunityRun> communityRuns = communityRunRepository.findAllActiveCommunityRuns(page, max);
            Assert.assertEquals(2, communityRuns.size());
            actualPages++;
        }
        Assert.assertEquals(expectedPages, actualPages);
    }

    @Test
    public void testFindAllActiveCommunityRunsByNameLike() throws Exception {
        int max = 2;
        int expectedPages = 5;
        int actualPages = 0;
        createCommunityRuns(10);
        for (int page = 1; page <= expectedPages; page++) {
            List<CommunityRun> communityRuns = communityRunRepository.findAllActiveCommunityRunsWithNameLike("javaone", page, max);
            Assert.assertEquals(2, communityRuns.size());
            actualPages++;
        }
        Assert.assertEquals(expectedPages, actualPages);
    }
}