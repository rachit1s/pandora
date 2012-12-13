/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/*
 * TBitsScheduler.java
 *
 * $Header:
 */
package transbit.tbits.scheduler;

//~--- non-JDK imports --------------------------------------------------------

//Quartz Imports.
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

//TBits Imports.
import transbit.tbits.common.Configuration;
import transbit.tbits.common.ConnectionProperties;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.File;
import java.io.FileInputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

//~--- classes ----------------------------------------------------------------

/**
 *
 *
 * @author Vaibhav,Abhishek
 * @version $Id: $
 */
public class TBitsScheduler {

    // Application Logger.
    public static final TBitsLogger LOG                     = TBitsLogger.getLogger(PKG_SCHEDULER);
    private static final String     QUARTZ_SERVER_PROP_FILE = "etc/tbits-quartz.serverdb";

// Names of Columns for Key , Value pairs in database
	
	public static final String QUARTZ_TABLE_NAME = "quartz_properties";
	
	public static final String QUARTZ_COL_NAME = "name";
	
	public static final String QUARTZ_COL_VALUE = "value";	
	
	private static final String QUARTZ_DS_KEY = "org.quartz.jobStore.dataSource";
	
	private static final String QUARTZ_DS_VAL = "tbits";
	
	private static final String QUARTZ_DB_URL = "org.quartz.dataSource." + QUARTZ_DS_VAL + ".URL";
	
	private static final String QUARTZ_DB_USER = "org.quartz.dataSource." + QUARTZ_DS_VAL + ".user";
	
	private static final String QUARTZ_DB_PASS = "org.quartz.dataSource." + QUARTZ_DS_VAL + ".password";
	
	private static final String QUARTZ_DB_DRIVER = "org.quartz.dataSource." + QUARTZ_DS_VAL + ".driver";
	
	private static final String PASSWORD = "password";
	
	private static final String USER = "user";
	
	private static final String DRIVER_U_R_L = "driverURL";
	
	private static final String DRIVER_CLASS = "driverClass";
	
    //~--- fields -------------------------------------------------------------

    private Properties myProperties;
    private static Scheduler  myScheduler = null;

    //~--- constructors -------------------------------------------------------

    /**
     * Only Constructor of this class.
     *
     */
    public TBitsScheduler() {
       
    }

    public static Scheduler getScheduler() {
    	if(null == myScheduler) {
    		final TBitsScheduler scheduler = new TBitsScheduler();
    		scheduler.start();
//    		Timer timer = new Timer();
    	}
//    		timer.schedule(new TimerTask(){
//				boolean reset = false;
//    			@Override
//				public void run() {
//					Connection conn = null;
//					try {
//						conn = DataSourcePool.getConnection();
//						PreparedStatement ps = null;
//			    		ResultSet rs = null;
//			    		
//			    		ps = conn.prepareStatement("SELECT GetDate()");
//			    		rs = ps.executeQuery();
////			    		System.out.println("---------------- accessing database correctly --------------");
//						if(reset){
//							myScheduler = null;
//							scheduler.start();
//							reset = false;
//						}
//					} catch (Exception e) {
//						System.out.println("---------------- error in accessing database --------------");
//						e.printStackTrace();
//						reset = true;
//						TBitsScheduler.Shutdown(false);
//					} catch (Throwable e) {
//						System.out.println("---------------- error in accessing database --------------");
//						e.printStackTrace();
//						reset = true;
//						TBitsScheduler.Shutdown(false);
//					}finally {
//						if (conn != null) {
//							try {
//								conn.close();
//							} catch (SQLException e) {
//								e.printStackTrace();
//								// TODO Auto-generated catch block
//								System.out
//										.println("Unable to close connection after reading Quartz Properties.");
//							}
//						}
//					}
//				}
//    		}, 60000, 60000);
//    	}
//    	
    	return myScheduler;
    }
    public int start()
    {
        if(null != myScheduler){
        	return 0;
        }
    	if (readProperties() == false) {
            return 1;
        }

        try {
            startScheduler();
        } catch (SchedulerException se) {
            LOG.severe("An exception occurred while starting the scheduler:\n" + "",(se));
            se.printStackTrace();
            return 1;
        }
         return 0;
    }
    
