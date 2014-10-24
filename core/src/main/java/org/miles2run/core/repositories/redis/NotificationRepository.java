package org.miles2run.core.repositories.redis;

import com.google.gson.Gson;
import org.miles2run.domain.kv_aggregates.Notification;
import redis.clients.jedis.Jedis;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotificationRepository {

    @Inject
    JedisExecution jedisExecution;

    public Long addNotification(final Notification notification) {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.zadd("notifications:" + notification.getUserToNotify(), notification.getTimestamp(), toJSON());
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

        return notificationStrings.stream().map(NotificationRepository::toNotification).collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
    }

    private static Notification toNotification(String notification) {
        Gson gson = new Gson();
        return gson.fromJson(notification, Notification.class);
    }

    private String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

