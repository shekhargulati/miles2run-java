package org.miles2run.business.recommender;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import org.miles2run.business.services.mongo.ProfileMongoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Recommend friends to a user based on user location. </p>
 */
@ApplicationScoped
public class LocationBasedFriendRecommender implements FriendRecommender {

    private static final int RECOMMENDED_FRIENDS_COUNT = 3;

    private final Logger logger = LoggerFactory.getLogger(LocationBasedFriendRecommender.class);

    @Inject
    private ProfileMongoService profileMongoService;

    /**
     * <p>This method recommend users that are closest to the user location.
     * User location is retrieved from the User profile data. The closest user would be the first user in the list.</p>
     *
     * @param username, user for which recommendations are to be computed.
     * @return {@link java.util.List} of recommended users(i.e. username) ordered by their distance from the user.
     */
    @Override
    public List<String> recommend(@NotNull final String username) {
        BasicDBObject userRecommendationQuery = userRecommendationQuery(username);
        logger.debug("Recommending friends to {} using query {}", username, userRecommendationQuery);
        DBCursor usersCursor = profileMongoService.findUsersByProximity(userRecommendationQuery, RECOMMENDED_FRIENDS_COUNT);
        return toUsers(usersCursor);
    }

    private BasicDBObject userRecommendationQuery(final String username) {
        Object lngLat = profileMongoService.getUserLngLat(username);
        BasicDBObject recommendationQuery = new BasicDBObject();
        recommendationQuery.put("username", new BasicDBObject("$ne", username));
        recommendationQuery.put("followers", new BasicDBObject("$nin", new String[]{username}));
        recommendationQuery.put("lngLat", new BasicDBObject("$near", lngLat));
        return recommendationQuery;
    }

    private List<String> toUsers(final DBCursor cursor) {
        List<String> userFriends = new ArrayList<>();
        while (cursor.hasNext()) {
            userFriends.add((String) cursor.next().get("username"));
        }
        return userFriends;
    }

}
