/**
 * 
 */
package ncc;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

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
public class NCCTransmittalProcess {
	
	private static final String PARAMETER_COLUMN = "parameter";
	private static final String TRN_PROCESS_PARAMETERS = "trn_process_parameters";
	private static final String TRN_MAX_SN_KEY = "trn_max_sn_key";
	private static final String TRN_DROPDOWN_ID = "trn_dropdown_id";
	private static final String MAIN_PROCESS_SELECT_QUERY = "select * from  trn_processes where sys_id=?" +
						" and transmittal_type_id not in (select sub_transmittal_type_id from transmittal_sub_processes where sys_id=?)";
	int mySystemId, myTrnProcessId, myDtnSysId, myDtrSysId, myTrnDropDownId, mySortOrder; 
	String myName, myDisplayName, myTrnMaxSnKey;
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
		
	public NCCTransmittalProcess(int aSystemId, int aTransmittalTypeId, String name, 
								String displayName, String aTrnMaxSnKey){
		this.mySystemId = aSystemId;
		this.myTrnProcessId = aTransmittalTypeId;
		this.myName = name;
		this.myDisplayName = displayName;
		this.myTrnMaxSnKey = aTrnMaxSnKey;
	}
	
	/*public NCCTransmittalType(NCCTransmittalType tt){
		this.mySystemId = tt.getSystemId();
		this.myTrnProcessId = tt.getTrnProcessId();
		this.myName = tt.getName();
		this.myDisplayName = tt.getDisplayName();
		this.myTrnMaxSnKey = tt.getTrnMaxSnKey();
		this.myDtnSysId = tt.getDtnSysId();
		this.myDtrSysId = tt.getDtrSysId();
		this.myTrnDropDownId = tt.getTrnDropDownId();
		this.mySortOrder = tt.getSortOrder();
	}*/
	
	public NCCTransmittalProcess(NCCTransmittalProcess tt){
		this.mySystemId = tt.getSystemId();
		this.myTrnProcessId = tt.getTrnProcessId();
		this.myName = tt.getName();
		this.myDisplayName = tt.getDisplayName();
		this.myTrnMaxSnKey = tt.getTrnMaxSnKey();
		this.myDtnSysId = tt.getDtnSysId();
		this.myDtrSysId = tt.getDtrSysId();
		this.myTrnDropDownId = tt.getTrnDropDownId();
	}
	
	public NCCTransmittalProcess(int aSystemId, int aTrnProcessId, String name, 
			String displayName, String aTrnMaxSnKey, int dtnSysId,
			int dtrSysId, int trnDropDownId, int sortOrder){
			this.mySystemId = aSystemId;
			this.myTrnProcessId = aTrnProcessId;
			this.myName = name;
			this.myDisplayName = displayName;
			this.myTrnMaxSnKey = aTrnMaxSnKey;
			this.myDtnSysId = dtnSysId;
			this.myDtrSysId = dtrSysId;
			this.myTrnDropDownId = trnDropDownId;
			this.mySortOrder = sortOrder;
	}
	
	public NCCTransmittalProcess(int aSystemId, int aTrnProcessId, String name, 
			String displayName, String aTrnMaxSnKey, int dtnSysId,
			int dtrSysId, int trnDropDownId){
			this.mySystemId = aSystemId;
			this.myTrnProcessId = aTrnProcessId;
			this.myName = name;
			this.myDisplayName = displayName;
			this.myTrnMaxSnKey = aTrnMaxSnKey;
			this.myDtnSysId = dtnSysId;
			this.myDtrSysId = dtrSysId;
			this.myTrnDropDownId = trnDropDownId;
	}
	
	public int getSystemId(){
		return this.mySystemId;
	}
	
	public int getTrnProcessId(){
		return this.myTrnProcessId;
	}
	
	public String getName(){
		return this.myName;
	}
	
	public String getDisplayName(){
		return this.myDisplayName;
	}
	
	public String getTrnMaxSnKey(){
		return this.myTrnMaxSnKey;
	}
	
	public int getDtnSysId(){
		return this.myDtnSysId;
	}
	
	public int getDtrSysId(){
		return this.myDtrSysId;
	}
	
	public int getTrnDropDownId(){
		return this.myTrnDropDownId;
	}
	
	public int getSortOrder(){
		return this.mySortOrder;
	}
	
	public void setTransmittalType(NCCTransmittalProcess tt){
		this.mySystemId = tt.getSystemId();
		this.myTrnProcessId = tt.getTrnProcessId();
		this.myName = tt.getName();
		this.myDisplayName = tt.getDisplayName();
		this.myTrnMaxSnKey = tt.getTrnMaxSnKey();
		this.myDtnSysId = tt.getDtnSysId();
		this.myDtrSysId = tt.getDtrSysId();
		this.myTrnDropDownId = tt.getTrnDropDownId();
		this.mySortOrder = tt.getSortOrder();
	}
	
