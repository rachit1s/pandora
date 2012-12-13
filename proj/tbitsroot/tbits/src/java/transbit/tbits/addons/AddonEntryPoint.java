/**
 * 
 */
package transbit.tbits.addons;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public interface AddonEntryPoint 
{
	/** 
	 * This will be executed before registering the addon with the system. i.e. before making any entry in the current_versions
	 * table and in the table addon_registry
	 * @param addonContext
	 * @throws AddonException 
	 */
	public void preRegister(AddonContext addonContext) throws AddonException;
	
	/**
	 * This will be executed after registering the addon with the system
	 * @param addonContext
	 */
	public void postRegister(AddonContext addonContext) throws AddonException;
	
	/**
	 * Before executing this method all the classes of the module and related libraries are guaranteed to be loaded
	 * also its guaranteed that all the db updates are done and successful.
	 * So in this method the addon author should register all the events with the system.
	 * Note registering the events with the system means that an entry for that event will be created
	 * in the event registry table. If an entry with the same parameters already exists then that entry 
	 * will be kept as is and marked activated. The registration of Events with the 
	 * EventManager is handled separately and takes into account the if the registered events are enabled or disabled.
	 * Activation of an addon occurs manually or when the system starts and finds an addon entry which is in activated state
	 * @param addonContext
	 */
	public void activate( AddonContext addonContext)  throws AddonException;
	
	/**
	 * This method is called after the deactivation of the addon. Deactivation occurs when either the addon 
	 * is deactivated manually or  when the jar corresponding to the addon is unloaded.
	 * @param addonContext
	 */
	public void postDeactivate( AddonContext addonContext)  throws AddonException;

	/**
	 * This method is called after the removing entries from the current_version and addon_registry about the addon.
	 * If the addon is currently deactivated
	 * @param addonContext
	 */
	public void postUnregister(AddonContext addonContext)  throws AddonException;
}
