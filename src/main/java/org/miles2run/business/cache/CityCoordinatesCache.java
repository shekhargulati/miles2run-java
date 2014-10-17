package org.miles2run.business.cache;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.miles2run.business.utils.GeocoderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CityCoordinatesCache {

    private final Logger logger = LoggerFactory.getLogger(CityCoordinatesCache.class);

    DBCollection cities;

    @Inject
    private DB db;

    @PostConstruct
    public void postConstruct() {
        cities = db.getCollection("cities");
    }

    public double[] findLatLng(final String city, final String country) {
        double[] lngLat = findLngLat(city, country);
        return lngLat.length == 0 ? new double[0] : new double[]{lngLat[1], lngLat[0]};
    }

    public double[] findLngLat(final String city, final String country) {
        BasicDBObject query = new BasicDBObject("city", city).append("country", country);
        DBObject cityDBObject = cities.findOne(query);
        if (cityDBObject == null) {
            double[] lngLat = fetchLngLat(city, country);
            cities.save(new BasicDBObject("city", city).append("country", country).append("lngLat", lngLat));
            return lngLat;
        }
        BasicDBObject cityDoc = (BasicDBObject) cityDBObject;
        double[] lngLat = (double[]) cityDoc.get("lngLat");
        return lngLat.length == 0 ? new double[0] : lngLat;
    }

    private double[] fetchLngLat(final String city, final String country) {
        return GeocoderUtils.lngLat(city, country);
    }
}
