package lntdcr;

import java.sql.Connection;
import java.util.ArrayList;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class WeightageComputation4KDI_LNT implements IRule, TBitsConstants{
	
	private static final String LAYOUT_DRAWING_LYD = "Layout_Drawing_LYD";
	private static final String SUBMITTED_FOR_COMMENTS = "UnderReview";
	private static final String RETURNED_WITH_DECISION = "ReturnedWithDecision";
	private static final String V_FACTOR = "v_Factor";
	private static final String O_FACTOR = "o_Factor";
	private static final String TYPE_PENDING_SUBMISSION	= "PendingReceipt";
	/*
	 * Fields
	 */
	private static final String GENERATION_AGENCY = "GenerationAgency";
	private static final String DOCUMENT_CATEGORY	= "DocumentCategory";
	private static final String REVISION			= "Revision";
	private static final String DECISION_FROM_OWNER= Field.SEVERITY;
	private static final String FLOW_STATUS_WITH_OWNER	= Field.STATUS;
	private static final String FLOW_STATUS_VENDOR= "FlowStatusWithVendor";
	private static final String DTN_OWNER			= "DTNToOwnerExt";
	private static final String DTN_VENDOR		= "DTNFromVendor";
	
	

	private static final String FIELD_DOCUMENT_TYPE		= Field.REQUEST_TYPE;
	private static final String FIELD_ENGINEERING_TYPE = "EngineeringType";
	private static final String TYPE_BASIC_ENGINEERING = "BasicEngineering";
	private static final String TYPE_DETAILED_ENGINEERING = "DetailedEngineering";
	
	
	private static final String SPECIFICATION 	= "Specification_SPEC";
	private static final String BID_EVALUATION 	= "Bid_Evaluation_Report_BER";
	
	/*
	 * Generating agnency
	 */
	private static String LTSL 			= "LTSL";
	
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
	private static String APPROVAL_DEC	= "Approved";
	
	private static String APPROVED_WITH_COMMENTS_RESUB 	= "ApprovedWithCommentsResubmissionRequired";
	private static String APPROVED_WITH_COMMENTS 		= "ApprovedWithComments";
	
	/*
	 * Flow Status
	 */
	private static String RFC			= "ReleaseForConstruction";
	private static String AS_BUILT		= "AsBuilt";
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
	 * Document Codes
	 */
	private static final String DCD	= "Design_Criteria_Document_DCD";
	private static final String SLD	= "Single_Line_Diagram_SLD";
	private static final String PID	= "PandID_PID";
	private static final String CALC	= "Calculation_CALC";
	private static final String PLOT  = "Plot_plan_PLOT";
	
	private static final String WEIGHTAGE 		= "Weightage";
	private static final String ACTUAL_COMPLETE = "ActualComplete";
	
	private static String DND	= "DND"; // Drawing/Document to be submitted for Approval ????
	private static final String FIELD_UPDATE_MANUALLY	= "UpdateProgressManually";
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{	
		RuleResult result = new RuleResult();
					
				
		// Rule to be executed in KDI_LNT
		if(ba.getSystemPrefix().equals("KDI_LNT") && (!isAddRequest))
		{
			String updStr = currentRequest.get(FIELD_UPDATE_MANUALLY);
			double upd = 0.0;
			if ((updStr != null) && (!updStr.trim().equals("")))
				upd = Double.parseDouble(updStr);

			double factor = 0.0;
			factor = calcFactorForOwner(connection, ba, oldRequest, currentRequest, Source, user, isAddRequest);
						
			double existingFactor = 0.0;
			String existingFactorStr = currentRequest.get(O_FACTOR);			
			if((existingFactorStr != null) && (existingFactorStr.trim().length() != 0))
				existingFactor = Double.parseDouble(existingFactorStr);			
						
			String oWtgStr = currentRequest.get(WEIGHTAGE);
			Double oWtg = 0.0;
			if ((oWtgStr != null) && (!oWtgStr.trim().equals("")))
				oWtg = Double.parseDouble(oWtgStr);			
			
			if (upd > 0)
				currentRequest.setObject(ACTUAL_COMPLETE, ((oWtg * upd) / 100.0));
			else if ((factor > 0.0) && (factor > existingFactor)){
				currentRequest.setObject(O_FACTOR, factor);
				currentRequest.setObject(ACTUAL_COMPLETE, ((oWtg * factor)/ 100.0));
			}
		}		
		return result;
	}
	
	private double calcFactorForOwner(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest){
		
		double factor = 0.0;
		String enggType = currentRequest.get(FIELD_ENGINEERING_TYPE);
		if (enggType != null){
			if (enggType.equals(TYPE_BASIC_ENGINEERING)
					&& (!pSeriesRev.contains(currentRequest.get(REVISION)))
					&& (!currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(oldRequest.get(FLOW_STATUS_WITH_OWNER))))
			{
				String docCode = currentRequest.get(FIELD_DOCUMENT_TYPE);
				if(docCode.equals(DCD))
				{
					if(currentRequest.get(DECISION_FROM_OWNER).equals(APPROVAL_DEC)
							|| currentRequest.get(DECISION_FROM_OWNER).equals(APPROVED_WITH_COMMENTS)){
						factor = 100.0;
					}
					else if(currentRequest.get(REVISION).equals(B)
								|| (oldRequest.get(FLOW_STATUS_WITH_OWNER).equals(RETURNED_WITH_DECISION)
										&& currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(SUBMITTED_FOR_COMMENTS))){
						factor = 95.0;
					}
					else if(currentRequest.get(REVISION).equals(A)
								|| (oldRequest.get(FLOW_STATUS_WITH_OWNER).equals(TYPE_PENDING_SUBMISSION)
										&& currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(SUBMITTED_FOR_COMMENTS))){
						factor = 80.0;
					}				
				}
				else if(docCode.equals(PID) || docCode.equals(SLD) || docCode.equals(PLOT) || docCode.equals(LAYOUT_DRAWING_LYD))
				{
					if(currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(AS_BUILT)){
						factor = 100.0;
					}else if(currentRequest.get(DECISION_FROM_OWNER).equals(APPROVAL_DEC)
							  || currentRequest.get(DECISION_FROM_OWNER).equals(APPROVED_WITH_COMMENTS)){
						factor = 97.5;
					}
					else if(currentRequest.get(REVISION).equals(B)
							|| (oldRequest.get(FLOW_STATUS_WITH_OWNER).equals(RETURNED_WITH_DECISION)
									&& currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(SUBMITTED_FOR_COMMENTS))){
						factor = 90.0;
					}
					else if(currentRequest.get(REVISION).equals(A) 
								|| (oldRequest.get(FLOW_STATUS_WITH_OWNER).equals(TYPE_PENDING_SUBMISSION)
										&& currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(SUBMITTED_FOR_COMMENTS))){
						factor = 70.0;
					}
				}
				else if (docCode.equals(CALC)){
					if(currentRequest.get(DECISION_FROM_OWNER).equals(APPROVAL_DEC)
							  || currentRequest.get(DECISION_FROM_OWNER).equals(APPROVED_WITH_COMMENTS)){
						factor = 100.0;
					}
					else if (currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(SUBMITTED_FOR_COMMENTS)
								&& oldRequest.get(FLOW_STATUS_WITH_OWNER).equals(RETURNED_WITH_DECISION))
						factor = 95.0;
					else if (currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(SUBMITTED_FOR_COMMENTS)
								&& oldRequest.get(FLOW_STATUS_WITH_OWNER).equals(TYPE_PENDING_SUBMISSION))
						factor = 80.0;
				}
			}
			else if (enggType.equals(TYPE_DETAILED_ENGINEERING) 
						&& ((!enggType.equals(SPECIFICATION) || (!enggType.equals(BID_EVALUATION)))
								|| (!enggType.equals("-"))))
			{
				String docCategory = currentRequest.get(DOCUMENT_CATEGORY);
				if (docCategory != null){
					if(docCategory.equals(APPROVAL))
					{
						if(currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(AS_BUILT)){
							factor = 100.0;
						}else if(currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(RFC)){
							factor = 97.5;
						}else if(currentRequest.get(REVISION).equals(B)
									|| (oldRequest.get(FLOW_STATUS_WITH_OWNER).equals(RETURNED_WITH_DECISION)
											&& currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(SUBMITTED_FOR_COMMENTS))){
							factor = 85.0;
						}else if(currentRequest.get(REVISION).equals(A)
									|| (currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(SUBMITTED_FOR_COMMENTS)
											&& oldRequest.get(FLOW_STATUS_WITH_OWNER).equals(TYPE_PENDING_SUBMISSION))){
							factor = 70.0;
						}
					}
					else if (docCategory.equals(INFORMATION))
					{
						if(currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(AS_BUILT)){
							factor = 100.0;
						}else if(currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(RFC)){
							factor = 97.5;
						}else if(currentRequest.get(REVISION).equals(A) 
								|| currentRequest.get(REVISION).equals(B) 
								|| (currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(SUBMITTED_FOR_COMMENTS)
										&& oldRequest.get(FLOW_STATUS_WITH_OWNER).equals(RETURNED_WITH_DECISION))
								|| (currentRequest.get(FLOW_STATUS_WITH_OWNER).equals(SUBMITTED_FOR_COMMENTS)
										&& oldRequest.get(FLOW_STATUS_WITH_OWNER).equals(TYPE_PENDING_SUBMISSION))){
							factor = 85.0;
						}
					}
				}
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

}
