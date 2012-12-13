package nccVNBill;

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

import static nccVNBill.VNBillConstants.*;

public class VNBillPreRul implements IRule {

	String classNameVN = "VendorBillingPreRule";
	//LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger("nccVN_BILL");
	private static final Double ZEROVALUE = 0.0;
	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		int sysId = ba.getSystemId();
		String baPrefix = ba.getSystemPrefix();
		RuleResult ruleResult = new RuleResult (true, "Vendor Billing", true);
		
		if(baPrefix.equalsIgnoreCase(VN_SYSPREFIX)){
			//Date loggedDate = currentRequest.getLastUpdatedDate();
			Calendar cal = Calendar.getInstance();
			
			Calendar nowCal = Calendar.getInstance();
			nowCal.setTimeZone(TimeZone.getTimeZone("IST"));
			Date nowDatenTime = new Date();
			nowDatenTime.setTime(cal.getTimeInMillis());
			
			String billingCategory = currentRequest.getCategoryId().getName();
			
			String warningString = null;
			String workOrderNumber = null;
			Type workOrderType = currentRequest.getSeverityId();
			String workOrderGroupType = null;
			String actionBy = null;
			Type pendingWithStoresMgr = null;
			Type pendingWithPlanningMgr = null;
			Type pendingWithAccountsMgr = null;
			Type pendingWithSitePrjCoord = null;
			Type pendingWithSitePrjMgr = null;
			String actionType = null;
			
			Type typeActionPending = null;
			Type typeActionByStoresMgr = null;
			Type typeActionByPlngMgr = null;
			Type typeFinActionPending = null;
			Type typeFinActionNone = null;
			
			try {
				workOrderGroupType = currentRequest.get(VN_WORK_ORDER_GROUP);
				actionBy = currentRequest.get(VN_ACTIONBY);
				actionType = currentRequest.get(VN_ACTION);
				pendingWithStoresMgr = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_PENDING_WITH, VN_PENDINGWITH_STORES_MGR);
				pendingWithPlanningMgr = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_PENDING_WITH, VN_PENDINGWITH_PLANNING_MGR);
				pendingWithAccountsMgr = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_PENDING_WITH, VN_PENDINGWITH_ACCOUNTS_MGR);
				pendingWithSitePrjCoord = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_PENDING_WITH, VN_PENDINGWITH_SITEPRJ_COORD);
				pendingWithSitePrjMgr = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_PENDING_WITH, VN_PENDINGWITH_SITEPRJ_MGR);
				
				typeActionPending = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_ACTION, VN_ACTION_PENDING);
				typeActionByPlngMgr = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_ACTIONBY, VN_ACTIONBY_PLANNING_MGR);
				typeActionByStoresMgr = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_ACTIONBY, VN_ACTIONBY_STORES_MGR);
				typeFinActionPending = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_FINANCE_ACTION, VN_FINANCE_ACTION_PENDING);
				typeFinActionNone = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_FINANCE_ACTION, VN_FINANCE_ACTION_NONE);
				
			} catch (IllegalStateException ie) {
				ie.printStackTrace();
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("Error Occured in Type, Field Lookup. "+ie.getMessage());
				return ruleResult;
				
			} catch (DatabaseException de) {
				de.printStackTrace();
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("Error Occured in Type, Field Lookup. "+de.getMessage());
				return ruleResult;
			}
			
			
			
			// if Add request
			if(isAddRequest){
				
				// Mandatory Fields
				if(!(currentRequest.getSubject().length() > 0))
					warningString = "Enter Subject field.";
				if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SELECT))
					warningString = "Select Billing Category.";
				if(workOrderType.getName().equalsIgnoreCase(VN_WORK_ORDER_TYPE_SELECT))
					warningString = "Select Work Order Type.";
				//if(workOrderGroupType.equalsIgnoreCase(VN_WORK_ORDER_GROUP_SELECT))
				//	warningString = "Select Work Order Group.";
				if(currentRequest.getObject(VN_WORK_ORDER_NUMBER) != null){
					if(currentRequest.get(VN_WORK_ORDER_NUMBER).length() > 0)
						warningString = "Work Order Number is System Generated, should not set it.";
				}
				if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SITESUPPLIES)
						|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_EXWKS_SUPPLIES)) {
					if(actionBy.equalsIgnoreCase(VN_ACTIONBY_PLANNING_MGR))
						warningString = "Stores Manager should initiate the Billing.";
				}
				if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SUBCONTRACTORS)
						|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_PRW)) {
					if(actionBy.equalsIgnoreCase(VN_ACTIONBY_STORES_MGR))
						warningString = "Planning Manager should initiate the Billing.";
				}
				if (warningString != null){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage(warningString);
					return ruleResult;
				}
				// creq upline data check
				try {
					warningString = creqFinInitialDataCheck(currentRequest, oldRequest);
				} catch (IllegalStateException ie) {
					ie.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error Occured: "+ie.getMessage());
					return ruleResult;
				} catch (DatabaseException de) {
					de.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error Occured in getting Ex-Fields. "+de.getMessage());
					return ruleResult;
				}
				if (warningString != null){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage(warningString);
					return ruleResult;
				}
				// set status active
				Type statusActive = null;
				try {
					statusActive = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_STATUS, VN_STATUS_ACTIVE);
				} catch (DatabaseException de) {
					de.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error Occured in Type, Field Lookup. "+de.getMessage());
					return ruleResult;
				}
				currentRequest.setStatusId(statusActive);
				
				// set Pending With
				
				// case1 - action by stores mgr and action not submitted
				if((billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SITESUPPLIES)
						|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_EXWKS_SUPPLIES))
						&& !(actionType.equalsIgnoreCase(VN_ACTION_SUBMITTED))) {
					
					try {
						// set action pending
						currentRequest.setObject(VN_ACTION, typeActionPending);
						// set Pending date
						currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
						// set assignee
						setAssignee(VN_LOGIN_STORES_MGR, currentRequest, sysId);
						
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
						return ruleResult;
					}
					// set pending with
					currentRequest.setRequestTypeId(pendingWithStoresMgr);
					
					// set due date to be T+2
					cal.setTimeInMillis(nowDatenTime.getTime());
					cal.add(Calendar.DAY_OF_MONTH, VN_STORESMGR_DURATION);
					Date storesMgrDDate = new Date();
					storesMgrDDate.setTime(cal.getTimeInMillis());
					currentRequest.setDueDate(storesMgrDDate);
				}
				
				// case 2 - stores mgr, action > submitted
				if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_EXWKS_SUPPLIES)
						&& actionType.equalsIgnoreCase(VN_ACTION_SUBMITTED)) {
					// mandatory fields MRN
					String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC); //string return in json form
					Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
					if (exMRN.isEmpty())
						warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
					// Vendor Invoice No.
					if ( currentRequest.getObject(VN_VENDOR_INVOICE_NO) == null)
						warningString = "Enter Vendor Invoice Number.";
					else {
						if(!(currentRequest.get(VN_VENDOR_INVOICE_NO).trim().length() > 0))
							warningString = "Enter Valid Vendor Invoice Number.";
					}
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					
					// pending with planning mgr
					try {
						// set action by planning mgr
						currentRequest.setObject(VN_ACTIONBY, typeActionByPlngMgr);
						// set action pending
						currentRequest.setObject(VN_ACTION, typeActionPending);
						// set Pending date
						currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
						// set assignee
						setAssignee(VN_LOGIN_PLANNING_MGR, currentRequest, sysId);
						
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
						return ruleResult;
					}
					// set pending with
					currentRequest.setRequestTypeId(pendingWithPlanningMgr);
					
					// set due date to be T+2
					cal.setTimeInMillis(nowDatenTime.getTime());
					cal.add(Calendar.DAY_OF_MONTH, VN_PLANNINGMGR_DURATION);
					//Timestamp tsPlanningMgrddate = new Timestamp(cal.getTimeInMillis());
					Date planingMgrDDate = new Date();
					planingMgrDDate.setTime(cal.getTimeInMillis());
					currentRequest.setDueDate(planingMgrDDate);
					
				}
				
				// case 3 - stores mgr, action > submitted
				if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SITESUPPLIES)
						&& actionType.equalsIgnoreCase(VN_ACTION_SUBMITTED)) {
					// mandatory fields MRN, Memorandum Vendor Bill
					String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC); //string return in json form
					Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
					if (exMRN.isEmpty())
						warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
					String jsonMemoVnBill = currentRequest.get(VN_ATTCH_MEMO_VN_BILL);; // string return in json form
					Collection<AttachmentInfo> exMemoVnBill = AttachmentInfo.fromJson(jsonMemoVnBill);
					if (exMemoVnBill.isEmpty())
						warningString = "Attach Memorandum Vendor bill.";
					try {
						if(currentRequest.getObject(VN_WORK_ORDER_SUMMARY) != null){
							String woSummary = currentRequest.get(VN_WORK_ORDER_SUMMARY);
							if( woSummary.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY1)
									|| woSummary.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY2)
										||!(woSummary.length() > 0))
								warningString = "Give Details of Work Order Summary.";
						} else {
							warningString = "Work Order Summary details is null.";
						}
						if( ((Double)currentRequest.getObject(VN_TOTAL_WORKORDER_VALUE)).equals(ZEROVALUE) )
							warningString = "Enter Total Work Order Value.";
						if ( currentRequest.getObject(VN_VENDOR_INVOICE_NO) == null)
							warningString = "Enter Vendor Invoice Number.";
						else {
							if(!(currentRequest.get(VN_VENDOR_INVOICE_NO).trim().length() > 0))
								warningString = "Enter Valid Vendor Invoice Number.";
						}
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
					
					// pending with Accounts mgr
					try {
						// set action pending
						currentRequest.setObject(VN_FINANCE_ACTION, typeFinActionPending);
						// set Pending date
						currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
						// set assignee
						setAssignee(VN_LOGIN_FINANCE_MGR, currentRequest, sysId);
						// generate and set Work order Number
						workOrderNumber = genWorkOrderNumber (currentRequest, sysId, connection, nowDatenTime);
						currentRequest.setObject(VN_WORK_ORDER_NUMBER, workOrderNumber);
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
						return ruleResult;
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					} catch (SQLException se) {
						se.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while retrieving maxid for running serial No. "+se.getMessage());
						return ruleResult;
					}
					// set pending with
					currentRequest.setRequestTypeId(pendingWithAccountsMgr);
					// set due date to be T+2
					cal.setTimeInMillis(nowDatenTime.getTime());
					cal.add(Calendar.DAY_OF_MONTH, VN_FINANCE_INITIAL_DURATION);
					//Timestamp tsFinanceMgrddate = new Timestamp(cal.getTimeInMillis());
					Date finMgrDDate = new Date();
					finMgrDDate.setTime(cal.getTimeInMillis());
					currentRequest.setDueDate(finMgrDDate);
					
				}
				
				// case 4 - Planning Mgr, action not submitted
				if((billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SUBCONTRACTORS)
						|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_PRW))
						&& !(actionType.equalsIgnoreCase(VN_ACTION_SUBMITTED))) {
					try {
						// set action pending
						currentRequest.setObject(VN_ACTION, typeActionPending);
						// set Pending date
						currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
						// set assignee
						setAssignee(VN_LOGIN_PLANNING_MGR, currentRequest, sysId);
						
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
						return ruleResult;
					}
					// set pending with
					currentRequest.setRequestTypeId(pendingWithPlanningMgr);
					
					// set due date to be T+2
					cal.setTimeInMillis(nowDatenTime.getTime());
					cal.add(Calendar.DAY_OF_MONTH, VN_PLANNINGMGR_DURATION);
					//Timestamp tsPlanningMgrddate = new Timestamp(cal.getTimeInMillis());
					Date planingMgrDDate = new Date();
					planingMgrDDate.setTime(cal.getTimeInMillis());
					currentRequest.setDueDate(planingMgrDDate);
					
				}
				
				// case 5 - Planning mgr, action > submitted
				if((billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SUBCONTRACTORS)
						|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_PRW))
						&& actionType.equalsIgnoreCase(VN_ACTION_SUBMITTED)) {
					// mandatory fields MRN, Memorandum Vendor Bill
					String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC);; //string return in json form
					Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
					if (exMRN.isEmpty())
						warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
					String jsonMemoVnBill = currentRequest.get(VN_ATTCH_MEMO_VN_BILL); // string return in json form
					Collection<AttachmentInfo> exMemoVnBill = AttachmentInfo.fromJson(jsonMemoVnBill);
					if (exMemoVnBill.isEmpty())
						warningString = "Attach Memorandum Vendor bill.";
					try {
						if(currentRequest.getObject(VN_WORK_ORDER_SUMMARY) != null){
							String woSummary = currentRequest.get(VN_WORK_ORDER_SUMMARY);
							if( woSummary.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY1)
									|| woSummary.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY2)
										||!(woSummary.length() > 0))
								warningString = "Give Details of Work Order Summary.";
						} else {
							warningString = "Work Order Summary details is null.";
						}
						if( ((Double)currentRequest.getObject(VN_TOTAL_WORKORDER_VALUE)).equals(ZEROVALUE) )
							warningString = "Enter Total Work Order Value.";
						if ( currentRequest.getObject(VN_VENDOR_INVOICE_NO) == null)
							warningString = "Enter Vendor Invoice Number.";
						else {
							if(!(currentRequest.get(VN_VENDOR_INVOICE_NO).trim().length() > 0))
								warningString = "Enter Valid Vendor Invoice Number.";
						}
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
					// pending with Accounts mgr
					try {
						// set action pending
						currentRequest.setObject(VN_FINANCE_ACTION, typeFinActionPending);
						// set Pending date
						currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
						// set assignee
						setAssignee(VN_LOGIN_FINANCE_MGR, currentRequest, sysId);
						// generate and set Work order Number
						workOrderNumber = genWorkOrderNumber (currentRequest, sysId, connection, nowDatenTime);
						currentRequest.setObject(VN_WORK_ORDER_NUMBER, workOrderNumber);
						
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
						return ruleResult;
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					} catch (SQLException se) {
						se.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured while retrieving maxid for running serial No. "+se.getMessage());
						return ruleResult;
					}
					// set pending with
					currentRequest.setRequestTypeId(pendingWithAccountsMgr);
					// set due date to be T+2
					cal.setTimeInMillis(nowDatenTime.getTime());
					cal.add(Calendar.DAY_OF_MONTH, VN_FINANCE_INITIAL_DURATION);
					Date finMgrDDate = new Date();
					finMgrDDate.setTime(cal.getTimeInMillis());
					currentRequest.setDueDate(finMgrDDate);
					
				}
				
				
			}
			
			
			// if update request
			if(!isAddRequest){
				String pendingWith = currentRequest.getRequestTypeId().getName();
				//String corresNo = null;
				String financeAction = null;
				String sitePrjCoordAction = null;
				String sitePrjMgrAction = null;
				Type statusClosed = null;
				Type pendingWithNone = null;
				boolean isGenCoverLetter = false;
				boolean isPaymentReleased = false;
				
				Type typeSitePrjCActPending = null;
				Type typeSitePrjCActNone = null;
				Type typeSitePrjMActPending = null;
				Type typeStatusSuspended = null;
				
				try {
					financeAction = currentRequest.get(VN_FINANCE_ACTION);
					sitePrjCoordAction = currentRequest.get(VN_SITEPRJ_COORD_ACTION);
					sitePrjMgrAction = currentRequest.get(VN_SITEPRJ_MGR_ACTION);
					statusClosed = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_STATUS, VN_STATUS_CLOSED);
					pendingWithNone = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_PENDING_WITH, VN_PENDINGWITH_NONE);
					// corresNo = currentRequest.get(VN_CORRESPONDANCE_NUMBER);
					
					isGenCoverLetter = ((Boolean)currentRequest.getObject(VN_GENERATE_COVER_LTR)).booleanValue();
					isPaymentReleased = ((Boolean)currentRequest.getObject(VN_PAYMENT_RELEASED_BOOLEAN)).booleanValue();
					
					typeSitePrjCActPending = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_SITEPRJ_COORD_ACTION, VN_SITEPRJ_COORD_ACTION_PENDING);
					typeSitePrjCActNone = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_SITEPRJ_COORD_ACTION, VN_SITEPRJ_COORD_ACTION_NONE);
					typeSitePrjMActPending = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_SITEPRJ_MGR_ACTION, VN_SITEPRJ_MGR_ACTION_PENDING);
					typeStatusSuspended = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, VN_STATUS, VN_STATUS_SUSPENDED);
					
				} catch (DatabaseException de) {
					de.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error Occured in getting Ex-Type, String. "+de.getMessage());
					return ruleResult;
				} catch (IllegalStateException ie) {
					ie.printStackTrace();
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Error Occured: "+ie.getMessage());
					return ruleResult;
				}
				
				// old data check for > during add request.
				// billing category, work order type, should not change 
				
				// Mandatory Fields
				if(!(currentRequest.getSubject().length() > 0))
					warningString = "Enter Subject field.";
				if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SELECT))
					warningString = "Select Billing Category.";
				if(workOrderType.getName().equalsIgnoreCase(VN_WORK_ORDER_TYPE_SELECT))
					warningString = "Select Work Order Type.";
				//if(workOrderGroupType.equalsIgnoreCase(VN_WORK_ORDER_GROUP_SELECT))
				//	warningString = "Select Work Order Group.";
				if (warningString != null){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage(warningString);
					return ruleResult;
				}
				
				if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SITESUPPLIES) ) {
					if(actionBy.equalsIgnoreCase(VN_ACTIONBY_PLANNING_MGR))
						warningString = "Stores Manager should initiate the Billing.";
				}
				if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SUBCONTRACTORS)
						|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_PRW)) {
					if(actionBy.equalsIgnoreCase(VN_ACTIONBY_STORES_MGR))
						warningString = "Planning Manager should initiate the Billing.";
				}
				if (warningString != null){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage(warningString);
					return ruleResult;
				}
				
				// pending with stores manager
				if(pendingWith.equalsIgnoreCase(VN_PENDINGWITH_STORES_MGR)){
					// creq upline data check
					try {
						warningString = creqFinInitialDataCheck(currentRequest, oldRequest);
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					}
					if( !(oldRequest.get(VN_WORK_ORDER_NUMBER).equalsIgnoreCase(currentRequest.get(VN_WORK_ORDER_NUMBER))) )
						warningString = "Work Order Number Should not change.";
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					// case1 - action by stores mgr and action not submitted
					if((billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SITESUPPLIES)
							|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_EXWKS_SUPPLIES))
							&& !(actionType.equalsIgnoreCase(VN_ACTION_SUBMITTED))) {
						
						try {
							if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SITESUPPLIES)){
								// Check for Resubmit - Fields value should not change
								if( currentRequest.get(VN_SITEPRJ_COORD_ACTION).equalsIgnoreCase(VN_SITEPRJ_COORD_ACTION_RESUBMIT)
										|| currentRequest.get(VN_SITEPRJ_MGR_ACTION).equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_RESUBMIT) ){
									if( !(oldRequest.getCategoryId().getName().equalsIgnoreCase(currentRequest.getCategoryId().getName())) )
										warningString = "Billing Category should not change.";
									if( !(oldRequest.getSeverityId().getName().equalsIgnoreCase(currentRequest.getSeverityId().getName())) )
										warningString = "Work Order Type Should not change.";
									if( !(oldRequest.get(VN_CONTRACT_REFERENCE).equalsIgnoreCase(currentRequest.get(VN_CONTRACT_REFERENCE))) )
										warningString = "Contract Reference should not change.";
								}
								if (warningString != null){
									ruleResult.setCanContinue(false);
									ruleResult.setMessage(warningString);
									return ruleResult;
								}
							}
							// set actionby Stores Mgr
							currentRequest.setObject(VN_ACTIONBY, typeActionByStoresMgr);
							// set action pending
							currentRequest.setObject(VN_ACTION, typeActionPending);
							// set assignee
							setAssignee(VN_LOGIN_STORES_MGR, currentRequest, sysId);
							
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
							return ruleResult;
						}
					}
					// case 2 - stores mgr, action > submitted
					if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_EXWKS_SUPPLIES)
							&& actionType.equalsIgnoreCase(VN_ACTION_SUBMITTED)) {
						// mandatory fields MRN
						String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC); //string return in json form
						Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
						if (exMRN.isEmpty())
							warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
						// Vendor Invoice No.
						if ( currentRequest.getObject(VN_VENDOR_INVOICE_NO) == null)
							warningString = "Enter Vendor Invoice Number.";
						else {
							if(!(currentRequest.get(VN_VENDOR_INVOICE_NO).trim().length() > 0))
								warningString = "Enter Valid Vendor Invoice Number.";
						}
						if (warningString != null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(warningString);
							return ruleResult;
						}
						// pending with planning mgr
						try {
							// set action by planning mgr
							currentRequest.setObject(VN_ACTIONBY, typeActionByPlngMgr);
							// set action pending
							currentRequest.setObject(VN_ACTION, typeActionPending);
							// set Pending date
							currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
							// set assignee
							setAssignee(VN_LOGIN_PLANNING_MGR, currentRequest, sysId);
							
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
							return ruleResult;
						}
						// set pending with
						currentRequest.setRequestTypeId(pendingWithPlanningMgr);
						
						// set due date to be T+2
						cal.setTimeInMillis(nowDatenTime.getTime());
						cal.add(Calendar.DAY_OF_MONTH, VN_PLANNINGMGR_DURATION);
						//Timestamp tsPlanningMgrddate = new Timestamp(cal.getTimeInMillis());
						Date planingMgrDDate = new Date();
						planingMgrDDate.setTime(cal.getTimeInMillis());
						currentRequest.setDueDate(planingMgrDDate);
						
					}
					// case 3 - stores mgr, action > submitted
					if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SITESUPPLIES)
							&& actionType.equalsIgnoreCase(VN_ACTION_SUBMITTED)) {
						// Check for Resubmit - Fields value should not change
						if( currentRequest.get(VN_SITEPRJ_COORD_ACTION).equalsIgnoreCase(VN_SITEPRJ_COORD_ACTION_RESUBMIT)
								|| currentRequest.get(VN_SITEPRJ_MGR_ACTION).equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_RESUBMIT) ){
							if( !(oldRequest.getCategoryId().getName().equalsIgnoreCase(currentRequest.getCategoryId().getName())) )
								warningString = "Billing Category should not change.";
							if( !(oldRequest.getSeverityId().getName().equalsIgnoreCase(currentRequest.getSeverityId().getName())) )
								warningString = "Work Order Type Should not change.";
							if( !(oldRequest.get(VN_CONTRACT_REFERENCE).equalsIgnoreCase(currentRequest.get(VN_CONTRACT_REFERENCE))) )
								warningString = "Contract Reference should not change.";
						}
						// mandatory fields MRN, Memorandum Vendor Bill
						String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC); //string return in json form
						Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
						if (exMRN.isEmpty())
							warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
						String jsonMemoVnBill = currentRequest.get(VN_ATTCH_MEMO_VN_BILL); // string return in json form
						Collection<AttachmentInfo> exMemoVnBill = AttachmentInfo.fromJson(jsonMemoVnBill);
						if (exMemoVnBill.isEmpty())
							warningString = "Attach Memorandum Vendor bill.";
						try {
							if(currentRequest.getObject(VN_WORK_ORDER_SUMMARY) != null){
								String woSummary = currentRequest.get(VN_WORK_ORDER_SUMMARY);
								if( woSummary.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY1)
										|| woSummary.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY2)
											||!(woSummary.length() > 0))
									warningString = "Give Details of Work Order Summary.";
							} else {
								warningString = "Work Order Summary details is null.";
							}
							if( ((Double)currentRequest.getObject(VN_TOTAL_WORKORDER_VALUE)).equals(ZEROVALUE) )
								warningString = "Enter Total Work Order Value.";
							if ( currentRequest.getObject(VN_VENDOR_INVOICE_NO) == null)
								warningString = "Enter Vendor Invoice Number.";
							else {
								if(!(currentRequest.get(VN_VENDOR_INVOICE_NO).trim().length() > 0))
									warningString = "Enter Valid Vendor Invoice Number.";
							}
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
						
						// pending with Accounts mgr
						try {
							// set action pending
							currentRequest.setObject(VN_FINANCE_ACTION, typeFinActionPending);
							// set Pending date
							currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
							// set assignee
							setAssignee(VN_LOGIN_FINANCE_MGR, currentRequest, sysId);
							// generate and set Work order Number
							if( !(currentRequest.get(VN_SITEPRJ_COORD_ACTION).equalsIgnoreCase(VN_SITEPRJ_COORD_ACTION_RESUBMIT)
									|| currentRequest.get(VN_SITEPRJ_MGR_ACTION).equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_RESUBMIT)) ){
								workOrderNumber = genWorkOrderNumber (currentRequest, sysId, connection, nowDatenTime);
								currentRequest.setObject(VN_WORK_ORDER_NUMBER, workOrderNumber);
							}
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
							return ruleResult;
						} catch (IllegalStateException ie) {
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						} catch (SQLException se) {
							se.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while retrieving maxid for running serial No. "+se.getMessage());
							return ruleResult;
						}
						// set pending with accounts mgr
						currentRequest.setRequestTypeId(pendingWithAccountsMgr);
						// set due date to be T+2
						cal.setTimeInMillis(nowDatenTime.getTime());
						cal.add(Calendar.DAY_OF_MONTH, VN_FINANCE_INITIAL_DURATION);
						Date finMgrDDate = new Date();
						finMgrDDate.setTime(cal.getTimeInMillis());
						currentRequest.setDueDate(finMgrDDate);
					}
					
				}
				
				// pending with planning mgr
				if(pendingWith.equalsIgnoreCase(VN_PENDINGWITH_PLANNING_MGR)) {
					// set actionby Planning Mgr
					currentRequest.setObject(VN_ACTIONBY, typeActionByPlngMgr);
					// creq upline data check
					try {
						warningString = creqFinInitialDataCheck(currentRequest, oldRequest);
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					}
					if( !(oldRequest.get(VN_WORK_ORDER_NUMBER).equalsIgnoreCase(currentRequest.get(VN_WORK_ORDER_NUMBER))) )
						warningString = "Work Order Number Should not change.";
					// Check for Resubmit - Fields value should not change
					if( currentRequest.get(VN_SITEPRJ_COORD_ACTION).equalsIgnoreCase(VN_SITEPRJ_COORD_ACTION_RESUBMIT)
							|| currentRequest.get(VN_SITEPRJ_MGR_ACTION).equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_RESUBMIT) ){
						if( !(oldRequest.getCategoryId().getName().equalsIgnoreCase(currentRequest.getCategoryId().getName())) )
							warningString = "Billing Category should not change.";
						if( !(oldRequest.getSeverityId().getName().equalsIgnoreCase(currentRequest.getSeverityId().getName())) )
							warningString = "Work Order Type Should not change.";
						if( !(oldRequest.get(VN_CONTRACT_REFERENCE).equalsIgnoreCase(currentRequest.get(VN_CONTRACT_REFERENCE))) )
							warningString = "Contract Reference should not change.";
					}
					
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					// case 4 - Planning Mgr, action not submitted
					if( (billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SUBCONTRACTORS)
							|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_PRW)
								|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_EXWKS_SUPPLIES))
							&& !(actionType.equalsIgnoreCase(VN_ACTION_SUBMITTED)) ) {
						try {
							//  not able to change stores manager data when ex-works supplies
							if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_EXWKS_SUPPLIES)) {
								warningString = oreqStoresMgrDataCheck(currentRequest, oldRequest);
								if (warningString != null){
									ruleResult.setCanContinue(false);
									ruleResult.setMessage(warningString);
									return ruleResult;
								}
							}
							// set action pending
							currentRequest.setObject(VN_ACTION, typeActionPending);
							// set assignee
							setAssignee(VN_LOGIN_PLANNING_MGR, currentRequest, sysId);
							
						} catch (IllegalStateException ie) {
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
							return ruleResult;
						}
							
					}
					
					// case 5 - Planning mgr, action > submitted
					if( (billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_SUBCONTRACTORS)
							|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_PRW)
								|| billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_EXWKS_SUPPLIES))
							&& actionType.equalsIgnoreCase(VN_ACTION_SUBMITTED) ) {
						
						// not able to change stores manager data when ex-works supplies
						if(billingCategory.equalsIgnoreCase(VN_BILLING_CATEGORY_EXWKS_SUPPLIES)) {
							try {
								warningString = oreqStoresMgrDataCheck(currentRequest, oldRequest);
							} catch (IllegalStateException ie) {
								ie.printStackTrace();
								ruleResult.setCanContinue(false);
								ruleResult.setMessage("Error Occured: "+ie.getMessage());
								return ruleResult;
							} catch (DatabaseException de) {
								de.printStackTrace();
								ruleResult.setCanContinue(false);
								ruleResult.setMessage("Error Occured in getting Ex-Fields. "+de.getMessage());
								return ruleResult;
							}
							if (warningString != null){
								ruleResult.setCanContinue(false);
								ruleResult.setMessage(warningString);
								return ruleResult;
							}
						}
						
						// mandatory fields MRN, Memorandum Vendor Bill
						String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC); //string return in json form
						Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
						if (exMRN.isEmpty())
							warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
						String jsonMemoVnBill = currentRequest.get(VN_ATTCH_MEMO_VN_BILL); // string return in json form
						Collection<AttachmentInfo> exMemoVnBill = AttachmentInfo.fromJson(jsonMemoVnBill);
						if (exMemoVnBill.isEmpty())
							warningString = "Attach Memorandum Vendor bill.";
						try {
							if(currentRequest.getObject(VN_WORK_ORDER_SUMMARY) != null){
								String woSummary = currentRequest.get(VN_WORK_ORDER_SUMMARY);
								if( woSummary.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY1)
										|| woSummary.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY2)
											||!(woSummary.length() > 0))
									warningString = "Give Details of Work Order Summary.";
							} else {
								warningString = "Work Order Summary details is null.";
							}
							if( ((Double)currentRequest.getObject(VN_TOTAL_WORKORDER_VALUE)).equals(ZEROVALUE) )
								warningString = "Enter Total Work Order Value.";
							if ( currentRequest.getObject(VN_VENDOR_INVOICE_NO) == null)
								warningString = "Enter Vendor Invoice Number.";
							else {
								if(!(currentRequest.get(VN_VENDOR_INVOICE_NO).trim().length() > 0))
									warningString = "Enter Valid Vendor Invoice Number.";
							}
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
						
						// pending with Accounts mgr
						try {
							// set action pending
							currentRequest.setObject(VN_FINANCE_ACTION, typeFinActionPending);
							// set Pending date
							currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
							// set assignee
							setAssignee(VN_LOGIN_FINANCE_MGR, currentRequest, sysId);
							// generate and set Work order Number
							if( !(currentRequest.get(VN_SITEPRJ_COORD_ACTION).equalsIgnoreCase(VN_SITEPRJ_COORD_ACTION_RESUBMIT)
									|| currentRequest.get(VN_SITEPRJ_MGR_ACTION).equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_RESUBMIT)) ){
								workOrderNumber = genWorkOrderNumber (currentRequest, sysId, connection, nowDatenTime);
								currentRequest.setObject(VN_WORK_ORDER_NUMBER, workOrderNumber);
							}
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
							return ruleResult;
						} catch (IllegalStateException ie) {
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						} catch (SQLException se) {
							se.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured while retrieving maxid for running serial No. "+se.getMessage());
							return ruleResult;
						}
						// set pending with
						currentRequest.setRequestTypeId(pendingWithAccountsMgr);
						// set due date to be T+2
						cal.setTimeInMillis(nowDatenTime.getTime());
						cal.add(Calendar.DAY_OF_MONTH, VN_FINANCE_INITIAL_DURATION);
						Date finMgrDDate = new Date();
						finMgrDDate.setTime(cal.getTimeInMillis());
						currentRequest.setDueDate(finMgrDDate);
						
					}
					
				}
				
				// pending with accounts
				if(pendingWith.equalsIgnoreCase(VN_PENDINGWITH_ACCOUNTS_MGR) &&
						!(sitePrjMgrAction.equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_APPROVED)) ) {
					
					try {
						// upline dept data should not fill
						warningString = creqSPrjCoordInitialDataCheck(currentRequest, oldRequest);
						// stores n planning mgr Data Check
						if (warningString == null)
							warningString = oreqStoresNPlngMgrDataCheck(currentRequest, oldRequest);
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					}
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					// if not submitted
					if(financeAction.equalsIgnoreCase(VN_FINANCE_ACTION_NONE)
							|| financeAction.equalsIgnoreCase(VN_FINANCE_ACTION_PENDING)){
						
						try {
							currentRequest.setObject(VN_FINANCE_ACTION, typeFinActionPending);
							setAssignee(VN_LOGIN_FINANCE_MGR, currentRequest, sysId);
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-Type. "+de.getMessage());
							return ruleResult;
						}
						
					}
					// if submitted
					if(financeAction.equalsIgnoreCase(VN_FINANCE_ACTION_APPROVED)){
						
						// mandatory Check
						String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC); //string return in json form
						Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
						if (exMRN.isEmpty())
							warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
						String jsonMemoVnBill = currentRequest.get(VN_ATTCH_MEMO_VN_BILL); // string return in json form
						Collection<AttachmentInfo> exMemoVnBill = AttachmentInfo.fromJson(jsonMemoVnBill);
						if (exMemoVnBill.isEmpty())
							warningString = "Attach Memorandum Vendor bill.";
						try {
							if( ((Double)currentRequest.getObject(VN_TOTAL_TAXES_APPLICABLE)).equals(ZEROVALUE) )
								warningString = "Enter Total Taxes Applicable.";
							if( ((Double)currentRequest.getObject(VN_TOTAl_OTHER_DED)).equals(ZEROVALUE) )
								warningString = "Enter Total Other Deductions.";
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
						try {
							double totalWOVal = ((Double)currentRequest.getObject(VN_TOTAL_WORKORDER_VALUE)).doubleValue();
							double taxval = ((Double)currentRequest.getObject(VN_TOTAL_TAXES_APPLICABLE)).doubleValue();
							double otherdedVal = ((Double)currentRequest.getObject(VN_TOTAl_OTHER_DED)).doubleValue();
							//double sumtotalVal = taxval + otherdedVal;

							double netPayable = totalWOVal + taxval - otherdedVal;
							currentRequest.setObject(VN_NETPAYABLE, netPayable);
						} catch (IllegalStateException ie) {
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}
						// pending with site project coordinator
						try {
							// set action pending
							currentRequest.setObject(VN_SITEPRJ_COORD_ACTION, typeSitePrjCActPending);
							// set Pending date
							currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
							// set assignee
							setAssignee(VN_LOGIN_SITE_PROJECT_COORD, currentRequest, sysId);
							
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
							return ruleResult;
						} catch (IllegalStateException ie) {
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}
						// set pending with site project coord
						currentRequest.setRequestTypeId(pendingWithSitePrjCoord);
						// set due date to be T+1
						cal.setTimeInMillis(nowDatenTime.getTime());
						cal.add(Calendar.DAY_OF_MONTH, VN_SITEPROJECT_COORD_DURATION);
						Date prjCoordDDate = new Date();
						prjCoordDDate.setTime(cal.getTimeInMillis());
						currentRequest.setDueDate(prjCoordDDate);
						
					}
					
				}
				
				// pending with site prj coordinator
				if(pendingWith.equalsIgnoreCase(VN_PENDINGWITH_SITEPRJ_COORD)
						&& !(sitePrjMgrAction.equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_APPROVED))) {
					try {
						// upline dept data should not fill
						warningString = creqSPrjMgrDataCheck(currentRequest, oldRequest);
						// Finance mgr Data Check
						if (warningString == null)
							warningString = oreqFinMgrDataCheck(currentRequest, oldRequest);
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					}
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					// mandatory check
					String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC); //string return in json form
					Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
					if (exMRN.isEmpty())
						warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
					String jsonMemoVnBill = currentRequest.get(VN_ATTCH_MEMO_VN_BILL); // string return in json form
					Collection<AttachmentInfo> exMemoVnBill = AttachmentInfo.fromJson(jsonMemoVnBill);
					if (exMemoVnBill.isEmpty())
						warningString = "Attach Memorandum Vendor bill.";
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					// if approved
					if(sitePrjCoordAction.equalsIgnoreCase(VN_SITEPRJ_COORD_ACTION_APPROVED)) {
						// pending with site project Mgr
						try {
							// set action pending
							currentRequest.setObject(VN_SITEPRJ_MGR_ACTION, typeSitePrjMActPending);
							// set Pending date
							currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
							// set assignee
							setAssignee(VN_LOGIN_SITE_PROJECT_MGR, currentRequest, sysId);
							
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
							return ruleResult;
						} catch (IllegalStateException ie) {
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}
						// set pending with site project Mgr
						currentRequest.setRequestTypeId(pendingWithSitePrjMgr);
						// set due date to be T+1
						cal.setTimeInMillis(nowDatenTime.getTime());
						cal.add(Calendar.DAY_OF_MONTH, VN_SITEPRJ_MGR_DURATION);
						Date sitePrjMgrDDate = new Date();
						sitePrjMgrDDate.setTime(cal.getTimeInMillis());
						currentRequest.setDueDate(sitePrjMgrDDate);
						
					}
					
					// if resubmit
					if(sitePrjCoordAction.equalsIgnoreCase(VN_SITEPRJ_COORD_ACTION_RESUBMIT)) {
						// description cannot be empty
						if(currentRequest.getDescription() != null){
							String descBoxString = currentRequest.getDescription().trim();
							if ( descBoxString.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY1)
									|| descBoxString.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY2)
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
						try {
							//currentRequest.setObject(VN_SITEPRJ_COORD_ACTION, typeSitePrjCActNone);
							if (currentRequest.getCategoryId().getName().equalsIgnoreCase(VN_BILLING_CATEGORY_SITESUPPLIES)) {
								// set pending with
								currentRequest.setRequestTypeId(pendingWithStoresMgr);
								// set action by stores mgr
								currentRequest.setObject(VN_ACTIONBY, typeActionByStoresMgr);
								currentRequest.setObject(VN_ACTION, typeActionPending);
								// set assignee
								setAssignee(VN_LOGIN_STORES_MGR, currentRequest, sysId);
								// set due date to be T+2
								cal.setTimeInMillis(nowDatenTime.getTime());
								cal.add(Calendar.DAY_OF_MONTH, VN_STORESMGR_DURATION);
								Date storesMgrDDate = new Date();
								storesMgrDDate.setTime(cal.getTimeInMillis());
								currentRequest.setDueDate(storesMgrDDate);
								
							} else {
								// set pending with
								currentRequest.setRequestTypeId(pendingWithPlanningMgr);
								// set action by planning mgr
								currentRequest.setObject(VN_ACTIONBY, typeActionByPlngMgr);
								currentRequest.setObject(VN_ACTION, typeActionPending);
								// set assignee
								setAssignee(VN_LOGIN_PLANNING_MGR, currentRequest, sysId);
								// set due date to be T+2
								cal.setTimeInMillis(nowDatenTime.getTime());
								cal.add(Calendar.DAY_OF_MONTH, VN_PLANNINGMGR_DURATION);
								//Timestamp tsPlanningMgrDdate = new Timestamp(cal.getTimeInMillis());
								Date planingMgrDDate = new Date();
								planingMgrDDate.setTime(cal.getTimeInMillis());
								currentRequest.setDueDate(planingMgrDDate);
							}
							// set Pending date
							currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
							// data resets to null
							currentRequest.setObject(VN_NETPAYABLE, 0.0);
							currentRequest.setObject(VN_TOTAl_OTHER_DED, 0.0);
							currentRequest.setObject(VN_TOTAL_TAXES_APPLICABLE, 0.0);
							currentRequest.setObject(VN_FINANCE_ACTION, typeFinActionNone);
							
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
							return ruleResult;
						}
						
						// pending with stores / planning mgr
						// data resets to null
						// due date, pending from
						// set assignee
						// set action pending, set action by
						// set none for site project coordinator
						
					}
					
					// If suspend
					if(sitePrjCoordAction.equalsIgnoreCase(VN_SITEPRJ_COORD_ACTION_SUSPEND)) {
						// description cannot be empty
						if(currentRequest.getDescription() != null){
							String descBoxString = currentRequest.getDescription().trim();
							if ( descBoxString.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY1)
									|| descBoxString.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY2)
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
						currentRequest.setStatusId(typeStatusSuspended);
						currentRequest.setRequestTypeId(pendingWithNone);
						currentRequest.setObject(VN_DUE_DATE, null);
						currentRequest.setObject(VN_PENDING_DATE, null);
						currentRequest.setAssignees(null);
					}
					
				}
				
				// pending with site project mgr
				if(pendingWith.equalsIgnoreCase(VN_PENDINGWITH_SITEPRJ_MGR)) {
					try {
						// upline dept data should not fill
						warningString = creqSPrjCoordFinalDataCheck(currentRequest);
						// site project coord initial Data Check
						if (warningString == null)
							warningString = oreqSPrjCoordInitialDataCheck(currentRequest, oldRequest);
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					}
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					// mandatory check
					String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC); //string return in json form
					Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
					if (exMRN.isEmpty())
						warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
					String jsonMemoVnBill = currentRequest.get(VN_ATTCH_MEMO_VN_BILL); // string return in json form
					Collection<AttachmentInfo> exMemoVnBill = AttachmentInfo.fromJson(jsonMemoVnBill);
					if (exMemoVnBill.isEmpty())
						warningString = "Attach Memorandum Vendor bill.";
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					
					// if pending
					
					// if approved
					if(sitePrjMgrAction.equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_APPROVED)) {
						// pending with site project coord
						try {
							// set Pending date
							currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
							// set assignee
							setAssignee(VN_LOGIN_SITE_PROJECT_COORD, currentRequest, sysId);
							
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-datetime. "+de.getMessage());
							return ruleResult;
						} catch (IllegalStateException ie) {
							ie.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured: "+ie.getMessage());
							return ruleResult;
						}
						// set pending with site project Coord
						currentRequest.setRequestTypeId(pendingWithSitePrjCoord);
						// set due date to be T+1
						cal.setTimeInMillis(nowDatenTime.getTime());
						cal.add(Calendar.DAY_OF_MONTH, VN_SITEPROJECT_COORD_DURATION);
						Date sitePrjCoordDDate = new Date();
						sitePrjCoordDDate.setTime(cal.getTimeInMillis());
						currentRequest.setDueDate(sitePrjCoordDDate);
						
					}
					
					// if resubmit
					if(sitePrjMgrAction.equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_RESUBMIT)) {
						// description cannot be empty
						if(currentRequest.getDescription() != null){
							String descBoxString = currentRequest.getDescription().trim();
							if ( descBoxString.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY1)
									|| descBoxString.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY2)
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
						try {
							currentRequest.setObject(VN_SITEPRJ_COORD_ACTION, typeSitePrjCActNone);
							//currentRequest.setObject(VN_SITEPRJ_MGR_ACTION, typeSitePrjMActNone);
							if (currentRequest.getCategoryId().getName().equalsIgnoreCase(VN_BILLING_CATEGORY_SITESUPPLIES)) {
								// set pending with
								currentRequest.setRequestTypeId(pendingWithStoresMgr);
								// set action by stores mgr
								currentRequest.setObject(VN_ACTIONBY, typeActionByStoresMgr);
								currentRequest.setObject(VN_ACTION, typeActionPending);
								// set assignee
								setAssignee(VN_LOGIN_STORES_MGR, currentRequest, sysId);
								// set due date to be T+2
								cal.setTimeInMillis(nowDatenTime.getTime());
								cal.add(Calendar.DAY_OF_MONTH, VN_STORESMGR_DURATION);
								Date storesMgrDDate = new Date();
								storesMgrDDate.setTime(cal.getTimeInMillis());
								currentRequest.setDueDate(storesMgrDDate);
								
							} else {
								// set pending with
								currentRequest.setRequestTypeId(pendingWithPlanningMgr);
								// set action by planning mgr
								currentRequest.setObject(VN_ACTIONBY, typeActionByPlngMgr);
								currentRequest.setObject(VN_ACTION, typeActionPending);
								// set assignee
								setAssignee(VN_LOGIN_PLANNING_MGR, currentRequest, sysId);
								// set due date to be T+2
								cal.setTimeInMillis(nowDatenTime.getTime());
								cal.add(Calendar.DAY_OF_MONTH, VN_PLANNINGMGR_DURATION);
								Date planingMgrDDate = new Date();
								planingMgrDDate.setTime(cal.getTimeInMillis());
								currentRequest.setDueDate(planingMgrDDate);
							}
							// set Pending date
							currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
							// data resets to null
							currentRequest.setObject(VN_NETPAYABLE, 0.0);
							currentRequest.setObject(VN_TOTAl_OTHER_DED, 0.0);
							currentRequest.setObject(VN_TOTAL_TAXES_APPLICABLE, 0.0);
							currentRequest.setObject(VN_FINANCE_ACTION, typeFinActionNone);
							
						} catch (DatabaseException de) {
							de.printStackTrace();
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Error Occured in setting Ex-Type, datetime. "+de.getMessage());
							return ruleResult;
						}
						
					}
					
					// If suspend
					if(sitePrjMgrAction.equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_SUSPEND)) {
						// description cannot be empty
						if(currentRequest.getDescription() != null){
							String descBoxString = currentRequest.getDescription().trim();
							if ( descBoxString.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY1)
									|| descBoxString.equalsIgnoreCase(VN_WORKORDER_SUMMARY_EMPTY2)
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
						currentRequest.setStatusId(typeStatusSuspended);
						currentRequest.setRequestTypeId(pendingWithNone);
						currentRequest.setObject(VN_DUE_DATE, null);
						currentRequest.setObject(VN_PENDING_DATE, null);
						currentRequest.setAssignees(null);
					}
					
				}
				                                                              
				
				// pending with site prj coordinator for correspondence number generation
				if(pendingWith.equalsIgnoreCase(VN_PENDINGWITH_SITEPRJ_COORD)
						&& sitePrjMgrAction.equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_APPROVED)) {
					
					try {
						// upline dept data should not fill
						warningString = creqFinFinalDataCheck(currentRequest);
						// site project mgr Data Check
						if (warningString == null)
							warningString = oreqSPrjMgrDataCheck(currentRequest, oldRequest);
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					}
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					// mandatory check
					String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC); //string return in json form
					Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
					if (exMRN.isEmpty())
						warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
					String jsonMemoVnBill = currentRequest.get(VN_ATTCH_MEMO_VN_BILL); // string return in json form
					Collection<AttachmentInfo> exMemoVnBill = AttachmentInfo.fromJson(jsonMemoVnBill);
					if (exMemoVnBill.isEmpty())
						warningString = "Attach Memorandum Vendor bill.";
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					
					// generate Cover Letter
					if(isGenCoverLetter) {
						// gen Correspondance No. then attach PDF , Preview PDF option will also be given
						
						try {
							currentRequest.setObject(VN_CORRESPONDANCE_NUMBER, "NPT10109/Corres/Number/String/SerialNo");
							// gen PDF File function if attachment null > Error rule.
							String fileType = "pdf";
							
							warningString = GenNAttachFile(fileType, currentRequest, sysId, baPrefix);
							if (warningString != null){
								ruleResult.setCanContinue(false);
								ruleResult.setMessage(warningString);
								return ruleResult;
							}
							
							// set Pending with, duedate, pending from, assignee accounts mgr
							currentRequest.setRequestTypeId(pendingWithAccountsMgr);
							setAssignee(VN_LOGIN_FINANCE_MGR, currentRequest, sysId);
							
							// set due date to be T+1, Pending From current
							currentRequest.setObject(VN_PENDING_DATE, nowDatenTime);
							cal.setTimeInMillis(nowDatenTime.getTime());
							cal.add(Calendar.DAY_OF_MONTH, VN_FINANCE_LATER_DURATION);
							Date finMgrDDate = new Date();
							finMgrDDate.setTime(cal.getTimeInMillis());
							currentRequest.setDueDate(finMgrDDate);
							
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
				
				// pending with accounts
				if(pendingWith.equalsIgnoreCase(VN_PENDINGWITH_ACCOUNTS_MGR)
						&& sitePrjMgrAction.equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_APPROVED)) {
					try {
						// old req site project coord final Data Check
						warningString = oreqSPrjCoordFinalDataCheck(currentRequest, oldRequest);
					} catch (IllegalStateException ie) {
						ie.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured: "+ie.getMessage());
						return ruleResult;
					} catch (DatabaseException de) {
						de.printStackTrace();
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Error Occured in getting Ex-Fields. "+de.getMessage());
						return ruleResult;
					}
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					// mandatory check MRN, Memo Vn Bill, vendor payment cover letter
					String jsonMRN = currentRequest.get(VN_ATTCH_MRN_CERT_BILL_DC); //string return in json form
					Collection<AttachmentInfo> exMRN = AttachmentInfo.fromJson(jsonMRN);
					if (exMRN.isEmpty())
						warningString = "Attach Scanned Copy MRN, Certificates, Bill, DC, etc.";
					String jsonMemoVnBill = currentRequest.get(VN_ATTCH_MEMO_VN_BILL); // string return in json form
					Collection<AttachmentInfo> exMemoVnBill = AttachmentInfo.fromJson(jsonMemoVnBill);
					if (exMemoVnBill.isEmpty())
						warningString = "Attach Memorandum Vendor bill.";
					String jsonVnPymtCvrLtr = currentRequest.get(VN_ATTCH_VN_PYMT_COVER_LTR); // string return in json form
					Collection<AttachmentInfo> exVnPymtCvrLtr = AttachmentInfo.fromJson(jsonVnPymtCvrLtr);
					if (exVnPymtCvrLtr.isEmpty())
						warningString = "Attach Vendor Payment Cover Letter.";
					if (warningString != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage(warningString);
						return ruleResult;
					}
					
					// payment released yes
					if(isPaymentReleased){
						Date pymtReldate = (Date)currentRequest.getObject(VN_PAYMENT_RELEASED_DATE);
						if(pymtReldate == null) {
							warningString = "Enter Payment Release date.";
							if (warningString != null){
								ruleResult.setCanContinue(false);
								ruleResult.setMessage(warningString);
								return ruleResult;
							}
						} else {
							// payment cheque scanned copy
							String jsonScnCopyPymtChq = currentRequest.get(VN_ATTCH_SCANNED_COPY_CHEQUE); // string return in json form
							Collection<AttachmentInfo> exScnCopyPymtChq = AttachmentInfo.fromJson(jsonScnCopyPymtChq);
							if (exScnCopyPymtChq.isEmpty())
								warningString = "Attach Scanned copy of payment Cheque.";
							// Check for past date only
							//if(pymtReldate.getTime() > nowDatenTime.getTime())
							//	warningString = "Can't give Payment Released date as Future date.";
							if (warningString != null){
								ruleResult.setCanContinue(false);
								ruleResult.setMessage(warningString);
								return ruleResult;
							}
							
							// gen and attach final report
							
							// close the invoice
							currentRequest.setStatusId(statusClosed);
							currentRequest.setRequestTypeId(pendingWithNone);
							currentRequest.setObject(VN_DUE_DATE, null);
							currentRequest.setAssignees(null);
							try {
								currentRequest.setObject(VN_PENDING_DATE, null);
							}catch (IllegalStateException ie){
								ie.printStackTrace();
								ruleResult.setCanContinue(false);
								ruleResult.setMessage("Error Occured: "+ie.getMessage());
								return ruleResult;
							}
							
						}
						
					}
					
				}
				
				// if closed & pending with none
				if(pendingWith.equalsIgnoreCase(VN_PENDINGWITH_NONE)
						&& currentRequest.getStatusId().getName().equalsIgnoreCase(VN_STATUS_CLOSED)) {
					warningString = "WorkOrder Request Closed.";
					ruleResult.setCanContinue(false);
					ruleResult.setMessage(warningString);
					return ruleResult;
				}
				
				// if suspended & pending with none
				if(pendingWith.equalsIgnoreCase(VN_PENDINGWITH_NONE)
						&& currentRequest.getStatusId().getName().equalsIgnoreCase(VN_STATUS_SUSPENDED)) {
					warningString = "WorkOrder Request Suspended, Create Sub-Request.";
					ruleResult.setCanContinue(false);
					ruleResult.setMessage(warningString);
					return ruleResult;
				}
				
			}
			
			
		}else
			ruleResult.setMessage("Vendor Billing Rule not applicable to current BA: "+baPrefix);
		
		return ruleResult;
	}
	
	
	// old request Stores Mgr Data Check.
	private String oreqStoresMgrDataCheck(Request cRequest, Request oRequest) 
			throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(oRequest.getCategoryId().getName().equalsIgnoreCase(cRequest.getCategoryId().getName())))
			warningString = "Billing Category Changed.";
		if(!(oRequest.getSeverityId().getName().equalsIgnoreCase(cRequest.getSeverityId().getName())))
			warningString = "Work Order Type Changed.";
		if(!(oRequest.get(VN_VENDOR_INVOICE_NO).equalsIgnoreCase(cRequest.get(VN_VENDOR_INVOICE_NO))))
			warningString = "Vendor Invoice Number Changed.";
		if( ((Date)oRequest.getObject(VN_VENDOR_INVOICE_DATED)) != null) {
			String oreqDatePart = oRequest.get(VN_VENDOR_INVOICE_DATED).split("\\s")[0];
			if( ((Date)cRequest.getObject(VN_VENDOR_INVOICE_DATED)) != null){
				String creqDatePart = cRequest.get(VN_VENDOR_INVOICE_DATED).split("\\s")[0];
				if( !(oreqDatePart.equalsIgnoreCase(creqDatePart)) )
					warningString = "Vendor Invoice Dated Changed.";
			}else
				warningString = "Enter Vendor Invoice Dated.";
		}
		
		return warningString;
	}


	// old request Stores / Planning Mgr data Check
	private String oreqStoresNPlngMgrDataCheck(Request cRequest,
			Request oRequest)  throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(oRequest.getCategoryId().getName().equalsIgnoreCase(cRequest.getCategoryId().getName())))
			warningString = "Billing Category Changed.";
		if(!(oRequest.getSeverityId().getName().equalsIgnoreCase(cRequest.getSeverityId().getName())))
			warningString = "Work Order Type Changed.";
		if(!(oRequest.get(VN_VENDOR_INVOICE_NO).equalsIgnoreCase(cRequest.get(VN_VENDOR_INVOICE_NO))))
			warningString = "Vendor Invoice Number Changed.";
		if(!(oRequest.get(VN_WORK_ORDER_NUMBER).equalsIgnoreCase(cRequest.get(VN_WORK_ORDER_NUMBER))))
			warningString = "Work Order Number Changed.";
		if(!(oRequest.get(VN_WORK_ORDER_GROUP).equalsIgnoreCase(cRequest.get(VN_WORK_ORDER_GROUP))))
			warningString = "Work Order Group Changed.";
		if(!(oRequest.get(VN_CONTRACT_REFERENCE).equalsIgnoreCase(cRequest.get(VN_CONTRACT_REFERENCE))))
			warningString = "Contract Reference Changed.";
		if(!( ((Double)oRequest.getObject(VN_TOTAL_WORKORDER_VALUE)).equals( ((Double)cRequest.getObject(VN_TOTAL_WORKORDER_VALUE)) ) ))
			warningString = "Total Work Order value Changed.";
		if(!(oRequest.get(VN_ACTIONBY).equalsIgnoreCase(cRequest.get(VN_ACTIONBY))))
			warningString = "Action By Changed.";
		if(!(oRequest.get(VN_ACTION).equalsIgnoreCase(cRequest.get(VN_ACTION))))
			warningString = "Action Changed.";
		return warningString;
	}
	
	// old request Finance Mgr Data Check
	private String oreqFinMgrDataCheck(Request cRequest,
			Request oRequest)  throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!( ((Double)oRequest.getObject(VN_TOTAL_TAXES_APPLICABLE)).equals( ((Double)cRequest.getObject(VN_TOTAL_TAXES_APPLICABLE)) ) ))
			warningString = "Total Taxes Applicable Changed.";
		if(!( ((Double)oRequest.getObject(VN_TOTAl_OTHER_DED)).equals( ((Double)cRequest.getObject(VN_TOTAl_OTHER_DED)) ) ))
			warningString = "Total Other Deductions Changed.";
		if(!( ((Double)oRequest.getObject(VN_NETPAYABLE)).equals( ((Double)cRequest.getObject(VN_NETPAYABLE)) ) ))
			warningString = "NetPayable Changed.";
		if(!(oRequest.get(VN_FINANCE_ACTION).equalsIgnoreCase(cRequest.get(VN_FINANCE_ACTION))))
			warningString = "Finance Action Changed.";
		if(warningString == null)
			warningString = oreqStoresNPlngMgrDataCheck(cRequest, oRequest);
		return warningString;
	}
	
	// old request siteproject coord initial data check
	private String oreqSPrjCoordInitialDataCheck(Request cRequest,
			Request oRequest)  throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(oRequest.get(VN_SITEPRJ_COORD_ACTION).equalsIgnoreCase(cRequest.get(VN_SITEPRJ_COORD_ACTION))))
			warningString = "Site Project Coordinator Action Changed.";
		if(warningString == null)
			warningString = oreqFinMgrDataCheck(cRequest, oRequest);
		return warningString;
	}
	
	// old request site project mgr data check
	private String oreqSPrjMgrDataCheck(Request cRequest,
			Request oRequest)  throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(oRequest.get(VN_SITEPRJ_MGR_ACTION).equalsIgnoreCase(cRequest.get(VN_SITEPRJ_MGR_ACTION))))
			warningString = "Site Project Manager Action Changed.";
		if(warningString == null)
			warningString = oreqSPrjCoordInitialDataCheck(cRequest, oRequest);
		return warningString;
	}
	
	// old request site ptoject coord final data check
	private String oreqSPrjCoordFinalDataCheck(Request cRequest,
			Request oRequest)  throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!(oRequest.get(VN_CORRESPONDANCE_NUMBER).equalsIgnoreCase(cRequest.get(VN_CORRESPONDANCE_NUMBER))))
			warningString = "Correspondence Number Changed.";
		if(warningString == null)
			warningString = oreqSPrjMgrDataCheck(cRequest, oRequest);
		return warningString;
	}
	
	
	// Current request upline dept data check
	// crequest finance final data check
	private String creqFinFinalDataCheck(Request cRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(cRequest.getObject(VN_PAYMENT_RELEASED_DATE) != null)
			warningString = "Payment Released Date should not set.";
		if( ((Boolean)cRequest.getObject(VN_PAYMENT_RELEASED_BOOLEAN)).booleanValue() )
			warningString = "Payment Released should not be tick.";
		return warningString;
	}
	
	// creq site prj coord final data check
	private String creqSPrjCoordFinalDataCheck(Request cRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if( (cRequest.getObject(VN_CORRESPONDANCE_NUMBER) != null) ){
			if(cRequest.get(VN_CORRESPONDANCE_NUMBER).length() > 0)
				warningString = "Correspondence Number should not be filled.";
		}
		if(warningString == null)
			warningString = creqFinFinalDataCheck(cRequest);
		return warningString;
	}
	
	// site project mgr data check
	private String creqSPrjMgrDataCheck(Request cRequest, Request oRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(oRequest == null){
			if(!(cRequest.get(VN_SITEPRJ_MGR_ACTION).equalsIgnoreCase(VN_SITEPRJ_MGR_ACTION_NONE)))
				warningString = "Site Project Manager action changed from None.";
		} else {
			if( !(oRequest.get(VN_SITEPRJ_MGR_ACTION).equalsIgnoreCase(cRequest.get(VN_SITEPRJ_MGR_ACTION))) )
				warningString = "Site Project Manager action changed on update Request.";
		}
		if(warningString == null)
			warningString = creqSPrjCoordFinalDataCheck(cRequest);
		return warningString;
	}
	
	// site project coord initial data check
	private String creqSPrjCoordInitialDataCheck(Request cRequest, Request oRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(oRequest == null){
			if(!(cRequest.get(VN_SITEPRJ_COORD_ACTION).equalsIgnoreCase(VN_SITEPRJ_COORD_ACTION_NONE)))
				warningString = "Site Project Coordinator action changed from None.";
		} else {
			if( !(oRequest.get(VN_SITEPRJ_COORD_ACTION).equalsIgnoreCase(cRequest.get(VN_SITEPRJ_COORD_ACTION))) )
				warningString = "Site Project Coordinator action changed on update Request.";
		}
		if(warningString == null)
			warningString = creqSPrjMgrDataCheck(cRequest, oRequest);
		return warningString;
	}
	
	// Finance Mgr initial data Check
	private String creqFinInitialDataCheck(Request cRequest, Request oRequest) throws IllegalStateException, DatabaseException {
		String warningString = null;
		if(!( ((Double)cRequest.getObject(VN_TOTAL_TAXES_APPLICABLE)).equals(ZEROVALUE) ))
			warningString = "Total taxes applicable should not be filled.";
		if(!( ((Double)cRequest.getObject(VN_TOTAl_OTHER_DED)).equals(ZEROVALUE) ))
			warningString = "Total other deductions should not be filled.";
		if(!( ((Double)cRequest.getObject(VN_NETPAYABLE)).equals(ZEROVALUE) ))
			warningString = "Net Payable should not be filled.";
		if(!(cRequest.get(VN_FINANCE_ACTION).equalsIgnoreCase(VN_FINANCE_ACTION_NONE)))
			warningString = "Finance action changed from None.";
		if(warningString == null)
			warningString = creqSPrjCoordInitialDataCheck(cRequest, oRequest);
		return warningString;
	}
	
	
	// Work Order No. NPT10109/X/Y/AAAA/VB/10-11/ZZZZ
	private String genWorkOrderNumber(Request currentRequest, int sysId,
			Connection connection, Date acurrentDate) throws IllegalStateException, DatabaseException, SQLException {
		String workOrderNumber = null;
		String currentFinancialYr = "10-11";
		int nextSerialNo = 1;
		String dependentWorkOrderNoPrefix = null;
		
		String contractRefCode = ((Type)currentRequest.getObject(VN_CONTRACT_REFERENCE)).getDescription();
		String billingCatCode = currentRequest.getCategoryId().getDescription();
		String woTypeCode = currentRequest.getSeverityId().getDescription();
		String [] woTypeCodestrings = woTypeCode.split(VN_SPLITTER_DESCRIPTION_BOX);
		
		String datePart = Timestamp.toCustomFormat(acurrentDate, TBitsConstants.API_DATE_FORMAT).split("\\s")[0];
		String[] dateTokens = datePart.split("-");
		int yearCurr  = Integer.parseInt(dateTokens[0]);
		int monthCurr = Integer.parseInt(dateTokens[1]);
		if(monthCurr > 3){
			currentFinancialYr = (Integer.toString(yearCurr)).substring(2) + "-" + (Integer.toString( (yearCurr + 1) )).substring(2);
		} else {
			currentFinancialYr = (Integer.toString( (yearCurr - 1) )).substring(2) + "-" + (Integer.toString(yearCurr)).substring(2);
		}
		
		String workOrderTypeCode = currentRequest.getSeverityId().getDescription().split(VN_SPLITTER_DESCRIPTION_BOX)[0];
		workOrderNumber = VN_FIXED_NPT10109 + VN_FORWARD_SLASH + contractRefCode + VN_FORWARD_SLASH + billingCatCode + VN_FORWARD_SLASH
						+ workOrderTypeCode + VN_FORWARD_SLASH + VN_FIXED_VB + VN_FORWARD_SLASH + currentFinancialYr;
		
		dependentWorkOrderNoPrefix = VN_FIXED_NPT10109 + VN_DELIMITER_DASH + billingCatCode + VN_DELIMITER_DASH + workOrderTypeCode
									+ VN_DELIMITER_DASH + VN_FIXED_VB;
		nextSerialNo = getNextDocumentNumberCount(connection, dependentWorkOrderNoPrefix);
		NumberFormat formatter = new DecimalFormat("0000");
	    String serialNumber = formatter.format(nextSerialNo);
		workOrderNumber = workOrderNumber + VN_FORWARD_SLASH + serialNumber;
		return workOrderNumber;
	}
	
	private int getNextDocumentNumberCount (Connection con,
			String workOrderNumberPrefix) throws SQLException {
		
		System.out.println("Generating Work Order No. for : " + workOrderNumberPrefix );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, workOrderNumberPrefix );
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
							VN_REQUEST_USER_ORDERING, VN_REQUEST_USER_IS_PRIMARY, assigneeField.getFieldId());
		ru.setUser(assig);
		
		ArrayList<RequestUser> assList= new ArrayList<RequestUser>();
		assList.add(ru);
		currentReq.setAssignees(assList);
	}
	
	private String GenNAttachFile(String fileType, Request currentRequest, int sysId, String currentBA
									) throws IllegalStateException, DatabaseException {
		File pdfFile = null;
		String CorrespondanceNo = null;
		String currentDate = null;
		String warningString = null;
		double taxesNdeductions = 0.0;
		String reqId = Integer.toString(currentRequest.getRequestId()) ;
		Hashtable <String, String> params = new Hashtable<String, String> ();
		// refrence dated should not be Null.
		Date dated = (Date)currentRequest.getObject(VN_VENDOR_INVOICE_DATED);
		CorrespondanceNo = currentRequest.get(VN_CORRESPONDANCE_NUMBER);
		taxesNdeductions = ((Double)currentRequest.getObject(VN_TOTAL_TAXES_APPLICABLE)).doubleValue()
							+ ((Double)currentRequest.getObject(VN_TOTAl_OTHER_DED)).doubleValue();
		currentDate = currentRequest.get(VN_LOGGED_DATETIME).split("\\s")[0].replace("-", "");	
		String workOrderNo = currentRequest.get(VN_WORK_ORDER_NUMBER);
		
		// Put the rptdesign pdf File parameters in the hashtable  
		params.put(VN_RPT_CORRESNO, CorrespondanceNo);
		// params.put(CL_RPT_CORRESNO_DATE, currentDate); // variable nt defined
		//params.put(VN_RPT_CONTRACT_REFERENCE, ((Type)currentRequest.getObject(VN_CONTRACT_REFERENCE)).getDisplayName());
		params.put(VN_RPT_VENDOR_ADDRESS, currentRequest.getSeverityId().getDescription().split(VN_SPLITTER_DESCRIPTION_BOX)[4]);
		params.put(VN_RPT_VENDOR_NAME, currentRequest.getSeverityId().getDescription().split(VN_SPLITTER_DESCRIPTION_BOX)[3]);
		params.put(VN_RPT_VENDOR_INVOICE_NO, currentRequest.get(VN_VENDOR_INVOICE_NO));
		if (dated != null)
		params.put(VN_RPT_VENDOR_INVOICENO_DATED, currentRequest.get(VN_VENDOR_INVOICE_DATED).split("\\s")[0]);
		params.put(VN_RPT_TOTAL_WORKORDER_VAL, Double.toString( ((Double)currentRequest.getObject(VN_TOTAL_WORKORDER_VALUE)).doubleValue() ));
		params.put(VN_RPT_TAXES_N_DEDUCTION, Double.toString(taxesNdeductions));
		params.put(VN_RPT_NET_PAYABLE, Double.toString( ((Double)currentRequest.getObject(VN_NETPAYABLE)).doubleValue() ));
		
		// Select a report file to generate based on Generation Agency
		String reportName = "NCC_vendor_invoice_template.rptdesign";
		
		HashMap<String,String> reportParamMap = new HashMap<String,String>();
		reportParamMap.put(REP_RID, reqId  ) ;			
		String tbits_base_url = WebUtil.getNearestPath("") ; // PropertiesHandler.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE)
		System.out.println( "tbits_base_url : " + tbits_base_url ) ;
		reportParamMap.put(REP_TBITS_BASE_URL_KEY, tbits_base_url );

		LOG.info("Sending parameters to generate rptdesign WorkOrder Cover Letter pdf File to BIRT Engine.");
		pdfFile = generateReport( reportName, params, reportParamMap, fileType ) ;
		
		if( pdfFile == null ) 
			warningString = "Cannot Generate the WorkOrder Cover Letter.";
		else {
			LOG.info(" About to attach generated WorkOrder Cover letter pdf File.");
			String displayName = currentDate+"_"+workOrderNo+".pdf";
			int requestId = currentRequest.getRequestId() ;
			int actionId = currentRequest.getMaxActionId() ;
			Uploader up = new Uploader( requestId, actionId, currentBA ) ;
			
			AttachmentInfo atinfo = up.moveIntoRepository(pdfFile) ;
			atinfo.name=displayName;
			
			ArrayList<AttachmentInfo> attachArray = new ArrayList<AttachmentInfo>() ;// arrayList Collection
			attachArray.add(atinfo) ;
			currentRequest.setObject(VN_ATTCH_VN_PYMT_COVER_LTR, attachArray);
			
			LOG.info(" Attached the generated WorkOrder Cover Letter pdf File.");		
		}
		return warningString;
	}

	@Override
	public String getName() {
		return classNameVN;
	}

	@Override
	public double getSequence() {
		return 2;
	}

}
