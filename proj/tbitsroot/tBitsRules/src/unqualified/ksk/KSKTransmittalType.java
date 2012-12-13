/**
 * 
 */
package ksk;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

/**
 * @author lokesh
 *
 */
public class KSKTransmittalType {
	
	private static final String TABLE_TRANSMITTAL_TYPES = "transmittal_types";
	private static final String TABLE_TRANSMITTAL_PROCESS_PARAMETERS = "transmittal_process_parameters";
	
	private static final String COLUMN_VALUE = "value";
	private static final String COLUMN_ENTRY = "entry";
	private static final String COLUMN_SORT_ORDER = "sort_order";
	private static final String COLUMN_TARGET_SYS_ID = "target_sys_id";
	private static final String COLUMN_DTR_SYS_ID = "dtr_sys_id";
	private static final String COLUMN_DTN_SYS_ID = "dtn_sys_id";	
	private static final String COLUMN_DISPLAY_NAME = "display_name";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_TRANSMITTAL_TYPE_ID = "transmittal_type_id";
	private static final String COLUMN_SYS_ID = "sys_id";	
	public static final String COLUMN_MAX_TRANSMITTAL_ID = "max_transmittal_id";
	private static final String COLUMN_TABLE_TRANSMITTAL_PROCESS = "transmittal_process";
		
	public static final String STP_TRANSMITTAL_GET_MAX_TRANSMITTAL_ID = "stp_transmittal_getMaxTransmittalId";
	
	int mySystemId, myTransmittalTypeId, myDtnSysId, myDtrSysId, myTargetSysId, mySortOrder;  
	String myName, myDisplayName, myTransmittalProcess;
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
		
	public KSKTransmittalType(int aSystemId, int aTransmittalTypeId, String name, 
								String displayName, String aTransmittalProcess){
		this.mySystemId = aSystemId;
		this.myTransmittalTypeId = aTransmittalTypeId;
		this.myName = name;
		this.myDisplayName = displayName;
		this.myTransmittalProcess = aTransmittalProcess;
	}
	
	public KSKTransmittalType(int aSystemId, int aTransmittalTypeId, String name, 
			String displayName, String aTransmittalProcess, int dtnSysId,
			int dtrSysId, int targetSysId, int sortOrder){
			this.mySystemId = aSystemId;
			this.myTransmittalTypeId = aTransmittalTypeId;
			this.myName = name;
			this.myDisplayName = displayName;
			this.myTransmittalProcess = aTransmittalProcess;
			this.myDtnSysId = dtnSysId;
			this.myDtrSysId = dtrSysId;
			this.myTargetSysId = targetSysId;
			this.mySortOrder = sortOrder;
	}
	
	public int getSystemId(){
		return mySystemId;
	}
	
	public int getTransmittalTypeId(){
		return myTransmittalTypeId;
	}
	
	public String getName(){
		return myName;
	}
	
	public String getDisplayName(){
		return myDisplayName;
	}
	
	public String getTransmittalProcess(){
		return myTransmittalProcess;
	}
	
	public int getDtnSysId(){
		return this.myDtnSysId;
	}
	
	public int getDtrSysId(){
		return this.myDtrSysId;
	}
	
	public int getTargetSysId(){
		return this.myTargetSysId;
	}
	
	public int getSortOrder(){
		return this.mySortOrder;
	}
	
	public static ArrayList<KSKTransmittalType> lookupTransmittalTypesBySystemId(int aSystemId) throws SQLException{
		Connection connection = null;
		ArrayList<KSKTransmittalType> ttList = new ArrayList<KSKTransmittalType>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + TABLE_TRANSMITTAL_TYPES + " WHERE " + COLUMN_SYS_ID + "=?");
			ps.setInt(1, aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while(rs.next())
					ttList.add(new KSKTransmittalType(rs.getInt(COLUMN_SYS_ID), rs.getInt(COLUMN_TRANSMITTAL_TYPE_ID), rs.getString(COLUMN_NAME), 
							rs.getString(COLUMN_DISPLAY_NAME), rs.getString(COLUMN_TABLE_TRANSMITTAL_PROCESS), rs.getInt(COLUMN_DTN_SYS_ID), 
							rs.getInt(COLUMN_DTR_SYS_ID), rs.getInt(COLUMN_TARGET_SYS_ID), rs.getInt(COLUMN_SORT_ORDER)));					
			rs.close();
			ps.close();

		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		return ttList;
	}	
	
