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
public class AdhocIPLPPostTransmittalDCRUpdate implements ITransmittalRule {

	private static final String APPROVAL = "Approval";
	private static final String FLOW_STATUS_TO_SITE = "FlowStatusToSite";
	private static final String DTN_TO_IPLE = "DTNToIPLE";
	private static final String COMMENTED_BY_IPLP = "CommentedByIPLP";
	private static final String TRANSMIT_TO_IPLE = "TransmitToIPLE";
	private static final String SITE_RESPONSE_DATE = "SiteResponseDate";
	private static final String DTN_TO_SITE = "DTNToSite";

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
		
		if ((businessAreaType == IPLUtils.DCR_BUSINESS_AREA) && 
				currentBA.getSystemPrefix().trim().equals(IPLUtils.ADHOC_IPLP)){
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String iplpDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
			
			paramTable.put(Field.USER, "DC-IPLP");
			paramTable.put(Field.DUE_DATE, IPLUtils.EMPTY_STRING);
			paramTable.put(Field.NOTIFY, false + "");			
			
			if (transmittalType.equals(TRANSMIT_TO_IPLE)){
//				1.Due Date is set to Null
//				2.DTN to IPLP is updated with IPLP DTN No
//				3.Transmit To IPLE is set to False
				paramTable.put(Field.STATUS, COMMENTED_BY_IPLP);				
				paramTable.put(DTN_TO_IPLE, iplpDTNNumber);
				paramTable.put(TRANSMIT_TO_IPLE, false + "");				
				paramTable.put (Field.DESCRIPTION,"Document submitted to IPLE with decision via letter of Transmital number #" 
									+ iplpDTNNumber
									+ " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}
			else if (transmittalType.equals("TransmitToSite")){
				//1.Flow Status to SITE is changed to Submitted for Approval (if Document Type is Approval) or Submitted for Information (if Document Type is Information)
				//2.DTN to SITE is updated with IPLP DTN No
				//3.Transmit to Site is set to False
				//4.SITE Response Date is set to IPLP  DTN Date + RT-SITE (based on rev)
				
				if (dcrRequest.get(IPLUtils.DOCUMENT_TYPE).equals(APPROVAL))
					paramTable.put(FLOW_STATUS_TO_SITE, IPLUtils.STATUS_UNDER_REVIEW);
				paramTable.put(DTN_TO_SITE, iplpDTNNumber);
				paramTable.put("TransmitToSite", false + "");
//				int slideOffset = IPLUtils.getExFieldSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable,
//						10, 7, isAddRequest, DTN_TO_SITE);				
//				if (slideOffset != 0){
//					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
//					paramTable.put(SITE_RESPONSE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
//				}									
				
				paramTable.put (Field.DESCRIPTION,"Document submitted to IPLE with decision via letter of Transmital number #" 
									+ iplpDTNNumber
									+ " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}			
		}
	}
}
