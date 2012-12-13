package transmittal.com.tbitsGlobal.server.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.exception.TBitsException;
import transmittal.com.tbitsGlobal.server.cacheObjects.PostTrnFieldMapObject;


public class PostTrnFieldMapReader implements CacheEntryFactory {

	private static final String TRN_POST_TRANSMITTAL_FIELD_VALUES = "trn_post_transmittal_field_values";
	private static final String TRN_PROCESS_ID = "trn_process_id";
	private static Logger Log = Logger.getLogger("transmittal");
	
	public Object createEntry(Object key) throws Exception {
		Hashtable<String, String> paramTable = new Hashtable<String, String>();
		
		Integer dtnBAId				= ((PostTrnFieldMapObject) key).getDtnBAId();
		Integer trnProcessDtnSysId 	= ((PostTrnFieldMapObject) key).getDtnSysId();
		Integer trnProcessDtrSysId 	= ((PostTrnFieldMapObject) key).getDtrSysId();
		Integer trnProcessId	  	= ((PostTrnFieldMapObject) key).getTrnProcessId();
		try {
			Connection connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			if ((dtnBAId == trnProcessDtnSysId) || (dtnBAId == trnProcessDtrSysId)){
			
				PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + TRN_POST_TRANSMITTAL_FIELD_VALUES +
						" WHERE target_sys_id=? and " + TRN_PROCESS_ID + "=?");
				ps.setInt(1, dtnBAId);
				ps.setInt(2, trnProcessId);
				ResultSet rs = ps.executeQuery();
				if(rs != null){
					while (rs.next()){
						int targetFieldId = rs.getInt("target_field_id");
						String fieldValue = rs.getString("target_field_value");					
						Field targetField = Field.lookupBySystemIdAndFieldId(dtnBAId, targetFieldId);
						
						if (targetField != null){										
							switch (targetField.getDataTypeId()){
								case DataType.DATE:
									
								case DataType.DATETIME:
									if (fieldValue != null){
										if (fieldValue.trim().equals(""))
											paramTable.put(targetField.getName(), "");
										else{
												/**
												 * TODO: Not handling as of now. If a logic is required 
												 * will be added as per requirement. 
												 */
										}
									}
									else
										Log.log(Level.WARNING, "No value provided for field: " + targetField.getDisplayName() + ", hence ignoring it in post transmittal update.");
									break;
									
								case DataType.TEXT:
									
								case DataType.STRING:
									if (targetField.getName().equals(Field.DESCRIPTION)){
										/**
										 * TODO: Not handling as of now. If a logic is required will be added as per requirement.
										 */
									}else{
										if (fieldValue == null) 
												Log.log(Level.WARNING, "No value provided for field: " + targetField.getDisplayName() + ", hence ignoring it in post transmittal update.");
										else paramTable.put(targetField.getName(), fieldValue);
									}
									break;
													
								default:					
									paramTable.put(targetField.getName(), fieldValue);
								}
							}
							else 
								Log.log(Level.WARNING, "Skipped setting the value of field with field-id: " + targetFieldId + ", for business area with system id: " + dtnBAId);
						}			
					} 
				}
			if((connection != null) && !connection.isClosed()){
				connection.close();  
			}
			
			}catch (NumberFormatException nfe){
				nfe.printStackTrace();
				throw new TBitsException("Error occurred while parsing date-offset of a date field.", nfe);
			}catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new DatabaseException("Database error occurred while retrieving mapped fields.", sqle);
			}	
		return paramTable;
	}

}
