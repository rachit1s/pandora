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
import transbit.tbits.common.DataSourcePool;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class DeleteData implements Configurator{

	/* (non-Javadoc)
	 * @see transbit.tbits.addons.Configurator#execute(transbit.tbits.addons.ConfigContext)
	 */
	@Override
	public void execute(ConfigContext configContext) throws AddonException 
	{
		Connection con = (Connection) configContext.get(ConfigContext.CONNECTION);
		try 
		{
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("select * from example_addon_data");
			System.out.println("Number of rows in table currently : ");
			if( rs != null )
				while(rs.next())
					System.out.println(rs.getInt(1)+"," + rs.getString(2));
			
			rs.close();
			
			s.execute("delete from example_addon_data");
			s.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new AddonException(e);
		}
	}

}
