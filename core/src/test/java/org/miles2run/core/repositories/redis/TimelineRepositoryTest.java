package org.miles2run.core.repositories.redis;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TimelineRepositoryTest {


    private TimelineRepository repository;
    @Mock
    private JedisPool jedisPool;


    @Before
    public void setUp() throws Exception {
        repository = new TimelineRepository();
        JedisExecution jedisExecution = new JedisExecution();
        jedisExecution.setJedisPool(jedisPool);
        repository.jedisExecution = jedisExecution;
    }


    @Test
    public void givenUserHomeTimelineWith5Activities_whenGetHomeTimelineIds_ThenReturnHomeTimelineIds() throws Exception {
        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.zrevrange("home:test_user:timeline", 0, 4)).thenReturn(Stream.of("1", "2", "3", "4", "5").collect(toSet()));

        Set<String> homeTimelineIds = repository.getHomeTimelineIds("test_user", 1, 5);
        assertThat(homeTimelineIds, hasSize(5));
        verify(jedisPool, times(1)).getResource();
        verify(jedisPool, times(1)).returnResource(any());
        verify(jedis).zrevrange("home:test_user:timeline", 0, 4);
        verifyNoMoreInteractions(jedis, jedisPool);
    }
}