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
public class OHSEPCOPostTransmittalDCRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " : Updates the SEPCO(DCR) business area post-transmittal.";
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
		
		if ((businessAreaType == KSKUtils.DCR_BUSINESS_AREA) && KSKUtils.isODSEPCODCR(dcrBA)){
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			paramTable.put(Field.USER, "DC-SEPCO");
			//paramTable.put(Field.LOGGER, "DC-SEPCO");
			paramTable.put(Field.DUE_DATE, KSKUtils.EMPTY_STRING);
			paramTable.put(Field.NOTIFY, false + "");			
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_WPCL)){
				//paramTable.put(Field.STATUS, KSKUtils.STATUS_DOCUMENT_RECEIVED);
				//paramTable.put(KSKUtils.TRANSMIT_TO_WPCL, false + "");
				//paramTable.put(KSKUtils.TRANSMITTED_TO_WPCL, true + "");				
				paramTable.put (Field.DESCRIPTION,"Document submitted to WPCL for review via letter of Transmital number #" 
									+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
									+ " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}
			/*else if (transmittalType.equals(KSKUtils.TRANSMIT_FOR_RFC)){
				paramTable.put(KSKUtils.TRANSMIT_FOR_RFC, KSKUtils.FALSE);
				paramTable.put(KSKUtils.STATUS_RFC_RECEIVED, KSKUtils.TRUE);
				paramTable.put(Field.STATUS, KSKUtils.STATUS_A1);
				paramTable.put (Field.DESCRIPTION,"Document 'Released For Construction' via letter of Transmittal number #" 
									+ KSKUtils.getFormattedStringFromNumber(transmittalId)
									+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
			else if (transmittalType.equals(KSKUtils.TRANSMIT_FOR_AS_BUILT)){
				 paramTable.put(KSKUtils.TRANSMIT_FOR_AS_BUILT, KSKUtils.FALSE);
				 paramTable.put("TransmittedAsBuilt", KSKUtils.TRUE);
				 paramTable.put(Field.STATUS, KSKUtils.STATUS_AB_AS_BUILT);
				 paramTable.put(Field.DESCRIPTION, "Document released for 'As Built' via letter of Transmittal number #" 
						 			+ KSKUtils.getFormattedStringFromNumber(transmittalId)
						 			+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}*/
		}
	}
}
