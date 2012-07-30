package fr.valtech.damselfly.domain.model;

import java.util.List;

public interface Repository {

	ConfigData  create(ConfigData created);

	ConfigData  delete(Long id);

	List<ConfigData > findAll();

	List<ConfigData > findById(Long id);

	List<ConfigData > findByKey(String key);

	ConfigData  update(ConfigData updated);
}
