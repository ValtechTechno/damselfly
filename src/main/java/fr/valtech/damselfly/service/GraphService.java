package fr.valtech.damselfly.service;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.Traversal;

import fr.valtech.damselfly.domain.model.EnumRelationship;

public class GraphService {

	public  final static String APP = "application";
	public  final static String ENVIRONMENT = "environment";
	public  final String KEY = "key";
	public  final String VALUE = "value";

	private final GraphDatabaseService graphDb;
	private static Index<Node> nodeIndex;

	public GraphDatabaseService getGraphDb() {
		return graphDb;
	}

	/**
	 * Create new or open existing DB.
	 * 
	 * @param storeDir
	 *            location of DB files
	 */
	public GraphService(final String storeDir) {
		deleteFileOrDirectory(new File(storeDir));
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);

		registerShutdownHook(graphDb);
		nodeIndex = graphDb.index().forNodes("nodes");
	}

	public void createRelationshipFromRoot(Map<String, String> propertyMap)
			throws Exception {
		Transaction tx = graphDb.beginTx();
		try {
			Node n1 = createNode(propertyMap);
			graphDb.getReferenceNode().createRelationshipTo(n1,
					EnumRelationship.ROOT);
			tx.success();
		} finally {
			tx.finish();
		}

	}

	private Node createNode(final Map<String, String> propertyMap) {
		Node n1 = graphDb.createNode();
		for (String key : propertyMap.keySet()) {
			n1.setProperty(key, propertyMap.get(key));
			nodeIndex.add(n1, key, propertyMap.get(key));
		}
		return n1;
	}

	public void addRelationship(long idNodeOrigin,
			Map<String, String> propertyNodeDestination, String relationship) {
		Transaction tx = graphDb.beginTx();
		RelationshipType e = null;

		if (relationship.equals("ENVIRONMENT")) {
			e = EnumRelationship.ENV;
		}
		// TO BE COMPLETED !!
		try {
			findById(idNodeOrigin).createRelationshipTo(
					createNode(propertyNodeDestination), e);
			tx.success();
		} finally {
			tx.finish();
		}

	}

	private Node findById(long id) {
		ExecutionEngine engine = new ExecutionEngine(graphDb);
		ExecutionResult result = engine.execute("start n=node(" + id
				+ ") return n");
		Iterator<Node> n_column = result.columnAs("n");

		return IteratorUtil.asIterable(n_column).iterator().next();
	}

	public static void deleteFileOrDirectory(final File file) {
		if (!file.exists()) {
			return;
		}

		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				deleteFileOrDirectory(child);
			}
		} else {
			file.delete();
		}
	}

	public static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	public Node getENVNode() {
		return graphDb
				.getReferenceNode()
				.getSingleRelationship(EnumRelationship.ROOT,
						Direction.OUTGOING).getEndNode();
	}

	public Traverser getENV(final Node n) {
		TraversalDescription td = Traversal.description().breadthFirst()
				.relationships(EnumRelationship.ENV, Direction.OUTGOING)
				.evaluator(Evaluators.excludeStartPosition());
		return td.traverse(n);
	}

	private String printENV() {
		Node neoNode = getENVNode();
		int numberOfENV = 0;
		String output = neoNode.getProperty(APP) + "'s ENV:\n";
		Traverser ENVTraverser = getENV(neoNode);
		for (Path ENVPath : ENVTraverser) {
			output += "At depth " + ENVPath.length() + " => "
					+ ENVPath.endNode().getProperty(ENVIRONMENT) + "\n";
			numberOfENV++;
		}
		output += "Number of ENV found: " + numberOfENV + "\n";
		return output;
	}
	/*
	 * void removeData() { Transaction tx = graphDb.beginTx(); try { // START
	 * SNIPPET: removingData // let's remove the data
	 * firstNode.getSingleRelationship( RelTypes.KNOWS, Direction.OUTGOING
	 * ).delete(); firstNode.delete(); secondNode.delete(); // END SNIPPET:
	 * removingData
	 * 
	 * tx.success(); } finally { tx.finish(); } }
	 */

}
