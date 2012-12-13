/**
 * 
 */
package ksk;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class ODSEPCOPostTransmittalTargetBAUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates the PHO business area post SEPCO transmittal.";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 1.3;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, 
			int businessAreaType, boolean isAddRequest)
			throws TBitsException {		
		
		if (KSKUtils.isODSEPCODCR(dcrBA)){
			
			//To apply this rule, check if the transmittal process is being initiated from SEPCO business area
			//and the current business area a particular target BA.
			paramTable.put(Field.USER, "DC-SEPCO");
			paramTable.put(KSKUtils.SYSTEM_DEFINITION, dcrRequest.get(KSKUtils.SYSTEM_DEFINITION));
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_WPCL)){
				if (currentBA.getSystemPrefix().equals("ODPHO")){
					paramTable.put (Field.DESCRIPTION,"Document submitted to WPCL for review via letter of Transmital number #" 
							+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
							+ " [DTN#" + transmittalRequest.getRequestId() + "]");
					paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));

					int slideOffset = KSKUtils.getSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 7, 5, isAddRequest);
					if (slideOffset != 0){
						Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, slideOffset);
						paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
					}
				}
				else if (currentBA.getSystemPrefix().equals("ODCEC")){
					paramTable.put (Field.DESCRIPTION,"Document submitted to WPCL for review via letter of Transmital number #" 
							+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
							+ " [DTN#" + transmittalRequest.getRequestId() + "]");
					paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));

					/*int slideOffset = KSKUtils.getSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 7, 5, isAddRequest);
					if (slideOffset != 0){
						Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, slideOffset);
						paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
					}*/
				}
				else if (currentBA.getSystemPrefix().equals("ODDCPL")){
					paramTable.put (Field.DESCRIPTION,"Document submitted to WPCL for review via letter of Transmital number #" 
							+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
							+ " [DTN#" + transmittalRequest.getRequestId() + "]");
					paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));

					/*int slideOffset = KSKUtils.getSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 7, 5, isAddRequest);
					if (slideOffset != 0){
						Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, slideOffset);
						paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
					}*/
				}
				if (currentBA.getSystemPrefix().equals("ODSITE")){
					paramTable.put (Field.DESCRIPTION,"Document submitted to WPCL for review via letter of Transmital number #" 
							+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
							+ " [DTN#" + transmittalRequest.getRequestId() + "]");
					paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));

					/*int slideOffset = KSKUtils.getSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 7, 5, isAddRequest);
					if (slideOffset != 0){
						Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, slideOffset);
						paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
					}*/
				}				
			}
			else if (transmittalType.equals(ODSEPCOPostTransmittalDCRUpdate.TRANSMIT_FROM_SEPCO_SITE_TO_WPCL) && currentBA.getSystemPrefix().equals("ODPHO")){
				paramTable.put (Field.DESCRIPTION,"Document submitted by SEPCO-SITE to WPCL-HO for review via letter of Transmital number #" 
														+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
				
				int slideOffset = KSKUtils.getSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 7, 5, isAddRequest);
				if (slideOffset != 0){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
			}
			else if (transmittalType.equals("TransmitToCEC") && currentBA.getSystemPrefix().equals("ODCEC")){
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
			else if (transmittalType.equals("TransmitToSITE") && currentBA.getSystemPrefix().equals("ODSITE")){
				paramTable.put (Field.DESCRIPTION,"Document sent to SITE via letter of Transmital number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
			}
			else if (transmittalType.equals("TransmitToNWEPDI") && currentBA.getSystemPrefix().equals("ODNWEPDI")){
				paramTable.put (Field.DESCRIPTION,"Document sent to NWEPDI via letter of Transmital number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
			}
		}
	}
}
