package addon.event.proxy;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.addons.AddonException;
import transbit.tbits.addons.AddonInfo;
import transbit.tbits.addons.AddonInfoManager;
import transbit.tbits.addons.AddonManager;
import transbit.tbits.addons.Result;
import transbit.tbits.api.IProxyServlet;
import transbit.tbits.common.Configuration;
/**
 * 
 */
import transbit.tbits.events.EventAlreadyRegisteredException;
import transbit.tbits.events.EventException;
import transbit.tbits.events.EventManager;
import transbit.tbits.events.EventRegistry;
import transbit.tbits.events.EventRegistryManager;
import transbit.tbits.exception.PersistenceException;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
<pre>
	Usage : 
	url : http[s]://server:port/proxy/addons_and_events?object=[addon/event/EventManager/AddonManager]&action=[...]&[other_parameters]
	the valid values of various parameters 
	object=addon
		action=list
		action=upload 
			jarname=name_of_the_jar_file_placed_in build/tmp_addons folder
		action=[register/activate/deactivate/unregister/unload]
			id=jar_id for that entry in db.
	object=event
		action=list
		action=toggle
			id=event_id for that entry in db.
	object=EventManager
		action=list
	object=AddonManager
		action=list
		
	Explanations:
		Please read documentation for understanding the addon cycle
		Event's action=toggle means that if event is in enabled state then it will be made disabled and viceversa
		EventManager : note capitalization should be there. The action=list shows the currently registered events in the runtime of Java. Its purpose is to diagnose any problem
		AddonManager : note capitalization should be there. The action=list shows the currently loaded addons in the runtime of Java. Its purpose is to diagnose any problem
<pre>
 */
public class AddonEventProxy implements IProxyServlet{

	public static final String USAGE = "<pre>\n"+
			"	Usage : \n"+
			"	url : http[s]://server:port/proxy/addons_and_events?object=[addon/event/EventManager/AddonManager]&action=[...]&[other_parameters]\n"+
			"	the valid values of various parameters \n"+
			"	object=addon\n"+
			"		action=list\n"+
			"		action=upload \n"+
			"			jarname=name_of_the_jar_file_placed_in build/tmp_addons folder\n"+
			"		action=[register/activate/deactivate/unregister/unload]\n"+
			"			id=jar_id for that entry in db.\n"+
			"	object=event\n"+
			"		action=list\n"+
			"		action=toggle\n"+
			"			id=event_id for that entry in db.\n"+
			"		action=setorder\n" +
			"			id=event_id for that entry in db.\n" +
			"			order=the new integer order you want to set for this event.\n"+
			"	object=EventManager\n"+
			"		action=list\n"+
			"	object=AddonManager\n"+
			"		action=list\n"+
			"		\n"+
			"	Explanations:\n"+
			"		Please read documentation for understanding the addon cycle\n"+
			"		Event's action=toggle means that if event is in enabled state then it will be made disabled and viceversa\n"+
			"		EventManager : note capitalization should be there. The action=list shows the currently registered events in the runtime of Java. Its purpose is to diagnose any problem\n"+
			"		AddonManager : note capitalization should be there. The action=list shows the currently loaded addons in the runtime of Java. Its purpose is to diagnose any problem\n"+
			"<pre>";
	
