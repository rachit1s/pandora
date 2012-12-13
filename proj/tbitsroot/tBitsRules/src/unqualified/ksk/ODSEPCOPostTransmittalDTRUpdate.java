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
public class ODSEPCOPostTransmittalDTRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName() + ": Updates DTR business area post-transmittal for SEPCO initiated transmittal.";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 1.2;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, 
			int businessAreaType, boolean isAddRequest)
			throws TBitsException {
		
		if ((businessAreaType == KSKUtils.DTR_BUSINESS_AREA) && KSKUtils.isODSEPCODCR(dcrBA)){	
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, KSKUtils.FALSE);
			paramTable.put(KSKUtils.SYSTEM_DEFINITION, dcrRequest.get(KSKUtils.SYSTEM_DEFINITION));
			
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_WPCL)){
				paramTable.put(Field.USER, "SEPCO,WPCL");
				paramTable.put(Field.ASSIGNEE, "WPCL");
				paramTable.put (Field.DESCRIPTION,"Document submitted to WPCL for review via letter of Transmital number #" 
														+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
			else if (transmittalType.equals(ODSEPCOPostTransmittalDCRUpdate.TRANSMIT_FROM_SEPCO_SITE_TO_WPCL)){
				paramTable.put(Field.USER, "SEPCO,WPCL");
				paramTable.put(Field.ASSIGNEE, "WPCL");
				paramTable.put (Field.DESCRIPTION,"Document from SEPCO-SITE submitted to WPCL-HO for review via letter of Transmital number #" 
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
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
			}
			else if (transmittalType.equals("TransmitToSITE")){
				paramTable.put (Field.DESCRIPTION,"Document sent to SITE via letter of Transmital number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
			}
			else if (transmittalType.equals("TransmitToNWEPDI")){
				paramTable.put (Field.DESCRIPTION,"Document sent to NWEPDI via letter of Transmital number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
			}
		}
	} 
}
