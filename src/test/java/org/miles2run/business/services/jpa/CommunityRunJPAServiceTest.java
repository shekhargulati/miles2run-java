package org.miles2run.business.services.jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.joda.time.DateTime;
import org.junit.*;
import org.junit.runner.RunWith;
import org.miles2run.business.domain.jpa.*;
import org.miles2run.business.producers.EntityManagerProducer;
import org.miles2run.business.vo.ProfileDetails;
import org.miles2run.business.vo.ProfileGroupDetails;
import org.miles2run.business.vo.ProfileSocialConnectionDetails;
import org.miles2run.jaxrs.forms.ProfileForm;
import org.miles2run.shared.exceptions.NoUserExistsException;
import org.miles2run.shared.repositories.ProfileRepository;

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

    @Inject
    private CommunityRunJPAService communityRunJPAService;
    @Inject
    private EntityManager entityManager;
    @Inject
    private UserTransaction userTransaction;
    @Inject
    private ProfileRepository profileRepository;

    @Deployment
    public static Archive<?> deployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class).
                addClass(CommunityRun.class).
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
                addClass(CommunityRunBuilder.class).
                addClass(CommunityRunJPAService.class).
                addClass(EntityManagerProducer.class).
                addClass(NoUserExistsException.class).
                addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("joda-time:joda-time", "org.jadira.usertype:usertype.core").withTransitivity().asFile()).
                addAsResource("META-INF/test_persistence.xml", "META-INF/persistence.xml").
                addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.printf("WebArchive %s", webArchive.toString(true));
        return webArchive;
    }

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
    @Ignore
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
        List<CommunityRun> allActiveRaces = communityRunJPAService.findAllActiveCommunityRuns();
        Assert.assertEquals(10, allActiveRaces.size());
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
        List<CommunityRun> communityRuns = communityRunJPAService.findAllActiveCommunityRunsWithNameLike("JavaOne");
        Assert.assertEquals(10, communityRuns.size());

    }

    @Test
    public void shouldReturn10CommunityRunsWhenNameCaseIsDifferent() throws Exception {
        createCommunityRuns(10);
        List<CommunityRun> communityRuns = communityRunJPAService.findAllActiveCommunityRunsWithNameLike("JaVa");
        Assert.assertEquals(10, communityRuns.size());

    }

    @Test
    public void shouldReturn0CommunityRunsWhenNoNameMatches() throws Exception {
        createCommunityRuns(10);
        List<CommunityRun> communityRuns = communityRunJPAService.findAllActiveCommunityRunsWithNameLike("xxx");
        Assert.assertEquals(0, communityRuns.size());
    }

    @Test
    public void shouldThrowExceptionWhenFindingWithCommunityRaceNameNull() throws Exception {
        createCommunityRuns(1);
        try {
            communityRunJPAService.findAllActiveCommunityRunsWithNameLike(null);
            Assert.fail("Should fail when name is null");
        } catch (Exception e) {
            Assert.assertEquals("javax.validation.ConstraintViolationException", e.getCause().getClass().getName());
        }
    }

    @Test
    public void shouldRespectContraintsOnFindAllActiveCommunityRuns() throws Exception {
        createCommunityRuns(1);
        try {
            communityRunJPAService.findAllActiveCommunityRuns(100, 0);
        } catch (Exception e) {
            Assert.assertEquals("javax.validation.ConstraintViolationException", e.getCause().getClass().getName());
        }
    }

    @Test
    public void shouldRespectContraintsOnFindAllActiveCommunityRunsWithNameLike() throws Exception {
        createCommunityRuns(1);
        try {
            communityRunJPAService.findAllActiveCommunityRunsWithNameLike("javaone", 100, 0);
        } catch (Exception e) {
            Assert.assertEquals("javax.validation.ConstraintViolationException", e.getCause().getClass().getName());
        }
    }

    @Test
    public void testFindAllActiveCommunityRuns() throws Exception {
        int resultSet = 10;
        int max = 2;
        int expectedPages = 5;
        int actualPages = 0;
        createCommunityRuns(10);
        for (int page = 1; page <= expectedPages; page++) {
            List<CommunityRun> communityRuns = communityRunJPAService.findAllActiveCommunityRuns(max, page);
            Assert.assertEquals(2, communityRuns.size());
            actualPages++;
        }
        Assert.assertEquals(expectedPages, actualPages);
    }

    @Test
    public void testFindAllActiveCommunityRunsByNameLike() throws Exception {
        int resultSet = 10;
        int max = 2;
        int expectedPages = 5;
        int actualPages = 0;
        createCommunityRuns(10);
        for (int page = 1; page <= expectedPages; page++) {
            List<CommunityRun> communityRuns = communityRunJPAService.findAllActiveCommunityRunsWithNameLike("javaone", max, page);
            Assert.assertEquals(2, communityRuns.size());
            actualPages++;
        }
        Assert.assertEquals(expectedPages, actualPages);
    }
}
