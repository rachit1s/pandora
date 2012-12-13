package admin.com.tbitsglobal.admin.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;
import admin.com.tbitsglobal.admin.client.AdminDBService;
import admin.com.tbitsglobal.admin.client.trn.models.TrnAttachmentList;
import admin.com.tbitsglobal.admin.client.trn.models.TrnDistList;
import admin.com.tbitsglobal.admin.client.trn.models.TrnFieldMapping;
import admin.com.tbitsglobal.admin.client.trn.models.TrnPostProcessValue;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcessParam;
import bulkupdate.com.tbitsglobal.bulkupdate.client.IBulkUpdateConstants;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;
import commons.com.tbitsGlobal.utils.server.UtilServiceImpl;

public class AdminDBServiceImpl extends UtilServiceImpl implements AdminDBService, IBulkUpdateConstants {

	private static final long serialVersionUID = 1L;
	
	public List<BusinessAreaClient> getBAs() throws TbitsExceptionClient {
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		ArrayList<BusinessArea> baList;
		try {
			baList = GWTServiceHelper.getBAList(user);
		} catch (DatabaseException e) {
			e.printStackTrace();
			return null;
		}
		
		ArrayList<BusinessAreaClient> baListClient = new ArrayList<BusinessAreaClient>();
		for(BusinessArea ba : baList){
			BusinessAreaClient baClient = new BusinessAreaClient();
			GWTServiceHelper.setValuesInDomainObject(ba, baClient);
			baListClient.add(baClient);
		}
		
		return baListClient;
	}
	
