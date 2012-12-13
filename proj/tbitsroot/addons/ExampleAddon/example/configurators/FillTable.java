/**
 * 
 */
package example.configurators;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import transbit.tbits.addons.AddonException;
import transbit.tbits.addons.ConfigContext;
import transbit.tbits.addons.Configurator;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class FillTable implements Configurator{

	/* (non-Javadoc)
	 * @see transbit.tbits.addons.Configurator#execute(transbit.tbits.addons.ConfigContext)
	 */
	@Override
	public void execute(ConfigContext configContext) throws AddonException 
	{
		try
		{
			Connection con = (Connection) configContext.get(ConfigContext.CONNECTION);
			
			Statement statement = con.createStatement();
			statement.execute("insert into example_addon_data values ('nitiraj')");
			statement.execute("insert into example_addon_data values ('singh')");
			statement.execute("insert into example_addon_data values ('rathore')");
			statement.close();
		}
		catch(Exception e)
		{
			throw new AddonException(e);
		}
	}

}
