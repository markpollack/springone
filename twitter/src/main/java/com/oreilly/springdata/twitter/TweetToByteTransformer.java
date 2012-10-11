package com.oreilly.springdata.twitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

public class TweetToByteTransformer {

	/**
	 * Serialize a Tweet into byte[] (json format) ready to be saved to hdfs.
	 * 
	 * @return content to write to hdfs
	 */
	public byte[] serialize(EntityAwareTweet tweet) {
		JsonGenerator jg = null;
		try {
			// main init
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			JsonFactory jf = new JsonFactory();
			jg = jf.createJsonGenerator(bos, JsonEncoding.UTF8);

			jg.writeStartObject();
			jg.writeStringField("from", tweet.getFrom());
			jg.writeStringField("text", tweet.getText());
			jg.writeStringField("date", tweet.getDateTime());
			writeArray(jg, "tags", tweet.getTags());
			writeArray(jg, "mentions", tweet.getMentions());
			writeArray(jg, "tickerSymbols", tweet.getTickerSymbols());
		
			jg.writeEndObject();
			jg.writeRaw("\n");
			jg.close();
			return bos.toByteArray();
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Cannot serialize tweets", ex);
		}
		finally {
			if (jg != null) {
				try {
					jg.close();
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void writeArray(JsonGenerator jg, String fieldName, List<String> tags) throws IOException {
		jg.writeArrayFieldStart(fieldName);
		for (String string : tags) {
			jg.writeString(string);
		}
		jg.writeEndArray();
	}

}
