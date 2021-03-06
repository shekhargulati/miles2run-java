package org.miles2run.core.repositories.mongo;

import com.mongodb.*;
import org.miles2run.core.cache.CityCoordinatesCache;
import org.miles2run.core.utils.GeocoderUtils;
import org.miles2run.domain.documents.UserProfile;
import org.miles2run.domain.entities.Profile;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an API to work with profiles MongoDB collection.
 */
@ApplicationScoped
public class UserProfileRepository {

    private static final String PROFILES_COLLECTION = "profiles";

    @Inject
    private CityCoordinatesCache cityCache;
    @Inject
    private DB db;

    private DBCollection profiles;

    @PostConstruct
    public void postConstruct() {
        profiles = db.getCollection(PROFILES_COLLECTION);
    }

    public void save(Profile profile) {
        double[] lngLat = cityCache.findLngLat(profile.getCity(), profile.getCountry());
        profiles.save(new BasicDBObject().append("username", profile.getUsername()).append("lngLat", lngLat));
    }

    public UserProfile find(String username) {
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
        list.addAll(basicDBList.stream().map(o -> (String) o).collect(Collectors.toList()));
        return list;
    }

    public void update(String username, String city, String country) {
        double[] lngLat = GeocoderUtils.lngLat(city, country);
        profiles.update(new BasicDBObject("username", username), new BasicDBObject("$set", new BasicDBObject("lngLat", lngLat)));
    }

    public Object getUserLngLat(final String username) {
        BasicDBObject userQuery = new BasicDBObject("username", username);
        BasicDBObject lngLatField = new BasicDBObject("lngLat", 1);
        return profiles.findOne(userQuery, lngLatField).get("lngLat");
    }

    public DBCursor findUsersByProximity(DBObject userRecommendationQuery, int limit) {
        return profiles.find(userRecommendationQuery, new BasicDBObject("username", 1)).limit(limit);

    }

}
