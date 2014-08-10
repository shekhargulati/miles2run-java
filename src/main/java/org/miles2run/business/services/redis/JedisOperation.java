package org.miles2run.business.services.redis;

import redis.clients.jedis.Jedis;

/**
 * Created by shekhargulati on 17/03/14.
 */
public interface JedisOperation<T> {

    <T> T perform(Jedis jedis);
}
