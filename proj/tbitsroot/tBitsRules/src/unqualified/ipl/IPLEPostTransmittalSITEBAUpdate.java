/**
 * 
 */
package ipl;

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
public class IPLEPostTransmittalSITEBAUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates the SITE business area post IPLP transmittal.";
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
		
		if (IPLUtils.isIPLEDCR(dcrBA) && transmittalType.equals("TransmitToSite")
				&& currentBA.getSystemPrefix().trim().equals(IPLUtils.SITE)){	
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String iplpDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);

			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			paramTable.put(Field.USER, "DC-IPLE");
			paramTable.put(Field.ASSIGNEE, "SITE");
			
			
			//1.Due Date is set to IPLP  DTN Date + RT-SITE (based on rev)
			int slideIPLPOffset = IPLUtils.getResponseDateOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 
					10, 7, isAddRequest);//"DTNFromSEPCOExt");				
			if (slideIPLPOffset != 0){
				Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideIPLPOffset);
				paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
			}
			
			//2.Revision is updated to the values in Revision in IPLP DCR
			//3.Flow Status From IPLP is changed to Under Review
			paramTable.put("FlowStatusFromIPLP", IPLUtils.STATUS_UNDER_REVIEW);
			//4.DTN from IPLP is updated with IPLP DTN No
			paramTable.put("DTNfromIPLP", iplpDTNNumber);
						
			paramTable.put (Field.DESCRIPTION,"Document returned to IPLE with decision from IPLP via letter of Transmital number #" 
					+ iplpDTNNumber
					+ " [DTN#" + transmittalRequest.getRequestId() + "]");
		}
	}
}
