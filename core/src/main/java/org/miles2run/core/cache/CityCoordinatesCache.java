package org.miles2run.core.cache;

import com.mongodb.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.miles2run.core.utils.GeocoderUtils.lngLat;

@ApplicationScoped
public class CityCoordinatesCache {

    public static final String CITIES_COLLECTION = "cities";

    DBCollection cities;

    @Inject
    private DB db;

    @PostConstruct
    public void postConstruct() {
        cities = db.getCollection(CITIES_COLLECTION);
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

    private DBObject findCity(String city, String country) {
        BasicDBObject query = new BasicDBObject("city", city).append("country", country);
        return cities.findOne(query);
    }

    private double[] fetchLngLat(final String city, final String country) {
        return lngLat(city, country);
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
}
