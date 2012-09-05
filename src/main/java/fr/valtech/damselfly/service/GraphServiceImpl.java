package fr.valtech.damselfly.service;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.Traversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.valtech.damselfly.domain.model.ConfigData;
import fr.valtech.damselfly.domain.model.EnumRelationship;

@Service
public class GraphServiceImpl implements GraphService {

	/**
	 * Properties of nodes. Some nodes have some and others some others else.
	 */
	public final static String REFERENCE = "reference";
	public final static String APP = "application";
	public final static String ENVIRONMENT = "environment";
	public final static String KEY = "key";
	public final static String VALUE = "value";
	private String pathdNeoFile;

	private final GraphDatabaseService graphDb;
	private static Index<Node> nodeIndex;
	
	static final Logger logger = LoggerFactory.getLogger(GraphServiceImpl.class);


	public GraphDatabaseService getGraphDb() {
		return graphDb;
	}

	/**
	 * Create new or open existing DB.
	 * 
	 * @param storeDir
	 *            location of DB files
	 */
	@Autowired
	public GraphServiceImpl(@Qualifier("pathdNeoFile") final String storeDir) {
		logger.debug("*** storeDir -> "+storeDir);

		deleteFileOrDirectory(new File(storeDir));
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
		registerShutdownHook(graphDb);
		nodeIndex = graphDb.index().forNodes("nodes");
		createGraph();
	}

	public Node createReference(Map<String, String> propertyMap) {
		Transaction tx = graphDb.beginTx();
		Node nd = null;
		try {
			nd = createNode(propertyMap);
			RelationshipType e = findRelationship("REFERENCE");
			getReferenceNode().createRelationshipTo(nd, e);

			tx.success();
		} finally {
			tx.finish();
		}
		return nd;
	}

	public Node createNode(final Map<String, String> propertyMap) {

		Transaction tx = graphDb.beginTx();
		Node n1 = graphDb.createNode();
		try {

			for (String key : propertyMap.keySet()) {
				n1.setProperty(key, propertyMap.get(key));
				nodeIndex.add(n1, key, propertyMap.get(key));
			}

			tx.success();
		} finally {
			tx.finish();
		}
		return n1;
	}

	public void addRelationship(long idNodeOrigin,
			Map<String, String> propertyNodeDestination, String relationship) {

		Transaction tx = graphDb.beginTx();
		RelationshipType e = findRelationship(relationship);

		try {
			Node n = createNode(propertyNodeDestination);
			findById(idNodeOrigin).createRelationshipTo(n, e);
			tx.success();
		} finally {
			tx.finish();
		}

	}

	public void addRelationship(long idNodeOrigin, long idNodeDestination,
			String relationship) {
		Transaction tx = graphDb.beginTx();
		RelationshipType e = findRelationship(relationship);
		try {
			findById(idNodeOrigin).createRelationshipTo(
					findById(idNodeDestination), e);
			tx.success();
		} finally {
			tx.finish();
		}
	}

	public void addRelationship(Map<String, String> propertyNodeOrigin,
			Map<String, String> propertyNodeDestination, String relationship) {
		Transaction tx = graphDb.beginTx();
		RelationshipType e = findRelationship(relationship);

		try {
			Node origin = createNode(propertyNodeOrigin);
			origin.createRelationshipTo(createNode(propertyNodeDestination), e);
			tx.success();
		} finally {
			tx.finish();
		}

	}

