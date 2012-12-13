/**
 * 
 */
package ksk;

import java.sql.Connection;
import java.util.Hashtable;

import org.jfree.util.Log;

import transbit.tbits.Helper.TBitsConstants;
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
public class VendorEleconPostTransmittalPHOBAUpdate implements ITransmittalRule {

	private static final String KSK_IRULE_DCRASSIGNEE_UPDATE_RULE_ELECON_PHO_CC = "ksk.irule.DCRAssigneeUpdateRule.EleconPHOCc";

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates the PHO business area post ELECON transmittal.";
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
		
		if (KSKUtils.isEleconDCR(dcrBA) && currentBA.getSystemPrefix().equals(KSKUtils.PHO_SYS_PREFIX)){			
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_PHO)){
				populateCommonFields(dcrRequest, paramTable);
				
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
				paramTable.put(Field.STATUS, KSKUtils.STATUS_DOCUMENT_RECEIVED);
				paramTable.put (Field.DESCRIPTION,"Document submitted for review via letter of Transmital number #"
														+ dtnNumber
														+ " [" + KSKUtils.DTN + "#" 
														+ transmittalRequest.getRequestId() + "]");
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, dtnNumber);
				
				int slideOffset = KSKUtils.getSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 3, 3, isAddRequest);
				if (slideOffset != 0){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat(TBitsConstants.API_DATE_FORMAT));
				}
				String assigneeList = DCRAssingeeUpdateRule.getAssigneeList(connection, currentBA, dcrRequest, transmittalType, isAddRequest);
				if ((assigneeList != null) && (assigneeList.trim().length() != 0))
					paramTable.put(Field.ASSIGNEE, assigneeList);
				String ccList = KSKUtils.getProperty(KSK_IRULE_DCRASSIGNEE_UPDATE_RULE_ELECON_PHO_CC);
				if ((ccList != null) && (ccList.trim().length() != 0))
					paramTable.put(Field.CC, ccList);
			}
			else if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_PHO_RFC)){
				populateCommonFields(dcrRequest, paramTable);
				
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
				paramTable.put(Field.DUE_DATE, "");	
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, dtnNumber);
				paramTable.put (Field.DESCRIPTION,"Document 'Released For Construction' via letter of Transmittal number #" 
													+ dtnNumber
													+ " [" + KSKUtils.DTN + "#" 
													+ transmittalRequest.getRequestId() + "]");
			}
			else if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_AS_BUILT)){
				populateCommonFields(dcrRequest, paramTable);
				
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
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(Field.DESCRIPTION, "Document released for 'As Built' via letter of Transmittal number #" 
														+ dtnNumber
														+ " [" + KSKUtils.DTN + "#"
														+ transmittalRequest.getRequestId() + "]");
			}
			/*else if (transmittalType.equals("TransmitToKMPCLForInfo")){
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(Field.DESCRIPTION, "Document submitted for information via letter of Transmittal number #" 
														+dtnNumber       
														//+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}*/
		}
	}

	private void populateCommonFields(Request dcrRequest,
			Hashtable<String, String> paramTable) {
		//To apply this rule, check if the transmittal process is being initiated from SEPCO business area
		//and the current business area is PHO.
		paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
		paramTable.put(Field.CATEGORY, dcrRequest.getCategoryId().getName());
		paramTable.put(Field.REQUEST_TYPE, dcrRequest.getRequestTypeId().getName());
		paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());
		paramTable.put(Field.USER, "DC-ELECON");
	}

}
