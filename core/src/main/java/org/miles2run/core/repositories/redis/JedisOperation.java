package org.miles2run.core.repositories.redis;

import redis.clients.jedis.Jedis;

public interface JedisOperation<T> {

    <T> T perform(Jedis jedis);
}
