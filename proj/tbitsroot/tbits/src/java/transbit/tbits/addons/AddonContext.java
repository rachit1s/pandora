/**
 * 
 */
package transbit.tbits.addons;

import java.sql.Connection;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class AddonContext extends TbitsContext
{
	public static final String CONNECTION = "Connection";
	public static final String ADDON_INFO = "addonInfo";
	
	public AddonInfo getAddonInfo()
	{
		return (AddonInfo) this.get(ADDON_INFO);
	}
	
	public Connection getConnection()
	{
		return (Connection) this.get(CONNECTION);
	}
}
