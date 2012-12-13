package transbit.tbits.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;

public class TemplatesHash {
//	private String id;
//	private String template;
//	
//	public TemplateConfig(String id, String template) {
//		this.id = id;
//		this.template = template;
//	}
//	void setId(String id) {
//		this.id = id;
//	}
//	String getId() {
//		return id;
//	}
//	void setTemplate(String template) {
//		this.template = template;
//	}
//	String getTemplate() {
//		return template;
//	}
	
	/**
	 * @return Retuns a hashtable of format_id verses template
	 */
	public static Hashtable<Integer, String> lookupAllFromDB(int systemId) throws SQLException
	{
		Connection aCon = null;
		PreparedStatement preparedStatement;
		try
		{
			Hashtable<Integer, String> table = new Hashtable<Integer, String>();
			
				aCon = DataSourcePool.getConnection();
				preparedStatement = aCon.prepareStatement("SELECT * from msg_format where sys_id = ?");
				preparedStatement.setInt(1, systemId);
				ResultSet rs = preparedStatement.executeQuery();
				while(rs.next())
		        {
		        	table.put(rs.getInt("format_id"), rs.getString("template"));
		        }
			return table;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
		finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {
                }
            }
        }
	}
}


