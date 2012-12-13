package nccCLBill;

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

public class ClBillConstants {

	public static final TBitsLogger LOG = TBitsLogger.getLogger("nccCLBill");
	
	public static final String CL_SYSPREFIX 					= "CL_BILL";
	public static final String CL_PENDINGWITH_PLANNINGMGR 		= "PlanningMgr";
	public static final String CL_PENDINGWITH_ACCOUNTS 			= "Accounts";
	public static final String CL_PENDINGWITH_PROJECTCOORD 		= "ProjectCoord";
	public static final String CL_PENDINGWITH_VICEPRESIDENT 	= "VicePresident";
	public static final String CL_PENDINGWITH_CLIENT 			= "Client";
	public static final String CL_PENDINGWITH_NONE	 			= "pending";
	public static final String CL_STATUS_ACTIVE					= "Active";
	public static final String CL_STATUS_CLOSED					= "Closed";
	public static final String CL_STATUS_RESUBMIT				= "resubmit";
	public static final String CL_STATUS_SUSPENDED				= "Suspended";
	public static final String CL_SUB_BREAKUP					= "bbuSubDesc";
	public static final String CL_BILLDETAILS					= "BillRemarks";
	public static final String CL_INVOICENO						= "InvoiceNo";
	public static final String CL_CONTRACT_REFERENCE			= "ContractRef";
	public static final String CL_TOTAL_BILLVALUE				= "TotalBV";
	public static final String CL_CLIENT_LETTER_REF				= "ClientLtrRefNo";
	public static final String CL_CLIENT_LETTER_REF_DATED		= "AmendmentDated";
	public static final String CL_PLANNINGMGR_ACTION			= "PlanningMgrAction";
	public static final String CL_PLANNINGMGR_ACTION_NONE		= "None";
	public static final String CL_PLANNINGMGR_ACTION_PENDING	= "Pending";
	public static final String CL_PLANNINGMGR_ACTION_SUBMITTED	= "Submitted";
	public static final String CL_TOTAL_TAXES_APPLICABLE		= "TotalTaxesApp";
	public static final String CL_TOTAL_OTHER_DEDUCTIONS		= "TotalOthDed";
	public static final String CL_NET_PAYABLE					= "NetPayable";
	public static final String CL_FINANCEMGR_ACTION				= "FinAction";
	public static final String CL_FINANCEMGR_ACTION_NONE		= "None";
	public static final String CL_FINANCEMGR_ACTION_PENDING		= "Pending";
	public static final String CL_FINANCEMGR_ACTION_APPROVED	= "Approved";
	public static final String CL_FINANCEMGR_ACTION_RESUBMIT	= "Resubmit";
	public static final String CL_PRJCOORD_ACTION				= "PrjCoordAction";
	public static final String CL_PRJCOORD_ACTION_NONE			= "None";
	public static final String CL_PRJCOORD_ACTION_PENDING		= "Pending";
	public static final String CL_PRJCOORD_ACTION_APPROVED		= "Approved";
	public static final String CL_PRJCOORD_ACTION_RESUBMIT		= "Resubmit";
	public static final String CL_PRJCOORD_ACTION_SUSPEND		= "suspend";
	public static final String CL_VP_POWER_ACTION				= "VPPowerAction";
	public static final String CL_VP_POWER_ACTION_NONE			= "None";
	public static final String CL_VP_POWER_ACTION_PENDING		= "Pending";
	public static final String CL_VP_POWER_ACTION_APPROVED		= "Approved";
	public static final String CL_VP_POWER_ACTION_RESUBMIT		= "Resubmit";
	public static final String CL_VP_POWER_ACTION_SUSPEND		= "suspend";
	public static final String CL_CORRESPONDANCE_NO				= "CorresNo";
	public static final String CL_GENERATE_COVERLTR				= "GenFormalLtr";
	public static final String CL_CLIENT_ACKNOWLEDGEMENT		= "Clientacknowledge";
	public static final String CL_CLIENT_DECISION				= "ClientAction";
	public static final String CL_CLIENT_DECISION_NONE			= "None";
	public static final String CL_CLIENT_DECISION_PENDING		= "Pending";
	public static final String CL_CLIENT_DECISION_ACCEPTED		= "Accepted";
	public static final String CL_CLIENT_DECISION_RESUBMIT		= "Resubmit";
	public static final String CL_CLIENT_DECISION_SUSPEND		= "suspend";
	public static final String CL_CLIENT_PAYMENT_REL_DATE		= "PaymentReldate";
	public static final String CL_PAYMENT_RECEIPT_NCCPL			= "InvoiceReceipt";
	public static final String CL_PAYMENT_RECEIPT_NCCPL_YES		= "Yes";
	public static final String CL_DUE_DATE						= "due_datetime";
	public static final String CL_LOGGED_DATETIME				= "logged_datetime";
	public static final String CL_LAST_UPDATED					= "lastupdated_datetime";
	
