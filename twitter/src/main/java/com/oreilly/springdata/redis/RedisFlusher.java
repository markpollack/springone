package com.oreilly.springdata.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

public class RedisFlusher {

	private static final Log log = LogFactory.getLog(RedisFlusher.class);

	private final StringRedisTemplate redisTemplate;

	public RedisFlusher(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;		
	}

	@Scheduled(cron="0 0/1 * * * ?")
	public void flush() {
		log.info("Flushing redis keys, hashtags, tickers, mentions");
		this.redisTemplate.delete("hashtags");
		this.redisTemplate.delete("tickers");
		this.redisTemplate.delete("mentions");
	}

}
