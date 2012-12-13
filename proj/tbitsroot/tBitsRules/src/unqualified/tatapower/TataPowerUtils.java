/**
 * 
 */
package tatapower;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.TimeZone;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.ActionEx;
import transbit.tbits.domain.CalenderUtils;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.DefaultHolidayCalendar;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

/**
 * @author Lokesh
 *
 */
public class TataPowerUtils {
	
	//Property file name.
	static final String APP_PROPERTIES = "app.properties";
	
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	
	//Property Strings
	static final String TYPE_OTHERS_FINANCE_ASSIGNEES = "tatapower.others_finance_assignees";
	static final String TYPE_OTHERS_HEAD_OF_CONST_ASSIGNEES = "tatapower.others_head_of_const_assignees";
	static final String TYPE_OTHERS_QTY_SURVEYOR_ASSIGNEES = "tatapower.others_qty_surveyor_assignees";
	static final String TYPE_OTHERS_SITE_INCHARGE_ASSIGNEES = "tatapower.others_site_incharge_assignees";
	static final String TYPE_OTHERS_DCC_ASSIGNEES = "tatapower.others_document_cell_assigneess";
	static final String TYPE_OTHERS_MECH_GH_ASSIGNEES = "tatapower.others_mechanical_group_head";
	static final String TYPE_OTHERS_CIVIL_GH_ASSIGNEES = "tatapower.others_civil_group_head";
	static final String TYPE_OTHERS_ELEC_GH_ASSIGNEES = "tatapower.others_electrical_group_head";
	static final String TYPE_OTHERS_SAFETY_GH_ASSIGNEES = "tatapower.others_safety_group_head";
	static final String TYPE_OTHERS_HR_GH_ASSIGNEES = "tatapower.others_hr_group_head";
	static final String TYPE_OTHERS_MM_GH_ASSIGNEES = "tatapower.others_mm_group_head";
	static final String TYPE_OTHERS_PROC_GH_ASSIGNEES = "tatapower.others_procurement_group_head";
	
	static final String TATAPOWER_SYS_PREFIX_MOM_SUBJECT_RULE = "tatapower.sys_prefix_mom_subject_rule";
	static final String TATA_POWER_SYS_PREFIXES = "tatapower.sys_prefixes";
	static final String TATAPOWER_ONLY_ASSIGNEES_RULE_SYS_PREFIXES = "tatapower.only_assignees_rule_sys_prefixes";
	static final String TATAPOWER_TYPE_NAMES = "tatapower.typeNames";
	static final String TATAPOWER_TYPE_NAMES_FOR_STATUS = "tatapower.typeNames_for_status";
	static final String TATAPOWER_DEPT_NAMES_FOR_FINANCE = "tatapower.deptNames_for_finance";
		
	//Property - Offsets
	static final String SIXTY_PERCENT_DCC_OFFSET = "tatapower.60percent_dcc_date_offset"; 
	static final String SIXTY_PERCENT_SI_OFFSET = "tatapower.60percent_site_incharge_date_offset";
	static final String SIXTY_PERCENT_QS_OFFSET = "tatapower.60percent_qs_date_offset";
	static final String SIXTY_PERCENT_GH_OFFSET = "tatapower.60percent_group_head_offset";
	static final String SIXTY_PERCENT_HOC_OFFSET = "tatapower.60percent_hoc_date_offset";
	static final String SIXTY_PERCENT_FINANCE_OFFSET = "tatapower.60percent_finance_date_offset";
	
	static final String FOURTY_PERCENT_DCC_OFFSET = "tatapower.40percent_dcc_date_offset"; 
	static final String FOURTY_PERCENT_SI_OFFSET = "tatapower.40percent_site_incharge_date_offset";
	static final String FOURTY_PERCENT_QS_OFFSET = "tatapower.40percent_qs_date_offset";
	static final String FOURTY_PERCENT_GH_OFFSET = "tatapower.40percent_group_head_offset";
	static final String FOURTY_PERCENT_HOC_OFFSET = "tatapower.40percent_hoc_date_offset";
	static final String FOURTY_PERCENT_FINANCE_OFFSET = "tatapower.40percent_finance_date_offset";
	
