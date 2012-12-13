package transbit.tbits.Helper;

/**
 * Author  @Abhishek Agarwal
 * This class takes Job Details,Trigger details ,Calendars  from a XML file specified in filePath1 and then stores these table
 * into the dataSource specified in filePath2
 * 
 */

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.SchedulerException;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Hashtable;


public class InsertToDb {
	private Properties properties;
	private Scheduler dbScheduler;
	
		
	public boolean loadProperties(String filePath) {
		
		properties = new Properties();
		try {
		FileInputStream fis = new FileInputStream(filePath);
		properties.load(fis);
		}
		catch(FileNotFoundException f){
			f.printStackTrace();
			return false;
		}
		catch(IOException i) {
			i.printStackTrace();
			return false;
		}
	
		return true;
		
	}
	
	public boolean storeInDb(String filePath) throws InterruptedException {
			ArrayList<JobDetail> jobs = new ArrayList<JobDetail>();
		if(loadProperties(filePath) == false) {
			System.out.println("Properties Could Not be Loaded");
			return false;
		}

				StdSchedulerFactory factory =  new StdSchedulerFactory();
				
				try {
					factory.initialize(properties);
					dbScheduler = factory.getScheduler();
//					dbScheduler.start();
//					dbScheduler.pauseAll();
					
				} catch (SchedulerException e) {
					System.out.println("Scheduler for DataSource Could not be initialized");
					e.printStackTrace();
					return false;
				}
				
			
			
			
				Trigger[] triggers;
				JobDetail tempJob;
				Hashtable<JobDetail,Trigger[]> jobWithTriggers = new Hashtable<JobDetail,Trigger[]>();
			
			try {
				String[] jobGroups = dbScheduler.getJobGroupNames();
				System.out.println(jobGroups.length);
					if(jobGroups!= null) {
						for(int i=0; i<jobGroups.length; i++) {
							String[] jobNames = dbScheduler.getJobNames(jobGroups[i]);
								if(jobNames != null) {
									for(int j=0; j<jobNames.length; j++) {
										if(jobNames[j].trim().startsWith("JobInitializationPlugin_") == false) {
											tempJob = dbScheduler.getJobDetail(jobNames[j],jobGroups[i]);
												if(tempJob != null) {
													jobs.add(tempJob);
													jobWithTriggers.put(tempJob, dbScheduler.getTriggersOfJob(jobNames[j],jobGroups[i]));
												}
											}
										}
									}
							}
						}
				}					//Try Ends
		
			catch(SchedulerException s) {
				System.out.println("Could Not Load the Job Details from Data Source");
				s.printStackTrace();
				return false;
			}
			
			JobDetail referenceJob = new JobDetail();
			referenceJob= jobs.get(1);
			Trigger referenceTrigger = (jobWithTriggers.get(referenceJob))[0];
			
			try {
				for(int i=1; i<1000; i++) {
					JobDetail myJob = new JobDetail();
					myJob = (JobDetail)referenceJob.clone();
					myJob.setName(myJob.getName()+i);
					myJob.setGroup(myJob.getGroup()+i);
				
	
	//##########    First Delete this if present   #########
					
					boolean isDeleted = dbScheduler.deleteJob(myJob.getName(),myJob.getGroup());
	//				dbScheduler.addJob(myJob, true);
					System.out.println(myJob.getName()+" was deleted "+isDeleted);
					
	//#################  Stores the Trigger and Job into Database ##############################
										
					
		
						Trigger myTrigger = (Trigger)referenceTrigger.clone();
						myTrigger.setName(myTrigger.getName()+i);
						myTrigger.setGroup(myTrigger.getGroup()+i);
						myTrigger.setJobName(myJob.getName());
						myTrigger.setJobGroup(myJob.getGroup());
						dbScheduler.scheduleJob(myJob,myTrigger);
				
					}
			} catch(SchedulerException e) {
				System.out.println("Could Not Store the Job Detail and Trigger in Database ");
				e.printStackTrace();
				return false;
			}

			try {
				dbScheduler.shutdown();
			} catch (SchedulerException e) {
				System.out.println("Scheduler was not Shutdown");
				e.printStackTrace();
				return false;
			}
			
			return true;
	}
}

