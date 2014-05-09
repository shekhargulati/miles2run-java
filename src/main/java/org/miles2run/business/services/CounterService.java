package org.miles2run.business.services;

import redis.clients.jedis.Jedis;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by shekhargulati on 17/03/14.
 */
@ApplicationScoped
public class CounterService {

    static final String COUNTRY_COUNTER = "countries";
    static final String DEVELOPER_COUNTER = "developers";
    static final String RUN_COUNTER = "run";

    @Inject
    JedisExecutionService jedisExecutionService;

    public Long updateCountryCounter(final String country) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.sadd(COUNTRY_COUNTER, country);
            }
        });
    }

    public Long getCountryCounter() {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.scard(COUNTRY_COUNTER);
            }
        });
    }

    public Long updateDeveloperCounter() {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.incr(DEVELOPER_COUNTER);
            }
        });
    }

    public Long getDeveloperCounter() {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                String counter = jedis.get(DEVELOPER_COUNTER);
                return counter == null ? Long.valueOf(0) : Long.valueOf(counter);
            }
        });
    }

    public Long updateRunCounter(final long distanceCovered) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.incrBy(RUN_COUNTER, distanceCovered);
            }
        });
    }

    public Long getRunCounter() {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                String counter = jedis.get(RUN_COUNTER);
                return counter == null ? Long.valueOf(0) : Long.valueOf(counter);
            }
        });
    }
}
