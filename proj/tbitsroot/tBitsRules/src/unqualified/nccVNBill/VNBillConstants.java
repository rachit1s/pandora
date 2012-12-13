package nccVNBill;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.activity.SemanticException;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.report.TBitsReportEngine;

public class VNBillConstants {
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger("nccVNBill");
	
	public static final String VN_SYSPREFIX 						= "VN_BILL";
	public static final String VN_BILLING_CATEGORY_SELECT			= "None";
	public static final String VN_BILLING_CATEGORY_SUBCONTRACTORS	= "Subcontractors";
	public static final String VN_BILLING_CATEGORY_SITESUPPLIES		= "Sitesupplies";
	public static final String VN_BILLING_CATEGORY_EXWKS_SUPPLIES	= "ExworksSupplies";
	public static final String VN_BILLING_CATEGORY_PRW				= "Prw";
	public static final String VN_WORK_ORDER_TYPE_SELECT			= "Select";
	public static final String VN_STATUS							= "status_id";
	public static final String VN_STATUS_NONE						= "None";
	public static final String VN_STATUS_ACTIVE						= "Active";
	public static final String VN_STATUS_CLOSED						= "Closed";
	public static final String VN_STATUS_SUSPENDED					= "Suspended";
	public static final String VN_PENDING_WITH						= "request_type_id";
	public static final String VN_PENDINGWITH_NONE					= "None";
	public static final String VN_PENDINGWITH_STORES_MGR			= "StoresMgr";
	public static final String VN_PENDINGWITH_PLANNING_MGR			= "PlanningMgr";
	public static final String VN_PENDINGWITH_ACCOUNTS_MGR			= "AccountsMgr";
	public static final String VN_PENDINGWITH_SITEPRJ_COORD			= "SiteProjectCoordntr";
	public static final String VN_PENDINGWITH_SITEPRJ_MGR			= "SiteProjectMgr";
	public static final String VN_WORK_ORDER_SUMMARY				= "WorkOrderDetails";
	public static final String VN_VENDOR_INVOICE_NO					= "VendorInvNo";
	public static final String VN_VENDOR_INVOICE_DATED				= "VendorInvNoDated";
	public static final String VN_WORK_ORDER_NUMBER					= "WorkOrderNo";
	public static final String VN_WORK_ORDER_GROUP					= "WorkOrderGroup";
	public static final String VN_WORK_ORDER_GROUP_SELECT			= "Select";
	public static final String VN_PENDING_DATE						= "PendingDate";
	public static final String VN_CONTRACT_REFERENCE				= "ContractReference";
	public static final String VN_TOTAL_WORKORDER_VALUE				= "TotalWorkOrderValue";
	public static final String VN_ACTIONBY							= "Actionby";
	public static final String VN_ACTIONBY_STORES_MGR				= "StoresMgr";
	public static final String VN_ACTIONBY_PLANNING_MGR				= "PlaningMgr";
	public static final String VN_ACTION							= "Actiontaken";
	public static final String VN_ACTION_PENDING					= "Pending";
	public static final String VN_ACTION_SUBMITTED					= "Submitted";
	public static final String VN_ACTION_REJECTED					= "Rejected";
	public static final String VN_TOTAL_TAXES_APPLICABLE			= "TotalTaxesAppl";
	public static final String VN_TOTAl_OTHER_DED					= "TotalOtherDed";
	public static final String VN_NETPAYABLE						= "NetPayable";
	public static final String VN_FINANCE_ACTION					= "FinanceAction";
	public static final String VN_FINANCE_ACTION_NONE				= "None";
	public static final String VN_FINANCE_ACTION_PENDING			= "Pending";
	public static final String VN_FINANCE_ACTION_APPROVED			= "Approved";
	public static final String VN_FINANCE_ACTION_RESUBMIT			= "Resubmit";
	public static final String VN_SITEPRJ_COORD_ACTION				= "SiteProjectCoordntr";
	public static final String VN_SITEPRJ_COORD_ACTION_NONE			= "Select";
	public static final String VN_SITEPRJ_COORD_ACTION_PENDING		= "Pending";
	public static final String VN_SITEPRJ_COORD_ACTION_APPROVED		= "Approved";
	public static final String VN_SITEPRJ_COORD_ACTION_RESUBMIT		= "Resubmit";
	public static final String VN_SITEPRJ_COORD_ACTION_SUSPEND		= "suspend";
	public static final String VN_SITEPRJ_MGR_ACTION				= "SiteProjectMgr";
	public static final String VN_SITEPRJ_MGR_ACTION_NONE			= "Select";
	public static final String VN_SITEPRJ_MGR_ACTION_PENDING		= "Pending";
	public static final String VN_SITEPRJ_MGR_ACTION_APPROVED		= "Approved";
	public static final String VN_SITEPRJ_MGR_ACTION_RESUBMIT		= "Resubmit";
	public static final String VN_SITEPRJ_MGR_ACTION_SUSPEND		= "suspend";
	public static final String VN_CORRESPONDANCE_NUMBER				= "CorrespondanceNo";
	public static final String VN_GENERATE_COVER_LTR				= "GenWorkOrderLetter";
	public static final String VN_PAYMENT_RELEASED_BOOLEAN			= "PaymentReleased";
	public static final String VN_PAYMENT_RELEASED_DATE				= "PaymentReleasedActionDate";
	public static final String VN_ATTCH_MEMO_VN_BILL				= "MemorandumVnBill";
	public static final String VN_ATTCH_MRN_CERT_BILL_DC			= "ScannedMatRecptDoc";
	public static final String VN_ATTCH_SCANNED_COPY_CHEQUE			= "ScannedCpyPaymtChq";
	public static final String VN_ATTCH_VN_PYMT_COVER_LTR			= "VendorWOCoverLtr";
	public static final String VN_LOGGED_DATETIME					= "logged_datetime";
	public static final String VN_DUE_DATE							= "due_datetime";
	public static final String VN_WORKORDER_SUMMARY_EMPTY1			= "<br />\r\n";
	public static final String VN_WORKORDER_SUMMARY_EMPTY2			= "<br >";
	
