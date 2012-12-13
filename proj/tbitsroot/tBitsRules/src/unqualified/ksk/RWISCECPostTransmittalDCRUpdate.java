/**
 * 
 */
package ksk;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class RWISCECPostTransmittalDCRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": RWIS-CEC initiated post transmittal update in DCR(RWIS-CEC) business area";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 4.0;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId,  Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {
		
		if(KSKUtils.isRWISCECDCR(dcrBA) && currentBA.getSystemPrefix().trim().equals(KSKUtils.RWIS_CEC)){
			paramTable.put(Field.USER, "DC-RWIS-ERC");
			paramTable.put(Field.DUE_DATE, "");
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ZUBERI)){
				//1.	The Due date becomes NULL.
				//2.	Field â€“ â€œTransmitted to SEPCOâ€? is marked as True.
				//3.	Description is updated as â€œdocument submitted to SEPCO with comments via letter of Transmittal number #â€?.
				//4.	The actual % complete is computed based on the decision status given by DCPL.
				
				paramTable.put(KSKUtils.TRANSMIT_TO_SEPCO, KSKUtils.FALSE);
				paramTable.put(KSKUtils.TRANSMITTED_TO_SEPCO, KSKUtils.TRUE);
				paramTable.put(Field.DESCRIPTION, "Document submitted to ZUBERI with comments via letter of Transmittal number #"
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");
				//TODO: Compute the actual % complete
				//4.	The actual % complete is computed based on the decision status given by DCPL.
			}
			/*else if(transmittalType.equals(KSKUtils.TRANSMIT_RFC_VALIDATION)){
				//1.	Field â€“ â€œTransmitted RFC Validationâ€? is marked as True.
				//2.	Description is updated as â€œThe RFC file has been validated by WPCL â€“ DCPL & has been found
				//		as Correct/Incorrect (one of the two based on RFC Validation) & is transmitted via letter of Transmittal number #â€?.
				paramTable.put(KSKUtils.TRANSMIT_RFC_VALIDATION, KSKUtils.FALSE);
				paramTable.put(KSKUtils.TRANSMITTED_RFC_VALIDATION, KSKUtils.TRUE);
				paramTable.put(Field.DESCRIPTION, "The RFC file has been transmitted via letter of" +
						" Transmittal number #"	+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");

			}
			else if(transmittalType.equals(KSKUtils.TRANSMIT_AB_VALIDATION)){
				//1.	Field â€“ â€œTransmitted AsBuilt Validationâ€? is marked as True.
				//2.	Description is updated as â€œThe AsBuilt file has been validated by WPCL â€“ DCPL & has been 
				//		found as Correct/Incorrect (one of the two based on RFC Validation) & is transmitted via
				//		letter of Transmittal number #â€?.
				paramTable.put(KSKUtils.TRANSMIT_AB_VALIDATION, KSKUtils.FALSE);
				paramTable.put(KSKUtils.TRANSMITTED_AB_VALIDATION, KSKUtils.TRUE);
				paramTable.put(Field.DESCRIPTION, "The AsBuilt file has been transmitted via" +
						"letter of Transmittal number #" + trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");;
			}*/
		}

	}

}
