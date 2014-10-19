package org.miles2run.core.suggester;

import java.util.List;

/**
 * <p>
 * Implementations of this interface suggests friends to a user. A list of users that a user can follow
 * </p>
 */
public interface UserSuggester {

    /**
     * Recommends a list of users that a user can follow.
     *
     * @param username, user for which recommendations are to be computed.
     * @return {@link java.util.List} of suggested users(i.e. username) ordered from most strongly recommended to least.
     */
    public List<String> suggestions(String username);
}
