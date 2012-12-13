package transbit.tbits.scheduler.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import net.sf.json.JSONArray;
import transbit.tbits.mail.RecieveMail;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.report.PerUserReportEmailer;
import transbit.tbits.scheduler.BirtReportMailer;
import transbit.tbits.scheduler.CSVImportJob;
import transbit.tbits.scheduler.GarbageCollectorJob;
import transbit.tbits.scheduler.MaintenanceJob;
import transbit.tbits.scheduler.PluginJobCaller;
import transbit.tbits.scheduler.PreventiveAlerts;
import transbit.tbits.scheduler.SCurveJob;
import transbit.tbits.scheduler.ScheduleRequest;
import transbit.tbits.scheduler.SearchAndUpdateJob;
import transbit.tbits.scheduler.SeverityBasedEscalation;
import transbit.tbits.scheduler.sap.request.PushDataToFileJob;
import transbit.tbits.scheduler.sap.request.PushRequestToSAPJob;

public class PreDefinedJobFactory {
	private static Hashtable<JSONArray, ITBitsJob> jobs;
	private static HashMap<JobClass, ITBitsJob> predefinedJobs;
	private static PreDefinedJobFactory pFactory = null;
	public synchronized static PreDefinedJobFactory getInstance(){
		if(pFactory == null)
			pFactory = new PreDefinedJobFactory();
		return pFactory;
	}
	
	private PreDefinedJobFactory(){
		jobs = new Hashtable<JSONArray, ITBitsJob>();
		predefinedJobs = new HashMap<JobClass, ITBitsJob>();
		ITBitsJob job;
		JSONArray x;
		JobClass jc;
		
		ArrayList<Class> arr = PluginManager.getInstance().findPluginsByInterface(ITBitsJob.class.getName());
		arr.add(BirtReportMailer.class);
		arr.add(MaintenanceJob.class);
		arr.add(PreventiveAlerts.class);
		arr.add(SearchAndUpdateJob.class);
		arr.add(SeverityBasedEscalation.class);
		arr.add(PerUserReportEmailer.class);
		arr.add(ScheduleRequest.class);
		arr.add(PluginJobCaller.class);
		arr.add(GarbageCollectorJob.class);
		arr.add(RecieveMail.class);
		arr.add(SCurveJob.class);
		arr.add(CSVImportJob.class);
        arr.add(PushRequestToSAPJob.class);
		
		for(Class c:arr){
			try {
				Object o = c.newInstance();
				job = (ITBitsJob)o;
				x = new JSONArray();
				x.add(job.getDisplayName());
				x.add(job.getClass().getName());
				jobs.put(x, job);
				jc = new JobClass();
				jc.setClassName(job.getClass().getName());
				jc.setDisplayName(job.getDisplayName());
				predefinedJobs.put(jc, job);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Hashtable<JSONArray, ITBitsJob> getPreDefinedJobs(){
		return jobs;
	}
	public HashMap<JobClass, ITBitsJob> getPreDefinedJobsNew(){
		return predefinedJobs;
	}
}
