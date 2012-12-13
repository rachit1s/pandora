/**
 * 
 */
package lntdcr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.ActionEx;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;

/**
 * @author Lokesh
 *
 */
public class UtilityForWeightageComputation4APLLTP {
	
	private static final String CAND_I_DRAWINGS_DOCUMENTS_CNI = "CandI_Drawings_Documents_CNI";
	private static final String LAYOUT_DRAWING_LYD 		= "Layout_Drawing_LYD";
	private static final String SUPPORT_DRAWING_SUPT 	= "Support_Drawing_SUPT";
	private static final String IBR_DOCUMENT_IBR 		= "IBR_Document_IBR";
	private static final String ISOMETRICS_ISO 			= "Isometrics_ISO";
	private static final String MECHANICAL 				= "Mechanical";
	private static final String DISCIPLINE_C_AND_I		= "CandI";
	private static final String ELECTRICAL 				= "Electrical";
	private static final String NOT_APPLICABLE 			= "NotApplicable";
	private static final String GENERATION_AGENCY_LTSL 	= "LTSL";
	private static final String GENERATION_AGENCY_LNT 	= "LnT";
	private static final String DISCIPLINE 				= "Discipline";
	private static final String GENERATION_AGENCY 		= "GenerationAgency";
	private static final String GENERAL_ARRANGEMENT_DRAWING_GA = "General_Arrangement_Drawing_GA";
	
	private static final int APL_LNT_SYS_ID 			= 104;
	private static final String DOCUMENT_TYPE 			= Field.REQUEST_TYPE;
	private static final String O_DOCUMENT_CATEGORY		= "o_DocumentCategory";
	private static final String V_DOCUMENT_CATEGORY 	= "DocumentCategory";

	private static final String FLOW_STATUS_WITH_DESEIN = Field.STATUS;
	private static final String DEC_FROM_DESEIN			= Field.SEVERITY;
	
	private static final String FLOW_STATUS_WITH_VENDOR = "FlowStatusWithVendor";
	private static final String DEC_TO_VENDOR			= "DecisionToVendor";
	
	private static final String FIELD_UPDATE_MANUALLY	= "UpdateProgressManually";
	
	private static final String APPROVAL 				= "Approval";
	private static final String INFORMATION 			= "Information";
	
	private static final String OWNER_WEIGHTAGE 		= "OwnerWeightage";
	private static final String O_FACTOR 				= "o_Factor";
	private static final String OWNER_ACTUAL_COMPLETE 	= "OwnerActualComplete";
	
	private static final String V_FACTOR 				= "v_Factor";
	private static final String WEIGHTAGE 				= "Weightage";
	
	private static final String ACTUAL_COMPLETE 		= "ActualComplete";
	
	private static final String TYPE_PENDING_SUBMISSION	= "PendingReceipt";
	private static final String TYPE_SUBMITTED     		= "UnderReview";
	private static final String TYPE_RETURNED_WITH_DECISION		= "ReturnedWithDecision";
	private static final String TYPE_RFC						= "ReleaseForConstruction";
	private static final String TYPE_AS_BUILT					= "AsBuilt";
	
	private static final String TYPE_APPROVED					= "Approved";
	private static final String TYPE_APP_WITH_COMMENTS			= "ApprovedWithComments";
	private static final String TYPE_APP_RESUB_REQD				= "ApprovedWithCommentsResubmissionRequired";
	
	private static final String CRS 							= "CRS";
	private static final String SUBMISSION_FILE_TYPE 			= "SubmissionFileType";
	
	private static final String ELECTRICAL_DRAWINGS				= "Electrical_Drawings_Documents_ELEC";
	
