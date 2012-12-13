package transbit.tbits.plugin;

import java.util.Hashtable;
import java.util.Properties;

/**
 * 
 * @author sourabh
 * 
 * Object Class for a Plugin. It carries the Meta Data for a Plugin
 */
public class PluginModule {
	private String name;
	private String gwtName;
	private String author;
	private String version;
	private String description;
	private String activator;
	private String dbUpgradesDir;
	private Hashtable<String, String> dependencies;
	
	public static String AUTHOR = "Author";
	public static String NAME = "Name";
	public static String DESCRIPTION = "Description";
	public static String ACTIVATOR = "Activator";
	public static String VERSION = "Version";
	public static String DEPENDS = "Depends";
	
	public PluginModule() {
	}
	
	public void generateValuesFromProps(Properties props){
		name = props.getProperty(NAME);
		author = props.getProperty(AUTHOR);
		version = props.getProperty(VERSION);
		description = props.getProperty(DESCRIPTION);
		activator = props.getProperty(ACTIVATOR);
		
		dependencies = new Hashtable<String, String>();
		String dependencyString = props.getProperty(DEPENDS);
		if(dependencyString != null){
			String[] modules = dependencyString.split(",");
			for(String module : modules){
				String[] moduleDependency = module.split(":");
				if(moduleDependency.length == 2){
					String moduleName = moduleDependency[0];
					String version = moduleDependency[1];
					dependencies.put(moduleName, version);
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGwtName() {
		return gwtName;
	}

	public void setGwtName(String gwtName) {
		this.gwtName = gwtName;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Hashtable<String, String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Hashtable<String, String> dependencies) {
		this.dependencies = dependencies;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setActivator(String activator) {
		this.activator = activator;
	}

	public String getActivator() {
		return activator;
	}

	public void setDBUpgradesDir(String dbUpgradesDir) {
		this.dbUpgradesDir = dbUpgradesDir;
	}

	public String getDBUpgradesDir() {
		return dbUpgradesDir;
	}
}
