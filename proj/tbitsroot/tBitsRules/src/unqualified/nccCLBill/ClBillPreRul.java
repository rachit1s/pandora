package nccCLBill;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TimeZone;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.webapps.WebUtil;

import static nccCLBill.ClBillConstants.*;

public class ClBillPreRul implements IRule {

	String classNameCl = "ClBillPrerule";
	//LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger("nccCL_BILL");
	private static final Double ZEROVALUE = 0.0;
	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		int sysId = ba.getSystemId();
		String baPrefix = ba.getSystemPrefix();
		RuleResult ruleResult = new RuleResult (true, "Client Billing", true);
		//WebUtil.g
		if(baPrefix.equalsIgnoreCase(CL_SYSPREFIX)){
			//Date loggedDate = currentRequest.getLastUpdatedDate();
			
			Calendar cal = Calendar.getInstance();
			
			Calendar nowCal = Calendar.getInstance();
			nowCal.setTimeZone(TimeZone.getTimeZone("IST"));
			Date nowDatenTime = new Date();
			nowDatenTime.setTime(cal.getTimeInMillis());
			
			
			String warningString = null;
			String planningMgrAction = null;
			String invoiceNo = null;
			Type pendingWtPlanningMgr = null;
			Type pendingWtAccounts = null;
			Type pendingWtPrjCoord = null;
			Type pendingWtVpPower = null;
			Type pendingWtClientKVK = null;
			Type pendingWtNone = null;
			Type statusActive = null;
			
			Type typePlngMgrActPending = null;
			Type typeFinMgrActPending = null;
			try {
				pendingWtPlanningMgr = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_PENDING_WITH, CL_PENDINGWITH_PLANNINGMGR);
				pendingWtAccounts = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_PENDING_WITH, CL_PENDINGWITH_ACCOUNTS);
				pendingWtPrjCoord = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_PENDING_WITH, CL_PENDINGWITH_PROJECTCOORD);
				pendingWtVpPower = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_PENDING_WITH, CL_PENDINGWITH_VICEPRESIDENT);
				pendingWtClientKVK = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_PENDING_WITH, CL_PENDINGWITH_CLIENT);
				pendingWtNone = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_PENDING_WITH, CL_PENDINGWITH_NONE);
				
				statusActive = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_STATUS, CL_STATUS_ACTIVE);
				
				
				typePlngMgrActPending = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_PLANNINGMGR_ACTION, CL_PLANNINGMGR_ACTION_PENDING);
				typeFinMgrActPending = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_FINANCEMGR_ACTION, CL_FINANCEMGR_ACTION_PENDING);
				
			} catch (DatabaseException de){
				de.printStackTrace();
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("Error Occured in Type, Field Lookup. "+de.getMessage());
				return ruleResult;
			}
			
			// Add Request
			if(isAddRequest){
				try{
					// Other Blank Data Should not Fill
					warningString = creqFinMgrDataCheck (currentRequest, oldRequest);
					planningMgrAction = currentRequest.get(CL_PLANNINGMGR_ACTION);
					
				} catch (DatabaseException de) {
					de.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error Occured while Getting Ex-Fields. "+de.getMessage());
					return ruleResult;
				} catch (IllegalStateException ie) {
					ie.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error Occured: "+ie.getMessage());
					return ruleResult;
				}
				if (warningString != null){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage(warningString);
					return ruleResult;
				}
				
				if (!(planningMgrAction.equalsIgnoreCase(CL_PLANNINGMGR_ACTION_SUBMITTED))){ // Not submitted by Planning Mgr
					// set Action Pending, Pending with, Status, due date, assignee.
					
					currentRequest.setCategoryId(pendingWtPlanningMgr);
					currentRequest.setStatusId(statusActive);
					try{
						//currentRequest.setExType(CL_PLANNINGMGR_ACTION, CL_PLANNINGMGR_ACTION_PENDING);
						currentRequest.setObject(CL_PLANNINGMGR_ACTION, typePlngMgrActPending);
						setAssignee(CL_LOGIN_PLANNING_MGR, currentRequest, sysId);
					} catch (DatabaseException de){
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
						return ruleResult;
					}
					
					// set due date to be 28th of current month
					cal.setTimeInMillis(nowDatenTime.getTime());
					String dateDay = (Timestamp.toCustomFormat(nowDatenTime, "yyyy-MM-dd")).split("-")[2];
					int day = Integer.parseInt(dateDay);
					if(day < (CL_PLANNING_MGR_DUE_DAY - 1))
						cal.set(Calendar.DAY_OF_MONTH, CL_PLANNING_MGR_DUE_DAY);
					else
						cal.add(Calendar.DAY_OF_MONTH, CL_PLANNING_MGR_DURATION);
					Date adueDate = new Date();
					adueDate.setTime(cal.getTimeInMillis());
					currentRequest.setDueDate(adueDate);
				}
				
				if (planningMgrAction.equalsIgnoreCase(CL_PLANNINGMGR_ACTION_SUBMITTED)){ // Submitted by Planning Mgr
					
					// check for mandatory fields
					if(!(currentRequest.getSubject().length() > 0))
						warningString = "Enter Subject field.";
					
					try {
						if(currentRequest.getObject(CL_BILLDETAILS) != null){
							String billDetails = currentRequest.get(CL_BILLDETAILS);
							if( billDetails.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
									|| billDetails.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
										|| !(billDetails.length() > 0) )
								warningString = "Enter Bill Details.";
						} else {
							warningString = "Bill Details Field is null.";
						}
						// Check for Sub breakup Text Field
						if(currentRequest.getObject(CL_SUB_BREAKUP) != null){
							String billDetails = currentRequest.get(CL_SUB_BREAKUP);
							if( billDetails.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
									|| billDetails.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
										|| !(billDetails.length() > 0) )
								warningString = "Enter BBU Sub Breakup details.";
						} else {
							warningString = "Sub Breakup Field is null.";
						}
						if( (currentRequest.getObject(CL_TOTAL_BILLVALUE)).equals(ZEROVALUE) )
							warningString = "Enter Total Bill Value.";
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while getting Ex fields. "+ie.getMessage());
						return ruleResult;
					}
					// Memorandum RA Bill
					String jsonMemoRABill = currentRequest.get(CL_MEMORANDUM_RABILL); //string return in json form
					Collection<AttachmentInfo> exMemoRABill = AttachmentInfo.fromJson(jsonMemoRABill);
					if (exMemoRABill.isEmpty())
						warningString = "Attach Memorandum RA Bill.";
					
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					
					// set Pending with, Status, Action pending, due date, Pending from date, assignee for finance mgr. Gen Invoice No.
					currentRequest.setCategoryId(pendingWtAccounts);
					currentRequest.setStatusId(statusActive);
					try {
						//currentRequest.setExType(CL_FINANCEMGR_ACTION, CL_FINANCEMGR_ACTION_PENDING);
						currentRequest.setObject(CL_FINANCEMGR_ACTION, typeFinMgrActPending);
						setAssignee(CL_LOGIN_FINANCE_MGR, currentRequest, sysId);
						
						// set due date to be T+1, Pending From Date
						currentRequest.setObject(CL_PENDING_FROM, nowDatenTime);
						cal.setTimeInMillis(nowDatenTime.getTime());
						cal.add(Calendar.DAY_OF_MONTH, CL_FINANCE_DURATION);
						Date accountsDueDate = new Date();
						accountsDueDate.setTime(cal.getTimeInMillis());
						currentRequest.setDueDate(accountsDueDate);
						
						// Generate and set Unique Invoice No.
						invoiceNo = genInvoiceNo (currentRequest, sysId, connection, nowDatenTime);
						currentRequest.setObject(CL_INVOICENO, invoiceNo);
						
					}catch (DatabaseException de){
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
						return ruleResult;
					}catch (SQLException se){
						se.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while retrieving maxid for running serial No. "+se.getMessage());
						return ruleResult;
					}catch (IllegalStateException ie){
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					}
				}
				
				
			}
			// Update Request
			if (!isAddRequest) {
				String creqPendingWith = currentRequest.getCategoryId().getName();
				String creqStatus = currentRequest.getStatusId().getName();
				
				String financeMgrAction = null;
				String projectCoordAction = null;
				String vpPowerAction = null;
				String clientDecision = null;
				String paymentReceiptatNCC = null;
				boolean isGenCoverLetter = false;
				boolean isClientAck = false;
				Date clientPymtRelDate = null;
				
				Type typePrjCoordActpending = null;
				Type typeFinMgrActNone = null;
				Type typePrjCoordActNone = null;
				Type typeVpPwrActPending = null;
				Type typeClDecPending = null;
				Type typeStatusResubmit = null;
				Type statusClosed = null;
				Type statusSuspended = null;
				
				try {
					planningMgrAction = currentRequest.get(CL_PLANNINGMGR_ACTION);
					financeMgrAction = currentRequest.get(CL_FINANCEMGR_ACTION);
					projectCoordAction = currentRequest.get(CL_PRJCOORD_ACTION);
					vpPowerAction = currentRequest.get(CL_VP_POWER_ACTION);
					clientDecision = currentRequest.get(CL_CLIENT_DECISION);
					paymentReceiptatNCC = currentRequest.get(CL_PAYMENT_RECEIPT_NCCPL); // Type Field
					
					isGenCoverLetter = ((Boolean)(currentRequest.getObject(CL_GENERATE_COVERLTR))).booleanValue();
					isClientAck = ((Boolean)(currentRequest.getObject(CL_CLIENT_ACKNOWLEDGEMENT))).booleanValue();
					clientPymtRelDate = (Date)currentRequest.getObject(CL_CLIENT_PAYMENT_REL_DATE);
					typePrjCoordActpending = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_PRJCOORD_ACTION, CL_PRJCOORD_ACTION_PENDING);
					typeFinMgrActNone = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_FINANCEMGR_ACTION, CL_FINANCEMGR_ACTION_NONE);
					typePrjCoordActNone = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_PRJCOORD_ACTION, CL_PRJCOORD_ACTION_NONE);
					typeVpPwrActPending = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_VP_POWER_ACTION, CL_VP_POWER_ACTION_PENDING);
					typeClDecPending = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_CLIENT_DECISION, CL_CLIENT_DECISION_PENDING);
					statusClosed = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_STATUS, CL_STATUS_CLOSED);
					statusSuspended = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_STATUS, CL_STATUS_SUSPENDED);
					typeStatusResubmit = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, CL_STATUS, CL_STATUS_RESUBMIT);
					
				}catch(DatabaseException de){
					de.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error Occured in retrieving Type Ex-Fields. "+de.getMessage());
					return ruleResult;
				}
				
				// Pending with Planning Mgr
				if(creqPendingWith.equalsIgnoreCase(CL_PENDINGWITH_PLANNINGMGR) && creqStatus.equalsIgnoreCase(CL_STATUS_ACTIVE)){
					
					try{
						if( !(oldRequest.get(CL_INVOICENO).equalsIgnoreCase(currentRequest.get(CL_INVOICENO))) )
							warningString = "Invoice number must not change.";
						// Other Blank Data Should not Fill
						if(warningString == null)
						warningString = creqFinMgrDataCheck (currentRequest, oldRequest);
						
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while Getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					}
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					if (!(planningMgrAction.equalsIgnoreCase(CL_PLANNINGMGR_ACTION_SUBMITTED))){ // Not submitted by Planning Mgr
						// set Action Pending, Pending with, assignee.
						try {
							// if resubmit by prj coord or vp power
							if(currentRequest.get(CL_PRJCOORD_ACTION).equalsIgnoreCase(CL_PRJCOORD_ACTION_RESUBMIT)
									|| currentRequest.get(CL_VP_POWER_ACTION).equalsIgnoreCase(CL_VP_POWER_ACTION_RESUBMIT)) {
								if( !(currentRequest.getSeverityId().getName().equalsIgnoreCase(oldRequest.getSeverityId().getName())) )
									warningString = "Billing Schedule should not Change.";
								if( !(currentRequest.get(CL_CONTRACT_REFERENCE).equalsIgnoreCase(oldRequest.get(CL_CONTRACT_REFERENCE))) )
									warningString = "Contract Reference should not Change.";
								if (warningString != null){
									ruleResult.setCanContinue(false);
									ruleResult.setMessage(warningString);
									return ruleResult;
								}
							}
							currentRequest.setObject(CL_PLANNINGMGR_ACTION, typePlngMgrActPending);
							setAssignee(CL_LOGIN_PLANNING_MGR, currentRequest, sysId);
						} catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
							return ruleResult;
						}
						/*
						// set due date to be 28th of current month or T+2
						cal.setTimeInMillis(nowDatenTime.getTime());
						String dateDay = (Timestamp.toCustomFormat(nowDatenTime, "yyyy-MM-dd")).split("-")[2];
						int day = Integer.parseInt(dateDay);
						if(day < (CL_PLANNING_MGR_DUE_DAY - 1))
							cal.set(Calendar.DAY_OF_MONTH, CL_PLANNING_MGR_DUE_DAY);
						else
							cal.add(Calendar.DAY_OF_MONTH, CL_PLANNING_MGR_DURATION);
						Date adueDate = new Date();
						adueDate.setTime(cal.getTimeInMillis());
						currentRequest.setDueDate(adueDate);
						*/
					}
					
					if (planningMgrAction.equalsIgnoreCase(CL_PLANNINGMGR_ACTION_SUBMITTED)){ // Submitted by Planning Mgr
						// check for mandatory fields
						if(!(currentRequest.getSubject().length() > 0))
							warningString = "Enter Subject field.";
						try {
							if(currentRequest.getObject(CL_BILLDETAILS) != null){
								String billDetails = currentRequest.get(CL_BILLDETAILS);
								if( billDetails.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
										|| billDetails.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
											|| !(billDetails.length() > 0) )
									warningString = "Enter Bill Details.";
							} else {
								warningString = "Bill Details Field is null.";
							}
							// Check for Sub breakup Text Field
							if(currentRequest.getObject(CL_SUB_BREAKUP) != null){
								String billDetails = currentRequest.get(CL_SUB_BREAKUP);
								if( billDetails.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
										|| billDetails.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
											|| !(billDetails.length() > 0) )
									warningString = "Enter BBU Sub Breakup details.";
							} else {
								warningString = "Sub Breakup Field is null.";
							}
							if( (currentRequest.getObject(CL_TOTAL_BILLVALUE)).equals(ZEROVALUE) )
								warningString = "Enter Total Bill Value.";
						} catch (IllegalStateException ie) {
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while getting Ex fields. "+ie.getMessage());
							return ruleResult;
						}
						// Memorandum RA Bill
						String jsonMemoRABill = currentRequest.get(CL_MEMORANDUM_RABILL); //string return in json form
						Collection<AttachmentInfo> exMemoRABill = AttachmentInfo.fromJson(jsonMemoRABill);
						if (exMemoRABill.isEmpty())
							warningString = "Attach Memorandum RA Bill.";
						// if resubmit by prj coord or vp power
						if(currentRequest.get(CL_PRJCOORD_ACTION).equalsIgnoreCase(CL_PRJCOORD_ACTION_RESUBMIT)
								|| currentRequest.get(CL_VP_POWER_ACTION).equalsIgnoreCase(CL_VP_POWER_ACTION_RESUBMIT)) {
							if( !(currentRequest.getSeverityId().getName().equalsIgnoreCase(oldRequest.getSeverityId().getName())) )
								warningString = "Billing Schedule should not Change.";
							if( !(currentRequest.get(CL_CONTRACT_REFERENCE).equalsIgnoreCase(oldRequest.get(CL_CONTRACT_REFERENCE))) )
								warningString = "Contract Reference should not Change.";
						}
						if (warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						
						// set Pending with, Status, Action pending, due date, Pending from date, assignee for finance mgr. Gen Invoice No.
						currentRequest.setCategoryId(pendingWtAccounts);
						currentRequest.setStatusId(statusActive);
						try {
							//currentRequest.setExType(CL_FINANCEMGR_ACTION, CL_FINANCEMGR_ACTION_PENDING);
							currentRequest.setObject(CL_FINANCEMGR_ACTION, typeFinMgrActPending);
							setAssignee(CL_LOGIN_FINANCE_MGR, currentRequest, sysId);
							
							// set due date to be T+1, Pending From Date
							currentRequest.setObject(CL_PENDING_FROM, nowDatenTime);
							cal.setTimeInMillis(nowDatenTime.getTime());
							cal.add(Calendar.DAY_OF_MONTH, CL_FINANCE_DURATION);
							Date accountsDueDate = new Date();
							accountsDueDate.setTime(cal.getTimeInMillis());
							//Timestamp tsAccountsddate = new Timestamp(cal.getTimeInMillis());
							currentRequest.setDueDate(accountsDueDate);
							
							// Generate and set Unique Invoice No.
							if( !(currentRequest.get(CL_PRJCOORD_ACTION).equalsIgnoreCase(CL_PRJCOORD_ACTION_RESUBMIT) 
									|| currentRequest.get(CL_VP_POWER_ACTION).equalsIgnoreCase(CL_VP_POWER_ACTION_RESUBMIT)) ){
								invoiceNo = genInvoiceNo (currentRequest, sysId, connection, nowDatenTime);
								currentRequest.setObject(CL_INVOICENO, invoiceNo);
							}
							
						}catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
							return ruleResult;
						}catch (SQLException se){
							se.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while retrieving maxid for running serial No. "+se.getMessage());
							return ruleResult;
						}catch (IllegalStateException ie){
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}
					}
					// If Action Suspended
					
				}
				
				// Pending with Accounts
				if(creqPendingWith.equalsIgnoreCase(CL_PENDINGWITH_ACCOUNTS) && creqStatus.equalsIgnoreCase(CL_STATUS_ACTIVE)){
					// Authenticate the user
					
					try{
						// should not temper with planning Mgr data n invoice Details, if user is not Accounts or BA Admin, not authorised to change.
						warningString = oreqPlngMgrDataCheck (currentRequest, oldRequest);
						// Other Blank Data Should not Fill
						if(warningString == null)
						warningString = creqPrjCoordDataCheck (currentRequest, oldRequest);
						// Memorandum RA Bill
						String jsonMemoRABill = currentRequest.get(CL_MEMORANDUM_RABILL); //string return in json form
						Collection<AttachmentInfo> exMemoRABill = AttachmentInfo.fromJson(jsonMemoRABill);
						if (exMemoRABill.isEmpty())
							warningString = "Attach Memorandum RA Bill.";
						
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while Getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					}
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					
					// if not Approved
					if(financeMgrAction.equalsIgnoreCase(CL_FINANCEMGR_ACTION_NONE) || financeMgrAction.equalsIgnoreCase(CL_FINANCEMGR_ACTION_PENDING)){
						// set Action Pending, Assignee, Memorandum RA Bill.
						try {
							currentRequest.setObject(CL_FINANCEMGR_ACTION, typeFinMgrActPending);
							setAssignee(CL_LOGIN_FINANCE_MGR, currentRequest, sysId);
							
						} catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Type Ex-Fields. "+de.getMessage());
							return ruleResult;
						}
					}
					
					// If Approved
					if (financeMgrAction.equalsIgnoreCase(CL_FINANCEMGR_ACTION_APPROVED)){
						// Mandatory Fields
						try {
							if ( (currentRequest.getObject(CL_TOTAL_TAXES_APPLICABLE)).equals(ZEROVALUE) )
								warningString = "Enter Total Taxes Applicable.";
							if ( (currentRequest.getObject(CL_TOTAL_OTHER_DEDUCTIONS)).equals(ZEROVALUE) )
								warningString = "Enter Total Other Deductions.";
							if(!(currentRequest.get(CL_CLIENT_LETTER_REF).length() > 0))
								warningString = "Enter Client Letter Reference.";
						} catch (IllegalStateException ie) {
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while getting Ex fields. "+ie.getMessage());
							return ruleResult;
						}
						
						if (warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						// Actions taken
						// Set NetPayable
						double totalBillVal = ((Double)currentRequest.getObject(CL_TOTAL_BILLVALUE)).doubleValue();
						double totalTxsVal = ((Double)currentRequest.getObject(CL_TOTAL_TAXES_APPLICABLE)).doubleValue();
						double totalOthDedVal = ((Double)currentRequest.getObject(CL_TOTAL_OTHER_DEDUCTIONS)).doubleValue();
						
						double netPayable = totalBillVal + totalTxsVal - totalOthDedVal;
						currentRequest.setObject(CL_NET_PAYABLE, netPayable);
						
						// Set Pending with, Status, Action pending, due date, Pending from date, assignee project Co-ord.  
						currentRequest.setCategoryId(pendingWtPrjCoord);
						// currentRequest.setStatusId(statusActive);
						try {
							currentRequest.setObject(CL_PRJCOORD_ACTION, typePrjCoordActpending);
							setAssignee(CL_LOGIN_PROJECT_COORD, currentRequest, sysId);
							
							// set due date to be T+1, Pending From Date
							currentRequest.setObject(CL_PENDING_FROM, nowDatenTime);
							
							cal.setTimeInMillis(nowDatenTime.getTime());
							cal.add(Calendar.DAY_OF_MONTH, CL_PROJECT_COORD_DURATION);
							Date prjCoordDueDate = new Date();
							prjCoordDueDate.setTime(cal.getTimeInMillis());
							//Timestamp tsProjectCoordDdate = new Timestamp(cal.getTimeInMillis());
							currentRequest.setDueDate(prjCoordDueDate);
							
						}catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
							return ruleResult;
						}catch (IllegalStateException ie){
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}
						
					}
					// If Resubmit
					
				}
				
				// Pending With Project Co-ordinator
				if(creqPendingWith.equalsIgnoreCase(CL_PENDINGWITH_PROJECTCOORD) && creqStatus.equalsIgnoreCase(CL_STATUS_ACTIVE)
						&& !(vpPowerAction.equalsIgnoreCase(CL_VP_POWER_ACTION_APPROVED))){
					
					// should not temper with old data.
					try{
						// should not temper with old data
						warningString = oreqFinMgrDataCheck (currentRequest, oldRequest);
						// Other Blank Data Should not Fill
						if(warningString == null)
						warningString = creqVpPowerDataCheck (currentRequest, oldRequest);
						
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while Getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					}
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					// Memorandum RA Bill
					String jsonMemoRABill = currentRequest.get(CL_MEMORANDUM_RABILL); //string return in json form
					Collection<AttachmentInfo> exMemoRABill = AttachmentInfo.fromJson(jsonMemoRABill);
					if (exMemoRABill.isEmpty()){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Attach Memorandum RA Bill.");
						return ruleResult;
					}
					// Pending
					
					// Approved
					if (projectCoordAction.equalsIgnoreCase(CL_PRJCOORD_ACTION_APPROVED)){
						// set Pending with, duedate, pending from, Action Pending, assignee VP Power
						currentRequest.setCategoryId(pendingWtVpPower);
						try {
							//currentRequest.setExType(CL_VP_POWER_ACTION, CL_VP_POWER_ACTION_PENDING);
							currentRequest.setObject(CL_VP_POWER_ACTION, typeVpPwrActPending);
							setAssignee(CL_LOGIN_VP_POWER, currentRequest, sysId);
							
							// set due date to be T+1, Pending From current
							currentRequest.setObject(CL_PENDING_FROM, nowDatenTime);
							
							cal.setTimeInMillis(nowDatenTime.getTime());
							cal.add(Calendar.DAY_OF_MONTH, CL_VP_POWER_DURATION);
							Date vpPwrDueDate = new Date();
							vpPwrDueDate.setTime(cal.getTimeInMillis());
							//Timestamp tsVpPowerDdate = new Timestamp(cal.getTimeInMillis());
							currentRequest.setDueDate(vpPwrDueDate);
							
						}catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
							return ruleResult;
						}catch (IllegalStateException ie){
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}
						
					}
					// Resubmit
					if (projectCoordAction.equalsIgnoreCase(CL_PRJCOORD_ACTION_RESUBMIT)){
						// description cannot be empty
						if(currentRequest.getDescription() != null){
							String descBoxString = currentRequest.getDescription().trim();
							if ( descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
									|| descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
										|| !(descBoxString.length() > 0) ){
								warningString = "Give Details in Description Box for Resubmit.";
							}
						} else {
							warningString = "Description box is Null.";
						}
						if (warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						
						currentRequest.setCategoryId(pendingWtPlanningMgr);
						// set due date to be T+1, Pending From current
						try {
							//currentRequest.setExType(CL_PLANNINGMGR_ACTION, CL_PLANNINGMGR_ACTION_PENDING);
							currentRequest.setObject(CL_PLANNINGMGR_ACTION, typePlngMgrActPending);
							//currentRequest.setExType(CL_FINANCEMGR_ACTION, CL_FINANCEMGR_ACTION_NONE);
							currentRequest.setObject(CL_FINANCEMGR_ACTION, typeFinMgrActNone);
							
							currentRequest.setObject(CL_TOTAL_OTHER_DEDUCTIONS, 0.0);
							currentRequest.setObject(CL_TOTAL_TAXES_APPLICABLE, 0.0);
							currentRequest.setObject(CL_NET_PAYABLE, 0.0);
							currentRequest.setObject(CL_CLIENT_LETTER_REF, "");
							currentRequest.setObject(CL_CLIENT_LETTER_REF_DATED, null);
							setAssignee(CL_LOGIN_PLANNING_MGR, currentRequest, sysId);
							currentRequest.setObject(CL_PENDING_FROM, nowDatenTime);
							
						} catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
							return ruleResult;
						}
						cal.setTimeInMillis(nowDatenTime.getTime());
						cal.add(Calendar.DAY_OF_MONTH, CL_PLANNING_MGR_DURATION);
						Date planingMgrDueDate = new Date();
						planingMgrDueDate.setTime(cal.getTimeInMillis());
						currentRequest.setDueDate(planingMgrDueDate);
						
					}
					
					// if suspended
					if (projectCoordAction.equalsIgnoreCase(CL_PRJCOORD_ACTION_SUSPEND)){
						// description cannot be empty
						if(currentRequest.getDescription() != null){
							String descBoxString = currentRequest.getDescription().trim();
							if ( descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
									|| descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
										|| !(descBoxString.length() > 0) ){
								warningString = "Give Details in Description Box for Suspend.";
							}
						} else {
							warningString = "Description box is Null.";
						}
						if (warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						currentRequest.setCategoryId(pendingWtNone);
						currentRequest.setStatusId(statusSuspended);
						currentRequest.setObject(CL_DUE_DATE, null);
						currentRequest.setAssignees(null);
						
					}
				}
				
				// Pending With VP Power
				if(creqPendingWith.equalsIgnoreCase(CL_PENDINGWITH_VICEPRESIDENT) && creqStatus.equalsIgnoreCase(CL_STATUS_ACTIVE)){
					
					//  should not temper with old data.
					try{
						// should not temper with old data.
						warningString = oreqPrjCoordDataCheck (currentRequest, oldRequest);
						// Other Blank Data Should not Fill
						if(warningString == null)
						warningString = creqCorresDataCheck (currentRequest);
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while Getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					}
					// Memorandum RA Bill
					String jsonMemoRABill = currentRequest.get(CL_MEMORANDUM_RABILL); //string return in json form
					Collection<AttachmentInfo> exMemoRABill = AttachmentInfo.fromJson(jsonMemoRABill);
					if (exMemoRABill.isEmpty())
						warningString = "Attach Memorandum RA Bill.";
					
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					
					// Pending
						
					// Approved
					if (vpPowerAction.equalsIgnoreCase(CL_VP_POWER_ACTION_APPROVED)){
						// set Pending with, duedate, pending from, Action Pending, assignee VP Power
						currentRequest.setCategoryId(pendingWtPrjCoord);
						try {
							//currentRequest.setExType(CL_VP_POWER_ACTION, CL_VP_POWER_ACTION_PENDING);
							setAssignee(CL_LOGIN_PROJECT_COORD, currentRequest, sysId);
							
							// set due date to be T+1, Pending From current
							currentRequest.setObject(CL_PENDING_FROM, nowDatenTime);
							
							cal.setTimeInMillis(nowDatenTime.getTime());
							cal.add(Calendar.DAY_OF_MONTH, CL_PROJECT_COORD_DURATION);
							Date prjCoordDueDate = new Date();
							prjCoordDueDate.setTime(cal.getTimeInMillis());
							currentRequest.setDueDate(prjCoordDueDate);
							
						}catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
							return ruleResult;
						}catch (IllegalStateException ie){
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}
						
					}
					// Resubmit
					if (vpPowerAction.equalsIgnoreCase(CL_VP_POWER_ACTION_RESUBMIT)){
						// description cannot be empty
						if(currentRequest.getDescription() != null){
							String descBoxString = currentRequest.getDescription().trim();
							if ( descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
									|| descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
										|| !(descBoxString.length() > 0) ){
								warningString = "Give Details in Description Box for Resubmit.";
							}
						} else {
							warningString = "Description box is Null.";
						}
						if (warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						
						currentRequest.setCategoryId(pendingWtPlanningMgr);
						// set due date to be T+1, Pending From current
						try {
							//currentRequest.setExType(CL_PLANNINGMGR_ACTION, CL_PLANNINGMGR_ACTION_PENDING);
							currentRequest.setObject(CL_PLANNINGMGR_ACTION, typePlngMgrActPending);
							//currentRequest.setExType(CL_FINANCEMGR_ACTION, CL_FINANCEMGR_ACTION_NONE);
							currentRequest.setObject(CL_FINANCEMGR_ACTION, typeFinMgrActNone);
							//currentRequest.setExType(CL_PRJCOORD_ACTION, CL_PRJCOORD_ACTION_NONE);
							currentRequest.setObject(CL_PRJCOORD_ACTION, typePrjCoordActNone);
							//currentRequest.setObject(CL_VP_POWER_ACTION, typeVpPwrActNone);
							currentRequest.setObject(CL_TOTAL_OTHER_DEDUCTIONS, 0.0);
							currentRequest.setObject(CL_TOTAL_TAXES_APPLICABLE, 0.0);
							currentRequest.setObject(CL_NET_PAYABLE, 0.0);
							currentRequest.setObject(CL_CLIENT_LETTER_REF, "");
							currentRequest.setObject(CL_CLIENT_LETTER_REF_DATED, null);
							setAssignee(CL_LOGIN_PLANNING_MGR, currentRequest, sysId);
							currentRequest.setObject(CL_PENDING_FROM, nowDatenTime);
							
						} catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
							return ruleResult;
						}
						cal.setTimeInMillis(nowDatenTime.getTime());
						cal.add(Calendar.DAY_OF_MONTH, CL_PLANNING_MGR_DURATION);
						//Timestamp tsPlaningMgrDdate = new Timestamp(cal.getTimeInMillis());
						Date planingMgrDueDate = new Date();
						planingMgrDueDate.setTime(cal.getTimeInMillis());
						currentRequest.setDueDate(planingMgrDueDate);
					}
					
					// if suspended
					if(vpPowerAction.equalsIgnoreCase(CL_VP_POWER_ACTION_SUSPEND)) {
						// description cannot be empty
						if(currentRequest.getDescription() != null){
							String descBoxString = currentRequest.getDescription().trim();
							if ( descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
									|| descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
										|| !(descBoxString.length() > 0) ){
								warningString = "Give Details in Description Box for Suspend.";
							}
						} else {
							warningString = "Description box is Null.";
						}
						if (warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						currentRequest.setCategoryId(pendingWtNone);
						currentRequest.setStatusId(statusSuspended);
						currentRequest.setObject(CL_DUE_DATE, null);
						currentRequest.setAssignees(null);
						
					}
				}
				
				// VP Power action - Approved ..  no one can change and approve other than VP Power
				
				// Pending With Project Coord and VP Power Approved.
				if(creqPendingWith.equalsIgnoreCase(CL_PENDINGWITH_PROJECTCOORD) && creqStatus.equalsIgnoreCase(CL_STATUS_ACTIVE)
						&& vpPowerAction.equalsIgnoreCase(CL_VP_POWER_ACTION_APPROVED) &&
									((Date)oldRequest.getObject(CL_CLIENT_PAYMENT_REL_DATE)) == null){
					
					try{
						// should not temper with old data.
						warningString = oreqVpPowerDataCheck (currentRequest, oldRequest);
						// Other Blank Data Should not Fill
						if(warningString == null)
						warningString = creqClientDataCheck (currentRequest);
						
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while Getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					}
					// Memorandum RA Bill
					String jsonMemoRABill = currentRequest.get(CL_MEMORANDUM_RABILL); //string return in json form
					Collection<AttachmentInfo> exMemoRABill = AttachmentInfo.fromJson(jsonMemoRABill);
					if (exMemoRABill.isEmpty())
						warningString = "Attach Memorandum RA Bill.";
					
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}	
					
					// Pending
					
					// Generate Correspondence and cover Letter to be sent to Client,  pending with - Client
					if (isGenCoverLetter){
						// gen Correspondance No. then attach PDF , Preview PDF option will also be given
						
						
						// set Pending with, duedate, pending from, Client Decision Pending, assignee client kvk
						
						try {
							currentRequest.setObject(CL_CORRESPONDANCE_NO, "NPT10109/Corres/Number/String/SerialNo");
							// gen PDF File function if attachment null > Error rule.
							//Field fieldInvoiceCoverLetter = null;
							String fileType = "pdf";
							
							warningString = GenNAttachFile(fileType, currentRequest, sysId, baPrefix);
							if (warningString != null){
								ruleResult.setCanContinue(false);
								ruleResult.setMessage(warningString);
								return ruleResult;
							}
							
							currentRequest.setCategoryId(pendingWtClientKVK);
							setAssignee(CL_LOGIN_CLIENT_KVK, currentRequest, sysId);
							currentRequest.setObject(CL_CLIENT_DECISION, typeClDecPending);
							// set due date to be T+7, Pending From current
							currentRequest.setObject(CL_PENDING_FROM, nowDatenTime);
							cal.setTimeInMillis(nowDatenTime.getTime());
							cal.add(Calendar.DAY_OF_MONTH, CL_CLIENT_KVK_DURATION);
							Date clientKvkDueDate = new Date();
							clientKvkDueDate.setTime(cal.getTimeInMillis());
							currentRequest.setDueDate(clientKvkDueDate);
							
						}catch (IllegalStateException ie){
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
							return ruleResult;
						}
						
					}
					
				}
				
				// Pending with Client
				if(creqPendingWith.equalsIgnoreCase(CL_PENDINGWITH_CLIENT) && creqStatus.equalsIgnoreCase(CL_STATUS_ACTIVE)){
					
					// authorize users action only from kvk client
					
					// old data Check
					try{
						// Old data Check
						warningString = oreqCorresDataCheck (currentRequest, oldRequest);
						// Other Blank Data Should not Fill
						if(warningString == null)
						warningString = creqPlngnFinDataCheck (currentRequest);
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while Getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					}
					// Memorandum RA Bill
					String jsonMemoRABill = currentRequest.get(CL_MEMORANDUM_RABILL); //string return in json form
					Collection<AttachmentInfo> exMemoRABill = AttachmentInfo.fromJson(jsonMemoRABill);
					if (exMemoRABill.isEmpty())
						warningString = "Attach Memorandum RA Bill";
					// Invoice Cover letter
					String jsonCoverLetter = currentRequest.get(CL_INVOICE_COVER_LETTER); //string return in json form
					Collection<AttachmentInfo> exCoverLetter = AttachmentInfo.fromJson(jsonCoverLetter);
					if (exCoverLetter.isEmpty())
						warningString = "Attach Invoice Cover Letter";
					
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					
					if ( ((Boolean)currentRequest.getObject(CL_CLIENT_ACKNOWLEDGEMENT)).booleanValue() 
							&& !(clientDecision.equalsIgnoreCase(CL_CLIENT_DECISION_ACCEPTED)) ) {
						
						try{
							setAssignee(CL_LOGIN_CLIENT_KVK, currentRequest, sysId);
							currentRequest.setObject(CL_CLIENT_DECISION, typeClDecPending);
							
						}catch (IllegalStateException ie){
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
							return ruleResult;
						}
						
					}
					
					if(clientDecision.equalsIgnoreCase(CL_CLIENT_DECISION_ACCEPTED)){
						if(clientPymtRelDate == null)
							warningString = "Kindly give the Payment Released date.";
						else {
							// Check for Past date only
							//if(clientPymtRelDate.getTime() > nowDatenTime.getTime())
							//	warningString = "Kindly provide Past Date.";
						}
						if(!isClientAck)
							warningString = "Kindly Tick the Client Acknowledgement field.";
						
						if(warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						
							
						// set for project cord
						try{
							currentRequest.setCategoryId(pendingWtPrjCoord);
							setAssignee(CL_LOGIN_PROJECT_COORD, currentRequest, sysId);
							// Pending From current date, Due date null
							currentRequest.setObject(CL_PENDING_FROM, nowDatenTime);
							//currentRequest.setDueDate(null);
							currentRequest.setObject(CL_DUE_DATE, null);
						}catch (DatabaseException de){
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while Setting Ex-Fields. "+de.getMessage());
							return ruleResult;
						}catch (IllegalStateException ie){
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}
					}
					// if Resubmit
					if (clientDecision.equalsIgnoreCase(CL_CLIENT_DECISION_RESUBMIT)) {
						// description cannot be empty
						if(currentRequest.getDescription() != null){
							String descBoxString = currentRequest.getDescription().trim();
							if ( descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
									|| descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
										|| !(descBoxString.length() > 0) ){
								warningString = "Give Details in Description Box for Resubmit.";
							}
						} else {
							warningString = "Description box is Null.";
						}
						if (warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						currentRequest.setCategoryId(pendingWtNone);
						currentRequest.setStatusId(typeStatusResubmit);
						currentRequest.setObject(CL_DUE_DATE, null);
						currentRequest.setAssignees(null);
					}
					
					// if Suspend
					if (clientDecision.equalsIgnoreCase(CL_CLIENT_DECISION_SUSPEND)) {
						// description cannot be empty
						if(currentRequest.getDescription() != null){
							String descBoxString = currentRequest.getDescription().trim();
							if ( descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
									|| descBoxString.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
										|| !(descBoxString.length() > 0) ){
								warningString = "Give Details in Description Box for Suspend.";
							}
						} else {
							warningString = "Description box is Null.";
						}
						if (warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						currentRequest.setCategoryId(pendingWtNone);
						currentRequest.setStatusId(statusSuspended);
						currentRequest.setObject(CL_DUE_DATE, null);
						currentRequest.setAssignees(null);
					}
					
				}
				
				
				// Pending with Accounts/Project Co-ordinator and Payment Released date not null
				if(creqPendingWith.equalsIgnoreCase(CL_PENDINGWITH_PROJECTCOORD) && creqStatus.equalsIgnoreCase(CL_STATUS_ACTIVE)
						&& clientDecision.equalsIgnoreCase(CL_CLIENT_DECISION_ACCEPTED) && clientPymtRelDate != null){
					
					// old data Check
					try{
						warningString = oreqClientDataCheck (currentRequest, oldRequest);
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while Getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					}
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					
					if(paymentReceiptatNCC.equalsIgnoreCase(CL_PAYMENT_RECEIPT_NCCPL_YES)){
						// Memorandum RA Bill
						String jsonMemoRABill = currentRequest.get(CL_MEMORANDUM_RABILL); //string return in json form
						Collection<AttachmentInfo> exMemoRABill = AttachmentInfo.fromJson(jsonMemoRABill);
						if (exMemoRABill.isEmpty())
							warningString = "Attach Memorandum RA Bill";
						// Invoice Cover letter
						String jsonCoverLetter = currentRequest.get(CL_INVOICE_COVER_LETTER);//string return in json form
						Collection<AttachmentInfo> exCoverLetter = AttachmentInfo.fromJson(jsonCoverLetter);
						if (exCoverLetter.isEmpty())
							warningString = "Attach Invoice Cover Letter";
						// payment advice Scanned copy
						String jsonPaymentadvice = currentRequest.get(CL_PAYMENT_ADVICE); //string return in json form
						Collection<AttachmentInfo> exPaymentadvice = AttachmentInfo.fromJson(jsonPaymentadvice);
						if (exPaymentadvice.isEmpty())
							warningString = "Attach scanned copy of Payment Advice";
						if( (currentRequest.getObject(CL_RELEASED_AMOUNT)).equals(ZEROVALUE) )
							warningString = "Enter Released Payment.";
						// released payment should be <= net payable.
						else {
							double netPayable = ((Double)currentRequest.getObject(CL_NET_PAYABLE)).doubleValue();
							double releasedPymt = ((Double)currentRequest.getObject(CL_RELEASED_AMOUNT)).doubleValue();
							if (releasedPymt > netPayable)
								warningString = "Released Amount is more than NetPayable Amount.";
						}
						
						if(warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						
						// gen and attach Final report
						
						//  Close the Invoice
						currentRequest.setCategoryId(pendingWtNone);
						currentRequest.setStatusId(statusClosed);
						currentRequest.setAssignees(null);
						try{
							currentRequest.setObject(CL_PENDING_FROM, null);
							
						}catch (IllegalStateException ie){
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}
						
					}
					
				}
				
				// Status Closed
				if(creqPendingWith.equalsIgnoreCase(CL_PENDINGWITH_NONE) && creqStatus.equalsIgnoreCase(CL_STATUS_CLOSED)){
					warningString = "Invoice Request Closed.";
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
				}
				// Status Suspended
				if(creqPendingWith.equalsIgnoreCase(CL_PENDINGWITH_NONE) && (creqStatus.equalsIgnoreCase(CL_STATUS_RESUBMIT)
						|| creqStatus.equalsIgnoreCase(CL_STATUS_SUSPENDED)) ){
					warningString = "Invoice Request Suspended/Resubmitted, Create Sub-Request.";
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
				}
				
			}
			
			
		}else
		ruleResult.setMessage("Client Billing Rule not applicable to current BA: "+baPrefix);
		
		return ruleResult;
	}
	
	
	private String creqFinMgrDataCheck(Request currentRequest, Request oldRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if( !(((Double)currentRequest.getObject(CL_TOTAL_TAXES_APPLICABLE)).equals(ZEROVALUE)) )
			warningString = "Total Taxes Applicable Changed from Zero.";
		if(  !(((Double)currentRequest.getObject(CL_TOTAL_OTHER_DEDUCTIONS)).equals(ZEROVALUE)) )
			warningString = "Total Other Deductions Changed from Zero.";
		if(currentRequest.get(CL_CLIENT_LETTER_REF).length() > 0)
			warningString = "Client Letter Reference Should fill by Finance.";
		if(!(currentRequest.get(CL_FINANCEMGR_ACTION).equalsIgnoreCase(CL_FINANCEMGR_ACTION_NONE)))
			warningString = "Finance Mgr Action Changed from None.";
		if(warningString == null)
		warningString = creqPrjCoordDataCheck(currentRequest, oldRequest);
		return warningString;
	}




	private String creqPrjCoordDataCheck(Request currentRequest, Request oldRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(oldRequest == null){
			if(!(currentRequest.get(CL_PRJCOORD_ACTION).equalsIgnoreCase(CL_PRJCOORD_ACTION_NONE)))
				warningString = "Project Coordinator Action Changed from None.";
		} else {
			if( !(oldRequest.get(CL_PRJCOORD_ACTION).equalsIgnoreCase(currentRequest.get(CL_PRJCOORD_ACTION))) )
				warningString = "Project Coordinator Action Changed on Update Request.";
		}
		if(warningString == null)
		warningString = creqVpPowerDataCheck(currentRequest, oldRequest);
		return warningString;
	}

	
	private String creqVpPowerDataCheck(Request currentRequest, Request oldRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(oldRequest == null){
			if(!(currentRequest.get(CL_VP_POWER_ACTION).equalsIgnoreCase(CL_VP_POWER_ACTION_NONE)))
				warningString = "VP Power Action Changed from None.";
		} else {
			if( !(oldRequest.get(CL_VP_POWER_ACTION).equalsIgnoreCase(currentRequest.get(CL_VP_POWER_ACTION))) )
				warningString = "VP Power Action Changed on Update Request.";
		}
		if(warningString == null)
		warningString = creqCorresDataCheck(currentRequest);
		return warningString;
	}




	private String creqCorresDataCheck(Request currentRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(currentRequest.get(CL_CORRESPONDANCE_NO).length() > 0)
			warningString = "Correspondence Number should not Set.";
		if( ((Boolean)currentRequest.getObject(CL_GENERATE_COVERLTR)).booleanValue() )
			warningString = "Generate Cover Letter should not be Ticked.";
		if(warningString == null)
		warningString = creqClientDataCheck(currentRequest);
		return warningString;
	}




	private String creqClientDataCheck(Request currentRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(currentRequest.get(CL_CLIENT_DECISION).equalsIgnoreCase(CL_CLIENT_DECISION_NONE)))
			warningString = "Client decision changed from None.";
		if( ((Boolean)currentRequest.getObject(CL_CLIENT_ACKNOWLEDGEMENT)).booleanValue() )
			warningString = "Client Acknowledgement should not be Ticked.";
		if(!(currentRequest.getObject(CL_CLIENT_PAYMENT_REL_DATE) == null))
			warningString = "Payment Released date should not Set.";
		if(warningString == null)
		warningString = creqPlngnFinDataCheck(currentRequest);
		return warningString;
	}


	private String creqPlngnFinDataCheck(Request currentRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(currentRequest.get(CL_PAYMENT_RECEIPT_NCCPL).equalsIgnoreCase(CL_PAYMENT_RECEIPT_NCCPL_YES))
			warningString = "Payment receipt at NCCPL Changed.";
		if(  !(((Double)currentRequest.getObject(CL_RELEASED_AMOUNT)).equals(ZEROVALUE)) )
			warningString = "Released Amount Changed.";
		return warningString;
	}




	private String oreqClientDataCheck(Request currentRequest,
			Request oldRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(oldRequest.get(CL_CLIENT_DECISION).equalsIgnoreCase(currentRequest.get(CL_CLIENT_DECISION))))
			warningString = "Client Decision Changed.";
		if( !( ((Boolean)currentRequest.getObject(CL_CLIENT_ACKNOWLEDGEMENT)).booleanValue() )  )
			warningString = "Client Acknowledgement not Ticked.";
		if( ((Date)currentRequest.getObject(CL_CLIENT_PAYMENT_REL_DATE)) == null )
			warningString = "Payment Released Date is Empty.";
		else {
			if( !(oldRequest.get(CL_CLIENT_PAYMENT_REL_DATE).split("\\s")[0].equalsIgnoreCase(currentRequest.get(CL_CLIENT_PAYMENT_REL_DATE).split("\\s")[0])) )
				warningString = "Payment Released Date Changed.";
		}
		//if(!( ((Date)oldRequest.getObject(CL_CLIENT_PAYMENT_REL_DATE)).equals((Date)currentRequest.getObject(CL_CLIENT_PAYMENT_REL_DATE)) ))
		//	warningString = "Payment Released date Changed.";
		
		if(warningString == null)
		warningString = oreqCorresDataCheck(currentRequest, oldRequest);
		return warningString;
	}

	
	private String oreqCorresDataCheck(Request currentRequest,
			Request oldRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(oldRequest.get(CL_CORRESPONDANCE_NO).equalsIgnoreCase(currentRequest.get(CL_CORRESPONDANCE_NO))))
			warningString = "Correspondence Number Changed.";
		if(warningString == null)
		warningString = oreqVpPowerDataCheck(currentRequest, oldRequest);
		return warningString;
	}


	private String oreqVpPowerDataCheck(Request currentRequest,
			Request oldRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(oldRequest.get(CL_VP_POWER_ACTION).equalsIgnoreCase(currentRequest.get(CL_VP_POWER_ACTION))))
			warningString = "VP Power Action Changed.";
		if(warningString == null)
		warningString = oreqPrjCoordDataCheck(currentRequest, oldRequest);
		return warningString;
	}


	private String oreqPrjCoordDataCheck(Request currentRequest,
			Request oldRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(oldRequest.get(CL_PRJCOORD_ACTION).equalsIgnoreCase(currentRequest.get(CL_PRJCOORD_ACTION))))
			warningString = "Project Coordinator Action Changed.";
		if(warningString == null)
		warningString = oreqFinMgrDataCheck(currentRequest, oldRequest);
		return warningString;
	}


	private String oreqFinMgrDataCheck(Request currentRequest,
			Request oldRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if( !((oldRequest.getObject(CL_TOTAL_TAXES_APPLICABLE)).equals(currentRequest.getObject(CL_TOTAL_TAXES_APPLICABLE))) )
			warningString = "Total Taxes Applicable Changed.";
		if( !((oldRequest.getObject(CL_TOTAL_OTHER_DEDUCTIONS)).equals(currentRequest.getObject(CL_TOTAL_OTHER_DEDUCTIONS))) )
			warningString = "Total Other Deductions Changed.";
		if(!(oldRequest.get(CL_CLIENT_LETTER_REF).equalsIgnoreCase(currentRequest.get(CL_CLIENT_LETTER_REF))))
			warningString = "Client Letter Reference Changed.";
		if(!(oldRequest.get(CL_FINANCEMGR_ACTION).equalsIgnoreCase(currentRequest.get(CL_FINANCEMGR_ACTION))))
			warningString = "Finance Mgr Action Changed.";
		if(warningString == null)
		warningString = oreqPlngMgrDataCheck(currentRequest, oldRequest);
		return warningString;
	}


	private String oreqPlngMgrDataCheck(Request currentRequest,
			Request oldRequest)  throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(oldRequest.getSeverityId().getName().equals(currentRequest.getSeverityId().getName())))
			warningString = "Billing Schedule Changed.";
		if(currentRequest.getObject(CL_BILLDETAILS) != null){
			String billDetails = currentRequest.get(CL_BILLDETAILS);
			if( billDetails.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY1)
					|| billDetails.equalsIgnoreCase(CL_BILL_DETAILS_EMPTY2)
						|| !(billDetails.length() > 0) )
				warningString = "Enter Bill Details.";
		} else {
			warningString = "Bill Details Field is null.";
		}
		if(!(oldRequest.get(CL_INVOICENO).equalsIgnoreCase(currentRequest.get(CL_INVOICENO))))
			warningString = "Invoice No Changed.";
		if(!(oldRequest.get(CL_CONTRACT_REFERENCE).equalsIgnoreCase(currentRequest.get(CL_CONTRACT_REFERENCE))))
			warningString = "Contract Reference Changed.";
		//double oreqTotalbillv = (Double)oldRequest.getObject(CL_TOTAL_BILLVALUE);
		//double creqTotalbillv = (Double)currentRequest.getObject(CL_TOTAL_BILLVALUE);
		if(!(oldRequest.getObject(CL_TOTAL_BILLVALUE).equals(currentRequest.getObject(CL_TOTAL_BILLVALUE))))
			warningString = "Total Bill Value Changed.";
		if(!(oldRequest.get(CL_PLANNINGMGR_ACTION).equalsIgnoreCase(currentRequest.get(CL_PLANNINGMGR_ACTION))))
			warningString = "Planning Mgr Action Changed.";
		
		return warningString;
	}


	private String GenNAttachFile(String fileType, Request currentRequest, int sysId, String currentBA
									) throws IllegalStateException, DatabaseException {
		File pdfFile = null;
		String CorrespondanceNo = null;
		String currentDate = null;
		String warningString = null;
		String reqId = Integer.toString(currentRequest.getRequestId()) ;
		Hashtable <String, String> params = new Hashtable<String, String> ();
		// letter reference No dated should not be null.
		Date dated = (Date)currentRequest.getObject(CL_CLIENT_LETTER_REF_DATED);
		CorrespondanceNo = currentRequest.get(CL_CORRESPONDANCE_NO);
		//currentDate = currentRequest.getLoggedDate().toCustomFormat("yyyy-MM-dd").replace("-", "");	
		currentDate = currentRequest.get(CL_LAST_UPDATED).split("\\s")[0].replace("-", "");
		String invoiceNo = currentRequest.get(CL_INVOICENO);
		// Put the rptdesign pdf File parameters in the hashtable  
		params.put(CL_RPT_CORRESNO, CorrespondanceNo);
		// params.put(CL_RPT_CORRESNO_DATE, currentDate); // variable nt defined
		params.put(CL_RPT_CONTRACT_REFERENCE, ((Type)(currentRequest.getObject(CL_CONTRACT_REFERENCE))).getDisplayName());
		params.put(CL_RPT_LETTER_REFERENCE, currentRequest.get(CL_CLIENT_LETTER_REF));
		if(dated != null)
		params.put(CL_RPT_REF_DATED, currentRequest.get(CL_CLIENT_LETTER_REF_DATED).split("\\s")[0]);
		params.put(CL_RPT_INVOICE_NO, invoiceNo);
		params.put(CL_RPT_INVOICE_NO_DATE, currentRequest.get(CL_PENDING_FROM).split("\\s")[0]);
		params.put(CL_RPT_BILL_DETAILS, currentRequest.get(CL_BILLDETAILS));
		params.put(CL_RPT_NET_PAYABLE, Double.toString((Double)currentRequest.getObject(CL_NET_PAYABLE)));
		
		// Select a report file to generate based on Generation Agency
		String reportName = "NCC_client_invoice_template.rptdesign";
		
		HashMap<String,String> reportParamMap = new HashMap<String,String>();
		reportParamMap.put(REP_RID, reqId  ) ;			
		String tbits_base_url = WebUtil.getNearestPath("") ; // PropertiesHandler.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE)
		System.out.println( "tbits_base_url : " + tbits_base_url ) ;
		reportParamMap.put(REP_TBITS_BASE_URL_KEY, tbits_base_url );

		LOG.info("Sending parameters to generate rptdesign Invoice Cover Letter pdf File to BIRT Engine.");
		pdfFile = generateReport( reportName, params, reportParamMap, fileType ) ;
		
		if( pdfFile == null ) 
			warningString = "Cannot Generate the Invoice Cover Letter.";
		else {
			LOG.info(" About to attach generated Invoice Cover letter pdf File.");
			String displayName = currentDate+"_"+invoiceNo+".pdf";
			int requestId = currentRequest.getRequestId() ;
			int actionId = currentRequest.getMaxActionId() ;
			Uploader up = new Uploader( requestId, actionId, currentBA ) ;
			
			AttachmentInfo atinfo = up.moveIntoRepository(pdfFile) ;
			atinfo.name=displayName;
			
			// RequestEx requestEx = extendedFields.get(fieldInvoiceCoverLetter) ;
			
			ArrayList<AttachmentInfo> attachArray = new ArrayList<AttachmentInfo>() ;// arrayList Collection
			attachArray.add(atinfo) ;
			//String newJson = AttachmentInfo.toJson(attachArray) ;
			//requestEx.setTextValue(newJson ) ;
			currentRequest.setObject(CL_INVOICE_COVER_LETTER, attachArray);
			LOG.info(" Attached the generated Invoice Cover Letter pdf File.");		
		}
		return warningString;
	}

	// Invoice No. String - NPT10109/X/YY/RA/10-11/ZZZZ
	private String genInvoiceNo(Request currentRequest, int sysId, Connection con, Date acurrentDate) throws DatabaseException, SQLException {
		String invoiceNo = null;
		String currentFinancialYr = "10-11";
		int nextSerialNo = 1;
		String contractRefCode = ((Type)(currentRequest.getObject(CL_CONTRACT_REFERENCE))).getDescription();
		String billingScheduleCode = currentRequest.getSeverityId().getDescription();
		
		String datePart = Timestamp.toCustomFormat(acurrentDate, TBitsConstants.API_DATE_FORMAT).split("\\s")[0];
		String[] dateTokens = datePart.split("-");
		int yearCurr  = Integer.parseInt(dateTokens[0]);
		int monthCurr = Integer.parseInt(dateTokens[1]);
		if(monthCurr > 3){
			currentFinancialYr = (Integer.toString(yearCurr)).substring(2) + "-" + (Integer.toString( (yearCurr + 1) )).substring(2);
		} else {
			currentFinancialYr = (Integer.toString( (yearCurr - 1) )).substring(2) + "-" + (Integer.toString(yearCurr)).substring(2);
		}
		
		invoiceNo = CL_FIXED_NPT10109 + CL_FORWARD_SLASH + contractRefCode + CL_FORWARD_SLASH + billingScheduleCode + CL_FORWARD_SLASH + 
					CL_FIXED_RA + CL_FORWARD_SLASH + currentFinancialYr;
		
		String dependentInvoiceNoPrefix = CL_FIXED_NPT10109 + "-" + contractRefCode + "-" + billingScheduleCode + "-" + CL_FIXED_RA;
		nextSerialNo = getNextDocumentNumberCount(con, dependentInvoiceNoPrefix);
		
		NumberFormat formatter = new DecimalFormat("0000");
	    String serialNumber = formatter.format(nextSerialNo);
	    invoiceNo = invoiceNo + CL_FORWARD_SLASH + serialNumber;
		return invoiceNo;
	}
	
	private int getNextDocumentNumberCount (Connection con,
			String invoiceNumberPrefix) throws SQLException {
		
		System.out.println("Generating Invoice No. for : " + invoiceNumberPrefix );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, invoiceNumberPrefix );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				return id;
			}
			else{
				throw new SQLException();
			}
		}
		catch (SQLException e) {
			throw e;
		}
	}


	private void setAssignee(String assignees, Request currentReq, int sysId) throws DatabaseException {
		// set assignees to be String passed.
		Field assigneeField = Field.lookupBySystemIdAndFieldName(currentReq.getSystemId(), Field.ASSIGNEE);
		User assig  = User.lookupByUserLogin(assignees); // throws database exception
		RequestUser ru = new RequestUser(sysId, currentReq.getRequestId(), assig.getUserId(),
										CL_REQUEST_USER_ORDERING, CL_REQUEST_USER_IS_PRIMARY, assigneeField.getFieldId());
		ru.setUser(assig);
		
		ArrayList<RequestUser> assList= new ArrayList<RequestUser>();
		assList.add(ru);
		currentReq.setAssignees(assList);
		
	}

	@Override
	public String getName() {
		return classNameCl;
	}

	@Override
	public double getSequence() {
		return 1.1;
	}

}
