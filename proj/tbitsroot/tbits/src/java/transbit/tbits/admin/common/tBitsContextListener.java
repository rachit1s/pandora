package transbit.tbits.admin.common;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import transbit.tbits.addons.AddonManager;
import transbit.tbits.admin.AdminAllUsers;
import transbit.tbits.admin.AdminAppProperties;
import transbit.tbits.admin.AdminCaptions;
import transbit.tbits.admin.AdminCategories;
import transbit.tbits.admin.AdminDisplayGroups;
import transbit.tbits.admin.AdminEscalations;
import transbit.tbits.admin.AdminFields;
import transbit.tbits.admin.AdminMailingLists;
import transbit.tbits.admin.AdminProperties;
import transbit.tbits.admin.AdminRoles;
import transbit.tbits.admin.AdminTransmittals;
import transbit.tbits.admin.AdminUserEscalation;
import transbit.tbits.admin.AdminUsers;
import transbit.tbits.admin.AdminUtil;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.scheduler.AllJobActions;
import transbit.tbits.scheduler.MaintenanceJob;
import transbit.tbits.sms.SMSLogs;
import transbit.tbits.sms.SmsLogServlet;
import transbit.tbits.webapps.AdminReports;

/**
 * Application Lifecycle Listener implementation class tBitsContextListener
 *
 */
public class tBitsContextListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public tBitsContextListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0) {
    	// INITIALIZE THE ADDON MANAGER. THIS WILL THROW ERROR IF IT COULD NOT BE INITIALIZED PROPERLY
    	AddonManager.getInstance().initialize();
    	
    	System.out.println("Context Listener started.");
        try {
        	Class.forName(AdminProperties.class.getName());
        	Class.forName(AdminFields.class.getName());
        	Class.forName(AdminRoles.class.getName());
        	Class.forName(AdminUsers.class.getName());
        	Class.forName(AdminCategories.class.getName());
        	Class.forName(AdminDisplayGroups.class.getName());        	
        	
        	Class.forName(AdminCaptions.class.getName());
        	Class.forName(AdminEscalations.class.getName());
        	
        	boolean hasTransmittals = false;
        	try
        	{
        		hasTransmittals = Boolean.parseBoolean(PropertiesHandler.getProperty("transbit.tbits.transmittal"));
        	}
        	catch(Exception e)
        	{
        		
        	}
        	if(hasTransmittals)
        		Class.forName(AdminTransmittals.class.getName());
        	
        	Class.forName(AdminAppProperties.class.getName());
        	Class.forName(AllJobActions.class.getName());
        	Class.forName(AdminAllUsers.class.getName());
        	Class.forName(AdminMailingLists.class.getName());
        	Class.forName(AdminReports.class.getName());        	
        	Class.forName(SmsLogServlet.class.getName());
        	Class.forName(MaintenanceJob.class.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
        // TODO Auto-generated method stub
    }
	
}