	public static final String VN_LOGIN_STORES_MGR				= "syam"; // to be change
	public static final String VN_LOGIN_PLANNING_MGR			= "yamini";
	public static final String VN_LOGIN_FINANCE_MGR				= "ramanakumar";
	public static final String VN_LOGIN_SITE_PROJECT_COORD		= "sharma"; // to be change
	public static final String VN_LOGIN_SITE_PROJECT_MGR		= "syam"; // to be change
	public static final int VN_REQUEST_USER_ORDERING			= 1; // to be change
	public static final boolean VN_REQUEST_USER_IS_PRIMARY		= false; // to be change
	
	
	// public static final String CL_LOGIN_CLIENT_KVK				= "syam"; // to be change
	
	
	public static final int VN_PLANNINGMGR_DURATION				= 2;
	public static final int VN_STORESMGR_DURATION				= 2;
	public static final int VN_FINANCE_INITIAL_DURATION			= 2;
	public static final int VN_SITEPROJECT_COORD_DURATION		= 1;
	public static final int VN_SITEPRJ_MGR_DURATION				= 1;
	public static final int VN_FINANCE_LATER_DURATION			= 1;
	
	// Invoice No. String - NPT10109/X/Y/AAAA/VB/10-11/ZZZZ
	public static final String VN_FIXED_NPT10109				= "NPT10109";
	public static final String VN_FIXED_VB						= "VB";
	public static final String VN_FORWARD_SLASH					= "/";
	public static final String VN_DELIMITER_DASH				= "-";
	public static final String VN_SPLITTER_DESCRIPTION_BOX		= "~~";
	
	// RPT File Parameters
	public static final String VN_RPT_CORRESNO					= "RefNo"; // ok
	public static final String VN_RPT_VENDOR_ADDRESS			= "To"; // ok
	public static final String VN_RPT_VENDOR_NAME				= "KindAttn"; // ok
	public static final String VN_RPT_SUBJECT					= "Subject"; // ok
	public static final String VN_RPT_TOTAL_WORKORDER_VAL		= "YourInvoiceAmount"; // ok
	public static final String VN_RPT_TAXES_N_DEDUCTION			= "TaxesDeduction"; // ok
	public static final String VN_RPT_ATTACHMENTS				= "Att"; // ok
	
	//public static final String VN_RPT_CORRESNO_DATE			= ""; // 
	//public static final String VN_RPT_BILL_DETAILS			= ""; // 
	//public static final String VN_RPT_CONTRACT_REFERENCE		= "";
	public static final String VN_RPT_VENDOR_INVOICE_NO			= "YourInvoiceNo"; // ok
	public static final String VN_RPT_VENDOR_INVOICENO_DATED	= "Dated"; // ok
	//public static final String VN_RPT_WORKORDER_NO			= ""; // 
	//public static final String VN_RPT_WORKORDER_NO_DATE		= ""; // 
	public static final String VN_RPT_NET_PAYABLE				= "NetAmountPayable"; // ok
	//public static final String VN_RPT_MEMO_VN_FILE			= "";
	
	public static final String REP_RID 							= "rid";
	public static final String REP_TBITS_BASE_URL_KEY			= "tbits_base_url";
	
	
	
	
	// Function to generate pdf report for VN_BILL by BIRT Engine.
	public static File generateReport( String reportName, Hashtable<String, String> params,
			HashMap<String, String> reportParams, String format ){
		
		IReportDocument ird			= null ; // Birt
		TBitsReportEngine tre		= null ; // tBits
		
		try {
			
			tre = new TBitsReportEngine();
			if(tre == null){
				LOG.error("Unable to get the instance of ReportEngine.");
				return null ;
			}
			
			IReportRunnable reportDesign; // Birt
			reportDesign = tre.getReportDesign(reportName);
			if(reportDesign == null){
				LOG.error("Unable to get the design instance of " + reportName);
				return null ;
			}
			
			IReportEngine ire = tre.getEngine() ; // Birt
			EngineConfig ec = ire.getConfig() ; // Birt
			
			// set all non-report parameters
			for( Enumeration<String> keys = params.keys() ; keys.hasMoreElements() ; )
			{			
				String key = keys.nextElement() ;
				String value = params.get(key) ;
				ec.getAppContext().put(key,value) ;
			}
			
			ird = tre.getReportDocument(reportDesign, reportParams) ;
			
			File outFile = null ;
			if( format.trim().equalsIgnoreCase("pdf"))
			 outFile = tre.getPDFReport(ird);
			else 
				outFile = tre.getHTMLReport(ird) ; // default
			
			// print file info
			if( outFile != null ) 
			{
				System.out.println( "Name:" + outFile.getName() + " path = " + outFile.getAbsolutePath() ) ;
				return outFile ;
			}
			else
			{
				LOG.error("OutPUT file is null" ) ;
				return null ;
			}
						
		}
		catch (EngineException e) {
			e.printStackTrace();
			return null;
		}
		catch (SemanticException e) {
			e.printStackTrace();
			return null;
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}		
		finally
		{
			if (tre != null)
				tre.destroy();
		}
					
	}
}
