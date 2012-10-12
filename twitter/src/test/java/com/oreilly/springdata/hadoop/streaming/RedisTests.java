package com.oreilly.springdata.hadoop.streaming;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;
import org.springframework.data.redis.connection.srp.SrpConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

public class RedisTests {

	@Test
	public void test() {
		SrpConnectionFactory connectionFactory = new SrpConnectionFactory();
		connectionFactory.afterPropertiesSet();
		StringRedisTemplate redisTemplate = new StringRedisTemplate();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.afterPropertiesSet();
		
		Set<TypedTuple<String>> results = redisTemplate.opsForZSet().rangeByScoreWithScores("hashtags", 0, 100);
		
		TreeSet<NameCountData> convertedResults = new TreeSet<NameCountData>();
		for (TypedTuple<String> typedTuple : results) {
			if (NumberUtils.isNumber(typedTuple.getValue())) {
				// in Right to left language, the order is reversed.
				convertedResults.add(new NameCountData(typedTuple.getScore().toString(), Integer.valueOf(typedTuple.getValue())));
			} else {
				convertedResults.add(new NameCountData(typedTuple.getValue(), Integer.valueOf(typedTuple.getScore().intValue())));
			}
		}
		for (NameCountData nameCountData : convertedResults) {
			System.out.println(nameCountData);
		}
		
	}
}
