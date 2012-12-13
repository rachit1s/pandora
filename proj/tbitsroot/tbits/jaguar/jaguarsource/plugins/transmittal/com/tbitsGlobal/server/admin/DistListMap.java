package transmittal.com.tbitsGlobal.server.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import transmittal.com.tbitsGlobal.client.models.TrnDistList;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;

public class DistListMap {
	
	/**
	 * Get the Distribution list for the specified process
	 * @param process
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static List<TrnDistList> getDistList(TrnProcess process) throws TbitsExceptionClient{
		List<TrnDistList> resp = new ArrayList<TrnDistList>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			
			String sql = "SELECT * FROM trn_distribution_table_column_config where trn_process_id = ?";
			
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, process.getProcessId());
			
			ResultSet rs = ps.executeQuery();
			
			if(null != rs) {
				while(rs.next()) {
					String name = rs.getString("name");
					String displayName = rs.getString("display_name");
					int dataTypeId = rs.getInt("data_type_id");
					String fieldConfig = rs.getString("field_config");
					boolean isEditable = rs.getBoolean("is_editable");
					boolean isActive = rs.getBoolean("is_active");
					int order = rs.getInt("column_order");
					
					TrnDistList param = new TrnDistList();
					param.setProcessId(process.getProcessId());
					param.setName(name);
					param.setDisplayName(displayName);
					param.setDataType(dataTypeId);
					param.setFieldConfig(fieldConfig);
					param.setIsEditable(isEditable);
					param.setIsActive(isActive);
					param.setOrder(order);
					
					resp.add(param);
				}	
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                    throw new TbitsExceptionClient(sqle);
                }
                connection = null;
            }
        }
		
		return resp;
	}
	
	
	public static List<TrnDistList> saveDistLists(TrnProcess process, List<TrnDistList> params) throws TbitsExceptionClient{
		List<TrnDistList> resp = new ArrayList<TrnDistList>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "DELETE FROM trn_distribution_table_column_config where trn_process_id = ?";
			
			PreparedStatement cs = connection.prepareStatement(sql);
			cs.setInt(1, process.getProcessId());
			
			cs.execute();
			cs.close();
			
			for(TrnDistList param : params){
				resp.add(saveDistList(connection, process, param));
			}
			connection.commit();
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				//TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				//TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			throw new TbitsExceptionClient(e);
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                    throw new TbitsExceptionClient(sqle);
                }

                connection = null;
            }
        }
		
		return resp;
	}
	
	
	private static TrnDistList saveDistList(Connection connection, TrnProcess process, TrnDistList param) throws SQLException{
		String sql = "INSERT INTO trn_distribution_table_column_config " +
				"(trn_process_id, name, display_name, data_type_id, field_config, is_editable, is_active, column_order) " +
				"VALUES(?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		System.out.println( param.getName());
		ps.setInt(1, process.getProcessId());
		ps.setString(2, param.getName());
		ps.setString(3, param.getDisplayName());
		ps.setInt(4, param.getDataType());
		ps.setString(5, param.getFieldConfig());
		if(param.getIsEditable()==null)
			param.setIsEditable(false);
		ps.setBoolean(6, param.getIsEditable());
		
		if(param.getIsActive()==null)
			param.setIsActive(false);
		
		ps.setBoolean(7, param.getIsActive());
		ps.setInt(8, param.getOrder());
		
		ps.execute();
		ps.close();
		
		param.setProcessId(process.getProcessId());
		param.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		
		return param;
	}
}
