package com.oreilly.springdata.hadoop.streaming;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.springframework.data.redis.connection.srp.SrpConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

public class RedisTests {

	DateTimeFormatter dailyFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	DateTimeFormatter hourlyFormatter = DateTimeFormat
			.forPattern("yyyy-MM-dd-HH");
	DateTimeFormatter minuteFormatter = DateTimeFormat
			.forPattern("yyyy-MM-dd-HH-mm");

	@Test
	public void test() {
		StringRedisTemplate redisTemplate = createTemplate();

		Set<TypedTuple<String>> results = redisTemplate.opsForZSet()
				.rangeByScoreWithScores("hashtags", 0, 100);

		TreeSet<NameCountData> convertedResults = new TreeSet<NameCountData>();
		for (TypedTuple<String> typedTuple : results) {
			if (NumberUtils.isNumber(typedTuple.getValue())) {
				// in Right to left language, the order is reversed.
				convertedResults.add(new NameCountData(typedTuple.getScore()
						.toString(), Integer.valueOf(typedTuple.getValue())));
			} else {
				convertedResults.add(new NameCountData(typedTuple.getValue(),
						Integer.valueOf(typedTuple.getScore().intValue())));
			}
		}
		for (NameCountData nameCountData : convertedResults) {
			System.out.println(nameCountData);
		}

	}

	/**
	 * @return
	 */
	private StringRedisTemplate createTemplate() {
		SrpConnectionFactory connectionFactory = new SrpConnectionFactory();
		connectionFactory.afterPropertiesSet();
		StringRedisTemplate redisTemplate = new StringRedisTemplate();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@Test
	public void hourlyCounters() {
		StringRedisTemplate redisTemplate = createTemplate();

		String candidate = "obama";
		DateTime dateTime = new DateTime();
		String dailyKey = getDailyKey(candidate, dateTime);
		String hourlyKey = getHourlyKey(candidate, dateTime);

		
		HashOperations<String, String, String> hashOps = redisTemplate	.opsForHash();
	
		hashOps.putIfAbsent(dailyKey, String.valueOf(dateTime.getHourOfDay()),
				"0");
		hashOps.increment(dailyKey, String.valueOf(dateTime.getHourOfDay()), 1);

		hashOps.putIfAbsent(hourlyKey, String.valueOf(dateTime.getHourOfDay()),
				"0");
		hashOps.increment(hourlyKey,
				String.valueOf(dateTime.getMinuteOfHour()), 1);

		ValueOperations<String, String> valueOps = redisTemplate.opsForValue();

		redisTemplate.opsForValue().setIfAbsent("tot:" + dailyKey, "0");
		valueOps.increment("tot:" + dailyKey, 1);
		// valueOps.increment(getHourlyKey(candidate, dateTime), 1);
		// valueOps.increment(getMinuteKey(candidate, dateTime), 1);

		/*
		 * ValueOperations<String, String> valueOps =
		 * redisTemplate.opsForValue();
		 * 
		 * String key = getDailyKey("obama", new DateTime());
		 * valueOps.setIfAbsent(key, "0"); valueOps.increment(key, 1);
		 */

	}

	@Test
	public void dailyCounters() {
		System.out.println(getDailyKey("obama", new DateTime()));

	}

	public String getDailyKey(String candidate, DateTime dateTime) {
		return "election:" + candidate + ":" + dailyFormatter.print(dateTime);
	}

	public String getHourlyKey(String candidate, DateTime dateTime) {
		return "election:" + candidate + ":" + hourlyFormatter.print(dateTime);
	}

	public String getMinuteKey(String candidate, DateTime dateTime) {
		return "election:" + candidate + ":" + minuteFormatter.print(dateTime);
	}
}
