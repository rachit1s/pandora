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
import org.quartz.Calendar;

import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;

public class XMLtoDatabase {
	private Properties properties;
	private Scheduler myScheduler;
	private Scheduler dbScheduler;
	
		
	public boolean loadProperties(File myFile) {
		
		properties = new Properties();
		try {
		FileInputStream fis = new FileInputStream(myFile);
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
	
	public boolean getJobs(File oldConfigFile,File newConfigFile) throws InterruptedException {
			ArrayList<JobDetail> jobs = new ArrayList<JobDetail>();
		if(loadProperties(oldConfigFile) == false) {
			System.out.println("Properties Could Not be Loaded");
			return false;
		}

				StdSchedulerFactory factory1 =  new StdSchedulerFactory();
				
				try {
					factory1.initialize(properties);
					myScheduler = factory1.getScheduler();
//					myScheduler.start();
//					myScheduler.pauseAll();
					
				} catch (SchedulerException e) {
					System.out.println("Scheduler for XML file Could not be initialized");
					e.printStackTrace();
					return false;
				}
				
				//dbSchs.start();
			
			
			
	//Add all the job details to Array List jobs
				Trigger[] triggers;
				JobDetail tempJob;
				Hashtable<JobDetail,Trigger[]> jobWithTriggers = new Hashtable<JobDetail,Trigger[]>();
			
			try {
				String[] jobGroups = myScheduler.getJobGroupNames();
				System.out.println(jobGroups.length);
					if(jobGroups!= null) {
						for(int i=0; i<jobGroups.length; i++) {
							String[] jobNames = myScheduler.getJobNames(jobGroups[i]);
								if(jobNames != null) {
									for(int j=0; j<jobNames.length; j++) {
										if(jobNames[j].trim().startsWith("JobInitializationPlugin_") == false) {
											tempJob = myScheduler.getJobDetail(jobNames[j],jobGroups[i]);
												if(tempJob != null) {
													jobs.add(tempJob);
													jobWithTriggers.put(tempJob, myScheduler.getTriggersOfJob(jobNames[j],jobGroups[i]));
												}
											}
										}
									}
							}
						}
				}					//Try Ends
		
			catch(SchedulerException s) {
				System.out.println("Could Not Load the Job Details from Given XML file");
				s.printStackTrace();
				return false;
			}
			
		//Gets the list of all the calendars
				
				
				String[] calendarNames;
				Hashtable<String,Calendar> myCalendars = new Hashtable<String,Calendar>();
				
				try {
					calendarNames = myScheduler.getCalendarNames();
					for(int strCount=0; strCount<calendarNames.length; strCount++) {
						myCalendars.put(calendarNames[strCount],myScheduler.getCalendar(calendarNames[strCount]));
					}
				} catch (SchedulerException e1) {
					System.out.println("Could Not Get The Calendar Properties");
				}
			
				try {
					myScheduler.shutdown();
				} catch (SchedulerException e) {
					System.out.println("Scheduler was not Shutdown");
					e.printStackTrace();
					return false;			
				}															
			
				if(loadProperties(newConfigFile) == false) {
					System.out.println("Properties Could Not be Loaded");
					return false;
				}	
			
				try {
				StdSchedulerFactory factory2 =  new StdSchedulerFactory();
				factory2.initialize(properties);
				dbScheduler = factory2.getScheduler();
				dbScheduler.start();
				} catch (SchedulerException e) {
					System.out.println("Scheduler for JobStoreDb Could not be initialized");
					e.printStackTrace();
					return false;
				}
			
			try {
				for(JobDetail myJob:jobs) {
					myJob.setDurability(true);
				
	
	//##########    First Delete any jobs if present and then store them into Db   #########
					
					boolean isDeleted = dbScheduler.deleteJob(myJob.getName(),myJob.getGroup());
					dbScheduler.addJob(myJob, true);
					System.out.println(myJob.getName()+" was deleted "+isDeleted);
					
	//#################  Stores the Triggers into Db ##############################
										
					triggers = jobWithTriggers.get(myJob);
					for(Trigger myTrigger:triggers) {
						dbScheduler.scheduleJob(myTrigger);
						}
					}
			} catch(SchedulerException e) {
				System.out.println("Could Not Store the Job Details in Database");
				e.printStackTrace();
				return false;
			}
	//###############	Stores the Calendars into Db ###############################	
					
				try {	
					for(Enumeration<String> tmpCalNames=myCalendars.keys(); tmpCalNames.hasMoreElements(); ) {
							String tmpName = tmpCalNames.nextElement();
							dbScheduler.addCalendar(tmpName,myCalendars.get(tmpName),true, true);
					}
				} catch(SchedulerException e) {
					System.out.println("Could Not Store the Calendar Details in database");
					
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

