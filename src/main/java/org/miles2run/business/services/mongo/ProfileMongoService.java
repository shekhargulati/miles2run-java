package org.miles2run.business.services.mongo;

import com.mongodb.*;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.domain.mongo.UserProfile;
import org.miles2run.business.utils.GeocoderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an API to work with profiles and cities MongoDB collection.
 */
@ApplicationScoped
public class ProfileMongoService {

    private static final int RECOMMENDED_FRIENDS_COUNT = 3;
    @Inject
    DB db;
    private Logger logger = LoggerFactory.getLogger(ProfileMongoService.class);
    private DBCollection profiles;
    private DBCollection cities;

    @PostConstruct
    public void postConstruct() {
        profiles = db.getCollection("profiles");
        cities = db.getCollection("cities");
    }


    public void save(Profile profile) {
        double[] existingCoordinatesForACity = findLngLatForACity(profile.getCity());
        double[] lngLat = (existingCoordinatesForACity.length == 0) ? fetchLngLatAndSave(profile.getCity(), profile.getCountry()) : existingCoordinatesForACity;
        profiles.save(new BasicDBObject().append("username", profile.getUsername()).append("lngLat", lngLat));
    }

    public double[] fetchLngLatAndSave(final String city, final String country) {
        double[] lngLat = GeocoderUtils.lngLat(city, country);
        cities.save(new BasicDBObject("city", city).append("country", country).append("lngLat", lngLat));
        return lngLat;
    }

    public double[] findLngLatForACity(String city) {
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
        return lngLat.length == 0 ? new double[0] : new double[]{lngLat[1], lngLat[0]};
    }


    public void createFriendship(final String follower, final String following) {
        updateFollowerFollowing(follower, following);
        updateFollowingFollowers(following, follower);
    }

    private void updateFollowingFollowers(final String following, final String follower) {
        profiles.update(new BasicDBObject("username", following), new BasicDBObject("$push", new BasicDBObject("followers", follower)));
    }

    private void updateFollowerFollowing(final String follower, final String following) {
        profiles.update(new BasicDBObject("username", follower), new BasicDBObject("$push", new BasicDBObject("following", following)));
    }

    public UserProfile findProfile(String username) {
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
        double[] lngLat = GeocoderUtils.lngLat(city, country);
        profiles.update(new BasicDBObject("username", username), new BasicDBObject("$set", new BasicDBObject("lngLat", lngLat)));
    }

    public boolean isUserFollowing(String currentLoggedInUser, String username) {
        BasicDBObject query = new BasicDBObject("username", currentLoggedInUser).append("following", username);
        logger.info("isUserFollowing MongoDB query {}", query.toString());
        DBObject exists = profiles.findOne(query);
        return exists != null;
    }

    public void destroyFriendship(String username, String userToUnFollow) {
        BasicDBObject findQuery = new BasicDBObject("username", username);
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.put("$pull", new BasicDBObject("following", userToUnFollow));
        profiles.update(findQuery, updateQuery);
        profiles.update(new BasicDBObject("username", userToUnFollow), new BasicDBObject("$pull", new BasicDBObject("followers", username)));
    }

    public List<String> findUsersByProximity(final String username) {
        BasicDBObject userRecommendationQuery = userRecommendationQuery(username);
        DBCursor cursor = profiles.find(userRecommendationQuery, new BasicDBObject("username", 1)).limit(RECOMMENDED_FRIENDS_COUNT);
        return toUsers(cursor);
    }

    private List<String> toUsers(DBCursor cursor) {
        List<String> userFriends = new ArrayList<>();
        while (cursor.hasNext()) {
            userFriends.add((String) cursor.next().get("username"));
        }
        return userFriends;
    }

    private Object getUserLngLat(final String username) {
        BasicDBObject userQuery = new BasicDBObject("username", username);
        BasicDBObject lngLatField = new BasicDBObject("lngLat", 1);
        return profiles.findOne(userQuery, lngLatField).get("lngLat");
    }

    public BasicDBObject userRecommendationQuery(final String username) {
        Object lngLat = getUserLngLat(username);
        BasicDBObject recommendationQuery = new BasicDBObject();
        recommendationQuery.put("username", new BasicDBObject("$ne", username));
        recommendationQuery.put("followers", new BasicDBObject("$nin", new String[]{username}));
        recommendationQuery.put("lngLat", new BasicDBObject("$near", lngLat));
        logger.debug("Recommending friends to {} using query {}", username, recommendationQuery);
        return recommendationQuery;
    }
}
