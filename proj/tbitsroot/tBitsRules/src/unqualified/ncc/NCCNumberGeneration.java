/**
 * 
 */
package ncc;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author lokesh
 *
 */
public class NCCNumberGeneration implements IRule {

	private static final String DELIMETER_HYPHEN = "-";	
	private static final String PROJECT_CODE = "NPT10109";
	private static final String NUMBER_GENERATION_BA_LIST = "transbit.tbits.transmittal.ncc.NumberGenerationBAList";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		
		return handleExecute(connection, ba, currentRequest, currentRequest, Source, user, isAddRequest);
	}

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		return handleExecute(connection, ba, currentRequest, currentRequest, Source, user, isAddRequest);
	}
	
	private RuleResult handleExecute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		//		Automatic generation of NCC No (with help of KKS code & DocumentCode)
		//		   -Project Code-NPT10109
		//		   -Function-CV(Civil) / ME (Mechanical) / EI (Elec or CnI) / PR (Process) 
		//		   -KKS Code-AAA if thats not available then AA-if this is also not available then A
		//		   -Document Code-AA
		//		   -Serial No-NNNN
		RuleResult ruleResult = new RuleResult();
		String baList = PropertiesHandler.getProperty(NUMBER_GENERATION_BA_LIST);
		if((baList == null) || (!NCCTransmittalUtils.isExistsInString(baList, ba.getSystemPrefix()))){
			ruleResult.setMessage("Not applicable to the business area: " + ba.getSystemPrefix());
			ruleResult.setCanContinue(true);
			return ruleResult;
		}

		//if (!isAddRequest){

		try {
			String existingNCCNo = currentRequest.getExString(NCCTransmittalUtils.NCC_NO_FIELD_NAME);
			if((existingNCCNo != null) && (!existingNCCNo.trim().equals(""))){
				ruleResult.setMessage("NCC Number already exists and hence not executing this rule.");
				ruleResult.setCanContinue(true);
				return ruleResult;
			}

			String nccNumber = PROJECT_CODE;
			if (currentRequest != null){
				String disciplineCode = currentRequest.getCategoryId().getDescription();
				if (!disciplineCode.trim().equals(""))
					nccNumber = nccNumber + DELIMETER_HYPHEN + disciplineCode;
				else {
					ruleResult.setMessage("Did not generate NCC number as, no discipline code could be retrieved.");
					return ruleResult;
				}
			}

			Type systemCode = currentRequest.getExType(NCCTransmittalUtils.SYSTEM_CODE);
			Type equipmentCode = currentRequest.getExType(NCCTransmittalUtils.EQUIPMENT_CODE);
			Type componentCode = currentRequest.getExType(NCCTransmittalUtils.COMPONENT_CODE);

			if ((componentCode != null) && (!componentCode.getName().equals("None"))){
				nccNumber = nccNumber + DELIMETER_HYPHEN + componentCode.getName();
			}
			else if ((equipmentCode != null) && (!equipmentCode.getName().equals("None"))){
				nccNumber = nccNumber + DELIMETER_HYPHEN + equipmentCode.getName();
			}
			else if ((systemCode != null) && (!systemCode.getName().equals("None"))){
				nccNumber = nccNumber + DELIMETER_HYPHEN + systemCode.getName();
			}
			else{
				ruleResult.setMessage("All the fields in KKS Numbering System  group were not " +
				"set appropriate value or set to 'None', hence did not generate NCC number.");
				ruleResult.setCanContinue(true);
				return ruleResult;
			}

			Type documentCode = currentRequest.getExType(NCCTransmittalUtils.DOCUMENT_CODE);
			if ((documentCode != null) && (!documentCode.getName().equals("None"))){
				nccNumber = nccNumber + DELIMETER_HYPHEN + documentCode.getName();
			}
			else{
				ruleResult.setMessage("Document Code was not set appropriate value or set to 'None', " +
				"hence did not generate NCC number.");
				ruleResult.setCanContinue(true);
				return ruleResult;
			}
			int maxId = NCCTransmittalUtils.getMaxIdByName(connection, ba.getSystemId(), nccNumber);
			if (maxId != -1){
				nccNumber = nccNumber + DELIMETER_HYPHEN + NCCTransmittalUtils.getFormattedStringFromNumber(maxId);
				currentRequest.setExString(NCCTransmittalUtils.NCC_NO_FIELD_NAME, nccNumber);
				ruleResult.setCanContinue(true);
			}
			else{
				ruleResult.setMessage("Did not generate NCC number, as maximum id could not be retrieved.");
				ruleResult.setCanContinue(true);
				return ruleResult;
			}

		} catch (DatabaseException e) {
			e.printStackTrace();
			ruleResult.setCanContinue(true);
			ruleResult.setSuccessful(false);
			ruleResult.setMessage("Could not generate NCC number due to database exception: \n" + e.getMessage());
		}
		//}
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	
	public String getName() {
		return this.getClass().getSimpleName() + "- Generates the NCC number based on  the System Code, Equipment Unit Code, " +
				"Component Code and the Discipline type.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		return 0;
	}
	
}
