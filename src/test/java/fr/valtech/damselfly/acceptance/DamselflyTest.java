package fr.valtech.damselfly.acceptance;

import static org.fest.assertions.Assertions.assertThat;
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

import fr.valtech.damselfly.domain.model.ConfigData;

public class DamselflyTest {
	private String url = "http://localhost:9999/damselfly/apps/dracar/dev/{key}";

	private RestTemplate restTemplate = new RestTemplate();

	private static Server server;

	@Test
	public void getValueByKey() {
		ConfigData cd = restTemplate.getForObject(url, ConfigData.class,
				"databaseUrl");
		assertThat(cd.getValue()).isEqualTo("jdbc:mysql://localhost/mydb");
		assertThat(cd.getKey()).isEqualTo("databaseUrl");
	}

	@Test
	public void putValueByKey() {
		System.out.println(this.getClass().getCanonicalName()+" Client: put");
		ConfigData newCD = new ConfigData(new Long(1), "dracar", "dev",
				"databaseUrl", "toto");
		restTemplate.put(url + "/{value}", newCD, "databaseUrl", "putr");

		ConfigData cd = restTemplate.getForObject(url, ConfigData.class,
				"databaseUrl");
		// TODO TEST A REVOIR QD LA MAJ SERA CODEE
		assertThat(cd.getValue()).isEqualTo("jdbc:mysql://localhost/mydb");
	}

	@Test
	public void create() {
		System.out.println(this.getClass().getCanonicalName()+" Client: post");
		url = "http://localhost:9999/damselfly/apps/dracar/dev/{key}/{value}";
		ConfigData cd = new ConfigData(new Long(1), "dracar", "dev", "langue",
				"français");
		ConfigData newCD = restTemplate.postForObject(url, cd,
				ConfigData.class, cd.getKey(), cd.getValue());
		assertThat(newCD.getValue()).isNotNull();
		assertThat(newCD.getValue()).isEqualTo("français");
	}

	// @Test
	// public void deleteValueByKey() {
	// String value = restTemplate.getForObject(url, String.class,
	// "databaseUrl");
	// assertThat(value).isNotEmpty();
	//
	// System.out.println(restTemplate.delete(url,""));
	//
	// value = restTemplate.getForObject(url, String.class, "databaseUrl");
	//
	// assertThat(value).isNotEqualTo("jdbc:mysql://localhost/mydb");
	// }

	@Test
	public void returns404WhenNoValueFoundForKey() {
		try {
			restTemplate.getForObject(url, String.class, "mykey");
			fail("Key is found");
		} catch (HttpClientErrorException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			fail("this isn't the expected exception: " + e.getMessage());
		}
	}

	@BeforeClass
	public static void init() throws Exception {
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

	@AfterClass
	public static void destroy() throws Exception {
		server.stop();
	}

}
