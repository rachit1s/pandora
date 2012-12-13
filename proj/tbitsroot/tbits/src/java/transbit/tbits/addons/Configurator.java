/**
 * 
 */
package transbit.tbits.addons;

import java.sql.Connection;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * This interface represents the configurator which can be given in the {@link AddonLoader#ADDON_DB_CONFIG_FILE} file.
 * Any class that implements this interface can appear as value for {@link Config#CLASS_FILE} TYPE
 * Use the {@link Connection} object passed in {@link ConfigContext#CONNECTION} to do all the db related activity, so that 
 * if the registration fails all the changes are safely reverted back.
 * You can obtain the {@link Connection} using {@link ConfigContext#get(Object)} method and passing the  {@link ConfigContext#CONNECTION}
 * Note that if it throws any exception it will be propogated and the upgrade will stop and rolledback.
 */
public interface Configurator 
{
	public void execute(ConfigContext configContext) throws AddonException;
}
