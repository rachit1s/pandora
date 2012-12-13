/**
 * 
 */
package ksk;

import java.sql.Connection;
import java.util.Hashtable;

import org.jfree.util.Log;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class ZuberiPostTransmittalPHOBAUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates the RWIS business area post ZUBERI transmittal.";
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
		
		if (KSKUtils.isZUBERIDCR(dcrBA) && currentBA.getSystemPrefix().equals(KSKUtils.RWIS)){
			
			//To apply this rule, check if the transmittal process is being initiated from SEPCO business area
			//and the current business area is PHO.
			paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
			paramTable.put(Field.CATEGORY, dcrRequest.getCategoryId().getName());
			paramTable.put(Field.REQUEST_TYPE, dcrRequest.getRequestTypeId().getName());
			paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());
			paramTable.put(Field.USER, "DC-ZUBERI");

			String dtnNumber = "";
			try {
				dtnNumber = transmittalRequest.getExString("DTNNumber");
			} catch (IllegalStateException e) {
				e.printStackTrace();
				Log.warn("Error occurred while copying newly created DTN number field from transmittal business area.");
			} catch (DatabaseException e) {
				e.printStackTrace();
				Log.warn("Error occurred while copying newly created DTN number field from transmittal business area.");
			}	
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_RWIS_PHO)){
				paramTable.put(Field.STATUS, KSKUtils.STATUS_DOCUMENT_RECEIVED);
				paramTable.put (Field.DESCRIPTION,"Document submitted to KMPCL-RWIS for review via letter of Transmital number #" 
														+ dtnNumber
														+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, dtnNumber);
				
				int slideOffset = KSKUtils.getSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 4, 4, isAddRequest);
				if (slideOffset != 0){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
			}
			else if (transmittalType.equals(KSKUtils.TRANSMIT_TO_RWIS_PHO_RFC)){
//				paramTable.put(Field.ASSIGNEE, "WPCL");
				paramTable.put(Field.DUE_DATE, "");	
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, "Z-RWIS-01-" + KSKUtils.getFormattedStringFromNumber(transmittalId));
				paramTable.put (Field.DESCRIPTION,"Document 'Released For Construction' via letter of Transmittal number #" 
														+ dtnNumber//+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");
			}
			/*else if (transmittalType.equals(KSKUtils.TRANSMIT_FOR_AS_BUILT)){
//				paramTable.put(Field.ASSIGNEE, "WPCL");
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(Field.DESCRIPTION, "Document released for 'As Built' via letter of Transmittal number #" 
														+ dtnNumber //+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");
			}
			else if (transmittalType.equals("TransmitToKMPCLForInfo")){
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(Field.DESCRIPTION, "Document submitted for information via letter of Transmittal number #" 
														+dtnNumber   
														+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");
			}*/
		}
	}

}
