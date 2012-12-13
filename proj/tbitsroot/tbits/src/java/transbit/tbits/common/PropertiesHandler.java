/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/**
 * PropertiesHandler.java
 *
 * $Header:
 *
 */
package transbit.tbits.common;

//~--- JDK imports ------------------------------------------------------------

//Current Package imports
// Java Imports.
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the system-specific and application-specific
 * properties and provides methods to read(accessor methods) and
 * change(mutator methods) them.
 * <ul>
 * <li>The system-specific properties are read from the <code>Properties</code>
 * object obtained from <code>System.getProperties()</code>.</li>
 * <li>The application-specific properties are read from the following files:
 * <ul>
 * <li> properties file specified through <code>app.propFile</code> system
 * property passed from command-line.
 * <li> authentication file specified through <code>app.authFile</code> system
 * property passed from command-line.
 * </ul>
 * These files are searched for in <code>APP_HOME/etc</code> directory.
 * </li>
 * </ul>
 *
 * <p>
 * if <code>app.authFile</code> system property is not set, then it is
 * assumed that the database authentication related properties are present
 * in the file specified through <code>app.propFile</code> system property.
 * </p>
 *
 *
 *
 * @author  : Vaibhav,Abhishek
 * @version : $Id: $
 * @see transbit.tbits.common.Configuration
 */
public class PropertiesHandler {

    private static final String TRANSBIT_TBITS_TRANSMITTAL = "transbit.tbits.transmittal";
	// Constants to represent the type of properties object.
    public static final int PROP_ALL            = 0;
    public static final int PROP_SYSTEM         = 1;
    public static final int PROP_LOGGING        = 3;
    public static final int PROP_DB_CON_POOLING = 4;
    public static final int PROP_APPLICATION    = 2;
//    public static final int PROP_APPLICATION_DB = 5;
    
    //Constants related to Table tbits_Properties
    public static final String DB_PROP_TABLE_NAME = "tbits_properties";
    public static final String DB_PROP_TABLE_COL_NAME = "name";
    public static final String DB_PROP_TABLE_COL_VALUE = "value";
    
    //Constants related to Table log4j_conf
    public static final String LOG4J_TABLE_NAME = "log4j_conf";
    public static final String LOG4J_TABLE_COL_NAME = "name";
    public static final String LOG4J_TABLE_COL_VALUE = "value";

    // Properties object that will hold all the TBits properties.
    private static Properties ourAppProperties;

    //Properties object that will hold all the TBits properties present in database
 //   private static Properties ourAppDbProperties;
    
    // Properties object that will hold all the Logging properties.
    private static Properties ourLogProperties;

    // Properties object that will hold all the System properties.
    private static Properties ourSysProperties;

    //~--- static initializers ------------------------------------------------
    public static class MailProperties{