	public List<TrnProcess> saveTransmittalProcesses(List<TrnProcess> processes) throws TbitsExceptionClient{
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
	
	private TrnProcess addTransmittalProcess(TrnProcess process) throws TbitsExceptionClient{
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
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
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
	
	private TrnProcess editTransmittalProcess(TrnProcess process) throws TbitsExceptionClient{
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			int dropdownId = getOrAddDropdownId(connection, process);
			
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
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
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
	
	private int getOrAddDropdownId(Connection connection, TrnProcess process) throws SQLException{
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
	
	public List<TrnProcess> getTransmittalProcesses() throws TbitsExceptionClient{
		List<TrnProcess> resp = null;
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			
			String sql = "select p.*,d.name, d.sort_order  from trn_processes p join trn_dropdown d on p.trn_dropdown_id = d.id order by trn_process_id";
			
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
//					int dropdownId = rs.getInt("trn_dropdown_id");
					int sortOrder = rs.getInt("sort_order");
					
					TrnProcess process = new TrnProcess();
					try {
						process.setSrcBA(getBAforSysId(srcSysId));
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					try {
						process.setDTNBA(getBAforSysId(dtnSysId));
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					try {
						process.setDTRBA(getBAforSysId(dtrSysId));
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
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
                    throw new TbitsExceptionClient(sqle);
                }

                connection = null;
            }
        }
		
		return resp;
	}
	
	private BusinessAreaClient getBAforSysId(int sysId) throws DatabaseException{
		BusinessArea ba = BusinessArea.lookupBySystemId(sysId);
		BusinessAreaClient baClient = new BusinessAreaClient();
		GWTServiceHelper.setValuesInDomainObject(ba, baClient);
		
		return baClient;
	}
	
	public List<TrnPostProcessValue> savePostProcessFieldValues(TrnProcess process, List<TrnPostProcessValue> values) throws TbitsExceptionClient{
		List<TrnPostProcessValue> resp = new ArrayList<TrnPostProcessValue>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "DELETE FROM trn_post_transmittal_field_values where trn_process_id = ?";
			
			PreparedStatement cs = connection.prepareStatement(sql);
			cs.setInt(1, process.getProcessId());
			
			cs.execute();
			cs.close();
			
			for(TrnPostProcessValue value : values){
				resp.add(savePostProcessParam(connection, process, value));
			}
			connection.commit();
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
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
	
	private TrnPostProcessValue savePostProcessParam(Connection connection, TrnProcess process, TrnPostProcessValue value) throws TbitsExceptionClient, SQLException{
		String sql = "INSERT INTO trn_post_transmittal_field_values " +
				"(src_sys_id, trn_process_id, target_sys_id, target_field_id, target_field_value) " +
				"VALUES(?,?,?,?,?)";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		
		ps.setInt(1, process.getSrcBA().getSystemId());
		ps.setInt(2, process.getProcessId());
		ps.setInt(3, value.getTargetBA().getSystemId());
		ps.setInt(4, value.getTargetField().getFieldId());
		ps.setString(5, value.getTargetFieldValue());
//		ps.setInt(6, value.getTemp());
		
		ps.execute();
		ps.close();
		
		value.setSrcBA(process.getSrcBA());
		value.setProcessId(process.getProcessId());
		value.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		
		return value;
	}
	
	public List<TrnPostProcessValue> getPostProcessFieldValues(TrnProcess process) throws TbitsExceptionClient{
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		
		List<TrnPostProcessValue> resp = new ArrayList<TrnPostProcessValue>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			
			String sql = "SELECT * FROM trn_post_transmittal_field_values where trn_process_id = ?";
			
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, process.getProcessId());
			
			ResultSet rs = ps.executeQuery();
			
			if(null != rs) {
				while(rs.next()) {
					int srcSysId 	= rs.getInt("src_sys_id");
					int targetSysId = rs.getInt("target_sys_id");
					int fieldId 	= rs.getInt("target_field_id");
					String value 	= rs.getString("target_field_value");
					int temp		= rs.getInt("temp");
					
					TrnPostProcessValue postProcessValue = new TrnPostProcessValue();
					postProcessValue.setProcessId(process.getProcessId());
					try {
						postProcessValue.setSrcBA(getBAforSysId(srcSysId));
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					try {
						postProcessValue.setTargetBA(getBAforSysId(targetSysId));
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					try {
						Field field = Field.lookupBySystemIdAndFieldId(targetSysId, fieldId);
						BAField baField = GWTServiceHelper.fromField(field, user, null, BAField.class);
						postProcessValue.setTargetField(baField);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}catch (TbitsExceptionClient e){
						e.printStackTrace();
					}
					postProcessValue.setTargetFieldValue(value);
					postProcessValue.setTemp(temp);
					resp.add(postProcessValue);
				}	
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
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
	
	public List<TrnFieldMapping> saveMapValues(TrnProcess process, List<TrnFieldMapping> mappings) throws TbitsExceptionClient{
		List<TrnFieldMapping> resp = new ArrayList<TrnFieldMapping>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			String sql = "DELETE FROM trn_src_target_field_mapping where trn_process_id = ?";
			
			PreparedStatement cs = connection.prepareStatement(sql);
			cs.setInt(1, process.getProcessId());
			
			cs.execute();
			
			for(TrnFieldMapping mapping : mappings){
				resp.add(saveMapValue(connection, process, mapping));
			}
			connection.commit();
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
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
	
	private TrnFieldMapping saveMapValue(Connection connection, TrnProcess process, TrnFieldMapping mapping) throws TbitsExceptionClient, SQLException{
		String sql = "INSERT INTO trn_src_target_field_mapping " +
				"(trn_process_id, src_sys_id, src_field_id, target_sys_id, target_field_id) " +
				"VALUES(?,?,?,?,?)";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		
		ps.setInt(1, process.getProcessId());
		ps.setInt(2, mapping.getSrcBA().getSystemId());
		ps.setInt(3, mapping.getSrcField().getFieldId());
		ps.setInt(4, mapping.getTargetBA().getSystemId());
		ps.setInt(5, mapping.getTargetField().getFieldId());
		
		ps.execute();
		ps.close();
		
		mapping.setProcessId(process.getProcessId());
		mapping.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		
		return mapping;
	}
	
	public List<TrnFieldMapping> getMapValues(TrnProcess process) throws TbitsExceptionClient{
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		
		List<TrnFieldMapping> resp = new ArrayList<TrnFieldMapping>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			
			String sql = "SELECT * FROM trn_src_target_field_mapping where trn_process_id = ?";
			
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, process.getProcessId());
			
			ResultSet rs = ps.executeQuery();
			
			if(null != rs) {
				while(rs.next()) {
					int srcSysId 		= rs.getInt("src_sys_id");
					int srcFieldId 		= rs.getInt("src_field_id");
					int targetSysId 	= rs.getInt("target_sys_id");
					int targetFieldId 	= rs.getInt("target_field_id");
					
					TrnFieldMapping value = new TrnFieldMapping();
					value.setProcessId(process.getProcessId());
					try {
						value.setSrcBA(getBAforSysId(srcSysId));
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					try {
						Field field = Field.lookupBySystemIdAndFieldId(srcSysId, srcFieldId);
						BAField baField = GWTServiceHelper.fromField(field, user, null, BAField.class);
						value.setSrcField(baField);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}catch (TbitsExceptionClient e){
						e.printStackTrace();
					}
					try {
						value.setTargetBA(getBAforSysId(targetSysId));
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					try {
						Field field = Field.lookupBySystemIdAndFieldId(targetSysId, targetFieldId);
						BAField baField = GWTServiceHelper.fromField(field, user, null, BAField.class);
						value.setTargetField(baField);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}catch (TbitsExceptionClient e){
						e.printStackTrace();
					}
					
					resp.add(value);
				}	
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
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
	
	public List<TrnProcessParam> saveProcessParams(TrnProcess process, List<TrnProcessParam> params) throws TbitsExceptionClient{
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
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
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
	
	private TrnProcessParam saveProcessParam(Connection connection, TrnProcess process, TrnProcessParam param) throws SQLException{
		String sql = "INSERT INTO trn_process_parameters (src_sys_id, trn_process_id, parameter, value) VALUES(?,?,?,?)";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		
		ps.setInt(1, process.getSrcBA().getSystemId());
		ps.setInt(2, process.getProcessId());
		ps.setString(3, param.getName());
		ps.setString(4, param.getValue());
		
		ps.execute();
		ps.close();
		
		param.setSrcBA(process.getSrcBA());
		param.setProcessId(process.getProcessId());
		param.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		
		return param;
	}
	
	public List<TrnProcessParam> getProcessParams(TrnProcess process) throws TbitsExceptionClient{
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
					try {
						param.setSrcBA(getBAforSysId(srcSysId));
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					param.setName(name);
					param.setValue(value);
					
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
	
	public List<TrnDistList> saveDistLists(TrnProcess process, List<TrnDistList> params) throws TbitsExceptionClient{
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
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
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
	
	private TrnDistList saveDistList(Connection connection, TrnProcess process, TrnDistList param) throws SQLException{
		String sql = "INSERT INTO trn_distribution_table_column_config " +
				"(trn_process_id, name, display_name, data_type_id, field_config, is_editable, is_active, column_order) " +
				"VALUES(?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		
		ps.setInt(1, process.getProcessId());
		ps.setString(2, param.getName());
		ps.setString(3, param.getDisplayName());
		ps.setInt(4, param.getDataType());
		ps.setString(5, param.getFieldConfig());
		ps.setBoolean(6, param.getIsEditable());
		ps.setBoolean(7, param.getIsActive());
		ps.setInt(8, param.getOrder());
		
		ps.execute();
		ps.close();
		
		param.setProcessId(process.getProcessId());
		param.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		
		return param;
	}
	
	public List<TrnDistList> getDistList(TrnProcess process) throws TbitsExceptionClient{
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
	
	public List<TrnAttachmentList> saveAttachmentLists(TrnProcess process, List<TrnAttachmentList> params) throws TbitsExceptionClient{
		List<TrnAttachmentList> resp = new ArrayList<TrnAttachmentList>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "DELETE FROM trn_attachment_selection_table_columns where trn_process_id = ?";
			
			PreparedStatement cs = connection.prepareStatement(sql);
			cs.setInt(1, process.getProcessId());
			
			cs.execute();
			cs.close();
			
			for(TrnAttachmentList param : params){
				resp.add(saveAttachmentList(connection, process, param));
			}
			connection.commit();
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
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
	
	private TrnAttachmentList saveAttachmentList(Connection connection, TrnProcess process, TrnAttachmentList param) throws SQLException{
		String sql = "INSERT INTO trn_attachment_selection_table_columns " +
				"(trn_process_id, name, field_id, data_type_id, default_value, is_editable, is_active, column_order, type_value_source, is_included) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		
		ps.setInt(1, process.getProcessId());
		ps.setString(2, param.getName());
		ps.setInt(3, param.getField().getFieldId());
		ps.setInt(4, param.getDataType());
		ps.setString(5, param.getDefaultValue());
		ps.setBoolean(6, param.getIsEditable());
		ps.setBoolean(7, param.getIsActive());
		ps.setInt(8, param.getOrder());
		ps.setInt(9, param.getTypeValueSource());
		ps.setBoolean(10, param.getIsIncluded());
		
		ps.execute();
		ps.close();
		
		param.setProcessId(process.getProcessId());
		param.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		
		return param;
	}
	
	public List<TrnAttachmentList> getAttachmentList(TrnProcess process) throws TbitsExceptionClient{
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		
		List<TrnAttachmentList> resp = new ArrayList<TrnAttachmentList>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			
			String sql = "SELECT * FROM trn_attachment_selection_table_columns where trn_process_id = ?";
			
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, process.getProcessId());
			
			ResultSet rs = ps.executeQuery();
			
			if(null != rs) {
				while(rs.next()) {
					String name = rs.getString("name");
					int fieldId = rs.getInt("field_id");
					int dataTypeId = rs.getInt("data_type_id");
					String defaultValue = rs.getString("default_value");
					boolean isEditable = rs.getBoolean("is_editable");
					boolean isActive = rs.getBoolean("is_active");
					int order = rs.getInt("column_order");
					int typeValueSource = rs.getInt("type_value_source");
					boolean isIncluded = rs.getBoolean("is_included");
					
					TrnAttachmentList param = new TrnAttachmentList();
					param.setProcessId(process.getProcessId());
					param.setName(name);
					try {
						Field field = Field.lookupBySystemIdAndFieldId(process.getSrcBA().getSystemId(), fieldId);
						BAField baField = GWTServiceHelper.fromField(field, user, null, BAField.class);
						param.setField(baField);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}catch (TbitsExceptionClient e){
						e.printStackTrace();
					}
					param.setDataType(dataTypeId);
					param.setDefaultValue(defaultValue);
					param.setIsEditable(isEditable);
					param.setIsActive(isActive);
					param.setOrder(order);
					param.setTypeValueSource(typeValueSource);
					param.setIsIncluded(isIncluded);
					
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


	public BusinessAreaClient getDummyBA(BusinessAreaClient ba){
		return new BusinessAreaClient();
	}
	
}
