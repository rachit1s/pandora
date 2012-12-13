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
public class ODSEPCOPostTransmittalDCRUpdate implements ITransmittalRule {

	public static final String TRANSMIT_FROM_SEPCO_SITE_TO_WPCL = "TransmitFromSepcoSiteToWPCL";

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
			paramTable.put(Field.DUE_DATE, KSKUtils.EMPTY_STRING);
			paramTable.put(Field.NOTIFY, false + "");			
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_WPCL)){			
				paramTable.put (Field.DESCRIPTION,"Document submitted to WPCL for review via letter of Transmital number #" 
									+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
									+ " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}
			
			if (transmittalType.equals(TRANSMIT_FROM_SEPCO_SITE_TO_WPCL)){	
				paramTable.put(KSKUtils.TRANSMIT_TO_WPCL, false + "");
				paramTable.put(KSKUtils.TRANSMITTED_TO_WPCL, true + "");		
				paramTable.put (Field.DESCRIPTION,"Document from SEPCO-SITE submitted to WPCL for review via letter of Transmital number #" 
									+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
									+ " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}
			else if (transmittalType.equals("TransmitToCEC")){
				paramTable.put (Field.DESCRIPTION,"Document sent to CEC via letter of Transmital number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
			}
			else if (transmittalType.equals("TransmitToDCPL")){
				paramTable.put (Field.DESCRIPTION,"Document sent to DCPL via letter of Transmital number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				//paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
			}
			else if (transmittalType.equals("TransmitToSITE")){
				paramTable.put (Field.DESCRIPTION,"Document sent to SITE via letter of Transmital number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				//paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
			}
			else if (transmittalType.equals("TransmitToNWEPDI")){
				paramTable.put (Field.DESCRIPTION,"Document sent to NWEPDI via letter of Transmital number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				//paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
			}			
		}
	}
}
