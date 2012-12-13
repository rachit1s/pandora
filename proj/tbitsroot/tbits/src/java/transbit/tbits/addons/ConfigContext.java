/**
 * 
 */
package transbit.tbits.addons;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class ConfigContext extends TbitsContext
{
	/**
	 * This is the key to obtain the current connection used during upgrade process using {@link ConfigContext#get(Object)}
	 * Using this connection makes sure that if {@link AddonManager#register(AddonInfo)} fails then
	 * all the changes in the DB are safely rollbacked.
	 */
	public static final String CONNECTION = "connection";
}