	private static Hashtable<String, WeightageInfo> wtgMap = null;
	
	
	public static void setActualWeightage(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest) throws DatabaseException
	{
		double factor = 0.0;
		boolean isOwnerStatusChanged = false;
						
		String docType = currentRequest.get(DOCUMENT_TYPE);
		String updStr = currentRequest.get(FIELD_UPDATE_MANUALLY);
		
		double upd = 0.0;
		if ((updStr != null) && (!updStr.trim().equals("")))
			upd = Double.parseDouble(updStr);
					
		int systemId = currentRequest.getSystemId();
		int requestId = currentRequest.getRequestId();
		Field ownerDecsionField = Field.lookupBySystemIdAndFieldName(systemId, DEC_FROM_DESEIN);
		Field discipline = Field.lookupBySystemIdAndFieldName(systemId, DISCIPLINE);
		String curFlowStatusWithDesein = currentRequest.get(FLOW_STATUS_WITH_DESEIN);
		Field factorField = Field.lookupBySystemIdAndFieldName(systemId, O_FACTOR);
				
		Action curAction 	= null;
		Action prevAction 	= null;
		Type ownerDecType	= null;
		ArrayList<Integer> actionIdsList = getPreviousActionsList(connection, systemId, 
																		requestId, currentRequest.getMaxActionId());
		if ((actionIdsList != null) && (!actionIdsList.isEmpty()))
		{			
			for (int index = 0 ; index<(actionIdsList.size()-1); index++)
			{
				if (actionIdsList.size()<2)
					continue;
				prevAction = Action.lookupBySystemIdAndRequestIdAndActionId(ba.getSystemId(), 
						requestId, actionIdsList.get(index));
				curAction = Action.lookupBySystemIdAndRequestIdAndActionId(ba.getSystemId(), 
											requestId, actionIdsList.get(index + 1));
								
				if ((curAction != null) && (prevAction != null))
				{					
					if (curAction.getStatusId()!= prevAction.getStatusId())
					{
						Type curOwnerStatusType = Type.lookupBySystemIdAndFieldNameAndTypeId(
								systemId, Field.STATUS, curAction.getStatusId());
						Type prevOwnerStatusType = Type.lookupBySystemIdAndFieldNameAndTypeId(
								systemId, Field.STATUS, prevAction.getStatusId());
						if (ownerDecsionField != null)
						{	
							ownerDecType = Type.lookupBySystemIdAndFieldNameAndTypeId(
									systemId, Field.SEVERITY, curAction.getSeverityId());
							String generationAgency = currentRequest.get(GENERATION_AGENCY);
							WeightageInfo wi = null;
							
							// Check if the document generates from LTSL or LNT, if yes, then continue with the existing docType
							// else set it to not applicable
							if (generationAgency.equals(GENERATION_AGENCY_LNT) ||
									generationAgency.equals(GENERATION_AGENCY_LTSL))
								docType = currentRequest.get(DOCUMENT_TYPE);
							else
								docType = NOT_APPLICABLE;

							if (currentRequest.get(O_DOCUMENT_CATEGORY).equals(APPROVAL))
							{								
								wi = wtgMap.get(getKey(O_DOCUMENT_CATEGORY, APPROVAL, docType));									
							}
							else if (currentRequest.get(O_DOCUMENT_CATEGORY).equals(INFORMATION))
							{
								wi = wtgMap.get(getKey(O_DOCUMENT_CATEGORY, INFORMATION, docType));									
							}
							
							//If the above does not fetch weightage info, check if the documents belong 
							//to electrical or CnI and apply the weightage as below
							if ((!docType.trim().equals(NOT_APPLICABLE)) && (wi == null)){
								String disciplineType = currentRequest.get(DISCIPLINE);
								if(disciplineType.equals(ELECTRICAL)){
									if (currentRequest.get(O_DOCUMENT_CATEGORY).equals(APPROVAL))
									{								
										wi = wtgMap.get(getKey(O_DOCUMENT_CATEGORY, APPROVAL, ELECTRICAL_DRAWINGS));									
									}
									else if (currentRequest.get(O_DOCUMENT_CATEGORY).equals(INFORMATION))
									{
										wi = wtgMap.get(getKey(O_DOCUMENT_CATEGORY, INFORMATION, ELECTRICAL_DRAWINGS));									
									}
								}
								else if (disciplineType.equals(DISCIPLINE_C_AND_I)){
									if (currentRequest.get(O_DOCUMENT_CATEGORY).equals(APPROVAL))
									{								
										wi = wtgMap.get(getKey(O_DOCUMENT_CATEGORY, APPROVAL, CAND_I_DRAWINGS_DOCUMENTS_CNI));									
									}
									else if (currentRequest.get(O_DOCUMENT_CATEGORY).equals(INFORMATION))
									{
										wi = wtgMap.get(getKey(O_DOCUMENT_CATEGORY, INFORMATION, CAND_I_DRAWINGS_DOCUMENTS_CNI));									
									}									
								}
								else if (disciplineType.equals(MECHANICAL)){
									if (docType.equals(IBR_DOCUMENT_IBR) || docType.equals(SUPPORT_DRAWING_SUPT))
										wi = wtgMap.get(getKey(O_DOCUMENT_CATEGORY, INFORMATION, ISOMETRICS_ISO));
									if (docType.equals(GENERAL_ARRANGEMENT_DRAWING_GA))
										wi = wtgMap.get(getKey(O_DOCUMENT_CATEGORY, APPROVAL, LAYOUT_DRAWING_LYD));
								}
							}							
							
							factor = getFactor(currentRequest, curOwnerStatusType,
									prevOwnerStatusType, ownerDecType, wi);
						}
						if (factor > 0){
							isOwnerStatusChanged = true;
						}
						
						if ( isOwnerStatusChanged )//&& isDocTypeNotEmpty(docType) )
						{			
							//OwnerActualComplete	With APPDCL Actual % Complete
							//OwnerWeightage		With APPDCL Weightage
							String existingFactorStr = currentRequest.get(O_FACTOR);
//							ActionEx actionEx = ActionEx.lookupBySystemIdRequestIdActionIdFieldId(systemId, requestId,
//									curAction.getActionId(), factorField.getFieldId());
							double existingFactor = 0.0;

							if ((existingFactorStr != null) && (existingFactorStr.trim().length() != 0))
								existingFactor = Double.parseDouble(existingFactorStr);
										
							String oWtg = currentRequest.get(OWNER_WEIGHTAGE);
							double wtg = 0.0;
							if ((oWtg != null) && (!oWtg.trim().equals("")))
								wtg = Double.parseDouble(oWtg);
							
//							if (upd > 0.0)
//								currentRequest.setObject(OWNER_ACTUAL_COMPLETE, ((upd * wtg)/100.0));
//							else 
							if ((factor > 0) && (factor > existingFactor))
							{
								Field ownerActualCompleteField = Field.lookupBySystemIdAndFieldName(systemId, OWNER_ACTUAL_COMPLETE);
								double actualComplete = computeActualComplete(factor, wtg);								
								updateActualCompleteToRequest(connection, systemId, requestId, factorField.getFieldId(), 
										ownerActualCompleteField.getFieldId(), factor, actualComplete);
								updateActualCompleteToAction(connection, systemId, requestId, curAction.getActionId(), 
										currentRequest.getMaxActionId(), factorField.getFieldId(), ownerActualCompleteField.getFieldId(),
										factor, actualComplete);
							}
							
						}
					}
					else
						continue;
				}				
			}
		}
		
//		if (!oldRequest.get(FLOW_STATUS_WITH_VENDOR).equals(currentRequest.get(FLOW_STATUS_WITH_VENDOR)))
//		{
//			String vFlowStatus = currentRequest.get(FLOW_STATUS_WITH_VENDOR);
//			String decToVendor = currentRequest.get(DEC_TO_VENDOR);
//			if (currentRequest.get(V_DOCUMENT_CATEGORY).equals(APPROVAL))
//			{
//				WeightageInfo wi = wtgMap.get(getKey(V_DOCUMENT_CATEGORY, APPROVAL, docType));
//				factor = getFactorForVendorApproval(currentRequest, vFlowStatus, decToVendor, wi);
//			}
//			else if (currentRequest.get(V_DOCUMENT_CATEGORY).equals(INFORMATION))
//			{
//				WeightageInfo wi = wtgMap.get(getKey(V_DOCUMENT_CATEGORY, INFORMATION, docType));
//				factor = getFactorForVendorInformation( currentRequest, vFlowStatus, wi );
//			}
//			//Weightage	With Vendor Weightage
//			//ActualComplete	With Vendor Actual % Complete
//			String existingFactorStr = currentRequest.get(V_FACTOR);
//			double existingFactor = 0.0;
//			if ((existingFactorStr != null) && (existingFactorStr.trim().length() != 0))
//				existingFactor = Double.parseDouble(existingFactorStr);	
//						
//			String vWtg = currentRequest.get(WEIGHTAGE);
//			double wtg = 0.0;
//			if ((vWtg != null) && (!vWtg.trim().equals("")))
//				wtg = Double.parseDouble(vWtg);
//			
//			if (upd > 0.0)
//				currentRequest.setObject(ACTUAL_COMPLETE, computeActualComplete(upd, wtg));
//			else if ((factor > 0) && (factor > existingFactor))
//			{ 
//				currentRequest.setObject(V_FACTOR, factor);				
//				currentRequest.setObject(ACTUAL_COMPLETE, computeActualComplete(factor, wtg));
//			}
//		}
	}
	
