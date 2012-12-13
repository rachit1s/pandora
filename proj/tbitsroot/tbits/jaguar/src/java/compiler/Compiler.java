package compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Creates the src/java/com/tbitsGlobal/jaguar/plugins.gwt.xml file containning all the modules found in plugin directory
 * A module is identified if it has a suffix <module name>.gwt.xml.
 * @author sandeepgiri
 *
 */
public class Compiler {
	private static final String GWT_XML	=	".gwt.xml";
	public static final String REBUS_TAG_MODULE = "<module>";
    public static final String REBUS_TAG_MODULE_END = "</module>";
    public static final String REBUS_BYTE_ENC = "US-ASCII";
    
	private static String PLUGINS_FOLDER 		= "war/WEB-INF/classes";
	private static String DEST_FOLDER 			= "./src/java/com/tbitsGlobal/jaguar";
	private static String PLUGINS_MODULE_FILE 	= DEST_FOLDER + "/" + "plugins" + GWT_XML;
	private static String MODULE				= "tbits";
	
	private static HashMap<String, List<String>> depencencyMap = new HashMap<String, List<String>>();
	
	/**
	 * Syntax: compiler <plugin folder> <main module|tbits> <dest folder>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting compiler");
		if(args.length == 3){
			PLUGINS_FOLDER = args[0];
			System.out.println("Plugins Folder : " + PLUGINS_FOLDER);
			MODULE = args[1];
			System.out.println("Module : " + MODULE);
			DEST_FOLDER = args[2];
			System.out.println("Destination : " + DEST_FOLDER);
			
			
			PLUGINS_MODULE_FILE = DEST_FOLDER + "/" + "plugins" + GWT_XML;
		}
		
		String dependencyError = "";
		File pluginsDir = new File(PLUGINS_FOLDER);
		List<String> pluginModules = getPluginModules(pluginsDir, MODULE);
		
		for(String plugin : depencencyMap.keySet()){
			List<String> dependencyList = depencencyMap.get(plugin);
			for(String dependency : dependencyList){
				if(!pluginModules.contains(dependency)){
					dependencyError += "\nDependency Error... Plugin : " + plugin + " depends on module : " + dependency;
				}
			}
		}
		
		if(!dependencyError.equals(""))
			throw new IllegalArgumentException(dependencyError);
		
		try {
			File pluginsFile = new File(PLUGINS_MODULE_FILE);
			FileOutputStream fos = new FileOutputStream(pluginsFile);
			fos.getChannel().truncate(0);
			
			fos.write(REBUS_TAG_MODULE.getBytes(REBUS_BYTE_ENC));
			if(pluginModules != null){
				for(String pluginModule : pluginModules){
//					String module = pluginModule.substring(0, pluginModule.indexOf(GWT_XML));
					fos.write(("<inherits name=\"" + pluginModule + "\"/>").getBytes(REBUS_BYTE_ENC));
					System.out.println("Found plugin for " + MODULE + " : " + pluginModule);
				}
			}
			fos.write(REBUS_TAG_MODULE_END.getBytes(REBUS_BYTE_ENC));
            fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param pluginFolder
	 * @return Returns Plugins found in a directory
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<String> getPluginModules(File pluginFolder, String module){
		List<String> plugins = new ArrayList<String>();
		if(pluginFolder != null && pluginFolder.exists() && pluginFolder.isDirectory()){
			List<File> pluginFileFolders = findPluginFileFolders(pluginFolder, module);
			for(File pluginFileFolder : pluginFileFolders){
				if(pluginFileFolder != null && pluginFileFolder.exists()){
					File[] files = pluginFileFolder.listFiles();
					for(File file : files){
						if(!file.isDirectory() && file.getName().endsWith(module + GWT_XML)){
							String pluginFolderPath = pluginFolder.getPath();
							String pluginFileFolderPath = pluginFileFolder.getPath();
							String diff = pluginFileFolderPath.replace(pluginFolderPath + "/", "");
							diff = diff.replace(pluginFolderPath + "\\", ""); // for windows
							String packageName = diff.replace('/', '.');
							packageName = packageName.replace('\\', '.'); // for windows
							String pluginFileName = packageName + "." + file.getName();
							String gwtName = pluginFileName.substring(0, pluginFileName.indexOf(GWT_XML));
							
							plugins.add(gwtName);
						}
						
					}
				}
			}
		}
		return plugins;
	}
	
	/**
	 * Finds folders contianing .gwt.xml files for a plugin
	 * @param pluginFolder
	 * @return
	 */
	private static List<File> findPluginFileFolders(File pluginFolder, String module){
		List<File> pluginFileFolders = new ArrayList<File>();
		if(hasPluginFile(pluginFolder, module))
			pluginFileFolders.add(pluginFolder);
		
		if(pluginFolder != null && pluginFolder.exists() && pluginFolder.isDirectory()){
			File[] files = pluginFolder.listFiles();
			for(File file : files){
				if(file.isDirectory()){
					List<File> pluginFileFolderList = findPluginFileFolders(file, module);
					if(pluginFileFolderList != null)
						pluginFileFolders.addAll(pluginFileFolderList);
				}
			}
		}
		return pluginFileFolders;
	}
	
	/**
	 * determines if a Directory has a .gwt.xml file for a plugin
	 * @param aDir
	 * @return True if it is there
	 */
	private static boolean hasPluginFile(File aDir, String module){
		if(aDir != null && aDir.exists() && aDir.isDirectory()){
			File[] files = aDir.listFiles();
			for(File file : files){
				if(!file.isDirectory() && file.getName().endsWith(module + GWT_XML)){
					return true;
				}
			}
		}
		return false;
	}
	
	private static void calculateDependency(String moduleXMLName , File file){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			File pluginFile = new File(file.getParent() + "/plugins.xml");
			if(pluginFile.exists()){
				//Using factory get an instance of document builder
				DocumentBuilder db = factory.newDocumentBuilder();
	
				//parse using builder to get DOM representation of the XML file
				Document doc = db.parse(pluginFile);
				NodeList moduleList = doc.getElementsByTagName("plugin");
				if(moduleList != null && moduleList.getLength() > 0){
					Node moduleNode = moduleList.item(0);
					if(moduleNode.getNodeType() == Node.ELEMENT_NODE){
						Element element = (Element) moduleNode;
						NodeList inheritList = element.getElementsByTagName("inherits");
						if(inheritList != null){
							for(int i = 0; i < inheritList.getLength(); i++){
								Node inherit = inheritList.item(i);
//								System.out.println("Dependency : " + inherit.getAttributes().getNamedItem("name").getNodeValue());
								String dependency = inherit.getAttributes().getNamedItem("name").getNodeValue();
								if(!depencencyMap.containsKey(moduleXMLName)){
									depencencyMap.put(moduleXMLName, new ArrayList<String>());
								}
								if(!depencencyMap.get(moduleXMLName).contains(dependency)){
									depencencyMap.get(moduleXMLName).add(dependency);
								}
							}
						}
					}
				}
			}
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
