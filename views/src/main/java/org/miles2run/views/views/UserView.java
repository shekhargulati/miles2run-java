package org.miles2run.views.views;

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
import org.miles2run.core.repositories.jpa.ActivityRepository;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.jpa.SocialConnectionRepository;
import org.miles2run.core.repositories.jpa.vo.ActivityCountAndDistanceTuple;
import org.miles2run.core.repositories.mongo.FriendshipRepository;
import org.miles2run.core.repositories.mongo.UserProfileRepository;
import org.miles2run.core.repositories.redis.CounterStatsRepository;
import org.miles2run.core.repositories.redis.GoalStatsRepository;
import org.miles2run.core.utils.GeocoderUtils;
import org.miles2run.core.utils.UrlUtils;
import org.miles2run.core.utils.vo.CityAndCountry;
import org.miles2run.domain.documents.UserProfile;
import org.miles2run.domain.entities.*;
import org.miles2run.representations.UserRepresentation;
import org.miles2run.social.Google;
import org.miles2run.social.GoogleService;
import org.miles2run.views.filters.InjectProfile;
import org.miles2run.views.forms.ProfileForm;
import org.miles2run.views.forms.UpdateProfileForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.stream.Collectors;

@Path("/users")
public class UserView {

    private final Logger logger = LoggerFactory.getLogger(UserView.class);

    @Inject
    private TwitterFactory twitterFactory;
    @Inject
    private TemplateEngine templateEngine;
    @Inject
    private ProfileRepository profileRepository;
    @Inject
    private FacebookFactory facebookFactory;
    @Inject
    private SocialConnectionRepository socialConnectionService;
    @Inject
    private CounterStatsRepository counterService;
    @Inject
    private ActivityRepository activityRepository;
    @Context
    private SecurityContext securityContext;
    @Inject
    private UserProfileRepository userProfileRepository;
    @Inject
    private GoogleService googleService;
    @Inject
    private GoalRepository goalRepository;
    @Inject
    private GoalStatsRepository goalRedisService;
    @Inject
    private FriendshipRepository friendshipRepository;

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
            logger.warn("Unable to load profile form page.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
        return View.of("/signin", true);
    }

