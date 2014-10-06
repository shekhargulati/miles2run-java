package org.miles2run.business.recommender;

import java.util.List;

/**
 * <p>
 *     Implementations of this interface can recommend friends to a user.
 * </p>
 */
public interface FriendRecommender {

    /**
     * Recommends a list of users that a user can follow.
     *
     * @param username, user for which recommendations are to be computed.
     * @return {@link java.util.List} of recommended users(i.e. username) ordered from most strongly recommended to least.
     */
    public List<String> recommend(String username);
}