	static final String TYPE_RA1OO_DCC_ASSIGNEES = "tatapower.RA100_document_cell_assignees";
	static final String TYPE_RA1OO_SI_ASSIGNEES = "tatapower.RA100_site_incharge_assignees";
	static final String TYPE_RA1OO_QTY_ASSIGNEES = "tatapower.RA100_qty_surveyor_assignees";
	static final String TYPE_RA100_HEAD_OF_CONST_ASSIGNEES = "tatapower.RA100_head_of_const_assignees";
	static final String TYPE_RA100_FINANCE_ASSIGNEES = "tatapower.RA100_finance_assignees";
	static final String TYPE_RA100_MECH_GH_ASSIGNEES = "tatapower.RA100_mechanical_group_head";
	static final String TYPE_RA100_CIVIL_GH_ASSIGNEES = "tatapower.RA100_civil_group_head";
	static final String TYPE_RA100_ELEC_GH_ASSIGNEES = "tatapower.RA100_electrical_group_head";
	static final String TYPE_RA100_HR_GH_ASSIGNEES = "tatapower.RA100_hr_group_head";
	static final String TYPE_RA100_MM_GH_ASSIGNEES = "tatapower.RA100_mm_group_head";
	static final String TYPE_RA100_PROC_GH_ASSIGNEES ="tatapower.RA100_procurement_group_head";
	static final String TYPE_RA100_SAFETY_GH_ASSIGNEES ="tatapower.RA100_safety_group_head";
	
	static final String TYPE_GRN_DCC_ASSIGNEES = "tatapower.GRN_document_cell_assignees";
	static final String TYPE_GRN_STORE_ASSIGNEES = "tatapower.GRN_store_assignees";
	static final String TYPE_GRN_SI_ASSIGNEES = "tatapower.GRN_site_incharge_assignees";
	static final String TYPE_GRN_QUALITY_ASSIGNEES = "tatapower.GRN_quality_assignees";
	static final String TYPE_GRN_QTY_ASSIGNEES = "tatapower.GRN_qty_surveyor_assignees";
	static final String TYPE_GRN_HEAD_OF_CONST_ASSIGNEES = "tatapower.GRN_head_of_const_assignees";
	static final String TYPE_GRN_FINANCE_ASSIGNEES = "tatapower.GRN_finance_assignees";	
	
	static final String TYPE_GRN_CIVIL_SI_ASSIGNEES = "tatapower.GRN_civil_site_incharge_assignees";
	static final String TYPE_GRN_MECHANICAL_SI_ASSIGNEES = "tatapower.GRN_mechanical_site_incharge_assignees";
	static final String TYPE_GRN_ELECTRICAL_SI_ASSIGNEES = "tatapower.GRN_electrical_site_incharge_assignees";
	static final String TYPE_GRN_HR_SI_ASSIGNEES = "tatapower.GRN_hr_site_incharge_assignees";
	static final String TYPE_GRN_ADMIN_SI_ASSIGNEES = "tatapower.GRN_admin_site_incharge_assignees";
	static final String TYPE_GRN_PROC_SI_ASSIGNEES = "tatapower.GRN_procurement_site_incharge_assignees";
	static final String TYPE_GRN_SAFETY_SI_ASSIGNEES = "tatapower.GRN_safety_site_incharge_assignees";
	static final String TYPE_GRN_SECURITY_SI_ASSIGNEES = "tatapower.GRN_security_site_incharge_assignees";	
	
	static final String TYPE_GRN_CIVIL_GH_ASSIGNEES = "tatapower.GRN_civil_group_head";
	static final String TYPE_GRN_MECHANICAL_GH_ASSIGNEES = "tatapower.GRN_mechanical_group_head";
	static final String TYPE_GRN_ELECTRICAL_GH_ASSIGNEES = "tatapower.GRN_electrical_group_head";	
	static final String TYPE_GRN_ADMIN_GH_ASSIGNEES = "tatapower.GRN_admin_group_head";
	static final String TYPE_GRN_SECURITY_GH_ASSIGNEES = "tatapower.GRN_security_group_head=rameshdhawan";
	static final String TYPE_GRN_HR_GH_ASSIGNEES = "tatapower.GRN_hr_group_head";
	//static final String TYPE_GRN_MM_GH_ASSIGNEES = "tatapower.GRN_mm_group_head";
	static final String TYPE_GRN_PROC_GH_ASSIGNEES ="tatapower.GRN_procurement_group_head";
	static final String TYPE_GRN_SAFETY_GH_ASSIGNEES ="tatapower.GRN_safety_group_head";
	
