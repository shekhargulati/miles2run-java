package org.miles2run.jaxrs.views;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.IdNameEntity;
import org.jboss.resteasy.annotations.Form;
import org.jug.filters.AfterLogin;
import org.jug.filters.InjectPrincipal;
import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.domain.jpa.SocialConnection;
import org.miles2run.business.domain.jpa.SocialProvider;
import org.miles2run.business.domain.mongo.UserProfile;
import org.miles2run.business.services.*;
import org.miles2run.business.utils.CityAndCountry;
import org.miles2run.business.utils.GeocoderUtils;
import org.miles2run.business.utils.UrlUtils;
import org.miles2run.business.vo.Google;
import org.miles2run.business.vo.Progress;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.miles2run.jaxrs.forms.ProfileForm;
import org.miles2run.jaxrs.forms.UpdateProfileForm;
import org.miles2run.jaxrs.vo.GoalDetails;
import org.miles2run.jaxrs.vo.ProfileDetails;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
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
    @Inject
    private CounterService counterService;
    @Inject
    private ActivityService activityService;
    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileMongoService profileMongoService;
    @Inject
    private GoogleService googleService;
    @Inject
    private GoalService goalService;


    @GET
    @Produces("text/html")
    @Path("/new")
    public View profileForm(@QueryParam("connectionId") String connectionId) {
        try {
            logger.info("ConnectionId : " + connectionId);
            SocialConnection socialConnection = socialConnectionService.findByConnectionId(connectionId);
            if (socialConnection == null) {
                return View.of("/signin", true);
            }
            if (socialConnection.getProvider() == SocialProvider.TWITTER) {
                return twitterProfile(connectionId, socialConnection);
            } else if (socialConnection.getProvider() == SocialProvider.FACEBOOK) {
                return facebookProfile(connectionId, socialConnection);
            } else if (socialConnection.getProvider() == SocialProvider.GOOGLE_PLUS) {
                return googleProfile(connectionId, socialConnection);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to load profile form page.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
        return View.of("/signin", true);
    }

    @POST
    @Path("/new")
    @AfterLogin
    @Produces("text/html")
    public View createProfile(@Form ProfileForm profileForm) {
        List<String> errors = new ArrayList<>();
        try {
            if (profileService.findProfileByEmail(profileForm.getEmail()) != null) {
                errors.add(String.format("User already exist with email %s", profileForm.getEmail()));
            }
            if (profileService.findProfileByUsername(profileForm.getUsername()) != null) {
                errors.add(String.format("User already exist with username %s", profileForm.getUsername()));
            }
            if (!errors.isEmpty()) {
                return View.of("/createProfile", templateEngine).withModel("profile", profileForm).withModel("errors", errors);
            }
            Profile profile = new Profile(profileForm);
            profileService.save(profile);
            socialConnectionService.update(profile, profileForm.getConnectionId());
            profileMongoService.save(profile);
            counterService.updateRunnerCount();
            counterService.addCountry(profile.getCountry());
            counterService.addCity(profile.getCity());
            return View.of("/", true).withModel("principal", profile.getUsername());
        } catch (Exception e) {
            logger.info("createProfile() Exception class " + e.getClass().getCanonicalName());
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException) {
                return constraintVoilationView(profileForm, errors, (ConstraintViolationException) cause);
            }
            errors.add(e.getMessage());
            return View.of("/createProfile", templateEngine).withModel("profile", profileForm).withModel("errors", errors);
        }
    }

    private View constraintVoilationView(ProfileForm profileForm, List<String> errors, ConstraintViolationException constraintViolationException) {
        Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            errors.add(String.format("Field '%s' with value '%s' is invalid. %s", constraintViolation.getPropertyPath(), constraintViolation.getInvalidValue(), constraintViolation.getMessage()));
        }
        return View.of("/createProfile", templateEngine).withModel("profile", profileForm).withModel("errors", errors);
    }

    @GET
    @Produces("text/html")
    @Path("/edit")
    @LoggedIn
    @InjectProfile
    public View editForm() {
        return View.of("/editProfile", templateEngine);
    }

    @POST
    @Produces("text/html")
    @Path("/edit")
    @LoggedIn
    public View editProfile(@Form UpdateProfileForm profileForm) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            List<String> errors = new ArrayList<>();
            try {
                profileService.update(username, toProfile(profileForm));
                profileMongoService.update(username, profileForm.getCity(), profileForm.getCountry());
                return View.of("/profiles/" + username, true);
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
                        return View.of("/editProfile", templateEngine).withModel("profile", profileForm).withModel("errors", errors);
                    }
                }
                errors.add(e.getMessage());
                return View.of("/editProfile", templateEngine).withModel("profile", profileForm).withModel("errors", errors);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to load create profile.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }

    private Profile toProfile(UpdateProfileForm profileForm) {
       return Profile.createProfileForUpdate(profileForm.getFullname(),profileForm.getBio(),profileForm.getCity(),profileForm.getCountry(),profileForm.getGender());
    }


    @GET
    @Path("/{username}")
    @Produces("text/html")
    @InjectProfile
    @InjectPrincipal
    public View viewUserProfile(@PathParam("username") String username) {
        try {
            Profile profile = profileService.findProfileByUsername(username);
            if (profile == null) {
                throw new ViewResourceNotFoundException(String.format("No user exists with username %s", username), templateEngine);
            }
            Map<String, Object> model = new HashMap<>();
            model.put("userProfile", profile);
            String currentLoggedInUser = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
            logger.info("currentLoggedInUser : " + currentLoggedInUser);
            if (currentLoggedInUser != null) {
                boolean isMyProfile = currentLoggedInUser.equals(username);
                model.put("isMyProfile", isMyProfile);
                if (!isMyProfile) {
                    boolean isFollowing = isFollowing(currentLoggedInUser, username);
                    model.put("isFollowing", isFollowing);
                }
            }

            Long goalId = goalService.findLatestGoalWithActivity(username);
            if (goalId == null) {
                Goal activeGoal = goalService.findLatestCreatedGoal(username);
                if (activeGoal != null) {
                    model.put("activeGoal", new GoalDetails(activeGoal));
                    model.put("progress", new Progress(activeGoal));
                }
                return View.of("/profile", templateEngine).withModel(model);
            }
            Goal activeGoal = goalService.findGoal(username, goalId);
            model.put("activeGoal", new GoalDetails(activeGoal));
            Progress progress = activityService.calculateUserProgressForGoal(username, goalId);
            model.put("progress", progress);
            return View.of("/profile", templateEngine).withModel(model);
        } catch (Exception e) {
            if (e instanceof ViewResourceNotFoundException) {
                throw e;
            }
            logger.log(Level.SEVERE, String.format("Unable to load %s page.", username), e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }

    @GET
    @Path("/{username}/following")
    @Produces("text/html")
    @InjectPrincipal
    @InjectProfile
    public View following(@PathParam("username") String username) {
        try {
            Profile profile = profileService.findProfileByUsername(username);
            if (profile == null) {
                throw new ViewResourceNotFoundException(String.format("No user exists with username %s", username), templateEngine);
            }
            Map<String, Object> model = new HashMap<>();
            model.put("userProfile", profile);
            UserProfile userProfile = profileMongoService.findProfile(username);
            List<String> following = userProfile.getFollowing();
            if (!following.isEmpty()) {
                List<org.miles2run.business.vo.ProfileDetails> profiles = profileService.findAllProfiles(following);
                model.put("followingProfiles", profiles);
            }
            model.put("followers", userProfile.getFollowers().size());
            model.put("following", userProfile.getFollowing().size());
            model.put("activities", activityService.count(username));
            return View.of("/following", templateEngine).withModel(model);
        } catch (Exception e) {
            if (e instanceof ViewResourceNotFoundException) {
                throw e;
            }
            logger.log(Level.SEVERE, String.format("Unable to load %s page.", username), e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }

    }

    @GET
    @Path("/{username}/followers")
    @Produces("text/html")
    @InjectPrincipal
    @InjectProfile
    public View followers(@PathParam("username") String username) {
        try {
            Profile profile = profileService.findProfileByUsername(username);
            if (profile == null) {
                throw new ViewResourceNotFoundException(String.format("No user exists with username %s", username), templateEngine);
            }
            Map<String, Object> model = new HashMap<>();
            model.put("userProfile", profile);
            UserProfile userProfile = profileMongoService.findProfile(username);
            List<String> followers = userProfile.getFollowers();
            if (!followers.isEmpty()) {
                List<org.miles2run.business.vo.ProfileDetails> profiles = profileService.findAllProfiles(followers);
                model.put("followerProfiles", profiles);
            }
            model.put("followers", userProfile.getFollowers().size());
            model.put("following", userProfile.getFollowing().size());
            model.put("activities", activityService.count(username));
            return View.of("/followers", templateEngine).withModel(model);
        } catch (Exception e) {
            if (e instanceof ViewResourceNotFoundException) {
                throw e;
            }
            logger.log(Level.SEVERE, String.format("Unable to load %s page.", username), e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }

    private boolean isFollowing(String currentLoggedInUser, String username) {
        return profileMongoService.isUserFollowing(currentLoggedInUser, username);
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
            return View.of("/createProfile", templateEngine).withModel("profile", profile);
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
            return View.of("/createProfile", templateEngine).withModel("profile", profile);
        } catch (FacebookException e) {
            throw new RuntimeException(e);
        }
    }

    private View googleProfile(String connectionId, SocialConnection socialConnection) throws IOException {
        String accessToken = socialConnection.getAccessToken();
        GoogleTokenResponse token = new GoogleTokenResponse().setAccessToken(accessToken);
        Google user = googleService.getUser(token);
        String username = getUsernameFromEmail(user.getEmail());
        ProfileDetails profile = new ProfileDetails(username, user.getName(), null, connectionId, UrlUtils.removeProtocol(user.getPicture()), null, null);
        profile.setEmail(user.getEmail());
        profile.setGender(user.getGender());
        return View.of("/createProfile", templateEngine).withModel("profile", profile);
    }

    private String getUsernameFromEmail(String email) {
        try {
            return email.split("@")[0];
        } catch (Exception e) {
            return null;
        }
    }

}
