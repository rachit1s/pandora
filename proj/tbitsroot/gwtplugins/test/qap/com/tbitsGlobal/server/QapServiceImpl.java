package qap.com.tbitsGlobal.server;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineException;

import qap.com.tbitsGlobal.client.QapService;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.report.TBitsReportEngine;
import transbit.tbits.webapps.WebUtil;
import transmittal.com.tbitsGlobal.server.TransmittalUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class QapServiceImpl extends TbitsRemoteServiceServlet implements
		QapService, IFixedFields {
	
    String logger_ids="";
    String assignee_ids="";
    String subscriber_ids="";
    String AccessTo="";
    String notify="";
    String subject="";
    String emailBody="";
    int QtyEarlierReleased=0;
    int QtyAccepted=0;
    int QtyOrdered=0;
    int QtyRejected=0;
    int QtySubmitted=0;
    String POItemNo = "";
	public static final TBitsLogger LOG = TBitsLogger.getLogger("Qap");
	public static final String Inspection_Call_No = "InspectionCallNo";

	@Override
	public HashMap<String, Object> getRequestData(String sysPrefix, int sysid,
			ArrayList<Integer> requestIds) throws TbitsExceptionClient {

		HashMap<String, Object> map = new HashMap<String, Object>();
		HashMap<String, String> mdccTabledata = new HashMap<String, String>();

		map.put("requestData", getDataByRequestIds(sysPrefix, requestIds));

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select * from qap_mdcc_wizard_fields where sys_id=?");

			ps.setInt(1, sysid);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {

					String page1_commom = rs.getString("page1_common_fields");
					String page1_specific = rs.getString("page1_specific_fields");
					String page2_fields = rs.getString("page2_fields");
					String reportname = rs.getString("reportname");
					String reportfield = rs.getString("reportfield");

					mdccTabledata.put("page1common", page1_commom);
					mdccTabledata.put("pag1specific", page1_specific);
					mdccTabledata.put("page2fields", page2_fields);
					mdccTabledata.put("reportname", reportname);
					mdccTabledata.put("reportfield", reportfield);
				}
			}

			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		map.put("gridColumns", mdccTabledata);
		return map;

	}

	/**
	 * (non-Javadoc)returns the hashmap of requestId and requestObject
	 * corresponding to a list of requestIds
	 * 
	 * @param SystemPrefix
	 *            of a BA
	 * @param ArrayList
	 *            of requestIds
	 * 
	 *            * @see transmittal.com.tbitsGlobal.client.TransmittalService#
	 *            getDataByRequestIds(java.lang.String, java.util.List)
	 */
	public ArrayList<TbitsTreeRequestData> getDataByRequestIds(
			String sysPrefix, List<Integer> requestIds) {
		ArrayList<TbitsTreeRequestData> resp = new ArrayList<TbitsTreeRequestData>();

		for (int requestId : requestIds) {
			try {

				TbitsTreeRequestData model = getDataByRequestId(sysPrefix,
						requestId);
				if (model != null)
					resp.add(model);
			} catch (TbitsExceptionClient e) {
				LOG.info(TBitsLogger.getStackTrace(e));
			}

		}

		return resp;
	}

	/**
	 * (non-Javadoc)Retruns The Request Object corresponding to a requestId
	 * 
	 * @param System
	 *            Prefix of a business area
	 * @param RequestId
	 * @throws tbitsExceptionClient
	 * @return a requestobject
	 * @see transmittal.com.tbitsGlobal.client.TransmittalService#getDataByRequestId(java.lang.String,
	 *      int)
	 */

	public TbitsTreeRequestData getDataByRequestId(String sysPrefix,
			int requestId) throws TbitsExceptionClient {
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		return GWTServiceHelper.getDataByRequestId(user, ba, requestId);
	}

	@Override
	public String getHTMLTransmittalPreviewUsingBirt(
			HashMap<String, Object> paramTable) throws TbitsExceptionClient {
		
		Connection connection1 = null;
		Connection maxIdConn1 = null;
		String mdccbaSysPrefix1="MDCC";
		String dtnNumber1 = "";
		String inspectionSysPrefix1=(String) paramTable.get("sysPrefix");
			
		try {
			connection1 = DataSourcePool.getConnection();
			connection1.setAutoCommit(false);
			String sql="select * from qap_ba_mapping where itp_sys_prefix=?";
			
			PreparedStatement ps1=connection1.prepareStatement(sql);
			ps1.setString(1, inspectionSysPrefix1);
			ResultSet rs1= ps1.executeQuery();
			if(rs1.next())
				mdccbaSysPrefix1=rs1.getString("mdcc_sys_prefix");
			int transmittalMaxId1 = 0;

			String formattedTrnReqId1 = TransmittalUtils.EMPTY_STRING;
			
			String currentMdccFormat1=PropertiesHandler.getProperty("mdcc_number_format");
			
			String mdccFormat1="mdcc";
			
			String[] baFormat1=currentMdccFormat1.split(";");
			HashMap<String, String> formatMap1=new HashMap<String, String>();
			for(String s:baFormat1)
			{
				String[] split=s.split(",");
				formatMap1.put(split[0],split[1]);
			}
			
			if(formatMap1.containsKey(mdccbaSysPrefix1))
				mdccFormat1=formatMap1.get(mdccbaSysPrefix1);

			try {
				transmittalMaxId1 = this.getMaxTransmittalNumber(connection1, 0,
						mdccFormat1);
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			formattedTrnReqId1 = this
					.getFormattedStringFromNumber(transmittalMaxId1);

			// Now append the formatted running number with the
			// transmittal_id_prefix.
			dtnNumber1 = mdccFormat1+ "-";
			if (dtnNumber1 == null)
				dtnNumber1 = "";

			dtnNumber1 = dtnNumber1 + formattedTrnReqId1 + " " +"{likely}";
			connection1.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

//		String jsonString = (String) paramTable.get("json");
		String Remarks = (String) paramTable.get("Remarks");
		String Description = (String) paramTable.get("Description");
		String reqList = (String) paramTable.get("req");
		String[] page1common = (String[]) paramTable.get("page1common");
		String[] page1specific = (String[]) paramTable.get("page1specific");
//		this.logger_ids = (String) paramTable.get("logger_ids");
//	    this.assignee_ids =(String) paramTable.get("assignee_ids");
//	    this.subscriber_ids =(String) paramTable.get("subscriber_ids");
//	    this.AccessTo =(String) paramTable.get("AccessTo");
//	    this.notify =(String) paramTable.get("notify");
//	    this.subject =(String) paramTable.get("subject");
//	    this.emailBody =(String) paramTable.get("emailBody");

//		ArrayList<String[]> arr = new ArrayList<String[]>();
		String[] str = reqList.split(",");
		ArrayList<Integer> req = new ArrayList<Integer>();
		ArrayList<String[]> gridData = new ArrayList<String[]>();

		for (String reqId : str) {
			req.add(Integer.parseInt(reqId));
		}

		ArrayList<TbitsTreeRequestData> reqData = this.getDataByRequestIds(
				(String) paramTable.get("sysPrefix"), req);

		for (TbitsTreeRequestData tmd : reqData) {

			int count = 0;
			String[] tempString = new String[page1specific.length];

			for (String eachKey : page1specific) {

				if (eachKey.contains("date")) {
					POJO val3 = tmd.getAsPOJO(eachKey);
					tempString[count++] = val3.getValue().toString();
				} else {
					String val2 = tmd.getAsString(eachKey);
					tempString[count++] = val2;

				}

			}

			gridData.add(tempString);

		}

		HashMap<String, String> hm = new HashMap<String, String>();
		TbitsTreeRequestData trd = reqData.get(0);
        String linkedRequests = trd.getAsString(RELATED_REQUESTS);
        Object Qty_Submitte = trd.get("OfferQuantity");
        String Qty_Submitted = Qty_Submitte.toString();
//        QtySubmitted = Integer.parseInt(Qty_Submitted);
        Object Qty_Accepte = trd.get("no_of_irn");
        String Qty_Accepted = Qty_Accepte.toString();
        QtyAccepted = Integer.parseInt(Qty_Accepted);
        Object Qty_Rejecte = trd.get("no_of_nan");
        String Qty_Rejected = Qty_Rejecte.toString();
//        QtyRejected = Integer.parseInt(Qty_Rejected);
        hm.put("Qty_Submitted", Qty_Submitted);
        hm.put("Qty_Accepted", Qty_Accepted);
        hm.put("Qty_Rejected", Qty_Rejected);
        
        Collection<RequestDataType> crr = APIUtil.getRequestCollection(linkedRequests);
              if (crr != null)
              {
                for (RequestDataType rdt : crr) {
                  try
                  {
                    Request r = Request.lookupBySystemIdAndRequestId(
                      rdt.getSysId(), rdt.getRequestId());
//                    String EPCContractor = r.get("Agency");
//                    String nameOfSupplier = r.get("Contractor");
//                    String linkedRequest = r.get("related_requests");
                  }
                    catch (DatabaseException e)
                    {
                      e.printStackTrace();
                    }
                  }
                
//        			Collection<RequestDataType> rdt1=APIUtil.getRequestCollection(linkedRequest);
        			for(RequestDataType rd:crr)
        			{
        				 int sysId=rd.getSysId();
        				
        					try {
        						Request r1=Request.lookupBySystemIdAndRequestId(rd.getSysId(), rd.getRequestId());
        						String PO_Item_No = (String)r1.getObject("PONo");
        						String Qty_Ordered = r1.getObject("inventory").toString();
        						QtyOrdered = Integer.parseInt(Qty_Ordered);
//          						POItemNo = PO_Item_No;
          						QtyOrdered = Integer.parseInt(Qty_Ordered);
        	                    hm.put("PO_Item_No", PO_Item_No);
        	                    hm.put("Qty_Ordered", Qty_Ordered);
        	                    
        					} catch (DatabaseException e) {
        						e.printStackTrace();
        					}
        				
        			}
//                    hm.put("EPCContractor", EPCContractor);
//                    hm.put("nameOfSupplier", nameOfSupplier);
                    hm.put("MDCCNo", dtnNumber1);
                    String sql1 = "SELECT request_id FROM requests_ex where sys_id = ? and field_id=? and varchar_value=?";
				      for (RequestDataType rd : crr)
				      {
				          int reqId1 = rd.getRequestId();
				          int sysId1 = rd.getSysId();
				  			try {
								connection1 = DataSourcePool.getConnection();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								connection1.setAutoCommit(false);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        	Request r1;
				        	Field qapSerialField=null;
				        	String serial=null;
							try {
								r1 = Request.lookupBySystemIdAndRequestId(sysId1, reqId1);
					            qapSerialField = Field.lookupBySystemIdAndFieldName(sysId1, "QAPSerialNo");
					            serial = r1.get("QAPSerialNo");
							} catch (DatabaseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}


				              PreparedStatement ps1;
				              ArrayList reqList1 = new ArrayList();
							try {
								ps1 = connection1.prepareStatement(sql1);
					              ps1.setInt(1, sysId1);
					              ps1.setInt(2, qapSerialField.getFieldId());
					              ps1.setString(3, serial);
					              ResultSet rs1 = ps1.executeQuery();
					              while (rs1.next())
					              {
					                reqList1.add(Integer.valueOf(rs1.getInt("request_id")));
					              }
					              rs1.close();
					              ps1.close();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Request r2=null;
				              try {
								ArrayList<Request> reqListQap = Request.lookupBySystemIdAndRequestIdList(sysId1, reqList1);
								r2 = Request.lookupBySystemIdAndRequestId(sysId1, (Integer)reqList1.get(0));
							} catch (DatabaseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
				              
				              int no_of_mdcc= Integer.parseInt(r2.get("no_of_mdcc"));
				              String Qty_Earlier_Released = r2.getObject("no_of_mdcc").toString();
				              QtyEarlierReleased = Integer.parseInt(Qty_Earlier_Released);
				              hm.put("Qty_Earlier_Released", Qty_Earlier_Released);
				              try {
								connection1.close();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				          }
                  

              }
		for (String eachKey : page1common) {
			hm.put(eachKey, trd.getAsString(eachKey));
		}
		hm.put("Remarks", Remarks);
		hm.put("Description", Description);
		hm.put("OS_Unit", (String) paramTable.get("OS_Unit"));
		hm.put("QA_Ref_No", (String) paramTable.get("QA_Ref_No"));
		hm.put("Shipping_Release_No", (String) paramTable.get("Shipping_Release_No"));
		hm.put("Office_SR_No", (String) paramTable.get("Office_SR_No"));
		hm.put("QA_Ref_No_for_Main_Order", (String) paramTable.get("QA_Ref_No_for_Main_Order"));
		ArrayList users = null;
		try {
			users = Utility.toUsers((String) paramTable.get("user"));
		} catch (CorrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String logger = "";
	    Iterator localIterator = users.iterator(); if (localIterator.hasNext()) { User user1 = (User)localIterator.next();

	      if ((user1.getDisplayName() != null) && (!user1.getDisplayName().trim().equals("-"))) {
	    	  logger = logger +user1.getFirstName()+" "+user1.getLastName()+ "</br>"+user1.getDesignation();
	    	  hm.put("logger", logger);
	      }
	    }
	    
	    ArrayList to = null;
		try {
			to = Utility.toUsers((String) paramTable.get("assignee_ids"));
		} catch (CorrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String tolist = "";
	    Iterator localIterator1 = to.iterator(); if (localIterator1.hasNext()) { User user1 = (User)localIterator1.next();

	      if ((user1.getDisplayName() != null) && (!user1.getDisplayName().trim().equals("-"))) {
	    	  tolist = tolist +user1.getFullFirmName()+"</br>"+user1.getFirmAddress();
	    	  hm.put("tolist", tolist);
	      }
	    }
	    
	    ArrayList cc = null;
		try {
			cc = Utility.toUsers((String) paramTable.get("subscriber_ids"));
		} catch (CorrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String Copy_For = "";
		String Location = "";
	    Iterator localIterator2 = cc.iterator(); 
	    for(int i=0;localIterator2.hasNext();i++) 
	    { User user1 = (User)localIterator2.next();
	    
	      if ((user1.getDisplayName() != null) && (!user1.getDisplayName().trim().equals("-"))) {
	    	  Copy_For = (new StringBuilder(String.valueOf(Copy_For))).append(user1.getFirstName()+" "+user1.getLastName()).append("</br>").toString();
	    	  Location = (new StringBuilder(String.valueOf(Location))).append(user1.getLocation()).append("</br>").toString();
	      }
            continue;
	    }
	      hm.put("Copy_For", Copy_For);
	      hm.put("Location", Location);
	      
	      String Shipment_Status="";
	      if(QtyOrdered==(QtyEarlierReleased + QtyAccepted))
	    	 Shipment_Status="COMPLETE";
	      else
	    	  Shipment_Status="PARTIAL"; 
	      hm.put("Shipment_Status", Shipment_Status);
		String htmlString;
		QapBirtTemplateHelper kth = new QapBirtTemplateHelper(hm, gridData);

		try {
			ByteArrayOutputStream htmlOS = null;
			String rptDesignFileName = (String) paramTable.get("reportname");
			if (rptDesignFileName == null)
				throw new TbitsExceptionClient(
						"Error occurred while generating mdcc template. "
								+ "RPT Design file name was not configured for this MDCC process.");
			try {
				htmlOS = QapUtils.generateTransmittalNoteInHtml(
						rptDesignFileName, kth, this.getRequest()
								.getContextPath());
			} catch (IOException e) {
				e.printStackTrace();
				throw new TbitsExceptionClient(
						"Error occurred while generating MDCC template. "
								+ e.getMessage(), e);
			} catch (TBitsException e) {
				e.printStackTrace();
				throw new TbitsExceptionClient(e.getMessage());
			}
			htmlString = htmlOS.toString();
			htmlOS.close();
		} catch (EngineException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while generating transmittal template. "
							+ e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while generating transmittal template. "
							+ e.getMessage(), e);
		}

		return htmlString;

	}

	/**
	 * @param jsonMap
	 * @param jsonString
	 */
	protected static ArrayList<String[]> fetchKeyValuePairsfromJsonString(
			String jsonString) throws JsonParseException {

		ArrayList<String[]> arr = new ArrayList<String[]>();
		if (jsonString != null) {
			final JsonParser parseValue = new JsonParser();
			final JsonElement jElem = parseValue.parse(jsonString);
			final JsonArray jsonArray = jElem.getAsJsonArray();

			for (int i = 0; i < jsonArray.size(); i++) {
				if ((jsonArray.get(i) != null)
						&& (!jsonArray.get(i).isJsonNull()))

				{
					JsonArray jArray = null;
					jArray = jsonArray.get(i).getAsJsonArray();
					String[] str = new String[jArray.size()];
					for (int j = 0; j < jArray.size(); j++) {
						str[j] = jArray.get(j).getAsString();

					}
					arr.add(str);

				}
			}
		}
		return arr;
	}

	private String getDTNAttachment(QapBirtTemplateHelper kth,
			String templateName, String outputFileName, String attachmentHint)
			throws DatabaseException, FileNotFoundException, IOException,
			EngineException, TBitsException {

		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();
		String pdfFilePath = "";
		try {
			pdfFilePath = this.generateTransmittalNoteUsingBirt(templateName,
					kth, outputFileName);
		} catch (BirtException e) {
			e.printStackTrace();
		}
		File pdfFile = new File(pdfFilePath);
		Uploader uploader = new Uploader();
		uploader.setFolderHint(attachmentHint);
		AttachmentInfo trnNoteInfo = uploader.moveIntoRepository(pdfFile);
		trnAttCollection.add(trnNoteInfo);
		tempAttachments.append(AttachmentInfo.toJson(trnAttCollection));
		return tempAttachments.toString();
	}

	public static String generateTransmittalNoteUsingBirt(
			String rptDesignFileName, QapBirtTemplateHelper kth,
			String outputFileName) throws BirtException, TBitsException {

		TBitsReportEngine tBitsEngine = TBitsReportEngine.getInstance();
		Map<Object, Object> reportVariables = new HashMap<Object, Object>();
		Map<String, Object> reportParams = new HashMap<String, Object>();
		String tempDir = Configuration.findAbsolutePath(PropertiesHandler
				.getProperty(transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
		outputFileName = outputFileName.replaceAll("[^A-Za-z0-9]+", "_");
		String pdfFilePath = tempDir + File.separator + outputFileName
				+ TransmittalUtils.PDF;
		File outFile = new File(pdfFilePath);
		reportVariables.put("BirtTemplateHandler", kth);
		File generatedPDFFile = tBitsEngine.generatePDFFile(rptDesignFileName,
				reportVariables, reportParams, outFile);
		return generatedPDFFile.getAbsolutePath();
	}

	/**
	 * @param transmittalDate
	 * @return
	 * @throws ParseException
	 */
	private static String getLoggedDate(String transmittalDate)
			throws ParseException {
		String logDate = "";
		if ((transmittalDate == null) || transmittalDate.trim().equals("")) {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("IST"));
			Date d = c.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat(
					TBitsConstants.API_DATE_FORMAT);
			logDate = sdf.format(d);
		} else {
			DateFormat df = new SimpleDateFormat(
					TBitsConstants.API_DATE_ONLY_FORMAT);
			Date d = df.parse(transmittalDate);
			logDate = Timestamp.toCustomFormat(d,
					TBitsConstants.API_DATE_FORMAT);
		}
		return logDate;
	}

	/*public static void main(String[] args) {
		Request insReq;
		Hashtable<String, String> aParamTable = new Hashtable<String, String>();
		aParamTable.put(BUSINESS_AREA, "QAP");
		aParamTable.put(USER, "root");
		AddRequest addRequest = new AddRequest();

		try {
			insReq = addRequest.addRequest(aParamTable);

		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/

	public TbitsTreeRequestData createTransmittal(
			HashMap<String, Object> paramTable,
			HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap)
			throws TbitsExceptionClient {

		Connection connection = null;
		Connection maxIdConn = null;
		TbitsTreeRequestData insMDCCTreeReqData = null;
		Request insReq = null;
		TBitsResourceManager tBitsResMgr = new TBitsResourceManager();

		String[] page1common = (String[]) paramTable.get("page1common");
		String[] page1specific = (String[]) paramTable.get("page1specific");
		String inspectionSysPrefix=(String) paramTable.get("sysPrefix");
		String reqList = (String) paramTable.get("requestList");
		this.logger_ids = (String) paramTable.get("logger_ids");
		if(this.logger_ids==null)
			this.logger_ids="";
	    this.assignee_ids =(String) paramTable.get("assignee_ids");
	    this.subscriber_ids =(String) paramTable.get("subscriber_ids");
	    if(this.subscriber_ids==null)
			this.subscriber_ids="";
	    this.AccessTo =(String) paramTable.get("AccessTo");
	    this.notify =(String) paramTable.get("notify");
	    this.subject =(String) paramTable.get("subject");
	    if(this.subject==null)
			this.subject="";
	    this.emailBody =(String) paramTable.get("emailBody");
		String user = (String) paramTable.get("user");
		System.out.println("Current User: " + user);
		String mdccbaSysPrefix="MDCC";
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			String sql="select * from qap_ba_mapping where itp_sys_prefix=?";
			
			PreparedStatement ps=connection.prepareStatement(sql);
			ps.setString(1, inspectionSysPrefix);
			ResultSet rs= ps.executeQuery();
			if(rs.next())
				mdccbaSysPrefix=rs.getString("mdcc_sys_prefix");
			int transmittalMaxId = 0;

			String dtnNumber = "";

			String formattedTrnReqId = TransmittalUtils.EMPTY_STRING;
			
			String currentMdccFormat=PropertiesHandler.getProperty("mdcc_number_format");
			
			String mdccFormat="mdcc";
			
			String[] baFormat=currentMdccFormat.split(";");
			HashMap<String, String> formatMap=new HashMap<String, String>();
			for(String s:baFormat)
			{
				String[] split=s.split(",");
				formatMap.put(split[0],split[1]);
			}
			
			if(formatMap.containsKey(mdccbaSysPrefix))
				mdccFormat=formatMap.get(mdccbaSysPrefix);

			transmittalMaxId = this.getMaxTransmittalNumber(connection, 0,
					mdccFormat);
			formattedTrnReqId = this
					.getFormattedStringFromNumber(transmittalMaxId);

			// Now append the formatted running number with the
			// transmittal_id_prefix.
			dtnNumber = mdccFormat+ "-";
			if (dtnNumber == null)
				dtnNumber = "";

			dtnNumber = dtnNumber + formattedTrnReqId;
			// ---------------------------------------------------------------------//

			/*
			 * 
			 * ?
			 */

			String[] str = reqList.split(",");
			ArrayList<Integer> req = new ArrayList<Integer>();
			ArrayList<String[]> gridData = new ArrayList<String[]>();

			for (String reqId : str) {
				req.add(Integer.parseInt(reqId));
			}

			ArrayList<TbitsTreeRequestData> reqData = this.getDataByRequestIds(
					(String) paramTable.get("sysPrefix"), req);
			// ----------------//

			String Remarks = (String) paramTable.get("Remarks");
			String Description = (String) paramTable.get("Description");
//			ArrayList<String[]> arr = fetchKeyValuePairsfromJsonString(jsonString);
			String attString = "";

			for (TbitsTreeRequestData tmd : reqData) {

				int count = 0;
				String[] tempString = new String[page1specific.length];

				for (String eachKey : page1specific) {

					if (eachKey.contains("date")) {
						POJO val3 = tmd.getAsPOJO(eachKey);
						tempString[count++] = val3.getValue().toString();
					} else {
						String val2 = tmd.getAsString(eachKey);
						tempString[count++] = val2;

					}

				}

				gridData.add(tempString);

			}

			HashMap<String, String> hm = new HashMap<String, String>();
			TbitsTreeRequestData trd = reqData.get(0);
			String linkedRequests = trd.getAsString(RELATED_REQUESTS);
	        Object Qty_Submitte = trd.get("OfferQuantity");
	        String Qty_Submitted = Qty_Submitte.toString();
	        QtySubmitted = Integer.parseInt(Qty_Submitted);
	        Object Qty_Accepte = trd.get("no_of_irn");
	        String Qty_Accepted = Qty_Accepte.toString();
	        QtyAccepted = Integer.parseInt(Qty_Accepted);
	        Object Qty_Rejecte = trd.get("no_of_nan");
	        String Qty_Rejected = Qty_Rejecte.toString();
	        QtyRejected = Integer.parseInt(Qty_Rejected);
	        hm.put("Qty_Submitted", Qty_Submitted);
	        hm.put("Qty_Accepted", Qty_Accepted);
	        hm.put("Qty_Rejected", Qty_Rejected);
	        Collection<RequestDataType> crr1 = APIUtil.getRequestCollection(linkedRequests);
	              if (crr1 != null)
	              {

	                  for (RequestDataType rdt : crr1) {
	                    try
	                    {
	                      Request r = Request.lookupBySystemIdAndRequestId(
	                        rdt.getSysId(), rdt.getRequestId());
//	                      String EPCContractor = r.get("Agency");
//	                      String nameOfSupplier = r.get("Contractor");
//	                      String linkedRequest = r.get("related_requests");
	                    }
	                    catch (DatabaseException e)
	                    {
	                      e.printStackTrace();
	                    }
	                  }
////	          			Collection<RequestDataType> rdt1=APIUtil.getRequestCollection(linkedRequest);
	          			for(RequestDataType rd:crr1)
	          			{
	          				 int sysId=rd.getSysId();
	          				
	          					try {
	          						Request r1=Request.lookupBySystemIdAndRequestId(rd.getSysId(), rd.getRequestId());
	          						String PO_Item_No = (String)r1.getObject("PONo");
	          						String Qty_Ordered = r1.getObject("inventory").toString();
	          						POItemNo = PO_Item_No;
	          						QtyOrdered = Integer.parseInt(Qty_Ordered);
	          	                    hm.put("PO_Item_No", PO_Item_No);
	          	                    hm.put("Qty_Ordered", Qty_Ordered);
	          					} catch (DatabaseException e) {
	          						e.printStackTrace();
	          					}
	          				
	          			}
//	                      hm.put("EPCContractor", EPCContractor);
//	                      hm.put("nameOfSupplier", nameOfSupplier);
	                      hm.put("MDCCNo", dtnNumber);
	                      String sql1 = "SELECT request_id FROM requests_ex where sys_id = ? and field_id=? and varchar_value=?";
	  				      for (RequestDataType rd : crr1)
	  				      {
	  				          int reqId1 = rd.getRequestId();
	  				          int sysId1 = rd.getSysId();
	  				        Connection connection1=null;
	  								connection1 = DataSourcePool.getConnection();
	  								connection1.setAutoCommit(false);
	  				        	Request r1 = Request.lookupBySystemIdAndRequestId(sysId1, reqId1);
	  				            Field qapSerialField = Field.lookupBySystemIdAndFieldName(sysId1, "QAPSerialNo");
	  				            String serial = r1.get("QAPSerialNo");

	  				              PreparedStatement ps1;
	  				              ArrayList reqList1 = new ArrayList();
	  							try {
	  								ps1 = connection1.prepareStatement(sql1);
	  					              ps1.setInt(1, sysId1);
	  					              ps1.setInt(2, qapSerialField.getFieldId());
	  					              ps1.setString(3, serial);
	  					              ResultSet rs1 = ps1.executeQuery();
	  					              while (rs1.next())
	  					              {
	  					                reqList1.add(Integer.valueOf(rs1.getInt("request_id")));
	  					              }
	  					              rs1.close();
	  					              ps1.close();
	  							} catch (SQLException e1) {
	  								// TODO Auto-generated catch block
	  								e1.printStackTrace();
	  							}
	  				              ArrayList<Request> reqListQap = Request.lookupBySystemIdAndRequestIdList(sysId1, reqList1);
	  				              Request r2 = Request.lookupBySystemIdAndRequestId(sysId1, (Integer)reqList1.get(0));
	  				              int no_of_mdcc= Integer.parseInt(r2.get("no_of_mdcc"));
	  				              String Qty_Earlier_Released = r2.getObject("no_of_mdcc").toString();
	  				             QtyEarlierReleased = Integer.parseInt(Qty_Earlier_Released);
	  				              hm.put("Qty_Earlier_Released", Qty_Earlier_Released);    
	  				            connection1.close();
	                    }
	              }

			// need to change places over here....
			for (String eachKey : page1common) {
				hm.put(eachKey, trd.getAsString(eachKey));
			}
			
			hm.put("Remarks", Remarks);
			hm.put("Description", Description);
			hm.put("OS_Unit", (String) paramTable.get("OS_Unit"));
			hm.put("QA_Ref_No", (String) paramTable.get("QA_Ref_No"));
			hm.put("Shipping_Release_No", (String) paramTable.get("Shipping_Release_No"));
			hm.put("Office_SR_No", (String) paramTable.get("Office_SR_No"));
			hm.put("QA_Ref_No_for_Main_Order", (String) paramTable.get("QA_Ref_No_for_Main_Order"));
			ArrayList users = null;
			try {
				users = Utility.toUsers((String) paramTable.get("user"));
			} catch (CorrException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String logger = "";
		    Iterator localIterator = users.iterator(); if (localIterator.hasNext()) { User user1 = (User)localIterator.next();

		      if ((user1.getDisplayName() != null) && (!user1.getDisplayName().trim().equals("-"))) {
		    	  logger = logger +user1.getFirstName()+" "+user1.getLastName()+ "</br>"+user1.getDesignation();
		    	  hm.put("logger", logger);
		      }
		    }
		    
		    ArrayList to = null;
			try {
				to = Utility.toUsers((String) paramTable.get("assignee_ids"));
			} catch (CorrException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String tolist = "";
		    Iterator localIterator1 = to.iterator(); if (localIterator1.hasNext()) { User user1 = (User)localIterator1.next();

		      if ((user1.getDisplayName() != null) && (!user1.getDisplayName().trim().equals("-"))) {
		    	  tolist = tolist +user1.getFullFirmName()+"</br>"+user1.getFirmAddress();
		    	  hm.put("tolist", tolist);
		      }
		    }
		    
		    ArrayList cc = null;
			try {
				cc = Utility.toUsers((String) paramTable.get("subscriber_ids"));
			} catch (CorrException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String Copy_For = "";
			String Location = "";
		    Iterator localIterator2 = cc.iterator(); 
		    for(int i=0;localIterator2.hasNext();i++) 
		    { User user1 = (User)localIterator2.next();
		    
		      if ((user1.getDisplayName() != null) && (!user1.getDisplayName().trim().equals("-"))) {
		    	  Copy_For = (new StringBuilder(String.valueOf(Copy_For))).append(user1.getFirstName()+" "+user1.getLastName()).append("</br>").toString();
		    	  Location = (new StringBuilder(String.valueOf(Location))).append(user1.getLocation()).append("</br>").toString();
		      }
	            continue;
		    }
		      hm.put("Copy_For", Copy_For);
		      hm.put("Location", Location);

		      String Shipment_Status="";
		      if(QtyOrdered==(QtyEarlierReleased + QtyAccepted))
		    	 Shipment_Status="COMPLETE";
		      else
		    	  Shipment_Status="PARTIAL"; 
		      hm.put("Shipment_Status", Shipment_Status);
//			hm.put("MDCCNo", dtnNumber);
			System.out.println(formattedTrnReqId);

			// *******************//

			QapBirtTemplateHelper kth = new QapBirtTemplateHelper(hm,
					gridData);
			String templatename = (String) paramTable.get("reportname");
			String attachmentHint = mdccbaSysPrefix;

			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> attachmentsInfoMap = new HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>>();
			ArrayList<AttachmentInfo> arr2 = new ArrayList<AttachmentInfo>();
			for (Integer requestId : attachmentInfoClientsMap.keySet()) {

				HashMap<String, List<FileClient>> attachmentInfoClients = attachmentInfoClientsMap
						.get(requestId);
				if (attachmentInfoClients != null) {
					ArrayList<AttachmentInfo> convertedAttachmentsList = getConvertedAttachmentsMap(attachmentInfoClients);
					if (convertedAttachmentsList != null)
						arr2.addAll(convertedAttachmentsList);

				}
			}

			String docs = AttachmentInfo.toJson(arr2);
			try {
				attString = this.getDTNAttachment(kth, templatename, dtnNumber,
						attachmentHint);
			} catch (FileNotFoundException e1) {
				rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
				e1.printStackTrace();
				LOG.error(
						"File not found exception occurred during MDCC process: "
								+ e1.getMessage(), e1);
				throw new TbitsExceptionClient(
						"File not found exception occurred during MDCC process: "
								+ e1.getMessage(), e1);
			} catch (EngineException e1) {
				rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1.getMessage(), e1);

			} catch (DatabaseException e1) {
				rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1.getMessage(), e1);
			} catch (IOException e1) {
				rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1.getMessage(), e1);
			} catch (TBitsException e1) {
				rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1.getMessage(), e1);
			}

			SimpleDateFormat datefrmat = new SimpleDateFormat(
					TBitsConstants.API_DATE_FORMAT);

			Hashtable<String, String> paramTable1 = new Hashtable<String, String>();
			paramTable1.put(USER, "root");
			paramTable1.put(BUSINESS_AREA, mdccbaSysPrefix);
			paramTable1.put("attachments", docs);
			paramTable1.put("MDCCFile", attString);
			paramTable1.put("MDCC", dtnNumber);
			paramTable1.put("transmittalDate", getLoggedDate(""));
			paramTable1.put("subject", this.subject);
			paramTable1.put("assignee_ids", this.assignee_ids);
			paramTable1.put("subscriber_ids", this.subscriber_ids);
			paramTable1.put("description", this.emailBody);
			Object PONo = POItemNo;
			paramTable1.put("po_item_no", PONo.toString());
			Object QtyOrd = QtyOrdered;
			paramTable1.put("qty_ordered", QtyOrd.toString());
			Object QtyEarlier = QtyEarlierReleased;
			paramTable1.put("qty_earlier_relsd", QtyEarlier.toString());
			Object QtySub = QtySubmitted;
			paramTable1.put("qty_submit", QtySub.toString());
			Object QtyAccep = QtyAccepted;
			paramTable1.put("qty_accepted", QtyAccep.toString());
			Object QtyReject = QtyRejected;
			paramTable1.put("qty_rejected", QtyReject.toString());

			String relatedRequests = "";
			for (TbitsTreeRequestData trData : reqData) {

				BusinessArea ba = BusinessArea.lookupBySystemId(trData
						.getSystemId());
				String relatedReq = ba.getSystemPrefix() + "#"
						+ trData.getRequestId() + "#" + trData.getMaxActionId();
				if (relatedRequests == null
						|| relatedRequests.equalsIgnoreCase(""))
					relatedRequests = relatedReq;
				else
					relatedRequests = relatedRequests + "," + relatedReq;
			}
			System.out.println("Related Request:  " + relatedRequests);

			paramTable.put(Field.RELATED_REQUESTS, relatedRequests);
			paramTable1.put(Field.RELATED_REQUESTS, relatedRequests);

			String contextpath = "";

			HttpServletRequest httpRequest = this.getRequest();

			if (httpRequest != null) {
				contextpath = httpRequest.getContextPath();
			}

			AddRequest addRequest = new AddRequest();
			//String mdccSysPrefix=BusinessArea.lookupBySystemId(insReq.getSystemId()).getSystemPrefix();

			try {

				insReq = addRequest.addRequest(connection, tBitsResMgr,
						paramTable1);
			} catch (APIException e1) {
				rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1.getMessage(), e1);
			}
			System.out.println(insReq.getRequestId() + "->"
					+ insReq.getSystemId());
			paramTable.clear();

			try {
				// paramTable.put(FileMetaData.LAST_UPDATED_DATE,
				// lastUpdatedDate);
				// String lastUpdatedDate = datefrmat.format(getLoggedDate(""));
			} catch (Exception e1) {
				rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1.getMessage(), e1);
			}

			Hashtable<String, String> paramTable2 = new Hashtable<String, String>();
			paramTable2.put(Field.BUSINESS_AREA,inspectionSysPrefix);
			paramTable2.put(Field.USER, user);
			
			int inspectionSysId= BusinessArea.lookupBySystemPrefix(inspectionSysPrefix).getSystemId();

			for (String reqId : reqList.split(",")) {
				try {
					String irn= "0";
					Request rqst = Request.lookupBySystemIdAndRequestId(inspectionSysId, Integer.parseInt(reqId));
					Integer OfferQuantity = Integer.parseInt(rqst.get("OfferQuantity")); 
					Integer no_of_irn = Integer.parseInt(rqst.get("no_of_irn"));
					Object offerqty = OfferQuantity - no_of_irn;
					String Offerquantity = (String) offerqty.toString();
					UpdateRequest upRequest = new UpdateRequest();
					paramTable2.put(Field.REQUEST, reqId);
					paramTable2.put(Field.DESCRIPTION,
							"Released the MDCC for the Particulars : \n "
									+ "MDCC NO.# [" + formattedTrnReqId + "]\n"
									+ "MDCC Document ID : " + "["+ mdccbaSysPrefix + "#"
									+ insReq.getRequestId() + "]" + "No. of MdCC released is : " 
									+ Request.lookupBySystemIdAndRequestId(inspectionSysId, Integer.parseInt(reqId)).get("no_of_irn")
									+"\n");
					paramTable2.put("no_of_irn", irn);
					paramTable2.put("OfferQuantity", Offerquantity);
					Request upsReq = upRequest.updateRequest(connection,
							tBitsResMgr, paramTable2);

					// ///////////////////////////////////////////

					String linkedRequsetIds = upsReq.getRelatedRequests();
					if (linkedRequsetIds != null) {
						System.out.println("linkedRequsetId :"
								+ linkedRequsetIds);
						Collection<RequestDataType> crr = APIUtil
								.getRequestCollection(linkedRequsetIds);
						if (crr != null) {
							for (RequestDataType rdt : crr) {
								Hashtable<String, String> paramTable3 = new Hashtable<String, String>();
								BusinessArea linkedBa = BusinessArea
										.lookupBySystemId(rdt.getSysId());
								paramTable3.put(Field.BUSINESS_AREA, linkedBa
										.getSystemPrefix());
								paramTable3.put(Field.USER, user);
								paramTable3.put(Field.REQUEST, Integer
										.toString(rdt.getRequestId()));
								paramTable3
										.put(
												Field.DESCRIPTION,
												"Released the MDCC for the Particulars : \n "
														+ "MDCC NO.# ["
														+ dtnNumber
														+ "]\n"
														+ "MDCC Document ID : "
														+ "[" + mdccbaSysPrefix 
														+ "#"
														+ insReq.getRequestId()
														+ "]\n"
														+ "Inspsction ID : ["
														+ upsReq
																.get("InspectionCallNo")
														+ "]\n"
														+ "On Inspection Call Document Id : "
														+ "["+ inspectionSysPrefix + "#"
														+ upsReq.getRequestId()
														+ "]"
														+ "No. of MdCC released is : " + 
														Request.lookupBySystemIdAndRequestId(inspectionSysId, Integer.parseInt(reqId)).get("no_of_irn")
														+"\n");
								UpdateRequest upRequest1 = new UpdateRequest();
								Request upsLinkedReq = upRequest1
										.updateRequest(connection, tBitsResMgr,
												paramTable3);
								System.out.println(upsLinkedReq.getRequestId());

					  Collection<RequestDataType> rdt1 = APIUtil.getRequestCollection(linkedRequsetIds);
				      String sql1 = "SELECT request_id FROM requests_ex where sys_id = ? and field_id=? and varchar_value=?";
				      UpdateRequest upRequests1 = new UpdateRequest();
				      for (RequestDataType rd : rdt1)
				      {

				          int reqId1 = rd.getRequestId();
				          int sysId1 = rd.getSysId();
				          try {
				            Request r = Request.lookupBySystemIdAndRequestId(sysId1, reqId1);
				            Field qapSerialField = Field.lookupBySystemIdAndFieldName(sysId1, "QAPSerialNo");
				            String serial = r.get("QAPSerialNo");

				              PreparedStatement ps1 = connection.prepareStatement(sql1);
				              ps1.setInt(1, sysId1);
				              ps1.setInt(2, qapSerialField.getFieldId());
				              ps1.setString(3, serial);
				              ArrayList reqList1 = new ArrayList();
				              ResultSet rs1 = ps1.executeQuery();
				              while (rs1.next())
				              {
				                reqList1.add(Integer.valueOf(rs1.getInt("request_id")));
				              }
				              rs1.close();
				              ps1.close();
				              ArrayList<Request> reqListQap = Request.lookupBySystemIdAndRequestIdList(sysId1, reqList1);
				              Request r1 = Request.lookupBySystemIdAndRequestId(sysId1, (Integer)reqList1.get(0));
				              int no_of_mdcc= Integer.parseInt(r1.get("no_of_mdcc"));
				              Object mdcc= no_of_mdcc + no_of_irn;
				              Hashtable paramTables = new Hashtable();
				              paramTables.put("sys_id", Integer.toString(sysId1));
				              paramTables.put(Field.USER, user);
				              paramTables.put("request_id", Integer.toString((Integer)reqList1.get(0)));
				              paramTables.put("no_of_mdcc", mdcc.toString());
				              Request localRequest1 = upRequests1.updateRequest(connection, tBitsResMgr, paramTables);
				          }
				          catch (Exception e)
				          {
								rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
								e.printStackTrace();
				          }
				      }

							}
						}
					}
				} catch (APIException e1) {
					rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
					e1.printStackTrace();
					throw new TbitsExceptionClient(e1.getMessage(), e1);
				} catch (TBitsException e1) {
					rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
					e1.printStackTrace();
					throw new TbitsExceptionClient(e1.getMessage(), e1);
				} catch (Exception e1) {
					rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
					e1.printStackTrace();
					throw new TbitsExceptionClient(e1.getMessage(), e1);
				}

			}
			connection.commit();
			tBitsResMgr.commit();
		} catch (DatabaseException e1) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			e1.printStackTrace();
			throw new TbitsExceptionClient(e1.getDescription(), e1);
		} catch (SQLException e1) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			APIException apie = new APIException();
			apie.addException(new TBitsException(
					"Unable to get connection to the database"));
			LOG.error(apie);
			throw new TbitsExceptionClient(e1.getMessage(), e1);
		} catch (ParseException e1) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			e1.printStackTrace();
			throw new TbitsExceptionClient(e1.getMessage(), e1);
		} finally {
			try {
				if ((connection != null) && (!connection.isClosed()))
					connection.close();
				if ((maxIdConn != null) && (!maxIdConn.isClosed()))
					maxIdConn.close();
			} catch (SQLException e) {
				LOG.error("Unable to close the connection to the database.", e);
				throw new TbitsExceptionClient(
						"Unable to get connection to the database: \n"
								+ e.getMessage(), e);
			}
		}
		insMDCCTreeReqData = getDataByRequestId(mdccbaSysPrefix, insReq.getRequestId());
		return insMDCCTreeReqData;
	}

	private ArrayList<AttachmentInfo> getConvertedAttachmentsMap(
			HashMap<String, List<FileClient>> attachmentInfoClients) {

		ArrayList<AttachmentInfo> attachments = new ArrayList<AttachmentInfo>();
		if (attachmentInfoClients != null) {
			for (String fieldName : attachmentInfoClients.keySet()) {
				ArrayList<FileClient> aicList = (ArrayList<FileClient>) attachmentInfoClients
						.get(fieldName);
				if (aicList != null) {
					for (FileClient aic : aicList) {
						if (aic != null) {
							AttachmentInfo ai = new AttachmentInfo();
							ai.name = aic.getFileName();
							ai.repoFileId = aic.getRepoFileId();
							ai.requestFileId = aic.getRequestFileId();
							ai.size = aic.getSize();
							attachments.add(ai);
						}
					}
				}

			}
		}
		return attachments;
	}

	public int getMaxTransmittalNumber(Connection connection, int aSystemId,
			String transmittalProcessName) throws DatabaseException {
		int maxTransmittalNumber = -1;
		try {
			CallableStatement cs = connection
					.prepareCall("stp_getAndIncrMaxId ?");
			cs.setString(1, transmittalProcessName);
			ResultSet rs = cs.executeQuery();
			if ((rs != null) && (rs.next())) {
				maxTransmittalNumber = rs.getInt("max_id");
				// System.out.println("MaxId: " + maxTransmittalNumber);
				return maxTransmittalNumber;
			} else {
				throw new SQLException();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(
					"Error occurred while retrieving max MDCC number for sysId: "
							+ aSystemId, e);
		}
	}

	/**
	 * @param transId
	 *            Transmittal Id.
	 * @return returns a String representing transmittal request id
	 */
	public String getFormattedStringFromNumber(int transmittalId) {
		String ref;
		NumberFormat formatter = new DecimalFormat("00000");
		ref = formatter.format(transmittalId);
		return ref;
	}

	/**
	 * @param connection
	 * @param maxIdConn
	 * @param tBitsResMgr
	 */
	private void rollbackAllOperations(Connection connection,
			Connection maxIdConn, TBitsResourceManager tBitsResMgr) {
		try {
			if (connection != null) {
				connection.rollback();
			}
			if (maxIdConn != null) {
				maxIdConn.rollback();
			}

			tBitsResMgr.rollback();

		} catch (SQLException e1) {
			e1.printStackTrace();
			LOG
					.error(
							"Error occurred while rolling back of transmittal process.",
							e1);
		}
	}

	@Override
	public ArrayList<String> getApplicableBas() throws TbitsExceptionClient {

		String inspectionProp = PropertiesHandler
				.getProperty("comma_seperated_list_of_inspection_ba");
		ArrayList<String> ar=new ArrayList<String>();

		if (inspectionProp != null) {

			String[] listOfInspectionBa = inspectionProp.split(",");

			if (listOfInspectionBa != null && listOfInspectionBa.length > 0) {

				for (int i = 0; i < listOfInspectionBa.length; i++) {
					ar.add(listOfInspectionBa[i]);
				}
				
			}
			
		}
		return ar;
	}

}
