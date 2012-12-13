package transmittal.com.tbitsGlobal.server.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;
import transmittal.com.tbitsGlobal.client.models.TrnDrawingNumber;
import transmittal.com.tbitsGlobal.server.TransmittalUtils;

/**
 * Utility Class for manipulating entries in trn_drawing_number_field table
 * @author devashish
 *
 */
public class DrawingNumberField {
	
	/**
	 * Save Drawing Number fields to database
	 * @param list of unsaved fields
	 * @return list of saved fields
	 * @throws TbitsExceptionClient 
	 */
	public static List<TrnDrawingNumber> saveDrawingNumberFields(List<TrnDrawingNumber> list) throws TbitsExceptionClient{
		List<TrnDrawingNumber> savedList = new ArrayList<TrnDrawingNumber>();
		Connection connection = null;
		
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "DELETE from trn_drawing_number_field";
			PreparedStatement ps = connection.prepareStatement(sql);
			
			ps.execute();
			ps.close();
			
			for(TrnDrawingNumber entry : list){
				savedList.add(insertIntoDrawingNumberField(connection, entry));
			}
			connection.commit();
			
		}catch (SQLException e){
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
		}catch (Exception e){
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}finally{
			try {
				if((connection != null) && (!connection.isClosed())){
					connection.close();
				}
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		return savedList;
	}
	
	/**
	 * Insert the entry int drawing number field table
	 * @param connection
	 * @param entry
	 * @return updated entry
	 * @throws SQLException
	 */
	private static TrnDrawingNumber insertIntoDrawingNumberField(Connection connection, TrnDrawingNumber entry) throws SQLException{
		String sql = "insert into trn_drawing_number_field " +
					" (sys_id, field_name) " +
					" VALUES(?,?) ";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, entry.getSrcBa().getSystemId());
		ps.setString(2, entry.getField().getName());
		
		ps.execute();
		ps.close();
		
		entry.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		return entry;
	}
	
	/**
	 * Get all the entries in trn_drawing_number_field table
	 * @param request
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static List<TrnDrawingNumber> getDrawingNumberFields(HttpServletRequest request) throws TbitsExceptionClient{
		List<TrnDrawingNumber> drawingNumberFields = new ArrayList<TrnDrawingNumber>();
		
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
		
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "select * from trn_drawing_number_field";
			PreparedStatement ps = connection.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			
			if(null != rs){
				while(rs.next()){
					TrnDrawingNumber drawingNumberFieldEntry = new TrnDrawingNumber();
					
					int srcSysId 		= rs.getInt("sys_id");
					String fieldName	= rs.getString("field_name");
					
					if(null == TransmittalUtils.getBAforSysId(srcSysId))
						continue;
					BusinessAreaClient ba = TransmittalUtils.getBAforSysId(srcSysId);
					drawingNumberFieldEntry.setSrcBa(ba);
					
					
					try {
						Field field = Field.lookupBySystemIdAndFieldName(srcSysId, fieldName);
						BAField baField = GWTServiceHelper.fromField(field, user, null, BAField.class);
						
						drawingNumberFieldEntry.setField(baField);
						
						drawingNumberFields.add(drawingNumberFieldEntry);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
				}
			}
		}catch (SQLException e){
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
		return drawingNumberFields;
	}
}
