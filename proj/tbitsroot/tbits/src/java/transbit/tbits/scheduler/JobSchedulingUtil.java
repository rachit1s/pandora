package transbit.tbits.scheduler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import transbit.tbits.exception.TBitsException;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobClass;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;
import transbit.tbits.scheduler.ui.PreDefinedJobFactory;

public class JobSchedulingUtil{

	public static Scheduler myScheduler = TBitsScheduler.getScheduler();
	
	public static ArrayList<JobDetail> getJobs() throws SchedulerException{
		JobDetail tempJob;
		ArrayList<JobDetail> result = new ArrayList<JobDetail>();
		String[] jobGroups = myScheduler.getJobGroupNames();
		if(jobGroups!= null) {
			for(int i=0; i<jobGroups.length; i++) {
				String[] jobNames = myScheduler.getJobNames(jobGroups[i]);
					if(jobNames != null) {
						for(int j=0; j<jobNames.length; j++) {
							tempJob = myScheduler.getJobDetail(jobNames[j],jobGroups[i]);
								if(tempJob != null)
									result.add(tempJob);
							}
						}
				}
		}
		return result;
	}
	
	
	public static Set<JobClass> getJobClasses(){
		return PreDefinedJobFactory.getInstance().getPreDefinedJobsNew().keySet();
	}
	
	
	public static ArrayList<JobParameter> getJobParams(String jobClassStr){
		HashMap<JobClass, ITBitsJob> jobs = PreDefinedJobFactory.getInstance().getPreDefinedJobsNew();
		ITBitsJob job = null;
		for(JobClass jc : jobs.keySet()){
			if(jc.getClassName().equals(jobClassStr)){
				job = jobs.get(jc);
				break;
			}
		}
		if(job != null){
			ArrayList<JobParameter> result = new ArrayList<JobParameter>();
			Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
			try{
				params = job.getParameters();
				for(JobParameter jp : params.values()){
					if(jp.getValues() != null){
						System.out.println("---------------------------------------Not Null---------------------------------");
					}
					if(jp.getType() == ParameterType.Select){
						System.out.println("---------------------------------------Select---------------------------------");
					}
					result.add(jp);
				}
				return result;
			}catch(SQLException se)
			{
				se.printStackTrace() ;
			}
		}
		return null;
	}
	
	public static void executeJob(String jobName, String jobGroup) throws TBitsException {
			System.out.println("Job name : " + jobName + ", Job Group : " + jobGroup + " is being executed");
			try {
				myScheduler.triggerJob(jobName, jobGroup);
			} catch (SchedulerException e) {
				throw new TBitsException("Unable to execute the Job" +
						jobName + ":" + jobGroup,e);
			}
	}
	
	public static void deleteJob(String jobName, String jobGroup) throws TBitsException {
		System.out.println("Job name : " + jobName + ", Job Group : " + jobGroup + " is being deleted");
		try {
			myScheduler.deleteJob(jobName, jobGroup);
		} catch (SchedulerException e) {
			throw new TBitsException("Unable to delete the Job" +
					jobName + ":" + jobGroup,e);
		}
}
	public static String getCronExpression(String jobName , String jobGroup){
		Trigger[] trigger;
		try {
			trigger = myScheduler.getTriggersOfJob(jobName,jobGroup);
			String cronExpression = "";
			if(trigger.length > 0)
			{
				CronTrigger myTrigger = (CronTrigger) trigger[0];
				cronExpression = myTrigger.getCronExpression();
			}
			return cronExpression;
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Date[] getJobDates(String jobName , String jobGroup){
		Trigger[] trigger;
		try {
			trigger = myScheduler.getTriggersOfJob(jobName,jobGroup);
			Date[] dates = new Date[2];
			if(trigger.length > 0)
			{
				dates[0] = trigger[0].getStartTime();
				dates[1] = trigger[0].getEndTime();
			}
			return dates;
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return null;
	}
}
