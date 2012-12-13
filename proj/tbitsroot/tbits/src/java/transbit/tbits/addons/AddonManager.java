/**
 * 
 */
package transbit.tbits.addons;

import static transbit.tbits.addons.AddonInfo.STATE_NAMES;
import static transbit.tbits.addons.AddonInfo.STATUS_ACTIVATED;
import static transbit.tbits.addons.AddonInfo.STATUS_DEACTIVATED;
import static transbit.tbits.addons.AddonInfo.STATUS_REGISTERED;
import static transbit.tbits.addons.AddonInfo.STATUS_UNLOADED;
import static transbit.tbits.addons.AddonInfo.STATUS_UNREGISTERED;
import static transbit.tbits.addons.AddonInfo.STATUS_UPLOADED;
import static transbit.tbits.addons.AddonLoader.ADDON_AUTHOR;
import static transbit.tbits.addons.AddonLoader.ADDON_DB_CONFIG_FILE;
import static transbit.tbits.addons.AddonLoader.ADDON_DESCRIPTION;
import static transbit.tbits.addons.AddonLoader.ADDON_NAME;
import static transbit.tbits.addons.AddonLoader.ADDON_VERSION;
import static transbit.tbits.addons.AddonManagerContext.ADDON_INFO;
import static transbit.tbits.addons.AddonManagerContext.CONNECTION;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import transbit.tbits.api.APIUtil;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.events.IEvent;
import transbit.tbits.events.IEventHandler;
import transbit.tbits.events.EventManager;
import transbit.tbits.events.EventRegistry;
import transbit.tbits.events.EventRegistryManager;
import transbit.tbits.exception.PersistenceException;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * This class takes care of managing all the addons for following actions
 * UPLOADING : it takes the given jar file and adds into the DB repository for the
 * INSTALLING : loads the jar and classes
 * REGISTERING : registers it with the system, i.e. make an entry for this module with version in the current_versions and executes all the db-upgrades
 * ACTIVATING : runs the entrypoint for registering events etc, in this it adds the events in the DB. But not in the event mechanism.
 * 				events activation deactivation is handled separately.
 * DEACTIVATING : runs the entrypoint for deactivating. in this it removes the said events etc from the db as guided by the deactivate method,
 * UNREGISTERING : runs the entrypoints unregister method for and removes entry from current_versions. and should also delete any table created by this addon
 * UNINSTALLING : removes all the classes  etc. from cache
 * UNLOADING : removes the jar from the system.
 */
public class AddonManager 
{
	private boolean isInitiazlied = false; 
	static Logger logger = Logger.getLogger("transbit.tbits.addons");
	
	public static final String CHECK_VERSIONS = "select * from current_version where modulename = ?";
	public static final String INSERT_VERSIONS = "insert into current_version ( major,modulename ) values (?,?) ";
	public static final String UPDATE_VERSIONS = "update current_version set major = ? where modulename = ?";
	public static final String DELETE_VERSIONS = "delete from current_version where modulename = ?";
	
	private Hashtable<AddonInfo,AddonLoader> addonLoaders = null;
	private static AddonManager instance = null;
	private AddonManager()
	{
		addonLoaders = new Hashtable<AddonInfo, AddonLoader>();
	}
	
	public synchronized static AddonManager getInstance()
	{
		if( null == instance )
			instance = new AddonManager();
		
		return instance;
	}
	
