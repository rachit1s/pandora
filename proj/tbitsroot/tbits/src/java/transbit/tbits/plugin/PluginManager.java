package transbit.tbits.plugin;

import java.io.File;
import java.util.ArrayList;

import transbit.tbits.api.IPostRule;
import transbit.tbits.api.IRule;
import transbit.tbits.common.Configuration;

public class PluginManager {
	
	static PluginManager instance = null;
	ClassLoader cl = null;
	File dir = null;
	File jaguarDir = null;
	Boolean jaguarPluginsLoaded=false;
	private PluginManager() {
		this.dir = Configuration.findPath("plugins");
		this.jaguarDir = Configuration.findPath("webapps/WEB-INF/classes");
		cl = new PluginClassLoader( this.getClass().getClassLoader(), dir);
		
		loadPlugins();
//		loadJaguarPlugins(jaguarDir, null);
	}
	
	private void loadPlugins()
	{
		
		if(dir == null)
		{
			System.out.println("plugin dir not found");
			return;
		}
		System.out.println("Plugin: using the folder: " + dir.getAbsolutePath());	
		loadDir(dir, null);
	}
	
	//TODO: individually marking whether plugin is loaded
	public synchronized void loadJaguarPlugins(){
		if(jaguarPluginsLoaded==false)
		loadJaguarPlugins(jaguarDir, null);
	}
	
	public void loadJaguarPlugins(File aDir, String basePkg){
		if (aDir.exists() && aDir.isDirectory()) {
			File[] files = aDir.listFiles();
			for (int i=0; i<files.length; i++) {
				try {
					String newPkg = "";
					if(basePkg == null)
						newPkg = "";
					else 
						newPkg = basePkg + ".";
					File f = files[i];
					if(f.isDirectory()){
						newPkg += f.getName();
						loadJaguarPlugins(f, newPkg);
					}else{
						String fName = f.getName();
						
						if (!fName.equals("Activator.class"))
							continue;
						
						String className = newPkg + fName.substring(0, fName.indexOf("."));
						Class c = Class.forName(className);
						((IActivator)c.newInstance()).activate();
						System.out.println("Jaguar Plugin: Loaded class: "+ c.getCanonicalName());
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
			}
		}
	  jaguarPluginsLoaded=true;
	}
	
	private void loadDir(File aDir, String basePkg)
	{
		if (aDir.exists() && aDir.isDirectory()) {
			// we'll only load classes directly in this directory -
			// no sub directories, and no classes in packages are recognized
			File[] files = aDir.listFiles();
			for (int i=0; i<files.length; i++) {
				try {
					String newPkg;
					if(basePkg == null)
						newPkg = "";
					else 
						newPkg = basePkg + ".";
					
					File f = files[i];
					if(f.isDirectory())
					{
						
						newPkg += f.getName();
						loadDir(f, newPkg);
					}
					else
					{
						String fName = f.getName();
//						System.out.println("fName:" + fName);
						// only consider files ending in ".class"
						if (! fName.endsWith(".class"))
							continue;
						
						String className = newPkg + fName.substring(0, fName.indexOf("."));
						Class c = cl.loadClass(className);
						if(c != null){
							System.out.println("Plugin: Loaded class: "+ c.getName());//CanonicalName());
							try{
								if(c.getSuperclass().equals(TbitsRemoteServiceServlet.class))
									Class.forName(c.getName(), true, c.getClassLoader());
							}catch(Exception ex){
								System.out.println("Plugin: Could not load class: "+ c.getCanonicalName());
							}
							allPlugins.add(c);
						}else{
							System.out.println("Unable to load the file :" + fName);
						}
					}
				} catch (Throwable ex) {
					System.err.println("File " + files[i] + " does not contain a valid PluginFunction class.");
					ex.printStackTrace();
				}
			}
		}
	}
	ArrayList<Class> allPlugins = new ArrayList<Class>();
	
	public ArrayList<Class> findPluginsByInterface(String interfaceName)
	{
		ArrayList<Class> list = new ArrayList<Class>();
		for(Class c: allPlugins)
		{
			Class[] intf = c.getInterfaces();
			for (int j=0; j<intf.length; j++) {
				if (intf[j].getName().equals(interfaceName)) {
					System.out.println("PLugin: name of The correct interface: " + c.getName());
					list.add(c);
				}
			}
		}
		return list;
	}
	
	public Class findPluginsByClassName(String className)
	{
		for(Class c: allPlugins)
		{
			if(c.getName().equals(className))
				return c;
		}
		return null;
	}
	
	public synchronized static PluginManager getInstance()
	{
		if(instance == null)
			instance = new PluginManager();
		return instance;
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		PluginManager rPm = PluginManager.getInstance();
		rPm.loadPlugins();
		rPm.loadJaguarPlugins();
		
		System.out.println("The following IRules are available: ");		
		for(Class c:rPm.findPluginsByInterface(IRule.class.getName()))
		{
			System.out.println(c.getCanonicalName());
		}
		System.out.println("The following IPostRules are available: ");		
		for(Class c:rPm.findPluginsByInterface(IPostRule.class.getName()))
		{
			System.out.println(c.getCanonicalName());
		}
	}
}
