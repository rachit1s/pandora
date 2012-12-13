package transmittal.com.tbitsGlobal.server.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import transbit.tbits.common.DataSourcePool;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;

/**
 * Utility class for editing Trnsmittal Process Values
 *
 */
public class TrnProcesses {
	
	public static List<TrnProcess> saveTransmittalProcesses(List<TrnProcess> processes) throws TbitsExceptionClient{
		List<TrnProcess> resp = new ArrayList<TrnProcess>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			
			for(TrnProcess process : processes){
				if(process.getProcessId() == 0){
					try {
						TrnProcess p = addTransmittalProcess(process);
						resp.add(p);
					} catch (TbitsExceptionClient e) {
						e.printStackTrace();
						process.set(IBulkUpdateConstants.RESPONSE_STATUS, e.getMessage());
						resp.add(process);
					}
					
				}else{
					try {
						TrnProcess p = editTransmittalProcess(process);
						resp.add(p);
					} catch (TbitsExceptionClient e) {
						e.printStackTrace();
						process.set(IBulkUpdateConstants.RESPONSE_STATUS, e.getMessage());
						resp.add(process);
					}
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
	
	
	private static TrnProcess addTransmittalProcess(TrnProcess process) throws TbitsExceptionClient{
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "SELECT max(trn_process_id) from trn_processes";
			
			PreparedStatement ps = connection.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			
			int maxProcessId = 0;
			if(null != rs) {
				if(rs.next()) {
					maxProcessId = rs.getInt(1) + 1;
				}
				rs.close();
			}
			ps.close();
			
			int dropdownId = getOrAddDropdownId(connection, process);
			
			sql = "INSERT INTO trn_processes " + 
					 "(src_sys_id " + 
					 ",trn_process_id " + 
					 ",description " + 
					 ",trn_max_sn_key " + 
					 ",dtn_sys_id " + 
					 ",dtr_sys_id " + 
					 ",trn_dropdown_id) " + 
					 "VALUES(?,?,?,?,?,?,?)";
			
			ps = connection.prepareStatement(sql);
			ps.setInt(1, process.getSrcBA().getSystemId());
			ps.setInt(2, maxProcessId);
			ps.setString(3, process.getDescription());
			ps.setString(4, process.getSerialKey());
			ps.setInt(5, process.getDTNBA().getSystemId());
			ps.setInt(6, process.getDTRBA().getSystemId());
			ps.setInt(7, dropdownId);
			ps.execute();
			ps.close();
			
			connection.commit();
			
			process.setProcessId(maxProcessId);
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
		process.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.ADDED);
		return process;
	}
	
	/**
	 * Get the Dropdown Id for the process if exists. Else insert the dropdown id for the process and return the i
	 * new inserted value
	 * @param connection
	 * @param process
	 * @return
	 * @throws SQLException
	 */
	private static int getOrAddDropdownId(Connection connection, TrnProcess process) throws SQLException{
		String sql = "SELECT id from trn_dropdown where name = ?";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setString(1, process.getName());
		ResultSet rs = ps.executeQuery();
		
		int dropdownId = 0;
		if(null != rs) {
			if(rs.next()) {
				dropdownId = rs.getInt(1);
			}
			rs.close();
		}
		ps.close();
		
		if(dropdownId == 0){
			sql = "SELECT max(id) from trn_dropdown";
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			
			if(null != rs) {
				if(rs.next()) {
					dropdownId = rs.getInt(1) + 1;
				}
				rs.close();
			}
			ps.close();
			
			sql = "INSERT INTO trn_dropdown " + 
			 "(src_sys_id " + 
			 ",id " + 
			 ",name " + 
			 ",sort_order) " + 
			 "VALUES(?,?,?,?)";
			
			ps = connection.prepareStatement(sql);
			ps.setInt(1, process.getSrcBA().getSystemId());
			ps.setInt(2, dropdownId);
			ps.setString(3, process.getName());
			ps.setInt(4, process.getOrder());
			ps.execute();
			ps.close();
		}
		
		return dropdownId;
	}
	
	/**
	 * Update the sort order of the process in the sort_order table
	 * @param connection
	 * @param process
	 * @throws SQLException
	 */
	private static void updateSortOrder(Connection connection, TrnProcess process) throws SQLException{
		String sql = "update trn_dropdown set sort_order = ? where src_sys_id = ? and id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, process.getOrder());
		ps.setInt(2, process.getSrcBA().getSystemId());
		ps.setInt(3, process.getProcessId());
		
		ps.execute();
		ps.close();
	}
	
	/**
	 * Edit the Transmittal process and insert the updated values in it
	 * @param process
	 * @return
	 * @throws TbitsExceptionClient
	 */
	private static TrnProcess editTransmittalProcess(TrnProcess process) throws TbitsExceptionClient{
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			int dropdownId = getOrAddDropdownId(connection, process);
			updateSortOrder(connection, process);
			
			String sql = "UPDATE trn_processes " + 
					 "SET src_sys_id = ? " + 
					 ",description = ? " + 
					 ",trn_max_sn_key = ? " + 
					 ",dtn_sys_id = ? " + 
					 ",dtr_sys_id = ? " + 
					 ",trn_dropdown_id = ? " +
					 "where trn_process_id = ?"; 
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, process.getSrcBA().getSystemId());
			ps.setString(2, process.getDescription());
			ps.setString(3, process.getSerialKey());
			ps.setInt(4, process.getDTNBA().getSystemId());
			ps.setInt(5, process.getDTRBA().getSystemId());
			ps.setInt(6, dropdownId);
			ps.setInt(7, process.getProcessId());
			ps.execute();
			ps.close();
			
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
		process.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		return process;
	}
}
