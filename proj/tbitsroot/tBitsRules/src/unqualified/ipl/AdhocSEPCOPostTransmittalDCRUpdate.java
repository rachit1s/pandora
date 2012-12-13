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
public class AdhocSEPCOPostTransmittalDCRUpdate implements ITransmittalRule {

	public static final String DOCUMENT_TYPE_INFORMATION = "Information";
	public static final String DOCUMENT_TYPE_APPROVAL = "Approval";
	public static final String IPLE_RESPONSE_DATE = "IPLEResponseDate";

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " : Updates the SEPCO(DCR) business area post-transmittal.";
	}
	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 1.1;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, java.util.Hashtable)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest, BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, int businessAreaType, boolean isAddRequest)
			throws TBitsException {
		/* 
		 *  1.	Due date is marked to Null
			2.	Flow Status to IPLE is changed to Submitted for Approval (if Document Type is Approval) or Submitted for Information (if Document Type is Information)
			3.	IPLE Response Date is set to SEPCO DTN Date + RT-IPLE (based on rev)
			4.	Transmit for Approval is set to False
			5.	DTN to IPLE/SITE is updated with SEPCO DTN No
		 */
		
		if ((businessAreaType == IPLUtils.DCR_BUSINESS_AREA) && 
				currentBA.getSystemPrefix().trim().equals(IPLUtils.ADHOC_SEPCO)){
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String trnIdNormalizedId = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
			
			paramTable.put(Field.USER, "DC-SEPCO");
			paramTable.put(Field.DUE_DATE, IPLUtils.EMPTY_STRING);
			paramTable.put(Field.NOTIFY, false + "");			
			
			if (transmittalType.equals("TransmitToIPLE")){				
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(IPLUtils.DTN_TO_IPLE_SITE, trnIdNormalizedId);
				
				//Transmit to IPLE is set to false
				paramTable.put("DecisionFromIPLE", "false");
				
				//if (transmittalRequest.get(IPLUtils.DOCUMENT_TYPE).equals(DOCUMENT_TYPE_APPROVAL))
				paramTable.put(Field.STATUS, IPLUtils.STATUS_UNDER_REVIEW);
				//else if (transmittalRequest.get(IPLUtils.DOCUMENT_TYPE).equals(DOCUMENT_TYPE_INFORMATION))
				//	paramTable.put(Field.STATUS, RELEASE_FOR_CONSTRUCTION);						
				
				paramTable.put (Field.DESCRIPTION,"Document submitted to IPLE for review via letter of Transmital number #" 
									+ trnIdNormalizedId + " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}
			else if (transmittalType.equals("TransmitForRFC")){				
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(IPLUtils.DTN_TO_IPLE_SITE, trnIdNormalizedId);
				
				//Transmit FOR RFC is set to false
				paramTable.put("TransmitForRFC", "false");
				
				//if (transmittalRequest.get(IPLUtils.DOCUMENT_TYPE).equals(DOCUMENT_TYPE_APPROVAL))
				paramTable.put(Field.STATUS, IPLUtils.RELEASE_FOR_CONSTRUCTION);
				//else if (transmittalRequest.get(IPLUtils.DOCUMENT_TYPE).equals(DOCUMENT_TYPE_INFORMATION))
				//	paramTable.put(Field.STATUS, RELEASE_FOR_CONSTRUCTION);
				
				//paramTable.put(IPLE_RESPONSE_DATE, "");
				
				paramTable.put (Field.DESCRIPTION,"Document submitted to IPLE for review via letter of Transmital number #" 
									+ trnIdNormalizedId + " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}			
		}
	}
}