    public static void Shutdown(boolean waitForJobsToComplete) 
    {
    	if(myScheduler != null)
			try {
				System.out.println("Shutting down scheduler.");
				myScheduler.shutdown(waitForJobsToComplete);
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    //~--- methods ------------------------------------------------------------

    /**
     * This is the main method that starts the proceeding of this class.
     *
     * @param args
     */
    public static void main(String[] args) {
       TBitsScheduler schedular = new TBitsScheduler();
       int retValue = schedular.start();       
       //System.exit(retValue);
    }

    private boolean readProperties() {
 
   //At first Try to read properties from database, if fails then read it from 
  //  	the given properties file
    	Connection conn = null;
    	try {
    		System.out.println("Trying to load quartz properties from database");
    		
    		conn = DataSourcePool.getConnection();
    		PreparedStatement ps = null;
    		ResultSet rs = null;
    		
    		ps = conn.prepareStatement("SELECT " + QUARTZ_COL_NAME + " , " +
    						QUARTZ_COL_VALUE + " FROM " + QUARTZ_TABLE_NAME);
    		rs = ps.executeQuery();
    		myProperties = new Properties();
    		
    		while(rs.next()) {
    			myProperties.put(rs.getString(1), rs.getString(2));
    		}
    		
    /* These Properties comes from the DB Pool  			*/
    		
    		myProperties.put(QUARTZ_DS_KEY,QUARTZ_DS_VAL);
    		myProperties.put(QUARTZ_DB_USER,ConnectionProperties.getDBPoolProperty(USER ));
    		myProperties.put(QUARTZ_DB_PASS,ConnectionProperties.getDBPoolProperty(PASSWORD));
    		myProperties.put(QUARTZ_DB_URL,ConnectionProperties.getDBPoolProperty(DRIVER_U_R_L));
    		myProperties.put(QUARTZ_DB_DRIVER,ConnectionProperties.getDBPoolProperty(DRIVER_CLASS));
    		
    		
    		System.out.println("Successfully read quartz properties from database");
    		
    	}catch(Exception e1) { 		//if any exception occurs, start reading properties from file
    	
    		System.out
					.println("Unable to read properties from database:trying to read from file");
			try {
				File file = Configuration.findPath(QUARTZ_SERVER_PROP_FILE);
				if (file == null) {
					LOG.severe("Could not find the Quartz's RMIScheduler Server"
									+ " properties file.");
					return false;
				}
				FileInputStream fis = new FileInputStream(file);
				myProperties = new Properties();
				myProperties.load(fis);
			} catch (Exception e2) {
				LOG
						.severe("Exception while reading properties of Scheduler Server"
								+ "",(e2));
				e2.printStackTrace();
				return false;
			} 
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
					// TODO Auto-generated catch block
					System.out
							.println("Unable to close connection after reading Quartz Properties.");
				}
			}
		}
        return true;
    }

    /**
     * This method
     *  1. Creates an object of StandardSchedulerFactory .
     *  2. Initializes it with the properties object prepared from our file.
     *  3. Obtains the scheduler object from the factory.
     *  4. Starts the scheduler.
     *
     * @throws SchedulerException
     */
    private void startScheduler() throws SchedulerException {

        // Create an instance of Scheduler Factory.
        StdSchedulerFactory factory = new StdSchedulerFactory();

        // Initialize the factory with our properties object.
        factory.initialize(myProperties);

        
        // Obtain the scheduler object.
        myScheduler = factory.getScheduler();

        LOG.info("Starting Scheduler...");
        // Start the scheduler.
        myScheduler.start();
        LOG.info("Scheduler started successfully....");
        System.out.println("[TBits Job Scheduler] Started successfully....");
        
        String[] groups = null;
        try
        {
        	groups = myScheduler.getJobGroupNames();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        if (groups != null) {
        	System.out.println("No. of Groups:" + groups.length);
            for (int i = 0; i < groups.length; i++) {
                LOG.info("Group: " + groups[i]);

                String[] jobs = myScheduler.getJobNames(groups[i]);

                if (jobs != null) {
                    for (int j = 0; j < jobs.length; j++) {
                        LOG.info("Job: " + jobs[j]);
                        System.out.println("Job: " + jobs[j]);
                    }
                }
            }
        }
        else
        {
        	LOG.error("Job groups are null");
        	System.out.println("Job groups are null");
        }
    }
}
