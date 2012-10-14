package com.oreilly.springdata.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.oreilly.springdata.twitter.EntityAwareTweet;

public class RedisElectionHashtagCounter {

	private static final Log log = LogFactory.getLog(RedisElectionHashtagCounter.class);
	
	private StringRedisTemplate redisTemplate;
	private DateTimeFormatter dailyFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	private DateTimeFormatter hourlyFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH");
	private DateTimeFormatter minuteFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm");
	
	private static String OBAMA = "obama";
	private static String ROMNEY = "romney";
	private static String BIEBER = "bieber";
	
	public RedisElectionHashtagCounter(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public EntityAwareTweet count(EntityAwareTweet entityAwareTweet) {
		DateTime dateTime = new DateTime();
		for (String hashtag : entityAwareTweet.getTags()) {
			if (hashtag.equalsIgnoreCase("voteobama")) {
				updateCounters(OBAMA, dateTime);
			}
			if (hashtag.equalsIgnoreCase("voteromney")) {
				updateCounters(ROMNEY, dateTime);
			}
			if (hashtag.equalsIgnoreCase("votebieber")) {
				updateCounters(BIEBER, dateTime);
			}			
		}
		return entityAwareTweet;
	}

	private void updateCounters(String candidate, DateTime dateTime) {
		
		String dailyKey = getDailyKey(candidate, dateTime);
		String hourlyKey = getHourlyKey(candidate, dateTime);
		
		HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
		
		//hours in the day
		hashOps.putIfAbsent("hash:" + dailyKey, String.valueOf(dateTime.getHourOfDay()), "0");
		hashOps.increment("hash:" + dailyKey, String.valueOf(dateTime.getHourOfDay()), 1);
		
		//minutes in the hour
		hashOps.putIfAbsent("hash:" + hourlyKey, String.valueOf(dateTime.getMinuteOfHour()), "0");		
		hashOps.increment("hash:" + hourlyKey, String.valueOf(dateTime.getMinuteOfHour()), 1);
		
		//total for day
		ValueOperations<String, String> valueOps = redisTemplate.opsForValue();		
		redisTemplate.opsForValue().setIfAbsent("tot:" + dailyKey, "0");
		valueOps.increment("tot:" + dailyKey, 1);
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
