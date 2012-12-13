package example.addons.first;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import transbit.tbits.addons.AddonContext;
import transbit.tbits.addons.AddonEntryPoint;
import transbit.tbits.addons.AddonException;
import transbit.tbits.addons.AddonManager;
import transbit.tbits.events.AddPreEvent;
import transbit.tbits.events.EventAlreadyRegisteredException;
import transbit.tbits.events.EventException;
import transbit.tbits.events.EventManager;
import transbit.tbits.events.IAddPostEvent;
import transbit.tbits.events.IAddPreEvent;
import transbit.tbits.events.IEvent;
import transbit.tbits.events.IPostRequestCommitEvent;
import transbit.tbits.events.IPreRequestCommitEvent;
import transbit.tbits.events.IUpdatePostEvent;
import transbit.tbits.events.IUpdatePreEvent;
import transbit.tbits.events.UpdatePostEvent;

/**
 * 
 */

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class MyEntryPoint implements AddonEntryPoint{

	/* (non-Javadoc)
	 * @see transbit.tbits.addons.AddonEntryPoint#preRegister(transbit.tbits.addons.AddonContext)
	 */
	@Override
	public void preRegister(AddonContext addonContext) throws AddonException {
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.addons.AddonEntryPoint#postRegister(transbit.tbits.addons.AddonContext)
	 */
	@Override
	public void postRegister(AddonContext addonContext) throws AddonException {
		
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.addons.AddonEntryPoint#activate(transbit.tbits.addons.AddonContext)
	 */
	@Override
	public void activate(AddonContext addonContext) throws AddonException 
	{
		AddonManager.registerHandler(addonContext, PostCommitHandler.class,IPostRequestCommitEvent.class);
		AddonManager.registerHandler(addonContext, AddPostHandler.class,IAddPostEvent.class);
		AddonManager.registerHandler(addonContext, AddPreHandler.class,IAddPreEvent.class);
		AddonManager.registerHandler(addonContext, APreHandler.class,AddPreEvent.class);
		AddonManager.registerHandler(addonContext, PreCommitHandler.class,IPreRequestCommitEvent.class);
		AddonManager.registerHandler(addonContext, UpdatePostHandler.class,IUpdatePostEvent.class);
		AddonManager.registerHandler(addonContext, UpdatePreHandler.class,IUpdatePreEvent.class);
		AddonManager.registerHandler(addonContext, UPostHandler.class,UpdatePostEvent.class);
		AddonManager.registerHandler(addonContext, AllEventHandler.class,IEvent.class);
		try {
			EventManager.getInstance().registerUnManagedHandler(UnManagedPostHandler.class, IPostRequestCommitEvent.class);
		} catch (EventAlreadyRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EventException e) {
			throw new AddonException(e);
		}
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.addons.AddonEntryPoint#postDeactivate(transbit.tbits.addons.AddonContext)
	 */
	@Override
	public void postDeactivate(AddonContext addonContext) throws AddonException {
		
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.addons.AddonEntryPoint#postUnregister(transbit.tbits.addons.AddonContext)
	 */
	@Override
	public void postUnregister(AddonContext addonContext) throws AddonException 
	{
		Connection con = addonContext.getConnection();
		
		try 
		{
			Statement s = con.createStatement();
			s.execute("drop table example_addon_data");
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AddonException(e);
		}
	}
}