	static final String RA100_DCC_OFFSET = "tatapower.RA100_dcc_date_offset";
	static final String RA100_SI_OFFSET = "tatapower.RA100_site_incharge_date_offset";
	static final String RA100_QS_OFFSET = "tatapower.RA100_qs_date_offset";
	static final String RA100_GH_OFFSET = "tatapower.RA100_group_head_offset";
	static final String RA100_HOC_OFFSET = "tatapower.RA100_hoc_date_offset";
	static final String RA100_FINANCE_OFFSET = "tatapower.RA100_finance_date_offset";
	
	static final String GRN_DCC_OFFSET = "tatapower.GRN_dcc_date_offset";
	static final String GRN_STORE_OFFSET = "tatapower.GRN_store_date_offset";
	static final String GRN_SI_OFFSET = "tatapower.GRN_site_incharge_date_offset";
	static final String GRN_QUALITY_OFFSET = "tatapower.GRN_quality_date_offset";
	static final String GRN_QS_OFFSET = "tatapower.GRN_qs_date_offset";
	static final String GRN_GH_OFFSET = "tatapower.GRN_group_head_offset";
	static final String GRN_HOC_OFFSET = "tatapower.GRN_hoc_date_offset";
	static final String GRN_FINANCE_OFFSET = "tatapower.GRN_finance_date_offset";	

	static final String TYPE_DESP_DCC_ASSIGNEES = "tatapower.DESP_document_cell_assignees";
	static final String TYPE_DESP_STORE_ASSIGNEES = "tatapower.DESP_store_assignees";
	static final String TYPE_DESP_FINANCE_ASSIGNEES = "tatapower.DESP_finance_assignees";
	static final String DESP_DCC_OFFSET = "tatapower.DESP_dcc_date_offset";
	static final String DESP_STORE_OFFSET = "tatapower.DESP_store_date_offset";
	static final String DESP_FINANCE_OFFSET = "tatapower.DESP_finance_date_offset";
	
	
	static final String TATAPOWER_60PERCENT_DATE_OFFSET = "tatapower.60percent_date_offset";
	static final String TATAPOWER_40PERCENT_DATE_OFFSET = "tatapower.40percent_date_offset";
	static final String TATAPOWER_RA100_DATE_OFFSET = "tatapower.RA100_date_offset";
	static final String TATAPOWER_GRN_DATE_OFFSET = "tatapower.GRN_date_offset";
	static final String TATAPOWER_DESP_DATE_OFFSET = "tatapower.DESP_date_offset";
	
	static final String TATAPOWER_ADMIN_DATE_OFFSET = "tatapower.admin_date_offset";
	static final String TATAPOWER_ADMIN_NON_SAP_DATE_OFFSET = "tatapower.admin_non_sap_po_date_offset";
	
	static final String ADMIN_DCC_OFFSET = "tatapower.admin_dcc_date_offset"; 
	static final String ADMIN_SI_OFFSET = "tatapower.admin_site_incharge_date_offset";
	static final String ADMIN_QS_OFFSET = "tatapower.admin_qs_date_offset";
	static final String ADMIN_GH_OFFSET = "tatapower.admin_group_head_offset";
	static final String ADMIN_HOC_OFFSET = "tatapower.admin_hoc_date_offset";
	static final String ADMIN_FINANCE_OFFSET = "tatapower.admin_finance_date_offset";
	
	static final String  NON_SAP_DCC_DATE_OFFSET = "tatapower.admin_non_sap_no_dcc_date_offset";
	static final String  NON_SAP_SITE_INCHARGE_DATE_OFFSET = "tatapower.admin_non_sap_po_site_incharge_date_offset";
	static final String  NON_SAP_FINANCE_DATE_OFFSET = "tatapower.admin_non_sap_po_finance_date_offset";
	
	//Status types
	static final String STATUS_DCC = "DocumentControlCell";
	static final String STATUS_STORE = "Store";
	static final String STATUS_UDSI = "UserDeptSiteIC";
	static final String STATUS_QUALITY = "Quality";
	static final String STATUS_QS = "QuantitySurveyor";
	static final String STATUS_HOC = "HeadOfConstruction";
	static final String STATUS_FINANCE = "Finance"; 
	static final String STATUS_GROUP_HEAD = "UnitHead";
	static final String STATUS_CLOSED = "Closed";
	static final String STATUS_RETURNED_TO_CONTRACTOR = "ReturnedToContractor";
	
