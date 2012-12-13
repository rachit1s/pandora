package transmittal.com.tbitsGlobal.server.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.exception.TBitsException;
import transmittal.com.tbitsGlobal.server.cacheObjects.SrcTargetFieldMapObject;


import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * This is called when the lookup in srcTargetFieldMapCache fails. This implements the 
 * db lookup to fetch the source target field map corresponding to the given values
 * @author devashish
 *
 */
public class SrcTargetFieldMapReader implements CacheEntryFactory {

	public static final String TRN_SRC_TARGET_FIELD_MAPPING_TABLE = "trn_src_target_field_mapping";
	
	public Object createEntry(Object key) throws Exception {
		Hashtable<String, String> fieldMap = new Hashtable<String, String>();
		
		try {
			Connection connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
//			Integer trnProcessId = ((HashMap<String, Integer>) key).get(SrcTargetFieldMapObject.TRN_PROCESS_ID);
//			Integer targetSysId = ((HashMap<String, Integer>) key).get(SrcTargetFieldMapObject.TARGET_SYS_ID);
//			Integer dcrSysId = ((HashMap<String, Integer>) key).get(SrcTargetFieldMapObject.SRC_SYS_ID);
			
			Integer trnProcessId = ((SrcTargetFieldMapObject) key).getTrnProcessId();
			Integer targetSysId  = ((SrcTargetFieldMapObject) key).getTargetSysId();
			Integer dcrSysId = ((SrcTargetFieldMapObject) key).getSrcSysId();
			
//			Integer trnProcessId = Integer.valueOf(((TestObj) key).getKey1());
//			Integer targetSysId = Integer.valueOf(((TestObj) key).getKey2());
//			Integer dcrSysId = Integer.valueOf(((TestObj) key).getKey3());
			
			PreparedStatement ps = connection.prepareStatement("SELECT src_field_id, target_field_id FROM "
					+ TRN_SRC_TARGET_FIELD_MAPPING_TABLE
					+ " WHERE  trn_process_id=? and target_sys_id=?");
			ps.setInt(1, trnProcessId);
			ps.setInt(2, targetSysId);
			ResultSet rs = ps.executeQuery();
			if(rs != null){
				while (rs.next()){
					int dcrFieldId = rs.getInt("src_field_id");
					int targetFieldId = rs.getInt("target_field_id");
					Field dcrField = Field.lookupBySystemIdAndFieldId( dcrSysId, dcrFieldId);
					Field targetField = Field.lookupBySystemIdAndFieldId(targetSysId, targetFieldId);
					if (dcrField == null){
						System.out.println("Skipping copying the value of field with DCR field Id: " + dcrFieldId
								+ ", for business area with id: " + dcrSysId);
					}else if (targetField == null){
						System.out.println("Skipping copying the value of field with Target BA field Id: " + targetFieldId 
								+ ", for business area with id: " + targetSysId);
					}else if ((dcrField != null) && (targetField != null)){
						if (dcrField.getDataTypeId() != DataType.ATTACHMENTS)
							fieldMap.put(dcrField.getName(), targetField.getName());
					}
				}
			}
			
			if((connection != null) && !connection.isClosed()){
				connection.close();  
			}
			
			return fieldMap;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Database error occurred while retrieving mapped fields.", sqle);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TBitsException("SrcTargetFieldMapReader : ", e);
		}
		
	}
}
