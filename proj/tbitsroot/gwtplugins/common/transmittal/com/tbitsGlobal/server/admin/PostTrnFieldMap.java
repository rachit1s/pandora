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
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;
import transmittal.com.tbitsGlobal.client.models.TrnPostProcessValue;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.server.TransmittalUtils;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

public class PostTrnFieldMap {
	
	public static List<TrnPostProcessValue> savePostProcessFieldValues(TrnProcess process, List<TrnPostProcessValue> values) throws TbitsExceptionClient{
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
	
	private static TrnPostProcessValue savePostProcessParam(Connection connection, TrnProcess process, TrnPostProcessValue value) throws TbitsExceptionClient, SQLException{
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
		if (null == value.getTargetFieldValue()) {
			ps.setString(5, "");
		} else {
			ps.setString(5,  value.getTargetFieldValue());
		}
		ps.execute();
		ps.close();
		
		value.setSrcBA(process.getSrcBA());
		value.setProcessId(process.getProcessId());
		value.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		
		return value;
	}
	
	public static List<TrnPostProcessValue> getPostProcessFieldValues(TrnProcess process, HttpServletRequest request) throws TbitsExceptionClient{
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
					if((null == TransmittalUtils.getBAforSysId(srcSysId)) || (null == TransmittalUtils.getBAforSysId(targetSysId)))
						continue;
					postProcessValue.setSrcBA(TransmittalUtils.getBAforSysId(srcSysId));
					postProcessValue.setTargetBA(TransmittalUtils.getBAforSysId(targetSysId));
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
}
