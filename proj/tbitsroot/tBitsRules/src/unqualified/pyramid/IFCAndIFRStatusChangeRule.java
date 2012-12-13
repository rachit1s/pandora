/**
 * 
 */
package pyramid;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * <code>IFCAndIFRStatusChangeRule</code> ensures that a drawing can have the status "IFC" or "IFR" if and 
 * only if, the drawing has already been approved or approved with comments.
 */
public class IFCAndIFRStatusChangeRule implements IRule {
	
	//Extended field names
	private static final String DOCUMENTCATEGORY = "documentcategory";
	
	//Status type names
	private static final String STATUS_IFR = "IFR";
	private static final String STATUS_IFC = "ifc";
	private static final String STATUS_APPROVED = "Approved";
	private static final String STATUS_APPROVED_WITH_COMMENTS = "ApprovedWithComments";

	//Document category type names
	private static final String DOCUMENT_CATEGORY_IFC = "IFC";
	private static final String DOCUMENT_CATEGORY_IFR = "IFR";
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		RuleResult ruleResult = new RuleResult();
		int aSystemId = ba.getSystemId();
		String sysPrefix = ba.getSystemPrefix();
		boolean isApplicable = PyramidUtils.inProperty345SysPrefixes(sysPrefix);
		if (isApplicable){
			Type statusId = currentRequest.getStatusId();
			String curStatusName = statusId.getName();
			if (curStatusName.equals(STATUS_IFC) || curStatusName.equals(STATUS_IFR)){
				Type prevStatusId = oldRequest.getStatusId();
				String prevStatusName = prevStatusId.getName();
				boolean isPermissiblePrevStatus = prevStatusName.equals(STATUS_APPROVED) || prevStatusName.equals(STATUS_APPROVED_WITH_COMMENTS) || prevStatusName.equals(curStatusName);
				if (isPermissiblePrevStatus){
					
					Field docCatField;
					try {
						docCatField = Field.lookupBySystemIdAndFieldName(aSystemId, DOCUMENTCATEGORY);
						if (docCatField == null){
							ruleResult.setCanContinue(true);
						}
						else{
							RequestEx docCatReqEx = extendedFields.get(docCatField);
							int docCatTypeId = docCatReqEx.getTypeValue();
							setRuleResultBasedOnDocCategory(ruleResult,
									aSystemId, curStatusName, docCatTypeId);
						}
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
				}
				else{
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Cannot set the status to \"" + curStatusName + "\", as the previous status was not \"" 
							+ STATUS_APPROVED + " or " + STATUS_APPROVED_WITH_COMMENTS + "\". Please choose status other than IFC or IFR."  );
				}				
			}
			else{
				ruleResult.setCanContinue(true);
			}			
		}
		else{
			ruleResult.setCanContinue(true);
		}
		
		return ruleResult;
	}

	/**
	 * @param ruleResult
	 * @param aSystemId
	 * @param curStatusName
	 * @param docCatTypeId
	 * @throws DatabaseException
	 */
	private void setRuleResultBasedOnDocCategory(RuleResult ruleResult,
			int aSystemId, String curStatusName, int docCatTypeId)
			throws DatabaseException {
		Type docCatType = Type.lookupBySystemIdAndFieldNameAndTypeId(aSystemId, DOCUMENTCATEGORY, docCatTypeId);
		String name = docCatType.getName();
		if (STATUS_IFC.equals(curStatusName)){
			if((DOCUMENT_CATEGORY_IFR.equals(name))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If document category is \"IFR\" then status cannot be set to \"IFC\". So, please select appropriate status");
			}
			else
				ruleResult.setCanContinue(true);
		}
		else if (STATUS_IFR.equals(curStatusName)){
			if((DOCUMENT_CATEGORY_IFC.equals(name))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If document category is \"IFC\" then status cannot be set to \"IFR\". So, please select appropriate status" );
			}
			else
				ruleResult.setCanContinue(true);
		}
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "IFCAndIFRStatusChangeRule - Status can be \"IFC/IFR\" only if previous status was \"Approved/Approved With Comments\"." +
				"Also status can be IFC if document category is IFC and IFR is only if document category is IFR";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
