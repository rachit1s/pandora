package transbit.tbits.addons;

/**
 * 
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.CompoundEnumeration;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * This class loads the addon jar file and resolves the library jar files inside the addon jar file.
 */
public class AddonLoader extends ClassLoader 
{
	public static final String ADDON_AUTHOR = "Addon-Author";
	public static final String ADDON_DB_CONFIG_FILE="Addon-DB-Config-File";
	public static final String ADDON_ENTRYPOINT = "Addon-EntryPointClass";

	public static final String ADDON_HELP_FILE = "Addon-Help-File";

	public static final String ADDON_NAME = "Addon-Name";
	public static final String ADDON_DESCRIPTION = "Addon-Description";

	public static final String ADDON_VERSION = "Addon-Version";

	/**
	 * This Name will will be used in the MANIFEST.MF for the addon jar file to mention relative path of other library jars inside the addon Jar 
	 */
	public static final String LIBRARY_PATH = "Addon-Library-Path";

	private static final Logger logger =	Logger.getLogger("com.tbitsglobal.addon");

	private String author;
	private String dbConfigFile;
	private String description;
	private String entryPointClassName;
	private String helpFile;
	/**
	 * the provided URL for the jar of addon
	 */
	private URL jarURL;

	/**
	 * list of urls of the library inside the addon jar
	 */
	private ArrayList<URL> libURLs = new ArrayList<URL>();

	private String name;

	/**
	 * internally uses URLClassLoader to load all the classes.
	 */
	private URLClassLoader ucl ;

	private String version;

	/** Utility method to read from input and write to output
	 * @param is
	 * @param fos
	 * @throws IOException 
	 */
	public static void readWrite(InputStream is, OutputStream fos) throws IOException 
	{
		// note even after using available method to find the size that can be read.
		// it is not guaranteed that the maximum size is this only. So we have to 
		// loop through while to make it work.
		byte[] buff = new byte[is.available()];
		int len = -1 ;
		while( ( len = is.read(buff)) != -1 )
		{
			fos.write(buff, 0, len);
		}
	}

