/**
 * 
 */
package ksk;

import java.sql.Connection;
import java.util.Hashtable;

import ksk.ITransmittalRule;
import ksk.KSKUtils;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class VendorEleconSITEPostTransmittalDCRUpdate implements ITransmittalRule {

	public String getName() {
		return this.getClass().getSimpleName() + ": Post transmittal DCR update in Elecon_SITE initiated transmittal process.";
	}


	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 3.0;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {
		if(KSKUtils.isSiteDCR(dcrBA) && currentBA.getSystemPrefix().trim().equals(KSKUtils.SITE_SYS_PREFIX)){
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String existingUser = paramTable.get(Field.USER);
			if (existingUser != null)
				paramTable.put(Field.USER, existingUser);
			else
				paramTable.put(Field.USER, "DC-ELECON-SITE");
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_CEC_FROM_SITE)){
				paramTable.put(KSKUtils.TRANSMIT_TO_CEC, KSKUtils.FALSE);
				paramTable.put(KSKUtils.TRANSMITTED_TO_CEC, KSKUtils.TRUE);
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(Field.STATUS, KSKUtils.STATUS_DOCUMENT_COMMENTED);
				paramTable.put(Field.DESCRIPTION, "Document sent via letter  of Transmittal number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [" + KSKUtils.DTN + "#" 
						+ transmittalRequest.getRequestId() + "]");
			}
		}	
	}

}
