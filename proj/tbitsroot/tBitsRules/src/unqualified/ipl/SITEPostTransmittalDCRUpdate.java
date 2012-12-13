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
public class SITEPostTransmittalDCRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ipl.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates SITE DCR.";
	}

	/* (non-Javadoc)
	 * @see ipl.ITransmittalRule#getSequence()
	 */
	public double getSequence() {		
		return 1.0;
	}

	/* (non-Javadoc)
	 * @see ipl.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.Request, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId,
			Request transmittalRequest, BusinessArea currentBA,
			BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {

		if (IPLUtils.isSiteDCR(dcrBA)){	

			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String siteDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);

			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			paramTable.put(Field.USER, "DC-SITE");
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals(IPLUtils.TRANSMIT_TO_IPLE) && 
					currentBA.getSystemPrefix().trim().equals(IPLUtils.SITE)){

				paramTable.put(Field.ASSIGNEE, "IPLE");
				//1.Due Date is set to Null
				paramTable.put(Field.DUE_DATE, "");
				//2.Flow Status from IPLP is changed to Returned with Decision
				paramTable.put("FlowStatusFromIPLP", IPLUtils.RETURNED_WITH_DECISION);
				//3.DTN to IPLP is updated with SITE DTN No
				paramTable.put("DTNtoIPLP", siteDTNNumber);
				//4.Transmit to IPLE  is updated to False
				paramTable.put(IPLUtils.TRANSMIT_TO_IPLE, "false");
				
				paramTable.put (Field.DESCRIPTION,"Document returned with decision via letter of Transmital number #" 
						+ siteDTNNumber
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}

		}
	}

}