		public static final String MAX_EMAIL_ATT_SIZE_PROP_NAME = "transbit.tbits.maximumEmailAttachmentSizeInBytes";
		public static final String AllowAutoReplyFrom_WithoutNotification = "AllowAutoReplyFrom_WithoutNotification";
		public static final String AllowAutoReplyFrom_WithNotification = "AllowAutoReplyFrom_WithNotification";
		public static String AUTH;
        public static boolean isDebugSession = false;
		public static String SMTP_LOGIN;
		public static String SMTP_PASSWORD;
		public static int SMTP_PORT;
		public static String SMTP_SERVER;
		public static String SMTP_PROTOCOL;
		public static String ENABLE_OUTGOING;
		public static String COMMON_FROM_ADDRESS;
		public static String OVERWRITE_FROM_ADDRESS;
//		public static String MAX_EMAIL_ATTACHEMENT_SIZE_IN_BYTES = "1024" ;
		public static TransbitAuthenticator tA;
		
    	
		public static void reloadMailProperties(){    	
        	AUTH =  PropertiesHandler.getProperty("transbit.tbits.smtp.authenticate");
        	if((AUTH == null) ||(AUTH.length() == 0))
        	{
        		AUTH = "y";
        	}
        	SMTP_LOGIN =  PropertiesHandler.getProperty("transbit.tbits.smtp.login");
        	if((AUTH.toLowerCase() == "y") && ((SMTP_LOGIN == null) || (SMTP_LOGIN.length() == 0)))
        	{
        		System.out.println("Login must be specified if authentication is set to 'y'");
        	}
        	SMTP_PASSWORD =  PropertiesHandler.getProperty("transbit.tbits.smtp.password");
        	
        	SMTP_PORT = 25;
        	String smtpPort = PropertiesHandler.getProperty("transbit.tbits.smtp.port");
        	if((smtpPort == null) ||(smtpPort.length() == 0))
        	{
        		System.out.println("transbit.tbits.smtp.port unspecified. Setting it to 25");
        	}
        	else
        	{
        		try
        		{
        			SMTP_PORT = Integer.parseInt(smtpPort);
        		}
        		catch(Exception exp)
        		{
        			System.out.println("Wrong value for transbit.tbits.smtp.port specified. Setting it to 25");
        		}
        	}
        	
        	SMTP_SERVER =  PropertiesHandler.getProperty("transbit.tbits.smtp.server");
        	SMTP_PROTOCOL = PropertiesHandler.getProperty("transbit.tbits.smtp.protocol");
        	if((SMTP_PROTOCOL == null) ||(SMTP_PROTOCOL.length() == 0))
        	{
        		System.out.println("transbit.tbits.smtp.protocol unspecified. Setting it to smtp");
        		SMTP_PROTOCOL = "smtp";
        	}
        	String isDebugStr = PropertiesHandler.getProperty("transbit.tbits.smtp.isdebug");
        	try
        	{
        		isDebugSession = Boolean.getBoolean(isDebugStr);
        	}
        	catch(Exception exp)
        	{
        		System.out.println("Wrong value of transbit.tbits.smtp.isdebug. Assuming false.");
        	}
        	
        	try
        	{
        	ENABLE_OUTGOING=PropertiesHandler.getProperty("transbit.tbits.mail.enableoutgoing");
        	}
        	catch(Exception exp)
        	{
        		
        	    System.out.println("Wrong value of transbit.tbits.mail.enableoutgoing");
        	}
        	if(ENABLE_OUTGOING == null)
        	{
        		System.out.println("transbit.tbits.smtp.protocol unspecified. Setting it to true");
        		ENABLE_OUTGOING = "true";
        	}
        	
        	
        	
        	
        	try
            {
            COMMON_FROM_ADDRESS=PropertiesHandler.getProperty("transbit.tbits.mail.commonfromaddr");
            }
            catch(Exception exp)
            {
                exp.printStackTrace();
                System.out.println("Wrong value of transbit.tbits.mail.commonfromaddr");
            }
        	
            
            try
            {
            OVERWRITE_FROM_ADDRESS=PropertiesHandler.getProperty("transbit.tbits.mail.overwritefromaddr");
            }
            catch(Exception exp)
            {
                exp.printStackTrace();
                System.out.println("Wrong value of transbit.tbits.mail.overwritefromaddr");
            }
        	
//            try
//            {
//            	MAX_EMAIL_ATTACHEMENT_SIZE_IN_BYTES = PropertiesHandler.getProperty(MAX_EMAIL_ATT_SIZE_PROP_NAME);
//            }
//            catch(Exception e)
//            {
//            	e.printStackTrace();
//            	System.out.println("Exception while setting the property : " + MAX_EMAIL_ATT_SIZE_PROP_NAME + " setting it to 1048576");
//            	MAX_EMAIL_ATTACHEMENT_SIZE_IN_BYTES = "1048576"; 
//            }
            
        	tA = new TransbitAuthenticator(MailProperties.SMTP_LOGIN, MailProperties.SMTP_PASSWORD);
     }
    }
    
