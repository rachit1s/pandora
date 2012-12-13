/**
 * 
 */
package lntdcr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;

/**
 * @author lokesh
 *
 */
public class WeightageInfo {

	private static final String WEIGHTAGE_TABLE_NAME = "weightage_info";//"weightage_field_map";
	private static final String DECISION_FIELD_MAP_BY_SYS_ID_QUERY = "SELECT * FROM " + WEIGHTAGE_TABLE_NAME + " WHERE SYS_ID = ?";	
	private static final String COLUMN_NAMES_QUERY = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.Columns where TABLE_NAME = ?";
	
	private static final String SYS_ID 				= "sys_id";
	private static final String CYCLE_ID			= "cycle_id";
	private static final String PRIMARY_FIELD_NAME  = "primary_field_name";
	private static final String PRIMARY_TYPE_NAME 	= "primary_type_name";
	private static final String SECONDARY_FIELD_NAME= "secondary_field_name";
	private static final String SECONDARY_TYPE_NAME = "secondary_type_name";
	private static final String FIRST_SUBMISSION  	= "first_submission";
	private static final String SECOND_SUBMISSION 	= "second_submission";
	private static final String APPROVAL_OR_RFC   	= "approval_or_rfc";
	private static final String AS_BUILT			= "as_built";
	
	protected static final String DELIMETER_UNDERSCORE = "_";
	
	private int systemId;
	private int cycleId;
	public int getCycleId() {
		return cycleId;
	}

	private String primaryFieldName;
	private String primaryTypeName;
	private String secondaryFieldName;
	private double firstSubmissionFactor;
	private double secondSubmissionFactor;
	private double approvalOrRFCFactor;
	private double asBuildFactor;
	private String secondaryTypeName;
	
