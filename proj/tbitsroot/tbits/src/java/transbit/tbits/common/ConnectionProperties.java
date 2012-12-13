package transbit.tbits.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConnectionProperties 
{
	// Object that will hold all the DB Connection Pooling related Properties.
	public static Properties ourDBPoolProperties;
	
	static
	{
		loadConnectionProperties() ;
	}

	private static void loadConnectionProperties() 
	{
		try
		{
			ourDBPoolProperties = new Properties() ;
			String dbPoolFileName = Configuration.getDBPoolPropertiesFileName();

			if ((dbPoolFileName != null)
					&& (dbPoolFileName.trim().equals("") == false)) 
			{
				String filePath = "etc/" + dbPoolFileName;
		        File   file     = Configuration.findPath(filePath);
//				File file = new File(ConnectionProperties.class.getResource(filePath).getFile());
		        if (file == null) {
		            System.out.println("[ " + ConnectionProperties.class.getName() + " ] " + "WARNING: File not found: " + dbPoolFileName);
		            return;
		        }
	            FileInputStream fis = new FileInputStream(file);
				ourDBPoolProperties.load(fis);
			} else {
				System.out.println("[ " + ConnectionProperties.class.getName()
						+ " ] " + "WARNING: Name of the connection "
						+ "pooling properties file is not provided.");
	
				if (Configuration.ourIsEnvInitialized == false) {
					System.out.println("[ " + ConnectionProperties.class.getName()
							+ " ] " + "WARNING: Initialize the "
							+ "tbits-commons library properly.");
				}
			}
		}
		catch( FileNotFoundException e)
		{
			System.out.println("Exception while loading the db property file.");
			e.printStackTrace() ;			
		} catch (IOException e) {
			System.out.println("Exception while loading the properties from db property file.");
			e.printStackTrace();
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
		}
		
	}

	public static Properties getDBPoolProperties() 
	{	
		return ourDBPoolProperties;
	}

	/**
	 * This method returns the value for given key in the dbpool properties.
	 *
	 * @param aKey  Key whose value is needed.
	 *
	 * @return Value for this key in the properties.
	 *
	 * @throws IllegalArgumentException Incase the key is not found in the
	 *                                  properties.
	 */
	public static String getDBPoolProperty(String aKey) throws IllegalArgumentException {
	    String aValue = "";
	
	    if (ourDBPoolProperties.containsKey(aKey) == true) {
	        aValue = ourDBPoolProperties.getProperty(aKey);
	    } else {
	        throw new IllegalArgumentException("DBPool Property Key not found: " + aKey);
	    }
	
	    return aValue;
	}

	/**
	 * This method sets the value of given key in the DBPool property
	 * list and returns the old value.
	 *
	 * @param aKey     Key of the property.
	 * @param aValue   Value of the property.
	 *
	 * @return Old Value of the property or null if the key is a new one.
	 */
	public static String setDBPoolProperty(String aKey, String aValue) {
	    String oldValue = null;
	
	    // If key is either null or empty leave it and return.
	    if ((aKey == null) || (aKey.trim().equals("") == true)) {
	        return oldValue;
	    }
	
	    // if the value is null, set value to empty string.
	    if (aValue == null) {
	        aValue = "";
	    }
	
	    oldValue = (String) ourDBPoolProperties.setProperty(aKey, aValue);
	
	    return oldValue;
	}

}
