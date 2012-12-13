/**
 * 
 */
package transmittal.com.tbitsGlobal.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class ApprovalCycleUtils {
	
	/*
	 * - List out all the documents to be transmitted.
	 * - Generate pdf of the DTN to be approved.
	 * - Log request in approval BA.
	 */
	
	private static final String TRN_APPROVAL_CYCLE_TRANSIENT_DATA = "trn_approval_cycle_transient_data";

	public static void insertTransientDataToDB(int systemId, int requestId,
			HashMap<String, String> transmittalParameters) throws DatabaseException, TBitsException{
		
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			insertTransientDataToDB(connection, systemId, requestId, transmittalParameters);			
		} catch(SQLException sqle){
			throw new DatabaseException(sqle.getMessage(), sqle);
		} finally {
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new DatabaseException("", e);
				}
			}
		}		
	}

	public static void insertTransientDataToDB(Connection connection, int systemId, int requestId,
			HashMap<String, String> transmittalParameters) throws TBitsException, DatabaseException {
		
		PreparedStatement ps = null;
		if ((transmittalParameters != null) && (!transmittalParameters.isEmpty())){
			try{
				ps = connection.prepareStatement("INSERT INTO " + TRN_APPROVAL_CYCLE_TRANSIENT_DATA + " VALUES (?, ?, ?, ?)");
				for (String key : transmittalParameters.keySet())
				{
					String value = transmittalParameters.get(key);
					value = ((value == null) || (value.trim().equals(""))) 
							? "" 
							: value.trim();
					ps.setInt(1, systemId);
					ps.setInt(2, requestId);
					ps.setString(3, key);
					ps.setString(4, value);
					ps.addBatch();
				}		
				ps.executeBatch();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new DatabaseException("Database errored occurred while storing transient data.", sqle);
			} finally{
				if (ps != null)
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
						throw new DatabaseException("Database errored occurred while storing transient data.", e);
					}
			}
		}
		else
			throw new TBitsException("No transient data found for approval cycle. Invalid state.");
	}	
	
	public static void main(String[] args) throws DatabaseException, TBitsException {
		HashMap<String, String> testMap = new HashMap<String, String>();
//		testMap.put("a", "test1");
//		testMap.put("b", "value2");
//		insertTransientDataToDB (1, 2, testMap);
		System.out.println("Boolean: " + Boolean.valueOf(null));
		System.out.println("Done !!!!");
	}

	protected static void saveAttachementsStateToDB(Connection connection, int systemId, int requestId, boolean isApproval,
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments) throws DatabaseException{
	
		if (allAttachments != null){
			try {
				PreparedStatement ps = connection.prepareStatement("INSERT INTO "
						+ GenericTransmittalCreator.TRN_APPROVAL_CYCLE_TRANSIENT_DATA_ATTACHMENTS
						+ " VALUES (?, ?, ?, ?,?)");			
				for(Integer srcRequestId : allAttachments.keySet()){
					HashMap<String, Collection<AttachmentInfo>> allDelMap = allAttachments.get(srcRequestId);
					if (allDelMap != null)
						for(String fieldName : allDelMap.keySet()){
							if (fieldName != null){
								Collection<AttachmentInfo> tCollection = allDelMap.get(fieldName);
								if (tCollection != null){
									ps.setInt(1, systemId);
									ps.setInt(2, requestId);
									ps.setInt(3, srcRequestId);
									ps.setString(4, fieldName);
									ps.setString(5, AttachmentInfo.toJson(tCollection));
									ps.addBatch();
								}
							}
						}
					ps.executeBatch();
				}
				ps.close();
			} catch(SQLException sqle){
				throw new DatabaseException("Database error occurred while saving transient data(attachments) in the db.", sqle);
			}
		}
	}

}
