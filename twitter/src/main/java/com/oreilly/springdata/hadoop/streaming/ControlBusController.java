package com.oreilly.springdata.hadoop.streaming;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlBusController {
	private static final Log log = LogFactory.getLog(ControlBusController.class);
	
	@Autowired
	private MessageChannel inOperationChannel;	
	
	@Autowired
	private HashtagCounterAnalysis hashtagCounter;
	
	@RequestMapping("/admin")
	public @ResponseBody String simple(@RequestBody String message) {
		
		Message<String> operation = MessageBuilder.withPayload(message).build();
		MessagingTemplate template = new MessagingTemplate();
		template.setReceiveTimeout(1000);
		Message response = template.sendAndReceive(inOperationChannel, operation);
		return response != null ? response.getPayload().toString() : null;
		
	}
	
	@RequestMapping("/admin/hadoop/count")
	public @ResponseBody String count() {
		log.info("running cascading job");
		hashtagCounter.run();
		log.info("done running cascading job");
		return "ok";
	}
}
