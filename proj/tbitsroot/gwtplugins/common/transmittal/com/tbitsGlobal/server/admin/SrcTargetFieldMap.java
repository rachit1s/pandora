package transmittal.com.tbitsGlobal.server.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;
import transmittal.com.tbitsGlobal.client.models.TrnFieldMapping;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.server.TransmittalUtils;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

public class SrcTargetFieldMap {
	
	
	public static List<TrnFieldMapping> getSrcTargetFieldMap(TrnProcess process, HttpServletRequest request) throws TbitsExceptionClient{
		User user;
		try {
			user = WebUtil.validateUser(request);
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
					
					if(null == TransmittalUtils.getBAforSysId(srcSysId))
						continue;
					value.setSrcBA(TransmittalUtils.getBAforSysId(srcSysId));
					
					try {
						Field field = Field.lookupBySystemIdAndFieldId(srcSysId, srcFieldId);
						BAField baField = GWTServiceHelper.fromField(field, user, null, BAField.class);
						value.setSrcField(baField);
					}catch (DatabaseException e) {
						e.printStackTrace();
					}catch (TbitsExceptionClient e){
						e.printStackTrace();
					}
					
					if(null == TransmittalUtils.getBAforSysId(targetSysId))
						continue;
					value.setTargetBA(TransmittalUtils.getBAforSysId(targetSysId));
					
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
	
	public static List<TrnFieldMapping> saveSrcTargetFieldMap(TrnProcess process, List<TrnFieldMapping> mappings) throws TbitsExceptionClient{
		List<TrnFieldMapping> resp = new ArrayList<TrnFieldMapping>();
		
		Connection connection = null;
		
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			String sql = "DELETE FROM trn_src_target_field_mapping where trn_process_id = ?";
			
			PreparedStatement cs = connection.prepareStatement(sql);
			cs.setInt(1, process.getProcessId());
			
			cs.execute();
			cs.close();
			
			for(TrnFieldMapping mapping : mappings){
				if((null == mapping.getSrcBA()) || (null == mapping.getSrcField()) || (null == mapping.getTargetBA()) || (null == mapping.getTargetField()))
					continue;
				resp.add(saveMapValue(connection, process, mapping));
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
	
	private static TrnFieldMapping saveMapValue(Connection connection, TrnProcess process, TrnFieldMapping mapping) throws TbitsExceptionClient, SQLException{
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
}
