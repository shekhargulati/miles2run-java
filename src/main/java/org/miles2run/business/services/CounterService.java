package org.miles2run.business.services;

import org.miles2run.business.domain.Counter;
import redis.clients.jedis.Jedis;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by shekhargulati on 17/03/14.
 */
@ApplicationScoped
public class CounterService {

    private static final String COUNTRY_SET_KEY = "countries";
    private static final String RUNNER_COUNTER = "runners";
    private static final String DISTANCE_COUNTER = "distance";
    private static final String CITY_SET_KEY = "cities";
    private static final String SECONDS_COUNTER = "hours";


    @Inject
    private JedisExecutionService jedisExecutionService;

    public Long addCountry(final String country) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.sadd(COUNTRY_SET_KEY, country);
            }
        });
    }

    public Long getCountryCount() {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.scard(COUNTRY_SET_KEY);
            }
        });
    }

    public Long addCity(final String country) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.sadd(CITY_SET_KEY, country);
            }
        });
    }

    public Long getCityCount() {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.scard(CITY_SET_KEY);
            }
        });
    }

    public Long updateRunnerCount() {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.incr(RUNNER_COUNTER);
            }
        });
    }

    public Long getRunnerCount() {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                String counter = jedis.get(RUNNER_COUNTER);
                return counter == null ? Long.valueOf(0) : Long.valueOf(counter);
            }
        });
    }


    public Long updateActivitySecondsCount(final long seconds) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.incrBy(SECONDS_COUNTER, seconds);
            }
        });
    }

    public Long getActivitySecondCount() {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                String counter = jedis.get(SECONDS_COUNTER);
                return counter == null ? Long.valueOf(0) : Long.valueOf(counter);
            }
        });
    }

    public Double updateDistanceCount(final double distanceCovered) {
        return jedisExecutionService.execute(new JedisOperation<Double>() {
            @Override
            public Double perform(Jedis jedis) {
                return jedis.incrByFloat(DISTANCE_COUNTER, distanceCovered);
            }
        });
    }

    public Double getDistanceCount() {
        return jedisExecutionService.execute(new JedisOperation<Double>() {
            @Override
            public Double perform(Jedis jedis) {
                String counter = jedis.get(DISTANCE_COUNTER);
                return counter == null ? Double.valueOf(0) : Double.valueOf(counter);
            }
        });
    }

    public Counter currentCounter() {
        return new Counter(getRunnerCount(), getCountryCount(), getDistanceCount(), getCityCount(), getActivitySecondCount());
    }
}
