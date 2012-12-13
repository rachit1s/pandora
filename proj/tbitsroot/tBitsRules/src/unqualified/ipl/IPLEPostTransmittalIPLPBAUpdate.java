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
public class IPLEPostTransmittalIPLPBAUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates the DCPL business area post SEPCO transmittal.";
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
		if (IPLUtils.isIPLEDCR(dcrBA) && transmittalType.equals("TransmitToSEPCO")
				&& currentBA.getSystemPrefix().trim().equals("IPLP")){	
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String ipleDTNNumber = trnIdPrefix + IPLUtils.getFormattedStringFromNumber(transmittalId);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			paramTable.put(Field.USER, "DC-IPLE");
			paramTable.put(Field.ASSIGNEE, "IPLP");
			
			//1.	DTN to SEPCO Ext is updated with IPLE DTN No
			//2.	Final Decision to SEPCO is set to value of Decision to SEPCO in IPLE_DCR
			paramTable.put(IPLUtils.DTN_TO_SEPCO_EXT, ipleDTNNumber);			
			paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());
			
			paramTable.put(Field.STATUS, "ReturnedWithDecision");
			paramTable.put(Field.DESCRIPTION, "Document returned with decision to SEPCO via letter of Transmittal number #" 
					+ ipleDTNNumber
					+ " [DTN#" + transmittalRequest.getRequestId() + "]");
		}
	}
}
