package fr.valtech.damselfly.service;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Traverser;

public class GraphServiceTest {
	private static final String DB_PATH = "target/neo4j-hello-db";
	private static GraphServiceImpl gs = new GraphServiceImpl(DB_PATH);

	@BeforeClass
	public static void createRelationships() {

		final HashMap<String, String> propertyMap = new HashMap<String, String>();

		propertyMap.put(GraphServiceImpl.APP, "dracar");
		Node n = gs.createReference(propertyMap);

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.ENVIRONMENT, "dev");
		gs.addRelationship(n.getId(), propertyMap, "ENVIRONMENT");

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.KEY, "log4jpath");
		propertyMap.put(GraphServiceImpl.VALUE, "d:");
		gs.addRelationship(2, propertyMap, "CONFIGURATION");

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.KEY, "databaseURL");
		propertyMap.put(GraphServiceImpl.VALUE, "jdbc DEV");
		gs.addRelationship(2, propertyMap, "GLOBAL");

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.ENVIRONMENT, "prod");
		gs.addRelationship(n.getId(), propertyMap, "ENVIRONMENT");

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.KEY, "databaseURL");
		propertyMap.put(GraphServiceImpl.VALUE, "jdbc PROD");
		gs.addRelationship(5, propertyMap, "CONFIGURATION");

		gs.addRelationship(5, 4, "GLOBAL");

		gs.parcourirGraphe(n);
	}

	@Test
	public void compter() {
		assertThat(this.countENV()).isEqualTo(4);
		assertThat(this.countENV_CONFIGURATION()).isEqualTo(2);
		// assertThat(this.countGLOBAL_CONGIGURATION()).isEqualTo(2);
	}

	@Test
	public void ajoutDunNoeudDepuisReference() {
		final HashMap<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put(GraphServiceImpl.ENVIRONMENT, "preprod");
		gs.addRelationship(1, propertyMap, "ENVIRONMENT");
		assertThat(this.countENV()).isEqualTo(3);
	}

	@Test
	public void suppressionDunNoeudDepuisReference() {
		final HashMap<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put(GraphServiceImpl.ENVIRONMENT, "preprod2");
		gs.addRelationship(1, propertyMap, "ENVIRONMENT");

		assertThat(this.countENV()).isEqualTo(4);
		gs.removeNode(7);
		assertThat(this.countENV()).isEqualTo(3);
	}

	@Test
	public void modifieUneProprieteDunNoeudENV_CONFIGURATION() {
		String key = "log4jpath", newValue = "/logs";
		gs.updateNodeProperty(3, key, newValue);
		final HashMap<String, String> propertyMap = gs.getNodeProperties(3);
		for (String v : propertyMap.keySet()) {
			assertThat(propertyMap.get(v)).isEqualTo(newValue);
		}
	}

	public int countENV() {
		Node neoNode = gs.getREFERENCENode();
		int numberOf = 0;
		Traverser t = gs.getENV(neoNode);
		for (Path p : t) {
			numberOf++;
		}
		return numberOf;
	}

	public int countENV_CONFIGURATION() {

		System.out.println(gs.printENV_CONFIGURATION());

		Node neoNode = gs.getREFERENCENode();
		int numberOf = 0;
		Traverser t = gs.getENV_CONFIGURATION(neoNode);
		for (Path p : t) {
			numberOf++;
		}
		return numberOf;
	}

	public int countGLOBAL_CONGIGURATION() {

		System.out.println(gs.printGLOBAL_CONFIGURATION());

		Node neoNode = gs.getREFERENCENode();
		int numberOf = 0;
		Traverser t = gs.getGLOBAL_CONFIGURATION(neoNode);
		for (Path p : t) {
			System.out.println("----------");
			for (Node n : p.nodes()) {
				System.out.println(this.getClass().getCanonicalName()
						+ " CHEMIN GLOBAL_CONGIGURATION Noeud " + n.getId());
			}
			numberOf++;
		}
		return numberOf;
	}
}