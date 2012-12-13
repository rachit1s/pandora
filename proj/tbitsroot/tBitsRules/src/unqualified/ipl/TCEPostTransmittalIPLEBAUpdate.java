/**
 * 
 */
package ipl;

import java.sql.Connection;
import java.util.Hashtable;

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
public class TCEPostTransmittalIPLEBAUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates the IPLE business area post TCE transmittal.";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 1.3;
	}

	/* (non-Javadoc)
	 * @see ipl.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, 
			int businessAreaType, boolean isAddRequest)
			throws TBitsException {
		if (IPLUtils.isTCEDCR(dcrBA) && 
				transmittalType.equals("TransmitToIPLE") && 
				currentBA.getSystemPrefix().equals(IPLUtils.IPLE)){
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String dcplDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			paramTable.put(Field.USER, "DC-TCE");
			paramTable.put(Field.ASSIGNEE, "IPLE");
			
			//1.	Flow Status To DCPL is changed to Decision Received
			//2.	Decision from DCPL is changed to the value of Decision to IPLE in DCPL_DCR
			//3.	DCPL Response Date is set to NULL
			//4.	DTN from DCPL is set to DCPL DTN			
			paramTable.put(IPLUtils.FLOW_STATUS_TO_DCPL, IPLUtils.RETURNED_WITH_DECISION);
			Type dcplDecisionType = null;
			try {
				dcplDecisionType = dcrRequest.getExType(IPLUtils.DECISION_TO_IPLE);
			} catch (IllegalStateException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			}
			paramTable.put(IPLUtils.DECISION_FROM_DCPL, dcplDecisionType.getName());//dcrRequest.get(IPLUtils.DECISION_TO_IPLE));
			paramTable.put(IPLUtils.DCPL_RESPONSE_DATE, "");
			paramTable.put(IPLUtils.DTN_FROM_DCPL, dcplDTNNumber);
			paramTable.put (Field.DESCRIPTION,"Document returned to IPLE with decision from TCE via letter of Transmital number #" 
					+ dcplDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]");
		}
	}
}
