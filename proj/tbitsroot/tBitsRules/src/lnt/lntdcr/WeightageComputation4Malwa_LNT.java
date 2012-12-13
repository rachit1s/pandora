/**
 * 
 */
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

/**
 * @author lokesh
 *
 */
public class WeightageComputation4Malwa_LNT implements IRule {

	private static final String DECISION_FROM_MPPGCL 			= Field.SEVERITY;
	private static final String INFORMATION 					= "Information";
	private static final String APPROVAL 						= "Approval";
	private static final String O_FACTOR 						= "o_factor";
	private static final String MALWA_LNT 						= "Malwa_LNT";
	private static final String FIELD_FLOW_STATUS_WITH_OWNER 	= Field.STATUS;
	private static final String FIELD_DECISION_FROM_OWNER		= "DecisionToVendor";
	private static final String FIELD_DOCUMENT_TYPE				= Field.REQUEST_TYPE;
	private static final String FIELD_VENDOR_REVISION			= "VendorRevision";
	private static final String FIELD_DOCUMENT_CATEGORY			= "DocumentCategory";
	
	private static final String TYPE_PENDING_SUBMISSION			= "PendingReceipt";
	private static final String TYPE_SUBMITTED_FOR_COMMENTS     = "UnderReview";
	private static final String TYPE_RETURNED_WITH_DECISION		= "ReturnedWithDecision";
	private static final String TYPE_RFC						= "ReleaseForConstruction";
	private static final String TYPE_AS_BUILT					= "AsBuilt";
	
	private static final String TYPE_APPROVED					= "Approved";
	private static final String TYPE_APP_WITH_COMMENTS			= "ApprovedWithComments";
	private static final String TYPE_APP_RESUB_REQD				= "ApprovedWithCommentsResubmissionRequired";
	
	private static final String TYPE_DCD						= "Design_Criteria_Document_DCD";
	private static final String TYPE_P_ID						= "PandID_PID";
	private static final String TYPE_SLD						= "Single_Line_Diagram_SLD";
	private static final String TYPE_PLANT_LAYOUT_DRAWINGS		= "Layout_Drawing_LYD";
	
	private static final String FIELD_UPM						= "UpdateProgressManually";		
	private static final String FIELD_FLOW_STATUS_WITH_VENDOR	= "FlowStatusWithVendor";
	
	private static final String FIELD_O_WTG						= "oWeightage";
	private static final String FIELD_ACTUAL_COMPLETE			= "oActualComplete";
	
	private static final String CRS 							= "CRS";
	private static final String SUBMISSION_FILE_TYPE 			= "SubmissionFileType";
	
	private static final ArrayList<String> pSeriesRev = new ArrayList<String>();	
	static{		
		pSeriesRev.add("P0");
		pSeriesRev.add("P1");
		pSeriesRev.add("P2");
		pSeriesRev.add("P3");
		pSeriesRev.add("P4");
		pSeriesRev.add("P5");
	}
		
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request,
	 * 											transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult();
		
		if(ba.getSystemPrefix().equals(MALWA_LNT) && (!isAddRequest))
		{
			double factor = 0.0;
			double upmFactor = 0.0;
			
			String submissionFileType = currentRequest.get(SUBMISSION_FILE_TYPE);
			if ((submissionFileType != null) && (submissionFileType.trim().equals(CRS)))
				return new RuleResult();
			
			factor = getFactor(connection, ba, oldRequest, currentRequest, source, user,
						isAddRequest, ruleResult);
						
			double existingFactor = 0.0;
			String existingFactorStr = currentRequest.get(O_FACTOR);			
			if((existingFactorStr != null) && (existingFactorStr.trim().length() != 0))
				existingFactor = Double.parseDouble(existingFactorStr);			
			
			String upmValue = currentRequest.get(FIELD_UPM);
			String oWtgStr = currentRequest.get(FIELD_O_WTG);
			Double oWtg = 0.0;
			if ((oWtgStr != null) && (!oWtgStr.trim().equals("")))
				oWtg = Double.parseDouble(oWtgStr);
			
			if (upmValue != null){
				upmFactor = Double.parseDouble(upmValue);
			}
			if (upmFactor > 0)
				currentRequest.setObject(FIELD_ACTUAL_COMPLETE, ((oWtg * upmFactor) / 100.0));
			else if ((factor > 0.0) && (factor > existingFactor))
			{
				currentRequest.setObject(O_FACTOR, factor);
				currentRequest.setObject(FIELD_ACTUAL_COMPLETE, ((oWtg * factor)/ 100.0));
			}
		}
		
