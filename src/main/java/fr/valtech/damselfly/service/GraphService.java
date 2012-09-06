package fr.valtech.damselfly.service;

import org.neo4j.graphdb.Node;

import fr.valtech.damselfly.domain.model.ConfigData;

public interface GraphService {

	void removeNode(long i);

	void updateNodeProperty(long i, String key, String newValue);

	String retrieveNodeProperty(long i, String key);

	ConfigData retrieveNodeProperty(String appname, String env, String key);

	void createGraph();

	String getPathdNeoFile();

	void setPathdNeoFile(String pathdNeoFile);

	Node getReferenceNode();

	int countENV();

	int countENV_CONFIGURATION();

	int countGLOBAL_CONGIGURATION();
}