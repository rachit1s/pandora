package lntdcr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

public class WeightageComputation4APPDCL_LNT implements IRule {
	
	private static final String CAND_I_DRAWINGS_DOCUMENTS_CNI = "CandI_Drawings_Documents_CNI";
	private static final String CRS 					= "CRS";
	private static final String SUBMISSION_FILE_TYPE 	= "SubmissionFileType";
	private static final String OWNER_WEIGHTAGE 		= "OwnerWeightage";
	private static final String O_FACTOR 				= "o_Factor";
	private static final String OWNER_ACTUAL_COMPLETE 	= "OwnerActualComplete";
	private static final String V_FACTOR 				= "v_Factor";
	private static final String WEIGHTAGE 				= "Weightage";
	private static final String APPROVAL 				= "Approval";
	private static final String INFORMATION 			= "Information";
	private static final String ACTUAL_COMPLETE 		= "ActualComplete";
	private static final String DCD 					= "Design_Criteria_Document_DCD";
	private static final String CALC					= "Calculation_CALC";
	private static final String ELECTRICAL				= "Electrical_Drawings_Documents_ELEC";
	private static final String CABLE_SCH				= "Cable_Schedule_CSH";
	private static final String PID						= "PandID_PID";
	private static final String PIPING_ISOMETRICS		= "Isometrics_ISO";
	private static final String PIPING_STRESS_ANALYSIS	= "Pipe_Stress_Analysis_PandID_PIDPST";
	private static final String TURBINE_BUILDING_LAYOUT	= "Layout_Drawing_LYD";
	private static final String PCD						= "Piping_Composite_drawing_PCD";
	private static final String SD						= "System_Description_SD";
	private static final String LIST					= "List_LIST";
	private static final String MISCELLANEOUS			= "Others";
	private static final String VEN_DRG_SUB				= "Drawing_DWG";
	private static final String SUPPORT_DRG				= "Support_Drawing_SUPT";
	private static String REVISION						= "Revision";
	
	private static final String DOCUMENT_TYPE 			= Field.REQUEST_TYPE;
	private static final String O_DOCUMENT_CATEGORY		= "o_DocumentCategory";
	private static final String V_DOCUMENT_CATEGORY 	= "DocumentCategory";

	private static final String FLOW_STATUS_WITH_DESEIN = Field.STATUS;
	private static final String DEC_FROM_DESEIN			= Field.SEVERITY;
	
	private static final String FLOW_STATUS_WITH_VENDOR = "FlowStatusWithVendor";
	private static final String DEC_TO_VENDOR			= "DecisionToVendor";
	
	private static final String TYPE_PENDING_SUBMISSION	= "PendingReceipt";
	private static final String TYPE_SUBMITTED     		= "UnderReview";
	private static final String TYPE_RETURNED_WITH_DECISION		= "ReturnedWithDecision";
	private static final String TYPE_RFC						= "ReleaseForConstruction";
	private static final String TYPE_AS_BUILT					= "AsBuilt";
	
	private static final String TYPE_APPROVED					= "Approved";
	private static final String TYPE_APP_WITH_COMMENTS			= "ApprovedWithComments";
	private static final String TYPE_APP_RESUB_REQD				= "ApprovedWithCommentsResubmissionRequired";
	
	private static final String FIELD_UPDATE_MANUALLY			= "UpdateProgressManually";
	
	private static final ArrayList<String> pSeriesRev = new ArrayList<String>();
	static{		
		pSeriesRev.add("P0");
		pSeriesRev.add("P1");
		pSeriesRev.add("P2");
		pSeriesRev.add("P3");
		pSeriesRev.add("P4");
		pSeriesRev.add("P5");
	}
		
