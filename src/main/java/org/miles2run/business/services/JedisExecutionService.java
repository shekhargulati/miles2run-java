package org.miles2run.business.services;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by shekhargulati on 17/03/14.
 */
@ApplicationScoped
public class JedisExecutionService {

    @Inject
    JedisPool jedisPool;

    public <T> T execute(JedisOperation<T> operation) {
        Jedis jedis = jedisPool.getResource();
        try {
            return operation.perform(jedis);
        } catch (JedisConnectionException e) {
            if (null != jedis) {
                jedisPool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            if (null != jedis) {
                jedisPool.returnResource(jedis);
            }
        }
        return null;
    }
}