	private RelationshipType findRelationship(String relationship) {
		RelationshipType e = null;
		if (relationship.equals("REFERENCE")) {
			return EnumRelationship.ROOT;
		}
		if (relationship.equals("ENVIRONMENT")) {
			return EnumRelationship.ENV;
		}
		if (relationship.equals("CONFIGURATION")) {
			return EnumRelationship.ENV_CONFIGURATION;
		}
		if (relationship.equals("GLOBAL")) {
			return EnumRelationship.GLOBAL_CONFIGURATION;
		}
		return e;
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

	public Node getREFERENCENode() {
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

	public Traverser getENV_CONFIGURATION(final Node n) {
		TraversalDescription td = Traversal
				.description()
				.breadthFirst()
				.relationships(EnumRelationship.ENV, Direction.OUTGOING)
				.relationships(EnumRelationship.ENV_CONFIGURATION,
						Direction.OUTGOING)
				.evaluator(
						Evaluators
								.includeWhereLastRelationshipTypeIs(EnumRelationship.ENV_CONFIGURATION));
		return td.traverse(n);
	}

	public Traverser getGLOBAL_CONFIGURATION(final Node n) {

		// final ArrayList<RelationshipType> tt = new
		// ArrayList<RelationshipType>();
		// tt.add(EnumRelationship.ENV);
		// tt.add(EnumRelationship.GLOBAL_CONFIGURATION);
		//
		// TraversalDescription td1 = Traversal.description().evaluator(
		// new Evaluator() {
		// @Override
		// public Evaluation evaluate( Path path ) {
		// if (path.length() == 0) {
		// return Evaluation.EXCLUDE_AND_CONTINUE;
		// }
		// RelationshipType et = tt.get(path.length() - 1);
		// boolean isExpected = path.lastRelationship().isType(et);
		// boolean included = path.length()==tt.size() && isExpected;
		// boolean continued = path.length() < tt.size() && isExpected;
		// return Evaluation.of(included, continued);
		// }
		// });
		//
		// return td1.traverse(findById(1));
		//
		TraversalDescription td = Traversal
				.description()
				.breadthFirst()
				.relationships(EnumRelationship.ENV, Direction.OUTGOING)
				.relationships(EnumRelationship.GLOBAL_CONFIGURATION,
						Direction.OUTGOING)
				.evaluator(
						Evaluators.lastRelationshipTypeIs(
								Evaluation.INCLUDE_AND_CONTINUE,
								Evaluation.EXCLUDE_AND_CONTINUE,
								EnumRelationship.GLOBAL_CONFIGURATION));
		return td.traverse(n);
	}

	private String printENV() {
		Node neoNode = getREFERENCENode();
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

	public String printENV_CONFIGURATION() {
		Node neoNode = getREFERENCENode();
		int numberOfENV = 0;
		String output = neoNode.getProperty(APP) + "'s ENV_CONFIGURATION:\n";
		Traverser ENVTraverser = getENV_CONFIGURATION(neoNode);
		for (Path ENVPath : ENVTraverser) {
			output += "At depth " + ENVPath.length() + " => "
					+ ENVPath.endNode().getProperty(KEY) + "\n";
			numberOfENV++;
		}
		output += "Number of ENV_CONFIGURATION found: " + numberOfENV + "\n";
		return output;
	}

	public String printGLOBAL_CONFIGURATION() {
		Node neoNode = getREFERENCENode();
		int numberOfENV = 0;
		String output = neoNode.getProperty(APP) + "'s GLOBAL_CONFIGURATION:\n";
		Traverser ENVTraverser = getGLOBAL_CONFIGURATION(findById(4));
		for (Path ENVPath : ENVTraverser) {
			try {
				output += "At depth " + ENVPath.length() + " => "
						+ ENVPath.endNode().getProperty(KEY) + "\n";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			numberOfENV++;
		}
		output += "Number of GLOBAL_CONFIGURATION found: " + numberOfENV + "\n";
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

	public void parcourirGraphe(Node n) {
		for (Relationship r : n.getRelationships(EnumRelationship.ENV)) {
			for (Node nn : r.getNodes()) {
				for (Relationship tt : nn
						.getRelationships(EnumRelationship.GLOBAL_CONFIGURATION)) {
					System.out.println(tt.getStartNode().getId());
					if (tt.getEndNode().getId() == 4) {
						System.out.println("END "
								+ tt.getEndNode().getProperty(
										GraphServiceImpl.KEY));
						System.out.println("END "
								+ tt.getEndNode().getProperty(
										GraphServiceImpl.VALUE));
					}
					System.out.println(tt.getEndNode().getId());

				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.valtech.damselfly.service.GraphService#removeNode(long)
	 */
	@Override
	public void removeNode(long i) {
		Transaction tx = graphDb.beginTx();
		try {
			Node n = this.findById(i);
			for (Relationship rel : n.getRelationships(Direction.BOTH)) {
				rel.delete();
			}
			n.delete();
			tx.success();
		} finally {
			tx.finish();
		}
	}

	@Override
	public String retrieveNodeProperty(long i, String key) {
		Node n = this.findById(i);
		for (String k : n.getPropertyKeys()) {
			if (k.equals(GraphServiceImpl.KEY)) {
				if (key.equals(n.getProperty(GraphServiceImpl.KEY))) {
					return (String) n.getProperty(GraphServiceImpl.VALUE);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.valtech.damselfly.service.GraphService#updateNodeProperty(long,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void updateNodeProperty(long i, String key, String newValue) {
		Transaction tx = graphDb.beginTx();
		boolean update = false;
		try {
			Node n = this.findById(i);
			for (String k : n.getPropertyKeys()) {
				if (k.equals(GraphServiceImpl.KEY)) {
					if (key.equals(n.getProperty(GraphServiceImpl.KEY))) {
						n.setProperty(GraphServiceImpl.VALUE, newValue);
						update = true;
					}
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}
	}

	public HashMap<String, String> getNodeProperties(long i) {
		HashMap<String, String> m = new HashMap<String, String>();
		Node n = this.findById(i);
		int j = 0;
		for (String k : n.getPropertyKeys()) {
			if (k.equals(GraphServiceImpl.KEY)) {
				j++;
			}
		}
		String[] keys = new String[j];
		j = 0;
		for (String k : n.getPropertyKeys()) {
			if (k.equals(GraphServiceImpl.KEY)) {
				keys[j] = new String();
				keys[j++] = (String) n.getProperty(k);
			}
		}
		j = 0;
		for (String k : n.getPropertyKeys()) {
			if (k.equals(GraphServiceImpl.VALUE)) {
				m.put(keys[j++], (String) n.getProperty(k));
			}
		}
		return m;
	}

	@Override
	public ConfigData retrieveNodeProperty(String appname, String env,
			String key) {
		Node n = findByKey(appname, key);
		ConfigData cd = new ConfigData();
		cd.setApplication(appname);
		cd.setEnvrionment(env);
		cd.setId(n.getId());
		cd.setKey(key);
		cd.setValue(retrieveNodeProperty(n.getId(), key));
		return cd;
	}

	private Node findByKey(String appname, String key) {
		ExecutionEngine engine = new ExecutionEngine(graphDb);

		ExecutionResult result = engine.execute("START a=node(1) MATCH (a)-[:"
				+ EnumRelationship.ENV + "]->(b)-[:"
				+ EnumRelationship.ENV_CONFIGURATION + "]->(c) WHERE c.key=\""
				+ key + "\" and a.application=\"" + appname + "\" RETURN c");

		Iterator<Node> n_column = result.columnAs("c");

		// for (Map<String, Object> map : result) {
		// System.out.println(map);
		// }

		return IteratorUtil.asIterable(n_column).iterator().next();
	}

	private Node findByKeyGLOBAL(String appname, String key) {
		ExecutionEngine engine = new ExecutionEngine(graphDb);

		ExecutionResult result = engine.execute("START a=node(1) MATCH (a)-[:"
				+ EnumRelationship.ENV + "]->(b)-[:"
				+ EnumRelationship.GLOBAL_CONFIGURATION
				+ "]->(c) WHERE c.key=\"" + key + "\" and a.application=\""
				+ appname + "\" RETURN c");

		Iterator<Node> n_column = result.columnAs("c");

		// for (Map<String, Object> map : result) {
		// System.out.println(map);
		// }

		return IteratorUtil.asIterable(n_column).iterator().next();
	}

	@Override
	public void createGraph() {
		final HashMap<String, String> propertyMap = new HashMap<String, String>();

		propertyMap.put(GraphServiceImpl.APP, "dracar");
		// Node noeudDracar=gs.createNode(propertyMap);
		Node n = createReference(propertyMap);

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.ENVIRONMENT, "dev");
		addRelationship(n.getId(), propertyMap, "ENVIRONMENT");

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.KEY, "log4jpath");
		propertyMap.put(GraphServiceImpl.VALUE, "d:");
		addRelationship(2, propertyMap, "CONFIGURATION");

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.KEY, "databaseURL");
		propertyMap.put(GraphServiceImpl.VALUE, "jdbc DEV");
		addRelationship(2, propertyMap, "GLOBAL");

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.ENVIRONMENT, "prod");
		addRelationship(n.getId(), propertyMap, "ENVIRONMENT");

		propertyMap.clear();
		propertyMap.put(GraphServiceImpl.KEY, "databaseURL");
		propertyMap.put(GraphServiceImpl.VALUE, "jdbc PROD");
		addRelationship(5, propertyMap, "CONFIGURATION");

		addRelationship(5, 4, "GLOBAL");

	}

	public String getPathdNeoFile() {
		return pathdNeoFile;
	}

	public void setPathdNeoFile(String pathdNeoFile) {
		this.pathdNeoFile = pathdNeoFile;
	}

	@Override
	public Node getReferenceNode() {
		return graphDb.getReferenceNode();
	}

}
