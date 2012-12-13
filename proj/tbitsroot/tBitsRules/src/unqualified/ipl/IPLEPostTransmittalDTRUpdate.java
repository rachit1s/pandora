/**
 * 
 */
package ipl;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class IPLEPostTransmittalDTRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates DTR business area post-transmittal for BHEL initiated transmittal.";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 1.2;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, 
			int businessAreaType, boolean isAddRequest)
			throws TBitsException {
				
		if ((businessAreaType == IPLUtils.DTR_BUSINESS_AREA) && IPLUtils.isIPLEDCR(dcrBA)){	
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String ipleDtnNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals("TransmitToTCE")){
				paramTable.put(Field.USER, "DC-IPLE, DC-TCE");
				paramTable.put(Field.ASSIGNEE, "DCPL");
				try {
					paramTable.put(Field.STATUS, dcrRequest.getExType(IPLUtils.FLOW_STATUS_TO_DCPL).getName());
				} catch (IllegalStateException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				}
				
				paramTable.put(Field.SEVERITY, IPLUtils.STRING_NONE);
				
				paramTable.put (Field.DESCRIPTION,"Document submitted to DCPL for review via letter of Transmital number #" 
														+ ipleDtnNumber
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				
				/*int slideOffset = IPLUtils.getSlideOffsetBasedOnRevision(connection, currentBA.getSystemId(), dcrRequest, paramTable, 10, 7, isAddRequest);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}*/
			}
			/*else if (transmittalType.equals("TransmitToTPSC")){
				paramTable.put(Field.USER, "DC-IPLE, TPSC");
				paramTable.put(Field.ASSIGNEE, "TPSC");
				
				try {
					paramTable.put(Field.STATUS, dcrRequest.getExType(IPLUtils.FLOW_STATUS_TO_TPSC).getName());
				} catch (IllegalStateException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				}
				
				paramTable.put(Field.SEVERITY, IPLUtils.STRING_NONE);
				
				paramTable.put (Field.DESCRIPTION,"Document submitted via letter of Transmittal number #" 
													+ ipleDtnNumber
													+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				int slideOffset = IPLUtils.getSlideOffsetBasedOnRevision(connection, currentBA.getSystemId(), dcrRequest, paramTable, 10, 7, isAddRequest);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
			}*/
			else if (transmittalType.equals("TransmitToBHEL")){
				paramTable.put(Field.USER, "DC-IPLE");
				paramTable.put(Field.ASSIGNEE, "BHEL");				
				
				paramTable.put(Field.DESCRIPTION, "Document released via letter of Transmittal number #" 
														+ ipleDtnNumber
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				int slideOffset = 7;				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
			}
		}
	} 
}
