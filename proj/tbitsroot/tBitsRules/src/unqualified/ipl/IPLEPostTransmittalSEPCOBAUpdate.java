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
public class IPLEPostTransmittalSEPCOBAUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates the DCPL business area post IPLE transmittal.";
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
		if (IPLUtils.isIPLEDCR(dcrBA) && transmittalType.equals("TransmitToBHEL")
				&& currentBA.getSystemPrefix().trim().equals("BHEL")){
			
			/*1.	Due date is marked to IPLE DTN + SEPCO-RT (Based on rev)
			2.	Flow Status to IPLE is changed to Decision Received
			3.	Decision From IPLE is updated with  Decision to SEPCO in IPLE_DCR
			4.	IPLE Response Date is set to Null
			5.	DTN from IPLE is updated with IPLE DTN No*/

			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String ipleDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			paramTable.put(Field.USER, "DC-IPLE");
			paramTable.put(Field.ASSIGNEE, "BHEL");
			
			paramTable.put("IPLEResponseDate", "");
			paramTable.put(IPLUtils.DTN_FROM_IPLE_EXT, ipleDTNNumber);
			
			paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());
			
			paramTable.put(Field.STATUS, "ReturnedWithDecision");
			
			paramTable.put(Field.DESCRIPTION, "Document recieved with comments via Transmittal number #" 
					+ ipleDTNNumber
					+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			int slideOffset = 7;
			Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
			paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
		}
	}
}
