package com.oreilly.springdata.hadoop.streaming;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.srp.SrpConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

public class RedisTests {

	DateTimeFormatter dailyFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	DateTimeFormatter hourlyFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH");
	DateTimeFormatter minuteFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm");
	
	StringRedisTemplate stringRedisTemplate;

	public RedisTests() {
		JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
		connectionFactory.afterPropertiesSet();
		this.stringRedisTemplate = new StringRedisTemplate();		
		stringRedisTemplate.setConnectionFactory(connectionFactory);
		stringRedisTemplate.afterPropertiesSet();
	}
	
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
	public void testPastFewHours() {
		
		//StringRedisTemplate redisTemplate = createTemplate();
		DateTime today = new DateTime();
		/*
		getCountForHour(stringRedisTemplate, "obama", today);
		getCountForHour(stringRedisTemplate, "obama", today.minusHours(1));
		getCountForHour(stringRedisTemplate, "obama", today.minusHours(2));
		getCountForHour(stringRedisTemplate, "obama", today.minusHours(3));
		getCountForHour(stringRedisTemplate, "obama", today.minusHours(4));
		getCountForHour(stringRedisTemplate, "obama", today.minusHours(5));
		*/
		//int totalLast6 = getCountForLastXHours("obama", 5, today);
		
		getCountForHour(stringRedisTemplate, "romney", new DateTime());
		
		
	}
	
	
	public int getCountForLastXHours(String candidate, int hoursBackInTime, DateTime dateTime) {
		
		int totalCount = 0;
		for (int i=1; i<=hoursBackInTime; i++) {
			totalCount += getCountForHour(stringRedisTemplate, candidate, dateTime.minusHours(i));
		}			
		return totalCount;
	}

	/**
	 * @param redisTemplate
	 * @param today
	 */
	private int getCountForHour(StringRedisTemplate redisTemplate,  String candidate, DateTime today) {
		String lastHourHashKey = "hash:" + this.getHourlyKey(candidate, today);
		int totalCount = 0;
		if (redisTemplate.hasKey(lastHourHashKey)) {
			System.out.println(lastHourHashKey);
			Map<Object, Object> map = redisTemplate.opsForHash().entries(
					lastHourHashKey);

			for (Map.Entry<Object, Object> cursor : map.entrySet()) {
				System.out.println(cursor.getKey() + "," + cursor.getValue());
				totalCount += Double.valueOf(cursor.getValue().toString())
						.intValue();
			}
			System.out.println(lastHourHashKey + ", total count = "
					+ totalCount);
		} else {
			System.err.println("no key found for " + lastHourHashKey);
		}
		return totalCount;
	}

	@Test
	public void testDailyCountsCounters() {
		
		StringRedisTemplate redisTemplate = createTemplate();
		
		redisTemplate.opsForValue().set("tot:election:obama:2012-10-12", "300");
		redisTemplate.opsForValue().set("tot:election:romney:2012-10-12", "350");
		redisTemplate.opsForValue().set("tot:election:bieber:2012-10-12", "270");
	
		redisTemplate.opsForValue().set("tot:election:obama:2012-10-13", "150");
		redisTemplate.opsForValue().set("tot:election:romney:2012-10-13", "80");
		redisTemplate.opsForValue().set("tot:election:bieber:2012-10-13", "300");


		System.out.println("obama  10/12 - " + redisTemplate.opsForValue().get("tot:election:obama:2012-10-12"));
		System.out.println("romney 10/12 - " + redisTemplate.opsForValue().get("tot:election:romney:2012-10-12"));
		System.out.println("bieber 10/12 - " + redisTemplate.opsForValue().get("tot:election:bieber:2012-10-12"));

		System.out.println("obama  10/12 - " + redisTemplate.opsForValue().get("tot:election:obama:2012-10-13"));
		System.out.println("romney 10/12 - " + redisTemplate.opsForValue().get("tot:election:romney:2012-10-13"));
		System.out.println("bieber 10/12 - " + redisTemplate.opsForValue().get("tot:election:bieber:2012-10-13"));

		
		System.out.println("obama  10/14 - " + redisTemplate.opsForValue().get("tot:election:obama:2012-10-14"));
		System.out.println("romney 10/14 - " + redisTemplate.opsForValue().get("tot:election:romney:2012-10-14"));
		System.out.println("bieber 10/14 - " + redisTemplate.opsForValue().get("tot:election:bieber:2012-10-14"));


		
		redisTemplate.opsForValue().set("tot:election:obama:2012-10-15", "25");
		redisTemplate.opsForValue().set("tot:election:romney:2012-10-15", "25");
		redisTemplate.opsForValue().set("tot:election:bieber:2012-10-15", "25");
		System.out.println("obama  10/15 - " + redisTemplate.opsForValue().get("tot:election:obama:2012-10-15"));
		System.out.println("romney 10/15 - " + redisTemplate.opsForValue().get("tot:election:romney:2012-10-15"));
		System.out.println("bieber 10/15 - " + redisTemplate.opsForValue().get("tot:election:bieber:2012-10-15"));
		
		String dailyTotKey = "tot:" + this.getDailyKey("obama", new DateTime());
		
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
