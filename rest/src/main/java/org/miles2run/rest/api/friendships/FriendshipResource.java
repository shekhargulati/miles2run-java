package org.miles2run.rest.api.friendships;

import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.mongo.FriendshipRepository;
import org.miles2run.core.repositories.redis.NotificationRepository;
import org.miles2run.core.repositories.redis.TimelineRepository;
import org.miles2run.domain.kv_aggregates.Action;
import org.miles2run.domain.kv_aggregates.Notification;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Path("friendships")
public class FriendshipResource {

    @Context
    private SecurityContext securityContext;

    @Inject
    private FriendshipRepository friendshipRepository;
    @Inject
    private NotificationRepository notificationRepository;
    @Inject
    private TimelineRepository timelineRepository;

    @Path("/create")
    @POST
    @Consumes("application/json")
    @LoggedIn
    public Response create(FriendshipRequest friendshipRequest) {
        String username = securityContext.getUserPrincipal().getName();
        friendshipRepository.createFriendship(username, friendshipRequest.getUserToFollow());
        timelineRepository.updateTimelineWithFollowingTimeline(username, friendshipRequest.getUserToFollow());
        notificationRepository.addNotification(new Notification(friendshipRequest.getUserToFollow(), username, Action.FOLLOW, new Date().getTime()));
        Map<String, String> jsonObj = new HashMap<>();
        jsonObj.put("msg", "Successfully followed user " + friendshipRequest.getUserToFollow());
        return Response.status(Response.Status.CREATED).entity(jsonObj).build();
    }

    @Path("/destroy")
    @POST
    @Consumes("application/json")
    @LoggedIn
    public Response destroy(UnfollowRequest unfollowRequest) {
        String username = securityContext.getUserPrincipal().getName();
        friendshipRepository.destroyFriendship(username, unfollowRequest.getUserToUnfollow());
        timelineRepository.removeFollowingTimeline(username, unfollowRequest.getUserToUnfollow());
        notificationRepository.addNotification(new Notification(unfollowRequest.getUserToUnfollow(), username, Action.UNFOLLOW, new Date().getTime()));
        Map<String, String> jsonObj = new HashMap<>();
        jsonObj.put("msg", "Successfully unfollowed user " + unfollowRequest.getUserToUnfollow());
        return Response.status(Response.Status.OK).entity(jsonObj).build();
    }
}
