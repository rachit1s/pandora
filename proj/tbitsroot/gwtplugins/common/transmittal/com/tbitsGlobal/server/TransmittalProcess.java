/**
 * 
 */
package transmittal.com.tbitsGlobal.server;

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
public class TransmittalProcess {
	
	private static final String PARAMETER_COLUMN = "parameter";
	private static final String TRN_PROCESS_PARAMETERS = "trn_process_parameters";
	private static final String TRN_MAX_SN_KEY = "trn_max_sn_key";
	private static final String TRN_DROPDOWN_ID = "trn_dropdown_id";
	private static final String MAIN_PROCESS_SELECT_QUERY = "select * from  trn_processes where sys_id=?" +
						" and transmittal_type_id not in (select sub_transmittal_type_id from transmittal_sub_processes where sys_id=?)";
	int mySystemId, myTrnProcessId, myDtnSysId, myDtrSysId, myTrnDropDownId, mySortOrder; 
	String myName, myDisplayName, myTrnMaxSnKey;
	private String myDescription;
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
		
	public TransmittalProcess(int aSystemId, int aTransmittalTypeId, String aDescription, String aTrnMaxSnKey){
		this.mySystemId = aSystemId;
		this.myTrnProcessId = aTransmittalTypeId;
		this.myDescription = aDescription;
		this.myTrnMaxSnKey = aTrnMaxSnKey;
	}
		
	public TransmittalProcess(TransmittalProcess tt){
		this.mySystemId = tt.getSystemId();
		this.myTrnProcessId = tt.getTrnProcessId();
		this.myDescription = tt.getDescription();
		this.myTrnMaxSnKey = tt.getTrnMaxSnKey();
		this.myDtnSysId = tt.getDtnSysId();
		this.myDtrSysId = tt.getDtrSysId();
		this.myTrnDropDownId = tt.getTrnDropDownId();
	}
	
	public TransmittalProcess(int aSystemId, int aTrnProcessId, String description,
			String aTrnMaxSnKey, int dtnSysId,
			int dtrSysId, int trnDropDownId, int sortOrder){
			this.mySystemId = aSystemId;
			this.myTrnProcessId = aTrnProcessId;
			this.myDescription = description;
			this.myTrnMaxSnKey = aTrnMaxSnKey;
			this.myDtnSysId = dtnSysId;
			this.myDtrSysId = dtrSysId;
			this.myTrnDropDownId = trnDropDownId;
			this.mySortOrder = sortOrder;
	}
	
