package org.miles2run.business.recommender;

import org.miles2run.business.services.mongo.ProfileMongoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>Recommend friends to a user based on user location. </p>
 */
@ApplicationScoped
public class LocationBasedFriendRecommender implements FriendRecommender {

    private Logger logger = LoggerFactory.getLogger(LocationBasedFriendRecommender.class);

    @Inject
    ProfileMongoService profileMongoService;

    /**
     * <p>This method recommend users that are closest to the user location.
     * User location is retrieved from the User profile data. The closest user would be the first user in the list.</p>
     *
     * @param username, user for which recommendations are to be computed.
     * @return {@link java.util.List} of recommended users(i.e. username) ordered by their distance from the user.
     */
    @Override
    public List<String> recommend(@NotNull final String username) {
        return profileMongoService.findUsersByProximity(username);
    }


}
