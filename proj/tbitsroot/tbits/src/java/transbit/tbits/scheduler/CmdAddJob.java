package transbit.tbits.scheduler;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import transbit.tbits.domain.Field;

/*
 * Adds a job from command line. For example:
 * java -Dapp.name=tbits -Dtbits.home=/Users/sandeepgiri/tbits/dist CmdAddJob \
 * _cronexp="0 0/1 * * * ?" _jobclass=transbit.tbits.scheduler.ScheduleRequest \
 * _jobdesc="first test job" _jobname=testjob1 _jobgroup=testjobgroup _shouldexit=true \
 * duedate_lag=5 subject="Test subject" user_id=root sys_id=tbits
 */
public class CmdAddJob {
	private static final String _CRONEXP = "_cronexp";
	private static final String _JOBCLASS = "_jobclass";
	private static final String _JOBDESC = "_jobdesc";
	private static final String _JOBNAME = "_jobname";
	private static final String _JOBGROUP = "_jobgroup";
	private static final String _CANOVERRIDEEXISTINGJOB = "_canoverrideexistingjob";
	private static final String _SHOULD_EXIT = "_shouldexit";
	private static String[] systemParams = { _JOBGROUP, _JOBNAME, _JOBDESC, _JOBCLASS, _CRONEXP, _SHOULD_EXIT, _CANOVERRIDEEXISTINGJOB };
	private static String[] mandatoryParams = { _JOBGROUP, _JOBNAME, _JOBCLASS, _CRONEXP };
	private static Scheduler myScheduler = TBitsScheduler.getScheduler();
	
	public static void addJob(Map<String, String> paramtable)
	{
		Set<String> paramKeys = paramtable.keySet();
		if(paramKeys != null)
		{
			if (paramKeys.containsAll(Arrays.asList(mandatoryParams))) {
				String group = paramtable.get(_JOBGROUP);
				String name = paramtable.get(_JOBNAME);

				JobDetail jd = null;
				try {
					jd = myScheduler.getJobDetail(name, group);
				} catch (SchedulerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				boolean canOverride = false;
				if(jd != null)
				{
					String canOverrideStr = paramtable.get(_CANOVERRIDEEXISTINGJOB);
					if( (canOverrideStr != null) && ( canOverrideStr.trim().equalsIgnoreCase("true")))
					{
						canOverride = true;
					}
					
					if(canOverride)
					{
						try {
							myScheduler.deleteJob(jd.getGroup(), jd.getName());
							System.out.println("Deleted the job: " + jd.getFullName());
							jd = null;
						} catch (SchedulerException e) {
							System.out.println("Could not delete the job.: " + jd.getFullName());
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					}
				}
				if (jd == null) {
					
					String jobclassName = paramtable.get(_JOBCLASS);
					String desc = paramtable.get(_JOBDESC);
					String cronExpress = paramtable.get(_CRONEXP);
					Class jobClass = testExistance(jobclassName);
					String shouldExit = paramtable.get(_SHOULD_EXIT);
					if (jobClass == null)
						return;

					jd = new JobDetail();
					jd.setName(name);
					jd.setGroup(group);
					jd.setJobClass(jobClass);
					jd.setDescription(desc);
					jd.setRequestsRecovery(false);
					jd.setDurability(true);
					jd.setVolatility(false);

					// Remove system params
					paramKeys.removeAll(Arrays.asList(systemParams));

					// get job data map
					jd.setJobDataMap(new JobDataMap(paramtable));

					try {
						myScheduler.addJob(jd, true);
						if(canOverride)
						{
							myScheduler.unscheduleJob(name, group);
						}
						myScheduler.scheduleJob(new CronTrigger(name, group,
								name, group, cronExpress));
						System.out.println("Scheduled (group: " + group
								+ ", name: " + name + ")");
					} catch (SchedulerException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}

					if ((shouldExit != null)
							&& shouldExit.trim().equalsIgnoreCase("true")) {
						System.exit(0);
					}
				} else {
					System.out.println("The job (group: " + group + ", name: "
							+ name + ") alreay exists. Not adding.");
				}
			} else {
				System.out.println("Not all paramters were supplied.");
				
				Set<String> leftParams = new HashSet(Arrays.asList(mandatoryParams));
				leftParams.removeAll(paramKeys);
				System.out.println("The following paramters went missing.: "  + leftParams);
				showHelp();
			}
		}
		else
		{
			System.out.println("No Parameter was passed.");
			showHelp();
			return;
		}
	}
	private static Class testExistance(String jobclass) {
		// TODO Auto-generated method stub
		try {
			Class c = Class.forName(jobclass);
			Class[] interfaces = c.getInterfaces();
			boolean isFound = false;
			return c;
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("WARNING: the job class '" + jobclass + "' doesnt exist. Anyway continuing with scheduling.");
		}
		return null;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, String> paramtable = new Hashtable<String, String>();
		for(String kv:args)
		{
			String k = null;
			String v = null;
			if (!kv.contains("=")){
				System.out.println("Invalid key-value pair: " + kv);
				showHelp();
				return;
			}
			
			String[] avArray = kv.split("=");
			if(avArray.length == 1)
			{
				k = avArray[0];
				if(kv.endsWith("="))
				{
					v = "";
				}
			}
			else if(avArray.length == 2)
			{
				k = avArray[0];
				v = avArray[1];
			}
			else 
			{
				System.out.println("Invalid argument: " + kv);
				showHelp();
				return;
			}
			
			if( (k != null) && (k.trim().length() != 0))
			{
				paramtable.put(k, v);
			}
			else
			{
				System.out.println("Invalid argument: '" + kv + "'. It should be in the form of <key>=<value>. It is being Ignored.");
			}
		}
		addJob(paramtable);
		System.exit(1);
	}
	private static void showHelp() {
		System.out.println("Syntax:CmdAddJob <key>=<value>");
		System.out.println("System Params: " + Arrays.asList(systemParams));
		System.out.println("Mandaroty Params: " + Arrays.asList(mandatoryParams));
	}

}
