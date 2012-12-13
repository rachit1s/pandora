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
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class SITEPostTransmittalIPLEUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ipl.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates IPLE post transmittal from SITE DCR.";
	}

	/* (non-Javadoc)
	 * @see ipl.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 1.1;
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
					currentBA.getSystemPrefix().trim().equals(IPLUtils.IPLE)){
				//1.Flow Status To SITE is changed to Returned with Decision
				paramTable.put("FlowStatusToSite", IPLUtils.RETURNED_WITH_DECISION);
				try{
					//2.Decision from SITE is changed to the value of Decision to IPLP in SITE BA
					paramTable.put("DecisionFromSite", dcrRequest.getExType("DecisiontoIPLP").getName());
				} catch (IllegalStateException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				}
				//3.SITE Response Date is set to NULL
				paramTable.put("SiteResponseDate", "");
				
				//4.DTN from SITE is set to SITE DTN No as in DTN to IPLP in SITE BA
				paramTable.put("DTNFromSite", siteDTNNumber);
				
				paramTable.put (Field.DESCRIPTION,"Document returned with decision via letter of Transmital number #" 
						+ siteDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
		}
	}
}
