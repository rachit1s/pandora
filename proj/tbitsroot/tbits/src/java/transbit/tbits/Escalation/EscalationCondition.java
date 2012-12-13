/**
 * 
 */
package transbit.tbits.Escalation;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

/**
 * @author Lokesh
 *
 */
public class EscalationCondition {
	
	private static final int COLUMN_SYSTEM_ID = 1;
	private static final int COLUMN_SEVERITY_ID = 2;
	private static final int COLUMN_SPAN = 3;
	private static final int COLUMN_CATEGORY_ID = 4;
	private static final int COLUMN_STATUS_ID = 5;
	private static final int COLUMN_TYPE_ID = 6;
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_SCHEDULER);
	// Escalation condition fields
	private int mySystemId;
	private int mySeverityId;
	private int myCategoryId;
	private int myStatusId;
	private int myTypeId;
	private int mySpan;
	
	public EscalationCondition(int systemId, int severityId, int span, int categoryId, int statusId, int typeId){
		this.mySystemId = systemId;		
		this.mySeverityId = severityId;
		this.myCategoryId = categoryId;
		this.myStatusId = statusId;
		this.myTypeId = typeId;
		this.mySpan = span;
	}
	
	public int getSystemId(){
		return this.mySystemId;
	}

	public int getSeverityId(){
		return this.mySeverityId;
	}
	
	public int getCategoryId(){
		return this.myCategoryId;
	}
	
	public int getStatusId(){
		return this.myStatusId;
	}
	
	public int getTypeId(){
		return this.myTypeId;
	}
	
	public int getSpan(){
		return this.mySpan;
	}
	
	public static ArrayList<EscalationCondition> lookupEscConditionBySysId(int aSystemId) throws DatabaseException{
		
		ArrayList<EscalationCondition> escConditionsList = new ArrayList<EscalationCondition>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM escalation_conditions WHERE " +
					"sys_id=" + aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					EscalationCondition ec = createFromResultSet(rs);
					if (ec != null)
						escConditionsList.add(ec);
				}
				rs.close();
				rs = null;
			}
			ps.close();
			ps = null;
//			connection.close();
		} catch (SQLException sqle) {StringBuilder message = new StringBuilder();

		message.append("An exception occurred while retrieving Escalation conditions.\n");

		throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}            
		}   	
		return escConditionsList;
	}
	
	public static void insert (EscalationCondition ec) throws DatabaseException{
		
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			CallableStatement cs = connection.prepareCall("stp_admin_insert_escalation_condition ?,?,?,?,?,?");
			cs.setInt(COLUMN_SYSTEM_ID, ec.getSystemId());
			cs.setInt(COLUMN_SEVERITY_ID, ec.getSeverityId());
			cs.setInt(COLUMN_SPAN, ec.getSpan());
			cs.setInt(COLUMN_CATEGORY_ID, ec.getCategoryId());
			cs.setInt(COLUMN_STATUS_ID, ec.getStatusId());
			cs.setInt(COLUMN_TYPE_ID, ec.getTypeId());
			cs.execute();
			cs.close();
			cs = null;
//			connection.close();
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while inserting Escalation condition.\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}            
		}
	}
	
	public static void delete (EscalationCondition ec) throws DatabaseException{

		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			CallableStatement cs = connection.prepareCall("stp_admin_delete_escalation_condition ?,?,?,?,?,?");
			cs.setInt(COLUMN_SYSTEM_ID, ec.getSystemId());
			cs.setInt(COLUMN_SEVERITY_ID, ec.getSeverityId());
			cs.setInt(COLUMN_SPAN, ec.getSpan());
			cs.setInt(COLUMN_CATEGORY_ID, ec.getCategoryId());
			cs.setInt(COLUMN_STATUS_ID, ec.getStatusId());
			cs.setInt(COLUMN_TYPE_ID, ec.getTypeId());
			cs.execute();
			cs.close();
			cs = null;
//			connection.close();
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while deleting Escalation condition.\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}            
		}
	}
	
	private static EscalationCondition createFromResultSet(ResultSet rs) throws SQLException {
		EscalationCondition ec = null;
		if (rs != null)
			ec = new EscalationCondition(rs.getInt(COLUMN_SYSTEM_ID), rs.getInt(COLUMN_SEVERITY_ID), rs.getInt(COLUMN_SPAN), 
					rs.getInt(COLUMN_CATEGORY_ID), rs.getInt(COLUMN_STATUS_ID), rs.getInt(COLUMN_TYPE_ID));
		return ec;
	}
	
	public static void main(String[] args){
		try {
			//System.out.println("%%%%%%%%%%%%%%%%%%%" + EscalationCondition.lookupEscConditionBySysId(3).toString());
			EscalationCondition ec = new EscalationCondition(6,2,9,0,0,0);
			System.out.println("#################inserting:  ");
			//EscalationCondition.insert(ec);
			EscalationCondition.delete(ec);
			System.out.println("Done!!!");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
