package org.miles2run.core.repositories.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class FriendshipRepository {

    public static final String PROFILES_COLLECTION = "profiles";
    private final Logger logger = LoggerFactory.getLogger(FriendshipRepository.class);

    @Inject
    private DB db;

    private DBCollection profiles;

    @PostConstruct
    public void postConstruct() {
        profiles = db.getCollection(PROFILES_COLLECTION);
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

}
