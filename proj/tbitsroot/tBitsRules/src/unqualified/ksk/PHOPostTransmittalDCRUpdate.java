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
public class PHOPostTransmittalDCRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates PHO BA post transmittal.";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 2.0;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {
		
		//To apply this rule, check if the transmittal process is being initiated from PHO business area
		//and the current business area is PHO.
		if (KSKUtils.isPHODCR(dcrBA) && (businessAreaType == KSKUtils.DCR_BUSINESS_AREA)){
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);	
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_CEC)){
				//Had to add the actual user's login, hence passed it in the paramtable, 
				//to be able to append it. Instead of changing the method signature and 
				//pass user separately.
				String existingUser = paramTable.get(Field.USER);
				if (existingUser != null)
					paramTable.put(Field.USER, existingUser);
				else
					paramTable.put(Field.USER, "DC-PHO");
				paramTable.put(KSKUtils.TRANSMIT_TO_CEC, false + "");
				paramTable.put(KSKUtils.TRANSMITTED_TO_CEC, true + "");
				paramTable.put(Field.STATUS, KSKUtils.STATUS_DOCUMENT_COMMENTED);
				paramTable.put (Field.DESCRIPTION,"Document released to CEC via letter of Transmital number #" 
									+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
									+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				//paramTable.put(KSKUtils.SEPCO_INCOMING_TRANSMITTAL_NO_FIELD_NAME, "S-01-" + KSKUtils.getFormattedStringFromNumber(transmittalId));
				paramTable.put(Field.DUE_DATE, "");
			}			
		}
	}
}