	public static final String ACTION = "action";
	public static final String OBJECT = "object";
	public static final String ID = "id";
	public static final String ORDER = "order";
	public static final String ADDON_TMP_FOLDER 	= "tmp_addons";
	public static final String ACTION_ADDON_UPLOAD 		= "upload";
	public static final String JAR_NAME = "jarname";
	public static final String ACTION_ADDON_REGISTER 	= "register";
	public static final String ACTION_ADDON_ACTIVATE 	= "activate";
	public static final String ACTION_ADDON_DEACTIVATE 	= "deactivate";
	public static final String ACTION_ADDON_UNREGISTER 	= "unregister";
	public static final String ACTION_ADDON_UNLOAD 		= "unload";
	public static final String ACTION_EVENT_TOGGLE_STATE = "toggle";
	public static final String ACTION_EVENT_SET_ORDER = "setorder";
	public static final String ACTION_LIST = "list";
	public static final String OBJECT_EVENT = "event";
	public static final String OBJECT_ADDON = "addon";
	public static final String OBJECT_EVENTMANAGER = "EventManager";
	public static final String OBJECT_ADDON_MANAGER = "AddonManager";
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IProxyServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		try
		{
			String action = request.getParameter(ACTION);
			String object = request.getParameter(OBJECT);
			if( null == action || null == object )
			{
				writeOutput("'object' and 'action' parameters are compulsory",response);
				return;
			}
				
			if(object.equals(OBJECT_ADDON))
			{
				handleAddon(request,response);
			}
			else if ( object.equals(OBJECT_EVENT) )
			{
				handleEvent(request,response);
			}
			else if( object.equals(OBJECT_EVENTMANAGER))
			{
				handleEventManager(request,response);
			}
			else if( object.equals(OBJECT_ADDON_MANAGER))
			{
				handleAddonManager(request,response);
			}
			else
			{
				writeOutput( "only 'event', 'EventManager', 'addon' and 'AddonManager' values are supported for 'object' parameter",response);
				return;
			}
		}
		catch (IOException e) {
			throw e;
		} catch (Exception e) 
		{
			e.printStackTrace();
			writeOutput(e.getMessage(), response);
		}
	}		

	/**
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	private void handleAddonManager(HttpServletRequest request,
			HttpServletResponse response) throws IOException 
	{
		String action = request.getParameter(ACTION);
		if( !action.equals(ACTION_LIST) )
		{
			writeOutput("Only action allowed for " + OBJECT_ADDON_MANAGER + " is " + ACTION_LIST,response);
			return;
		}
		
		writeOutput(AddonManager.getInstance().toString(),response);
	}

	/**
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	private void handleEventManager(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String action = request.getParameter(ACTION);
		if( !action.equals(ACTION_LIST) )
		{
			writeOutput("Only action allowed for " + OBJECT_EVENTMANAGER + " is " + ACTION_LIST,response);
			return;
		}
		
		writeOutput(EventManager.getInstance().toString(),response);
	}

	/**
	 * @param request
	 * @param response
	 * @throws AddonException 
	 * @throws IOException 
	 * @throws PersistenceException 
	 */
	private void handleAddon(HttpServletRequest request,
			HttpServletResponse response) throws AddonException, PersistenceException, IOException 
	{
		String action = request.getParameter(ACTION);
		if( action.equals(ACTION_LIST))
		{
			handleAddonList(request,response);
		}
		else 
		{
			int state = getState(action);
			if( state == 0 )
			{
				writeOutput("The action mentioned is not valid. Valid actions on addon are : " + ACTION_ADDON_UPLOAD 		
					+ "," +	ACTION_ADDON_REGISTER 	
					+ "," +	ACTION_ADDON_ACTIVATE 	
					+ "," +	ACTION_ADDON_DEACTIVATE 	
					+ "," +	ACTION_ADDON_UNREGISTER 	
					+ "," +	ACTION_ADDON_UNLOAD,response 		 );
				return;
			}
			
			if( state == 1 )
			{
				handleAddonUpload(request,response);
			}
			else
			{
				String id = request.getParameter(ID);
				if( null == id )
				{
					writeOutput("'"+ ID + "' parameter is compulsory for all action other than " + ACTION_LIST +  " and " + ACTION_ADDON_UPLOAD + " on an addon.", response  );
					return;
				}
				Long aiid = Long.parseLong(id);
				AddonInfo ai = AddonInfoManager.getInstance().lookupAddonInfo(aiid);
				if( null == ai )
					throw new AddonException("Addon not found with id : " + aiid );
				
				ai.setStatus(state);
				Result result = AddonManager.getInstance().saveAddonInfo(ai);
				writeOutput("The result of your action is : " + result.getMessage(), response );
				return;
			}
		}
	}

	/**
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws AddonException 
	 */
	private void handleAddonUpload(HttpServletRequest request,
			HttpServletResponse response) throws IOException, AddonException 
	{
		String jarName = request.getParameter(JAR_NAME);
		if( null == jarName )
		{
			writeOutput("'" + JAR_NAME +"' parameter is compulsory for " + ACTION_ADDON_UPLOAD  + " of addon.", response);
			return;
		}
		
		String tempAddonFolderPath = Configuration.findAbsolutePath(ADDON_TMP_FOLDER);
		if( null == tempAddonFolderPath )
			throw new AddonException("You have to create a folder $TBITS_INSTALL/build/tmp_addons and put your jar-files in that place.");
		
		File addonFolder = new File(tempAddonFolderPath);
		if(addonFolder.exists() == false )
		{
			writeOutput("The folder " + addonFolder + " does not exists on the server. Please create it and put you jar in this folder first.",response);
			return;
		}
		
		File jarFile = new File(addonFolder,jarName);
		if( jarFile.exists() == false )
		{
			writeOutput("The jar-file with name " + jarName + " does not exist in folder " + tempAddonFolderPath + ". Please put your file there first and then upload.",response);
			return;
		}
		
		AddonInfo ai = AddonManager.getInstance().upload(jarFile, jarName);
		writeOutput("The addon file was successfully uploaded. Details : AddonInfo : "+ ai, response );
		return ;
	}

	/**
	 * @param request
	 * @param response
	 * @throws PersistenceException 
	 * @throws IOException 
	 */
	private void handleAddonList(HttpServletRequest request,
			HttpServletResponse response) throws PersistenceException, IOException 
	{
		StringBuffer sb = new StringBuffer("AddonInfos are :<br />" );
		List<AddonInfo> addons = AddonInfoManager.getInstance().lookupAllAddonInfos();
		for( AddonInfo ai : addons )
		{
			sb.append(getAddonHtml(ai));
		}
		
		writeOutput(sb.toString(),response);
	}

	/**
	 * @param ai
	 * @return
	 */
	private Object getAddonHtml(AddonInfo ai) {
		return ai.toString() + "<br />";
	}

	/**
	 * @param id2
	 * @return
	 */
	private int getState(String state) 
	{
		/*
	public static final int STATUS_UPLOADED     = 1;
	public static final int STATUS_REGISTERED   = 2;
	public static final int STATUS_ACTIVATED    = 3;
	public static final int STATUS_DEACTIVATED  = 4;
	public static final int STATUS_UNREGISTERED = 5;
	public static final int STATUS_UNLOADED = 6; // this will never be persisted in the DB. It will always be deleted.
		 */
		if( state.equals(ACTION_ADDON_UPLOAD))
			return 1 ;
		else if( state.equals(ACTION_ADDON_REGISTER))
			return 2;
		else if( state.equals(ACTION_ADDON_ACTIVATE))
			return 3;
		else if( state.equals(ACTION_ADDON_DEACTIVATE))
			return 4;
		else if( state.equals(ACTION_ADDON_UNREGISTER))
			return 5;
		else if( state.equals(ACTION_ADDON_UNLOAD))
			return 6;
		
		return 0;
	}

	/**
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws PersistenceException 
	 * @throws EventException 
	 * @throws EventAlreadyRegisteredException 
	 * @throws NumberFormatException 
	 */
	private void handleEvent(HttpServletRequest request,
			HttpServletResponse response) throws IOException, PersistenceException, NumberFormatException, EventAlreadyRegisteredException, EventException 
	{
		String action = request.getParameter(ACTION);
		if( action.equals(ACTION_LIST))
		{
			handleEventList(request,response);
		}
		else if( action.equals(ACTION_EVENT_TOGGLE_STATE))
		{
			handleEventToggle(request,response);
		}
		else if ( action.equals(ACTION_EVENT_SET_ORDER))
		{
			handleEventOrder(request,response);
		}
		else
		{
			writeOutput("only '" + ACTION_EVENT_TOGGLE_STATE +"' and '" + ACTION_LIST + "' are supported for object = " + OBJECT_EVENT,response );
			return;
		}
	}

	/**
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws PersistenceException 
	 * @throws NumberFormatException 
	 * @throws EventException 
	 */
	private void handleEventOrder(HttpServletRequest request,
			HttpServletResponse response) throws IOException, NumberFormatException, PersistenceException, EventException 
	{
		String id = request.getParameter(ID);
		if( null == id )
		{
			writeOutput("'"+ID + "' parameter is compulsory for setorder an event.",response);
			return;
		}
		
		String order = request.getParameter(ORDER);
		if( null == order )
		{
			writeOutput("'"+ ORDER + "' parameter is compulsory for setorder an event.",response);
			return;
		}
		
		EventRegistry er = EventRegistryManager.getInstance().lookupEventRegistryById(Long.parseLong(id));
		if( null == er )
		{	
			writeOutput("Cannot find event with id : " + id,response);
			return;
		}
		
		er.setEventOrder(Integer.parseInt(order));
		EventManager.getInstance().changeEventOrder(er);
		
		writeOutput("The order was changed successfully.", response);
	}

	/**
	 * @param request
	 * @param response
	 * @throws PersistenceException 
	 * @throws NumberFormatException 
	 * @throws IOException 
	 * @throws EventException 
	 * @throws EventAlreadyRegisteredException 
	 */
	private void handleEventToggle(HttpServletRequest request,
			HttpServletResponse response) throws NumberFormatException, PersistenceException, IOException, EventAlreadyRegisteredException, EventException 
	{
		String id = request.getParameter(ID);
		if( null == id )
		{
			writeOutput("'"+ID + "' parameter is compulsory for toggle an event.",response);
			return;
		}

		EventRegistry er = EventRegistryManager.getInstance().lookupEventRegistryById(Long.parseLong(id));
		if( null == er )
		{	
			writeOutput("Cannot find event with id : " + id,response);
			return;
		}
		
		EventManager.getInstance().toggleManagedEvent(er);
		
		writeOutput("Your action was conducted successfully.", response);
		return;
	}

	/**
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws PersistenceException 
	 */
	private void handleEventList(HttpServletRequest request,
			HttpServletResponse response) throws IOException, PersistenceException 
	{
			List<EventRegistry> events = EventRegistryManager.getInstance().lookupAllEventRegistry();
			StringBuffer sb = new StringBuffer();
			for( EventRegistry er : events )
			{
				sb.append(getEventHtml(er));
			}
			
			writeOutput("EventRegistry Entries:<br /><br />" + sb.toString(),response);
			return;
	}

	/**
	 * @param er
	 * @return
	 */
	private Object getEventHtml(EventRegistry er) 
	{
		return er.toString() + "<br />";
	}

	/**
	 * @param string
	 * @param response 
	 * @throws IOException 
	 */
	private void writeOutput(String message, HttpServletResponse response) throws IOException 
	{
		PrintWriter pw = response.getWriter();
		pw.write(message + "<br /><br /><br /><br /><br />" + USAGE);
		pw.flush();
		pw.close();
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IProxyServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request,response);
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IProxyServlet#getName()
	 */
	@Override
	public String getName() {
		return "addons_and_events";
	}

}
