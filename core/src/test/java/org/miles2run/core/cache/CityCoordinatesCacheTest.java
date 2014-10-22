package org.miles2run.core.cache;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CityCoordinatesCacheTest {

    @Rule
    public FongoRule fongoRule = new FongoRule();
    private CityCoordinatesCache cache;

    @Before
    public void setUp() throws Exception {
        DB db = fongoRule.getDB("miles2run-test");
        DBCollection cities = db.getCollection("cities");
        cache = new CityCoordinatesCache();
        cache.cities = cities;
    }

    @Test
    public void findLngLat_CityNotInCache_FetchCoordinatesAndSaveInDb() throws Exception {
        double[] lngLat = cache.findLngLat("Gurgaon", "India");
        double[] expected = {77.0266383, 28.4594965};
        Assert.assertArrayEquals(expected, lngLat, 0.0001d);
        Assert.assertThat(cache.cities.count(), Is.is(IsEqual.equalTo(1L)));
    }

    @Test
    public void findLngLat_CityInCache_ReturnCityCoordinatesFromCache() throws Exception {
        // first call should cache the data in MongoDB
        cache.findLngLat("Gurgaon", "India");
        // this call should get data directly from MongoDB
        double[] lngLat = cache.findLngLat("Gurgaon", "India");
        double[] expected = {77.0266383, 28.4594965};
        Assert.assertArrayEquals(expected, lngLat, 0.0001d);
        // As data is only written once in MongoDB so number of documents in Mongo should be 1
        Assert.assertThat(cache.cities.count(), Is.is(IsEqual.equalTo(1L)));
    }

    @Test
    public void findLatLng_CityNotInCache_FetchLatLng() throws Exception {
        double[] latLng = cache.findLatLng("Gurgaon", "India");
        double[] expected = {28.4594965, 77.0266383};
        Assert.assertArrayEquals(expected, latLng, 0.0001d);
        Assert.assertThat(cache.cities.count(), Is.is(IsEqual.equalTo(1L)));
    }

    @Test
    public void findLatLng_InvalidCityCountryName_ReturnEmptyArray() throws Exception {
        double[] latLng = cache.findLatLng("test_city", "test_country");
        Assert.assertThat(latLng.length, IsEqual.equalTo(0));

    }
}