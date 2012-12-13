/**
 * 
 */
package ncc;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.Timestamp;
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
public class NCCUnControlledDocumentsNumberGeneration implements IRule {

	private static final String DELIMETER_HYPHEN = "-";	
	private static final String PROJECT_CODE = "NPT10109";
	private static final String NUMBER_GENERATION_BA_LIST = "transbit.tbits.transmittal.ncc.UnControlledNumberGenerationBAList";

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
			String existingNCCNo = currentRequest.getExString(NCCTransmittalUtils.DRAWING_NO);
			if((existingNCCNo != null) && (!existingNCCNo.trim().equals(""))){
				ruleResult.setMessage("NCC Number already exists and hence not executing this rule.");
				ruleResult.setCanContinue(true);
				return ruleResult;
			}

			String nccNumber = PROJECT_CODE;
			if (currentRequest != null){
				String contractReferenece = currentRequest.get("ContractReference");
				if (!contractReferenece.trim().equals(""))
					nccNumber = nccNumber + DELIMETER_HYPHEN + contractReferenece;
				else {
					ruleResult.setMessage("Did not generate NCC number as, no 'Contract Reference' could be retrieved.");
					return ruleResult;
				}
			}			

			Type documentCode = currentRequest.getRequestTypeId();
			if ((documentCode != null) && (!documentCode.getName().equals("None"))){
				nccNumber = nccNumber + DELIMETER_HYPHEN + documentCode.getName();
			}
			else{
				ruleResult.setMessage("Document Code was not set appropriate value or set to 'None', " +
				"hence did not generate NCC number.");
				ruleResult.setCanContinue(true);
				return ruleResult;
			}
						
			String currentYear = getCurrentFinancialYearString(DELIMETER_HYPHEN);
			nccNumber = nccNumber + DELIMETER_HYPHEN + currentYear;
			
			int maxId = NCCTransmittalUtils.getMaxIdByName(connection, ba.getSystemId(), nccNumber);
			if (maxId != -1){
				nccNumber = nccNumber + DELIMETER_HYPHEN + NCCTransmittalUtils.getFormattedStringFromNumber(maxId);
				currentRequest.setExString(NCCTransmittalUtils.DRAWING_NO, nccNumber);
				ruleResult.setCanContinue(true);
			}
			else{
				ruleResult.setMessage("Did not generate un-controlled NCC number, as maximum id could not be retrieved.");
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
		return this.getClass().getSimpleName() + "- Generates the un-controlled NCC number based on  the Control Referenece, Document Code, " +
				"financial year.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		return 0;
	}
	
	private static String getCurrentFinancialYearString(String delimeter)
	{
		Calendar ndd = Calendar.getInstance() ;
		int currMonth = ndd.get(Calendar.MONTH) ;
		
		if( currMonth < Calendar.APRIL )
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, -1);
			Timestamp nowTs = new Timestamp(ndd.getTimeInMillis()) ;
			Timestamp otherTs = new Timestamp(other.getTimeInMillis()) ;
			return otherTs.toCustomFormat("yy") + "-" + nowTs.toCustomFormat("yy") ;
		}
		else
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, 1);
			Timestamp nowTs = new Timestamp(ndd.getTimeInMillis()) ;
			Timestamp otherTs = new Timestamp(other.getTimeInMillis()) ;
			return nowTs.toCustomFormat("yy") + "-" + otherTs.toCustomFormat("yy") ;
		}		
	}

	
}
