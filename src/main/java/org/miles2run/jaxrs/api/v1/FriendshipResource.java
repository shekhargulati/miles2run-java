package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.mongo.Action;
import org.miles2run.business.domain.redis.Notification;
import org.miles2run.business.repository.mongo.FriendshipRepository;
import org.miles2run.business.services.redis.NotificationService;
import org.miles2run.business.services.redis.TimelineService;
import org.miles2run.jaxrs.vo.FriendshipRequest;
import org.miles2run.jaxrs.vo.UnfollowRequest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shekhargulati on 25/03/14.
 */
@Path("/api/v1/profiles/{username}/friendships")
public class FriendshipResource {

    @Inject
    private FriendshipRepository friendshipRepository;
    @Inject
    private NotificationService notificationService;
    @Inject
    private TimelineService timelineService;

    @Path("/create")
    @POST
    @Consumes("application/json")
    @LoggedIn
    public Response create(@PathParam("username") String username, FriendshipRequest friendshipRequest) {
        friendshipRepository.createFriendship(username, friendshipRequest.getUserToFollow());
        timelineService.updateTimelineWithFollowingTimeline(username, friendshipRequest.getUserToFollow());
        notificationService.addNotification(new Notification(friendshipRequest.getUserToFollow(), username, Action.FOLLOW, new Date().getTime()));
        Map<String, String> jsonObj = new HashMap<>();
        jsonObj.put("msg", "Successfully followed user " + friendshipRequest.getUserToFollow());
        return Response.status(Response.Status.CREATED).entity(jsonObj).build();
    }

    @Path("/destroy")
    @POST
    @Consumes("application/json")
    @LoggedIn
    public Response destroy(@PathParam("username") String username, UnfollowRequest unfollowRequest) {
        friendshipRepository.destroyFriendship(username, unfollowRequest.getUserToUnfollow());
        timelineService.removeFollowingTimeline(username, unfollowRequest.getUserToUnfollow());
        notificationService.addNotification(new Notification(unfollowRequest.getUserToUnfollow(), username, Action.UNFOLLOW, new Date().getTime()));
        Map<String, String> jsonObj = new HashMap<>();
        jsonObj.put("msg", "Successfully unfollowed user " + unfollowRequest.getUserToUnfollow());
        return Response.status(Response.Status.OK).entity(jsonObj).build();
    }
}