		return ruleResult;
	}
	
	//FlowStatusWithVendor	
	private double getFactor(Connection connection,
			BusinessArea ba, Request oldRequest, Request currentRequest,
			int source, User user, boolean isAddRequest, RuleResult ruleResult)
	{
		double factor = 0.0;
		String ownerFlowStatus 	= currentRequest.get(FIELD_FLOW_STATUS_WITH_OWNER);
		String vendorFlowStatus = currentRequest.get(FIELD_FLOW_STATUS_WITH_VENDOR);
		if (ownerFlowStatus != null)
		{			
			if (currentRequest.get(DECISION_FROM_MPPGCL).equals(TYPE_APPROVED)
					|| currentRequest.get(DECISION_FROM_MPPGCL).equals(TYPE_APP_WITH_COMMENTS)
					|| currentRequest.get(DECISION_FROM_MPPGCL).equals(TYPE_APP_RESUB_REQD))
				factor = 100.0;
			else if (ownerFlowStatus.equals(TYPE_SUBMITTED_FOR_COMMENTS)
						&& oldRequest.get(FIELD_FLOW_STATUS_WITH_OWNER).equals(TYPE_RETURNED_WITH_DECISION))
				factor = 95.0;
			else if (ownerFlowStatus.equals(TYPE_SUBMITTED_FOR_COMMENTS) 
						&& oldRequest.get(FIELD_FLOW_STATUS_WITH_OWNER).equals(TYPE_PENDING_SUBMISSION))
				factor = 75.0;
			else if (vendorFlowStatus.equals(TYPE_SUBMITTED_FOR_COMMENTS)){
				factor = 50.0;
			}
		}
		return factor;
	}
	
	

