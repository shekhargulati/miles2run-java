package org.miles2run.rest.api.users.notifications;

import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.redis.NotificationRepository;
import org.miles2run.domain.kv_aggregates.Notification;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.Set;

@Path("notifications")
public class NotificationResource {

    @Inject
    private NotificationRepository notificationRepository;
    @Context
    private SecurityContext securityContext;

    @GET
    @Produces("application/json")
    @LoggedIn
    public Set<Notification> userNotifications() {
        String username = securityContext.getUserPrincipal().getName();
        return notificationRepository.notifications(username);
    }
}
