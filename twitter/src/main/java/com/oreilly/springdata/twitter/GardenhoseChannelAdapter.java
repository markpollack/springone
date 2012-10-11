package com.oreilly.springdata.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.social.support.URIBuilder;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

public class GardenhoseChannelAdapter extends MessageProducerSupport {

	private static final String API_URL_BASE = "https://stream.twitter.com/1/";

	private static final LinkedMultiValueMap<String, String> EMPTY_PARAMETERS = new LinkedMultiValueMap<String, String>();


	private final Twitter twitter;

	private final AtomicBoolean running = new AtomicBoolean(false);

	private final Object monitor = new Object();


	public GardenhoseChannelAdapter(Twitter twitter) {
		this.twitter = twitter;
	}


	@Override
	 public String getComponentType() {
		return "twitter:gardenhose-channel-adapter";
	}

	@Override
	protected void doStart() {
		synchronized (this.monitor) {
			if (this.running.get()) {
				// already running
				return;
			}
			this.running.set(true);
			StreamReadingTask task = new StreamReadingTask();
			TaskScheduler scheduler = getTaskScheduler();
			if (scheduler != null) {
				scheduler.schedule(task, new Date());
			}
			else {
				Executor executor = Executors.newSingleThreadExecutor();
				executor.execute(task);
			}
		}
	}

	@Override
	protected void doStop() {
		this.running.set(false);
	}

	private URI buildUri(String path) {
		return URIBuilder.fromUri(API_URL_BASE + path).queryParams(EMPTY_PARAMETERS).build();
	}

	private class StreamReadingTask implements Runnable {
		public void run() {
			RestTemplate restTemplate = (RestTemplate) new DirectFieldAccessor(twitter).getPropertyValue("restTemplate");
			while (running.get()) {
				try {
					readStream(restTemplate);
				}
				catch (Exception e) {
					logger.warn("Exception occurred while reading stream; restarting", e);
				}
			}
		}

		private void readStream(RestTemplate restTemplate) {
			restTemplate.execute(buildUri("statuses/sample.json"), HttpMethod.GET, new RequestCallback() {
				public void doWithRequest(ClientHttpRequest request) throws IOException {
				}
			},
			new ResponseExtractor<String>() {
				public String extractData(ClientHttpResponse response) throws IOException {
					InputStream inputStream = response.getBody();
					LineNumberReader reader = new LineNumberReader(new InputStreamReader(inputStream));
					while (running.get()) {
						String line = reader.readLine();
						if (!StringUtils.hasText(line)) {
							break;
						}
						sendMessage(MessageBuilder.withPayload(line).build());
					}
					return null;
				}
			});
		}
	}

}