	public static KSKTransmittalType lookupTransmittalTypeBySystemIdAndName(int aSystemId, String aName) throws SQLException{
		Connection connection = null;
		KSKTransmittalType kskTT = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + TABLE_TRANSMITTAL_TYPES
					+ " WHERE " + COLUMN_SYS_ID + "=? AND " + COLUMN_NAME + "=?");
			ps.setInt(1, aSystemId);
			ps.setString(2, aName);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new KSKTransmittalType(rs.getInt(COLUMN_SYS_ID), rs.getInt(COLUMN_TRANSMITTAL_TYPE_ID), rs.getString(COLUMN_NAME), 
													rs.getString(COLUMN_DISPLAY_NAME), rs.getString(COLUMN_TABLE_TRANSMITTAL_PROCESS),
													rs.getInt(COLUMN_DTN_SYS_ID), rs.getInt(COLUMN_DTR_SYS_ID), rs.getInt(COLUMN_TARGET_SYS_ID),
													rs.getInt(COLUMN_SORT_ORDER));
			
			rs.close();
			ps.close();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		return kskTT;
	}
	
	public static KSKTransmittalType lookupTransmittalTypeBySystemIdAndName(Connection connection, 
			int aSystemId, String aName) throws SQLException{
		
		KSKTransmittalType kskTT = null;
		try{
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + TABLE_TRANSMITTAL_TYPES
					+ " WHERE " + COLUMN_SYS_ID + "=? AND " + COLUMN_NAME + "=?");
			ps.setInt(1, aSystemId);
			ps.setString(2, aName);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new KSKTransmittalType(rs.getInt(COLUMN_SYS_ID), rs.getInt(COLUMN_TRANSMITTAL_TYPE_ID), rs.getString(COLUMN_NAME), 
						rs.getString(COLUMN_DISPLAY_NAME), rs.getString(COLUMN_TABLE_TRANSMITTAL_PROCESS), rs.getInt(COLUMN_DTN_SYS_ID), 
						rs.getInt(COLUMN_DTR_SYS_ID), rs.getInt(COLUMN_TARGET_SYS_ID), rs.getInt(COLUMN_SORT_ORDER));

			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;			
		} 	
		return kskTT;
	}
	
	public static KSKTransmittalType lookupTransmittalTypeBySystemIdAndTransmittalTypeId(int aSystemId,
																							int aTransmittalTypeId) throws SQLException{
		Connection connection = null;
		KSKTransmittalType kskTT = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + TABLE_TRANSMITTAL_TYPES
					+ " WHERE " + COLUMN_SYS_ID + "=? AND " + COLUMN_TRANSMITTAL_TYPE_ID + "=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new KSKTransmittalType(rs.getInt(COLUMN_SYS_ID), rs.getInt(COLUMN_TRANSMITTAL_TYPE_ID), rs.getString(COLUMN_NAME), 
																rs.getString(COLUMN_DISPLAY_NAME), rs.getString(COLUMN_TABLE_TRANSMITTAL_PROCESS),
																rs.getInt(COLUMN_DTN_SYS_ID), rs.getInt(COLUMN_DTR_SYS_ID), rs.getInt(COLUMN_TARGET_SYS_ID),
																rs.getInt(COLUMN_SORT_ORDER));
			rs.close();
			ps.close();
			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}
	
			connection = null;
		}
		return kskTT;
	}

	public static KSKTransmittalType lookupTransmittalTypeBySystemIdAndTransmittalTypeId(Connection connection, 
																							int aSystemId, int aTransmittalTypeId) throws SQLException{
		KSKTransmittalType kskTT = null;
		try{
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + TABLE_TRANSMITTAL_TYPES
					+ " WHERE " + COLUMN_SYS_ID + "=? AND " + COLUMN_TRANSMITTAL_TYPE_ID + "=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new KSKTransmittalType(rs.getInt(COLUMN_SYS_ID), rs.getInt(COLUMN_TRANSMITTAL_TYPE_ID), rs.getString(COLUMN_NAME), 
																rs.getString(COLUMN_DISPLAY_NAME), rs.getString(COLUMN_TABLE_TRANSMITTAL_PROCESS),
																rs.getInt(COLUMN_DTN_SYS_ID), rs.getInt(COLUMN_DTR_SYS_ID), rs.getInt(COLUMN_TARGET_SYS_ID),
																rs.getInt(COLUMN_SORT_ORDER));
			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			throw sqle;			
		} 	
		return kskTT;
	}
	
	public static Hashtable<String, String> getTransmittalProcessParameters(int aSystemId, int aTransmittalTypeId) throws SQLException{
		Hashtable<String, String> ttProcessParams = new Hashtable<String, String>();
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + TABLE_TRANSMITTAL_PROCESS_PARAMETERS
					+ " WHERE " + COLUMN_SYS_ID + "=? AND " + COLUMN_TRANSMITTAL_TYPE_ID + "=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					String key = rs.getString(COLUMN_ENTRY);
					String value = rs.getString(COLUMN_VALUE);
					ttProcessParams.put(key, value);					
				}
			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}
	
			connection = null;
		}
		return ttProcessParams;
	}
	
	public static Hashtable<String, String> getTransmittalProcessParameters(
			Connection connection, int aSystemId, int aTransmittalTypeId) throws SQLException{
		Hashtable<String, String> ttProcessParams = new Hashtable<String, String>();
		try{
			//connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + TABLE_TRANSMITTAL_PROCESS_PARAMETERS
					+ " WHERE " + COLUMN_SYS_ID + "=? AND " + COLUMN_TRANSMITTAL_TYPE_ID + "=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					String key = rs.getString(COLUMN_ENTRY);
					String value = rs.getString(COLUMN_VALUE);
					ttProcessParams.put(key, value);					
				}
			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;
		} 
		return ttProcessParams;
	}
	
	/**
	 * Gets the maximum transmittal number for a particular business area.
	 * @param aSystemId
	 * @param aTransmittalProcessName
	 * @return maximum transmittal number
	 * @throws DatabaseException
	 */
	public static int getMaxTransmittalNumber(int aSystemId, String aTransmittalProcessName) throws DatabaseException{
		int maxTransmittalNumber = -1;		
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			maxTransmittalNumber = getMaxTransmittalNumber(connection, aSystemId, aTransmittalProcessName);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving max transmittal number for sysId: " + aSystemId, e);
		}finally{
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				LOG.warn("Error occurred while retrieving max transmittal number for sysId: " + aSystemId);
			}
		}
		return maxTransmittalNumber;
	}
	
	public static int getMaxTransmittalNumber(Connection connection, int aSystemId, String transmittalProcessName) throws DatabaseException{
		int maxTransmittalNumber = -1;
		try{
			CallableStatement cs = connection.prepareCall(STP_TRANSMITTAL_GET_MAX_TRANSMITTAL_ID + " ?");//"stp_getAndIncrMaxId ?");
			cs.setString(1, transmittalProcessName);
			ResultSet rs = cs.executeQuery();
			if ((rs != null) && (rs.next())){
				maxTransmittalNumber = rs.getInt(COLUMN_MAX_TRANSMITTAL_ID);
				return maxTransmittalNumber;
			}else {
				throw new SQLException("Error occurred while retrieving max transmittal number for sysId: " + aSystemId
						+ ".\n" + "Could not retrieve max transmittal id with name: " + transmittalProcessName);
			}
		}catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving max transmittal number for sysId: " + aSystemId, e);
		}
	}
	
		
	/**
	 * Gets the maximum transmittal number for a particular business area.
	 * @param aSystemId
	 * @return maximum transmittal number
	 * @throws DatabaseException
	 */
	/*public static void resetMaxTransmittalNumber(int aSystemId) throws DatabaseException{
		//int maxTransmittalNumber = -1;		
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			resetMaxTransmittalNumber(connection, aSystemId);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error occurred while resetting max transmittal number for sysId: " + aSystemId, e);
		}finally{
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				LOG.warn("Error occurred while resetting max transmittal number for sysId: " + aSystemId);
			}
		}
		//return maxTransmittalNumber;
	}
	
	public static void resetMaxTransmittalNumber(Connection connection, int aSystemId) throws SQLException{
		System.out.println("Reversing transmittal number %%%%%%%%%%");
		CallableStatement cs = connection.prepareCall("stp_transmittal_max_id_reversal ?");
		cs.setInt(1, aSystemId);
		cs.execute();
		cs.close();
		cs = null;
	}*/
	
	
	
	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		//KSKTransmittalType kskTT = KSKTransmittalType.lookupTransmittalTypeBySystemIdAndName(17, "TransmitToWPCLRFC");
		//System.out.println("TT: " + kskTT.myDisplayName);
		/* NumberFormat formatter = new DecimalFormat("00000");
		 String s = formatter.format(45);
		 System.out.println("s: " + s);*/
		
		Hashtable<String, String> pp = KSKTransmittalType.getTransmittalProcessParameters(17, 1);
		System.out.println("catList: \n" + pp.get("approvalCategories"));		
	}

}