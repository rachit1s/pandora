package ncchse;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.activity.SemanticException;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.report.TBitsReportEngine;

public class HSEConstants {
public static final String PAR_PREFIX="PAR";
public static final String PAR_AP="Photo";
public static final String PAR_AGE="Age";
public static final String PAR_NAME="Name";
public static final String PAR_SEX="Sex";
public static final String PAR_DESIGNATION="Designation";
public static final String PAR_CONDITIONS="Conditions";
public static final String PAR_CONTRACTOR="category_id";
public static final String PAR_DATE="DateTime";
public static final String PAR_DEPARTMENT="severity_id";
public static final String PAR_DDATE="due_datetime";
public static final String PAR_EQUIPMENT="equipment";
public static final String PAR_INJURY_TYPE="request_type_id";
public static final String PAR_LOCATION="AccidentLocation";
public static final String PAR_BRIEF_DESCRIPTION="description";
public static final String PAR_SAFETY_APPLIANCES="AppliancesUsed";
public static final String PAR_REMIDY="Remidy";
public static final String PAR_WITNESS="Witness";
public static final String PAR_ATTACHMENTS="attachments";
public static final String PAR_TIME="AccidentTime";
public static final String PAR_OTHER_INFO="OtherInfo";
public static final String PAR_LOGGERS="logger_ids";
public static final String PAR_DateTime="DateTime";

public static final String INJURY_TYPE_BURNS="Burns";
public static final String INJURY_TYPE_CURRENT="ElectricCurrent";
public static final String INJURY_TYPE_FRACTURE = "Fracture";
public static final String INJURY_TYPE_MULTIPLE = "MultipleInjuries";
public static final String INJURY_TYPE_SUPERFICIAL = "Superficial";
public static final String INJURY_TYPE_OTHERS = "Others";
public static final String DEPARTMENT_TYPE_CIVIL="Civil";
public static final String DEPARTMENT_TYPE_MECHANICAL="Mechanical";
public static final String DEPARTMENT_TYPE_ELECTRICAL="Electrical";


public static final String PAR_REP_REPORTNO="reportNo";
public static final String PAR_REP_CONTRACTOR="contractor";
public static final String PAR_REP_DATE="date";
public static final String PAR_REP_LOCATION="location";
public static final String PAR_REP_DESCRIPTION="description";
public static final String PAR_REP_NATURE="nature";
public static final String PAR_REP_EQUIPMENT="equipment";
public static final String PAR_REP_CAUSE="cause";
public static final String PAR_REP_SAFTYAPPLIANCES="saftyAppliances";
public static final String PAR_REP_OTHERINFORMATION="otherInformation";
public static final String PAR_REP_WITNESS="witness";
public static final String PAR_REP_PREVENT="prevent";
public static final String PAR_REP_NAME="name";
public static final String PAR_REP_AGE="age";
public static final String PAR_REP_SEX="sex";
public static final String PAR_REP_DESIGNATION="designation";
public static final String PAR_LINKED_REQUEST="related_requests";
public static final String PAR_REP_LOGGED_DATE="loggedDate";
public static final String PAR_REP_LOGGER_NAME="loggerName";
public static final String PAR_REP_FILE_NAME="Ncc_hse_Preliminary_Accident.rptdesign";
public static final String AIR_REP_FILE_NAME="Ncc_hse_Accident_Investigation.rptdesign";

	
public static final String AIR_PREFIX="AIR";
public static final String AIR_AP="Photo";
public static final String AIR_AGE="age";
public static final String AIR_Name="name";
public static final String AIR_SEX="sex";
public static final String AIR_DESIGNATION="designation";
public static final String AIR_CONDITIONS="Conditions";
public static final String AIR_CONTRACTOR="category_id";
public static final String AIR_DATE="DateTime";
public static final String AIR_DEPARTMENT="severity_id";
public static final String AIR_DDATE="due_datetime";
public static final String AIR_EQUIPMENT="Equipment";
public static final String AIR_INJURY_TYPE="request_type_id";
public static final String AIR_LOCATION="AccidentLocation";
public static final String AIR_BRIEF_DESCRIPTION="Briefdescription";
public static final String AIR_SAFETY_APPLIANCES="AppliancesUsed";
public static final String AIR_Remidy="Remidy";
public static final String AIR_WITNESS="Witness";
public static final String AIR_DIRECT_CAUSE="DirectCause";
public static final String AIR_INDIRECT_CAUSE="IndirectCause";
public static final String AIR_WORKING_SINCE="WorkingSince";
public static final String AIR_SITE_ENGINEER="SiteEngineer";
public static final String AIR_SITE_INCHARGE="SiteInCharge";
public static final String AIR_HSE_OFFICER="HSEOfficer";
public static final String AIR_ACCIDENT_CATEGORY="AccidentCategory";
public static final String AIR_LOST_MANHOUR="LostManHours";
public static final String AIR_ANY_OTHER_INFORMATION="description";
public static final String AIR_LINKED_REQUEST="related_requests";
public static final String AIR_TIME="Time";
public static final String AIR_LOGGERS="logger_ids";


public static final String AIR_REP_REPORTNO="reportNo";
public static final String AIR_REP_SITEENGINEER="siteEngineer";
public static final String AIR_REP_HSEOFFICER="hseOfficer";
public static final String AIR_REP_SITEINCHARGE="siteIncharge";
public static final String AIR_REP_SUBCONTRACTOR="subContractor";
public static final String AIR_REP_CATEGORY="category";
public static final String AIR_REP_NAME="name";
public static final String AIR_REP_AGE="age";
public static final String AIR_REP_SEX="sex";
public static final String AIR_REP_DESIGNATION="designation";
public static final String AIR_REP_WORKINGSINCE="workingSince";
public static final String AIR_REP_DATE="date";
public static final String AIR_REP_LOCATION="location";
public static final String AIR_REP_NATURE="nature";
public static final String AIR_REP_EQUIPMENT="equipment";
public static final String AIR_REP_DESCRIPTION="description";
public static final String AIR_REP_DIRECTCAUSE="directCause";
public static final String AIR_REP_INDIRECTCAUSE="indirectCause";
public static final String AIR_REP_PRECAUTIONARY="precautionary";
public static final String AIR_REP_MANHOURLOST="manHourlost";
public static final String AIR_REP_OTHERINFORMATION="otherInformation";
public static final String AIR_REP_LOGGED_DATE="loggedDate";

public static final String REP_RID = "rid";
public static final String REP_TBITS_BASE_URL_KEY = "tbits_base_url";


public static final String PAR_SYS_ID="parSysId";
public static final String AIR_SYS_ID="airSysID";
public static final String PAR_REQ_ID="parReqId";
public static final String AIR_REQ_ID="airReqID";

public static final String HSE_ADD_LINK_NAME = "Generate AIR";
public static final String HSE_UPDATE_LINK_NAME = "Update AIR";

public static final String PREFILL_CATEGORY_ID="category_id";
public static final String PREFILL_REQUEST_TYPE_ID="request_type_id";
public static final String PREFILL_DESCRIPTION="description";
public static final String PREFILL_ACCIDENTLOCATION="AccidentLocation";
public static final String PREFILL_NAME="name";
public static final String PREFILL_AGE="age";
public static final String PREFILL_SEX="sex";
public static final String PREFILL_CONDITIONS="Conditions";
public static final String PREFILL_EQUIPMENT="Equipment";
public static final String PREFILL_OTHERINFO="Otherinfo";
public static final String PREFILL_REMIDY="Remidy";
public static final String PREFILL_SUBJECT="subject";


public static final TBitsLogger LOG = TBitsLogger.getLogger("KSKHSE");



public static int getNextRepNo(Connection con, String repCat ) throws SQLException
{
System.out.println("generating rep. no. for : " + repCat );
	try {	
		CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
		stmt.setString(1, repCat );
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			int id = rs.getInt("max_id");
			return id;
		} else {
			throw new SQLException();
		}
	} catch (SQLException e) {
		throw e;
	}		
}

