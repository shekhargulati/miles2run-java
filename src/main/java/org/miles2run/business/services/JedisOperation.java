package org.miles2run.business.services;

import redis.clients.jedis.Jedis;

/**
 * Created by shekhargulati on 17/03/14.
 */
public interface JedisOperation<T> {

    <T> T perform(Jedis jedis);
}
