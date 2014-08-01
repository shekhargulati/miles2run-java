package org.miles2runtest.business.services.jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.miles2run.business.domain.jpa.BaseEntity;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.CommunityRunBuilder;
import org.miles2run.business.producers.EntityManagerProducer;
import org.miles2run.business.services.jpa.CommunityRunJPAService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.List;

/**
 * Created by shekhargulati on 01/08/14.
 */
@RunWith(Arquillian.class)
public class CommunityRunJPAServiceTest {

    @Deployment
    public static Archive<?> deployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class).
                addClass(CommunityRun.class).
                addClass(BaseEntity.class).
                addClass(CommunityRunBuilder.class).
                addClass(CommunityRunJPAService.class).
                addClass(EntityManagerProducer.class).
                addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("joda-time:joda-time").withoutTransitivity().asFile()).
                addAsResource("META-INF/test_persistence.xml", "META-INF/persistence.xml").
                addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.printf("WebArchive %s", webArchive.toString(true));
        return webArchive;
    }

    @Inject
    private CommunityRunJPAService communityRunJPAService;

    @Inject
    private EntityManager entityManager;
    @Inject
    private UserTransaction userTransaction;

    @Before
    public void setUp() throws Exception {
        userTransaction.begin();
        entityManager.createQuery("DELETE from CommunityRun cr").executeUpdate();
        userTransaction.commit();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldSaveCommunityRunWhenDateIsCorrect() throws Exception {
        CommunityRun communityRun = createCommunityRun("JavaOne 2014", "javaone-2014");
        Long communityRunId = communityRunJPAService.save(communityRun);
        Assert.assertNotNull(communityRunId);
    }

    @Test(expected = Exception.class)
    public void shouldNotSaveACommunityRunWithSameName() throws Exception {
        CommunityRun communityRun = createCommunityRun("JavaOne", "javaone");
        communityRunJPAService.save(communityRun);

        CommunityRun anotherCommunityRunWithSameName = createCommunityRun("JavaOne", "javaone-1");
        communityRunJPAService.save(anotherCommunityRunWithSameName);
    }


    @Test(expected = Exception.class)
    public void shouldNotSaveACommunityRunWithSameSlug() throws Exception {
        CommunityRun communityRun = createCommunityRun("JavaOne 1", "javaone");
        communityRunJPAService.save(communityRun);

        CommunityRun anotherCommunityRunWithSameName = createCommunityRun("JavaOne 2", "javaone");
        communityRunJPAService.save(anotherCommunityRunWithSameName);
    }

    @Test(expected = AssertionError.class)
    public void shouldGiveErrorWhenStartDateIsGreaterThanEndDate() {
        CommunityRun communityRun = new CommunityRunBuilder().
                setName("JavaOne 2014").
                setBannerImg("http://example.com/javaone.png").
                setDescription("biggest Java conference").
                setSlug("javaone-2014").
                setStartDate(new Date()).
                setEndDate(new DateTime().minusDays(5).toDate()).
                setTwitterHandle("javaoneconf").
                setWebsite("https://www.oracle.com/javaone/index.html").
                createCommunityRun();
        communityRunJPAService.save(communityRun);
    }


    @Test
    public void shouldFindAllActiveRuns() throws Exception {
        createCommunityRuns(10);
        List<CommunityRun> allActiveRaces = communityRunJPAService.findAllActiveRaces();
        Assert.assertEquals(10, allActiveRaces.size());
    }


    @Test
    public void shouldFindBySlug() throws Exception {
        createCommunityRuns(10);
        CommunityRun communityRun = communityRunJPAService.findBySlug("javaone-2014");
        Assert.assertNotNull(communityRun);
    }


    @Test
    public void shouldReturnNullWhenCommunityRunDoesNotExistForSlug() throws Exception {
        createCommunityRuns(10);
        CommunityRun communityRun = communityRunJPAService.findBySlug("xxx");
        Assert.assertNull(communityRun);
    }

    @Test
    public void shouldReturn10CommunityRunsWithNameJavaOne() throws Exception {
        createCommunityRuns(10);
        List<CommunityRun> communityRuns = communityRunJPAService.findAllActiveRacesWithNameLike("JavaOne");
        Assert.assertEquals(10, communityRuns.size());

    }


    @Test
    public void shouldReturn10CommunityRunsWhenNameCaseIsDifferent() throws Exception {
        createCommunityRuns(10);
        List<CommunityRun> communityRuns = communityRunJPAService.findAllActiveRacesWithNameLike("JaVa");
        Assert.assertEquals(10, communityRuns.size());

    }

    @Test
    public void shouldReturn0CommunityRunsWhenNoNameMatches() throws Exception {
        createCommunityRuns(10);
        List<CommunityRun> communityRuns = communityRunJPAService.findAllActiveRacesWithNameLike("xxx");
        Assert.assertEquals(0, communityRuns.size());
    }

    @Test
    public void shouldThrowExceptionWhenFindingWithCommunityRaceNameNull() throws Exception {
        createCommunityRuns(1);
        try {
            communityRunJPAService.findAllActiveRacesWithNameLike(null);
            Assert.fail("Should fail when name is null");
        } catch (Exception e) {
            Assert.assertEquals("javax.validation.ConstraintViolationException", e.getCause().getClass().getName());
        }

    }

    private void createCommunityRuns(int n) {
        for (int i = 0; i < n; i++) {
            CommunityRun communityRun = new CommunityRunBuilder().
                    setName("JavaOne 201" + i).
                    setBannerImg("http://example.com/javaone.png").
                    setDescription("biggest Java conference").
                    setSlug("javaone-201" + i).
                    setStartDate(new Date()).
                    setEndDate(new DateTime().plusDays(5).toDate()).
                    setTwitterHandle("javaoneconf").
                    setWebsite("https://www.oracle.com/javaone/index.html").
                    createCommunityRun();

            communityRunJPAService.save(communityRun);
        }
    }


    private CommunityRun createCommunityRun(String name, String slug) {
        return new CommunityRunBuilder().
                setName(name).
                setBannerImg("http://example.com/javaone.png").
                setDescription("biggest Java conference").
                setSlug(slug).
                setStartDate(new Date()).
                setEndDate(new DateTime().plusDays(5).toDate()).
                setTwitterHandle("javaoneconf").
                setWebsite("https://www.oracle.com/javaone/index.html").
                createCommunityRun();
    }
}
