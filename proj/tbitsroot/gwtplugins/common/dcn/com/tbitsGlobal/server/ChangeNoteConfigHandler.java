/**
 * 
 */
package dcn.com.tbitsGlobal.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import transbit.tbits.common.DataSourcePool;
import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

/**
 * @author Lokesh
 *
 */
public class ChangeNoteConfigHandler {
		
	public static ChangeNoteConfig lookupChangeNoteConfigBySrcSysPrefixAndTargetSysPrefix (
			String aSysPrefix, String targetSysPrefix){
		
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(SELECT_FROM + TRN_CHANGE_NOTE_CONFIG_TABLE_NAME 
																	+ WHERE + SRC_SYS_PREFIX + "=? " + AND 
																			+ TARGET_SYS_PREFIX
																			+ "=? ");
			ps.setString(1, aSysPrefix);		
			ps.setString(2, targetSysPrefix);
			ResultSet rs = ps.executeQuery();			
			if ((rs != null) && (rs.next())){
				ChangeNoteConfig cnbMap = ChangeNoteConfigHandler.createCNBMapFromResultSet(rs);
				return cnbMap;
			}
	
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}	

	public static ArrayList<ChangeNoteConfig> lookupChangeNoteConfigByBAType(String baType) {
	
		ArrayList<ChangeNoteConfig> cnbMapList = new ArrayList<ChangeNoteConfig>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(SELECT_FROM + TRN_CHANGE_NOTE_CONFIG_TABLE_NAME 
																	+ WHERE + BA_TYPE + "=? ");
			ps.setString(1, baType);
			ResultSet rs = ps.executeQuery();			
			if (rs != null)
				while(rs.next()){
					ChangeNoteConfig cnbMap = createCNBMapFromResultSet(rs);
					if (cnbMap != null)
						cnbMapList.add(cnbMap);
				}
	
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return cnbMapList;
	}
	
	public static ArrayList<ChangeNoteConfig> lookupChangeNoteConfigBySourceSysPrefix(String srcSysPrefix) {
		
		ArrayList<ChangeNoteConfig> changeNoteConfigList = new ArrayList<ChangeNoteConfig>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(SELECT_FROM + TRN_CHANGE_NOTE_CONFIG_TABLE_NAME 
																	+ WHERE + SRC_SYS_PREFIX + "=? ");
			ps.setString(1, srcSysPrefix);
			ResultSet rs = ps.executeQuery();			
			if (rs != null)
				while(rs.next()){
					ChangeNoteConfig cnbMap = createCNBMapFromResultSet(rs);
					if (cnbMap != null)
						changeNoteConfigList.add(cnbMap);
				}
	
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return changeNoteConfigList;
	}

	public static ArrayList<String> lookupSrcBusinessAreaByBAType(String baType) {
	
		ArrayList<String> changeNoteConfigList = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("Select " + SRC_SYS_PREFIX + " from " 
																+ TRN_CHANGE_NOTE_CONFIG_TABLE_NAME 
																+ WHERE + BA_TYPE + "=?");
			ps.setString(1, baType);
			ResultSet rs = ps.executeQuery();			
			if (rs != null){
				while((rs.next())){
					String srcSysPrefix = rs.getString(SRC_SYS_PREFIX);
					if ((srcSysPrefix != null) && (srcSysPrefix.trim().length() == 0) )
						changeNoteConfigList.add(srcSysPrefix);
				}
			}
	
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return changeNoteConfigList;
	}
	
	public static ArrayList<ChangeNoteConfig> lookupAllChangeNoteConfig() {
		ArrayList<ChangeNoteConfig> changeNoteConfigList = new ArrayList<ChangeNoteConfig>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("Select *" + " from " 
																	+ TRN_CHANGE_NOTE_CONFIG_TABLE_NAME);
			ResultSet rs = ps.executeQuery();			
			if (rs != null){
				while(rs.next()){
					ChangeNoteConfig cnbMap = createCNBMapFromResultSet(rs);
					if (cnbMap != null)
						changeNoteConfigList.add(cnbMap);
				}
			}
	
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return changeNoteConfigList;
	}
	
	public static HashMap<String, String> lookDistinctBATypes() {
		HashMap<String, String> baTypeMap = new HashMap<String, String>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("Select distinct " + BA_TYPE + ", " +  CAPTION+ " from " 
																	+ TRN_CHANGE_NOTE_CONFIG_TABLE_NAME);
			ResultSet rs = ps.executeQuery();			
			if (rs != null){
				while(rs.next()){
					String baType = rs.getString (BA_TYPE);
					String caption = rs.getString (CAPTION);
					if ((baType != null) && (caption != null))
						baTypeMap.put(baType, caption);
				}
			}
	
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return baTypeMap;
	}
	


	public static ChangeNoteConfig lookupChangeNoteConfigBySysPrefixAndBAType(String aSysPrefix, String baType){
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(SELECT_FROM + TRN_CHANGE_NOTE_CONFIG_TABLE_NAME 
																	+ WHERE + SRC_SYS_PREFIX + "=? " + AND + BA_TYPE
																    + "=? ");
			ps.setString(1, aSysPrefix);
			ps.setString(2, baType);
			ResultSet rs = ps.executeQuery();			
			if (rs != null) 
				while (rs.next()){
					ChangeNoteConfig cnbMap = ChangeNoteConfigHandler.createCNBMapFromResultSet(rs);
					return cnbMap;
				}
	
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}	
		
	static ChangeNoteConfig createCNBMapFromResultSet(ResultSet rs) throws SQLException {
		return new ChangeNoteConfig(rs.getInt(CHANGE_NOTE_ID), rs.getString(SRC_SYS_PREFIX), rs.getString(BA_TYPE), 
				rs.getString(TARGET_SYS_PREFIX), rs.getString(CAPTION), rs.getString(TEMPLATE_NAME), 
				rs.getInt(SRC_ATTACHMENT_FIELD_ID), rs.getInt(TARGET_ATTACHMENT_FIELD_ID),rs.getString(UPDATE_SYS_PREFIX));
	}
			
	static final String TRN_CHANGE_NOTE_CONFIG_TABLE_NAME 	= "trn_change_note_configuration";
	static final String TARGET_ATTACHMENT_FIELD_ID 			= "target_attachment_field_id";
	static final String SRC_ATTACHMENT_FIELD_ID 			= "src_attachment_field_id";
	static final String TARGET_SYS_PREFIX 					= "target_sys_prefix";
	static final String CAPTION 							= "caption";	
	static final String TEMPLATE_NAME 						= "template_name";
	static final String BA_TYPE 							= "ba_type";
	static final String SRC_SYS_PREFIX 						= "src_sys_prefix";
	static final String CHANGE_NOTE_ID 						= "change_note_id";
	static final String UPDATE_SYS_PREFIX                   = "update_sys_prefix";
	static final String SELECT_FROM 						= "Select * from ";
	static final String AND 								= " and ";
	static final String WHERE 								= " where ";
	
}
