package transbit.tbits.scheduler;

import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

import java.sql.SQLException;
import java.util.Hashtable;

import org.jfree.util.Log;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;

/*
 * Calls a  job defined in plugin. The name of the class has to be mentioned in the arg: "pluginjobclass"
 */
public class PluginJobCaller implements ITBitsJob {
	
	private static final String PLUGIN_JOB_CLASS_KEY = "pluginjobclass";
	 // Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		JobDataMap jdm = arg0.getJobDetail().getJobDataMap();
		String className = jdm.getString(PLUGIN_JOB_CLASS_KEY);
		if ((className != null) && (className.length() != 0)) {
			Class c = PluginManager.getInstance().findPluginsByClassName(className);
			if (c != null) {
				try {
					Job job = (Job) c.newInstance();
					job.execute(arg0);
				} catch (InstantiationException e) {
					LOG.error("Unable to instantiate the class '" + className
							+ "'. Make sure it is available.");
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					LOG.error("Can not access the class '"
									+ className
									+ "'. Make sure you are allowed to create the instance of it.");
				}catch (ClassCastException e) {
					LOG.error("Class '" + className + "' is not an istance of " 
							+ Job.class.getName() + ". The class should implement the interface ' " 
							+ Job.class.getName());
				}
			} else
				LOG.error("Unable to find the class '" + className + "'");
		} else {
			LOG.error("Class name is missing. You must specify the class name using key '"
							+ PLUGIN_JOB_CLASS_KEY + "'");
		}
	}

	public String getDisplayName() {
		return "PluginJobCaller";
	}

	public Hashtable<String, JobParameter> getParameters() throws SQLException 
	{
		Hashtable<String,JobParameter> params = new Hashtable<String,JobParameter>() ;
		JobParameter jp = new JobParameter();
		jp.setType(ParameterType.Text);
		jp.setName(PLUGIN_JOB_CLASS_KEY);
		jp.setMandatory(true);
		
		params.put(PLUGIN_JOB_CLASS_KEY, jp);
		return params;
	}
	public boolean validateParams(Hashtable<String, String> params)
			throws IllegalArgumentException 
	{
		return true;
	}
}
