/**
 * 
 */
package transbit.tbits.scheduler;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * @author Lokesh
 *
 */
public class ReportsScheduler extends HttpServlet {
	
	private static final String _CRONEXP = "_cronexp";
	private static final String _JOBCLASS = "_jobclass";
	private static final String _JOBDESC = "_jobdesc";
	private static final String _JOBNAME = "_jobname";
	private static final String _JOBGROUP = "_jobgroup";
	private static final String _SHOULD_EXIT = "_shouldexit";
	private static String[] systemParams = { _JOBGROUP, _JOBNAME, _JOBDESC, _JOBCLASS, _CRONEXP, _SHOULD_EXIT };
	private static String[] mandatoryParams = { _JOBGROUP, _JOBNAME, _JOBCLASS, _CRONEXP };
	
	private static Scheduler myScheduler = TBitsScheduler.getScheduler();
	
	public static void addReportSchedule(Map<String, String> paramtable)
	{
		Set<String> paramKeys = paramtable.keySet();
		if((paramKeys != null) && paramKeys.containsAll(Arrays.asList(mandatoryParams)))
		{
			String group = paramtable.get(_JOBGROUP);
			String name = paramtable.get(_JOBNAME);
			
			JobDetail jd = null;
			try {
				jd = myScheduler.getJobDetail(name, group);
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(jd == null)
			{
				String jobclassName = paramtable.get(_JOBCLASS);
				String desc = paramtable.get(_JOBDESC);
				String cronExpress = paramtable.get(_CRONEXP);
				Class jobClass = testExistance(jobclassName);
				String shouldExit = paramtable.get(_SHOULD_EXIT);
				if(jobClass == null)
					return;
				
				jd = new JobDetail();
				jd.setName(name);
				jd.setGroup(group);
				jd.setJobClass(jobClass);
				jd.setDescription(desc); 
				jd.setRequestsRecovery(false);
				jd.setDurability(true);
				jd.setVolatility(false);
				
				//Remove system params
				paramKeys.removeAll(Arrays.asList(systemParams));
				
				//get job data map
				jd.setJobDataMap(new JobDataMap(paramtable));
				
				try {
					myScheduler.addJob(jd, true);
					myScheduler.scheduleJob(new CronTrigger(name, group, name, group, cronExpress));
					System.out.println("Scheduled (group: " + group + ", name: " + name + ")");
				} catch (SchedulerException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if( (shouldExit != null) && shouldExit.trim().equalsIgnoreCase("true") )
				{
					System.exit(0);
				}
			}
			else
			{
				System.out.println("The job (group: " + group + ", name: " + name + ") alreay exists. Not adding.");
			}
		}
		else
		{
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
	
	private static void showHelp() {
		System.out.println("Syntax:CmdAddJob <key>=<value>");
		System.out.println("System Params: " + systemParams);
		System.out.println("Mandaroty Params: " + mandatoryParams);
	}	
	
	
	public static void printReportParameters(){	
		IReportEngine engine = null;
		try{
			EngineConfig config = new EngineConfig( );
			config.setEngineHome( "D:/tbits/src/birt-runtime/ReportEngine");//D:\\tbits-reports\\src\\birt-runtime\\ReportEngine" );
			config.setLogConfig("D:/temp", Level.FINE);
					
			Platform.startup( config );  //If using RE API in Eclipse/RCP application this is not needed.
			IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY );
			engine = factory.createReportEngine( config );
			engine.changeLogLevel( Level.WARNING );
					
		}catch( Exception ex){
			ex.printStackTrace();
		}

		//Open a report design 
		IReportRunnable design = null;
		try {
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$Opening report");
			design = engine.openReportDesign("D:/tbits/dist/build/tbitsreports/taskslogggedbyme.rptdesigntasksloggedbyme.rptdesign");
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
				
		IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask( design );
		Collection params = task.getParameterDefns( true );

		Iterator iter = params.iterator( );
		//Iterate over all parameters
		while ( iter.hasNext( ) )
		{
			IParameterDefnBase param = (IParameterDefnBase) iter.next( );
			//Group section found
			if ( param instanceof IParameterGroupDefn )
			{
				//Get Group Name
				IParameterGroupDefn group = (IParameterGroupDefn) param;
				System.out.println( "Parameter Group: " + group.getName( ) );
				
				//Get the parameters within a group
				Iterator i2 = group.getContents( ).iterator( );
				while ( i2.hasNext( ) )
				{
					IScalarParameterDefn scalar = (IScalarParameterDefn) i2.next( );
					System.out.println("	" + scalar.getName());
				}
				        
			}
			else
			{
				//Parameters are not in a group
				IScalarParameterDefn scalar = (IScalarParameterDefn) param;
				System.out.println("%%%%%%%%%%%ParamName: "+ param.getName());
				
				//Parameter is a List Box
				if(scalar.getControlType() ==  IScalarParameterDefn.LIST_BOX)
				{
				    Collection selectionList = task.getSelectionList( param.getName() );
				    //Selection contains data    
					if ( selectionList != null )
					{						
						for ( Iterator sliter = selectionList.iterator( ); sliter.hasNext( ); )
						{
							//Print out the selection choices
							IParameterSelectionChoice selectionItem = (IParameterSelectionChoice) sliter.next( );
							String value = (String)selectionItem.getValue( );
							String label = selectionItem.getLabel( );
							System.out.println( label + "--" + value);
						}
					}		        
				}   
			}
		}
				
		task.close();
		
		engine.destroy();
		Platform.shutdown();
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ReportsScheduler.printReportParameters();

	}

}
