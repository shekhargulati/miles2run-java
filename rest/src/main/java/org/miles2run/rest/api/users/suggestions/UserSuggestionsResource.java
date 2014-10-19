package org.miles2run.rest.api.users.suggestions;

import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.suggester.UserSuggester;
import org.miles2run.core.vo.ProfileDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.List;

@Path("users/suggestions")
public class UserSuggestionsResource {

    private final Logger logger = LoggerFactory.getLogger(UserSuggestionsResource.class);

    @Context
    private SecurityContext securityContext;
    @Inject
    private UserSuggester userSuggester;
    @Inject
    private ProfileRepository profileRepository;

    @GET
    @Produces("application/json")
    @LoggedIn
    public List<ProfileDetails> suggestUsers() {
        String username = securityContext.getUserPrincipal().getName();
        List<String> suggestions = userSuggester.suggestions(username);
        if (suggestions.isEmpty()) {
            logger.debug("No user suggestions for {}", username);
            return Collections.emptyList();
        }
        logger.debug("Suggested users for {} are {}", username, suggestions);
        return profileRepository.findAllProfiles(suggestions);
    }

}
