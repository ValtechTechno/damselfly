package fr.valtech.damselfly.service;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;

import org.fest.assertions.Fail;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Traverser;

public class GraphServiceTest {
	private final String DB_PATH = "target/neo4j-hello-db";
	private static GraphService gs;

	@Before
	public void createRelationship() {

		gs = new GraphService(DB_PATH);

		final HashMap<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put(gs.APP, "dracar");

		try {
			gs.createRelationshipFromRoot(propertyMap);
		} catch (Exception e) {
			Fail.fail();
		}

		/*
		 * try {
		 * 
		 * Node n1 = graphDb.createNode(); n1.setProperty(APP, "dracar");
		 * nodeIndex.add(n1, APP, "dracar");
		 * 
		 * graphDb.getReferenceNode().createRelationshipTo(n1, RelTypes.ROOT);
		 * 
		 * Node n2 = graphDb.createNode(); n2.setProperty(ENV, "dev");
		 * nodeIndex.add(n2, ENV, "dev");
		 * 
		 * Node n3 = graphDb.createNode(); n3.setProperty(KEY, "databaseUrl");
		 * n3.setProperty(VALUE, "jdbc machin chose DEV");
		 * 
		 * Relationship rela = n1.createRelationshipTo(n2, RelTypes.ENV);
		 * Relationship relb = n2.createRelationshipTo(n3,
		 * RelTypes.ENV_CONFIGURATION);
		 * 
		 * rela.setProperty("dracar", "développement");
		 * 
		 * Node n4 = graphDb.createNode(); n4.setProperty(ENV, "prod");
		 * 
		 * Node n5 = graphDb.createNode(); n5.setProperty(KEY, "databaseUrl");
		 * n5.setProperty(VALUE, "jdbc machin chose PROD");
		 * 
		 * Relationship relc = n1.createRelationshipTo(n4, RelTypes.ENV);
		 * relc.setProperty("dracar", "production");
		 * 
		 * Relationship reld = n4.createRelationshipTo(n5,
		 * RelTypes.ENV_CONFIGURATION);
		 * 
		 * Node n6 = graphDb.createNode(); n6.setProperty(KEY, "clecommune");
		 * n6.setProperty(VALUE, "valeur commune");
		 * 
		 * Relationship rele = n1.createRelationshipTo(n6,
		 * RelTypes.GLOBAL_CONGIGURATION); rele.setProperty("dracar", "GLOBAL");
		 * 
		 * tx.success(); } catch (Exception e) { e.printStackTrace(); } finally
		 * { tx.finish(); }
		 */
	}

	@Test
	public void testalacon() {
		HashMap<String, String> propertyMap = new HashMap<String, String>();

		propertyMap.put(GraphService.ENVIRONMENT, "dev");

		gs.addRelationship(1, propertyMap, "ENVIRONMENT");
		assertThat(this.countENV()).isEqualTo(1);
	}

	public int countENV() {
		Node neoNode = gs.getENVNode();
		int numberOfENV = 0;
		Traverser ENVTraverser = gs.getENV(neoNode);
		for (Path ENVPath : ENVTraverser) {
			numberOfENV++;
		}
		return numberOfENV;
	}

}