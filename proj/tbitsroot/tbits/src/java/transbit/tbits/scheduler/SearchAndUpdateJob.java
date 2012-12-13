package transbit.tbits.scheduler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;


public class SearchAndUpdateJob implements ITBitsJob, TBitsConstants {
	
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_COMMON);
	   // Property keys to be read from JobDataMap.
    public static final String CMD_PARAM_BA_REGEX = "BA_REGEX";
    public static final String CMD_PARAM_DQL = "DQL";
    public static final String CMD_PARAM_USER = "USER";
    public static final String CMD_DISPLAY_NAME = "Search and Update Job";
    
   // public static final String CMD_ARG_NAMES = "CmdLineArgumentNameCSV";
    
    
	public  void execute(JobExecutionContext arg0) throws JobExecutionException 
	{
		// TODO Auto-generated method stub
		
		// Get the JobDetail object.
        JobDetail jd = arg0.getJobDetail();

        // Read the properties of the Job from the JobDetail object.
        String     jobName  = jd.getName();
        JobDataMap jdm      = jd.getJobDataMap();
        
        String dql= "request:>0";
        Hashtable<String, String>  updatefieldValues = new Hashtable<String, String>();
        String userLogin = null;
        String basRegex = "";
        
        String keysList[]  = jdm.getKeys();
        
        for(String key : keysList)
        {
        	
        	if(key.equals(CMD_PARAM_BA_REGEX))
        	{
        		basRegex = jdm.getString(key); 
        	}
        	else if(key.equals(CMD_PARAM_DQL))
        	{
        		dql = jdm.getString(key);
        	}
        	else if(key.equals(CMD_PARAM_USER))
        	{
        		userLogin = jdm.getString(key).trim();
        	}
        	//assuming all other as key value pairs for updatefieldValues
        	else
        	{
        		updatefieldValues.put(key, jdm.getString(key));
        	}
        }
        if(userLogin == null)
        {
        	LOG.error("User not supplied in the job definition file.");
        	return;
        }
        try {
			searchAndUpdateJob(dql, userLogin,  basRegex, updatefieldValues);
		} catch (DatabaseException e) {
			LOG.error("Database exception occurred. " + e.getMessage(), e);
		}
    }
	
	public void searchAndUpdateJob(String dql, String userLogin, String basRegex, Hashtable<String, String>  updatefieldValues) throws DatabaseException
	{
		if(userLogin == null)
			throw new IllegalArgumentException("User login can not be null");
		
		User user = null;
        ArrayList<BusinessArea> baList = new ArrayList<BusinessArea>();
         
		ArrayList<BusinessArea> activeBAs = BusinessArea.getActiveBusinessAreas();
		for(BusinessArea ba: activeBAs)
		{
			if(ba.getSystemPrefix().matches(basRegex))
			{
				baList.add(ba);
			}
		}
		System.out.println("Active BAs matching the pattern: " + baList);
		
		try {
			user = User.lookupAllByUserLogin(userLogin);
		} catch (DatabaseException e) {
			LOG.error("Invalid user login '" + userLogin + "' or database exception");
		}
		if(user == null)
        {
        	LOG.error("Either invalid user is supplied or not supplied at all. "
        			+"Can not continue Search and Update. Check the quartz job configuration.");
        	return;
        }     
		updatefieldValues.put(Field.USER, Integer.toString(user.getUserId()));
		SearchAndUpdate sch = new  SearchAndUpdate();
        sch.searchAndUpdateRequest(dql, updatefieldValues, user, baList);		
	}
	
	public Hashtable<String, JobParameter> getParameters() throws SQLException{
		Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
		JobParameter param;
		
		param = new JobParameter() ;
		param.setName( CMD_PARAM_BA_REGEX ) ;
		param.setMandatory(true);
		param.setType( ParameterType.Text ) ;
		params.put(CMD_PARAM_BA_REGEX, param);
		
		param = new JobParameter() ;
		param.setName( CMD_PARAM_DQL ) ;		
		param.setType( ParameterType.Text ) ;
		params.put( CMD_PARAM_DQL, param) ;
		
		param = new JobParameter() ;
		param.setName(CMD_PARAM_USER) ;
		param.setMandatory( true ) ;
		param.setType(ParameterType.Text) ;
		params.put(CMD_PARAM_USER, param) ;
     
		return params;
	}
    
    public String getDisplayName(){
    	return CMD_DISPLAY_NAME;
    }
    
    public boolean validateParams(Hashtable<String,String> params) 
    	throws IllegalArgumentException{
    	
    	if( "".equals(params.get(CMD_PARAM_BA_REGEX).trim()) || null == params.get(CMD_PARAM_BA_REGEX ) )
    		throw new IllegalArgumentException( "Illegal Argument for " + CMD_PARAM_BA_REGEX + " field.") ;
    	
    	if( "".equals(params.get(CMD_PARAM_USER).trim()) 
    			|| null == params.get(CMD_PARAM_USER)    			
    	  )
    	    throw new IllegalArgumentException( "Illegal Argument for " + CMD_PARAM_USER + " field.") ;
    	
    	User user = null ;		
		try {
			user = User.lookupAllByUserLogin(params.get(CMD_PARAM_USER));
		} catch (DatabaseException e) {
			throw new IllegalArgumentException( "Database Exception occured! Please try again.") ;
		}
		
    	if( null == user ) 
    		throw new IllegalArgumentException( "The user login entered is not valid" ) ;
    	
    	return true;
    }
    
	public static void main(String arg[]) {
		
		System.out.println("\nEnterd in static main\n");
		Hashtable<String, String>  updatefieldValues = new Hashtable<String, String>();
		
		//updatefieldValues.put(Field.BUSINESS_AREA, "1");
		updatefieldValues.put(Field.DESCRIPTION, "2.testing inside main itself ");
		updatefieldValues.put(Field.USER,"1");
		
		String userLogin = "giris";
		String baRegex = "tbit.*";
		String dql = "has:Logger";
		
		SearchAndUpdateJob suj = new SearchAndUpdateJob();
		try {
			suj.searchAndUpdateJob(dql, userLogin, baRegex, updatefieldValues);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ;
	}
}
