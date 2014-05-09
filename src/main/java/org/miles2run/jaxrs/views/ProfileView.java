package org.miles2run.jaxrs.views;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.IdNameEntity;
import org.jboss.resteasy.annotations.Form;
import org.jug.filters.AfterLogin;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.domain.SocialConnection;
import org.miles2run.business.domain.SocialProvider;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.services.SocialConnectionService;
import org.miles2run.jaxrs.utils.CityAndCountry;
import org.miles2run.jaxrs.utils.GeocoderUtils;
import org.miles2run.jaxrs.utils.UrlUtils;
import org.miles2run.jaxrs.vo.ProfileDetails;
import org.miles2run.jaxrs.forms.ProfileForm;
import org.thymeleaf.TemplateEngine;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.RollbackException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 05/03/14.
 */
@Path("/profiles")
@Transactional
public class ProfileView {

    @Inject
    private Logger logger;
    @Inject
    private TwitterFactory twitterFactory;
    @Inject
    private TemplateEngine templateEngine;
    @Inject
    private ProfileService profileService;
    @Inject
    private FacebookFactory facebookFactory;
    @Inject
    private SocialConnectionService socialConnectionService;

    @GET
    @Produces("text/html")
    @Path("/new")
    public View profileForm(@QueryParam("connectionId") String connectionId) {
        try {
            logger.info("ConnectionId : " + connectionId);
            SocialConnection socialConnection = socialConnectionService.findByConnectionId(connectionId);
            if (socialConnection == null) {
                return new View("/signin", true);
            }
            if (socialConnection.getProvider() == SocialProvider.TWITTER) {
                return twitterProfile(connectionId, socialConnection);
            } else if (socialConnection.getProvider() == SocialProvider.FACEBOOK) {
                return facebookProfile(connectionId, socialConnection);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to load profile form page.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
        return new View("/signin", true);
    }

    @POST
    @Path("/new")
    @AfterLogin
    public View createProfile(@Form ProfileForm profileForm) {
        try {
            logger.info(profileForm.toString());
            List<String> errors = new ArrayList<>();
            if (profileService.findProfileByEmail(profileForm.getEmail()) != null) {
                errors.add(String.format("User already exist with email %s", profileForm.getEmail()));
            }
            if (profileService.findProfileByUsername(profileForm.getUsername()) != null) {
                errors.add(String.format("User already exist with username %s", profileForm.getUsername()));
            }
            if (!errors.isEmpty()) {
                return new View("/createProfile", profileForm, "profile", errors).setTemplateEngine(templateEngine);
            }
            Profile profile = new Profile(profileForm);
            try {
                profileService.save(profile);
            } catch (Exception e) {
                logger.info(e.getClass().getCanonicalName());
                RollbackException rollbackException = (RollbackException) e;
                Throwable rollbackCause = rollbackException.getCause();
                if (rollbackCause instanceof PersistenceException) {
                    PersistenceException persistenceException = (PersistenceException) rollbackCause;
                    if (persistenceException.getCause() instanceof ConstraintViolationException) {
                        ConstraintViolationException constraintViolationException = (ConstraintViolationException) persistenceException.getCause();
                        Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();
                        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
                            errors.add(String.format("Field '%s' with value '%s' is invalid. %s", constraintViolation.getPropertyPath(), constraintViolation.getInvalidValue(), constraintViolation.getMessage()));
                        }
                        return new View("/createProfile", profileForm, "profile", errors).setTemplateEngine(templateEngine);
                    }
                }
                errors.add(e.getMessage());
                return new View("/createProfile", profileForm, "profile", errors).setTemplateEngine(templateEngine);
            }
            socialConnectionService.update(profile, profileForm.getConnectionId());
            Map<String, Object> model = new HashMap<>();
            model.put("principal", profileService.findProfileByUsername(profile.getUsername()));
            return new View("/home", true, model);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to load create profile.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }

    private View twitterProfile(String connectionId, SocialConnection socialConnection) {
        try {
            Twitter twitter = twitterFactory.getInstance();
            twitter.setOAuthAccessToken(new AccessToken(socialConnection.getAccessToken(), socialConnection.getAccessSecret()));
            User user = twitter.showUser(Long.valueOf(connectionId));
            String twitterProfilePic = user.getOriginalProfileImageURL();
            twitterProfilePic = UrlUtils.removeProtocol(twitterProfilePic);
            CityAndCountry cityAndCountry = GeocoderUtils.parseLocation(user.getLocation());
            ProfileDetails profile = new ProfileDetails(user.getScreenName(), user.getName(), user.getDescription(), connectionId, twitterProfilePic, cityAndCountry.getCity(), cityAndCountry.getCountry());
            return new View("/createProfile", profile, "profile").setTemplateEngine(templateEngine);
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        }

    }

    private View facebookProfile(String connectionId, SocialConnection socialConnection) {
        Facebook facebook = facebookFactory.getInstance(new facebook4j.auth.AccessToken(socialConnection.getAccessToken(), null));
        try {

            facebook4j.User user = facebook.users().getMe();
            String facebookProfilePic = null;
            URL picture = facebook.getPictureURL(user.getId());
            if (picture != null) {
                try {
                    facebookProfilePic = picture.toURI().toString();
                    logger.info("Facebook Picture URL" + facebookProfilePic);
                    facebookProfilePic = UrlUtils.removeProtocol(facebookProfilePic);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            }

            String email = user.getEmail();
            String gender = user.getGender();
            IdNameEntity location = user.getLocation();
            CityAndCountry cityAndCountry = new CityAndCountry();
            if (location != null) {
                cityAndCountry = GeocoderUtils.parseLocation(location.getName());
            }

            ProfileDetails profile = new ProfileDetails(user.getUsername(), user.getName(), user.getBio(), connectionId, facebookProfilePic, cityAndCountry.getCity(), cityAndCountry.getCountry());
            profile.setEmail(email);
            profile.setGender(gender);
            logger.info("Profile: " + profile);
            return new View("/createProfile", profile, "profile").setTemplateEngine(templateEngine);
        } catch (FacebookException e) {
            throw new RuntimeException(e);
        }
    }

}
