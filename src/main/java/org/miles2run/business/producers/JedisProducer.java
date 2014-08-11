package org.miles2run.business.producers;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Created by shekhargulati on 17/03/14.
 */
@ApplicationScoped
public class JedisProducer {

    private Logger logger = LoggerFactory.getLogger(JedisProducer.class);

    @Produces
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxActive(128);
        poolConfig.setMaxIdle(20);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setMinIdle(10);
        poolConfig.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
        String host = System.getenv("OPENSHIFT_REDIS_DB_HOST");
        if (host == null) {
            System.out.print("Localhost Redis Configuration");
            return new JedisPool(poolConfig, "localhost");
        }
        int port = Integer.valueOf(System.getenv("OPENSHIFT_REDIS_DB_PORT"));
        String password = System.getenv("OPENSHIFT_REDIS_DB_PASSWORD");
        logger.info("Redis configuration : Host {} Port {} Password {}", host, port, password);
        JedisPool jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        return jedisPool;
    }

    @PreDestroy
    public void close() {
        jedisPool().destroy();
    }
}
