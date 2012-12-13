/**
 * 
 */
package ipl;

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
public class AdhocIPLPPostTransmittalIPLEBAUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates the IPLE business area post IPLP transmittal.";
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
		if (IPLUtils.isAdhocIPLPDCR(dcrBA) && transmittalType.equals("TransmitToIPLE")
				&& currentBA.getSystemPrefix().trim().equals(IPLUtils.ADHOC_IPLE)){	
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String iplpDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
			
//			1.	Decision from IPLP is changed to the value of Decision to IPLE in IPLE DCR.
//			2.	IPLP Response Date is set to Null
//			3.	DTN from IPLE is updated with IPLP DTN No
			paramTable.put("DecisionFromIPLP", dcrRequest.get(IPLUtils.DECISION_TO_IPLE));
			paramTable.put("IPLPResponseDate", "");
			paramTable.put("DTNFromIPLP", iplpDTNNumber);

			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			paramTable.put(Field.USER, "DC-IPLP");
			paramTable.put(Field.ASSIGNEE, "IPLE");	
						
			paramTable.put (Field.DESCRIPTION,"Document returned to IPLE with decision from IPLP via letter of Transmital number #" 
					+ iplpDTNNumber
					+ " [DTN#" + transmittalRequest.getRequestId() + "]");
		}
	}
}