	public static ArrayList<NCCTransmittalProcess> lookupTransmittalTypesBySystemId(int aSystemId) throws SQLException{
		Connection connection = null;
		ArrayList<NCCTransmittalProcess> ttList = new ArrayList<NCCTransmittalProcess>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(MAIN_PROCESS_SELECT_QUERY);
					//"SELECT * FROM trn_processes WHERE sys_id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while(rs.next())
					ttList.add(new NCCTransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID), rs.getString("name"), 
														rs.getString("display_name"), rs.getString(TRN_MAX_SN_KEY),
														rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt(TRN_DROPDOWN_ID),
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
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE sys_id=? AND name=?");
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
	
	public static NCCTransmittalProcess lookupTransmittalTypeBySystemIdAndName(int aSystemId, String aName) throws SQLException{
		Connection connection = null;
		NCCTransmittalProcess kskTT = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE src_sys_id=? AND name=?");
			ps.setInt(1, aSystemId);
			ps.setString(2, aName);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new NCCTransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID), rs.getString("name"), 
												rs.getString("display_name"), rs.getString(TRN_MAX_SN_KEY),
												rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt(TRN_DROPDOWN_ID));
												//,	rs.getInt("sort_order"));
			
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
	
	public static ArrayList<NCCTransmittalProcess> lookupTransmittalTypeBySystemIdAndtrnDropDownId(int aSystemId, int trnDropDownId) 
	throws SQLException{
		Connection connection = null;
		ArrayList<NCCTransmittalProcess> ttList = new ArrayList<NCCTransmittalProcess>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE src_sys_id=? AND trn_dropdown_id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, trnDropDownId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while(rs.next())
					ttList.add(new NCCTransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID), rs.getString("name"), 
												rs.getString("display_name"), rs.getString(TRN_MAX_SN_KEY),
												rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt(TRN_DROPDOWN_ID)));
												//,	rs.getInt("sort_order")));
			
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
		return ttList;
	}
		
	public static NCCTransmittalProcess lookupTransmittalTypeBySystemIdAndName(Connection connection, 
			int aSystemId, String aName) throws SQLException{
		
		NCCTransmittalProcess kskTT = null;
		try{
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE src_sys_id=? AND name=?");
			ps.setInt(1, aSystemId);
			ps.setString(2, aName);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new NCCTransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID), rs.getString("name"), 
						rs.getString("display_name"), rs.getString(TRN_MAX_SN_KEY), rs.getInt("dtn_sys_id"),
						rs.getInt("dtr_sys_id"), rs.getInt(TRN_DROPDOWN_ID), rs.getInt("sort_order"));

			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;			
		} 	
		return kskTT;
	}
	
	public static NCCTransmittalProcess lookupTransmittalTypeBySystemIdAndTransmittalTypeId(int aSystemId,
																							int aTransmittalTypeId) throws SQLException{
		Connection connection = null;
		NCCTransmittalProcess kskTT = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE src_sys_id=? AND transmittal_type_id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new NCCTransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID), rs.getString("name"), 
																rs.getString("display_name"), rs.getString(TRN_MAX_SN_KEY),
																rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt(TRN_DROPDOWN_ID),
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

	public static NCCTransmittalProcess lookupTransmittalTypeBySystemIdAndTransmittalTypeId(Connection connection, 
																							int aSystemId, int aTransmittalTypeId) throws SQLException{
		NCCTransmittalProcess kskTT = null;
		try{
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE src_sys_id=? AND transmittal_type_id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new NCCTransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID), rs.getString("name"), 
																rs.getString("display_name"), rs.getString(TRN_MAX_SN_KEY),
																rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt(TRN_DROPDOWN_ID),
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
			PreparedStatement ps = connection.prepareStatement("SELECT * from " + TRN_PROCESS_PARAMETERS
					+ " WHERE src_sys_id=? AND " + TransmittalUtils.TRN_PROCESS_ID + "=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					String key = rs.getString(PARAMETER_COLUMN);
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
			Connection connection, int aSystemId, int aTrnProcessId) throws SQLException{
		Hashtable<String, String> ttProcessParams = new Hashtable<String, String>();
		try{
			//connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * from " + TRN_PROCESS_PARAMETERS
					+ " WHERE src_sys_id=? AND " + TransmittalUtils.TRN_PROCESS_ID + "=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aTrnProcessId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					String key = rs.getString(PARAMETER_COLUMN);
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
	
	public static int getMaxTransmittalNumber(Connection connection, int aSystemId, String transmittalProcessName)
			throws DatabaseException{
		return TransmittalUtils.getMaxIdByName(connection, aSystemId, transmittalProcessName);
	}

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
		
		/*Hashtable<String, String> pp = NCCTransmittalType.getTransmittalProcessParameters(17, 1);
		System.out.println("catList: \n" + pp.get("approvalCategories"));*/
		
		ArrayList<NCCTransmittalProcess> tt= lookupTransmittalTypesBySystemId(8);
		System.out.println(tt.get(0).getSystemId() + "," + tt.get(0).getDisplayName());
	}

}