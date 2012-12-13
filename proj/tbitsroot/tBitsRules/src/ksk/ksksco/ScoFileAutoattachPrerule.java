package ksksco;

import java.sql.Connection;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Calendar;
import java.util.Date;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

import static ksksco.scoConstants.*;

/* @author:	Manoj
 * @Date  : 6th may 2010
 * Rule   : The SCO Pdf File Generate through Birt engine with required fields 
 * 			and auto attach during request generation/Update. 
 */

public class ScoFileAutoattachPrerule implements IRule {

	private static final String GEN_AGENCY_CHO = "CHO";
	private static final String GEN_AGENCY_KCO = "KCO";
	private static final int REQUESTUSER_ORDERING = 5; 
	
	//LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger("kskSCO");
	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult(true, "", true);
		String currentBa = ba.getSystemPrefix();
		if (currentBa.equalsIgnoreCase(SCO_sysprefix)){
			
			File pdfFile = null;
			String ScoNo = null;
			String currentDate = null;
			Field fieldAttachScoFile = null;
			User assigne = null;
			boolean isGenPdfFile = false;
			String genAgencyCode = null;
			Collection <RequestUser> assList = currentRequest.getAssignees();
			HashSet <RequestUser> assSetList = new HashSet <RequestUser> ();
			Hashtable <String, String> params = new Hashtable<String, String> ();
			String statusVal = currentRequest.getStatusId().getName();
			String reqId = Integer.toString(currentRequest.getRequestId()) ;
			int sysId = ba.getSystemId();
			Field assigneeField ;
			
			for(RequestUser ru : assList)
				assSetList.add(ru);
			assList = new ArrayList<RequestUser> ();
			try {
				isGenPdfFile = ((Boolean)currentRequest.getObject(SCO_GENERATE_SCOPDF)).booleanValue();
				genAgencyCode = ((Type)currentRequest.getObject(SCO_GENERATION_AGENCY)).getDescription();
				assigneeField = Field.lookupBySystemIdAndFieldName(sysId, Field.ASSIGNEE);
			} catch (DatabaseException de) {
				de.printStackTrace();
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("Error occured on getting Ex fields. Error: "+de.getMessage());
				return ruleResult;
			} catch (Exception e) {
				e.printStackTrace();
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("Error occured on getting Ex fields. Error: "+e.getMessage());
				return ruleResult;
			}
			
			// For some Status field, gp rao can only decide and assignee kiran panda.
			if (statusVal.equals(SCO_STATUS_TYPE_ACCEPTED) || statusVal.equals(SCO_STATUS_TYPE_REJECTED)
					|| statusVal.equals(SCO_STATUS_TYPE_SUSPENDED)){
				String userLogin = user.getUserLogin();
				// Check for user
				if (!(userLogin.equalsIgnoreCase(USER_LOGIN_GPRAO))){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Not authorized to set status to accepted, rejected or suspended.");
					return ruleResult;
				}
				// Set assignee KiranPanda
				try {
					assigne = User.lookupByUserLogin(USER_LOGIN_KIRANPANDA);
				} catch (DatabaseException e) {
					e.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error occured while lookup by userlogin. Error: "+e.getMessage());
					return ruleResult;
				} // throws database exception
				// Constructor - RequestUser(int aSystemId, int aRequestId, int aUserId, int aOrdering, boolean aIsPrimary,int aFieldId)
				// Field assigneeField = Field.lookupBySystemIdAndFieldName(sysId, Field.ASSIGNEE);
				RequestUser ru = new RequestUser(sysId, currentRequest.getRequestId(), assigne.getUserId(), REQUESTUSER_ORDERING, false, assigneeField.getFieldId());
				ru.setUser(assigne);
				assSetList.add(ru);
			}
			// if generation agency is SEPCO, set assignee to gprao
			if(genAgencyCode.equalsIgnoreCase(GEN_AGENCY_CHO)){
				try {
					assigne = User.lookupByUserLogin(USER_LOGIN_GPRAO);
				} catch (DatabaseException e) {
					e.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error occured while lookup by userlogin. Error: "+e.getMessage());
					return ruleResult;
				} // throws database exception
				// Constructor - RequestUser(int aSystemId, int aRequestId, int aUserId, int aOrdering, boolean aIsPrimary,int aFieldId)
				RequestUser ru = new RequestUser(sysId, currentRequest.getRequestId(), assigne.getUserId(), REQUESTUSER_ORDERING, false, assigneeField.getFieldId());
				ru.setUser(assigne);
				assSetList.add(ru);
				
			}
			// if Generation Agency is KMPCL, set assignee to xueli
			else if (genAgencyCode.equalsIgnoreCase(GEN_AGENCY_KCO)){
				// Set assignee xueli
				try {
					assigne = User.lookupByUserLogin(USER_LOGIN_XUELI);
				} catch (DatabaseException e) {
					e.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error occured while lookup by userlogin. Error: "+e.getMessage());
					return ruleResult;
				} // throws database exception
				// Constructor - RequestUser(int aSystemId, int aRequestId, int aUserId, int aOrdering, boolean aIsPrimary,int aFieldId)
				RequestUser ru = new RequestUser(sysId, currentRequest.getRequestId(), assigne.getUserId(), REQUESTUSER_ORDERING, false, assigneeField.getFieldId());
				ru.setUser(assigne);
				assSetList.add(ru);
				
			}
			
			for(RequestUser ru : assSetList)
				assList.add(ru);
			currentRequest.setAssignees(assList);
			// If add Request then set due date to T+30
			if(isAddRequest){
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(currentRequest.getLoggedDate().getTime());
				cal.add(Calendar.DAY_OF_MONTH, SCO_DUEDATE_DURATION);
				//Timestamp scoduedate = new Timestamp(cal.getTimeInMillis());
				Date dueDt = new Date();
				dueDt.setTime(cal.getTimeInMillis());
				currentRequest.setDueDate(dueDt);
			}
			currentRequest.setIsPrivate(true); // All request private by default, either add or update.

			// Generate SCO pdf File 
			if (isGenPdfFile) {
				try{
					Date SCODate = ((Date)currentRequest.getObject(SCO_SCO_Date)); //Data Type - date, Returns Object
					if (SCODate == null && isAddRequest)
						currentRequest.setObject(SCO_SCO_Date, currentRequest.getLoggedDate());
					if (SCODate == null && !isAddRequest)
						currentRequest.setObject(SCO_SCO_Date, ((Date)oldRequest.getObject(SCO_SCO_Date)));
				
					String initiatedBy = currentRequest.get(SCO_Intiated_By).trim();
					if(!(initiatedBy.length() > 0) || initiatedBy == null){
						currentRequest.setObject(SCO_Intiated_By, user.getDisplayName());
					}
				
					fieldAttachScoFile = Field.lookupBySystemIdAndFieldName(sysId, SCO_SCO_File);
				
					String originalSchedule = Double.toString((Double)currentRequest.getObject(SCO_Exp_Time_Change)) + " " 
											+ ((Type)currentRequest.getObject(SCO_ORIGINAL_SCHEDULE)).getDisplayName();
					String proposedSchedule = Double.toString((Double)currentRequest.getObject(SCO_Actual_Time_Change)) + " "
											+ ((Type)currentRequest.getObject(SCO_PROPOSED_SCHEDULE)).getDisplayName();
					String originalCurrency = ((Type)currentRequest.getObject(SCO_ORIGINAL_CURRENCY)).getDisplayName()+ " "
											+ Double.toString((Double)currentRequest.getObject(SCO_Exp_Value_Change__USD_));
					String proposedCurrency = ((Type)currentRequest.getObject(SCO_PROPOSED_CURRENCY)).getDisplayName() + " "
											+ Double.toString((Double)currentRequest.getObject(SCO_Actual_Value_Change__USD_));
				
					ScoNo = currentRequest.get(SCO_SCO_Order__);
					currentDate = ((Timestamp) currentRequest.getLoggedDate()).toCustomFormat("yyyy-MM-dd").replace("-", "");
				
					// Put the rptdesign pdf File parameters in the hashtable  
					params.put(SCO_REP_INITIATEDBY,(" "+currentRequest.get(SCO_Intiated_By)));
					params.put(SCO_REP_SCO_NO, ScoNo);
					params.put(SCO_REP_SCODATE, ((Timestamp)currentRequest.getObject(SCO_SCO_Date)).toCustomFormat("yyyy-MM-dd")); // date
					params.put(SCO_REP_DRAWINGNO, currentRequest.get(SCO_Drawings_Affected));
					params.put(SCO_REP_EQUIPMENT, currentRequest.get(SCO_System_Affected));
					params.put(SCO_REP_OSCOPE_DESC, currentRequest.get(SCO_Original_Clause));
					params.put(SCO_REP_PCSCOPE_DESC, currentRequest.get(SCO_Proposed_Clause));
					params.put(SCO_REP_OSCOPE_ADJCONTRCT_VAL, originalCurrency); // conf
					params.put(SCO_REP_PCSCOPE_ADJCONTRCT_VAL, proposedCurrency); // conf
					params.put(SCO_REP_OSCOPE_ADJPROJECT_SCH, originalSchedule); // conf
					params.put(SCO_REP_PCSCOPE_ADJPROJECT_SCH, proposedSchedule); // conf
					params.put(SCO_REP_OTHERIMPLCT, currentRequest.get(SCO_Other_Implications));
					
					// Select a report file to generate based on Generation Agency
					String reportName = "";
					if(genAgencyCode.equalsIgnoreCase(GEN_AGENCY_CHO)){
						reportName = "ksk_sco_sepco_template.rptdesign" ;
					}
					else if (genAgencyCode.equalsIgnoreCase(GEN_AGENCY_KCO)){
						reportName = "ksk_sco_wpcl_template.rptdesign";
					}
				
					HashMap<String,String> reportParamMap = new HashMap<String,String>() ;
					reportParamMap.put(REP_RID, reqId  ) ;			
					String tbits_base_url = WebUtil.getNearestPath("") ; // PropertiesHandler.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE)
					System.out.println( "tbits_base_url : " + tbits_base_url ) ;
					reportParamMap.put(REP_TBITS_BASE_URL_KEY, tbits_base_url );
	
					String format = "pdf";	
					
					LOG.info("Sending parameters to generate rptdesign SCO pdf File to BIRT Engine.");
					pdfFile = generateReport( reportName, params, reportParamMap, format ) ;
					
				}
				catch(DatabaseException de){
					de.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error occured while retrieving Ex-fields. Error: " +de.getMessage());
					return ruleResult;
				}
				catch(IllegalStateException ie){
					ie.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error occured while retrieving Ex-fields. Error: " +ie.getMessage());
					return ruleResult;
				} catch (TBitsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if( pdfFile == null ){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Cannot Generate the SCO File.");
					return ruleResult;
				}
				else {
					LOG.info(" About to attach generated SCO pdf File.");
					String displayName = currentDate+"_"+ScoNo+".pdf";
					int requestId = currentRequest.getRequestId() ;
					int actionId = currentRequest.getMaxActionId() ;
					Uploader up = new Uploader( requestId, actionId, currentBa ) ;
					
					AttachmentInfo atinfo = up.moveIntoRepository(pdfFile) ;
					atinfo.name=displayName;
					
					// RequestEx requestEx = extendedFields.get(fieldAttachScoFile) ;
					
					ArrayList<AttachmentInfo> attachArray = new ArrayList<AttachmentInfo>() ;// arrayList Collection
					attachArray.add(atinfo) ;
					currentRequest.setObject(fieldAttachScoFile, attachArray);
					
					// String newJson = AttachmentInfo.toJson(attachArray) ;
					// requestEx.setTextValue(newJson ) ;
					
					LOG.info(" Attached the generated SCO pdf File.");
					ruleResult.setMessage("SCO File Attached Successfull.");
					
				}
			}
			
			return ruleResult;
		}
		else {
			ruleResult.setMessage("Skipping ScoFileAutoattachPrerule as current BA is: "+currentBa);
			return ruleResult;
		}
		
	}

	@Override
	public String getName() {
		return "ScoFileAutoattachPrerule";
	}

	@Override
	public double getSequence() {
		return 4.5;
	}

}