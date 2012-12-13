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
public class VendorEleconSITEPostTransmittalTargetBAUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": target BA update post ELECON_SITE initiated transmittal.";
	}

	/* (non-Javadoc)
	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 2.2;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, 
	 * transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {
		
		if (KSKUtils.isSiteDCR(dcrBA) && currentBA.getSystemPrefix().trim().equals(KSKUtils.PHO_SYS_PREFIX)){

			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);	
			paramTable.put(Field.USER, "DC-ELECON-SITE");
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_CEC_FROM_SITE)){
//				paramTable.put(Field.DUE_DATE, "");
				paramTable.put (Field.DESCRIPTION,"Comments received from SITE via DTN #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [" + KSKUtils.DTN + "#" 
						+ transmittalRequest.getRequestId() + "]");
			}
		}
		
		if (KSKUtils.isEleconSiteDCR(dcrBA) && currentBA.getSystemPrefix().trim().equals(KSKUtils.CEC_SYS_PREFIX)){

			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);	
			paramTable.put(Field.USER, "DC-ELECON-SITE");
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_CEC_FROM_SITE)){
//				paramTable.put(Field.DUE_DATE, "");
				paramTable.put (Field.DESCRIPTION,"Comments received from SITE via DTN #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [" + KSKUtils.DTN + "#" 
						+ transmittalRequest.getRequestId() + "]");
			}
		}
	}
}
