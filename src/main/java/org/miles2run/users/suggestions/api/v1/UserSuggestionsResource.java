package org.miles2run.users.suggestions.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.vo.ProfileDetails;
import org.miles2run.shared.repositories.ProfileRepository;
import org.miles2run.users.suggestions.suggester.UserSuggester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.List;

@Path("/api/v1/users/suggestions")
public class UserSuggestionsResource {

    private final Logger logger = LoggerFactory.getLogger(UserSuggestionsResource.class);

    @Inject
    private SecurityContext context;
    @Inject
    private UserSuggester userSuggester;
    @Inject
    private ProfileRepository profileRepository;

    @GET
    @Produces("application/json")
    @LoggedIn
    public List<ProfileDetails> suggestUsers() {
        String username = context.getUserPrincipal().getName();
        List<String> suggestions = userSuggester.suggestions(username);
        if (suggestions.isEmpty()) {
            logger.debug("No user suggestions for {}", username);
            return Collections.emptyList();
        }
        logger.debug("Suggested users for {} are {}", username, suggestions);
        return profileRepository.findAllProfiles(suggestions);
    }

}