public static File generateReport( String reportName,Hashtable<String, String> params, HashMap<String,String> reportParams, String format )
{
	IReportDocument ird = null ;
	TBitsReportEngine tre = null ;
	try
	{
			tre = new TBitsReportEngine();
			if(tre == null)
			{
				LOG.error("Unable to get the instance of ReportEngine.");
				return null ;
			}
			IReportRunnable reportDesign;
			reportDesign = tre.getReportDesign(reportName);
			if(reportDesign == null)
			{
				LOG.error("Unable to get the design instance of " + reportName);
				return null ;
			}
			
			IReportEngine ire = tre.getEngine() ;
			EngineConfig ec = ire.getConfig() ;
			
			// set all non-report parameters
			for( Enumeration<String> keys = params.keys() ; keys.hasMoreElements() ; )
			{			
				String key = keys.nextElement() ;
				String value = params.get(key) ;
				ec.getAppContext().put(key,value) ;
			}
			
			
			ird = tre.getReportDocument(reportDesign, reportParams) ;
			
			File outFile = null ;
			if( format.trim().equalsIgnoreCase("pdf"))
			 outFile = tre.getPDFReport(ird);
			else 
				outFile = tre.getHTMLReport(ird) ; // default
			
			/////// print file info
			if( outFile != null ) 
			{
				System.out.println( "Name:" + outFile.getName() + " path = " + outFile.getAbsolutePath() ) ;
				return outFile ;
			}
			else
			{
				LOG.error("OutPUT file is null" ) ;
				return null ;
			}
//			
//			if(!leaveOutputFile)
//			{
//				if (!outFile.delete())
//					LOG.warn("Can not delete the temporary file: "
//							+ outFile.getAbsolutePath());
//			}
					
	} catch (EngineException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	} catch (SemanticException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	}		
	finally
	{
		if (tre != null)
			tre.destroy();
	}
	
}



}
