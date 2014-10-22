package org.miles2run.core.producers;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static org.miles2run.core.test_helpers.TestHelpers.persistenceDescriptor;

@RunWith(Arquillian.class)
public class EntityManagerProducerTest {

    @Inject
    private EntityManager entityManager;

    @Deployment
    public static Archive<?> deployment() {
        String persistenceXml = persistenceDescriptor().exportAsString();
        System.out.println(persistenceXml);
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ShrinkWrap.create(JavaArchive.class).addClass(EntityManagerProducer.class)
                        .addAsManifestResource(new StringAsset(persistenceXml), "persistence.xml").addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(webArchive.toString(true));
        return webArchive;
    }


    @Test
    public void entityManagerShouldNotBeNull() throws Exception {
        Assert.assertNotNull(entityManager);

    }
}