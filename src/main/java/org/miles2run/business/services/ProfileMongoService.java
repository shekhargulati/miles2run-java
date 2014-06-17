package org.miles2run.business.services;

import com.mongodb.*;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.domain.UserProfile;

import javax.enterprise.context.ApplicationScoped;

import org.miles2run.business.utils.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 20/03/14.
 */
@ApplicationScoped
public class ProfileMongoService {

    @Inject
    private DB db;

    @Inject
    private Logger logger;

    public void save(Profile profile) {
        DBCollection profiles = db.getCollection("profiles");
        double[] lngLat = GeocoderUtils.lngLat(profile.getCity(), profile.getCountry());
        profiles.save(new BasicDBObject().append("username", profile.getUsername()).append("lngLat", lngLat));
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
        logger.info("isUserFollowing() " + query.toString());
        DBObject exists = profiles.findOne(query);
        System.out.println("isUserFollowing() " + exists);
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
