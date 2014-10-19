package org.miles2run.core.repositories.redis;

import org.miles2run.core.vo.Notification;
import redis.clients.jedis.Jedis;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@ApplicationScoped
public class NotificationRepository {

    @Inject
    JedisExecution jedisExecution;

    public Long addNotification(final Notification notification) {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.zadd("notifications:" + notification.getUserToNotify(), notification.getTimestamp(), notification.toJSON());
            }
        });
    }

    public Set<Notification> notifications(final String username) {
        Set<String> notificationStrings = jedisExecution.execute(new JedisOperation<Set<String>>() {
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

