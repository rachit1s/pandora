/**
 * 
 */
package ncc;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;
import transmittal.com.tbitsGlobal.server.ITransmittalRule;
import transmittal.com.tbitsGlobal.server.TransmittalProcess;
import transmittal.com.tbitsGlobal.server.TransmittalUtils;

/**
 * @author lokesh
 *
 */
public class NCCWorkflowCycleRule implements ITransmittalRule {
	
	private static final String FIELD_SUBMISSION_NO = "SubmissionNo";
	private static final String WORKFLOW_ORIGINATOR = "workflowOriginator";

	/* (non-Javadoc)
	 * @see transmittal.com.tbitsGlobal.server.ITransmittalRule#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getName() + "- Increments the workflow cycle by 1 everytime the originating agency transmits a document.";
	}

	/* (non-Javadoc)
	 * @see transmittal.com.tbitsGlobal.server.ITransmittalRule#getSequence()
	 */
	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see transmittal.com.tbitsGlobal.server.ITransmittalRule#process(java.sql.Connection, transbit.tbits.domain.Request, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, transmittal.com.tbitsGlobal.server.TransmittalProcess, java.util.HashMap, int, boolean)
	 */
	@Override
	public void process(Connection connection, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable,
			TransmittalProcess transmittalProcess,
			HashMap<String, String> transmittalParams, int businessAreaType,
			boolean isAddRequest) throws TBitsException {		
		if ((dcrBA == null) || (currentBA == null))
			return;
		
		if ((currentBA.getSystemId() == dcrBA.getSystemId()) && !isAddRequest 
				&& (businessAreaType == TransmittalUtils.DCR_BUSINESS_AREA))
		{
			String workflowOriginator = transmittalParams.get(WORKFLOW_ORIGINATOR);
			if ((workflowOriginator != null) && Boolean.parseBoolean(workflowOriginator.trim()))
			{
				String wfCycleNo = dcrRequest.get(FIELD_SUBMISSION_NO);
				if (wfCycleNo != null){
					int submissionNo = Integer.parseInt(wfCycleNo);
					submissionNo = submissionNo + 1;
					paramTable.put(FIELD_SUBMISSION_NO, submissionNo + "");
				}
			}
		}
	}
}
