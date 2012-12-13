/**
 * 
 */
package tatapower;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class SetFieldsCheckMatRecon implements IRule {
	
	private static final String OR_RETURN_DATE = "OR_ReturnDate";

	private static final String TATAPOWER_SYS_PREFIX_MTRL_RECON = "tatapower.sys_prefix_MtrlRecon";
	
	//Category
	private static final String INCOMING_MATERIAL = "Incoming_Material";
	private static final String OUTGOING_NON_RETURNABLE_MATERIAL = "Outgoing_Non_Returnable_Material";
	private static final String OUTGOING_RETURNABLE_MATERIAL = "Outgoing_Returnable_Material";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		ruleResult.setCanContinue(true);
		String parentString = TataPowerUtils.getProperty(TATAPOWER_SYS_PREFIX_MTRL_RECON);
		boolean isApplicableBA = TataPowerUtils.isExistsInString(parentString, ba.getSystemPrefix());
		
		if (isApplicableBA){
			int aSystemId = ba.getSystemId();
			Type categoryId = currentRequest.getCategoryId();
			String categoryName = categoryId.getName();
			if (!categoryName.equals(INCOMING_MATERIAL)){
				if (hasValue(aSystemId, "I_GRN", extendedFields)||
						hasValue(aSystemId, "I_IncomingDate", extendedFields)||
						hasValue(aSystemId, "I_InvoiceNo", extendedFields)||
						hasValue(aSystemId, "I_MaterialDescription", extendedFields)||
						hasValue(aSystemId, "I_PartyName", extendedFields)||
						hasValue(aSystemId, "I_Quantity", extendedFields)||
						hasValue(aSystemId, "I_ValueOfGoods", extendedFields)){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("If 'Material Type' is not 'Incoming Material' cannot set " +
					"any field in the group 'Incoming Material Details'.");
				}		
			}
			if ((!categoryName.equals(OUTGOING_NON_RETURNABLE_MATERIAL)) &&
					(!categoryName.equals(OUTGOING_RETURNABLE_MATERIAL))){
				if (hasValue(aSystemId, "O_GatePassNo", extendedFields)||
						hasValue(aSystemId, "O_MaterialDescription", extendedFields)||
						hasValue(aSystemId, "O_OutgoingDate", extendedFields)||
						hasValue(aSystemId, "O_PartyName", extendedFields)||
						hasValue(aSystemId, "O_Quantity", extendedFields)){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("If 'Material Type' is not 'Outgoing Returnable Material' or 'Outgoing Non Returnable Material' cannot set " +
					"any field in the group 'Outgoing Material Details'.");
				}
			}
			if (!categoryName.equals(OUTGOING_RETURNABLE_MATERIAL)){				
				if (hasValue(aSystemId, OR_RETURN_DATE, extendedFields)){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("If 'Material Type' is not 'Outgoing Non Returnable Material' cannot set '"  + OR_RETURN_DATE + "'.");
				}
			}
		}
		
		return ruleResult;
	}

	/**
	 * @param aSystemId
	 * @param extendedFields
	 */
	private boolean hasValue(int aSystemId, String fieldName,
			Hashtable<Field, RequestEx> extendedFields) {
		Field extField;
		try {
			extField = Field.lookupBySystemIdAndFieldName(aSystemId, fieldName);			
			if (extField != null){
				RequestEx reqEx = extendedFields.get(extField);
				if (reqEx != null){
					int extFieldDataType = extField.getDataTypeId();
					switch (extFieldDataType){
					case DataType.DATE:{
						if (reqEx.getDateTimeValue()!= null)
							return true;
					}
					case DataType.REAL:{
						if (reqEx.getRealValue() > 0)
							return true;
					}
					case DataType.STRING:{
						if ((reqEx.getVarcharValue() != null) && (!reqEx.getVarcharValue().trim().equals("")))
							return true;
					}
					default : return false;
					}
				}	
			}			
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - Checks if appropriate fields are set based on the category type.";
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
