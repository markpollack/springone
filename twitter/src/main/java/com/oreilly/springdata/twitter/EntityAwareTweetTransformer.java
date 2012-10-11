package com.oreilly.springdata.twitter;

import org.springframework.social.twitter.api.Tweet;

public class EntityAwareTweetTransformer {

	public EntityAwareTweet process(Tweet tweet) {
		return new EntityAwareTweet(tweet);
	}
}
