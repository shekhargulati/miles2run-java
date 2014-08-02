package org.miles2run.business.services;

import com.mongodb.*;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.domain.mongo.UserProfile;
import org.miles2run.business.utils.GeocoderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shekhargulati on 20/03/14.
 */
@ApplicationScoped
public class ProfileMongoService {

    private Logger logger = LoggerFactory.getLogger(ProfileMongoService.class);

    @Inject
    DB db;


    public void save(Profile profile) {
        DBCollection profiles = db.getCollection("profiles");
        double[] existingCoordinatesForACity = findLngLatForACity(profile.getCity());
        double[] lngLat = (existingCoordinatesForACity.length == 0) ? fetchLngLatAndSave(profile.getCity(), profile.getCountry()) : existingCoordinatesForACity;
        profiles.save(new BasicDBObject().append("username", profile.getUsername()).append("lngLat", lngLat));
    }

    public double[] fetchLngLatAndSave(final String city, final String country) {
        double[] lngLat = GeocoderUtils.lngLat(city, country);
        DBCollection cities = db.getCollection("cities");
        cities.save(new BasicDBObject("city", city).append("country", country).append("lngLat", lngLat));
        return lngLat;
    }

    public double[] findLngLatForACity(String city) {
        DBCollection cities = db.getCollection("cities");
        BasicDBObject query = new BasicDBObject("city", city);
        DBObject cityDBObject = cities.findOne(query);
        if (cityDBObject == null) {
            return new double[0];
        }
        BasicDBObject basicDBObject = (BasicDBObject) cityDBObject;
        BasicDBList basicDbList = (BasicDBList) basicDBObject.get("lngLat");
        return basicDbList == null || basicDbList.isEmpty() ? new double[0] : new double[]{(Double) basicDbList.get(0), (Double) basicDbList.get(1)};
    }

    public double[] findLatLngForACity(String city) {
        double[] lngLat = findLngLatForACity(city);
        double[] latLng = lngLat.length == 0 ? new double[0] : new double[]{lngLat[1], lngLat[0]};
        return latLng;
    }


    public void createFriendship(String username, String userToFollow) {
        DBCollection profiles = db.getCollection("profiles");
        BasicDBObject findQuery = new BasicDBObject("username", username);
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.put("$push", new BasicDBObject("following", userToFollow));
        profiles.update(findQuery, updateQuery);
        profiles.update(new BasicDBObject("username", userToFollow), new BasicDBObject("$push", new BasicDBObject("followers", username)));
    }

    public UserProfile findProfile(String username) {
        DBCollection profiles = db.getCollection("profiles");
        BasicDBObject findQuery = new BasicDBObject("username", username);
        DBObject dbObject = profiles.findOne(findQuery);
        if (dbObject == null) {
            return null;
        }
        UserProfile userProfile = new UserProfile();
        BasicDBObject basicDBObject = (BasicDBObject) dbObject;
        String profileUsername = basicDBObject.getString("username");
        BasicDBList followingDbList = (BasicDBList) basicDBObject.get("following");
        BasicDBList followersDbList = (BasicDBList) basicDBObject.get("followers");
        userProfile.setUsername(profileUsername);
        userProfile.getFollowers().addAll(toList(followersDbList));
        userProfile.getFollowing().addAll(toList(followingDbList));
        return userProfile;
    }

    private List<String> toList(BasicDBList basicDBList) {
        List<String> list = new ArrayList<>();
        if (basicDBList == null) {
            return list;
        }
        for (Object o : basicDBList) {
            list.add((String) o);
        }
        return list;
    }

    public void update(String username, String city, String country) {
        DBCollection profiles = db.getCollection("profiles");
        double[] lngLat = GeocoderUtils.lngLat(city, country);
        profiles.update(new BasicDBObject("username", username), new BasicDBObject("$set", new BasicDBObject("lngLat", lngLat)));
    }

    public boolean isUserFollowing(String currentLoggedInUser, String username) {
        DBCollection profiles = db.getCollection("profiles");
        BasicDBObject query = new BasicDBObject("username", currentLoggedInUser).append("following", username);
        logger.info("isUserFollowing MongoDB query {}", query.toString());
        DBObject exists = profiles.findOne(query);
        return exists != null ? true : false;
    }

    public void destroyFriendship(String username, String userToUnFollow) {
        DBCollection profiles = db.getCollection("profiles");
        BasicDBObject findQuery = new BasicDBObject("username", username);
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.put("$pull", new BasicDBObject("following", userToUnFollow));
        profiles.update(findQuery, updateQuery);
        profiles.update(new BasicDBObject("username", userToUnFollow), new BasicDBObject("$pull", new BasicDBObject("followers", username)));
    }
}
