package fr.valtech.damselfly.acceptance;

import static org.fest.assertions.Assertions.*;
import static org.junit.Assert.fail;


import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class DamselflyTest {


	
	private String url = "http://localhost:9999/damselfly/apps/dracar/dev/{key}";
	
	private RestTemplate restTemplate = new RestTemplate();

	
	private static Server server;
	
	
	@Test
	public void getValueByKey() {
		
		String value = restTemplate.getForObject(url, String.class, "databaseUrl");
		assertThat(value).isEqualTo("jdbc:mysql://localhost/mydb");
		
	}
	
	
	@Test
	public void returns404WhenNoValueFoundForKey() {
		try {
			restTemplate.getForObject(url, String.class, "mykey");
			fail("Key is found");
		} catch (HttpClientErrorException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		} catch(Exception e) {
			fail("this isn't the expected exception: "+e.getMessage());
		}
	}
	
	
	
	@BeforeClass public static void init() throws Exception {
		server = new Server();
		Connector connector = new SelectChannelConnector();
		connector.setPort(9999);
		connector.setHost("127.0.0.1");
		server.addConnector(connector);

		WebAppContext wac = new WebAppContext();
		wac.setContextPath("/damselfly");
		wac.setWar("./src/main/webapp");
		server.setHandler(wac);
		server.setStopAtShutdown(true);
		server.start();

	}
	
	@AfterClass public static void destroy() throws Exception {
		server.stop();
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
