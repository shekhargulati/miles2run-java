package org.miles2run.business.services;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.GoalUnit;
import org.miles2run.business.utils.DateUtils;
import org.mockito.Matchers;
import org.mockito.Mockito;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.util.*;

/**
 * Created by shekhargulati on 03/07/14.
 */
public class ChartServiceTest {


    @Test
    public void testDistanceAndPaceOverNDays() throws Exception {
        ChartService chartService = new ChartService();
        JedisExecutionService jedisExecutionService = new JedisExecutionService();
        JedisPool jedisPool = Mockito.mock(JedisPool.class);
        jedisExecutionService.jedisPool = jedisPool;
        chartService.jedisExecutionService = jedisExecutionService;

        Jedis jedis = Mockito.mock(Jedis.class);
        Mockito.when(jedisPool.getResource()).thenReturn(jedis);
        Goal goal = Goal.of(Long.valueOf(3), 100L, GoalUnit.MI);

        double yesterdayTimestamp = DateUtils.timestampInDouble(1);
        Set<Tuple> activitiesWithScore = Sets.newHashSet(new Tuple("1", yesterdayTimestamp), new Tuple("2", yesterdayTimestamp), new Tuple("3", DateUtils.timestampInDouble(2)));
        Mockito.when(jedis.zrangeByScoreWithScores(Matchers.anyString(), Matchers.anyLong(), Matchers.anyLong())).thenReturn(activitiesWithScore);

        Mockito.when(jedis.hmget(Matchers.anyString(), Matchers.anyString(), Matchers.anyString())).thenReturn(Lists.newArrayList("1", "600"));
        Mockito.when(jedis.hmget(Matchers.anyString(), Matchers.anyString(), Matchers.anyString())).thenReturn(Lists.newArrayList("1", "600"));
        Mockito.when(jedis.hmget(Matchers.anyString(), Matchers.anyString(), Matchers.anyString())).thenReturn(Lists.newArrayList("1", "600"));

        List<Object[]> result = chartService.distanceAndPaceOverNDays("ajy.deb.5", goal, 30);
        Assert.assertEquals(2, result.size());
        Mockito.verify(jedis).zrangeByScoreWithScores(Matchers.anyString(), Matchers.anyLong(), Matchers.anyLong());
        Mockito.verify(jedis, Mockito.times(3)).hmget(Matchers.anyString(), Matchers.anyString(), Matchers.anyString());
    }

    @Test
    public void testGetActivitiesPerformedInLastNMonthsForGoal() throws Exception {
        ChartService chartService = new ChartService();
        JedisExecutionService jedisExecutionService = new JedisExecutionService();
        JedisPool jedisPool = Mockito.mock(JedisPool.class);
        jedisExecutionService.jedisPool = jedisPool;
        chartService.jedisExecutionService = jedisExecutionService;

        Jedis jedis = Mockito.mock(Jedis.class);
        Mockito.when(jedisPool.getResource()).thenReturn(jedis);
        Goal goal = Goal.of(Long.valueOf(3), 100L, GoalUnit.KM);

        double yesterdayTimestamp = DateUtils.timestampInDouble(1);
        Set<Tuple> activitiesWithScore = Sets.newHashSet(new Tuple("1", yesterdayTimestamp), new Tuple("2", yesterdayTimestamp), new Tuple("3", DateUtils.timestampInDouble(2)));
        Mockito.when(jedis.zrangeByScoreWithScores(Matchers.anyString(), Matchers.anyLong(), Matchers.anyLong())).thenReturn(activitiesWithScore);

        Mockito.when(jedis.hget(Matchers.anyString(), Matchers.anyString())).thenReturn("1000");
        Mockito.when(jedis.hget(Matchers.anyString(), Matchers.anyString())).thenReturn("1000");
        Mockito.when(jedis.hget(Matchers.anyString(), Matchers.anyString())).thenReturn("1000");

        Map<String, Double> result = chartService.getActivitiesPerformedInLastNMonthsForGoal("ajy.deb.5", goal, 30);
        Assert.assertEquals(2, result.size());
        System.out.printf("Result %s", result);
        Mockito.verify(jedis).zrangeByScoreWithScores(Matchers.anyString(), Matchers.anyLong(), Matchers.anyLong());
        Mockito.verify(jedis, Mockito.times(3)).hget(Matchers.anyString(), Matchers.anyString());

    }
}
