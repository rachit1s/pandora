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
 * <code>RuleIFCIssued</code> ensures that "IFC Issued" can be true only is the drawing is "Approved" 
 * or "Approved With Comments".
 *
 */
public class RuleIFCIssued implements IRule {

	private static final String APPROVED_WITH_COMMENTS = "ApprovedWithComments";
	private static final String APPROVED = "Approved";
	private static final String IFCISSUED = "IFCIssued";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		
		//String sysPrefix = "DCR343, DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = PyramidUtils.isExistsInCommons(ba.getSystemPrefix());
		
		if (isRuleApplicable && (currentRequest.getParentRequestId() == 0)){			
			Field ifcField = null;
			try {
				ifcField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), IFCISSUED);
				if (ifcField != null){
					RequestEx reqEx = extendedFields.get(ifcField);
					if (reqEx != null){
						if (reqEx.getBitValue()){
							int systemId = ba.getSystemId();
							Type curStatusType = currentRequest.getStatusId();
							Type type1 = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, APPROVED);					
							Type type2 = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, APPROVED_WITH_COMMENTS);
							if (curStatusType.equals(type1) || curStatusType.equals(type2)){
								return new RuleResult(true, "Status set correctly", true);						
							}
							else{
								return new RuleResult (false, "Cannot select \"IFC Issued\" unless status is \"Approved\" or \"Approved with comments\"", false);
							}
						}
						else{
							return new RuleResult(true, "As IFC Issued is not set to true");
						}
					}
					else
					{
						return new RuleResult(true, "Not applicable as extended field does not exist");
					}
				}
				else
				{
					return new RuleResult(true, "Not applicable IFCIssued field does not exist");
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		return new RuleResult(true, "Not applicable to this business area/IDC(sub-request)");
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "RuleIFCIssued - \"IFC Issued\" can only be true if the status is either \"Approved\" \"Approved with comments\"";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