	private static final String APL_LTP = "APL_LTP";
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int source, User user,
			boolean isAddRequest) {
		
		if(!ba.getSystemPrefix().equals(APL_LTP) || isAddRequest)
			return new RuleResult();
		
		String submissionFileType = currentRequest.get(SUBMISSION_FILE_TYPE);
		if ((submissionFileType != null) && (submissionFileType.trim().equals(CRS)))
			return new RuleResult();

		getFactor(connection, ba, oldRequest, currentRequest, source, user, isAddRequest);		
		return new RuleResult();
	}

	private void getFactor(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest)
	{
		double factor = 0.0;
		String docType = currentRequest.get(DOCUMENT_TYPE);
		String updStr = currentRequest.get(FIELD_UPDATE_MANUALLY);
		
		double upd = 0.0;
		if ((updStr != null) && (!updStr.trim().equals("")))
			upd = Double.parseDouble(updStr);
				
		String curFlowStatusWithDesein = currentRequest.get(FLOW_STATUS_WITH_DESEIN);
		if (!curFlowStatusWithDesein.equals(oldRequest.get(FLOW_STATUS_WITH_DESEIN)))
		{
			if (currentRequest.get(O_DOCUMENT_CATEGORY).equals(APPROVAL))
			{				
				//DCD, CALC, PID, TBL
				if (docType.equals(DCD) || docType.equals(CALC))
				{
					factor = getFactorApprovalWithoutBreakUp (oldRequest, currentRequest, 
									FLOW_STATUS_WITH_DESEIN, DEC_FROM_DESEIN);					
				}					
				else if (docType.equals(PID) || docType.equals(TURBINE_BUILDING_LAYOUT))
				{
					factor = getFactorApprovalWithBreakUp(oldRequest, currentRequest, 
									FLOW_STATUS_WITH_DESEIN, DEC_FROM_DESEIN);
				}
				else if (docType.equals(ELECTRICAL) || docType.equals(CABLE_SCH)
							|| docType.equals(CAND_I_DRAWINGS_DOCUMENTS_CNI)){
					//85, 15
					factor = getFactorForApproval(oldRequest, currentRequest,
							curFlowStatusWithDesein, DEC_FROM_DESEIN);
				}				
			}
			else if (currentRequest.get(O_DOCUMENT_CATEGORY).equals(INFORMATION))
			{
				if (docType.equals(PID) || 
						docType.equals(PIPING_ISOMETRICS)
						|| docType.equals(PIPING_STRESS_ANALYSIS) 
						|| docType.equals(SUPPORT_DRG)
						|| docType.equals(CABLE_SCH)
						|| docType.equals(ELECTRICAL)
						|| docType.equals(CAND_I_DRAWINGS_DOCUMENTS_CNI)){
					factor = getFactorForInformation(oldRequest, currentRequest, 
							curFlowStatusWithDesein);
				}				
			}
			
			//OwnerActualComplete	With APPDCL Actual % Complete
			//OwnerWeightage	With APPDCL Weightage
			String existingFactorStr = currentRequest.get(O_FACTOR);
			double existingFactor = 0.0;
			if ((existingFactorStr != null) && (existingFactorStr.trim().length() != 0))
				existingFactor = Double.parseDouble(existingFactorStr);
						
			String oWtg = currentRequest.get(OWNER_WEIGHTAGE);
			double wtg = 0.0;
			if ((oWtg != null) && (!oWtg.trim().equals("")))
				wtg = Double.parseDouble(oWtg);
			
			if (upd > 0.0)
				currentRequest.setObject(OWNER_ACTUAL_COMPLETE, ((upd * wtg)/100.0));
			else if ((factor > 0) && (factor > existingFactor))
			{
				currentRequest.setObject(O_FACTOR, factor);
				currentRequest.setObject(OWNER_ACTUAL_COMPLETE, ((factor * wtg)/100.0));
			}
			
		}
		
		if (!oldRequest.get(FLOW_STATUS_WITH_VENDOR).equals(currentRequest.get(FLOW_STATUS_WITH_VENDOR)))
		{
			String vFlowStatus = currentRequest.get(FLOW_STATUS_WITH_VENDOR);
			String decToVendor = currentRequest.get(DEC_TO_VENDOR);
			if (currentRequest.get(V_DOCUMENT_CATEGORY).equals(APPROVAL))
			{		
				factor = getFactorForVendorApproval(oldRequest, factor, vFlowStatus, decToVendor);
			}
			else if (currentRequest.get(V_DOCUMENT_CATEGORY).equals(INFORMATION))
			{
				factor = getFactorForVendorInformation(oldRequest, factor, vFlowStatus);
			}
			//Weightage	With Vendor Weightage
			//ActualComplete	With Vendor Actual % Complete
			String existingFactorStr = currentRequest.get(V_FACTOR);
			double existingFactor = 0.0;
			if ((existingFactorStr != null) && (existingFactorStr.trim().length() != 0))
				existingFactor = Double.parseDouble(existingFactorStr);	
						
			String vWtg = currentRequest.get(WEIGHTAGE);
			double wtg = 0.0;
			if ((vWtg != null) && (!vWtg.trim().equals("")))
				wtg = Double.parseDouble(vWtg);
			
			if (upd > 0.0)
				currentRequest.setObject(ACTUAL_COMPLETE, ((upd * wtg)/100.0));
			else if ((factor > 0) && (factor > existingFactor))
			{ 
				currentRequest.setObject(V_FACTOR, factor);				
				currentRequest.setObject(ACTUAL_COMPLETE, ((factor * wtg)/100.0));
			}
		}
	}

	private double getFactorForVendorApproval(Request oldRequest, double factor,
			String vFlowStatus, String decToVendor) {
		//First submission /Approval /As Built
		if (vFlowStatus.equals(TYPE_AS_BUILT))
			factor = 100.0;				
		else if (decToVendor.equals(TYPE_APPROVED) || decToVendor.equals(TYPE_APP_RESUB_REQD)
					|| decToVendor.equals(TYPE_APP_WITH_COMMENTS))
			factor = 97.5;				
		else if (oldRequest.get(FLOW_STATUS_WITH_VENDOR).equals(TYPE_PENDING_SUBMISSION) 
					&& vFlowStatus.equals(TYPE_SUBMITTED))
			factor = 85.0;
		return factor;
	}

	private double getFactorForVendorInformation(Request oldRequest, double factor,
			String vFlowStatus) {
		//First Submission /Second Submission /As Built
		if (vFlowStatus.equals(TYPE_AS_BUILT))
			factor = 100.0;				
		else if (oldRequest.get(FLOW_STATUS_WITH_VENDOR).equals(TYPE_RETURNED_WITH_DECISION) 
					&& vFlowStatus.equals(TYPE_SUBMITTED))
			factor = 97.5;				
		else if (oldRequest.get(FLOW_STATUS_WITH_VENDOR).equals(TYPE_PENDING_SUBMISSION) 
					&& vFlowStatus.equals(TYPE_SUBMITTED))
			factor = 85.0;
		return factor;
	}

	private static double getFactorForApproval(Request oldRequest, Request currentRequest,
			String curFlowStatusWithDesein, String decFieldname) {
		double factor = 0.0;
		String decFromDesein = currentRequest.get(decFieldname);
		if (curFlowStatusWithDesein.equals(TYPE_AS_BUILT))
			factor = 100.0;				
		else if (decFromDesein.equals(TYPE_APPROVED)
					|| decFromDesein.equals(TYPE_APP_WITH_COMMENTS)
					|| decFromDesein.equals(TYPE_APP_RESUB_REQD))
			factor = 97.5;					
		else if (oldRequest.get(FLOW_STATUS_WITH_DESEIN).equals(TYPE_PENDING_SUBMISSION)
					&& curFlowStatusWithDesein.equals(TYPE_SUBMITTED)){
			factor = 85.0;
		}
		return factor;
	}

	public static double getFactorForInformation(Request oldRequest, Request currentRequest, 
			String curFlowStatusWithDesein){
		double factor = 0.0;
//		String curFlowStatusWithDesein = currentRequest.get(flowStatusFieldName);
		if (curFlowStatusWithDesein.equals(TYPE_AS_BUILT))
			factor = 100.0;				
		else if (curFlowStatusWithDesein.equals(TYPE_SUBMITTED) 
					&& (oldRequest.get(FLOW_STATUS_WITH_DESEIN).equals(TYPE_RETURNED_WITH_DECISION)))
			factor = 97.5;					
		else if (oldRequest.get(FLOW_STATUS_WITH_DESEIN).equals(TYPE_PENDING_SUBMISSION)
					&& curFlowStatusWithDesein.equals(TYPE_SUBMITTED)){
			factor = 85.0;
		}
		return factor;
	}
	
	public static double getFactorApprovalWithoutBreakUp (Request oldRequest, Request currentRequest, 
													String flowStatusFieldName, String decFieldname){
		
		double factor = 0.0;		
		String curFlowStatusWithDesein = currentRequest.get(flowStatusFieldName);
		String decFromDesein = currentRequest.get(decFieldname);
		if (decFromDesein.equals(TYPE_APPROVED)
				|| decFromDesein.equals(TYPE_APP_WITH_COMMENTS)
				|| decFromDesein.equals(TYPE_APP_RESUB_REQD))
			factor = 100.0;
		
		else if (curFlowStatusWithDesein.equals(TYPE_SUBMITTED) 
					&& (oldRequest.get(FLOW_STATUS_WITH_DESEIN).equals(TYPE_RETURNED_WITH_DECISION)))
			factor = 95.0;
		
		else if (oldRequest.get(FLOW_STATUS_WITH_DESEIN).equals(TYPE_PENDING_SUBMISSION)
					&& curFlowStatusWithDesein.equals(TYPE_SUBMITTED))
			factor = 80.0;
		return factor;
	}
	
	public static double getFactorApprovalWithBreakUp (Request oldRequest, Request currentRequest, 
							String flowStatusFieldName, String decFieldname)
	{
		double factor = 0.0;		
		String curFlowStatusWithDesein = currentRequest.get(flowStatusFieldName);
		String decFromDesein = currentRequest.get(decFieldname);
		if (curFlowStatusWithDesein.equals(TYPE_AS_BUILT))
			factor = 100.0;					
		else if (decFromDesein.equals(TYPE_APPROVED)
				|| decFromDesein.equals(TYPE_APP_WITH_COMMENTS)
				|| decFromDesein.equals(TYPE_APP_RESUB_REQD))
			factor = 97.5;
		
		else if (curFlowStatusWithDesein.equals(TYPE_SUBMITTED) 
				&& (oldRequest.get(FLOW_STATUS_WITH_DESEIN).equals(TYPE_RETURNED_WITH_DECISION)))
			factor = 95.0;
		
		else if (oldRequest.get(FLOW_STATUS_WITH_DESEIN).equals(TYPE_PENDING_SUBMISSION)
				&& curFlowStatusWithDesein.equals(TYPE_SUBMITTED)){
			factor = 80.0;
		}
		return factor;
	}
		
	public String getName() {
		return this.getClass().getSimpleName() + ": APPDCL_LNT weightage rule.";
	}

	
	public double getSequence() {
		return 0;
	}
	
	public static void main(String[] args) throws TBitsException, APIException
	{
		updateRequest();		
	}
	
	private static void addRequest() throws APIException, TBitsException {
		AddRequest addRequest = new AddRequest();
		addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
		Hashtable<String, String> aParamTable = new Hashtable<String, String>();
		aParamTable.put(Field.USER, "root");
		aParamTable.put(Field.BUSINESS_AREA, "APL_LTP");
		addRequest.addRequest(aParamTable);
	}

	private static void updateRequest() throws APIException, TBitsException {
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
		Hashtable<String, String> aParamTable = new Hashtable<String, String>();
		aParamTable.put(Field.REQUEST, "1");
		aParamTable.put(Field.USER, "root");
		aParamTable.put(Field.BUSINESS_AREA, "APL_LTP");
		updateRequest.updateRequest(aParamTable);
	}
}
/*
Description	Status													1st Submission	2nd Submission	Approval	As - built		
							
Deseign criteria Document										A	80.00%	15.00%	5.00%	0.00%		
							
Calculations													A	80.00%	15.00%	5.00%	0.00%		
							
P&ID															A	70.00%	15.00%	12.50%	2.50%		Note - 1
																I	85.00%	15.00%						Note - 1
							
Datasheet / Procurement											A	0.00%	0.00%	0.00%	0.00%		
							
Turbine building Layout - GA									A	70.00%	15.00%	15.00%				Note - 1
							
System Description												I	0.00%	0.00%		0.00%		
							
Piping Composite Drawing										I	85.00%	12.50%		2.50%			Note - 1
							
Pipe stress analysis											I	85.00%	15.00%		0.00%		
							
Piping Isometrics												I	85.00%	12.50%		2.50%			Note - 1
							
Pipe support Drawing											I	85.00%	12.50%		2.50%			Note - 1
							
Electrical Schematics, Feeder, Cable Sch. & Termination etc.	I	85.00%	15.00%						Note - 1
																A	85.00%	15.00%						Note - 1
							
C&I Control Schemes,Install Dwgs,Cable Sch. & Termination etc.	I	85.00%	15.00%						Note - 1
																A	85.00%	15.00%						Note - 1
							
List															I	85.00%	15.00%				
							
Miscellaneous														0.00%	0.00%		0.00%		
							
Vendor Drawings Submission										I	85.00%	15.00%						Note - 1
																A	85.00%	15.00%						Note - 1


*/