package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.ArrayList;
import java.util.Date;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class JobDetailClient extends TbitsModelData{
	private static final long serialVersionUID = 1L;
	public static String JOB_STATE = "job_state";
	public static String JOB_NAME  =  "job_name";
	public static String JOB_GROUP  =  "job_group";
	public static String DESCRIPTION = "description";
	public static String JOB_CLASS_NAME = "Job_class_name";
	public static String CRON_EXPRESSION = "cron_expression";
	public static String JOB_PARAMETERS = "job_params";
	public static String END_DATE = "end_date";
	public static String START_DATE = "start_date";
	
	public static String IS_RECOVERABLE = "recoverable";
	public static String IS_DURABLE = "durable";
	public static String IS_VOLATILE = "volatile";
		
	
	public void setRequestRecovery(Boolean parameter){
		this.set(IS_RECOVERABLE,parameter);
	}
	
	public Boolean requestRecovery(){
		return this.get(IS_RECOVERABLE);
	}
	
	public void setDurability(Boolean parameter){
		this.set(IS_DURABLE,parameter);
	}
	
	public Boolean isDurable(){
		return this.get(IS_DURABLE);
	}
	
	public void setVolatility(Boolean parameter){
		this.set(IS_VOLATILE,parameter);
	}
	
	public Boolean isVolatile(){
		return this.get(IS_VOLATILE);
	}
	
	public void setJobState( String parameter){
		this.set(JOB_STATE, parameter);
	}
	public String getJobState(){
		return this.get(JOB_STATE);
	}
	
	public void setJobName( String parameter){
		this.set(JOB_NAME, parameter);
	}
	public String getJobName(){
		return this.get(JOB_NAME);
	}
	
	public void setJobClassName(String parameter){
		this.set(JOB_CLASS_NAME, parameter);
	}
	public String getJobClassName(){
		return this.get(JOB_CLASS_NAME);
	}
	
	public void setJobGroup(String parameter){
		this.set(JOB_GROUP, parameter);
	}
	public String getJobGroup(){
		return this.get(JOB_GROUP);
	}
	
	public void setDescription(String parameter){
		this.set(DESCRIPTION, parameter);
	}
	public String getDescripton(){
		return this.get(DESCRIPTION);
	}
	
	public void setCronExpression(String parameter){
		this.set(CRON_EXPRESSION, parameter);
	}
	public String getCronExpression(){
		return this.get(CRON_EXPRESSION);
	}
	
	public void setJobParameters(ArrayList<JobParameterClient> parameter){
		this.set(JOB_PARAMETERS, parameter);
	}
	public ArrayList<JobParameterClient> getJobParameters(){
		return this.get(JOB_PARAMETERS);
	}
	
	public void setEndDate(Date parameter){
		this.set(END_DATE, parameter);
	}
	public Date getEndDate(){
		return this.get(END_DATE);
	}
	public void setStartDate(Date parameter){
		this.set(START_DATE, parameter);
	}
	public Date getStartDate(){
		return this.get(START_DATE);
	}
}
