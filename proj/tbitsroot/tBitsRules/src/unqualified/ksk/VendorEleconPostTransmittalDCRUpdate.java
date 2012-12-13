/**
 * 
 */
package ksk;

import java.sql.Connection;
import java.util.Hashtable;

import org.jfree.util.Log;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class VendorEleconPostTransmittalDCRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " : Updates the ELECON(DCR) business area post-transmittal.";
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
	public void process(Connection connection, int transmittalId, Request transmittalRequest, BusinessArea currentBA,
			BusinessArea dcrBA, Request dcrRequest, Hashtable<String, String> paramTable, String transmittalType, 
			int businessAreaType, boolean isAddRequest) throws TBitsException {
		
		if ((businessAreaType == KSKUtils.DCR_BUSINESS_AREA) && KSKUtils.isEleconDCR(dcrBA)){
			
			String dtnNumber = "";
					
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_PHO)){
				try {
					dtnNumber = transmittalRequest.getExString("DTNNumber");
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Log.warn("Error occurred while copying newly created DTN number field from transmittal business area.");
				} catch (DatabaseException e) {
					e.printStackTrace();
					Log.warn("Error occurred while copying newly created DTN number field from transmittal business area.");
				}	
				
				populateCommonFields(paramTable);
				
				Type statusType = dcrRequest.getStatusId();
				if ((statusType != null) && ((!statusType.getName().equals("A2Approved")) 
												&& (!statusType.getName().equals("A1ReleaseForConstruction"))))
					paramTable.put(Field.STATUS, KSKUtils.STATUS_DOCUMENT_RECEIVED);
				
				paramTable.put(KSKUtils.TRANSMIT_TO_WPCL, false + "");
				paramTable.put(KSKUtils.TRANSMITTED_TO_WPCL, true + "");				
				paramTable.put (Field.DESCRIPTION,"Document submitted for review via letter of Transmital number #" 
									+ dtnNumber
									+ " [" + KSKUtils.DTN + "#"
									+ transmittalRequest.getRequestId() + "]");		
			}
			else if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_PHO_RFC)){
				try {
					dtnNumber = transmittalRequest.getExString("DTNNumber");
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Log.warn("Error occurred while copying newly created DTN number field from transmittal business area.");
				} catch (DatabaseException e) {
					e.printStackTrace();
					Log.warn("Error occurred while copying newly created DTN number field from transmittal business area.");
				}	
				
				populateCommonFields(paramTable);
				
				paramTable.put(KSKUtils.TRANSMIT_FOR_RFC, KSKUtils.FALSE);
				paramTable.put(KSKUtils.STATUS_RFC_RECEIVED, KSKUtils.TRUE);
				paramTable.put(Field.STATUS, KSKUtils.STATUS_A1);
				paramTable.put (Field.DESCRIPTION,"Document 'Released For Construction' via letter of Transmittal number #" 
									+ dtnNumber
									+ " [" + KSKUtils.DTN + "#" + dtnNumber
									+ transmittalRequest.getRequestId() + "]");
			}
			else if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_AS_BUILT)){
				try {
					dtnNumber = transmittalRequest.getExString("DTNNumber");
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Log.warn("Error occurred while copying newly created DTN number field from transmittal business area.");
				} catch (DatabaseException e) {
					e.printStackTrace();
					Log.warn("Error occurred while copying newly created DTN number field from transmittal business area.");
				}	
				
				populateCommonFields(paramTable);
				
				 paramTable.put(KSKUtils.TRANSMIT_FOR_AS_BUILT, KSKUtils.FALSE);
				 paramTable.put("TransmittedAsBuilt", KSKUtils.TRUE);
				 paramTable.put(Field.STATUS, KSKUtils.STATUS_AB_AS_BUILT);
				 paramTable.put(Field.DESCRIPTION, "Document released for 'As Built' via letter of Transmittal number #" 
						 			+ dtnNumber
						 			+ " [" + KSKUtils.DTN + "#" + dtnNumber
									+ transmittalRequest.getRequestId() + "]");
			}
			/*else if (transmittalType.equals("TransmitToKMPCLForInfo")){
				paramTable.put(Field.DESCRIPTION, "Document transmitted for information via letter of Transmittal number #" 
														+ dtnNumber
														//+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(Field.STATUS, KSKUtils.STATUS_A6);
				paramTable.put("TransmitForInformation", KSKUtils.FALSE);
				paramTable.put("TransmittedForInformation", KSKUtils.TRUE);
			}*/
		}
	}

	private void populateCommonFields(Hashtable<String, String> paramTable) {
		paramTable.put(Field.USER, "DC-ELECON");
		paramTable.put(Field.DUE_DATE, KSKUtils.EMPTY_STRING);
		paramTable.put(Field.NOTIFY, false + "");
	}
}
