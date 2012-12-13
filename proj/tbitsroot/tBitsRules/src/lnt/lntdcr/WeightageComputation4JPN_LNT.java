package lntdcr;

import java.sql.Connection;
import java.util.ArrayList;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class WeightageComputation4JPN_LNT implements IRule, TBitsConstants{
	
	private static final String UPDATE_PROGRESS_MANUALLY = "UpdateProgressManually";
	private static final String V_FACTOR = "v_Factor";
	private static final String O_FACTOR = "o_Factor";
	/*
	 * Fields
	 */
	private static String GENERATION_AGENCY = "GenerationAgency";
	private static String DOCUMENT_CATEGORY = "DocumentCategory";
	private static String DOC_CAT_OWNER 	= "DocumentTypeWithOwner";
	private static String REVISION			= "Revision";
	private static String DOCUMENT_CODE		= Field.REQUEST_TYPE;
	private static String DECISION_DCPL		= Field.SEVERITY;
	private static String FLOW_STATUS_DCPL	= Field.STATUS;
	private static String FLOW_STATUS_VENDOR= "FlowStatusWithVendor";
	
	private static String PENDING_SUBMISSION= "PendingReceipt";
	private static String SUBMITTED			= "UnderReview";
	private static String RETURNED_WITH_DEC	= "ReturnedWithDecision";
	
	/*
	 * Generating agency
	 */
	private static String LTSL 							= "LTSL";
	private static final String FLOW_STATUS_WITH_LTSL 	= "FlowStatusWithCC";
	private static final String DECISION_FROM_LTSL 		= "DecisionFromCC";
	private static final String APPROVED_LTSL 			= "Approved";
	private static final String PENDING_SUBMISSION_LTSL = "PendingReceipt";
	
	private static final String CRS 					= "CRS";
	private static final String SUBMISSION_FILE_TYPE 	= "SubmissionFileType";
	
	private static final ArrayList<String> pSeriesRev = new ArrayList<String>();
	static{		
		pSeriesRev.add("P0");
		pSeriesRev.add("P1");
		pSeriesRev.add("P2");
		pSeriesRev.add("P3");
		pSeriesRev.add("P4");
		pSeriesRev.add("P5");
	}
	
	/*
	 * Category (Document Type)
	 */
	private static String APPROVAL 		= "Approval";
	private static String INFORMATION 	= "Information";
	
	/*
	 * Revision
	 */	
	private static String A				= "A";
	private static String B				= "B";
	
	/*
	 * DECISION
	 */
	private static String APPROVAL_DEC	= APPROVED_LTSL;
	
	/*
	 * Flow Status
	 */
	private static String RFC					 = "ReleaseForConstruction";
	private static String AS_BUILT				 = "AsBuilt";
	private static String RFC_LTSL 				 = "ReleaseForConstruction";

	private static String With_JPVL_Actual_Complete 	= "OwnerActualComplete";
	private static String With_JPVL_Weightage 			= "OwnerWeightage";	

	private static String With_Vendor_Weightage 		= "Weightage";
	private static String With_Vendor_Actual_Complete 	= "ActualComplete";

	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{	
		RuleResult result = new RuleResult();		
		
		// Rule to be executed in JPN_LNT BA
		if(ba.getSystemPrefix().equals("JPN_LNT") && (!isAddRequest))
		{
			double factor = 0.0;
			
			String submissionFileType = currentRequest.get(SUBMISSION_FILE_TYPE);
			if ((submissionFileType != null) && (submissionFileType.trim().equals(CRS)))
				return new RuleResult();
			
			if(currentRequest.get(GENERATION_AGENCY).equals(LTSL))
			{ // Doing for LTSL i.e. consultant
				factor = calcFactorForLTSL(connection, ba, oldRequest, currentRequest, Source, user, isAddRequest);
				
				double existingFactor = 0.0;
				String existingFactorStr = currentRequest.get(O_FACTOR);			
				if((existingFactorStr != null) && (existingFactorStr.trim().length() != 0))
					existingFactor = Double.parseDouble(existingFactorStr);
				
				
				double manualFactor = getManualFactor(oldRequest, currentRequest);
				double oWtg = getWeightage(currentRequest, With_JPVL_Weightage);
				if (manualFactor > 0){
					currentRequest.setObject(With_Vendor_Actual_Complete, (manualFactor * oWtg/100.0));
				}
				else if((factor > 0.0) && (factor > existingFactor)){
					currentRequest.setObject(O_FACTOR, factor);
					currentRequest.setObject(With_JPVL_Actual_Complete, (factor * oWtg/100.0));
				}				
			}else{ // All vendors
				try {
					calcFactor(connection, ba, oldRequest, currentRequest, Source, user, isAddRequest);
				} catch (DatabaseException e) {
					e.printStackTrace();
					result.setCanContinue(false);
					result.setMessage("Error Occured in fetching actions. "+ e.getMessage());
					return result;
				}
			}			
		}
		
		return result;
	}
	
	private double calcFactorForLTSL(Connection connection, BusinessArea ba, Request oldRequest, Request currentRequest, 
			int Source, User user,	boolean isAddRequest)
	{
		double factor = 0.0;
		if (!currentRequest.get(FLOW_STATUS_WITH_LTSL).equals(PENDING_SUBMISSION_LTSL)
				&& (!pSeriesRev.contains(currentRequest.get(REVISION))))
		{
			if(currentRequest.get(DOCUMENT_CATEGORY).equals(APPROVAL)){ // For Approval category
				//Let's roll!!
				if (currentRequest.get(FLOW_STATUS_WITH_LTSL).equals(AS_BUILT))
					factor = 100.0;
				else if (currentRequest.get(FLOW_STATUS_WITH_LTSL).equals(RFC_LTSL) 
							|| currentRequest.get(DECISION_FROM_LTSL).equals(APPROVED_LTSL))
					factor = 97.5;
				else if(currentRequest.get(REVISION).equals(B)
							|| (currentRequest.get(FLOW_STATUS_WITH_LTSL).equals(SUBMITTED)
									&& oldRequest.get(FLOW_STATUS_WITH_LTSL).equals(RETURNED_WITH_DEC))){
					factor = 85.0;
				}
				if(currentRequest.get(REVISION).equals(A) 
						|| (currentRequest.get(FLOW_STATUS_WITH_LTSL).equals(SUBMITTED))){
					factor = 70.0;
				}
			}
			else if(currentRequest.get(DOCUMENT_CATEGORY).equals(INFORMATION)){ // For Information category 
				if (currentRequest.get(FLOW_STATUS_WITH_LTSL).equals(RFC_LTSL) 
						|| currentRequest.get(DECISION_FROM_LTSL).equals(APPROVED_LTSL))
					factor = 100.0;
				else if(currentRequest.get(REVISION).equals(A)
						|| currentRequest.get(REVISION).equals(B)){
					factor = 85.0;
				}
			}
			if (factor > 0.0)
				currentRequest.setObject(O_FACTOR, factor);
		}

		return factor;
	}
	
	private double calcFactor(Connection connection, BusinessArea ba,	Request oldRequest, Request currentRequest, 
			int Source, User user, boolean isAddRequest) throws DatabaseException
	{
		double factor = 0.0;
			
		String flowStatusWithDCPL = currentRequest.get(FLOW_STATUS_DCPL);
		if((!oldRequest.get(FLOW_STATUS_DCPL).equals(currentRequest.get(FLOW_STATUS_DCPL)))
				&& (!flowStatusWithDCPL.equals(PENDING_SUBMISSION))
				&& (!pSeriesRev.contains(currentRequest.get(REVISION))))
		{
			if(currentRequest.get(DOC_CAT_OWNER).equals(APPROVAL)){
				/*
				 * Document submitted to/received from client
				 */
				if(currentRequest.get(FLOW_STATUS_DCPL).equals(AS_BUILT)){
					factor = 100.0;					
				}
				else if(currentRequest.get(FLOW_STATUS_DCPL).equals(RFC)){
					factor = 97.5;
				}
				else if(currentRequest.get(REVISION).equals(B) || 
							(oldRequest.get(FLOW_STATUS_DCPL).equals(RETURNED_WITH_DEC) 
									&& flowStatusWithDCPL.equals(SUBMITTED))){
					factor = 85.0;
				}
				else if(flowStatusWithDCPL.equals(SUBMITTED) &&
							(oldRequest.get(FLOW_STATUS_DCPL).equals(PENDING_SUBMISSION))){
					factor = 70.0;
				}
			}
			else if(currentRequest.get(DOC_CAT_OWNER).equals(INFORMATION)){ // For Information category 
				if (currentRequest.get(FLOW_STATUS_DCPL).equals(RFC))
					factor = 100.0;
				else if(currentRequest.get(REVISION).equals(A)
							|| currentRequest.get(REVISION).equals(B)
							|| flowStatusWithDCPL.equals(SUBMITTED)){
					factor = 85.0;
				}
			}
			/*
			 * For any other revisions skip.
			 */
			double userInputFactor= getWeightage(currentRequest, UPDATE_PROGRESS_MANUALLY);
			double oWtg = getWeightage(currentRequest, With_JPVL_Weightage);
			
			double existingFactor = 0.0;
			String existingFactorStr = currentRequest.get(O_FACTOR);			
			if((existingFactorStr != null) && (existingFactorStr.trim().length() != 0))
				existingFactor = Double.parseDouble(existingFactorStr);
			
			if (userInputFactor > 0){					
				currentRequest.setObject(With_JPVL_Actual_Complete, (userInputFactor * oWtg/100.0));
			}
			else if ((factor > 0) && (factor > existingFactor)) {
				currentRequest.setObject(O_FACTOR, factor);
				currentRequest.setObject(With_JPVL_Actual_Complete, (factor * oWtg/100.0));
			}			
		}
		else if(!oldRequest.get(FLOW_STATUS_VENDOR).equals(currentRequest.get(FLOW_STATUS_VENDOR))
					&& (!currentRequest.get(FLOW_STATUS_VENDOR).equals(PENDING_SUBMISSION))
					&& (!pSeriesRev.contains(currentRequest.get(REVISION))))
		{ 
			if(currentRequest.get(DOCUMENT_CATEGORY).equals(APPROVAL))
			{
				if(currentRequest.get(FLOW_STATUS_VENDOR).equals(AS_BUILT)){
					factor = 100.0;
				}
				else if(currentRequest.get(FLOW_STATUS_VENDOR).equals(RFC)){
					factor = 97.5;
				}
				else if(currentRequest.get(REVISION).equals(B) || 
							(currentRequest.get(FLOW_STATUS_VENDOR).equals(SUBMITTED)
								&& oldRequest.get(FLOW_STATUS_VENDOR).equals(RETURNED_WITH_DEC))){
					factor = 85.0;
				}
				else if(currentRequest.get(REVISION).equals(A) || 
							(currentRequest.get(FLOW_STATUS_VENDOR).equals(SUBMITTED)
									&& oldRequest.get(FLOW_STATUS_VENDOR).equals(PENDING_SUBMISSION))){
					factor = 70.0;
				}
			}
			else if(currentRequest.get(DOCUMENT_CATEGORY).equals(INFORMATION)){
				if(currentRequest.get(FLOW_STATUS_VENDOR).equals(AS_BUILT)){
					factor = 100.0;
				}
				else if((currentRequest.get(FLOW_STATUS_VENDOR).equals(SUBMITTED)
									&& oldRequest.get(FLOW_STATUS_VENDOR).equals(PENDING_SUBMISSION))){
					factor = 85.0;
				}
			}
			
			double existingFactor = 0.0;
			String existingFactorStr = currentRequest.get(V_FACTOR);
			if((existingFactorStr != null) && (existingFactorStr.trim().length() != 0))
				existingFactor = Double.parseDouble(existingFactorStr);
						
			double userInputFactor = getWeightage(currentRequest, UPDATE_PROGRESS_MANUALLY);
			double vWtg = getWeightage(currentRequest, With_Vendor_Weightage);
			if (userInputFactor > 0){					
				currentRequest.setObject(With_Vendor_Actual_Complete, (userInputFactor * vWtg/100.0));
			}
			else if ((factor > 0) && (factor > existingFactor)){
				currentRequest.setObject(V_FACTOR, factor);
				currentRequest.setObject(With_Vendor_Actual_Complete, (factor * vWtg/100.0));
			}
		}
		
		return factor;
	}
	
	public String getName() {
		return "Weightage Computation Rule for JPN_LNT";
	}

	public double getSequence() {
		return 1;
	}
	
	double getWeightage (Request currentRequest, String wtgFieldName){
		double owner_weightage = 0.0;
		String owner_weightage_str = currentRequest.get(wtgFieldName);
		if (owner_weightage_str != null){
			owner_weightage = Double.parseDouble(owner_weightage_str);
		}
		return owner_weightage;
	}
	
	double getManualFactor (Request oldRequest, Request currentRequest){
		double manualFactor = 0.0;
		String upm = currentRequest.get(UPDATE_PROGRESS_MANUALLY);
		if (((oldRequest.get(UPDATE_PROGRESS_MANUALLY) == null) 
				|| oldRequest.get(UPDATE_PROGRESS_MANUALLY).trim().equals(""))
				&& (upm != null) && (upm.trim().length() != 0)){
			manualFactor = Double.parseDouble(upm);		
		}
		return manualFactor;
	}	

	
	
	/*
	 * 
	 BASIS FOR CLIENT PROGRESS MEASUREMENT
    1   DRAWING / DOCUMENTS TO BE SUBMITTED FOR APPROVAL
        1ST SUBMISSION TO JPVL / DCPL
   1.1                                                                    70.00%
        2ND SUBMISSION AFTER INCORPORATING COMMENTS
   1.2                                                                    15.00%
        APPROVED / RELEASED FOR CONSTRUCTION
   1.3                                                                    12.50%
        AS BUILT
   1.4                                                                    2.50%
    2   DRAWING / DOCUMENTS TO BE SUBMITTED FOR INFORMATION
        1ST SUBMISSION TO JPVL / DCPL
   2.1                                                                    85.00%
        APPROVED / RELEASED FOR CONSTRUCTION
   2.2                                                                    15.00%
        AS BUILT
   2.3                                                                       -
BASIS FOR VENDOR PROGRESS MEASUREMENT
    1   DRAWING / DOCUMENTS TO BE SUBMITTED FOR APPROVAL
   1.1                                                                    70.00%
        1ST SUBMISSION TO STGI
        2ND SUBMISSION AFTER INCORPORATING COMMENTS
   1.2                                                                    15.00%
        APPROVED / RELEASED FOR CONSTRUCTION
   1.3                                                                    12.50%
        AS BUILT
   1.4                                                                    2.50%
        DRAWING / DOCUMENTS TO BE SUBMITTED FOR INFORMATION
    2
        1ST SUBMISSION TO JPVL / DCPL
   2.1                                                                    85.00%
        APPROVED / RELEASED FOR CONSTRUCTION
   2.2                                                                    15.00%
        AS BUILT
   2.3                                                                       -
BASIS FOR LTSL PROGRESS MEASUREMENT
    1   DRAWING / DOCUMENTS TO BE SUBMITTED FOR APPROVAL
   1.1                                                                    70.00%
        1ST SUBMISSION TO STGI
        2ND SUBMISSION AFTER INCORPORATING COMMENTS
   1.2                                                                    15.00%
        APPROVED / RELEASED FOR CONSTRUCTION
   1.3                                                                    12.50%
        AS BUILT
   1.4                                                                    2.50%
    2   DRAWING / DOCUMENTS TO BE SUBMITTED FOR INFORMATION
        1ST SUBMISSION TO JPVL / DCPL
   2.1                                                                    85.00%
        APPROVED / RELEASED FOR CONSTRUCTION
   2.2                                                                    15.00%
        AS BUILT
   2.3                                                                       -
PROGRESS BOOKING OF LTSL FOR RPOCUREMENT ASSISTANCE
SR. NO.          DESCRIPTION          REV - P0 REV - A FINAL SUBMISSION TOTAL %
        SPECIFICATION
    1                                    54.55   27.27         18.18       100%
        BID EVALUATION REPORT
    2                                     60       40                      100%

	 
	 
	 
	 * 
	 */
	

}
