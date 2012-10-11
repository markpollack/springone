package com.oreilly.springdata.twitter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.util.StringUtils;

/**
 * @author Jon Brisbin <jbrisbin@vmware.com>
 */
@SuppressWarnings("serial")
public class EntityAwareTweet implements Serializable {

	private final String from;

	private final String text;

	private final List<String> tags = new ArrayList<String>();

	private final List<String> mentions = new ArrayList<String>();

	private final List<String> tickerSymbols = new ArrayList<String>();

	private String dateTime;

	public EntityAwareTweet(Tweet tweet) {
		this.dateTime = ISODateTimeFormat.dateTime().print(new DateTime());
		this.from = tweet.getFromUser();
		this.text = tweet.getText();
		String[] tokens = StringUtils.tokenizeToStringArray(text, " \r\t\n");
		for (String token : tokens) {
			if (token.startsWith("#")) {
				tags.add(token.substring(1).trim());
			}
			else if (token.startsWith("@")) {
				mentions.add(token.substring(1).trim());
			}
			else if (token.startsWith("$")) {
				tickerSymbols.add(token.substring(1).trim());
			}
		}
	}

	public String getFrom() {
		return from;
	}

	public String getText() {
		return text;
	}

	public List<String> getTags() {
		return tags;
	}

	public List<String> getMentions() {
		return mentions;
	}

	public List<String> getTickerSymbols() {
		return tickerSymbols;
	}
	
	public String getDateTime() {
		return dateTime;
	}

	@Override
	public String toString() {
		return "Tweet{" + "from='" + from + '\'' + ", text='" + text + '\''
				+ ", tags=" + tags + ", mentions=" + mentions
				+ ", tickerSymbols=" + tickerSymbols  
				+ ", dateTime = " + dateTime + 	'}';
	}

}