	//Category type
	static final String TYPE_ADVANCE= "Advance";
	static final String MECHANICAL = "Mechanical";
	static final String ELECTRICAL = "Electrical";
	static final String CIVIL = "Civil";
	static final String ADMINISTRATION = "Administration";
	static final String HR = "HR";
	static final String MATERIAL_MANAGEMENT = "MaterialManagement";
	static final String PROCUREMENT = "Procurement";
	static final String SAFETY = "Safety";
	static final Object SECURITY = "Security";
		
	//Date field names
	static final String FIN_PAYMENT_DATE = "finpayment";
	static final String FINANCE_RECIEPT_DATE = "finreceived";
	static final String HOC_RECIEPT_DATE = "hocreceived";
	static final String QS_RECIEPT_DATE = "qsreceived";
	static final String QUALITY_RECIEVED_DATE = "QualityReceived";
	static final String STORE_RECEIVED_DATE = "StoreReceived";
	static final String GH_RECIEPT_DATE = "UnitHeadReceived";
	static final String UDSI_RECIEPT_DATE = "siteicreceived";
	static final String DCC_RECEIPT_DATE = "dccreceipt";	
	static final String SITE_IC_CERTIFIED = "siteiccertified";
	static final String QS_VERIFIED = "qsverified";
	static final String HOC_APPROVED = "hocapproved";
	static final String UH_APPROVED = "UHApproved";
	static final String DCC_ACKNOWLEDGE = "dccacknowledge";
	static final String QUALITY_ACKNOWLEDGE = "QualityAcknowledge";
	static final String STORE_ACKNOWLEDGE = "StoreAcknowledge";
	
	
	static final String INVOICE_PAYMENT_DATE = "InvoicePaymentDate";
	static final String INVOICE_DATE = "invoicedate";
	static final String TATAPOWER_FINANCE_DATE_OFFSET = "tatapower.finance_date_offset";
	
	//Request type
	static final String TYPE_SIXTY_PERCENT = "60Payment";
	static final String TYPE_FORTY_PERCENT = "40Payment";
	static final String TYPE_ADMIN_WITH_SAP_PO = "Administration";
	static final String TYPE_ADMIN_WITHOUT_SAP_PO = "AdminWithoutSAPPO";
	static final String TYPE_ELECTRICAL_RUNNING = "electrical_running";
	static final String TYPE_ELECTRICAL_FINAL = "electrical_final";
	static final String TYPE_ELECTRICAL_THIRTY = "electrical_thirty";	
	static final String TYPE_OTHERS = "Others";
	static final String TYPE_RA100 = "RA100";
	static final String TYPE_GRN = "GRN";
	//GRN-Civil / GRN-Mechanical / GRN-Electrical / GRN-HR / GRN-Admin / GRN-Safety / GRN-Security / GRN-Procurement.
	static final String TYPE_GRN_CIVIL = "GRNCivil";
	static final String TYPE_GRN_MECHANICAL = "GRNMechanical";
	static final String TYPE_GRN_ELECTRICAL = "GRNElectrical";
	static final String TYPE_GRN_HR = "GRNHR";
	static final String TYPE_GRN_ADMIN = "GRNAdmin";
	static final String TYPE_GRN_SAFETY = "GRNSafety";
	static final String TYPE_GRN_SECURITY = "GRNSecurity";
	static final String TYPE_GRN_PROCUREMENT = "GRNProcurement";
	static final String TYPE_DESP = "Desp";
	
	static final String TYPE_ELECTRICAL_30_DATE_OFFSET = "tatapower.electrical30_date_offset";
	static final String TYPE_ELECTRICAL_30_DCC_OFFSET = "tatapower.electrical30_dcc_date_offset";
	static final String TYPE_ELECTRICAL_30_SI_OFFSET = "tatapower.electrical30_site_incharge_date_offset";
	static final String TYPE_ELECTRICAL_30_GH_OFFSET = "tatapower.electrical30_group_head_offset";
	static final String TYPE_ELECTRICAL_30_QS_OFFSET = "tatapower.electrical30_qs_date_offset";
	static final String TYPE_ELECTRICAL_30_HOC_OFFSET = "tatapower.electrical30_hoc_date_offset";
	static final String TYPE_ELECTRICAL_30_FINANCE_OFFSET = "tatapower.electrical30t_finance_date_offset";
	static final String TYPE_ELECTRICAL_30_DCC_ASSIGNEES = "tatapower.electrical30_document_cell_assignees";
	static final String TYPE_ELECTRICAL_30_SI_ASSIGNEES = "tatapower.electrical30_site_incharge_assignees";
	static final String TYPE_ELECTRICAL_30_QS_ASSIGNEES = "tatapower.electrical30_qty_surveyor_assignees";
	static final String TYPE_ELECTRICAL_30_GH_ASSIGNEES = "tatapower.electrical30_group_head_assignees";
	static final String TYPE_ELECTRICAL_30_HOC_ASSIGNEES = "tatapower.electrical30_head_of_const_assignees";
	static final String TYPE_ELECTRICAL_30_FINANCE_ASSIGNEES = "tatapower.electrical30_finance_assignees";
	
