/**
 * 
 */
package ncc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import ncc.NCCTransmittalUtils;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;
import transmittal.com.tbitsGlobal.server.ITransmittalController;
import transmittal.com.tbitsGlobal.server.TransmittalDropDownOption;
import transmittal.com.tbitsGlobal.server.TransmittalProcess;

/**
 * @author lokesh
 *
 */
public class NCCTransmittalController implements ITransmittalController {
	
	private static final int  STUP_TO_NCC_DROPDOWN_ID= 8;
	static final int CSEPDI_TO_NCC_DROPDOWN_ID = 1;

	/* (non-Javadoc)
	 * @see ncc.ITransmittalController#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ncc.ITransmittalController#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void process(Connection connection, BusinessArea dcrBA,
			ArrayList<Request> dcrRequestList, TransmittalDropDownOption ntp,
			ArrayList<TransmittalProcess> transmittalTypes,
			TransmittalProcess transmittalType) throws TBitsException {
		
		if ((transmittalType.getSystemId() == NCCTransmittalUtils.CSEPDI_SYSTEM_ID) && (ntp.getId() == CSEPDI_TO_NCC_DROPDOWN_ID)){
			StringBuffer wf1Details = new StringBuffer(); 
			StringBuffer wf2Details = new StringBuffer();

			try{
				boolean isContinueWF1 = WorkflowRules.isBelongsToWorkFlow1(dcrRequestList, wf1Details);				
				if (isContinueWF1){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
							NCCTransmittalUtils.CSEPDI_SYSTEM_ID, 1));
					return;
				}
				boolean isContinueWF2 = WorkflowRules.isBelongsToWorkFlow2(dcrRequestList, wf2Details);
				if (isContinueWF2){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
							NCCTransmittalUtils.CSEPDI_SYSTEM_ID, 2));
					return;
				}
				if ((!isContinueWF1) && (!isContinueWF2)){
					TBitsException tbe = new TBitsException(wf1Details + "\n" + wf2Details);
					tbe.setDescription(NCCTransmittalUtils.ALL_DRAWINGS_SAME_CATEGORY_MSG + wf1Details + NCCTransmittalUtils.BR_B_OR_B_BR + wf2Details);
					throw tbe;
				}

			} catch(SQLException sqle){
				throw new TBitsException("Error occurred while retrieving sub-process for transmittal from CSEPDI.");
			}
		}
		else if ((ntp.getDcrSystemId() == NCCTransmittalUtils.DCPL_SYSTEM_ID) && (ntp.getId() == NCCTransmittalUtils.DCPL_TO_NCC_DROPDOWN_ID)){
			
			StringBuffer wf5Details = new StringBuffer(); 
			StringBuffer wf6Details = new StringBuffer();
			
			try{
				boolean isContinueWF5 = WorkflowRules.isBelongsToWorkFlow5(dcrRequestList, wf5Details);				
				if (isContinueWF5){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
							NCCTransmittalUtils.DCPL_SYSTEM_ID, 7));
					return;
				}
				boolean isContinueWF6 = WorkflowRules.isBelongsToWorkFlow6(dcrRequestList, wf6Details);
				if (isContinueWF6){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
							NCCTransmittalUtils.DCPL_SYSTEM_ID, 8));
					return;
				}
				if ((!isContinueWF5) && (!isContinueWF6)){
					TBitsException tbe = new TBitsException(wf5Details + "\n" + wf6Details);
					tbe.setDescription(NCCTransmittalUtils.ALL_DRAWINGS_SAME_CATEGORY_MSG + wf5Details +
							NCCTransmittalUtils.BR_B_OR_B_BR + wf6Details);
					throw tbe;
				}
				
			} catch(SQLException sqle){
				throw new TBitsException("Error occurred while retrieving sub-process for transmittal from " + dcrBA.getSystemPrefix());
			}
		}
		else if ((ntp.getDcrSystemId() == NCCTransmittalUtils.DESEIN_SYSTEM_ID) && (ntp.getId() == NCCTransmittalUtils.DESEIN_TO_NCC_DROPDOWN_ID)){

			StringBuffer wf1Details = new StringBuffer(); 
			StringBuffer wf2Details = new StringBuffer();
			StringBuffer wf3Details = new StringBuffer();
			StringBuffer wf4Details = new StringBuffer();
			StringBuffer wf5Details = new StringBuffer();
			StringBuffer wf6Details = new StringBuffer();
			//StringBuffer wf7Details = new StringBuffer();			

			try{
				boolean isContinueWF1 = WorkflowRules.isBelongsToWorkFlow1(dcrRequestList, wf1Details);				
				if (isContinueWF1){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
											dcrBA.getSystemId(), 16));
					return;
				}
				boolean isContinueWF2 = WorkflowRules.isBelongsToWorkFlow2(dcrRequestList, wf2Details);
				if (isContinueWF2){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
											dcrBA.getSystemId(), 17));
					return;
				}
				boolean isContinueWF3 = WorkflowRules.isBelongsToWorkFlow3(dcrRequestList, wf3Details);
				if (isContinueWF3){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
											dcrBA.getSystemId(), 17));
					return;
				}
				boolean isContinueWF4 = WorkflowRules.isBelongsToWorkFlow4(dcrRequestList, wf4Details);
				if (isContinueWF4){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
											dcrBA.getSystemId(), 17));
					return;
				}
				boolean isContinueWF5 = WorkflowRules.isBelongsToWorkFlow5(dcrRequestList, wf5Details);
				if (isContinueWF5){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
											dcrBA.getSystemId(), 17));
					return;
				}
				boolean isContinueWF6 = WorkflowRules.isBelongsToWorkFlow6(dcrRequestList, wf6Details);
				if (isContinueWF6){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
											dcrBA.getSystemId(), 17));
					return;
				}
				/*boolean isContinueWF7 = WorkflowRules.isBelongsToWorkFlow7(dcrRequestList, wf7Details);
				if (isContinueWF2){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
											dcrBA.getSystemId(), "TransmitToNCCWorkFlowALL"));
					return;
				}*/
				if ((!isContinueWF1) && (!isContinueWF2) && (!isContinueWF3)
						&& (!isContinueWF4) && (!isContinueWF5) && (!isContinueWF6)//&& (!isContinueWF7)
						){
					TBitsException tbe = new TBitsException(wf1Details + "\n" + wf2Details);
					tbe.setDescription(wf1Details + "\n" + wf2Details);
					throw tbe;
				}

			} catch(SQLException sqle){
				throw new TBitsException("Error occurred while retrieving sub-process for transmittal from " + dcrBA.getSystemPrefix());
			}
		}
		else if ((ntp.getDcrSystemId() == NCCTransmittalUtils.EDTD_SYSTEM_ID) && (ntp.getId() == NCCTransmittalUtils.EDTD_TO_NCC_DROPDOWN_ID)){
			
			StringBuffer wf1Details = new StringBuffer();
			StringBuffer wf3Details = new StringBuffer();
			StringBuffer wf4Details = new StringBuffer();
			
			try{
				boolean isContinueWF1 = WorkflowRules.isBelongsToWorkFlow1(dcrRequestList, wf1Details);				
				if (isContinueWF1){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
															NCCTransmittalUtils.EDTD_SYSTEM_ID, 11));
					return;
				}
				boolean isContinueWF3 = WorkflowRules.isBelongsToWorkFlow3(dcrRequestList, wf3Details);
				if (isContinueWF3){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
															NCCTransmittalUtils.EDTD_SYSTEM_ID, 12));
					return;
				}
				boolean isContinueWF4 = WorkflowRules.isBelongsToWorkFlow4(dcrRequestList, wf4Details);
				if (isContinueWF4){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
															NCCTransmittalUtils.EDTD_SYSTEM_ID, 11));
					return;
				}
				if ((!isContinueWF1) && (!isContinueWF3) && (!isContinueWF4)){
					TBitsException tbe = new TBitsException(wf1Details + "\n" + wf4Details);
					tbe.setDescription(NCCTransmittalUtils.ALL_DRAWINGS_SAME_CATEGORY_MSG + wf1Details + 
							NCCTransmittalUtils.BR_B_OR_B_BR + wf3Details + NCCTransmittalUtils.BR_B_OR_B_BR + wf4Details);
					throw tbe;
				}
				
			} catch(SQLException sqle){
				throw new TBitsException("Error occurred while retrieving sub-process for transmittal from CSEPDI.");
			}
		}
		else if ((ntp.getDcrSystemId() == NCCTransmittalUtils.VENDOR_SYSTEM_ID) && (ntp.getId() == NCCTransmittalUtils.VENDOR1_TO_NCC_DROPDOWN_ID)){
			
			StringBuffer wf6Details = new StringBuffer();
						
			try{
				boolean isContinueWF6 = WorkflowRules.isBelongsToWorkFlow6(dcrRequestList, wf6Details);				
				if (isContinueWF6){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
															NCCTransmittalUtils.VENDOR_SYSTEM_ID, 18));
					return;
				}
								
				if (!isContinueWF6){
					TBitsException tbe = new TBitsException(wf6Details.toString());
					tbe.setDescription(wf6Details.toString());
					throw tbe;
				}
				
			} catch(SQLException sqle){
				throw new TBitsException("Error occurred while retrieving sub-process for transmittal from Vendor1.");
			}
		}
		else if ((ntp.getDcrSystemId() == NCCTransmittalUtils.STUP_SYSTEM_ID) && (ntp.getId() == STUP_TO_NCC_DROPDOWN_ID)){
			
			StringBuffer wf4Details = new StringBuffer();
						
			try{
				boolean isContinueWF4 = WorkflowRules.isBelongsToWorkFlow4(dcrRequestList, wf4Details);				
				if (isContinueWF4){
					transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
															NCCTransmittalUtils.STUP_SYSTEM_ID, 4));
					return;
				}
								
				if (!isContinueWF4){
					TBitsException tbe = new TBitsException(wf4Details.toString());
					tbe.setDescription(wf4Details.toString());
					throw tbe;
				}
				
			} catch(SQLException sqle){
				throw new TBitsException("Error occurred while retrieving sub-process for transmittal from STUP.");
			}
		}
	
	}

}