    static {
        loadProperties();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method lists all the Properties to the given PrintStream.
     *
     * @param ps  PrintStream to which the properties should be written.
     */
    public static void listProperties(PrintStream ps) {
        ourSysProperties.list(ps);
        ourAppProperties.list(ps);
        ourLogProperties.list(ps);
        ConnectionProperties.ourDBPoolProperties.list(ps);
    }

    /**
     * This method lists all the Properties to the given PrintWriter.
     *
     * @param pw  PrintWriter to which the properties should be written.
     */
    public static void listProperties(PrintWriter pw) {
        ourSysProperties.list(pw);
        ourAppProperties.list(pw);
        ourLogProperties.list(pw);
        ConnectionProperties.ourDBPoolProperties.list(pw);
    }

    /**
     * This method lists the requested Properties to the given PrintWriter.
     *
     * @param ps  PrintStream to which the properties should be written.
     */
    public static void listProperties(PrintStream ps, int type) {
        switch (type) {
        
       case PROP_APPLICATION :
            ourAppProperties.list(ps);

            break;

        case PROP_SYSTEM :
            ourSysProperties.list(ps);

            break;

        case PROP_LOGGING :
            ourLogProperties.list(ps);

            break;

        case PROP_DB_CON_POOLING :
            ConnectionProperties.ourDBPoolProperties.list(ps);

            break;

        case PROP_ALL :
        default :
        	ourAppProperties.list(ps);
            ourSysProperties.list(ps);
            ourLogProperties.list(ps);
            ConnectionProperties.ourDBPoolProperties.list(ps);

            break;
        }
    }

    /**
     * This method lists the requested Properties to the given PrintWriter.
     *
     * @param pw  PrintWriter to which the properties should be written.
     */
    public static void listProperties(PrintWriter pw, int type) {
        switch (type) {
       
        case PROP_APPLICATION :
            ourAppProperties.list(pw);

            break;

        case PROP_SYSTEM :
            ourSysProperties.list(pw);

            break;

        case PROP_LOGGING :
            ourLogProperties.list(pw);

            break;

        case PROP_DB_CON_POOLING :
            ConnectionProperties.ourDBPoolProperties.list(pw);

            break;

        case PROP_ALL :
        default :
        	ourAppProperties.list(pw);
            ourSysProperties.list(pw);
            ourLogProperties.list(pw);
            ConnectionProperties.ourDBPoolProperties.list(pw);

            break;
        }
    }

    public static boolean isTransmittalEnabled()
    {
    	boolean ret = false;
    	try
    	{
    		ret = Boolean.parseBoolean(PropertiesHandler.getProperty(TRANSBIT_TBITS_TRANSMITTAL));    		
    	}
    	catch(Exception ex)
    	{
    		System.out.println(TRANSBIT_TBITS_TRANSMITTAL + " Property not found.");
    	} 
    	return ret;
    }
    
    /**
     * This method loads the properties from the following two files.
     *      - etc/TBits.properties
     *      - etc/TBits.auth
     */
    private static synchronized void loadProperties() {
        ourSysProperties = System.getProperties();
		ourAppProperties = new Properties();
		ourLogProperties = new Properties();
//		ConnectionProperties.ourDBPoolProperties = new Properties();
        
		try {

			Connection connection = null;

			String authFileName = Configuration.getAuthenticationFileName();

			if ((authFileName != null)
					&& (authFileName.trim().equals("") == false)) {
				readProperties(authFileName, PROP_APPLICATION);
			} else {
				System.err.println("[ " + PropertiesHandler.class.getName()
						+ " ] " + "WARNING: Name of the application's "
						+ "authentication properties file is not "
						+ "provided.");

				if (Configuration.ourIsEnvInitialized == false) {
					System.err.println("[ " + PropertiesHandler.class.getName()
							+ " ] " + "WARNING: Initialize the "
							+ "tbits-commons library properly.");
				}
			}

			
			/*
			 * Application Properties from Database must be loaded after the
			 * Properties handler loads the properties related to Data Pool.
			 */
			try {
				System.out
						.println("Trying to load the application properties from database");
				connection = DataSourcePool.getConnection();

				PreparedStatement ps = connection.prepareStatement("SELECT "
						+ DB_PROP_TABLE_COL_NAME + ","
						+ DB_PROP_TABLE_COL_VALUE + " FROM "
						+ DB_PROP_TABLE_NAME);
				ResultSet rs = ps.executeQuery();

				if (null != rs) {
					while (rs.next() == true) {
						ourAppProperties.put(rs
								.getString(DB_PROP_TABLE_COL_NAME), rs
								.getString(DB_PROP_TABLE_COL_VALUE));
					}
					rs.close();
				}
				ps.close();

				System.out
						.println("Successfully loaded Properties from database");
			} catch (Exception e) {
				e.printStackTrace();
				System.out
						.println("Could Not load some or all Properties from database: "
								+ "Trying to load all the properties from file system");

				String propFileName = Configuration.getPropertyFileName();

				if ((propFileName != null)
						&& (propFileName.trim().equals("") == false)) {
					readProperties(propFileName, PROP_APPLICATION);
				} else {
					System.err.println("[ " + PropertiesHandler.class.getName()
							+ " ] " + "WARNING: Name of the application's "
							+ "properties file is not provided.");

					if (Configuration.ourIsEnvInitialized == false) {
						System.err.println("[ "
								+ PropertiesHandler.class.getName() + " ] "
								+ "WARNING: Initialize the "
								+ "tbits-commons library properly.");
					}
				}

			}

			finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						System.out
								.println("Unable to close the connection while loading the properties.");
					}
				}
			}