	/**
	 * read all the addonInfo and activate the one which are in activated state
	 */
	public synchronized void initialize()
	{
		try
		{
			if( isInitiazlied == true )
				return ;
			List<AddonInfo> addons = AddonInfoManager.getInstance().getAllAddonInfos();
			for(AddonInfo addon : addons )
			{
				if(addon.getStatus() == AddonInfo.STATUS_ACTIVATED )
				{
					Connection con = null;
					try	
					{
						con = DataSourcePool.getConnection();
						AddonManagerContext amc = new AddonManagerContext();
						amc.set(AddonManagerContext.ADDON_INFO, addon);
						amc.set(AddonManagerContext.CONNECTION, con	);
						
						handleActivation(amc);
					}
					catch(Exception e)
					{
						e.printStackTrace();
						throw e;
					}
					finally
					{
						if( null != con )
							if( !con.isClosed() )
								con.close();
					}
				}
			}
			
			this.isInitiazlied = true;
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING,"Exception occured while initializing the AddonManager : " + e.getMessage() , e);
			throw new RuntimeException("Exception occured while initializing the AddonManager : " + e.getMessage() , e);
		}
	}
	/**
	 * UPLOADED --(1)---> REGISTERED----(2)---> ACTIVATED---(3)----> DEACTIVATED---------
	 *   | 					  |						 ^                    |				|
	 *   |                    |                       |___________(10)____|  			|
	 *   | 					  |											  |(4) 	        |                                  
	 *   |					  |-------------------(7)--------------->UNREGISTERED		|				
	 *   | 					  |(6)										  |(5)			|
	 *   |----------(9)--------------------------------------------- > UNLOADED<--(8)---|
	 * @throws AddonException 
	 */
	
	/**
	 * Sets the state of the given addon to the state mentioned in the addon. In case of error throws error.
	 * @param addon
	 * @return : the Result object which contains a message and optionally an exception object which represents the irrecoverable error.
	 * @throws AddonException : throws exception 
	 */ 
	public Result saveAddonInfo(AddonInfo addon) throws AddonException
	{
		try
		{
			AddonInfo a = AddonInfoManager.getInstance().getAddonInfo(addon.getJarId());
			if( null == a )
				throw new AddonException("No database entry found for the addon : " + addon);
			
			return fromAtoB(a,addon);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING,"Exception occured while processing the addon save request." , e);
			throw new AddonException(e);
		}
		
	}
	/**
	 * Handles the state transition of an Addon from state a to state b
	 * @param a
	 * @param b
	 * @return
	 * @throws AddonException
	 */
	private Result fromAtoB(AddonInfo a, AddonInfo b) throws AddonException
	{
		int aStatus = a.getStatus();
		int bStatus = b.getStatus();
		AddonManagerContext amc = new AddonManagerContext();
		
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			con.setAutoCommit(false);
			amc.set(CONNECTION, con);
			amc.set(ADDON_INFO, b);
			Result result = null;
			switch(a.getStatus())
			{
				case STATUS_UPLOADED:
					switch(bStatus)
					{
						case STATUS_REGISTERED: result = fromUploadedToRegistered(amc);
						break;
						case STATUS_UNLOADED: result = fromUploadedToUnloaded(amc);
						break;
						default: return reportIllegalStateChange(aStatus,bStatus);
					}
					break;
				case STATUS_REGISTERED:
					switch(bStatus)
					{
						case STATUS_ACTIVATED: result = fromRegisteredToActivated(amc);
						break;
						case STATUS_UNREGISTERED: result = fromRegisteredToUnRegistered(amc);
						break;
						case STATUS_UNLOADED: result = fromRegisteredToUnloaded(amc);
						break;
						default: return reportIllegalStateChange(aStatus, bStatus);
					}
					break;
				case STATUS_ACTIVATED:
					switch(bStatus)
					{
						case STATUS_DEACTIVATED : result = fromActivatedToDeactivated(amc);
						break;
						default : return reportIllegalStateChange(aStatus, bStatus);
					}
					break;
				case STATUS_DEACTIVATED:
					switch(bStatus)
					{
						case STATUS_UNREGISTERED : result = fromDeactivatedToUnRegistered(amc);
						break;
						case STATUS_ACTIVATED : result = fromDeactivatedToActivated(amc);
						break;
						case STATUS_UNLOADED : result = fromDeactivatedToUnloaded(amc);
						break;
						default: return reportIllegalStateChange(aStatus, bStatus);
					}
					break;
				case STATUS_UNREGISTERED:
					switch(bStatus)
					{
						case STATUS_UNLOADED : result = fromUnRegisteredToUnloaded(amc);
						break;
						default : return reportIllegalStateChange(aStatus, bStatus);
					}
					break;
				default: return reportIllegalStateChange(aStatus, bStatus);
			}
			
			con.commit();
			return result;
		}
		catch(Exception e)
		{
			try {
				con.rollback();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Rollback of connection failed.", e1);
			}
			logger.log(Level.WARNING, "Exception occured while changing state of addon.", e);
			throw new AddonException("Exception occured while changing state of addon.\n Cause : " + e + ":" + e.getMessage(), e);
		}
		finally
		{
			if( null != con )
				try {
					if( !con.isClosed() )
						con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	private boolean checkStateValid(int status)
	{
		if( status < STATUS_UPLOADED || status > STATUS_UNLOADED )
			return false;
		
		return true;
	}
	
	/**
	 * @param aStatus
	 * @param bStatus
	 * @return 
	 * @throws AddonException 
	 */
	private Result reportIllegalStateChange(int aStatus, int bStatus) throws AddonException 
	{
		String from = "Illegal", to = "Illegal";
		if( checkStateValid(aStatus) )
			from = STATE_NAMES[aStatus];
		if( checkStateValid(bStatus) )
			to = STATE_NAMES[bStatus];
		
		throw new AddonException("Change of State from (" + aStatus + " = " + from + ") to (" + bStatus + "=" + to + ") is not allowed.");
	}

	/**
	 * 1. check basic validations on the jar uploaded
	 * 2. check/update current_version. check if the same plugin already exists and is > REGISTERED.
	 * 3. make db-upgrades
	 * 4. update status in addon_info table
	 * @param amc
	 * @throws Exception 
	 */
	private Result fromUploadedToRegistered(AddonManagerContext amc) throws Exception
	{
		AddonInfo ai = amc.getAddonInfo();
		AddonLoader al = loadJar(ai);
		// basic validations on the jar
		
		// check valid name
		if( al.getName() == null )
			throw new AddonException("The jar does not contain a valid " + AddonLoader.ADDON_NAME + " in its MANIFEST.MF : " + ai);
		else
		{
			// pre-check if already exists
			AddonInfo duplicate = AddonInfoManager.getInstance().getAddonInfoByName(al.getName());
			if( duplicate != null )
				throw new AddonException("An addon with same " + ADDON_NAME  + " already exists. Unload it first before registering this one. Existing addon : " + duplicate );
		}
		// check valid version
		if( al.getVersion() != null )
		{
			try
			{
				Double version = Double.parseDouble(al.getVersion());
				if( version < 0 )
					throw new AddonException(ADDON_VERSION + " cannot be less than 0");
			}
			catch(NumberFormatException nfe)
			{
				logger.log(Level.WARNING, ADDON_VERSION + " was not a valid double in MANIFEST.MF. version = " + al.getVersion() );
				throw new AddonException( ADDON_VERSION + " was not a valid double in MANIFEST.MF. version = " + al.getVersion());
			}
		}
		else
		{
			throw new AddonException("The jar does not contain a valid " + AddonLoader.ADDON_VERSION + " in its MANIFEST.MF : " + ai);
		}
		
		//check valid entry-point. its compulsory
		Class<? extends AddonEntryPoint> entryPointClass = null;
		if( null != al.getEntryPointClassName() )
		{
			entryPointClass = (Class<? extends AddonEntryPoint>) al.loadClass(al.getEntryPointClassName());
		}
		else
			throw new AddonException(AddonLoader.ADDON_ENTRYPOINT + " is a compulsory field in MANIFEST.MF and was not given in Adddon : " + ai);
		logger.info("EntryPointClass : " + entryPointClass);
		
		AddonEntryPoint aep = entryPointClass.newInstance();
		
		// check valid db-upgrade file if any
		if( al.getDbConfigFile() != null )
		{
			URL dbConfigsURL = al.getResource(al.getDbConfigFile());
			if(null == dbConfigsURL )
				throw new AddonException("The " + ADDON_DB_CONFIG_FILE + "(" + al.getDbConfigFile() + ") mentioned in MANIFEST.MF does not exists." );
		}
		
		// check valid author -- This is made compulsory so that atleast someone is made answerable for the addon
		if( null == al.getAuthor() || al.getAuthor().trim().equals(""))
			throw new AddonException("A valid " + ADDON_AUTHOR + " is required for an addon to be registered.");
		
		// check valid description -- This is make compulsory so that atleast some information is available for the addon
		if( null == al.getDescription() || al.getDescription().trim().equals(""))
			throw new AddonException("A valid " + ADDON_DESCRIPTION + " is required for an addon to be registered.");
		 
		// run the preRegister hook
		AddonContext ac = new AddonContext();
		ac.set(AddonContext.CONNECTION, amc.getConnection());
		ac.set(AddonContext.ADDON_INFO, amc.getAddonInfo());
		
		aep.preRegister(ac);
		
		makeDBUpgradesAndSetCurrentVersion(al, amc.getConnection());
		
		// update the AddonInfo
		ai.setAddonName(al.getName());
		ai.setAddonAuthor(al.getAuthor());
		ai.setAddonDescription(al.getDescription());
		ai.setStatus(STATUS_REGISTERED);
		
		ai = AddonInfoManager.getInstance().persist(ai, amc.getConnection());
		
		// run postRegister
		ac.set(AddonContext.ADDON_INFO, ai); // the changed AI
		aep.postRegister(ac);
		
		return new Result("Addon (" + ai + ") Registered Successfully.");
		// finish.
	}
	
	/**
	 *1. runs the activate of entry-point to register all the events. i.e. make their entries in DB event_registry
	 *if the entry already exists ( same event class and same eventHandler class ) then its left unchanged
	 *else an entry is created with the event enabled.
	 *2. All the event registries are given to event-manager for managed registration. if some exception occurs while
	 *registring the events with EventManager, then each entry is unregistered from EventManager and the addon reverted to Registered state
	 *The user will have to activate the addon manually again. Otherwise the addon is successfully marked activated.
	 *3. make available the AddonLoader for this addon from the AddonManager
	 * @param amc
	 * @throws Exception 
	 */
	private Result fromRegisteredToActivated(AddonManagerContext amc) throws Exception
	{
		return handleActivation(amc);
	}
	
	/**
	 *1. runs the activate of entry-point to register all the events. i.e. make their entries in DB event_registry
	 *if the entry already exists ( same event class and same eventHandler class ) then its left unchanged
	 *else an entry is created with the event enabled.
	 *2. All the event registries are given to event-manager for managed registration. if some exception occurs while
	 *registring the events with EventManager, then each entry is unregistered from EventManager and the addon reverted to its previous state
	 *The user will have to activate the addon manually again. Otherwise the addon is successfully marked activated.
	 *3. make available the AddonLoader for this addon from the AddonManager	 
	 * @param amc
	 * @throws Exception 
	 */
	private Result handleActivation(AddonManagerContext amc) throws Exception 
	{
		AddonInfo ai = amc.getAddonInfo();
		Connection con = amc.getConnection();
		
		ai.setStatus(STATUS_ACTIVATED);
		AddonInfoManager.getInstance().persist(ai, con);
		
		AddonLoader al = loadJar(ai);
		
		Class<? extends AddonEntryPoint> aepc = (Class<? extends AddonEntryPoint>) al.loadClass(al.getEntryPointClassName());
		AddonContext ac = new AddonContext();
		ac.set(AddonContext.ADDON_INFO, ai);
		ac.set(AddonContext.CONNECTION, con);
		AddonEntryPoint aep = aepc.newInstance();
		
		aep.activate(ac);
		
		// as no exception occurred in last step. Going ahead with putting the enabled events in the EventManager.
		List<EventRegistry> list = EventRegistryManager.getInstance().getAllEventRegistryBySourceId(ai.getAddonName(), con);
		// any exception in this block should be handled with following steps
		// 1. unregister all these events from EventManager -- this will not generate any error
		try 
		{
			for( EventRegistry er : list )
			{
				EventManager.getInstance().registerManagedHandler(er,al);
			}
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Event registering caught error. Now unregistering all the events." ,e);
			// unregister all the events from the EventManager -- This should success properly
			StringBuffer sb = new StringBuffer();
			for( EventRegistry er : list )
			{
				try 
				{
					EventManager.getInstance().unRegisterManagedHandler(er);
				} catch (ClassNotFoundException e1) {
					logger.log(Level.WARNING, "", e1);
					sb.append(e1.getMessage());
				}
			}
			
			throw new Exception("An exception occured while registering handler : " + e.getMessage() + ( sb.toString().equals("") ? "" : "\nFurther exception occured while reverting the registration of handlers : " + sb.toString() ), e);
		}
		
		addonLoaders.put(ai, al);
		
		return new Result("Addon (" + ai + ") successfully activated." );
		//finish
	}

	/**
	 * 1. remove all db entries for EventRegistries
	 * 2. mark the addon as deactivated 
	 * 3. remove all the managed EventRegistries from EventManager
	 * 4. remove the AddonLoader from AddonManager 
	 * so if 1 and 2 executes successfully 3 and 4 won't execute and no need to handle the error. 3 & 4 won't generate error
	 * @param amc
	 * @return 
	 * @throws PersistenceException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private Result fromActivatedToDeactivated(AddonManagerContext amc) throws PersistenceException // 3
, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		StringBuffer sb = new StringBuffer();
		
		AddonInfo ai = amc.getAddonInfo();
		ai.setStatus(STATUS_DEACTIVATED);
		AddonInfoManager.getInstance().persist(ai, amc.getConnection());
		
		List<EventRegistry> list = EventRegistryManager.getInstance().getAllEventRegistryBySourceId(ai.getAddonName(), amc.getConnection());
		for( EventRegistry er : list )
		{
			EventRegistryManager.getInstance().delete(er, amc.getConnection());
		}
		
		for( EventRegistry er : list)
			try {
				EventManager.getInstance().unRegisterManagedHandler(er);
			} catch (ClassNotFoundException e) {
				logger.log(Level.WARNING, "Exception occured while unregistering the er : " + er, e);
				sb.append("Exception occured while unregistering the er : " + er + ", Exception " + e.getMessage());
			}
		
		AddonLoader al = addonLoaders.remove(ai);

		try
		{
			Class<? extends AddonEntryPoint> aepc = (Class<? extends AddonEntryPoint>) al.loadClass(al.getEntryPointClassName());
			
			AddonEntryPoint aep = aepc.newInstance();
			AddonContext ac = new AddonContext();
			ac.set(AddonContext.CONNECTION, amc.getConnection());
			ac.set(AddonContext.ADDON_INFO, ai);
			
			aep.postDeactivate(ac);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception occured while running postDeactivate of the Addon : " + ai ,e );
			return new Result("Addon Deactivate. " + sb.toString() + ".But exception occured while running postDeactivate of the Addon : " + ai,e);
		}
		
		return new Result("Addon (" + ai + ") deactivated successfully." + sb.toString());
	}
	
	/**
	 * 1. change the state to UNREGISTERED
	 * 2. run the unregister method
	 * @param amc
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 * @throws PersistenceException 
	 * @throws ClassNotFoundException 
	 * @throws AddonException 
	 * @throws SQLException 
	 */
	private Result fromDeactivatedToUnRegistered(AddonManagerContext amc) throws ClassNotFoundException, PersistenceException, IOException, InstantiationException, IllegalAccessException // 4
, SQLException, AddonException
	{
		return handleUnRegister(amc);
	}
	/**
	 * 1. change the state to UNREGISTERED
	 * 2. delete the entry from current_version
	 * 3. load the jar and run the unregister method
	 * @param amc
	 * @throws ClassNotFoundException 
	 * @throws PersistenceException 
	 * @throws IOException 
	 * @throws IllegalAccessException y
	 * @throws InstantiationException 
	 * @throws AddonException 
	 * @throws SQLException 
	 */
	private Result handleUnRegister(AddonManagerContext amc) throws ClassNotFoundException, PersistenceException, IOException, InstantiationException, IllegalAccessException, SQLException, AddonException 
	{
		
		AddonInfo ai = amc.getAddonInfo();
		ai.setStatus(STATUS_UNREGISTERED);
		AddonInfoManager.getInstance().persist(ai, amc.getConnection());
		
		deleteVersion(ai,amc.getConnection());
		
		AddonLoader al = loadJar(ai);
		Class<? extends AddonEntryPoint> aepc = (Class<? extends AddonEntryPoint>) al.loadClass(al.getEntryPointClassName());
		
		AddonEntryPoint aep = aepc.newInstance();
		AddonContext ac = new AddonContext();
		ac.set(AddonContext.CONNECTION, amc.getConnection());
		ac.set(AddonContext.ADDON_INFO, ai);
		
		aep.postUnregister(ac);
		
		return new Result("Addon (" + ai + ") UnRegistered Successfully.");
	}

	/**
	 * @param ai
	 * @param connection
	 * @throws SQLException 
	 * @throws AddonException 
	 */
	private void deleteVersion(AddonInfo ai, Connection connection) throws SQLException, AddonException {
		PreparedStatement ps = connection.prepareStatement(DELETE_VERSIONS);
		ps.setString(1, ai.getAddonName());
		
		int changedRows = ps.executeUpdate();
		if( 0 == changedRows )
			throw new AddonException("No rows affected when deleting from current_versions");
		
		ps.close();
	}

	/**
	 * Just remove the AddonInfo entry. As the unregister process already removed the entry from current_version
	 * when a new jar is uploaded a new entry will be created for that addon.
	 * @param amc
	 * @throws PersistenceException 
	 * @throws SQLException 
	 */
	private Result fromUnRegisteredToUnloaded(AddonManagerContext amc) throws SQLException, PersistenceException // 5
	{
		return handleUnload(amc);
	}
	/**
	 * just remove the AddonInfo entry from the DB
	 * @param amc
	 * @return
	 * @throws PersistenceException 
	 * @throws SQLException 
	 */
	private Result handleUnload(AddonManagerContext amc) throws SQLException, PersistenceException 
	{
		AddonInfoManager.getInstance().delete(amc.getAddonInfo(), amc.getConnection());
		return new Result("Successfully unloaded the Addon : " + amc.getAddonInfo());
	}

	/**
	 * Just remove the AddonInfo entry. This will leave the database tables and entry in the current_versions intact.
	 * when a new jar is uploaded the same will be used for further upgrades.
	 * @param amc
	 * @return 
	 * @throws PersistenceException 
	 * @throws SQLException 
	 */
	private Result fromRegisteredToUnloaded(AddonManagerContext amc) throws SQLException, PersistenceException // 6
	{
		return handleUnload(amc);
	}
	
	/**
	 * 1. change the state to UNREGISTERED
	 * 2. delete the entry from current_version
	 * 3. load the jar and run the unregister method
	 * @param amc
	 * @return 
	 * @throws AddonException 
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 * @throws PersistenceException 
	 * @throws ClassNotFoundException 
	 */
	private Result fromRegisteredToUnRegistered(AddonManagerContext amc) throws ClassNotFoundException, PersistenceException, IOException, InstantiationException, IllegalAccessException, SQLException, AddonException // 7
	{
		return handleUnRegister(amc);
	}
	
	/**
	 * just remove the AddonInfo entry from the DB. This will keep the entry in current_version and other db changes intact and the same can be used
	 * when a new jar is uploaded and registered. 
	 * @param amc
	 * @return 
	 * @throws PersistenceException 
	 * @throws SQLException 
	 */
	private Result fromDeactivatedToUnloaded(AddonManagerContext amc) throws SQLException, PersistenceException // 8
	{
		return handleUnload(amc);
	}
	
	/**
	 * As no registration etc. happened till now. So just remove the AddonInfo.
	 * @param amc
	 * @return
	 * @throws SQLException
	 * @throws PersistenceException
	 */
	private Result fromUploadedToUnloaded(AddonManagerContext amc) throws SQLException, PersistenceException // 9
	{
		return handleUnload(amc);
	}
	
	/**
	 *1. runs the activate of entry-point to register all the events. i.e. make their entries in DB event_registry
	 *if the entry already exists ( same event class and same eventHandler class ) then its left unchanged
	 *else an entry is created with the event enabled.
	 *2. All the event registries are given to event-manager for managed registration. if some exception occurs while
	 *registring the events with EventManager, then each entry is unregistered from EventManager and the addon reverted to its Deactivated state
	 *The user will have to activate the addon manually again. Otherwise the addon is successfully marked activated.
	 *3. make available the AddonLoader for this addon from the AddonManager	 
	 * @param amc
	 * @return 
	 * @throws Exception 
	 */
	private Result fromDeactivatedToActivated(AddonManagerContext amc) throws Exception // 10
	{
		return handleActivation(amc);
	}
	
	/**
	 * Use this method in {@link AddonEntryPoint#activate(AddonContext)} to register for events in the DB.
	 * You have to pass the {@link AddonContext} passed in the {@link AddonEntryPoint#activate(AddonContext)} method.
	 * @param handler
	 * @param eventClass
	 * @throws AddonException
	 */
	public static <T extends IEvent, P extends IEventHandler<T>> EventRegistry registerHandler( AddonContext ac, Class<P> handler, Class<T> eventClass ) throws AddonException
	{
		try
		{
			IEventHandler eh = handler.newInstance();
			
			List<EventRegistry> erlist = EventRegistryManager.getInstance().getAllEventRegistryBySourceId(ac.getAddonInfo().getAddonName(), ac.getConnection());
			EventRegistry er = null;
			if( null != erlist )
			{	
				for( EventRegistry next : erlist )
				{
					if( next.getEventClass().equals(eventClass.getName()) && next.getEventHandlerClass().equals(handler.getName()))
					{
						er = next;
						logger.info("This handler for this event was already registered and the same configuration will be used." );
						break;
					}
				}
			}
			if( null == er ) // newly added events will be enabled.
			{
				er = new EventRegistry(0, ac.getAddonInfo().getAddonName(), eventClass.getName(), handler.getName(), true, (int)eh.getInitialOrder(), eh.getDescription() );
				er = EventRegistryManager.getInstance().persist(er,ac.getConnection());
			}
			
			return er;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE,"Event Registry failed. EventHandler : " + handler + " : EventClass : " + eventClass + " : AddonInfo : " + ac.getAddonInfo(),e);
			throw new AddonException("Event Registry failed. EventHandler : " + handler + " : EventClass : " + eventClass + " : AddonInfo : " + ac.getAddonInfo(),e);
		}
	}
	
	/**
	 * @return
	 * @throws PersistenceException 
	 * @throws IOException 
	 * @throws AddonLoaderException 
	 */
	private AddonLoader loadJar(AddonInfo ai) throws PersistenceException, IOException, AddonLoaderException {
		AddonInfoWithBytes aiwb = AddonInfoManager.getInstance().getAddonInfoWithBytes(ai.getJarId());
		
		// load bytes
		ByteArrayInputStream bais = new ByteArrayInputStream(aiwb.getJarBytes());
		File jarFile = File.createTempFile("addon", ".jar", APIUtil.getTBitsTMPDir() );
		FileOutputStream fos = new FileOutputStream(jarFile);
		readfully(bais, fos);
		bais.close();
		// not closing the fos to make sure that the file corresponding to it does not get deleted in running system.
		
		logger.info("Created a temp .jar (" + jarFile.getAbsolutePath() + ") for the addon : " + ai);
		URL jarURL = new URL("file", "localhost", jarFile.getAbsolutePath());
		return new AddonLoader(jarURL);
	}
	/**
	 * @param al
	 * @param con
	 * @throws SQLException 
	 * @throws AddonException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	private void makeDBUpgradesAndSetCurrentVersion(AddonLoader al, Connection con) throws Exception 
	{
		PreparedStatement ps = con.prepareStatement(CHECK_VERSIONS);
		ps.setString(1, al.getName());
		String versionString = al.getVersion();
		if( null == versionString )
			throw new AddonException(AddonLoader.ADDON_VERSION + " was not set.");
		
		Double version = Double.parseDouble(versionString.trim());
		ResultSet rs = ps.executeQuery();
		if( null != rs )
		{
			// found the entry in current_version table.
			if( rs.next() )
			{
				String major = rs.getString("major");
				if( null == major )
				{
					logger.info("current_version entry was found but major was null. Assuming it to be 0.0");
					major = "0.0";
				}
				
				Double m = Double.parseDouble(major.trim());
				if( m > version )
					throw new AddonException("A higher version(" + major + ") of this addon was already installed on this server. Hence cannot register the lower version(" + versionString + ")" );
				
				// update the version in the table.
				logger.info("The current version installed is " + major + " and the given version is " + version + ", so going ahead with installation of new scripts.");
				installDBUpdates(al,m,version,con);
				updateVersion(al,con);
			}
			else
			{
				// no entry for current version
				logger.info("No Entry for current version was found. Hence going with complete installation.");
				installDBUpdates(al,0.0,version,con);
				insertVersion(al,con);
			}
		}
		else
		{
			// no entry for current version
			logger.info("The ResultSet was null. Assuming no entry for current version was found. Hence going with complete installation.");
			installDBUpdates(al,0.0,version,con);
			insertVersion(al,con);
		}
	}

	/**
	 * @param al
	 * @param major
	 * @param version
	 * @param con
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SQLException 
	 * @throws AddonException 
	 */
	private void installDBUpdates(AddonLoader al, Double major, Double version,
			Connection con) throws Exception 
	{
		if( null == al.getDbConfigFile() )
		{	 
			logger.info("No DB Config File found.");
			return;
		}
		
		String configs = getResourceAsString(al,al.getDbConfigFile());
		
		List<Config> configList = AddonUtils.parseConfigs(configs);
		
		// sort first by versions. So that all the versions are together.
		// But do not rearrange the order within the version i.e. order by line number.
		Collections.sort(configList, new Comparator<Config>() {

			@Override
			public int compare(Config c1, Config c2) 
			{
				if( c1.getVersion() > c2.getVersion() )
					return 1;
				else if( c1.getVersion() < c2.getVersion() )
					return -1;
				else
					if( c1.getLineNumber() > c2.getLineNumber() )
						return 1 ;
					else return -1;
			}
		});
		
		ConfigContext cc = new ConfigContext();
		cc.set(ConfigContext.CONNECTION, con);
		for( Config c : configList )
		{
			logger.info("Executing db config : " + c);
			// if the config is for smaller version the ignore
			if( c.getVersion() <= major )
			{
				logger.info("Ignoring db config : " + c + " as the major is " + major);
				continue;
			}
			
			// handle class file
			if( c.getType().equals(Config.CLASS_FILE))
			{
				// run the class configurator
				Class<?> klass = al.loadClass(c.getValue());
				Configurator configurator = (Configurator) klass.newInstance();
				configurator.execute(cc);
			}
			// handle sql script
			else if( c.getType().equals(Config.SCRIPT_FILE))
			{
				// run the db script
				String sql = getResourceAsString(al, c.getValue());
				logger.info("Executing query : " + sql);
				Statement s = con.createStatement();
				s.execute(sql);
				s.close();
			}
			else // throw error
			{
				logger.log(Level.WARNING,"The db configuration entry has invalid type : " + c);
				throw new AddonException("The db configuration entry has invalid type : " + c);
			}
		}
	}

	/**
	 * @param al
	 * @param resourceFile
	 * @return
	 * @throws IOException 
	 * @throws AddonException 
	 */
	private String getResourceAsString(AddonLoader al, String resourceFile) throws IOException, AddonException 
	{
		InputStream resourceStream = al.getResourceAsStream(resourceFile);
		if( null == resourceStream )
			throw new AddonException("The resource (" + resourceFile + ") could not be found.");
		
		InputStreamReader isr = new InputStreamReader(resourceStream);
		StringWriter sw = new StringWriter();
		readfully(isr, sw);
		String contents = sw.getBuffer().toString();
		sw.close();
		isr.close();
		return contents;
	}

	/**
	 * @param isr
	 * @param sw
	 * @throws IOException 
	 */
	private void readfully(Reader reader, Writer writer) throws IOException 
	{
		char[] buffer = new char[1024];
		int len = -1 ;
		while( ( len = reader.read(buffer) ) != -1 )
		{
			writer.write(buffer, 0, len);
		}
	}

	private void install(AddonInfo ai) throws AddonException
	{
		if( null == ai )
			throw new AddonException("The addon provided was null." );
		
		synchronized (addonLoaders) {
			if( addonLoaders.get(ai) != null )
				throw new AddonException("The addon is already installed. AddonInfo : " + ai);
			try
			{
				AddonInfoWithBytes ad = AddonInfoManager.getInstance().lookupAddonInfoWithBytes(ai.getJarId());
				
				ByteArrayInputStream bais = new ByteArrayInputStream(ad.getJarBytes());
				File jarFile = File.createTempFile("addon", ".jar", APIUtil.getTBitsTMPDir() );
				FileOutputStream fos = new FileOutputStream(jarFile);
				readfully(bais, fos);
				bais.close();
				// not closing the fos to make sure that the file corresponding to it does not get deleted in running system.
				
				logger.info("Created a temp .jar (" + jarFile.getAbsolutePath() + ") for the addon : " + ai);
				URL jarURL = new URL("file://", "localhost", jarFile.getAbsolutePath());
				AddonLoader al = new AddonLoader(jarURL);
				
				addonLoaders.put(ai, al);
				logger.info("The jar was uploaded successfully : " + ai);
			}
			catch (Exception e) 
			{
				logger.log(Level.WARNING,"Exception while installing the addon : " + ai, e);
				throw new AddonException(e);
			}
		}
	}
	
	/**
	 * This method takes the file placed on the server and the original name of the file. The original name of the file is taken
	 * in case the file has been uploaded to temp folder and the original file name changes, so to make it visually right we also take the 
	 * original name of the file uploaded.
	 * @param jarFile
	 * @param originalName
	 * @return
	 * @throws AddonException
	 */
	public AddonInfo upload(File jarFile, String originalName) throws AddonException
	{
		if( null == jarFile || jarFile.exists() == false )
			throw new AddonException("The jar file provided was either null or does not exists on the system.");
		
		try
		{
			FileInputStream fis = new FileInputStream(jarFile);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			readfully(fis,baos);
			
			byte[] jarBytes = baos.toByteArray();
			AddonInfoWithBytes aiwb = new AddonInfoWithBytes(0, originalName, AddonInfo.STATUS_UPLOADED, jarBytes);
			AddonInfo ai = AddonInfoManager.getInstance().persistAddonInfoWithBytes(aiwb);
			logger.info("AddonManager : Uploading Successfull : fileName " + jarFile.getName() + " originalName : " + originalName );
			
			return ai;
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while uploading the jar file with name : " + originalName, e);
			throw new AddonException("Exception occured while uploading the jar file with name : " + originalName + " :\nCause : " + e.getMessage(), e);
		}
	}

	/**
	 * @param fis
	 * @param baos
	 * @throws IOException 
	 */
	private void readfully(InputStream is, OutputStream os) throws IOException 
	{
		int len = -1; 
		byte[] buffer = new byte[1024];
		while( (len = is.read( buffer )) != -1 )
		{
			os.write(buffer, 0, len);
		}
	}
	/**
	 * @param al
	 * @param con
	 * @throws SQLException 
	 * @throws AddonException 
	 */
	private void insertVersion(AddonLoader al, Connection con) throws SQLException, AddonException {
		PreparedStatement ps = con.prepareStatement(INSERT_VERSIONS);
		ps.setString(1, al.getVersion().trim());
		ps.setString(2, al.getName().trim());
		int affectedRows = ps.executeUpdate();
		if(0 == affectedRows )
			throw new AddonException("No rows affected when inserting addon in current_version.");
		
		ps.close();
	}

	/**
	 * @param al
	 * @param m
	 * @param version
	 * @param con
	 * @throws SQLException 
	 * @throws AddonException 
	 */
	private void updateVersion(AddonLoader al,Connection con) throws SQLException, AddonException 
	{
		PreparedStatement ps = con.prepareStatement(UPDATE_VERSIONS);
		ps.setString(1, al.getVersion().trim());
		ps.setString(2, al.getName().trim());
		int affectedRows = ps.executeUpdate();
		if( 0 == affectedRows )
			throw new AddonException("No rows affected when updating the current_version.");
		
		ps.close();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AddonManager [isInitiazlied=" + isInitiazlied
				+ ",<br /><br /><br />" +
				"addonLoaders=" + addonLoaders + "]";
	}

	
	
	/**
	 * This checks if a AddonRegistry is already created 
	 * @param ar
	 * @throws PersistenceException 
	 
	public void activate(AddonManagerContext amc) throws PersistenceException
	{
		AddonRegistry ar = (AddonRegistry) amc.get(AddonManagerContext.ADDON_REGISTRY);
		if( null == ar )
			throw new AddonException(AddonManagerContext.ADDON_REGISTRY + " must be passed.");
		
		AddonInfo ai = (AddonInfo) amc.get(AddonManagerContext.ADDON_INFO);

		if( null == ai )
		{ 
			ai = AddonInfoManager.getInstance().getAddonInfo(ar.getAddonId());
			amc.set(AddonManagerContext.ADDON_INFO, ai);
		}
		
		if( null == ai || ai.getStatus() == AddonInfo.STATUS_UNREGISTERED || ai.getStatus() == AddonInfo.STATUS_UPLOADED )
		{
			logger.log(Level.WARNING, "The addon jar associated with it is not is correct state. It should exist and should be in REGISTERED state. AddonInfo : " + ai);
			throw new AddonException("The addon jar associated with it is not is correct state. It should exist and should be in REGISTERED state. AddonInfo : " + ai	);
		}
		
		AddonLoader al = (AddonLoader) amc.get(AddonManagerContext.ADDON_LOADER);
		if( null == al )
		{
			al = this.addonLoaders.get(ai);
			amc.set(AddonManagerContext.ADDON_LOADER, al);
		}
		
		if( al == null )
			throw new AddonException("The addon was not first installed/Registered before activating it. You can deactivate it and then Register the jar again and then activate the addon.");
		
		if( ar.getStatus() == AddonRegistry.STATUS_ACTIVATED )
		{
			// as it was already in activated state so its safe to first deactivate it and then make activation again.
			deactivate(amc);
		}
		
		// now we run the entry-point's activate method. if the event registry already exists then we activate it.
		// otherwise we remove it.
		runActivate(amc);
		
		// remove the event registries which were not registered this time (if any).
		// if the event is enabled then register it with the EventManager else user will have to enable the event manually.
		Connection con = amc.getConnection();
		List<EventRegistry> list = null;
		if( null == con )
		{
			 list = EventRegistryManager.getInstance().getAllEventRegistryByAddon(ar);
		}
		else
			list = EventRegistryManager.getInstance().getAllEventRegistryByAddon(ar, con);
		
		if( null != list )
			for( EventRegistry er : list )
			{
				if( er.isActive() == false )
				{
					if( null == con )
						EventRegistryManager.getInstance().delete(er);
					else
						EventRegistryManager.getInstance().delete(er,con);
				}
			}
	}
	*/
	/**
	 * @param ai 
	 * @param ar
	 * @param al 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws AddonException 
	 
	private void runActivate(AddonManagerContext amc) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AddonException 
	{
		AddonRegistry ar = (AddonRegistry) amc.get(AddonManagerContext.ADDON_REGISTRY);
		AddonLoader al = (AddonLoader) amc.get(AddonManagerContext.ADDON_LOADER);
		String entryPointClassName = al.getEntryPointClassName();
		if( null == entryPointClassName )
		{
			logger.info("Entry-Point class was not found.");
			return ;
		}
		
		Class<?> klass = al.loadClass(entryPointClassName);
		AddonEntryPoint aep = (AddonEntryPoint) klass.newInstance();
		
		Connection con = (Connection) amc.get(AddonManagerContext.CONNECTION);
		AddonContext ac = new AddonContext();
		ac.set(AddonContext.ADDON_REGISTRY, ar);
		ac.set(AddonContext.CONNECTION,con);
		
		if( null == con )
		{
			try
			{
				con = DataSourcePool.getConnection();
				con.setAutoCommit(false);
				ac.set(AddonContext.CONNECTION, con);
				runActivator(aep,ac);
				con.commit();
			}
			catch(Exception e)
			{
				try {
					con.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				throw new AddonException("Exception occured while running the activate of " + ar);
			}
			finally
			{
				if( null != con )
					try {
						if( !con.isClosed() )
							con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		else
		{
			runActivator(aep,ac);
		}
	}
*/
	/**
	 * @param ac
	 * @param er
	 
	private void deActivateEvent(AddonContext ac, EventRegistry er) 
	{
		AddonLoader al = 
		EventManager.getInstance().unRegisterHandler(handler, eventClass)
	}
*/
	/**
	 * @param aep
	 * @param ac
	 
	private void runActivator(AddonEntryPoint aep, AddonContext ac) 
	{
		aep.activate(ac);
	}
*/
	/**
	 * @param ar
	
	private void deactivate(AddonManagerContext amc) {
		// TODO Auto-generated method stub
		
	}
 */
	/**
	 * This method
	 * 1. checks if the jar is already loaded, otherwise loads it.
	 * 2. runs the pre registry of entrypoint
	 * 3. makes an entry in current_versions for the addon. if not already there.
	 * 4. runs the required db upgrades
	 * 5. checks if the entry exists in the addon_registry, otherwise makes an entry.
	 * 6. runs the post registry of the entrypoint
	 * 
	 * @param ai
	 * @throws AddonException 
	 
	public AddonRegistry register(AddonInfo ai) throws AddonException 
	{
		if( null == ai )
			throw new AddonException("The addon provided was null." );
		
		//1.
		AddonLoader al = addonLoaders.get(ai);
		if( al == null )
		{
			install(ai);
		}
		
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			if( null == con )
				throw new AddonException("Cannot get a DB connection");
			
			con.setAutoCommit(false);
			
			Class<?> entryPointClass = al.loadClass(al.getEntryPointClassName());
			AddonEntryPoint entryPoint = (AddonEntryPoint) entryPointClass.newInstance();
			AddonContext context = new AddonContext();
			context.set(AddonContext.CONNECTION, con);
			entryPoint.preRegister(context);
			
			AddonRegistry ar = makeAddonRegistry(al,ai, con);
			
			makeDBUpgradesAndSetCurrentVersion(al,con);
			
			entryPoint.postRegister(context);
			
			// make AddonInfo -- Registered
			ai.setStatus(AddonInfo.STATUS_REGISTERED);
			AddonInfoManager.getInstance().persist(ai, con);
			
			con.commit();
			return ar;
		}
		catch(Exception e)
		{
			try {
				con.rollback();
			} catch (SQLException e1) 
			{
				logger.log(Level.WARNING,"The DB connection could not be rollback properly.", e);
			}
			logger.log(Level.WARNING, "Exception occured during registering the addon : " + ai, e);
			throw new AddonException("Exception occured during registering the addon : " + ai, e);
		}
		finally
		{
			if( null != con )
				try {
					if( !con.isClosed() )
						con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	*/
	
	/**
	 * see if the registry already exists, update it, otherwise create a new registry. 
	 * if the registry already exists and it is not deactivated then it generates and error.
	 * @param al
	 * @param con
	 * @throws PersistenceException 
	 * @throws SQLException 
	 * @throws AddonException 
	 
	private AddonRegistry makeAddonRegistry(AddonLoader al,AddonInfo ai, Connection con) throws PersistenceException, SQLException, AddonException 
	{
		AddonRegistry ar = AddonRegistryManager.getInstance().getAddonRegistryByName(al.getName().trim(), con);
		if( null == ar )
		{
			// entry was not found
			// make an entry
			AddonRegistry nar = new AddonRegistry(0, ai.getId(), AddonRegistry.STATUS_INITIALIZED, al.getName().trim(),al.getDescription(),al.getAuthor());
			return AddonRegistryManager.getInstance().persist(nar, con);
		}
		else
		{
			if( ar.getJarId() == ai.getId() ) // this entry was already associated with the given jar file. its ok then
			{
				ar.setStatus(AddonRegistry.STATUS_INITIALIZED);
				return AddonRegistryManager.getInstance().persist(ar);
			}
			else // it was associated with some other jar. then the status should be DEACTIVATED so that we are sure that none of its classes are cached.
			{
				if( ar.getStatus() == AddonRegistry.STATUS_ACTIVATED )
				{
					logger.log(Level.WARNING,"The Addon already exists and is associated with other jar and is active. It must be first deactivated before it can be associated with other jar. AddonRegistry : " + ar + " associated jar " + ai);
					throw new AddonException("The Addon already exists and is associated with other jar and is active. It must be first deactivated before it can be associated with other jar. AddonRegistry : " + ar + " associated jar " + ai	);
				}
				else // status is either initialized or deactivated. its safe to put it back to initialized
				{
					ar.setJarId(ai.getId());
					ar.setAddonDescription(al.getDescription());
					ar.setStatus(AddonRegistry.STATUS_INITIALIZED);
					ar.setAddonAuthor(al.getAuthor());
					
					return AddonRegistryManager.getInstance().persist(ar);
				}
			}
		}
		
	}
*/
	
	
}
