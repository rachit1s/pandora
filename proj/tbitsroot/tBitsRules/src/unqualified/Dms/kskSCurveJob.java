package Dms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;

import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
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
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;



public class kskSCurveJob implements ITBitsJob{
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_SCHEDULER);
	  
	
	
	public static final String BA = "BA_sys_prefix";
	public static final String AW = "Weightage";
	public static final String APC = "ActualPercentageComplete";
	
	public String getDisplayName() 
	  {
		return "Ksk_ScurveJob" ;
	  }	
	public Hashtable<String, JobParameter> getParameters() throws SQLException 
	  {  
		  Hashtable<String,JobParameter> Params = new Hashtable<String,JobParameter>();
		  JobParameter param;
		  param = new JobParameter();
		  param.setName(BA);
		  param.setMandatory(true);
		  param.setType(ParameterType.Text);
		  
		 Params.put(BA, param);
		 
		 //Activity weightage field Name
		  param = new JobParameter();
		  param.setName(AW);
		  param.setMandatory(true);
		  param.setType(ParameterType.Text);
		  
		  Params.put(AW, param);
		  
	//actual percentage complete.	  
		  param = new JobParameter();
		  param.setName(APC);
		  param.setMandatory(true);
		  param.setType(ParameterType.Text);
		 
		  Params.put(APC, param);
		  
		  
		 return Params;
	  }
	  public boolean validateParams(Hashtable<String, String> params)
			throws IllegalArgumentException {
		
		return true;
	}
	
	  
	  
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
	    JobDetail jd =  arg0.getJobDetail();
	    JobDataMap jdm = jd.getJobDataMap();
	    
	    this.execute(jdm);
	}
	
	public void execute(JobDataMap jdm) throws JobExecutionException 
	  {
		String[] keys = jdm.getKeys();
		
		HashMap<String, String> params = new HashMap<String, String>();
		
		String BA_sys_prefix = "IntDTR";
		String Weightage = "Weightage";
		String ActualPercentageComplete = "ActualPercentageComplete";
		
		for(String key:keys)
		{
			String val = jdm.getString(key);
			if(key.equals(BA))
			 {
				BA_sys_prefix = val;
			 }
			else if(key.equals(AW))
			{
				Weightage = val;
			}
			else if(key.equals(APC))
			{
				ActualPercentageComplete = val;
			}
		
		 else
		  {
			params.put(key, val);
		  }
		}
		try
		{
		executeInsertScurveData(BA_sys_prefix ,Weightage,ActualPercentageComplete);
		}
		catch (IllegalArgumentException e) {
			LOG.error(e);
			e.printStackTrace();
		}
		
	 }
	
		
	
	  
	 
	
	
		private void executeInsertScurveData(String sysPrefix ,String PlannedFieldName,String ActualFieldName)
		
		{
			
			Connection connection = null;
			PreparedStatement ps = null;
			PreparedStatement Stmt = null;
			
			
			try
			{    
				
				BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
				int systemId = ba.getSystemId();
				
				Field  PlannedWeightageDataField = Field.lookupBySystemIdAndFieldName(systemId, PlannedFieldName);
				int PlannedWeightageFieldId      = PlannedWeightageDataField.getFieldId();
				
				Field  ActualWeightageDataField = Field.lookupBySystemIdAndFieldName(systemId, ActualFieldName);
				int ActualWeightageFieldId      = ActualWeightageDataField.getFieldId();
				
				
				connection = DataSourcePool.getConnection();
				
				 ps = connection.prepareStatement("IF OBJECT_ID ('dbo.Scurve') IS NOT NULL"+
						                                           " Print 'Table Exists' ELSE "+
						                                           " create table Scurve( date datetime," +
						                                           " plannedWeight real,ActualWeight real)");
				ps.execute();
				
				               
				 Stmt = connection.prepareStatement("select r.sys_id,sum(re.real_value) activityWeight,a.due_datetime planned_Date"+
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
				System.out.println(systemId);
			System.out.println(PlannedWeightageFieldId);
			System.out.println(ActualWeightageFieldId);
			
				Stmt.execute();
				
				
				
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
			finally
			{ 
				try
				{
					ps.close();
					Stmt.close();
					connection.close();
				}
				catch(SQLException e)
				{
					LOG.error("Error while insert Scurve");
					e.printStackTrace();
				}
				
				
			}
		
		
	}
		
	/*	public static void main(String[] args) throws Exception
		{
			kskSCurveJob job = new kskSCurveJob();
			
			job.executeInsertScurveData("IntDTR","Weightage","ActualPercentageComplete");
			
		}*/

}
