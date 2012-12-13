package transmittal.com.tbitsGlobal.server.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnProcessParam;
import transmittal.com.tbitsGlobal.server.TransmittalUtils;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class TrnProcessParams {
	
	
	/**
	 * Get a list of transmittal processes from database
	 * @return List of all the configured transmittal processes
	 * @throws TbitsExceptionClient 
	 */
	public static List<TrnProcess> getTransmittalProcessesForBa(BusinessAreaClient srcBa) throws TbitsExceptionClient{
		List<TrnProcess> resp = null;
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			
			String sql = "select p.*,d.name, d.sort_order  from trn_processes p join trn_dropdown d on p.trn_dropdown_id = d.id " +
					"where p.src_sys_id = ? " +
					"order by trn_process_id";
			
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, srcBa.getSystemId());
			
			ResultSet rs = ps.executeQuery();
			
			if(null != rs) {
				resp = new ArrayList<TrnProcess>();
				
				while(rs.next()) {
					int srcSysId = rs.getInt("src_sys_id");
					int trnProcessId = rs.getInt("trn_process_id");
					String name = rs.getString("name");
					String desc = rs.getString("description");
					String key = rs.getString("trn_max_sn_key");
					int dtnSysId = rs.getInt("dtn_sys_id");
					int dtrSysId = rs.getInt("dtr_sys_id");
					int sortOrder = rs.getInt("sort_order");
					
					TrnProcess process = new TrnProcess();
					
					if((null == TransmittalUtils.getBAforSysId(srcSysId)) || (null == TransmittalUtils.getBAforSysId(dtnSysId))
							|| (null == TransmittalUtils.getBAforSysId(dtrSysId)))
						continue;
					process.setSrcBA(TransmittalUtils.getBAforSysId(srcSysId));
					process.setDTNBA(TransmittalUtils.getBAforSysId(dtnSysId));
				    process.setDTRBA(TransmittalUtils.getBAforSysId(dtrSysId));
				
					process.setProcessId(trnProcessId);
					process.setName(name);
					process.setDescription(desc);
					process.setSerialKey(key);
					process.setOrder(sortOrder);
					
					resp.add(process);
				}
				rs.close();
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
                connection = null;
            }
        }
		return resp;
	}
	/**
	 * Get a list of transmittal processes from database
	 * @return List of all the configured transmittal processes
	 * @throws TbitsExceptionClient 
	 */
	public static List<TrnProcess> getTransmittalProcesses() throws TbitsExceptionClient{
		List<TrnProcess> resp = null;
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			
			String sql = "select p.*,d.name, d.sort_order  from trn_processes p join trn_dropdown d on p.trn_dropdown_id = d.id " +
					"order by trn_process_id";
			
			PreparedStatement ps = connection.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			
			if(null != rs) {
				resp = new ArrayList<TrnProcess>();
				
				while(rs.next()) {
					int srcSysId = rs.getInt("src_sys_id");
					int trnProcessId = rs.getInt("trn_process_id");
					String name = rs.getString("name");
					String desc = rs.getString("description");
					String key = rs.getString("trn_max_sn_key");
					int dtnSysId = rs.getInt("dtn_sys_id");
					int dtrSysId = rs.getInt("dtr_sys_id");
					int sortOrder = rs.getInt("sort_order");
					
					TrnProcess process = new TrnProcess();
					
					if((null == TransmittalUtils.getBAforSysId(srcSysId)) || (null == TransmittalUtils.getBAforSysId(dtnSysId))
							|| (null == TransmittalUtils.getBAforSysId(dtrSysId)))
						continue;
					process.setSrcBA(TransmittalUtils.getBAforSysId(srcSysId));
					process.setDTNBA(TransmittalUtils.getBAforSysId(dtnSysId));
				    process.setDTRBA(TransmittalUtils.getBAforSysId(dtrSysId));
				
					process.setProcessId(trnProcessId);
					process.setName(name);
					process.setDescription(desc);
					process.setSerialKey(key);
					process.setOrder(sortOrder);
					
					resp.add(process);
				}
				rs.close();
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
                connection = null;
            }
        }
		return resp;
	}
	
	
	/**
	 * Get the list of Transmittal Process Parameters from database for the specified 
	 * transmittal process
	 * @param process
	 * @return transmittal process parameters for the process
	 * @throws TbitsExceptionClient 
	 */
	public static List<TrnProcessParam> getProcessParams(TrnProcess process) throws TbitsExceptionClient{
		List<TrnProcessParam> resp = new ArrayList<TrnProcessParam>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			
			String sql = "SELECT * FROM trn_process_parameters where trn_process_id = ?";
			
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, process.getProcessId());
			
			ResultSet rs = ps.executeQuery();
			
			if(null != rs) {
				while(rs.next()) {
					int srcSysId = rs.getInt("src_sys_id");
					String name = rs.getString("parameter");
					String value = rs.getString("value");
					
					TrnProcessParam param = new TrnProcessParam();
					param.setProcessId(process.getProcessId());
					if(null == TransmittalUtils.getBAforSysId(srcSysId))
						continue;
					else param.setSrcBA(TransmittalUtils.getBAforSysId(srcSysId));
					param.setName(name);
					param.setValue(value);
					
					resp.add(param);
				}	
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient();
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
                connection = null;
            }
        }
		return resp;
	}
	
	/**
	 * Save Transmittal Process Parameters to the database
	 * @param process
	 * @param params
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static List<TrnProcessParam> saveProcessParams(TrnProcess process, List<TrnProcessParam> params) throws TbitsExceptionClient{
		List<TrnProcessParam> resp = new ArrayList<TrnProcessParam>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "DELETE FROM trn_process_parameters where trn_process_id = ?";
			
			PreparedStatement cs = connection.prepareStatement(sql);
			cs.setInt(1, process.getProcessId());
			
			cs.execute();
			cs.close();
			
			for(TrnProcessParam param : params){
				resp.add(saveProcessParam(connection, process, param));
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
	
	private static TrnProcessParam saveProcessParam(Connection connection, TrnProcess process, TrnProcessParam param) throws SQLException{
		String sql = "INSERT INTO trn_process_parameters (src_sys_id, trn_process_id, parameter, value) VALUES(?,?,?,?)";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		
		ps.setInt(1, process.getSrcBA().getSystemId());
		ps.setInt(2, process.getProcessId());
		ps.setString(3, param.getName());
		if(null == param.getValue()){
			ps.setString(4, "");
		}else{
			ps.setString(4, param.getValue());
		}
		
		ps.execute();
		ps.close();
		
		param.setSrcBA(process.getSrcBA());
		param.setProcessId(process.getProcessId());
		param.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		
		return param;
	}
}
