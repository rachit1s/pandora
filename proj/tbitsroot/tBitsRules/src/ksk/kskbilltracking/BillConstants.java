package kskbilltracking;

public class BillConstants {
	public static final String Bill_sysprefix="Bill";
	public static final String Bill_Business_Area	="sys_id";
	public static final String Bill_Request	="request_id";
	public static final String Bill_Vendor	="category_id";
	public static final String Bill_Pending_With	="status_id";
	public static final String Bill_Bill_Initiation_Agency	="severity_id";
	public static final String Bill_Contract_Type	="request_type_id";
	public static final String Bill_Logger	="logger_ids";
	public static final String Bill_Assignee	="assignee_ids";
	public static final String Bill_Subscribers	="subscriber_ids";
	public static final String Bill_To	="to_ids";
	public static final String Bill_Cc	="cc_ids";
	public static final String Bill_Subject	="subject";
	public static final String Bill_Description	="description";
	public static final String Bill_Private	="is_private";
	public static final String Bill_Parent	="parent_request_id";
	public static final String Bill_Last_Update_By_	="user_id";
	public static final String Bill___U	="max_action_id";
	public static final String Bill_Due_Date	="due_datetime";
	public static final String Bill_Submitted_Date	="logged_datetime";
	public static final String Bill_Last_Updated	="lastupdated_datetime";
	public static final String Bill_Header_Description	="header_description";
	public static final String Bill_Bill_Attachments	="attachments";
	public static final String Bill_Summary	="summary";
	public static final String Bill_Memo	="memo";
	public static final String Bill_append_interface	="append_interface";
	public static final String Bill_Notify	="notify";
	public static final String Bill_Notify_Logger	="notify_loggers";
	public static final String Bill_replied_to_action	="replied_to_action";
	public static final String Bill_Linked_Requests	="related_requests";
	public static final String Bill_User_Department	="office_id";
	public static final String Bill_Send_SMS	="SendSMS";
	public static final String Bill_Invoice_No	="InvoiceNo";
	public static final String Bill_Invoice_Value	="InvoiceValue";
	public static final String Bill_Invoice_Date	="InvoiceDate";
	public static final String Bill_KMPCL_Payment_Date	="KMPCLPaymentDate";
	public static final String Bill_GRN_No	="GRNNo";
	public static final String Bill_MDCC_No	="MDCCNo";
	public static final String Bill_Deduction	="Deduction";
	public static final String Bill_Net_Invoice_Value	="NetInvoiceValue";
	public static final String Bill_GRN_Date	="GRNDate";
	public static final String Bill_Currency	="Currency";
	public static final String Bill_SCM_Receipt	="SCMReceipt";
	public static final String Bill_SCM_Decision	="SCMDecision";
	public static final String Bill_SCM_Acknowledgement	="SCMAcknowledgement";
	public static final String Bill_Stores_Receipt	="StoresReceipt";
	public static final String Bill_Stores_Acknowledgement	="StoresAcknowledgement";
	public static final String Bill_Site_Head_Receipt	="SiteHeadReceipt";
	public static final String Bill_Site_Head_Decision	="SiteHeadDecision";
	public static final String Bill_Site_Head_Acknowledgement	="SiteHeadAcknowledgement";
	public static final String Bill_Budgeting_Receipt	="BudgettingReceipt";
	public static final String Bill_Budgeting_Acknowledgement	="BudgettingAcknowledgement";
	public static final String Bill_Hard_Copy_Received	="HardCopyReceivedFromSite";
	public static final String Bill_Finance_Received	="FinanceReceived";
	public static final String Bill_Finance_Decision	="FinanceDecision";
	public static final String Bill_Finance_Payment	="FinancePayment";
	public static final String Bill_Budgeting_Decision	="BudgettingDecision";
	public static final String Bill_Stores_Decision	="StoresDecision";
	public static final String Bill_Doc_Cell_Receipt	="DocumentCellReceipt";
	public static final String Bill_Doc_Cell_Acknowledgement	="DocumentCellAcknowledgement";
	public static final String Bill_User_Dept_Receipt	="UserDeptReceipt";
	public static final String Bill_User_Dept_Decision	="UserDepartmentDecision";
	public static final String Bill_User_Dept_Acknowledgement	="UserDepartmentAcknowledgement";
	public static final String Bill_Unit_Code	="UnitCode";




