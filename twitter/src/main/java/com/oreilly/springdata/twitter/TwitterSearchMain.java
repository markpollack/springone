package com.oreilly.springdata.twitter;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
public class TwitterSearchMain {

	private static final Log log = LogFactory.getLog(TwitterSearchMain.class);

	public static void main(String[] args) throws Exception {

	    Server server = new Server(8095);
	    Context context = new Context(server, "/", Context.SESSIONS);

	    DispatcherServlet dispatcherServlet = new DispatcherServlet();
	    dispatcherServlet
	        .setContextConfigLocation("classpath:/META-INF/spring/search-application-context.xml");

	    ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
	    context.addServlet(servletHolder, "/*");

	    server.start();
	    server.join();
	    //createWebContainerWithWebXML();
	}

	/**
	 * @throws Exception
	 * @throws InterruptedException
	 */
	private static void createWebContainerWithWebXML() throws Exception,
			InterruptedException {
		String webappDirLocation = "src/main/webapp/";
	    
	    Server server = new Server(8080);
	    WebAppContext root = new WebAppContext();
	 
	    root.setContextPath("/");
	    root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
	    root.setResourceBase(webappDirLocation);
	 
	    root.setParentLoaderPriority(true);
	 
	    server.setHandler(root);
	 
	    server.start();
	    server.join();
	}
}
