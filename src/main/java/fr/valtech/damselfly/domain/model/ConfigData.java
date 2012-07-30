package fr.valtech.damselfly.domain.model;

public class ConfigData {

	private Long id;
	private String application;
	private String envrionment;
	private String key;
	private String value;

	public ConfigData() {
		super();
	}
	public ConfigData(Long id, String application, String envrionment,
			String key, String value) {
		super();
		this.id = id;
		this.application = application;
		this.envrionment = envrionment;
		this.key = key;
		this.value = value;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getEnvrionment() {
		return envrionment;
	}
	public void setEnvrionment(String envrionment) {
		this.envrionment = envrionment;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
}
