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
import transmittal.com.tbitsGlobal.client.models.TrnAttachmentList;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

public class AttachmentListMap {
	
	public static List<TrnAttachmentList> saveAttachmentLists(TrnProcess process, List<TrnAttachmentList> params) throws TbitsExceptionClient{
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
				System.out.println(param.getName());
				resp.add(saveAttachmentList(connection, process, param));
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
	
	private static TrnAttachmentList saveAttachmentList(Connection connection, TrnProcess process, TrnAttachmentList param) throws SQLException{
		String sql = "INSERT INTO trn_attachment_selection_table_columns " +
				"(trn_process_id, name, field_id, data_type_id, default_value, is_editable, is_active, column_order, type_value_source, is_included) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		
		ps.setInt(1, process.getProcessId());
		ps.setString(2, param.getName());
		ps.setInt(3, param.getField().getFieldId());
		ps.setInt(4, param.getDataType());
		if(param.getDefaultValue() == null)
		{
			param.setDefaultValue("");
		}
		ps.setString(5, param.getDefaultValue());
		if(param.getIsEditable()==null)
			param.setIsEditable(false);
			
		ps.setBoolean(6, param.getIsEditable());
		
		if(param.getIsActive()==null)
			param.setIsActive(false);
		
		ps.setBoolean(7, param.getIsActive());
		ps.setInt(8, param.getOrder());
		ps.setInt(9, param.getTypeValueSource());
		
		if(param.getIsIncluded()==null)
			param.setIsIncluded(false);
		ps.setBoolean(10, param.getIsIncluded());
		
		ps.execute();
		ps.close();
		
		param.setProcessId(process.getProcessId());
		param.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		
		return param;
	}
	
	public static List<TrnAttachmentList> getAttachmentList(TrnProcess process, HttpServletRequest request) throws TbitsExceptionClient{
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
}
