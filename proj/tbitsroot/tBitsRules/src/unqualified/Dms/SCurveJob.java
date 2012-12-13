package Dms;

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



public class SCurveJob {
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_SCHEDULER);
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		JobDataMap jdm = arg0.getJobDetail().getJobDataMap();
		String sysPrefix = jdm.getString("sysPrefix");
		String PlannedFieldName = jdm.getString("PlannedweightageFieldName");
		String ActualFieldName = jdm.getString("ActualdweightageFieldName");
		executeInsertScurveData(sysPrefix ,PlannedFieldName,ActualFieldName);
	}
		
		
		private void executeInsertScurveData(String sysPrefix ,String PlannedFieldName,String ActualFieldName)
		
		{
			
			Connection connection = null;
			try
			{    
				
				BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
				int systemId = ba.getSystemId();
				
				Field  PlannedWeightageDataField = Field.lookupBySystemIdAndFieldName(systemId, PlannedFieldName);
				int PlannedWeightageFieldId      = PlannedWeightageDataField.getFieldId();
				
				Field  ActualWeightageDataField = Field.lookupBySystemIdAndFieldName(systemId, ActualFieldName);
				int ActualWeightageFieldId      = ActualWeightageDataField.getFieldId();
				
				
				connection = DataSourcePool.getConnection();
				
				PreparedStatement ps = connection.prepareStatement("IF OBJECT_ID ('dbo.Scurve') IS NOT NULL"+
						                                           " Print 'Table Exists' ELSE "+
						                                           " create table Scurve( date datetime," +
						                                           " plannedWeight real,ActualWeight real)");
				ps.execute();
				
				               
				PreparedStatement Stmt = connection.prepareStatement("select r.sys_id,sum(re.real_value) activityWeight,a.due_datetime planned_Date"+
			               " into #tmp from requests r"+
			               " join actions a on a.sys_id  = r.sys_id  and a.request_id  = r.request_id and a.action_id  = 1"+
			               " join  requests_ex re on r.sys_id = re.sys_id and r.request_id  = re.request_id "+
			               " where r.sys_id  =  ? and re.field_id  = ? and a.action_id  = 1"+
			               " and a.due_datetime is not null group by a.due_datetime,r.sys_id order by a.due_datetime"+
			               " select t.sys_id,getdate() date,sum(t.activityWeight) plannedWeight"+
			               " into #tmp1 from #tmp t where t.planned_Date <= getdate() group by t.sys_id"+
			               " select  r.sys_id,getdate()date,sum(re.real_value) ActualWeight"+
			               " into #tmp2 from requests r"+
			               " join requests_ex re on r.sys_id  = re.sys_id and r.request_id  =  re.request_id and re.field_id  = ?" + 
			               " where r.sys_id  = ?  group by r.sys_id"+
			               " insert into Scurve select t.date,t.plannedWeight,t1.ActualWeight"+
			               " from #tmp1 t left join #tmp2 t1 on   t.sys_id  = t1.sys_id"+
			               " drop table #tmp drop table #tmp1 drop table #tmp2");
				
				
				
				Stmt.setInt(1, systemId);
				Stmt.setInt(2, PlannedWeightageFieldId);
				Stmt.setInt(3, ActualWeightageFieldId);
				Stmt.setInt(4, systemId);
				
				
				
				Stmt.execute();
				
				
				ps.close();
				Stmt.close();
				connection.close();
			}
			catch (SQLException e) 
			  {
				LOG.error("Error while insert Scurve");
				e.printStackTrace();
			  }
			catch (DatabaseException e) 
			{
				LOG.error("Error while retrieving field/type from database");
				e.printStackTrace();
			}
		
		
	}
		
		public static void main(String[] args)
		{
			SCurveJob job = new SCurveJob();
			
			job.executeInsertScurveData("DCR343","ActivityPercentage","actualpercentagecompleted");
			
		}

}
