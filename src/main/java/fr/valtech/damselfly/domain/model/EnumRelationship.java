package fr.valtech.damselfly.domain.model;

import org.neo4j.graphdb.RelationshipType;

public enum EnumRelationship implements RelationshipType {
	ROOT, ENV, ENV_CONFIGURATION, GLOBAL_CONGIGURATION;
}
