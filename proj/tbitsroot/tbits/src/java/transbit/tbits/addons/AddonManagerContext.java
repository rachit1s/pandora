/**
 * 
 */
package transbit.tbits.addons;

import java.sql.Connection;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class AddonManagerContext extends TbitsContext{
	public static final String CONNECTION = "connection";
	public static final String ADDON_LOADER = "addonLoader";
	public static final String ADDON_INFO = "addonInfo";
	
	public AddonInfo getAddonInfo()
	{
		return (AddonInfo) this.get(ADDON_INFO);
	}
	
	public AddonLoader getAddonLoader()
	{
		return (AddonLoader) this.get(ADDON_LOADER);
	}
	
	public Connection getConnection()
	{
		return (Connection) this.get(CONNECTION);
	}
}
