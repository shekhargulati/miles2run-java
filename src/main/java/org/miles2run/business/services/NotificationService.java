package org.miles2run.business.services;

import org.miles2run.business.domain.redis.Notification;
import redis.clients.jedis.Jedis;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by shekhargulati on 26/03/14.
 */
@ApplicationScoped
public class NotificationService {

    @Inject
    JedisExecutionService jedisExecutionService;

    public Long addNotification(final Notification notification) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.zadd("notifications:" + notification.getUserToNotify(), notification.getTimestamp(), notification.toJSON());
            }
        });
    }

    public Set<Notification> notifications(final String username) {
        Set<String> notificationStrings = jedisExecutionService.execute(new JedisOperation<Set<String>>() {
            @Override
            public Set<String> perform(Jedis jedis) {
                long currentTimestamp = new Date().getTime();
                return jedis.zrevrangeByScore("notifications:" + username, currentTimestamp, 0);
            }
        });

        Set<Notification> notifications = new LinkedHashSet<>();
        for (String notification : notificationStrings) {
            notifications.add(Notification.toNotification(notification));
        }
        return notifications;
    }
}

