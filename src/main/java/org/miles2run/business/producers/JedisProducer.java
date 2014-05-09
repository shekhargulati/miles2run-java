package org.miles2run.business.producers;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 17/03/14.
 */
@ApplicationScoped
public class JedisProducer {

    @Inject
    private Logger logger;

    @Produces
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        String host = System.getenv("OPENSHIFT_REDIS_HOST");
        if (host == null) {
            System.out.print("Localhost Redis Configuration");
            return new JedisPool(poolConfig, "localhost");
        }
        int port = Integer.valueOf(System.getenv("OPENSHIFT_REDIS_PORT"));
        String password = System.getenv("REDIS_PASSWORD");
        logger.info(String.format("Redis configuration : Host %s Port %d Password %s", host, port, password));
        JedisPool jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        return jedisPool;
    }

    @PreDestroy
    public void close() {
        jedisPool().destroy();
    }
}
