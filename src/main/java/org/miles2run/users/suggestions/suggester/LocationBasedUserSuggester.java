package org.miles2run.users.suggestions.suggester;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import org.miles2run.shared.repositories.UserProfileRepository;
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
public class LocationBasedUserSuggester implements UserSuggester {

    private static final int SUGGESTIONS_COUNT = 3;

    private final Logger logger = LoggerFactory.getLogger(LocationBasedUserSuggester.class);

    @Inject
    private UserProfileRepository userProfileRepository;

    /**
     * <p>This method suggests users that are closest to the user location.
     * User location is retrieved from the User profile data. The closest user would be the first user in the list.</p>
     *
     * @param username, user for which recommendations are to be computed.
     * @return {@link java.util.List} of recommended users(i.e. username) ordered by their distance from the user.
     */
    @Override
    public List<String> suggestions(@NotNull final String username) {
        BasicDBObject suggestionQuery = userSuggestionsQuery(username);
        logger.debug("Suggesting friends to {} using query {}", username, suggestionQuery);
        DBCursor usersCursor = userProfileRepository.findUsersByProximity(suggestionQuery, SUGGESTIONS_COUNT);
        return toUsers(usersCursor);
    }

    private BasicDBObject userSuggestionsQuery(final String username) {
        Object lngLat = userProfileRepository.getUserLngLat(username);
        BasicDBObject suggestionQuery = new BasicDBObject();
        suggestionQuery.put("username", new BasicDBObject("$ne", username));
        suggestionQuery.put("followers", new BasicDBObject("$nin", new String[]{username}));
        suggestionQuery.put("lngLat", new BasicDBObject("$near", lngLat));
        return suggestionQuery;
    }

    private List<String> toUsers(final DBCursor cursor) {
        List<String> userFriends = new ArrayList<>();
        while (cursor.hasNext()) {
            userFriends.add((String) cursor.next().get("username"));
        }
        return userFriends;
    }

}