//	String enggType = currentRequest.get(FIELD_ENGINEERING_TYPE);
//	if (enggType == null)
//		ruleResult.setCanContinue(false);
//
//	if(enggType.equalsIgnoreCase(BASIC_ENGINEERING))
//		getWeightageFactorForBasicEngineering(connection, ba,
//			oldRequest, currentRequest, Source, user,
//			isAddRequest, ruleResult);
//	else if(enggType.equalsIgnoreCase(DETAILED_ENGINEERING))
//		getWeightageFactorForDetailedEngineering(connection, ba,
//			oldRequest, currentRequest, Source, user,
//			isAddRequest, ruleResult);
	private void getWeightageFactorForBasicEngineering(Connection connection,
			BusinessArea ba, Request oldRequest, Request currentRequest,
			int source, User user, boolean isAddRequest, RuleResult ruleResult) {
		
		String documentType = currentRequest.get(FIELD_DOCUMENT_TYPE);
		if (documentType == null){
			ruleResult.setMessage("Document Type not found, hence skipping the rule.");
			return;
		}
			
		String revisionType = currentRequest.get(FIELD_VENDOR_REVISION);
		if (revisionType == null){
			ruleResult.setMessage("Revision field not found, hence skipping the rule.");
			return;
		}
		
		String flowType = currentRequest.get(FIELD_FLOW_STATUS_WITH_OWNER);
		if (flowType == null){
			ruleResult.setMessage("Flow status field not found, hence skipping the rule");
			return;
		}
		
		String decisionFromOwner = currentRequest.get(FIELD_DECISION_FROM_OWNER);
		if (decisionFromOwner == null){
			ruleResult.setMessage("Decision from owner field not found, hence skipping the rule.");
			return;
		}
		
		String curFactorStr = currentRequest.get(O_FACTOR);
		if (curFactorStr != null){
			double curFactor = Double.parseDouble(curFactorStr);
			double factor = getWeightageFactorForSubmissionBE(documentType, revisionType, flowType, decisionFromOwner);
			if ((factor == 0) && (curFactor > 0.0)){
				ruleResult.setMessage("Skipping");
				return;
			}
			currentRequest.setObject(O_FACTOR, factor);
		}
	}
	
	private void getWeightageFactorForDetailedEngineering(
			Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int source, User user,
			boolean isAddRequest, RuleResult ruleResult) {
		String documentCategory = currentRequest.get(FIELD_DOCUMENT_CATEGORY);
		if (documentCategory == null){
			ruleResult.setMessage("No Document Category field found, hence skipping.");
			return;
		}
			
		String revisionType = currentRequest.get(FIELD_VENDOR_REVISION);
		if (revisionType == null){
			ruleResult.setMessage("Revision field not found, hence skipping the rule.");
			return;
		}
		
		String flowStatusType = currentRequest.get(FIELD_FLOW_STATUS_WITH_OWNER);
		if (flowStatusType == null){
			ruleResult.setMessage("Flow status field not found, hence skipping the rule");
			return;
		}
		
		String decisionType = currentRequest.get(FIELD_DECISION_FROM_OWNER);
		if (decisionType == null){
			ruleResult.setMessage("Decision from owner field not found, hence skipping the rule.");
			return;
		}
		
		String curFactorStr = currentRequest.get(O_FACTOR);
		if (curFactorStr != null)
		{
			double curFactor = Double.parseDouble(curFactorStr);
			double factor = getWeightageFactorForSubmissionDE(documentCategory, revisionType, flowStatusType, decisionType);
			if ((factor == 0) && (curFactor > 0.0)){
				ruleResult.setMessage("Skipping");
				return;
			}
			currentRequest.setObject(O_FACTOR, factor);
		}
	}
	
	static double getWeightageFactorForSubmissionBE(String documentType, String revisionType, 
			String flowStatusType, String decisionType){
		 
		if(documentType.equals(TYPE_DCD)){
			if (revisionType.equals("A") && flowStatusType.equals(TYPE_SUBMITTED_FOR_COMMENTS)){
				return 0.8;
			}
			else if ((!pSeriesRev.contains(revisionType)) 
					&& (!revisionType.equals("A"))
					&& (flowStatusType.equals(TYPE_SUBMITTED_FOR_COMMENTS))){
				return 0.95;
			}
			else if (decisionType.equals(TYPE_APPROVED)){
				return 1.0;
			}
		}
		else if(documentType.equals(TYPE_P_ID) || documentType.equals(TYPE_SLD) 
					|| documentType.equals(TYPE_PLANT_LAYOUT_DRAWINGS)){
			
			if (revisionType.equals("A") && flowStatusType.equals(TYPE_SUBMITTED_FOR_COMMENTS)){
				return 0.75;
			}
			else if ((!pSeriesRev.contains(revisionType)) 
					&& (!revisionType.equals("A"))
					&& (flowStatusType.equals(TYPE_SUBMITTED_FOR_COMMENTS))){
				return 0.9;
			}
			else if (decisionType.equals(TYPE_APPROVED)){
				return 0.925;
			}
			else if (decisionType.equals(TYPE_AS_BUILT)){
				return 1.0;
			}
		}
		
		return 0.0;
	}
	
	static double getWeightageFactorForSubmissionDE(String documentCategory, String revisionType, 
			String flowStatusType, String decisionType){
		 
		if(documentCategory.equals(APPROVAL)){
			if (revisionType.equals("A") && flowStatusType.equals(TYPE_SUBMITTED_FOR_COMMENTS)){
				return 0.7;
			}
			else if ((!pSeriesRev.contains(revisionType)) 
					&& (!revisionType.equals("A"))
					&& (flowStatusType.equals(TYPE_SUBMITTED_FOR_COMMENTS))){
				return 0.825;
			}
			else if (decisionType.equals(TYPE_APPROVED)){
				return 1.0;
			}
		}
		else if(documentCategory.equals(INFORMATION)){			
			if (revisionType.equals("A") && flowStatusType.equals(TYPE_SUBMITTED_FOR_COMMENTS)){
				return 0.75;
			}
			else if ((!pSeriesRev.contains(revisionType)) 
					&& (!revisionType.equals("A"))
					&& (flowStatusType.equals(TYPE_SUBMITTED_FOR_COMMENTS))){
				return 0.85;
			}
			else if (decisionType.equals(TYPE_RFC)){
				return 0.975;
			}
			else if (decisionType.equals(TYPE_AS_BUILT)){
				return 1.0;
			}
		}
		
		return 0.0;
	}


	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		
		return this.getClass().getSimpleName() + "- Weightage computation for Malwa.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	@Override
	public double getSequence() {

		return 0;
	}
			
	public static void main(String[] argsv) throws APIException, TBitsException{
		
		/*Hashtable<String, String> paramTable = new Hashtable<String, String>();
		paramTable.put(Field.BUSINESS_AREA, "Malwa_LNT");
		paramTable.put(Field.USER, "root");
		paramTable.put(Field.SUBJECT, "malwa rule");
		paramTable.put(FIELD_O_WTG, "100.0");		*/
				
		//Request addRequest = addRequest(paramTable);
//		if (addRequest != null)
//			paramTable.put(Field.REQUEST, addRequest.getRequestId() + "");
		//updRequest(paramTable);
		//paramTable.put(FIELD_FLOW_STATUS_WITH_VENDOR, "");
		//paramTable.put(FIELD_FLOW_STATUS_WITH_OWNER, "");	
		System.out.println("%%%%%%%%%%%%%Done");
		
	}
	
	static Request addRequest(Hashtable<String, String> paramTable) throws APIException{
		AddRequest addRequest = new AddRequest();
		addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
		Request req = addRequest.addRequest(paramTable);
		return req;
	}
	
	static void updRequest(Hashtable<String, String> paramTable) throws TBitsException, APIException{
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
		//Malwa_LNT#10
		paramTable.put(Field.REQUEST, "11");
		
		paramTable.put(FIELD_FLOW_STATUS_WITH_VENDOR, TYPE_SUBMITTED_FOR_COMMENTS);
		//paramTable.put(FIELD_FLOW_STATUS_WITH_OWNER, TYPE_SUBMITTED_FOR_COMMENTS);
		//paramTable.put(DECISION_FROM_MPPGCL, TYPE_APP_RESUB_REQD);
		updateRequest.updateRequest(paramTable);
	}

}
