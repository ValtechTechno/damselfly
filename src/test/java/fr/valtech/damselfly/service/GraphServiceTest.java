package fr.valtech.damselfly.service;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Traverser;

import fr.valtech.damselfly.domain.model.ConfigData;

public class GraphServiceTest {
	private static final String DB_PATH = "/neo4j-hello-db-test";
	private static GraphServiceImpl gs = new GraphServiceImpl(DB_PATH);
	private static Node noeudApp1;

	@BeforeClass
	public static void createRelationships() {

		final HashMap<String, String> propertyMap = new HashMap<String, String>();

		
		propertyMap.put(GraphServiceImpl.APP, "dracar");
		noeudApp1 = gs.createReference(propertyMap);

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.ENVIRONMENT, "dev");
		gs.addRelationship(noeudApp1.getId(), propertyMap, "ENVIRONMENT");

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
		gs.addRelationship(noeudApp1.getId(), propertyMap, "ENVIRONMENT");

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.KEY, "databaseURL");
		propertyMap.put(GraphServiceImpl.VALUE, "jdbc PROD");
		gs.addRelationship(5, propertyMap, "CONFIGURATION");

		gs.addRelationship(5, 4, "GLOBAL");

		gs.parcourirGraphe(noeudApp1);
	}

	@Test
	public void compter() {
		assertThat(this.countENV()).isEqualTo(4);
		assertThat(this.countENV_CONFIGURATION()).isEqualTo(2);
		// assertThat(this.countGLOBAL_CONGIGURATION()).isEqualTo(2);
	}

	@Test
	public void ajoutDunNoeudDepuisReferenceApplication() {
		final HashMap<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put(GraphServiceImpl.ENVIRONMENT, "preprod");
		gs.addRelationship(noeudApp1.getId(), propertyMap, "ENVIRONMENT");
		assertThat(this.countENV()).isEqualTo(3);
	}

	@Test
	public void suppressionDunNoeudDepuisReference() {
		final HashMap<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put(GraphServiceImpl.ENVIRONMENT, "preprod2");
		gs.addRelationship(noeudApp1.getId(), propertyMap, "ENVIRONMENT");

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

	@Test
	public void rechercherUneCle() {
		ConfigData cd = gs.retrieveNodeProperty("dracar", "dev", "log4jpath");
		assertThat(cd).isNotNull();
		assertThat(cd.getId()).isEqualTo(3);
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