package org.miles2run.core.repositories.redis;

import org.miles2run.domain.kv_aggregates.Counter;
import redis.clients.jedis.Jedis;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CounterStatsRepository {

    @Inject
    private JedisExecution jedisExecution;

    public Long addCountry(final String country) {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.sadd(RedisKeyNames.COUNTRY_SET_KEY, country);
            }
        });
    }

    public Long addCity(final String country) {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.sadd(RedisKeyNames.CITY_SET_KEY, country);
            }
        });
    }

    public Long updateRunnerCount() {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.incr(RedisKeyNames.RUNNER_COUNTER);
            }
        });
    }

    public Long updateActivitySecondsCount(final long seconds) {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.incrBy(RedisKeyNames.SECONDS_COUNTER, seconds);
            }
        });
    }

    public Double updateDistanceCount(final double distanceCovered) {
        return jedisExecution.execute(new JedisOperation<Double>() {
            @Override
            public Double perform(Jedis jedis) {
                return jedis.incrByFloat(RedisKeyNames.DISTANCE_COUNTER, distanceCovered);
            }
        });
    }

    public Counter currentCounter() {
        return new Counter(getRunnerCount(), getCountryCount(), getDistanceCount(), getCityCount(), getActivitySecondCount());
    }

    public Long getCountryCount() {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.scard(RedisKeyNames.COUNTRY_SET_KEY);
            }
        });
    }

    public Long getCityCount() {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                return jedis.scard(RedisKeyNames.CITY_SET_KEY);
            }
        });
    }

    public Long getRunnerCount() {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                String counter = jedis.get(RedisKeyNames.RUNNER_COUNTER);
                return counter == null ? Long.valueOf(0) : Long.valueOf(counter);
            }
        });
    }

    public Long getActivitySecondCount() {
        return jedisExecution.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                String counter = jedis.get(RedisKeyNames.SECONDS_COUNTER);
                return counter == null ? Long.valueOf(0) : Long.valueOf(counter);
            }
        });
    }

    public Double getDistanceCount() {
        return jedisExecution.execute(new JedisOperation<Double>() {
            @Override
            public Double perform(Jedis jedis) {
                String counter = jedis.get(RedisKeyNames.DISTANCE_COUNTER);
                return counter == null ? Double.valueOf(0) : Double.valueOf(counter);
            }
        });
    }
}
