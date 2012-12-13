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
public class SEPCOPostTransmittalDTRUpdate implements ITransmittalRule {

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
		
		if ((businessAreaType == KSKUtils.DTR_BUSINESS_AREA) && KSKUtils.isSEPCODCR(dcrBA)){	
			
//			int dcrSystemId = dcrBA.getSystemId();
//			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
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
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, KSKUtils.FALSE);
			
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_WPCL)){
				paramTable.put(Field.USER, "SEPCO,WPCL");
				paramTable.put(Field.ASSIGNEE, "WPCL");
				paramTable.put (Field.DESCRIPTION,"Document submitted to WPCL for review via letter of Transmital number #" 
														+ dtnNumber
														//+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
				paramTable.put(Field.CATEGORY, dcrRequest.getCategoryId().getName());
				paramTable.put(Field.REQUEST_TYPE, dcrRequest.getRequestTypeId().getName());
				paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());	
				paramTable.put(KSKUtils.SYSTEM_DEFINITION, dcrRequest.get(KSKUtils.SYSTEM_DEFINITION));
				
				int slideOffset = KSKUtils.getSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 14, 10, isAddRequest);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
			}
			else if (transmittalType.equals(KSKUtils.TRANSMIT_FOR_RFC)){
				paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
				paramTable.put(Field.CATEGORY, dcrRequest.getCategoryId().getName());
				paramTable.put(Field.REQUEST_TYPE, dcrRequest.getRequestTypeId().getName());
				paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());
				paramTable.put(Field.USER, "SEPCO,WPCL");
				paramTable.put(Field.ASSIGNEE, "WPCL");
				paramTable.put(Field.DUE_DATE, "");		
				paramTable.put(KSKUtils.SYSTEM_DEFINITION, dcrRequest.get(KSKUtils.SYSTEM_DEFINITION));
				paramTable.put (Field.DESCRIPTION,"Document 'Released For Construction' via letter of Transmittal number #" 
													+ dtnNumber
													//+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
													+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
			else if (transmittalType.equals(KSKUtils.TRANSMIT_FOR_AS_BUILT)){
				paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
				paramTable.put(Field.CATEGORY, dcrRequest.getCategoryId().getName());
				paramTable.put(Field.REQUEST_TYPE, dcrRequest.getRequestTypeId().getName());
				paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());
				paramTable.put(Field.USER, "SEPCO,WPCL");
				paramTable.put(Field.ASSIGNEE, "WPCL");
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(KSKUtils.SYSTEM_DEFINITION, dcrRequest.get(KSKUtils.SYSTEM_DEFINITION));
				paramTable.put(Field.DESCRIPTION, "Document released for 'As Built' via letter of Transmittal number #" 
														+ dtnNumber
														//+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
			else if (transmittalType.equals("TransmitToKMPCLForInfo")){
				paramTable.put(Field.DESCRIPTION, "Document transmitted for information via letter of Transmittal number #" 
														+ dtnNumber
														//+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");				
				paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
				paramTable.put(Field.CATEGORY, dcrRequest.getCategoryId().getName());
				paramTable.put(Field.REQUEST_TYPE, dcrRequest.getRequestTypeId().getName());
				paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());
				paramTable.put(Field.USER, "SEPCO,WPCL");
				paramTable.put(Field.ASSIGNEE, "WPCL");
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(KSKUtils.SYSTEM_DEFINITION, dcrRequest.get(KSKUtils.SYSTEM_DEFINITION));
			}
		}
	} 
}