	//Indices used for hashtable mapping
	static final int CLOSED_INDEX = 7;
	static final int FINANCE_INDEX = 6;
	static final int HOC_INDEX = 5;
	static final int GH_INDEX = 4;
	static final int QS_INDEX = 3;
	static final int SITE_IC_INDEX = 2;	
	static final int DCC_INDEX = 1;
	
	
	static final int GRN_CLOSED_INDEX = 8;
	static final int GRN_FINANCE_INDEX = 7;
	static final int GRN_HOC_INDEX = 6;
	static final int GRN_GH_INDEX = 5;		
	static final int GRN_QUALITY_INDEX = 4;	
	static final int GRN_SITE_IC_INDEX = 3;	
	static final int GRN_STORE_INDEX = 2;
	static final int GRN_DCC_INDEX = 1;
			
	public static String getProperty(String propertyName)
	{
		URL url = TataPowerUtils.class.getResource(APP_PROPERTIES);
		String file = url.getFile();
		File f = new File(file);
		if (f.exists()) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(f));
				//props.list(System.out);
				String baPrefix = props.getProperty(propertyName);
				return baPrefix;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			LOG.error("TataPowerUtils : The " + f.getAbsolutePath()
					+ " file is missing. Please check is it exist.");
		}
		return null;
	}

	/*
	 * Takes comma separated string and a compare string. Checks if the compare string 
	 * exists in the comma separated string.
	 */
	public static boolean isExistsInString(String parentString, String childString){
		if ((parentString == null) || parentString.trim().equals(""))
			return false;
		String[] strArray = parentString.split(",");
		for (String str : strArray){
			if (str.trim().equals(childString.trim()))
				return true;
			else continue;
		}
		return false;
	}

	public static boolean isExistsInBillBABasedRulesSysPrefixes(String sysPrefix){
		return isExistsInString (getProperty(TATA_POWER_SYS_PREFIXES), sysPrefix);
	}
	
	public static boolean isExistsInOnlyAssigneesRuleProperty(String sysPrefix){
		return isExistsInString(getProperty(TATAPOWER_ONLY_ASSIGNEES_RULE_SYS_PREFIXES), sysPrefix);
	}
	
	public static boolean isApplyAlways(){
		return Boolean.parseBoolean(getProperty("tatapower.mail_always"));
	}
	
	public static boolean isExistsInPropertyMOMSubjectRuleSysPrefixes(String sysPrefix){
		return isExistsInString(getProperty(TATAPOWER_SYS_PREFIX_MOM_SUBJECT_RULE), sysPrefix);
	}
	
	public static int getUniqMaxId(String maxUniqIdName) throws SQLException
	{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			return getUniqMaxId(conn, maxUniqIdName);
		} catch (SQLException e) {
			throw e;
		} finally {
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	static int getUniqMaxId(Connection conn, String maxIdSequenceName) throws SQLException {
		CallableStatement stmt = conn
				.prepareCall("stp_getAndIncrMaxId ?");
		stmt.setString(1, maxIdSequenceName);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			int id = rs.getInt("max_id");
			return id;
		} else {
			throw new SQLException();
		}
	}

	/**
	 * @param currentRequest
	 * @param dateOffset
	 * @param extendedFields
	 * @param isAddRequest
	 * @param receiptDateReq
	 * @throws DatabaseException 
	 */
	public static boolean setDueDateBasedOnRecieptDate(Request currentRequest,
			Field extdDatefield, int dateOffset, Hashtable<Field, RequestEx> extendedFields,
			boolean isAddRequest) throws DatabaseException {

		ActionEx prevActionEx = null;
		boolean isPrevRecieptDD = false;
		RequestEx receiptDateReq = extendedFields.get(extdDatefield);
		int prevActionId = currentRequest.getMaxActionId() - 1;	
		Date date = null;
		Timestamp rTimeStamp = null;
		if ((receiptDateReq.getDateTimeValue() != null)){
			rTimeStamp = receiptDateReq.getDateTimeValue();
			date = new Date(rTimeStamp.getTime() + getRemainingMilliSecondsInTheDay()); 
		}
		if (!isAddRequest){
			prevActionEx = ActionEx.lookupBySystemIdRequestIdActionIdFieldId(currentRequest.getSystemId(), currentRequest.getRequestId(), 
					prevActionId, extdDatefield.getFieldId());
			 
			if (prevActionEx != null){
				Timestamp prevTS = prevActionEx.getDateTimeValue();
				if (prevTS == null)
					isPrevRecieptDD = true;
				else if ((prevTS != null) && (date!= null) && (prevTS.before(date)))
					isPrevRecieptDD = true;
				else
					isPrevRecieptDD = false;
			}
		}
		
		if ((isAddRequest || isPrevRecieptDD)){
			if (rTimeStamp != null){
				if (dateOffset == 0){
					long diff = getRemainingMilliSecondsInTheDay();					
					long sinceStart = Timestamp.getMillisSinceStart(TimeZone.getTimeZone("GMT"));					
					Date d = new Date(sinceStart + diff);
					Timestamp duedateTS = Timestamp.getTimestamp(d);
					currentRequest.setDueDate(duedateTS);
					return true;
				}
				else{
					Date dueDate = CalenderUtils.slideDate(date, dateOffset, new DefaultHolidayCalendar());	
					Timestamp duedateTS = Timestamp.getTimestamp(dueDate);
					currentRequest.setDueDate(duedateTS);
					return true;
				}
			}			
		}
		return false;
	}
	
		
	/**
	 * @return
	 */
	public static long getRemainingMilliSecondsInTheDay() {
		long diff = 0;
		Calendar istc = Calendar.getInstance(TimeZone.getTimeZone("IST"));
		long istMillisNow = istc.getTimeInMillis();
		istc.set(Calendar.HOUR_OF_DAY, 23);
		istc.set(Calendar.MINUTE, 59);
		istc.set(Calendar.SECOND, 59);
		long istMillis23Hour = istc.getTimeInMillis();
		diff = istMillis23Hour - istMillisNow;
		return diff;
	}
		
	
	
	/**
	 * @param currentRequest
	 * @param extendedFields
	 * @param isAddRequest
	 * @param receiptDateReq
	 * @throws DatabaseException 
	 */
	public static boolean setStatusBasedOnRecieptDate(Request currentRequest,
			Field extdDatefield, Hashtable<Field, RequestEx> extendedFields, 
			boolean isAddRequest) throws DatabaseException {
		
		ActionEx prevActionEx = null;
		boolean isPrevRecieptDD = false;
		RequestEx receiptDateReq = extendedFields.get(extdDatefield);
		int prevActionId = currentRequest.getMaxActionId() - 1;
		int systemId = currentRequest.getSystemId();
		
		if (!isAddRequest){			
			prevActionEx = ActionEx.lookupBySystemIdRequestIdActionIdFieldId(systemId, currentRequest.getRequestId(), 
					prevActionId, extdDatefield.getFieldId());
			if (prevActionEx != null)
				if (prevActionEx.getDateTimeValue() == null)
					isPrevRecieptDD = true;
				else
					isPrevRecieptDD = false;
			System.out.println("isPrevRecieptDD: " + isPrevRecieptDD);
		}
				
		if ((isAddRequest || isPrevRecieptDD) && (receiptDateReq.getDateTimeValue() != null)){
			return true;
		}
		else
			return false;
	}
	
	public static int getPropertyIntValue(String property){
		return (Integer.parseInt(getProperty(property).trim()));		
	}
	
	public static ArrayList<RequestUser> getRUList(int aSystemId,
			int aRequestId, String propertyName) throws DatabaseException{
		String assigneeList = getProperty(propertyName);
		if (assigneeList == null)
			return new ArrayList<RequestUser>();
		return TataPowerUtils.getRequestUsersList(aSystemId, aRequestId, assigneeList, UserType.ASSIGNEE);
	}

	/**
	 * @param aSystemId
	 * @param aRequestId
	 * @param userType TODO
	 * @param ccList
	 * @return
	 * @throws DatabaseException
	 */
	public static ArrayList<RequestUser> getRequestUsersList(int aSystemId,
			int aRequestId, String usersList, int userType) throws DatabaseException {
		int usrId = 0;		
		String[] userLoginArray = usersList.split(",");
		ArrayList<RequestUser> ruList = new ArrayList<RequestUser>();
		int ruListSize = ruList.size();
		for (String userLogin : userLoginArray){
			User tempUsr = User.lookupByUserLogin(userLogin);
			if (tempUsr != null){
				usrId = tempUsr.getUserId();
				RequestUser ru = new RequestUser(aSystemId, aRequestId, userType, usrId, ++ruListSize, false);
				ruList.add(ru);									
			}
			else{
				continue;
			}								
		}
		return ruList;
	}
	
	static int getStatusIndex(String statusName) {
		if (statusName.equals(TataPowerUtils.STATUS_DCC)) {
			return TataPowerUtils.DCC_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_UDSI)) {
			return TataPowerUtils.SITE_IC_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_QS)) {
			return TataPowerUtils.QS_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_GROUP_HEAD)) {
			return TataPowerUtils.GH_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_HOC)) {
			return TataPowerUtils.HOC_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_FINANCE)) {
			return TataPowerUtils.FINANCE_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_CLOSED))
			return TataPowerUtils.CLOSED_INDEX;
		else
			return -1;
	}
	
	/**
	 * @param fieldHierarchyHT
	 * @return 
	 */
	static Hashtable<Integer, String> getFieldHierarchy() {
		Hashtable<Integer, String> fieldHierarchyHT = new Hashtable<Integer, String>();
		fieldHierarchyHT.put(1, DCC_RECEIPT_DATE + "," + DCC_ACKNOWLEDGE);
		fieldHierarchyHT.put(2, UDSI_RECIEPT_DATE + "," + SITE_IC_CERTIFIED);
		fieldHierarchyHT.put(3, QS_RECIEPT_DATE + "," + QS_VERIFIED);		
		fieldHierarchyHT.put(4, GH_RECIEPT_DATE + "," + UH_APPROVED);
		fieldHierarchyHT.put(5, HOC_RECIEPT_DATE + "," + HOC_APPROVED);
		fieldHierarchyHT.put(6, FINANCE_RECIEPT_DATE + "," + FIN_PAYMENT_DATE);
		return fieldHierarchyHT;
	}
	
	static int getStatusIndexGRN(String statusName) {
		if (statusName.equals(TataPowerUtils.STATUS_DCC)) {
			return TataPowerUtils.GRN_DCC_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_STORE)) {
			return TataPowerUtils.GRN_STORE_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_UDSI)) {
			return TataPowerUtils.GRN_SITE_IC_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_QUALITY)) {
			return TataPowerUtils.GRN_QUALITY_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_GROUP_HEAD)) {
			return TataPowerUtils.GRN_GH_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_HOC)) {
			return TataPowerUtils.GRN_HOC_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_FINANCE)) {
			return TataPowerUtils.GRN_FINANCE_INDEX;
		} else if (statusName.equals(TataPowerUtils.STATUS_CLOSED))
			return TataPowerUtils.CLOSED_INDEX;
		else
			return -1;
	}
	
	/**
	 * @param fieldHierarchyHT
	 * @return 
	 */
	static Hashtable<Integer, String> getFieldHierarchyGRN() {
		Hashtable<Integer, String> fieldHierarchyHT = new Hashtable<Integer, String>();
		fieldHierarchyHT.put(1, DCC_RECEIPT_DATE + "," + DCC_ACKNOWLEDGE);
		fieldHierarchyHT.put(2, STORE_RECEIVED_DATE + "," + STORE_ACKNOWLEDGE);
		fieldHierarchyHT.put(3, UDSI_RECIEPT_DATE + "," + SITE_IC_CERTIFIED);
		fieldHierarchyHT.put(4, QUALITY_RECIEVED_DATE + "," + QUALITY_ACKNOWLEDGE);		
		fieldHierarchyHT.put(5, GH_RECIEPT_DATE + "," + UH_APPROVED);
		fieldHierarchyHT.put(6, HOC_RECIEPT_DATE + "," + HOC_APPROVED);
		fieldHierarchyHT.put(7, FINANCE_RECIEPT_DATE + "," + FIN_PAYMENT_DATE);
		return fieldHierarchyHT;
	}
	
	// Getters for the Extended Field Values.
	public static Timestamp getExDate(String fieldName, Request request) throws DatabaseException, IllegalStateException
	{		
		RequestEx rex = getRequestEx(fieldName, DataType.DATE, request );
	//	System.out.println("DataType.DATE = " + DataType.DATE + ",   DataType.DATETIME = " +DataType.DATETIME ) ;
		/*if( null == rex)
			rex = getRequestEx( fieldName, DataType.DATETIME, request ) ;*/
		return rex.getDateTimeValue();
	}
	
	public static RequestEx getRequestEx(String fieldName, int dataTypeId, Request request) throws DatabaseException 
	{	
		Field f = Field.lookupBySystemIdAndFieldName(request.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalStateException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalStateException("'" + fieldName + "' is not an extended field");
		}
	//	System.out.println( "dataTypeid = " + dataTypeId + "   f.getDataTypeId = " + f.getDataTypeId() + " name = " + f.getName() );
		if(f.getDataTypeId() != dataTypeId)
			throw new IllegalStateException("The field '" + fieldName + "' is not of the type '" + dataTypeId + "'");
		RequestEx rex = request.getExtendedFields().get(f);
		return rex;
	}
	
	public static String getExString(String fieldName, Request request) throws DatabaseException, IllegalStateException
	{
		Field f = Field.lookupBySystemIdAndFieldName(request.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalStateException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalStateException("'" + fieldName + "' is not an extended field");
		}
		RequestEx rex = request.getExtendedFields().get(f);
		if(rex == null)
			return null;
		if((f.getDataTypeId() == DataType.TEXT) )
			return rex.getTextValue();
		else if(f.getDataTypeId() == DataType.STRING)
			return rex.getVarcharValue();
		else throw new IllegalStateException("This field is neither Text nor Varchar");
	}
	
	public static void setExDate(String fieldName, Timestamp value, Request request) throws DatabaseException
	{
		RequestEx rex = getRequestEx(fieldName, DataType.DATE, request);
		rex.setDateTimeValue(value);
	}
	
	public static Boolean getExBoolean(String fieldName, Request request) throws DatabaseException, IllegalStateException
	{
		RequestEx rex = getRequestEx(fieldName, DataType.BOOLEAN, request);
		return rex.getBitValue();
	}
	
	public static Type getExType(String fieldName, Request request) throws DatabaseException, IllegalStateException
	{
		RequestEx rex = getRequestEx(fieldName, DataType.TYPE,request);
		int typeId = rex.getTypeValue();
		return Type.lookupBySystemIdAndFieldNameAndTypeId(request.getSystemId(), fieldName, typeId);
	}
	
	
	public static long getISTDiffTime(Timestamp ts){
		long diff = 0;		
		Calendar istc = Calendar.getInstance(TimeZone.getTimeZone("IST"));			
		//long istDiff = 19800*1000;
		Date d1 = new Date(ts.getTime());// + istDiff);
		istc.setTime(d1);
		long istMillisNow = istc.getTimeInMillis();
		System.out.println("Date IST: " + istc.getTime());
		istc.set(Calendar.HOUR_OF_DAY, 23);
		istc.set(Calendar.MINUTE, 59);
		istc.set(Calendar.SECOND, 59);
		System.out.println("Date IST: " + istc.getTime());
		long istMillis23Hour = istc.getTimeInMillis();
		diff = istMillis23Hour - istMillisNow;
		System.out.println("Diff: " + diff);
		return diff;
	}

	protected static final String PERCENTAGE = "percentage";

	static final String NET_INVOICE_VALUE_FIELD= "NetInvoiceValue";

	static final String INVOICE_VALUE_FIELD = "InvoiceValue";

		
	public Date midnight(Date date, TimeZone tz) {
		  Calendar cal = new GregorianCalendar(tz);
		  cal.setTime(date);
		  cal.set(Calendar.HOUR_OF_DAY, 0);
		  cal.set(Calendar.MINUTE, 0);
		  cal.set(Calendar.SECOND, 0);
		  cal.set(Calendar.MILLISECOND, 0);
		  return cal.getTime();
		}
	
	public static void main(String[] argsv) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat(TBitsConstants.API_DATE_FORMAT);
		Date d = sdf.parse("2010-04-20 11:30:30");
		
		Timestamp ts = new Timestamp(d.getTime());
		getISTDiffTime(ts);
		
		/*long diff = getRemainingMilliSecondsInTheDay();
		System.out.println("Diff: " + diff);
		long sinceStart = Timestamp.getMillisSinceStart(TimeZone.getTimeZone("GMT"));
		Date d = new Date(sinceStart + diff);*/
		
		//Calendar calendar = Calendar.getInstance();		
		/*SimpleDateFormat sdf = new SimpleDateFormat(TBitsConstants.API_DATE_FORMAT);
		;
		calendar.setTime(d);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		System.out.println("Date: " + sdf.format(d));//calendar.getTime());
*/	}
}