	public static final String Type_Vendor_SEPCO	= "SEPCO";
	public static final String Type_Vendor_Others	= "pending";
	public static final String Type_Pending_With_Document_Cell	= "DocCell";
	public static final String Type_Pending_With_Procurement	= "Procurement";
	public static final String Type_Pending_With_Stores	= "Stores";
	public static final String Type_Pending_With_User_Department	= "UserDepartment";
	public static final String Type_Pending_With_Site_Head	= "SiteHead";
	public static final String Type_Pending_With_Budgeting	= "Budgetting";
	public static final String Type_Pending_With_Finance___Accounts	= "FandA";
	public static final String Type_Pending_With_Returned_To_Vendor	= "ReturnedToVendor";
	public static final String Type_Pending_With_Closed	= "Closed";
	public static final String Type_Bill_Initiation_Agency_KMPCL_Procurement	= "KMPCLProcurement";
	public static final String Type_Bill_Initiation_Agency_KMPCL_Stores	= "KMPCLStores";
	public static final String Type_Contract_Type_Offshore_Supply	= "OffshoreSupply";
	public static final String Type_Contract_Type_Offshore_Services	= "OffshoreServices";
	public static final String Type_Contract_Type_Onshore_Supply	= "OnshoreSupply";
	public static final String Type_Contract_Type_Onshore_Services	= "OnshoreServices";
	public static final String Type_Contract_Type_Mandatory_Spares	= "MandatorySpares";
	public static final String Type_Contract_Type_Construction	= "Construction";
	public static final String Type_Contract_Type_Others	= "Others";
	public static final String Type_User_Department_Civil	= "Civil";
	public static final String Type_User_Department_Electrical	= "Electrical";
	public static final String Type_User_Department_Mechanical	= "Mechanical";
	public static final String Type_User_Department_Control___Instrumentation	= "CnI";
	public static final String Type_User_Department_Site_Erection___Commissioning	= "default";
	public static final String Type_User_Department_Chennai_Engineering_Center	= "CECEngineering";
	public static final String Type_Currency_USD	= "USD";
	public static final String Type_Currency_INR	= "INR";
	public static final String Type_SCM_Decision_Pending	= "Pending";
	public static final String Type_SCM_Decision_Approved	= "Approved";
	public static final String Type_SCM_Decision_Rejected	= "Rejected";
	public static final String Type_Site_Head_Decision_Pending	= "Pending";
	public static final String Type_Site_Head_Decision_Approved	= "Approved";
	public static final String Type_Site_Head_Decision_Rejected	= "Rejected";
	public static final String Type_Finance_Decision_Pending	= "Pending";
	public static final String Type_Finance_Decision_Approved	= "Approved";
	public static final String Type_Finance_Decision_Rejected	= "Rejected";
	public static final String Type_Budgeting_Decision_Pending	= "Pending";
	public static final String Type_Budgeting_Decision_Approved	= "Approved";
	public static final String Type_Budgeting_Decision_Rejected	= "Rejected";
	public static final String Type_Stores_Decision_Pending	= "Pending";
	public static final String Type_Stores_Decision_Approved	= "Approved";
	public static final String Type_Stores_Decision_Rejected	= "Rejected";
	public static final String Type_Unit_Code_Common__All_Units_	= "Common";
	public static final String Type_Unit_Code_Unit___1	= "Unit1";
	public static final String Type_Unit_Code_Unit___2	= "Unit2";
	public static final String Type_Unit_Code_Unit___3	= "Unit3";
	public static final String Type_Unit_Code_Unit___4	= "Unit4";
	public static final String Type_Unit_Code_Unit___5	= "Unit5";
	public static final String Type_Unit_Code_Unit___6	= "Unit6";
	public static final String Type_Unit_Code_Unit___1___2	= "Unit12";
	public static final String Type_Unit_Code_Unit___3___4	= "Unit34";
	public static final String Type_Unit_Code_Unit___5___6	= "Unit56";
	public static final String Type_Unit_Code_Unit___1_2_3__Ph_1_	= "Unit123";
	public static final String Type_Unit_Code_Unit___4_5_6__Ph_2_	= "Unit456";
	
	
	//step info in DB
	public static final String Db_table_Name="plugins_process_step_flow_datamap";
	public static final String Db_plugin_name="kskBillTracking";
	
	public static final String Db_step_Name="plugins_bill_flows";
	public static final String Db_step_Duration="duration";
	public static final String Db_step_Assignees="assignee";
	public static final String Db_step_loggers="logger";
	public static final String Db_step_Dep_Head="head";
	public static final String Db_step_pending_with="pendingwith";
	public static final String Db_step_Dep_Receipt_Date="receipt";
	public static final String Db_step_Dep_Acknowledge_Date="acknowledge";
    public static final String Db_step_Decision="decision";



	public static final int Total_Duration =21;



}
