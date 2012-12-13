/**
 * 
 */
package ksk;

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
public class ODNWPDIPostTransmittalTargetUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": CEC BA update post NWPDI initiated transmittal.";
	}

	/* (non-Javadoc)
	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 2.2;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {
		if (KSKUtils.isODNWPDI(dcrBA) && (businessAreaType != KSKUtils.DCR_BUSINESS_AREA)
				&& (businessAreaType != KSKUtils.DTR_BUSINESS_AREA)){

			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			paramTable.put(Field.USER, "DC-NWPDI");
			paramTable.put (Field.DESCRIPTION,"Documents received from NWPDI via DTN #" 
					+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
					+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_CEC) && currentBA.getSystemPrefix().trim().equals("CEC")){
				paramTable.put(Field.USER, "DC-NWPDI");
				paramTable.put (Field.DESCRIPTION,"Comments received from NWPDI via DTN #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
		}
	}
}
