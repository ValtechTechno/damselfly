package fr.valtech.damselfly.service;

public interface GraphService {

	void removeNode(long i);

	void updateNodeProperty(long i, String key, String newValue);

	String retrieveNodeProperty(long i, String key);

}