package org.miles2run.business.services.mongo;

import com.mongodb.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 20/03/14.
 */
@ApplicationScoped
public class LocationBasedFriendRecommender implements FriendRecommender {

    @Inject
    DB db;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public List<String> recommend(String username) {
        DBCollection profiles = db.getCollection("profiles");
        DBObject user = profiles.findOne(new BasicDBObject("username", username));
        BasicDBObject nearQuery = new BasicDBObject();
        nearQuery.put("username", new BasicDBObject("$ne", username));
        nearQuery.put("followers", new BasicDBObject("$nin", toArray(user.get("username"))));
        nearQuery.put("lngLat", new BasicDBObject("$near", user.get("lngLat")));
        logger.info(String.format("Near Query %s", nearQuery.toString()));
        DBCursor cursor = profiles.find(nearQuery, new BasicDBObject("username", 1)).limit(3);
        List<String> userFriends = new ArrayList<>();
        while (cursor.hasNext()) {
            userFriends.add((String) cursor.next().get("username"));
        }
        return userFriends;
    }

    private String[] toArray(Object obj) {
        if (obj == null) {
            return new String[0];
        }
        return new String[]{(String) obj};
    }
}
