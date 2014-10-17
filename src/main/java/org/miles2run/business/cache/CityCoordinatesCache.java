package org.miles2run.business.cache;

import com.mongodb.*;
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
        DBObject cityDBObject = findCity(city, country);
        if (cityDBObject == null) {
            double[] lngLat = fetchLngLat(city, country);
            if (lngLat.length == 0) {
                return lngLat;
            }
            cities.save(new BasicDBObject("city", city).append("country", country).append("lngLat", lngLat));
            return lngLat;
        }
        BasicDBObject cityDoc = (BasicDBObject) cityDBObject;
        return getLngLatFromDoc(cityDoc);
    }

    private double[] getLngLatFromDoc(BasicDBObject cityDoc) {
        Object lngLatObj = cityDoc.get("lngLat");
        if (lngLatObj instanceof BasicDBList) {
            BasicDBList lngLat = (BasicDBList) lngLatObj;
            return lngLat.isEmpty() ? new double[0] : new double[]{(Double) lngLat.get(0), (Double) lngLat.get(1)};
        } else {
            double[] lngLat = (double[]) lngLatObj;
            return lngLat.length == 0 ? new double[0] : lngLat;
        }
    }

    private DBObject findCity(String city, String country) {
        BasicDBObject query = new BasicDBObject("city", city).append("country", country);
        return cities.findOne(query);
    }

    private double[] fetchLngLat(final String city, final String country) {
        return GeocoderUtils.lngLat(city, country);
    }
}