			/*
			 * First Properties reader will try to read logging properties from
			 * database if unsuccessful, move to file system
			 */
			/*
			 * NOte here that there is a possibility that the same connection will get closed again 
			 * CASE : if the connection variable in the above try-catch-finally executed perfectly
			 * and then there is a sql exception in getConnection() in the below try-catch-finally then
			 * the reference to the connection object will still be pointing to the previous connection
			 * and in finally the previous connection will get closed again. The bug has been fixed by 
			 * setting the previous connection object to null before trying to get new connection object
			 */
			connection = null ;
			try {
				connection = DataSourcePool.getConnection();
				PreparedStatement ps = connection.prepareStatement("SELECT "
						+ LOG4J_TABLE_COL_NAME + "," + LOG4J_TABLE_COL_VALUE
						+ " FROM " + LOG4J_TABLE_NAME);
				ResultSet rs = ps.executeQuery();

				if (null != rs) {
					while (rs.next() == true) {
						ourLogProperties.put(
								rs.getString(LOG4J_TABLE_COL_NAME), rs
										.getString(LOG4J_TABLE_COL_VALUE));
					}
					rs.close();
				}
				ps.close();

			} catch (Exception e) {
				System.out
						.println("Unable to Load logging properties from database:"
								+ "Trying to load it from database");

				String logConfFileName = Configuration.getLogConfFileName();

				if ((logConfFileName != null)
						&& (logConfFileName.trim().equals("") == false)) {
					readProperties(logConfFileName, PROP_LOGGING);
				} else {
					System.err.println("[ " + PropertiesHandler.class.getName()
							+ " ] " + "WARNING: Name of the application's "
							+ "logger properties file is not provided.");

					if (Configuration.ourIsEnvInitialized == false) {
						System.err.println("[ "
								+ PropertiesHandler.class.getName() + " ] "
								+ "WARNING: Initialize the "
								+ "tbits-commons library properly.");
					}
				}

			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						System.out
								.println("Unable to close the connection while loading the properties.");
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
    
	MailProperties.reloadMailProperties();
    }

