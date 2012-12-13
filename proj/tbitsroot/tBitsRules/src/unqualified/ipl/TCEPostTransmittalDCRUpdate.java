/**
 * 
 */
package ipl;

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
public class TCEPostTransmittalDCRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " : Updates the TCE business area post-transmittal.";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 1.1;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, java.util.Hashtable)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest, BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, int businessAreaType, boolean isAddRequest)
			throws TBitsException {
		
		if ((businessAreaType == IPLUtils.DCR_BUSINESS_AREA) && IPLUtils.isTCEDCR(currentBA)){
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			paramTable.put(Field.USER, "DC-TCE");
			paramTable.put(Field.DUE_DATE, IPLUtils.EMPTY_STRING);
			paramTable.put(Field.NOTIFY, false + "");			
			
			if (transmittalType.equals("TransmitToIPLE")){
				String dcplDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
				
				paramTable.put(IPLUtils.DTN_TO_IPLE, dcplDTNNumber);
				
				paramTable.put("TransmitToIPLE", false + "");						
				
				paramTable.put (Field.DESCRIPTION,"Document returned with decision to IPLE via letter of Transmital number #" 
									+ dcplDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}			
		}
	}
}