	/**
	 * loads the jar from URL, and parent class loader as the classloader of this class
	 * @param jarURL
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	AddonLoader(URL jarURL) throws AddonLoaderException
	{
		this(jarURL,AddonLoader.class.getClassLoader());
	}
	
	/**
	 * specify the parent classloader and the jar file URL for addon
	 * @param jarURL
	 * @param parent
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	AddonLoader(URL jarURL, ClassLoader parent) throws AddonLoaderException
	{
		super(parent);
		this.jarURL = jarURL;
		loadAddOn();
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return the dbScriptFolder
	 */
	public String getDbConfigFile() {
		return dbConfigFile;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return the entryPointClassName
	 */
	public String getEntryPointClassName() {
		return entryPointClassName;
	}
	/**
	 * @return the helpFile
	 */
	public String getHelpFile() {
		return helpFile;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * It loads the classes inside the addon jar and the classes inside the library jars inside the addon jar
	 * @param jarURL2
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	private void loadAddOn() throws AddonLoaderException
	{
		resolveAddOn();
		
		try
		{
			URL[] urls = new URL[1 + libURLs.size()];
			urls[0] = jarURL;
			for( int i = 1 ; i <= libURLs.size() ; i++ )
				urls[i] = libURLs.get(i-1);
			
			ucl = new URLClassLoader(urls, this);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE,"Exception while loading the addon.",e);
			throw new AddonLoaderException("Exception while loading the addon.",e);
		}
		
//		testReadAllResources();
	}
	
	/**
	 * overridden method 
	 */
	public Class<?> loadClass(String name) throws ClassNotFoundException 
	{
		return ucl.loadClass(name);
	}
	
	/**
	 * overridden method for finding resources through the ucl
	 */
	@Override
	protected URL findResource(String name) {
		return ucl.findResource(name);
	}
	
	/**
	 * overridden method to find the resources using ucl
	 */
	@Override
	 protected Enumeration<URL> findResources(String name) throws IOException {
			return ucl.findResources(name);
	 }
	
	
	/**
	 * resolves the library jar files inside the addon jar file
	 * next it reads all the required manifest entries.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	private void resolveAddOn() throws AddonLoaderException
	{
		try
		{
			URL newURL = new URL("jar", "", jarURL + "!/");
			JarURLConnection uc = (JarURLConnection) newURL.openConnection();
			Manifest manifest = uc.getManifest();
			if( null == manifest )
				logger.info("Manifest Not found for jar : " + newURL);
			
			Attributes mainAttributes = manifest.getMainAttributes();
			logger.info(mainAttributes.toString());
			if( null != mainAttributes )
			{
				String libraries = mainAttributes.getValue(LIBRARY_PATH);
				logger.info(libraries);
				if( null != libraries )
				{
					String [] libraryEntries = libraries.split(" ");
					// get all the jars in the library and add then to the urls
					for( String lib : libraryEntries)
					{
						URL urlEntry = new URL("jar", "", jarURL + "!/" + lib);
						System.out.println("urlEntry = " + urlEntry);
						JarURLConnection jarEntryConnection = (JarURLConnection) urlEntry.openConnection();
						InputStream is = jarEntryConnection.getInputStream() ;
	
						File tempJarFile = File.createTempFile("addonJar", ".jar");
						FileOutputStream fos = new FileOutputStream(tempJarFile);
						tempJarFile.deleteOnExit();
						
						readWrite(is,fos);
						is.close();
						fos.close();
						
						URL libURL = new URL("file","localhost",tempJarFile.getAbsolutePath());
						
						this.libURLs.add(libURL);
					}
				}
			}
			
			// read all the required manifest entries
			this.setName(mainAttributes.getValue(ADDON_NAME));
			this.setVersion(mainAttributes.getValue(ADDON_VERSION));
			this.setEntryPointClassName(mainAttributes.getValue(ADDON_ENTRYPOINT));
			this.setAuthor(mainAttributes.getValue(ADDON_AUTHOR));
			this.setHelpFile(mainAttributes.getValue(ADDON_HELP_FILE));
			this.setDescription(mainAttributes.getValue(ADDON_DESCRIPTION));
			
			// read db config attribute
			this.setDbConfigFile(mainAttributes.getValue(ADDON_DB_CONFIG_FILE));
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while resolving the addon jar",e);
			throw new AddonLoaderException("Exception occured while resolving the addon jar", e);
		}
	}
	
	@Deprecated
	private void resolveAddOn1() throws IOException, URISyntaxException 
	{
		URL newURL = new URL("jar", "", jarURL + "!/");
		JarURLConnection uc = (JarURLConnection) newURL.openConnection();
		Manifest manifest = uc.getManifest();
		if( null == manifest )
			logger.info("Manifest Not found for jar : " + newURL);
		
		Attributes mainAttributes = manifest.getMainAttributes();
		logger.info(mainAttributes.toString());
		if( null != mainAttributes )
		{
			String libraries = mainAttributes.getValue(LIBRARY_PATH);
			logger.info(libraries);
			if( null != libraries )
			{
				String [] libraryEntries = libraries.split(" ");
				// get all the jars in the library and add then to the urls
				for( String lib : libraryEntries)
				{
					URL urlEntry = new URL("jar", "", jarURL + "!/" + lib);
					this.libURLs.add(urlEntry);
				}
			}
		}
	}
	
	/**
	 * @param author the author to set
	 */
	private void setAuthor(String author) 
	{
		if( null != author )
			author = author.trim();
		
		this.author = author;
	}
	
	/**
	 * @param dbConfigFile the dbScriptFolder to set
	 */
	private void setDbConfigFile(String dbConfigFile) 
	{
		if(null != dbConfigFile )
			dbConfigFile = dbConfigFile.trim();
		
		this.dbConfigFile = dbConfigFile;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) 
	{
		if( null != description )
			description = description.trim();
		
		this.description = description;
	}
	
	/**
	 * @param entryPointClassName the entryPointClassName to set
	 */
	private void setEntryPointClassName(String entryPointClassName) 
	{
		if( null != entryPointClassName )
			entryPointClassName = entryPointClassName.trim();
		
		this.entryPointClassName = entryPointClassName;
	}

	/**
	 * @param helpFile the helpFile to set
	 */
	private void setHelpFile(String helpFile) 
	{
		if( null != helpFile )
			helpFile = helpFile.trim();
		
		this.helpFile = helpFile;
	}

	/**
	 * @param name the name to set
	 */
	private void setName(String name) 
	{
		if( null != name )
			name = name.trim();
		
		this.name = name;
	}

	/**
	 * @param version the version to set
	 */
	private void setVersion(String version) 
	{
		if( null != version )
			version = version.trim();
		
		this.version = version;
	}
}