    private View twitterProfile(String connectionId, SocialConnection socialConnection) {
        try {
            Twitter twitter = twitterFactory.getInstance();
            twitter.setOAuthAccessToken(new AccessToken(socialConnection.getAccessToken(), socialConnection.getAccessSecret()));
            User user = twitter.showUser(Long.valueOf(connectionId));
            String twitterProfilePic = user.getOriginalProfileImageURL();
            twitterProfilePic = UrlUtils.removeProtocol(twitterProfilePic);
            CityAndCountry cityAndCountry = GeocoderUtils.parseLocation(user.getLocation());
            UserViewRepresentation userRepresentation = UserViewRepresentation.createUserRepresentation(user.getScreenName(), user.getName(), user.getDescription(), connectionId, twitterProfilePic, cityAndCountry.getCity(), cityAndCountry.getCountry());
            return View.of("/createProfile", templateEngine).withModel("profile", userRepresentation);
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
            IdNameEntity location = user.getLocation();
            CityAndCountry cityAndCountry = new CityAndCountry();
            if (location != null) {
                cityAndCountry = GeocoderUtils.parseLocation(location.getName());
            }

            UserViewRepresentation profile = UserViewRepresentation.createUserRepresentation(user.getUsername(), user.getName(), user.getBio(), connectionId, facebookProfilePic, cityAndCountry.getCity(), cityAndCountry.getCountry()).addEmail(user.getEmail()).setGender(user.getGender());
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
        UserViewRepresentation profile = UserViewRepresentation.createUserRepresentation(username, user.getName(), null, connectionId, UrlUtils.removeProtocol(user.getPicture()), null, null).addEmail(user.getEmail()).setGender(user.getGender());

        return View.of("/createProfile", templateEngine).withModel("profile", profile);
    }

    private String getUsernameFromEmail(String email) {
        try {
            return email.split("@")[0];
        } catch (Exception e) {
            return null;
        }
    }

    @POST
    @Path("/new")
    @AfterLogin
    @Produces("text/html")
    @Transactional(value = Transactional.TxType.REQUIRED)
    public View createProfile(@Form ProfileForm profileForm) {
        List<String> errors = new ArrayList<>();
        try {
            if (profileRepository.findByEmail(profileForm.getEmail()) != null) {
                errors.add(String.format("User already exist with email %s", profileForm.getEmail()));
            }
            if (profileRepository.findByUsername(profileForm.getUsername()) != null) {
                errors.add(String.format("User already exist with username %s", profileForm.getUsername()));
            }
            if (!errors.isEmpty()) {
                return View.of("/createProfile", templateEngine).withModel("profile", profileForm).withModel("errors", errors);
            }
            Profile profile = new ProfileBuilder().setFullname(profileForm.getFullname()).setGender(profileForm.getGender()).setCountry(profileForm.getCountry()).setCity(profileForm.getCity()).setUsername(profileForm.getUsername()).setEmail(profileForm.getEmail()).setBio(profileForm.getBio()).setProfilePic(profileForm.getProfilePic()).createProfile();
            profile.addSocialConnection(socialConnectionService.findByConnectionId(profileForm.getConnectionId()));
            profileRepository.save(profile);
            userProfileRepository.save(profile);
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
        errors.addAll(constraintViolations.stream().map(constraintViolation -> String.format("Field '%s' with value '%s' is invalid. %s", constraintViolation.getPropertyPath(), constraintViolation.getInvalidValue(), constraintViolation.getMessage())).collect(Collectors.toList()));
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
                Profile profile = profileRepository.findByUsername(username);
                setUpdatedProperties(profile, profileForm);
                profileRepository.update(profile);
                userProfileRepository.update(username, profileForm.getCity(), profileForm.getCountry());
                return View.of("/users/" + username, true);
            } catch (Exception e) {
                logger.info(e.getClass().getCanonicalName());
                if (e instanceof RollbackException) {
                    RollbackException rollbackException = (RollbackException) e;
                    Throwable rollbackCause = rollbackException.getCause();
                    if (rollbackCause instanceof PersistenceException) {
                        PersistenceException persistenceException = (PersistenceException) rollbackCause;
                        if (persistenceException.getCause() instanceof ConstraintViolationException) {
                            ConstraintViolationException constraintViolationException = (ConstraintViolationException) persistenceException.getCause();
                            Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();
                            errors.addAll(constraintViolations.stream().map(constraintViolation -> String.format("Field '%s' with value '%s' is invalid. %s", constraintViolation.getPropertyPath(), constraintViolation.getInvalidValue(), constraintViolation.getMessage())).collect(Collectors.toList()));
                            return View.of("/editProfile", templateEngine).withModel("profile", profileForm).withModel("errors", errors);
                        }
                    }
                    errors.add(e.getMessage());
                }
                return View.of("/editProfile", templateEngine).withModel("profile", profileForm).withModel("errors", errors);
            }
        } catch (Exception e) {
            logger.warn("Unable to load create profile.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }

    private void setUpdatedProperties(Profile profile, UpdateProfileForm updated) {
        profile.setFullname(updated.getFullname());
        profile.setCity(updated.getCity());
        profile.setCountry(updated.getCountry());
        profile.setGender(updated.getGender());
        profile.setBio(updated.getBio());
    }

    @GET
    @Path("/{username}")
    @Produces("text/html")
    @InjectProfile
    @InjectPrincipal
    public View viewUserProfile(@PathParam("username") String username) {
        try {
            Profile profile = profileRepository.findByUsername(username);
            if (profile == null) {
                throw new ViewResourceNotFoundException(String.format("No user exists with username %s", username), templateEngine);
            }
            UserRepresentation userProfile = UserRepresentation.from(profile);
            Map<String, Object> model = new HashMap<>();
            model.put("userProfile", userProfile);
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

            long activeGoalCount = goalRepository.countOfActiveGoalCreatedByUser(profile);
            ActivityCountAndDistanceTuple tuple = activityRepository.calculateTotalActivitiesAndDistanceCoveredByUser(profile);
            model.put("activeGoalCount", activeGoalCount);
            model.put("totalActivities", tuple.getActivityCount());
            model.put("totalDistance", toMiles(tuple));
            return View.of("/profile", templateEngine).withModel(model);
        } catch (Exception e) {
            if (e instanceof ViewResourceNotFoundException) {
                throw e;
            }
            logger.warn(String.format("Unable to load %s page.", username), e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }

    private boolean isFollowing(String currentLoggedInUser, String username) {
        return friendshipRepository.isUserFollowing(currentLoggedInUser, username);
    }

    double toMiles(ActivityCountAndDistanceTuple tuple) {
        return tuple.getDistanceCovered() / GoalUnit.MI.getConversion();
    }

    @GET
    @Path("/{username}/following")
    @Produces("text/html")
    @InjectPrincipal
    @InjectProfile
    public View following(@PathParam("username") String username) {
        try {
            Profile profile = profileRepository.findByUsername(username);
            if (profile == null) {
                throw new ViewResourceNotFoundException(String.format("No user exists with username %s", username), templateEngine);
            }
            UserRepresentation userProfile = UserRepresentation.from(profile);
            Map<String, Object> model = new HashMap<>();
            model.put("userProfile", userProfile);
            UserProfile userProfileMongo = userProfileRepository.find(username);
            List<String> following = userProfileMongo.getFollowing();
            if (!following.isEmpty()) {
                List<Profile> profiles = profileRepository.findProfiles(following);
                model.put("followingProfiles", profiles.stream().map(UserRepresentation::from).collect(Collectors.toList()));
            }
            model.put("followers", userProfileMongo.getFollowers().size());
            model.put("following", userProfileMongo.getFollowing().size());
            model.put("activities", activityRepository.count(profile));
            return View.of("/following", templateEngine).withModel(model);
        } catch (Exception e) {
            if (e instanceof ViewResourceNotFoundException) {
                throw e;
            }
            logger.warn(String.format("Unable to load %s page.", username), e);
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
            Profile profile = profileRepository.findByUsername(username);
            if (profile == null) {
                throw new ViewResourceNotFoundException(String.format("No user exists with username %s", username), templateEngine);
            }
            UserRepresentation userProfile = UserRepresentation.from(profile);
            Map<String, Object> model = new HashMap<>();
            model.put("userProfile", userProfile);
            UserProfile userProfileMongo = userProfileRepository.find(username);
            List<String> followers = userProfileMongo.getFollowers();
            if (!followers.isEmpty()) {
                List<Profile> profiles = profileRepository.findProfiles(followers);
                model.put("followerProfiles", profiles.stream().map(UserRepresentation::from).collect(Collectors.toList()));
            }
            model.put("followers", userProfileMongo.getFollowers().size());
            model.put("following", userProfileMongo.getFollowing().size());
            model.put("activities", activityRepository.count(profile));
            return View.of("/followers", templateEngine).withModel(model);
        } catch (Exception e) {
            if (e instanceof ViewResourceNotFoundException) {
                throw e;
            }
            logger.warn(String.format("Unable to load %s page.", username), e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }

}
