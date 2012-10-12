package com.oreilly.springdata.redis;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.oreilly.springdata.twitter.EntityAwareTweet;

public class RedisTweetEntityCounter implements InitializingBean {

	private final StringRedisTemplate redisTemplate;

	public RedisTweetEntityCounter(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void afterPropertiesSet() {
		this.redisTemplate.afterPropertiesSet();
	}

	public void count(EntityAwareTweet entityAwareTweet) {
		for (String hashtag : entityAwareTweet.getTags()) {
			this.redisTemplate.boundZSetOps("hashtags").incrementScore(hashtag, 1.0);
		}
		for (String ticker : entityAwareTweet.getTickerSymbols()) {
			this.redisTemplate.boundZSetOps("tickers").incrementScore(ticker, 1.0);
		}
		for (String mention : entityAwareTweet.getMentions()) {
			this.redisTemplate.boundZSetOps("mentions").incrementScore(mention, 1.0);
		}
	}

}