	public TransmittalProcess(int aSystemId, int aTrnProcessId, String description, 
			String aTrnMaxSnKey, int dtnSysId,
			int dtrSysId, int trnDropDownId){
			this.mySystemId = aSystemId;
			this.myTrnProcessId = aTrnProcessId;
			this.myDescription = description;
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
	
	private String getDescription() {
		return myDescription;
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
	
	public void setTransmittalType(TransmittalProcess tt){
		this.mySystemId = tt.getSystemId();
		this.myTrnProcessId = tt.getTrnProcessId();
		this.myDescription = tt.getDescription();
		this.myTrnMaxSnKey = tt.getTrnMaxSnKey();
		this.myDtnSysId = tt.getDtnSysId();
		this.myDtrSysId = tt.getDtrSysId();
		this.myTrnDropDownId = tt.getTrnDropDownId();
		this.mySortOrder = tt.getSortOrder();
	}	

	public static ArrayList<TransmittalProcess> lookupTransmittalTypesBySystemId(int aSystemId) throws SQLException{
		Connection connection = null;
		ArrayList<TransmittalProcess> ttList = new ArrayList<TransmittalProcess>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(MAIN_PROCESS_SELECT_QUERY);
			ps.setInt(1, aSystemId);
			ps.setInt(2, aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while(rs.next())
					ttList.add(new TransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID),
														rs.getString("description"), rs.getString(TRN_MAX_SN_KEY),
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
	
	public static ArrayList<TransmittalProcess> lookupTransmittalProcessesBySystemId(int aSystemId) throws SQLException{
		Connection connection = null;
		ArrayList<TransmittalProcess> ttList = new ArrayList<TransmittalProcess>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * from trn_processes where src_sys_id=?");
			ps.setInt(1, aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while(rs.next())
					ttList.add(new TransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID),
														rs.getString("description"), rs.getString(TRN_MAX_SN_KEY),
														rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt(TRN_DROPDOWN_ID)));					
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
	
	public static TransmittalProcess lookupTransmittalTypeBySystemIdAndName(int aSystemId, String aName) throws SQLException{
		Connection connection = null;
		TransmittalProcess kskTT = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE src_sys_id=? AND name=?");
			ps.setInt(1, aSystemId);
			ps.setString(2, aName);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new TransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID),
												rs.getString("description"), rs.getString(TRN_MAX_SN_KEY),
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
	
	public static ArrayList<TransmittalProcess> lookupTransmittalTypeBySystemIdAndtrnDropDownId(int aSystemId, int trnDropDownId) 
	throws SQLException{
		Connection connection = null;
		ArrayList<TransmittalProcess> ttList = new ArrayList<TransmittalProcess>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE src_sys_id=? AND trn_dropdown_id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, trnDropDownId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while(rs.next())
					ttList.add(new TransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID),
												rs.getString("description"), rs.getString(TRN_MAX_SN_KEY),
												rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt(TRN_DROPDOWN_ID)));
			
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
	
	public static ArrayList<TransmittalProcess> lookupTransmittalProcessesBySystemIdAndtrnDropDownId(int aSystemId, int trnDropDownId) 
	throws SQLException{
		Connection connection = null;
		ArrayList<TransmittalProcess> ttList = new ArrayList<TransmittalProcess>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE src_sys_id=? AND trn_dropdown_id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, trnDropDownId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while(rs.next())
					ttList.add(new TransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID),
												rs.getString("description"), rs.getString(TRN_MAX_SN_KEY),
												rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt(TRN_DROPDOWN_ID)));			
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
		
	public static TransmittalProcess lookupTransmittalProcessBySystemIdAndTransmittalProcessId(int aSystemId,
																							int aProcessId) throws SQLException{
		Connection connection = null;
		TransmittalProcess kskTT = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE src_sys_id=? AND " 
																	+ TransmittalUtils.TRN_PROCESS_ID + "=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aProcessId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new TransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID),
															rs.getString("description"), rs.getString(TRN_MAX_SN_KEY),
																rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"),
																rs.getInt(TRN_DROPDOWN_ID));
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

	
	public static TransmittalProcess lookupTransmittalProcessByTransmittalProcessId(int aProcessId)
	throws SQLException{
		Connection connection = null;
		TransmittalProcess kskTT = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM trn_processes WHERE " 
					+ TransmittalUtils.TRN_PROCESS_ID + "=?");
			ps.setInt(1, aProcessId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				kskTT = new TransmittalProcess(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt(TransmittalUtils.TRN_PROCESS_ID),
						rs.getString("description"), rs.getString(TRN_MAX_SN_KEY),
						rs.getInt("dtn_sys_id"), rs.getInt("dtr_sys_id"), rs.getInt(TRN_DROPDOWN_ID));
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
	
	public static Hashtable<String, String> getTransmittalProcessParameters(int aTransmittalProcessId) throws SQLException{
		Hashtable<String, String> ttProcessParams = new Hashtable<String, String>();
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * from " + TRN_PROCESS_PARAMETERS
					+ " WHERE " + TransmittalUtils.TRN_PROCESS_ID + "=?");
			ps.setInt(1, aTransmittalProcessId);
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
			CallableStatement cs = connection.prepareCall("stp_getAndIncrMaxId ?");
			cs.setString(1, transmittalProcessName);
			ResultSet rs = cs.executeQuery();
			if ((rs != null) && (rs.next())){
				maxTransmittalNumber = rs.getInt("max_id");
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
		
		ArrayList<TransmittalProcess> tt= lookupTransmittalTypesBySystemId(22);
		System.out.println(tt.get(0).getSystemId() + "," + tt.get(0).getDescription());
	}

}