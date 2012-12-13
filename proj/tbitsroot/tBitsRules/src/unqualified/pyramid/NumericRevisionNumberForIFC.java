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
 * <code>NumericRevisionNumberForIFC ensures that the revision numbers for the drawings are numeric rather than 
 * alphabetic letters when the status of the drawing is IFC or IFR.
 */
public class NumericRevisionNumberForIFC implements IRule {

	private static final String IFC = "ifc";
	private static final String REVISION = "Revision";
	private static final Object IFR = "IFR";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		RuleResult ruleResult = new RuleResult();
		int systemId = ba.getSystemId();
		boolean isApplicable = PyramidUtils.inProperty345SysPrefixes(ba.getSystemPrefix());
		if (isApplicable){
			Type statusId = currentRequest.getStatusId();
			if (statusId.getName().equals(IFC) || statusId.getName().equals(IFR)){
				Field revField;
				try {
					revField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), REVISION);

					if (revField != null){
						RequestEx revReqEx = extendedFields.get(revField);
						Type revType = Type.lookupBySystemIdAndFieldNameAndTypeId(systemId, revField.getName(), revReqEx.getTypeValue());

						if (revReqEx!=null){
							String revName = revType.getName();
							if (isRevisionNumberNumeric(revName)){
								ruleResult.setCanContinue(true);
								ruleResult.setSuccessful(true);
							}
							else{
								ruleResult.setCanContinue(false);
								ruleResult.setMessage("Revision number should be '0/1/2/3/4' instead of '" + revName + "'");
							}
						}
						else{
							ruleResult.setCanContinue(true);
							ruleResult.setMessage("Not applicable to this BA: " + ba.getName() + "as no revision field request info was found");
						}
					}
					else{
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Not applicable to this BA: " + ba.getName() + "as no revision field was found");
					}			
				} catch (DatabaseException e1) {				
					e1.printStackTrace();
				}
			}
			else{
				ruleResult.setCanContinue(true);
				ruleResult.setMessage("Not applicable for the current status");
			}
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to this BA: " + ba.getName());
		}			
		return ruleResult;
	}

	private boolean isRevisionNumberNumeric(String revisionNumber){
		revisionNumber = revisionNumber.trim();
		if (revisionNumber.equals("A")|| revisionNumber.equals("B")|| revisionNumber.equals("C")||
				revisionNumber.equals("D")||revisionNumber.equals("E"))
			return false;
		else
			return true;	
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "NumericRevisionNumberForIFC - When status  is IFC the revision can not be A, B, C ..... it must be 0, 1....";
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