	public static final String CL_RELEASED_AMOUNT				= "ReleasedAmt";
	public static final String CL_FINAL_REPORT					= "GeneratedReport";
	public static final String CL_INVOICE_COVER_LETTER			= "InvoiceLetter";
	public static final String CL_MEMORANDUM_RABILL				= "MemoRABill";
	public static final String CL_PAYMENT_ADVICE				= "PaymentAdvice";
	public static final String CL_DESCRIPTION					= "description";
	public static final String CL_PENDING_FROM					= "PendingFrom";
	public static final String CL_PENDING_WITH					= "category_id";
	public static final String CL_STATUS						= "status_id";
	public static final String CL_BILL_DETAILS_EMPTY1			= "<br />\r\n";
	public static final String CL_BILL_DETAILS_EMPTY2			= "<br >";
	public static final int CL_PLANNING_MGR_DUE_DAY				= 28;
	
	public static final int CL_REQUEST_USER_ORDERING			= 1;
	public static final boolean CL_REQUEST_USER_IS_PRIMARY		= false;
	public static final String CL_LOGIN_PLANNING_MGR			= "yamini";
	public static final String CL_LOGIN_FINANCE_MGR				= "ramanakumar";
	public static final String CL_LOGIN_PROJECT_COORD			= "sharma";
	public static final String CL_LOGIN_VP_POWER				= "rgiyer";
	public static final String CL_LOGIN_CLIENT_KVK				= "syam"; // to be change
	
	public static final int CL_PLANNING_MGR_DURATION			= 1;
	public static final int CL_FINANCE_DURATION					= 1;
	public static final int CL_PROJECT_COORD_DURATION			= 1;
	public static final int CL_VP_POWER_DURATION				= 1;
	public static final int CL_CLIENT_KVK_DURATION				= 7;
	
	
	// Invoice No. String - NPT10109/X/YY/RA/10-11/ZZZZ
	public static final String CL_FIXED_NPT10109				= "NPT10109";
	public static final String CL_FIXED_RA						= "RA";
	public static final String CL_FORWARD_SLASH					= "/";
	
	// RPT File Parameters
	public static final String CL_RPT_CORRESNO					= "RefNo"; // ok
	public static final String CL_RPT_CORRESNO_DATE				= ""; // variable
	// public static final String CL_RPT_SUBJECT					= ""; // Not Required , Dear and description in template
	public static final String CL_RPT_BILL_DETAILS				= "DetailsofBill"; // ok
	public static final String CL_RPT_CONTRACT_REFERENCE		= "ContractRef"; // ok
	public static final String CL_RPT_LETTER_REFERENCE			= "YourLetterRef"; // ok
	public static final String CL_RPT_REF_DATED					= "AmendmentDate"; // ok
	public static final String CL_RPT_INVOICE_NO				= "InvoiceNo"; // ok
	public static final String CL_RPT_INVOICE_NO_DATE			= "date"; // ok
	public static final String CL_RPT_NET_PAYABLE				= "Amount"; // ok
	public static final String CL_RPT_MEMO_RA_FILE				= "Att"; // ok
	public static final String CL_RPT_SERIAL_NO					= "SNo"; // ok
	
	public static final String REP_RID = "rid";
	public static final String REP_TBITS_BASE_URL_KEY = "tbits_base_url";
	
	
	// Function to generate pdf report for CL_BILL by BIRT Engine.
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