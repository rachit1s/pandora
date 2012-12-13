/**
 * 
 */
package ipl;

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
public class IPLTransmittalType {
	int mySystemId, myTransmittalTypeId, myDtnSysId, myDtrSysId, myTargetSysId, mySortOrder; 
	String myName, myDisplayName, myTransmittalProcess;
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
		
	public IPLTransmittalType(int aSystemId, int aTransmittalTypeId, String name, 
								String displayName, String aTransmittalProcess){
		this.mySystemId = aSystemId;
		this.myTransmittalTypeId = aTransmittalTypeId;
		this.myName = name;
		this.myDisplayName = displayName;
		this.myTransmittalProcess = aTransmittalProcess;
	}
	
	public IPLTransmittalType(int aSystemId, int aTransmittalTypeId, String name, 
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
		return this.mySystemId;
	}
	
	public int getTransmittalTypeId(){
		return this.myTransmittalTypeId;
	}
	
	public String getName(){
		return this.myName;
	}
	
	public String getDisplayName(){
		return this.myDisplayName;
	}
	
	public String getTransmittalProcess(){
		return this.myTransmittalProcess;
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
	
	public static ArrayList<IPLTransmittalType> lookupTransmittalTypesBySystemId(int aSystemId) throws SQLException{
		Connection connection = null;
		ArrayList<IPLTransmittalType> ttList = new ArrayList<IPLTransmittalType>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM transmittal_types WHERE sys_id=?");
			ps.setInt(1, aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while(rs.next())
					ttList.add(new IPLTransmittalType(rs.getInt("sys_id"), rs.getInt("transmittal_type_id"), rs.getString("name"), 
														rs.getString("display_name"), rs.getString("transmittal_process"),
														rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt("target_sys_id"),
														rs.getInt("sort_order")));					
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
	
	/*public static IPLTransmittalType lookupTransmittalTypeBySystemIdAndName(int aSystemId, String aName) throws SQLException{
		Connection connection = null;
		IPLTransmittalType kskTT = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM transmittal_types WHERE sys_id=? AND name=?");
			ps.setInt(1, aSystemId);
			ps.setString(2, aName);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new IPLTransmittalType(rs.getInt("sys_id"), rs.getInt("transmittal_type_id"), rs.getString("name"), 
													rs.getString("display_name"), rs.getString("transmittal_process"));
			
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
	}*/
	
	public static IPLTransmittalType lookupTransmittalTypeBySystemIdAndName(int aSystemId, String aName) throws SQLException{
		Connection connection = null;
		IPLTransmittalType kskTT = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM transmittal_types WHERE sys_id=? AND name=?");
			ps.setInt(1, aSystemId);
			ps.setString(2, aName);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new IPLTransmittalType(rs.getInt("sys_id"), rs.getInt("transmittal_type_id"), rs.getString("name"), 
												rs.getString("display_name"), rs.getString("transmittal_process"),
												rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt("target_sys_id"),
												rs.getInt("sort_order"));
			
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
	
	public static IPLTransmittalType lookupTransmittalTypeBySystemIdAndName(Connection connection, 
			int aSystemId, String aName) throws SQLException{
		
		IPLTransmittalType kskTT = null;
		try{
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM transmittal_types WHERE sys_id=? AND name=?");
			ps.setInt(1, aSystemId);
			ps.setString(2, aName);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new IPLTransmittalType(rs.getInt("sys_id"), rs.getInt("transmittal_type_id"), rs.getString("name"), 
						rs.getString("display_name"), rs.getString("transmittal_process"), rs.getInt("dtn_sys_id"),
						rs.getInt("dtr_sys_id"), rs.getInt("target_sys_id"), rs.getInt("sort_order"));

			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;			
		} 	
		return kskTT;
	}
	
	public static IPLTransmittalType lookupTransmittalTypeBySystemIdAndTransmittalTypeId(int aSystemId,
																							int aTransmittalTypeId) throws SQLException{
		Connection connection = null;
		IPLTransmittalType kskTT = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM transmittal_types WHERE sys_id=? AND transmittal_type_id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new IPLTransmittalType(rs.getInt("sys_id"), rs.getInt("transmittal_type_id"), rs.getString("name"), 
																rs.getString("display_name"), rs.getString("transmittal_process"),
																rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt("target_sys_id"),
																rs.getInt("sort_order"));
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

	public static IPLTransmittalType lookupTransmittalTypeBySystemIdAndTransmittalTypeId(Connection connection, 
																							int aSystemId, int aTransmittalTypeId) throws SQLException{
		IPLTransmittalType kskTT = null;
		try{
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM transmittal_types WHERE sys_id=? AND transmittal_type_id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new IPLTransmittalType(rs.getInt("sys_id"), rs.getInt("transmittal_type_id"), rs.getString("name"), 
																rs.getString("display_name"), rs.getString("transmittal_process"),
																rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt("target_sys_id"),
																rs.getInt("sort_order"));
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
			PreparedStatement ps = connection.prepareStatement("SELECT * from transmittal_process_parameters WHERE sys_id=? AND transmittal_type_id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					String key = rs.getString("entry");
					String value = rs.getString("value");
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
			PreparedStatement ps = connection.prepareStatement("SELECT * from transmittal_process_parameters WHERE sys_id=? AND transmittal_type_id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					String key = rs.getString("entry");
					String value = rs.getString("value");
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
		int maxTransmittalNumber = -1;System.out.println("Before stp_getAndIncr : " + transmittalProcessName);
		try{
			CallableStatement cs = connection.prepareCall("stp_transmittal_getMaxTransmittalId ?");//"stp_getAndIncrMaxId ?");
			cs.setString(1, transmittalProcessName);
			ResultSet rs = cs.executeQuery();
			if ((rs != null) && (rs.next())){
				maxTransmittalNumber = rs.getInt("max_transmittal_id");
				//System.out.println("MaxId: " + maxTransmittalNumber);
				return maxTransmittalNumber;
			}else {
				throw new SQLException();
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
		
		Hashtable<String, String> pp = IPLTransmittalType.getTransmittalProcessParameters(17, 1);
		System.out.println("catList: \n" + pp.get("approvalCategories"));		
	}

}