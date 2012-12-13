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
public class IPLEPostTransmittalDCRUpdate implements ITransmittalRule {

	private static final String UNDER_REVIEW = "UnderReview";
	private static final String DECISION_FROM_TPSC = "DecisionFromTPSC";
	private static final String TRANSMIT_TO_BHEL = "TransmitToSEPCO";
	private static final String DECISION_FROM_DCPL = "DecisionFromDCPL";
	private static final String TRANSMIT_TO_TPSC = "TransmitToTPSC";
	private static final String SEPCO_RESPONSE_DATE = "SEPCOResponseDate";
	private static final String TRANSMIT_TO_DCPL = "TransmitToTCE";
	private static final String DTN_TO_DCPL = "DTNToDCPL";
	private static final String DCPL_RESPONSE_DATE = "DCPLResponseDate";
	private static final String FLOW_STATUS_TO_DCPL = "FlowStatusToDCPL";
	private static final String FLOW_STATUS_TO_TPSC = "FlowStatusToTPSC";
	private static final String TPSC_RESPONSE_DATE = "TPSCResponseDate";
	private static final String DTN_TO_TPSC = "DTNToTPSC";
	private static final String DTN_TO_SEPCO_EXT = "DTNToSEPCOExt";
	
	private static final String APPROVAL = "Approval";
	private static final String FLOW_STATUS_TO_SITE = "FlowStatusToSite";
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
		return 1.0;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, java.util.Hashtable)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest, BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, int businessAreaType, boolean isAddRequest)
			throws TBitsException {
		
		if ((businessAreaType == IPLUtils.DCR_BUSINESS_AREA) && 
				currentBA.getSystemPrefix().trim().equals(IPLUtils.IPLE)){
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			String ipleDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
			
			paramTable.put(Field.USER, "DC-IPLE");
			//paramTable.put(Field.DUE_DATE, IPLUtils.EMPTY_STRING);
			paramTable.put(Field.NOTIFY, false + "");			
			
			if (transmittalType.equals(TRANSMIT_TO_DCPL)){
				//paramTable.put(Field.STATUS, UNDER_REVIEW);
				
				paramTable.put(FLOW_STATUS_TO_DCPL, UNDER_REVIEW);
				
//				2.	Decision from DCPL is changed to None
				paramTable.put(DECISION_FROM_DCPL, IPLUtils.STRING_NONE);				
				paramTable.put (Field.DESCRIPTION,"Document submitted to TCE for review via letter of Transmital number #" 
									+ ipleDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]");
				
//				3.	DCPL Response Date is changed to IPLE DTN Date + RT-DCPL (based on rev)
				int dcplSlideOffset = IPLUtils.getExFieldSlideOffsetBasedOnRevision(connection, currentBA.getSystemId(),
						dcrRequest, paramTable, 7, 5, isAddRequest);				
				if (dcplSlideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, dcplSlideOffset);
					paramTable.put(DCPL_RESPONSE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
				
//				4.	DTN to DCPL is set to IPLE DTN
				paramTable.put(DTN_TO_DCPL, ipleDTNNumber);
				
//				5.	Transmit To DCPL is set to False
				paramTable.put(TRANSMIT_TO_DCPL, false + "");
			}
			else if (transmittalType.equals("TransmitToSite")){
				//paramTable.put(Field.STATUS, "UnderReview");
//				TODO: 1.	Flow Status To TPSC is changed to Submitted for Approval/Submitted for Information (Based on Purpose Code)
				paramTable.put(FLOW_STATUS_TO_TPSC, UNDER_REVIEW);
				
//				2.	Decision from TPSC is changed to None
				paramTable.put(DECISION_FROM_TPSC, IPLUtils.STRING_NONE);
				
//				3.	TPSC Response Date is changed to IPLE DTN Date + RT-TPSC (based on rev)
				int slideOffset = IPLUtils.getExFieldSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable,
												10, 7, isAddRequest, DTN_TO_TPSC);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(TPSC_RESPONSE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
				
//				4.	DTN to TPSC is set to IPLE DTN
				paramTable.put(DTN_TO_TPSC, ipleDTNNumber);
				
//				5.	Transmit To TPSC is set to False
				paramTable.put(TRANSMIT_TO_TPSC, false + "");
				
				paramTable.put (Field.DESCRIPTION,"Document submitted to SITE via letter of Transmital number #" 
									+ ipleDTNNumber
									+ " [DTN#" + transmittalRequest.getRequestId() + "]");	
			}
			else if (transmittalType.equals(TRANSMIT_TO_BHEL)){
				
				paramTable.put(TRANSMIT_TO_BHEL, IPLUtils.FALSE);
				
//				1.	Due Date is set to Null
				paramTable.put(Field.DUE_DATE, IPLUtils.EMPTY_STRING);					
				
//				2.	Flow Status From SEPCO is changed to Returned with Decision
				paramTable.put(Field.STATUS, IPLUtils.STATUS_RETURNED_WITH_DECISION);
				
//				3.	SEPCO Response Date: IPLE DTN Date + SEPCO-RT (Based on Review)
				int slideOffset = 7;				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(SEPCO_RESPONSE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}				

//				4.	DTN to SEPCO Ext is updated with IPLE DTN No
				paramTable.put(DTN_TO_SEPCO_EXT, ipleDTNNumber);
				
				paramTable.put(Field.DESCRIPTION, "Document returned with decision to SEPCO via letter of Transmittal number #" 
						+ ipleDTNNumber
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
			/*else if (transmittalType.equals("TransmitToSite")){
				//1.Flow Status to SITE is changed to Submitted for Approval (if Document Type is Approval) or Submitted for Information (if Document Type is Information)
				//2.DTN to SITE is updated with IPLP DTN No
				//3.Transmit to Site is set to False
				//4.SITE Response Date is set to IPLP  DTN Date + RT-SITE (based on rev)

				if (dcrRequest.get(IPLUtils.DOCUMENT_TYPE).equals(APPROVAL))
					paramTable.put(FLOW_STATUS_TO_SITE, IPLUtils.STATUS_UNDER_REVIEW);
				
				paramTable.put(DTN_TO_SITE, ipleDTNNumber);
				paramTable.put("TransmitToSite", false + "");
				int slideOffset = IPLUtils.getExFieldSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable,
						10, 7, isAddRequest, DTN_TO_SITE);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(SITE_RESPONSE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}									

				paramTable.put (Field.DESCRIPTION,"Document submitted to IPLE with decision via letter of Transmital number #" 
						+ ipleDTNNumber
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}			*/
		}
	}
}
