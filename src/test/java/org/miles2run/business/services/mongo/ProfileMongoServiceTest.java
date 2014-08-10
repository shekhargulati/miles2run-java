package org.miles2run.business.services.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.miles2run.business.services.mongo.ProfileMongoService;

/**
 * Created by shekhargulati on 02/08/14.
 */
public class ProfileMongoServiceTest {


    private DB db;

    @Before
    public void setUp() throws Exception {
        db = new MongoClient("localhost", 27017).getDB("miles2run_test");
        db.getCollection("cities").drop();
    }


    @After
    public void tearDown() throws Exception {
        db.getCollection("cities").drop();
    }

    @Test
    public void shouldFindLngLatCoordinatesForACity() throws Exception {
        populateCities(db);
        ProfileMongoService profileMongoService = new ProfileMongoService();
        profileMongoService.db = db;
        double[] lngLat = profileMongoService.findLngLatForACity("test1");
        Assert.assertNotNull(lngLat);
        Assert.assertArrayEquals(new double[]{1.0d, 2.0d}, lngLat, 0.001d);
    }

    private void populateCities(DB db) {
        DBCollection cities = db.getCollection("cities");
        for (int i = 0; i < 20; i++) {
            cities.save(new BasicDBObject("city", "test" + i).append("lngLat", new double[]{i, i + 1}));
        }
    }
}
