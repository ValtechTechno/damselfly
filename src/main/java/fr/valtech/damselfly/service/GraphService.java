package fr.valtech.damselfly.service;

import fr.valtech.damselfly.domain.model.ConfigData;

public interface GraphService {

	void removeNode(long i);

	void updateNodeProperty(long i, String key, String newValue);

	String retrieveNodeProperty(long i, String key);

	ConfigData retrieveNodeProperty(String appname, String env, String key);

	void createGraph();

	String getPathdNeoFile();

	void setPathdNeoFile(String pathdNeoFile);
}