    /**
	 * Main method for testing.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
    public static void main(String[] args) throws SQLException, IOException {
 // PropertiesHandler.listProperties(System.out,
	// PropertiesHandler.PROP_APPLICATION_DB);
 // PropertiesHandler.listProperties(System.out,
	// PropertiesHandler.PROP_APPLICATION);
 // PropertiesHandler.listProperties(System.out,
	// PropertiesHandler.PROP_LOGGING);
 // PropertiesHandler.listProperties(System.out,
	// PropertiesHandler.PROP_DB_CON_POOLING);
    	
    	String fileName = "tbits-quartz.serverdb";
    
    	File file1 = new File("E:/tbits/src/etc/"+fileName);
    	FileInputStream fis = new FileInputStream(file1);
    	
    	Properties tempProp = new Properties();
    	tempProp.load(fis);
    	String TableName = "quartz_properties";
    	int count = 0;
    	File file = new File(fileName+".sql");
    	if(file.createNewFile() == false)
    		System.out.println("File was not created");
    	FileOutputStream fos = new FileOutputStream(file);
 /*   	Connection conn = DataSourcePool.getConnection();
    	PreparedStatement ps = conn.prepareStatement("SELECT name,value from " + TableName);
    	ResultSet rs = ps.executeQuery();
    	while(rs.next()) {
    		String str = "INSERT INTO " + TableName + "(name,value) Values('" 
    				+ rs.getString(1) +"','" + rs.getString(2) + "')\n";
    		fos.write(str.getBytes());
    		count++;
    	  	}		
    	System.out.println(count);	*/
   	for(Object key : tempProp.keySet()) {
   		String str = "INSERT INTO " + TableName + "(name,value) Values('" 
   				+ (String)key +"','" + (String)tempProp.get(key) + "')\n";
   		fos.write(str.getBytes());
   		count++;
    	}
    }

    /**
     * This method reads the properties from the files and stores them in the
     * Properties objects based on their type.
     *
     * @param fileName  Name of the file in java.util.Properties format.
     * @param type      Type of the file.
     */
    private static void readProperties(String fileName, int type) {
    	String filePath = "etc/" + fileName;
        File   file     = Configuration.findPath(filePath);

        if (file == null) {
            System.err.println("[ " + PropertiesHandler.class.getName() + " ] " + "WARNING: File not found: " + fileName);

            return;
        }

        try {
            FileInputStream fis = new FileInputStream(file);

            switch (type) {
            case PROP_APPLICATION :
                ourAppProperties.load(fis);

                break;

            case PROP_LOGGING :
                ourLogProperties.load(fis);
                break;
            }

            fis.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println("[ " + PropertiesHandler.class.getName() + " ] " + "An exception has occurred while loading properties " + "from the file: " + fileName + "\n");
            fnfe.printStackTrace(System.err);
        } catch (IOException ioe) {
            System.err.println("[ " + PropertiesHandler.class.getName() + " ] " + "An exception has occurred while loading properties " + "from the file: " + fileName + "\n");
            ioe.printStackTrace(System.err);
        }

        return;
    }

    /**
     * This is a hook to update all the properties objects.
     */
    public static void reload() {
        loadProperties();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the value for given key in the properties.
     * It throws an illegal argument exception if the key is not found in the
     * properties.
     *
     * @param aKey  Key whose value is needed.
     *
     * @return Value for this key in the properties.
     *
     * @throws IllegalArgumentException Incase the key is not found in the
     *                                  properties.
     */
    public static Integer getIntProperty(String aKey) throws IllegalArgumentException {
        String aValue = null;

        /*
         * We always search in the application properties first,that also in database
         * first and then in local file system and then in
         * the system properties. This is to ensure that the value present
         * in the application properties gets preference over the one in the
         * system properties.
         */
        if (ourAppProperties.containsKey(aKey) == true) {
            aValue = ourAppProperties.getProperty(aKey);
        } else if (ourSysProperties.containsKey(aKey) == true) {
            aValue = ourSysProperties.getProperty(aKey);
        } 
        
//        else {
//            throw new IllegalArgumentException("Property Key not found: " + aKey);
//        }

//        int intValue = -1;

        try {
           return Integer.parseInt(aValue);
        } catch (NumberFormatException nfe) {
            System.err.println(nfe.toString());
           return null; 
        }

//        return intValue;
    }

    /**
     * This method returns the value for given key in the logging properties.
     *
     * @param aKey  Key whose value is needed.
     *
     * @return Value for this key in the properties.
     *
     * @throws IllegalArgumentException Incase the key is not found in the
     *                                  properties.
     */
    public static String getLoggingProperty(String aKey) throws IllegalArgumentException {
        String aValue = null;

        if (ourLogProperties.containsKey(aKey) == true) {
            aValue = ourLogProperties.getProperty(aKey);
        } 
//        else {
//            throw new IllegalArgumentException("Logging Property Key not found: " + aKey);
//        }

        return aValue;
    }

    /**
     * This method returns the value for given key in the properties.
     * It always searches in the application properties first and then in
     * the system properties. This is to ensure that the value present
     * in the application properties gets preference over the one in the
     * system properties. It throws an illegal argument exception if the key is
     * not found in the properties.
     *
     * @param aKey  Key whose value is needed.
     *
     * @return Value for this key in the properties.
     *
     */
    public static String getProperty(String aKey) {
        String aValue = null;

        /*
         * We always search in the application properties first(that also 
         * in database first and then in local file system )and then in
         * the system properties. This is to ensure that the value present
         * in the application properties gets preference over the one in the
         * system properties.
         */
       if (ourAppProperties.containsKey(aKey) == true) {
            aValue = ourAppProperties.getProperty(aKey);
        } else if (ourSysProperties.containsKey(aKey) == true) {
            aValue = ourSysProperties.getProperty(aKey);
        } 
//        else {
//            throw new IllegalArgumentException("Property Key not found: " + aKey);
//        }

        return aValue;
    }
    public static Properties getAppAndSysProperties()
    {
    	Properties allProps = new Properties(ourSysProperties);
    	allProps.putAll(ourAppProperties);
    	return allProps;
    }

    public static Properties getAppProperties() {
    	Properties props = new Properties();
    	props.putAll(ourAppProperties);
    	return props;
    }
    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the value of given key in the Logging property
     * list and returns the old value.
     *
     * @param aKey     Key of the property.
     * @param aValue   Value of the property.
     *
     * @return Old Value of the property or null if the key is a new one.
     */
    public static String setLoggingProperty(String aKey, String aValue) {
        String oldValue = null;

        // If key is either null or empty leave it and return.
        if ((aKey == null) || (aKey.trim().equals("") == true)) {
            return oldValue;
        }

        // if the value is null, set value to empty string.
        if (aValue == null) {
            aValue = "";
        }

        oldValue = (String) ourLogProperties.setProperty(aKey, aValue);

        return oldValue;
    }

    /**
     * This method sets the value of given key in the application's property
     * list and returns the old value.
     *
     * @param aKey     Key of the property.
     * @param aValue   Value of the property.
     *
     * @return Old Value of the property or null if the key is a new one.
     */
    public static String setProperty(String aKey, String aValue) {
        String oldValue = null;

        // If key is either null or empty leave it and return.
        if ((aKey == null) || (aKey.trim().equals("") == true)) {
            return oldValue;
        }

        // if the value is null, set value to empty string.
        if (aValue == null) {
            aValue = "";
        }
        oldValue = (String) ourAppProperties.setProperty(aKey, aValue);
        //oldValue = (String) ourAppProperties.setProperty(aKey, aValue);

        return oldValue;
    }
    
    public static Properties getLoggingProperties() {
    	return ourLogProperties;
    }
    
    public static Properties getDBPoolProperties() {
    	return ConnectionProperties.getDBPoolProperties();
    }
    
    public static boolean insertAppProperties(String name,String value) throws DatabaseException{

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			
			String sql = "INSERT INTO tbits_properties(name, value) VALUES(?,?)";

			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, value);

			ps.execute();
			ps.close();
			PropertiesHandler.reload();
			return true;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occured while adding the app properties.");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
				}
				connection = null;
			}
		}
	}

	public static boolean deleteAppProperties(String name) throws DatabaseException{

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();

			String sql = "DELETE FROM tbits_properties where name = ?";

			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setString(1, name);
			ps.execute();
			ps.close();
			PropertiesHandler.reload();
			return true;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occured while deleting the app properties.");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
				}
				connection = null;
			}
		}
	}
}
