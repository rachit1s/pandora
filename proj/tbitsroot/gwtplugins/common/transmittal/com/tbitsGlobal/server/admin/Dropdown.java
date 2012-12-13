package transmittal.com.tbitsGlobal.server.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import transmittal.com.tbitsGlobal.client.models.TrnDropdown;
import transmittal.com.tbitsGlobal.server.TransmittalUtils;
/**
 * Utility class for manipulating entries in trn_dropdown table
 * @author devashish
 *
 */
public class Dropdown {
	
	
	/**
	 * Insert/Modify entries in the trn_dropdown table in the specified ba
	 * @param ba
	 * @param entries
	 * @return list of modified entries
	 * @throws TbitsExceptionClient 
	 */
	public static List<TrnDropdown> saveDropdownTable(BusinessAreaClient ba, List<TrnDropdown> entries) throws TbitsExceptionClient{
		List<TrnDropdown> savedList = new ArrayList<TrnDropdown>();
		
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "delete from trn_dropdown where src_sys_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, ba.getSystemId());
			
			ps.execute();
			ps.close();
			for(TrnDropdown entry : entries){
				savedList.add(insertIntoTrnDropdown(connection, entry));
			}
			
			connection.commit();
		}catch (SQLException e){
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				//TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient();
		}finally {
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
		
		return savedList;
	}
	
	private static TrnDropdown insertIntoTrnDropdown(Connection connection, TrnDropdown entry) throws SQLException{
		String sql = "insert into trn_dropdown " +
					" (src_sys_id, id, name, sort_order) " +
					" values(?,?,?,?) ";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, entry.getSrcBa().getSystemId());
		ps.setInt(2, entry.getDropdownId());
		ps.setString(3, entry.getProcessName());
		ps.setInt(4, entry.getSortOrder());
		
		ps.execute();
		ps.close();
		
		entry.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		return entry;
	}
	/**
	 * Get the list of entries in trn_dropdown table for the specified business area
	 * @param ba
	 * @return List of dropdown entries for 'ba'
	 * @throws TbitsExceptionClient
	 */
	public static List<TrnDropdown> getDropdownTable(BusinessAreaClient ba) throws TbitsExceptionClient{
		List<TrnDropdown> dropdownOptions = new ArrayList<TrnDropdown>();
		
		Connection connection = null;
		
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "select * from trn_dropdown where src_sys_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, ba.getSystemId());
			
			ResultSet rs = ps.executeQuery();
			if(null != rs){
				while(rs.next()){
					int srcSysId 	= rs.getInt("src_sys_id");
					int dropdownId 	= rs.getInt("id");
					String name 	= rs.getString("name");
					int sortOrder	= rs.getInt("sort_order");
					
					if(null == TransmittalUtils.getBAforSysId(srcSysId))
						continue;
					BusinessAreaClient srcBa = TransmittalUtils.getBAforSysId(srcSysId);
					
					TrnDropdown dropdownEntry = new TrnDropdown();
					dropdownEntry.setSrcBa(srcBa);
					dropdownEntry.setDropdownId(dropdownId);
					dropdownEntry.setProcessName(name);
					dropdownEntry.setSortOrder(sortOrder);
					
					dropdownOptions.add(dropdownEntry);
				}
			}
			
			ps.close();
		}catch(SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				//TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}catch(Exception e){
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		
		return dropdownOptions;
	}
	
	/**
	 * Get the dropdown values for all business areas
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static List<TrnDropdown> getAllDropdownEntries() throws TbitsExceptionClient{
		List<TrnDropdown> dropdownOptions = new ArrayList<TrnDropdown>();
		
		Connection connection = null;
		
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "select * from trn_dropdown";
			PreparedStatement ps = connection.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			if(null != rs){
				while(rs.next()){
					int srcSysId 	= rs.getInt("src_sys_id");
					int dropdownId 	= rs.getInt("id");
					String name 	= rs.getString("name");
					int sortOrder	= rs.getInt("sort_order");
					
					if(null == TransmittalUtils.getBAforSysId(srcSysId))
						continue;
					BusinessAreaClient srcBa = TransmittalUtils.getBAforSysId(srcSysId);
					
					TrnDropdown dropdownEntry = new TrnDropdown();
					dropdownEntry.setSrcBa(srcBa);
					dropdownEntry.setDropdownId(dropdownId);
					dropdownEntry.setProcessName(name);
					dropdownEntry.setSortOrder(sortOrder);
					
					dropdownOptions.add(dropdownEntry);
				}
			}
			
			ps.close();
		}catch(SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				//TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}catch(Exception e){
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		
		return dropdownOptions;
	}
}
