package transbit.tbits.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PluginHelper {
	
	public static final String GWT_XML	=	".gwt.xml";
	public static final String PLUGIN_MANIFEST = "PLUGIN.MF";
	public static final String DB_UPGRADES_DIR = "_db";
	
	public static List<PluginModule> getPluginModules(File pluginFolder){
		return getPluginModules(pluginFolder, "tbits");
	}
	
	/**
	 * @param pluginFolder
	 * @return Returns Plugins found in a directory
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<PluginModule> getPluginModules(File pluginFolder, String module){
		List<PluginModule> plugins = new ArrayList<PluginModule>();
		if(pluginFolder != null && pluginFolder.exists() && pluginFolder.isDirectory()){
			List<File> pluginFileFolders = findPluginFileFolders(pluginFolder, module);
			for(File pluginFileFolder : pluginFileFolders){
				if(pluginFileFolder != null && pluginFileFolder.exists()){
					PluginModule plugin = new PluginModule();
					File[] files = pluginFileFolder.listFiles();
					for(File file : files){
						if(!file.isDirectory() && module != null && file.getName().endsWith(module + GWT_XML)){
							String pluginFolderPath = pluginFolder.getPath();
							String pluginFileFolderPath = pluginFileFolder.getPath();
							String diff = pluginFileFolderPath.replace(pluginFolderPath + "/", "");
							diff = diff.replace(pluginFolderPath + "\\", ""); // for windows
							String packageName = diff.replace('/', '.');
							packageName = packageName.replace('\\', '.'); // for windows
							String pluginFileName = packageName + "." + file.getName();
							String gwtName = pluginFileName.substring(0, pluginFileName.indexOf(GWT_XML));
							plugin.setGwtName(gwtName);
						}
						if(file.isDirectory() && file.getName().equals(DB_UPGRADES_DIR)){
							plugin.setDBUpgradesDir(file.getPath());
						}
						if(file.isFile() && file.getName().equals(PLUGIN_MANIFEST)){
							try {
								Properties props = readManifest(file);
								plugin.generateValuesFromProps(props);
								plugins.add(plugin);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
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
	
	/**
	 * Reads manifest file for a plugin
	 * @param manifestFile
	 * @return The read properties
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Properties readManifest(File manifestFile) throws FileNotFoundException, IOException{
		Properties props = new Properties();
		props.load(new FileInputStream(manifestFile));
		return props;
	}
}