	public int getSystemId() {
		return systemId;
	}

	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}

	public String getPrimaryFieldName() {
		return primaryFieldName;
	}

	public void setCycleId(int cycleId){
		this.cycleId = cycleId;
	}
	
	public void setPrimaryFieldName(String primaryFieldName) {
		this.primaryFieldName = primaryFieldName;
	}

	public String getPrimaryTypeName() {
		return primaryTypeName;
	}

	public void setPrimaryTypeName(String primaryTypeName) {
		this.primaryTypeName = primaryTypeName;
	}

	public String getSecondaryFieldName() {
		return secondaryFieldName;
	}

	public void setSecondaryFieldName(String secondaryFieldName) {
		this.secondaryFieldName = secondaryFieldName;
	}

	public double getFirstSubmissionFactor() {
		return firstSubmissionFactor;
	}

	public void setFirstSubmissionFactor(double firstSubmissionFactor) {
		this.firstSubmissionFactor = firstSubmissionFactor;
	}

	public double getSecondSubmissionFactor() {
		return secondSubmissionFactor;
	}

	public void setSecondSubmissionFactor(double secondSubmissionFactor) {
		this.secondSubmissionFactor = secondSubmissionFactor;
	}

	public double getApprovalOrRFCFactor() {
		return approvalOrRFCFactor;
	}

	public void setApprovalOrRFCFactor(double approvalOrRFCFactor) {
		this.approvalOrRFCFactor = approvalOrRFCFactor;
	}

	public double getAsBuiltFactor() {
		return asBuildFactor;
	}

	public void setAsBuildFactor(double asBuildFactor) {
		this.asBuildFactor = asBuildFactor;
	}

	public String getSecondaryTypeName() {
		return secondaryTypeName;
	}

	public void setSecondaryTypeName(String secondaryTypeName) {
		this.secondaryTypeName = secondaryTypeName;
	}

	
	public WeightageInfo(int systemId, int cycleId, String primaryFieldName, String primaryTypeName, String secondaryFieldName, 
			String secondaryTypeName, double firstSubmissionFactor, double secondSubmissionFactor, double approvalOrRFCFactor, 
			double asBuildFactor){
		
		this.systemId = systemId;
		this.cycleId  = cycleId;
		this.primaryFieldName = primaryFieldName;
		this.primaryTypeName = primaryTypeName;
		this.secondaryFieldName = secondaryFieldName;
		this.secondaryTypeName = secondaryTypeName;
		this.firstSubmissionFactor = firstSubmissionFactor;
		this.secondSubmissionFactor = secondSubmissionFactor;
		this.approvalOrRFCFactor = approvalOrRFCFactor;
		this.asBuildFactor = asBuildFactor;		
	}
	
	public static ArrayList<WeightageInfo> lookupWeightageInfoBySysId(int systemId) throws DatabaseException{
		
		ArrayList<WeightageInfo> wiList = new ArrayList<WeightageInfo>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(DECISION_FIELD_MAP_BY_SYS_ID_QUERY);
			ps.setInt(1, systemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next())
				{
					WeightageInfo wtg = createFromResultSet(rs);
					if (wtg != null)
						wiList.add(wtg);
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Database error occurred while fetching decision_field_map", e);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new DatabaseException("Database error occurred while fetching decision_field_map", e);
				}
		}
		
		return wiList;
	}
			
	private static WeightageInfo createFromResultSet(ResultSet rs) throws SQLException {
		return new WeightageInfo(rs.getInt(SYS_ID), rs.getInt(CYCLE_ID), rs.getString(PRIMARY_FIELD_NAME),
				rs.getString(PRIMARY_TYPE_NAME), rs.getString(SECONDARY_FIELD_NAME), rs.getString(SECONDARY_TYPE_NAME),
				rs.getDouble(FIRST_SUBMISSION), rs.getDouble(SECOND_SUBMISSION), rs.getDouble(APPROVAL_OR_RFC),
				rs.getDouble(AS_BUILT));
	}

	public static ArrayList<String> fetchColumnNamesForTable(String tableName) throws DatabaseException{

		ArrayList<String> columnNames = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			columnNames.addAll(fetchColumnNamesForTable(connection));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Database error occurred while fetching column names of table: " + tableName, e);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new DatabaseException("Database error occurred while fetching column names of table: " + tableName, e);
				}
		}
		return columnNames;
	}

		
	private static ArrayList<String> fetchColumnNamesForTable(Connection connection)
			throws SQLException {
		
		ArrayList<String> columnNames = new ArrayList<String>();
		PreparedStatement ps = connection.prepareStatement(COLUMN_NAMES_QUERY);
		ps.setString(1, WEIGHTAGE_TABLE_NAME);
		ResultSet rs = ps.executeQuery();
		if (rs != null){
			String columnName = "";
			while(rs.next())
			{
				columnName = rs.getString("COLUMN_NAME");				
				if ((columnName != null) && (columnName.trim().length() != 0))
					columnNames.add(columnName);
			}
		}
		rs.close();
		ps.close();
		return columnNames;
	}
	
	public static Hashtable<String, WeightageInfo> getWeightageMapBySysId(int aSystemId) {
		Hashtable<String, WeightageInfo> wtgMap = new Hashtable<String, WeightageInfo>();
		try {
			ArrayList<WeightageInfo> wiList = WeightageInfo.lookupWeightageInfoBySysId(aSystemId);
			if ((wiList != null) && (!wiList.isEmpty())){
				for (WeightageInfo wi : wiList)
				{
					if (wi != null){
						String key = wi.getPrimaryFieldName() + DELIMETER_UNDERSCORE + wi.getPrimaryTypeName() 
										+ DELIMETER_UNDERSCORE + wi.getSecondaryTypeName();
						wtgMap.put(key, wi);
					}
				}
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return wtgMap;
	}
	
//	public String toString(){
//		return "";
//	}
	
	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException {
		System.out.println("Columns are: " + fetchColumnNamesForTable ("decision_field_map"));
		
		System.out.println("WI: \n" + lookupWeightageInfoBySysId(104));

	}

}
