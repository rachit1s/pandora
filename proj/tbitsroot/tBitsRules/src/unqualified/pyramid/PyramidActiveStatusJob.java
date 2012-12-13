/**
 * 
 */
package pyramid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

/**
 * @author Lokesh
 *
 */
public class PyramidActiveStatusJob implements Job, TBitsConstants {
	
	 // Application logger.
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_SCHEDULER);

	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		JobDataMap jdm = arg0.getJobDetail().getJobDataMap();
		String sysPrefix = jdm.getString("sysPrefix");
		String pendingStatus = jdm.getString("previousStatusName");
		String currentStatus = jdm.getString("currentStatusName");
		String plannedFieldName = jdm.getString("dateFieldName");
		executeStatusChange(sysPrefix, pendingStatus, currentStatus, plannedFieldName);
	}

	private void executeStatusChange(String sysPrefix, String pendingStatus,
			String currentStatus, String dateFieldName) {
		Connection connection = null;
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			int systemId = ba.getSystemId();
			
			Type prevStatusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, pendingStatus);
			int prevTypeId = prevStatusType.getTypeId();
			
			Field plannedDateField = Field.lookupBySystemIdAndFieldName(systemId, dateFieldName);
			int plannedDateFieldId = plannedDateField.getFieldId();
			
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("select r.request_id from requests r " +
					"join requests_ex re on r.sys_id=re.sys_id and r.request_id = re.request_id where r.sys_id=" + 
					systemId + " and r.status_id=" + prevTypeId + " and re.field_id=" + plannedDateFieldId + 
					" and re.datetime_value <= getdate()");
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				int tempRequestId = 0;
				Hashtable <String,String> aParamTable = new Hashtable<String, String>();
				
				while (rs.next()){
					tempRequestId = rs.getInt("request_id");
					UpdateRequest updateRequest= new UpdateRequest();
					updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);

					aParamTable.put(Field.BUSINESS_AREA, ba.getSystemPrefix());
					aParamTable.put(Field.USER, "root");
					aParamTable.put(Field.REQUEST, tempRequestId + "");
					aParamTable.put(Field.STATUS, currentStatus);
					updateRequest.updateRequest(aParamTable);
				}
			}
			rs.close();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			LOG.error("Error while retrieving request_id");
			e.printStackTrace();
		} catch (DatabaseException e) {
			LOG.error("Error while retrieving field/type from database");
			e.printStackTrace();
		} catch (TBitsException e) {
			e.printStackTrace();
		} catch (APIException e) {
			LOG.error("Error while updating a request");
			e.printStackTrace();
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
	
	public static void main(String[] args){
		PyramidActiveStatusJob job = new PyramidActiveStatusJob();
		job.executeStatusChange("DCR345", "Pending", "Active", "plannedstartdate");
	}
}