	private static double computeActualComplete (double factor, double weightage){
		return ((factor * weightage)/100.0);
	}
	
	private static ArrayList<Integer> getPreviousActionsList(Connection connection, 
			int aSystemId, int aRequestId, int maxActionId) throws DatabaseException{
		
		ArrayList<Integer> actionIds = new ArrayList<Integer>();
		Type submissionFileType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(aSystemId, SUBMISSION_FILE_TYPE, CRS);
		if (submissionFileType != null){
			try {
				PreparedStatement ps = connection.prepareStatement("SELECT action_id FROM ACTIONS_EX WHERE sys_id=? and request_id=? and" +
																		" field_id=? and type_value<>?");
				ps.setInt(1, aSystemId);
				ps.setInt(2, aRequestId);
				ps.setInt(3, submissionFileType.getFieldId());
				ps.setInt(4, submissionFileType.getTypeId());
				ResultSet rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						actionIds.add(rs.getInt("action_id"));
					}
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException("Error occurred while fetching previous actions", e);
			}
		}
		return actionIds;
	}
	
	
	public static double getFactor (Request currentRequest, Type curOwnerStatusType, 
			Type prevOwnerStatusType, Type curOwnerDecisionType, WeightageInfo wtgInfo)
	{
		double factor = 0.0;	
		if (wtgInfo == null)
			return 0;
		
		String decisionType = curOwnerDecisionType.getName();
		String prevStatus = prevOwnerStatusType.getName();
		String curOwnerStatus = curOwnerStatusType.getName();
		
		//First submission /Approval /As Built
		if (curOwnerStatusType.getName().equals(TYPE_AS_BUILT))
			factor = wtgInfo.getAsBuiltFactor() + wtgInfo.getApprovalOrRFCFactor() 
						+ wtgInfo.getSecondSubmissionFactor() + wtgInfo.getFirstSubmissionFactor();
		else if (decisionType.equals(TYPE_APPROVED) || decisionType.equals(TYPE_APP_RESUB_REQD)
						|| decisionType.equals(TYPE_APP_WITH_COMMENTS))
			factor = wtgInfo.getApprovalOrRFCFactor() + wtgInfo.getSecondSubmissionFactor() 
						+ wtgInfo.getFirstSubmissionFactor();
		else if (prevStatus.equals(TYPE_RETURNED_WITH_DECISION) 
							&& curOwnerStatus.equals(TYPE_SUBMITTED))
			factor = wtgInfo.getSecondSubmissionFactor() + wtgInfo.getFirstSubmissionFactor();				
		else if (prevStatus.equals(TYPE_PENDING_SUBMISSION) 
								&& curOwnerStatus.equals(TYPE_SUBMITTED))
				factor = wtgInfo.getFirstSubmissionFactor();
		
		return factor;
	}
	

	private static double getFactorForVendorApproval(Request oldRequest, 
			String vFlowStatus, String decToVendor, WeightageInfo wtgInfo) {
		double factor = 0.0;
		if (wtgInfo == null)
			return 0;
		
		//First submission /Approval /As Built
		if (vFlowStatus.equals(TYPE_AS_BUILT))
			factor = wtgInfo.getAsBuiltFactor();				
		else if (decToVendor.equals(TYPE_APPROVED) || decToVendor.equals(TYPE_APP_RESUB_REQD)
					|| decToVendor.equals(TYPE_APP_WITH_COMMENTS))
			factor = wtgInfo.getApprovalOrRFCFactor();
		else if (oldRequest.get(FLOW_STATUS_WITH_VENDOR).equals(TYPE_RETURNED_WITH_DECISION) 
				&& vFlowStatus.equals(TYPE_SUBMITTED))
			factor = wtgInfo.getSecondSubmissionFactor();				
		else if (oldRequest.get(FLOW_STATUS_WITH_VENDOR).equals(TYPE_PENDING_SUBMISSION) 
					&& vFlowStatus.equals(TYPE_SUBMITTED))
			factor = wtgInfo.getFirstSubmissionFactor();
		return factor;
	}
	
	private static String getKey(String docCategoryFieldName, String docCategoryFieldValue, String docType) {
		return docCategoryFieldName + WeightageInfo.DELIMETER_UNDERSCORE + docCategoryFieldValue 
					+ WeightageInfo.DELIMETER_UNDERSCORE + docType;
	}

	private static boolean isDocTypeNotEmpty(String docType) {
		if ((docType == null) || (docType.trim().equals("")))
			return false;
		return true;
	}
			
	private static void updateActualCompleteToRequest (Connection connection, 
			int systemId, int requestId, int factorFieldId, int actualCompleteFieldId, double factor, 
			double actualComplete) throws DatabaseException{
		
		String REQUEST_UPDATE_QUERY = "UPDATE requests_ex SET real_value=? WHERE sys_id=? and request_id=? and field_id=? " ;
		try {
			PreparedStatement ps = connection.prepareStatement(REQUEST_UPDATE_QUERY);
			addToBatchRequest(ps, systemId, requestId, factorFieldId, factor);
			addToBatchRequest(ps, systemId, requestId, actualCompleteFieldId, actualComplete);
			ps.executeBatch();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error occurred while updating factor and actual complete.", e);
		}		
	}
	
	private static void updateActualCompleteToAction (Connection connection, 
			int systemId, int requestId, int curActionId, int maxActionId, int factorFieldId,
			int actualCompleteFieldId, double factor, double actualComplete)
		throws DatabaseException{
		
		String ACTION_UPDATE_QUERY = "UPDATE actions_ex SET real_value=? WHERE sys_id=? and request_id=?  and action_id=? and field_id=? " ;
		try {
			PreparedStatement ps = connection.prepareStatement(ACTION_UPDATE_QUERY);
			for (int actionId=curActionId ; actionId <= maxActionId; actionId++){
				addToBatchAction(ps, systemId, requestId, actionId, factorFieldId, factor);
				addToBatchAction(ps, systemId, requestId, actionId, actualCompleteFieldId, actualComplete);
			}
			ps.executeBatch();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error occurred while updating factor and actual complete.", e);
		}
		
	}
	
	private static void addToBatchRequest(PreparedStatement ps, int systemId,
			int requestId, int fieldId, double value) throws SQLException {
		ps.setDouble(1, value);
		ps.setInt(2, systemId);
		ps.setInt(3, requestId);
		ps.setInt(4, fieldId);
		ps.addBatch();
	}
	
	private static void addToBatchAction(PreparedStatement ps, int systemId,
			int requestId, int actionId, int fieldId, double value) throws SQLException {
		ps.setDouble(1, value);
		ps.setInt(2, systemId);
		ps.setInt(3, requestId);
		ps.setInt(4, actionId);
		ps.setInt(5, fieldId);
		ps.addBatch();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		Connection connection = null;
		wtgMap = WeightageInfo.getWeightageMapBySysId(APL_LNT_SYS_ID);
		try {
			connection = DataSourcePool.getConnection();
			int startIndex = 0;
			int lastIndex  = 0;
			int firstRequestIndex = 1;
			
			BusinessArea ba = BusinessArea.lookupBySystemId(APL_LNT_SYS_ID);
			int maxRequestId = ba.getMaxRequestId();
			startIndex = firstRequestIndex;
			lastIndex = maxRequestId;
			
			if (maxRequestId > 0){
				System.out.println("Start udpate");
				
				if (args.length > 0){
					if (args[0] != null){
						startIndex = (args[0].trim().length() != 0)
										? Integer.parseInt(args[0].trim())
											: firstRequestIndex;
					}

					if ((args.length > 1) && (args[1] != null)){
						lastIndex = ((args[1].trim().length() != 0) && (Integer.parseInt(args[1].trim())<= maxRequestId))
										? Integer.parseInt(args[1].trim())
											: maxRequestId;
					}
				}
				
				for (int requestId= startIndex ; requestId <= lastIndex; requestId++){
					System.out.println("Request Id: " + requestId);
					Request currentRequest = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
					if (currentRequest != null){
						setActualWeightage(connection, ba, currentRequest, currentRequest);
					}
				}
				System.out.println("Done");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
