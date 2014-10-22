package org.miles2run.core.repositories.jpa.dummy;

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

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static org.miles2run.core.test_helpers.TestHelpers.domainDeployment;

@RunWith(Arquillian.class)
public class BookRepositoryTest {

    @Inject
    private EntityManager entityManager;

    @Deployment
    public static Archive<?> deployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(domainDeployment())
                .addClasses(TestHelpers.class, EntityManagerProducer.class, Book.class, BaseBookEntity.class)
                .addAsResource("META-INF/test_persistence_book.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(webArchive.toString(true));
        return webArchive;
    }

    @Transactional(TransactionMode.COMMIT)
    @Test
    public void shouldSave() throws Exception {
        Book book = new Book("OpenShift Cookbook", "Shekhar Gulati");
        Book merged = save(book);
        Assert.assertNotNull(merged.getId());
    }

    private Book save(Book book) {
        Book merged = entityManager.merge(book);
        entityManager.persist(merged);
        return merged;
    }

    @Test
    @UsingDataSet({"book.yml"})
    @ShouldMatchDataSet({"book_updated.yml"})
    public void shouldUpdate() throws Exception {
        Book book = entityManager.find(Book.class, 1000L);
        book.setAuthor("Test User");
    }
}
