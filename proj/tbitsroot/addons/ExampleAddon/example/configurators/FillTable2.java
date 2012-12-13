/**
 * 
 */
package example.configurators;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import transbit.tbits.addons.AddonException;
import transbit.tbits.addons.ConfigContext;
import transbit.tbits.addons.Configurator;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class FillTable2 implements Configurator{

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
			ResultSet rs = statement.executeQuery("select * from example_addon_data");
			System.out.println("Number of rows in table currently : ");
			if( rs != null )
				while(rs.next())
					System.out.println(rs.getInt(1)+"," + rs.getString(2));
			
			rs.close();
			statement.execute("insert into example_addon_data values ('nitiraj','singh')");
			statement.execute("insert into example_addon_data values ('singh','rathore')");
			statement.execute("insert into example_addon_data values ('rathore',null)");
			statement.close();
		}
		catch(Exception e)
		{
			throw new AddonException(e);
		}
	}

}
