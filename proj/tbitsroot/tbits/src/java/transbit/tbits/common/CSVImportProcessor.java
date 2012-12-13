package transbit.tbits.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.scheduler.ICSVImportConfig;
import transbit.tbits.search.Searcher;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.gson.Gson;


public class CSVImportProcessor implements ICSVImportConfig{
	// The Logger that is used to log messages to the application log.
	public static final String DATETIME_FORMAT = "MM/dd/yyyy HH:mm";
	public static final String DATE_FORMAT = "MM/dd/yyyy";
	private static final TBitsLogger LOG   = TBitsLogger.getLogger(TBitsConstants.PKG_SCHEDULER);

	CSVImportConfig importConfig;
	public CSVImportProcessor(CSVImportConfig config){
		importConfig=config;
	}

	public void operate() throws IOException, DatabaseException, SQLException{
        LOG.info("Starting CSV Import");
		Connection aCon=DataSourcePool.getConnection();
		try{
			TBitsResourceManager tr = new TBitsResourceManager();
			aCon.setAutoCommit(true);
			if(importConfig.getSingleTransaction()){
				aCon.setAutoCommit(false);
			}

			InputStreamReader isr=new InputStreamReader(importConfig.getInputFileStream());
			OutputStreamWriter logWriter = new OutputStreamWriter(importConfig.getOutputFileStream());
			String sysPrefix=importConfig.getSysPrefix();
			BusinessArea ba=null;
			if(sysPrefix!=null){
				try {
					ba=BusinessArea.lookupBySystemPrefix(sysPrefix);
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOG.error(e.getMessage());
				}
			}

			HashMap<String,String> columnFieldMap=importConfig.getColumnFieldMap();

			String userLogin=importConfig.getLoggerUserLogin();
			if(userLogin==null){
				userLogin="root";
			}
			User u = null;
			try {
				u=User.lookupAllByUserLogin(importConfig.getLoggerUserLogin());
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOG.error(e.getMessage());
			}
			CSVWriter csvw = new CSVWriter(logWriter);
			CSVReader csvr = new CSVReader(isr);
			List<String> colNames =null;
			List<String> dNames=null;

			String[] temp=null;

			if(importConfig.getHeaderAvailable()){
				int headerRows=importConfig.getNoOfHeaderRows();
				if(headerRows==2){
					temp=csvr.readNext();
					csvw.writeNext(temp);
					colNames=Arrays.asList(temp);
					temp=csvr.readNext();
					csvw.writeNext(temp);
					dNames=Arrays.asList(temp);
				}
				else if(headerRows==1){
					temp=csvr.readNext();
					csvw.writeNext(temp);
					colNames=Arrays.asList(temp);
				}
				else {
					throw new IllegalArgumentException("illegal column headers");
				}

			}else{
				colNames=importConfig.getColumnsOrderList();
				csvw.writeNext(colNames.toArray(new String[0]));
			}

			if((colNames == null) || (colNames.size() == 0))
				throw new IllegalArgumentException("The column headers not found.");

			HashMap<String, String> columnMetaData=importConfig.getColumnMetaDataMap();
			ArrayList<String> columnSkipList=importConfig.getSkipColumnList();

			String[] row = null;

			try {
				Hashtable<Integer, Integer> requestIdMapping = new Hashtable<Integer, Integer>();

				while( (row = csvr.readNext()) != null )
				{
					Hashtable<String, ArrayList<String>> addedFiles = new Hashtable<String, ArrayList<String>>(); 
					Hashtable<String, ArrayList<String>> deletedFiles = new Hashtable<String, ArrayList<String>>(); 

					Hashtable<String, String> params = new Hashtable<String, String>();
					int currentRelativeRequestId = 0;
					for(int i=0; (i < colNames.size()) && (i < row.length); i++)
					{
						String fieldName=null;
						String colName = colNames.get(i);
						if(columnSkipList!=null){
							if(columnSkipList.contains(colName))
								continue;
						}

						if(!importConfig.isColumnEqualsFieldName()&& columnFieldMap!=null){
							fieldName=columnFieldMap.get(colName);
						}else fieldName=colName;
						String value = row[i];
						Field f = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
						if(f == null)
						{
							LOG.warn("Unable to find field for column '" + colName + "'");
							continue;
						}
						if(f.getName()==Field.DESCRIPTION){
							if((value != null) && (value.trim().length() > 0))
							{
								String token=importConfig.getCommaToken();
								if(!(token==null||token.isEmpty())){
									value=value.replace(token,",");
									
								}
							}
						}
						
						if(f.getName() == Field.RELATED_REQUESTS)
						{
							if((value != null) && (value.trim().length() > 0))
							{
								String[] relReqs = value.split(",");
								StringBuilder finalReqStr = new StringBuilder();
								for(String relreq:relReqs)
								{
									relreq = relreq.trim();
									if(relreq.startsWith("#"))
									{
										relreq = relreq.substring(1);
										Integer reqId = requestIdMapping.get(Integer.parseInt(relreq));
										if(reqId == null)
											throw new IllegalArgumentException("Request id could not be found wrt to relative request '" + relreq + "'");
										relreq = reqId + "";
									}
									finalReqStr.append(relreq).append(",");
								}
								if(finalReqStr.length() > 0)
									finalReqStr.deleteCharAt(finalReqStr.length() - 1);
								value = finalReqStr.toString();
							}
						}
						if((f.getDataTypeId() == DataType.DATE) || (f.getDataTypeId() == DataType.DATETIME))
						{
							Date d;

							String dateFormat=null;
							if(columnMetaData!=null){
								dateFormat=columnMetaData.get(colName);
							}
							if(dateFormat==null)
								dateFormat=importConfig.getDateFormat();

							if(dateFormat==null)
								dateFormat = u.getWebConfigObject().getWebDateFormat();

							try {
								SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);//DATETIME_FORMAT);
								d = sdf.parse(value);
								SimpleDateFormat sdf1 = new SimpleDateFormat(TBitsConstants.API_DATE_FORMAT);
								value = sdf1.format(d);
							} catch (ParseException e) {
								e.printStackTrace();
							}
							LOG.debug("After parsing: " + value);
						}
						if(f.getDataTypeId() == DataType.ATTACHMENTS)
						{
							//continue;
							if(value.trim().length() == 0)
								continue;
							String[] values = null;
							try
							{
								if(value.indexOf('"') > -1)
									values = splitCommaSeparatedString(value);
								else
									values = value.split(",");
							}
							catch(Exception e)
							{
								e.printStackTrace();
								LOG.error(e.getMessage());
							}
							if((values == null) || (values.length == 0))
								continue;
							for(String v:values)
							{
								if((v == null) || (v.length() == 0))
									continue;
								if(v.startsWith("D "))
								{
									ArrayList<String> files = deletedFiles.get(f.getName());
									if(files == null)
									{
										files = new ArrayList<String>();
										deletedFiles.put(f.getName(), files);
									}
									files.add(v.substring(2));
								}
								else
								{
									ArrayList<String> files = addedFiles.get(f.getName());
									if(files == null)
									{
										files = new ArrayList<String>();
										addedFiles.put(f.getName(), files);
									}
									files.add(v);
								}
							}
							continue;
						}
						if(f.getDataTypeId() == DataType.INT)
						{
							try
							{
								value = value.trim();
								if(value.startsWith("#"))
								{
									value = value.substring(1);
									int relativeRequestId = Integer.parseInt(value);
									Integer actualRequestId = requestIdMapping.get(relativeRequestId);
									if(fieldName.equals(Field.REQUEST))
									{
										if(actualRequestId == null)
										{
											currentRelativeRequestId = relativeRequestId;
											value = null;
										}
										else
										{
											value = actualRequestId + "";
										}
									}
									else if(fieldName.equals(Field.PARENT_REQUEST_ID))
									{
										if(actualRequestId == null)
											throw new IllegalArgumentException("Relative request id " + relativeRequestId + " is not found");
										else
											value = actualRequestId +"";
									}
									else
										throw new NumberFormatException("Invalid value'" + value + "' for field: " + fieldName + "");
								}
								else
									value = Integer.toString(Integer.parseInt(value));
							}
							catch (Exception e) {
								LOG.error("Unable to parse integer value: '" + value + "' for field: " + fieldName);
								continue;
							}
						}
						if(f.getDataTypeId() == DataType.REAL)
						{
							try
							{
								value = Double.toString(Double.parseDouble(value));
							}
							catch (Exception e) {
								LOG.error("Unable to parse real value: '" + value + "' for field: " + fieldName);
								continue;
							}
						}
						if(value != null)
							params.put(fieldName, value);			
					}

					if(params.size() == 0)
					{
						continue;
					}
					String baStr = params.get(Field.BUSINESS_AREA);
					if((baStr == null) || (baStr.length() == 0))
					{
						params.put(Field.BUSINESS_AREA, ba.getSystemPrefix());
					}

					if(!params.containsKey(Field.USER) || !RoleUser.isSuperUser(u.getUserId()))
					{
						params.put(Field.USER, u.getUserLogin());
					}

					String extraCol = "";
					Request req = null;

					//Upload files
					Hashtable<String, ArrayList<AttachmentInfo>> addedRepoFiles = new Hashtable<String, ArrayList<AttachmentInfo>>();
					for(String field:addedFiles.keySet())
					{
						ArrayList<AttachmentInfo> files = addedRepoFiles.get(field);
						if(files == null)
						{
							files = new ArrayList<AttachmentInfo>();
							addedRepoFiles.put(field, files);
						}
						ArrayList<String> strFiles = addedFiles.get(field);
						for(String s:strFiles)
						{
							Uploader uploader = new Uploader();
							AttachmentInfo attInfo = uploader.copyIntoRepository(new File(s));
							files.add(attInfo);
						}
					}

					for(String s:params.keySet()){
						System.out.println(s+","+params.get(s));
					}
					//String reqIdStr = params.get(Field.REQUEST);
					ArrayList<String> uniqueFieldNames=importConfig.getPrimaryKeys();
					ArrayList<String> uniqueFieldValues=new ArrayList<String>();
					for(String str:uniqueFieldNames){
						String tempstr=params.get(str);
						uniqueFieldValues.add(tempstr);

					}

					String reqIdStr=getRequestIdByPrimaryKey(uniqueFieldNames,uniqueFieldValues,ba,u);
					try
					{
						if((reqIdStr == null)||(reqIdStr.length() == 0))
						{
							//Process Attachments
							for(String field:addedRepoFiles.keySet())
							{
								ArrayList<AttachmentInfo> addRF = addedRepoFiles.get(field);
								if(addRF == null)
									continue;
								for(AttachmentInfo ai:addRF)
								{
									ai.requestFileId = 0;
								}
								params.put(field, AttachmentInfo.toJson(addRF));
							}

							try {
								AddRequest addRequest = new AddRequest();
								addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
								req = addRequest.addRequest(aCon,tr,params);
								extraCol = "Added " + req.getRequestId() + " successfully.";
								if(currentRelativeRequestId != 0)
								{
									requestIdMapping.put(currentRelativeRequestId, req.getRequestId());
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								LOG.error(e.getMessage());
							}
						}
						else
						{
							params.put(Field.REQUEST,reqIdStr);
							Request oldRequest = Request.lookupBySystemIdAndRequestId(
									ba.getSystemId(), Integer.parseInt(reqIdStr));


							ArrayList<AttachmentInfo> finalAttachments  = new ArrayList<AttachmentInfo>();
							for(String field:addedRepoFiles.keySet())
							{
								Collection<AttachmentInfo> newAtts = addedRepoFiles.get(field);
								Collection<AttachmentInfo> oldAtts = new ArrayList<AttachmentInfo>();
								if(field.equals(Field.ATTACHMENTS))
								{
									oldAtts = oldRequest.getAttachments();
								}
								else
								{
									String attString = oldRequest.getExString(field);
									try
									{
										oldAtts = AttachmentInfo.fromJson(attString);
									}
									catch(Exception exp)
									{
										System.out.println("error while parsing: " + attString);
										exp.printStackTrace();
										LOG.error(exp.getMessage());
									}
								}
								finalAttachments.addAll(oldAtts);
								for(AttachmentInfo newAttInfo : newAtts)
								{
									AttachmentInfo foundOld = null;
									for(AttachmentInfo oldAttInfo:oldAtts)
									{
										if(oldAttInfo.name.equals(newAttInfo.name))
										{
											newAttInfo.requestFileId = oldAttInfo.requestFileId;
											foundOld = oldAttInfo;
											break;
										}
									}
									if(foundOld != null)
									{
										finalAttachments.remove(foundOld);
									}
									finalAttachments.add(newAttInfo);
								}
								params.put(field, AttachmentInfo.toJson(finalAttachments));
							}


							Boolean noChangeInParams=shouldUpdateRequest(params, oldRequest);
							if(!noChangeInParams){
								try {
									UpdateRequest updateRequest = new UpdateRequest();
									updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
									req = updateRequest.updateRequest(aCon,tr,params);
									extraCol = "Updated " + req.getRequestId() + " successfully.";
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									LOG.error(e.getMessage());
								}
							}
						}
					}
					catch (APIException e) {
						LOG.error("",(e));
						if (e.toString().contains(TBitsConstants.API_DATE_FORMAT))
							extraCol = "FAILED: Due date format should be: " + u.getWebConfigObject().getWebDateFormat();
						else
							extraCol = "FAILED:" + e.toString();
						e.printStackTrace();
						LOG.error(e.getMessage());
					}
					catch (Exception e) {
						LOG.error("",(e));
						extraCol = "FAILED:" + e.getMessage();
						e.printStackTrace();
					}
					//copy the input to output with one extra column - 
					//that will contain either the request number or the error message
					String[] output = new String[row.length + 1];
					int i = 0;
					for(; i < row.length; i++)
					{
						if((i < colNames.size()) && (colNames.get(i) != null))
						{
							String colName = colNames.get(i);

							if((req != null) && (req.get(colName) != null) )
								output[i] = req.get(colName);
							else
								output[i] = row[i];						
						}
					}
					output[i] = extraCol;
					csvw.writeNext(output);
				}
			} catch (IOException e) {
				csvw.writeNext(new String[]{"Unable to read from the input file. The file may not be in the CSV format."});
				LOG.error(e);
			}
			try {
				csvw.close();
			} catch (IOException e) {
				LOG.error("Unable to close the csv writer.", e);
			}

		} catch (Exception e){
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
		finally{
			aCon.close();
			File logFile=importConfig.getOutputFile();
			User user=User.lookupAllByUserLogin(importConfig.getLoggerUserLogin());
			String from=user.getEmail();
			String to=importConfig.getEmailId();
			String subject="Customized CSV Import Log";
			String fileName=logFile.getName();

			URI logFileURI=logFile.toURI();
			URL logFileURL=logFileURI.toURL();
			try {
				sendContent(from, to, subject,"PFA", fileName,logFileURL);
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//return outputWriter.toString();
	}

	private Boolean shouldUpdateRequest(Hashtable<String, String> newParams,
			Request oldRequest) {
		Boolean allMatched=true;
		for(String s:newParams.keySet()){
			if(s.equals(Field.BUSINESS_AREA)||s.equals(Field.USER)) continue;
			String newValue=newParams.get(s);
			String oldValue=oldRequest.get(s);
			System.out.println(s+"-->"+newValue+"--"+oldValue);
			Boolean atomicMatch=newValue.equals(oldValue);
			allMatched=allMatched&atomicMatch;
		}
		return allMatched;
	}





	// public void importCustomCSVData(CSVImportConfig importConfig,InputStream inputStream, User u, BusinessArea ba, String contextPath, OutputStream os) throws DatabaseException, UnsupportedEncodingException 

	private static String getRequestIdByPrimaryKey(ArrayList<String> uniqueFieldNames,ArrayList<String> uniqueFieldValues,BusinessArea ba,User u) {

		if(uniqueFieldNames.contains("request_id")){
			int index =	uniqueFieldNames.indexOf("request_id");
			return uniqueFieldValues.get(index);
		}

		String CONNECTOR=" AND ";
		StringBuffer dql=new StringBuffer();
		for(int i=0;i<uniqueFieldNames.size();i++){
			String uniqueFieldName=uniqueFieldNames.get(i);
			String uniqueFieldValue=uniqueFieldValues.get(i);
			if(i>0) dql.append(CONNECTOR);
			dql.append(uniqueFieldName+":"+uniqueFieldValue);
		}
		Searcher searcher = new Searcher(ba.getSystemId(),u.getUserId(),dql.toString());
		try {
			searcher.search();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
		ArrayList<String>reqList=searcher.getAllRequestIdList();
		if(reqList.size()>1){
			LOG.info("Duplicate records found in the BA with -->"+dql);
			//Return the first Request Id
		}
		if(reqList!=null && !reqList.isEmpty()){
			String reqIdStr=reqList.get(0);
			String[] tokens = reqIdStr.split("_");
			return tokens[1];
		}
		return null;





	}
	private static String getCSVTemplate(CSVImportConfig importConfig) throws DatabaseException
	{

		BusinessArea ba = BusinessArea.lookupBySystemPrefix(importConfig.getSysPrefix());
		User u = User.lookupAllByUserLogin(importConfig.getLoggerUserLogin());
		ArrayList<Field> fields = Field.getFieldsBySystemIdAndUserId(ba.getSystemId(), u.getUserId());
		Hashtable<String, Integer> myPerms = RolePermission.getPermissionsBySystemIdAndUserId(ba.getSystemId(), u.getUserId());
		StringWriter sw = new StringWriter();
		CSVWriter csvWrite = new CSVWriter(sw);
		ArrayList<String> cols = new ArrayList<String>();
		//ArrayList<String> types = new ArrayList<String>();
		ArrayList<String> displayNames = new ArrayList<String>();
		int i = 0;
		for(Field f:fields)
		{
			if(!f.getIsActive())
				continue;
			String name = f.getName();
			if(name.equals(Field.BUSINESS_AREA))
				continue;
			int perm = myPerms.get(name);
			if(((perm & Permission.ADD) == 0)&&((perm & Permission.CHANGE) == 0))
				continue;

			cols.add(name);
			//String dtName = getDataTypeName(dts, f.getDataTypeId());
			//if(dtName == null)
			//dtName = "";
			//types.add(dtName);
			String format = "";
			if((f.getDataTypeId() == DataType.DATETIME) || (f.getDataTypeId() == DataType.TIME) || (f.getDataTypeId() == DataType.DATE))
			{
				format = "(" + u.getWebConfigObject().getWebDateFormat() + ")";//DATETIME_FORMAT
			}
			else if(f.getDataTypeId() == DataType.BOOLEAN)
			{
				format = "(true/false/yes/no/1/0)";
			}
			else if(f.getDataTypeId() == DataType.TYPE)
			{
				ArrayList<Type> list = Type.lookupAllBySystemIdAndFieldName(f.getSystemId(), f.getName());
				StringBuilder sb = new StringBuilder();
				boolean isFirst = true;
				for(Type t: list)
				{
					if(isFirst)
						isFirst = false;
					else 
						sb.append("/");
					sb.append(t.getName());
				}
				format = "(" + sb.toString() + ")";
			}
			else if(f.getDataTypeId() == DataType.INT)
			{
				format = "(Integer)";
			}
			else if(f.getDataTypeId() == DataType.REAL)
			{
				format = "(Real Number)";
			}
			displayNames.add(f.getDisplayName() + format);
			i++;
		}
		csvWrite.writeNext(cols.toArray(new String[0]));
		csvWrite.writeNext(displayNames.toArray(new String[0]));
		//csvWrite.writeNext(types.toArray(new String[0]));
		return sw.toString();
	}

	private static String getDataTypeName(ArrayList<DataType> dts, int id)
	{
		for(DataType dt:dts)
		{
			if(dt.getDataTypeId() == id)
			{
				return dt.getDataType();
			}
		}
		return null;
	}

	/**
	 * Splits the data keeping in mind that the comma might exist inside quotes or commented.
	 * Example:
	 * "a,", "b", "c"
	 * "a\"", "b", "c" 
	 */
	public static String[] splitCommaSeparatedString(String s) {
		Gson gs = new Gson();
		String [] vals = gs.fromJson("[" + s + "]", String[].class);
		return vals;
	}


	private void sendContent(String from, String to, String subject, String body, String fileName, URL url4Attachment) throws AddressException, MessagingException, MalformedURLException
	{
		//MOST of the code is taken from: http://www.paolocorti.net/public/wordpress/index.php/2007/06/05/deployment-of-birt-reports-by-email/
		MimeMessage message = new MimeMessage(Mail.getSession());
		message.setFrom(new InternetAddress(from));

		String toEmails[] = to.split(",");
		for(int i=0; i<toEmails.length; i++)
		{
			message.addRecipient(Message.RecipientType.TO,
					new InternetAddress(toEmails[i]));
		}

		message.setSubject(subject);

		Multipart multipart = new MimeMultipart();
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(body);
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart(); 
		DataSource source = new URLDataSource(url4Attachment);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileName);

		// Add the second part (attached mimebody)
		multipart.addBodyPart(messageBodyPart);

		// Put parts in message
		message.setContent(multipart);

		// Send message
		Transport.send(message);	
	}

	public static void main(String[] args) {


		HashMap<String,String> params=new HashMap<String,String>();
		params.put(CMD_IMPORT_FILE_PATH,"/home/utkarsh/lntbilltracking/CSV/PO_Details_20110310.csv");
		params.put(CMD_IMPORT_LOG_FILES_DIRECTORY,"/home/utkarsh/logs");
		params.put(CMD_IMPORT_UNIQUE_KEY,"POnumber");
		params.put(CMD_HEADER_AVAILABLE,"false");
		params.put(CMD_HEADER_COLUMN_ORDER,"POnumber,Project,VendorName,POdate,POvalue,RecordUpdationDatetime,Materialdescription");
		params.put(CMD_COLUMN_EQUALS_FIELD_NAME,"true");
		params.put(CMD_NO_OF_HEADER_ROWS,"");
		params.put(CMD_COLUMN_FIELD_MAPPING,"");
		params.put(CMD_COLUMN_METADATA,"POdate:yyyyMMdd,RecordUpdationDatetime:yyyyMMdd");
		params.put(CMD_IMPORT_DATE_FORMAT,DATE_FORMAT);
		params.put(CMD_SINGLE_TRANSACTION,"yes");
		params.put(CMD_BA_PREFIX,"POImport");
		params.put(CMD_EMAIL_ID,"just.utkarsh@gmail.com");
		params.put(CMD_LOGGER_USERLOGIN ,"root");
		params.put(JOB_NAME ,"csvPOImportTest");

		CSVImportConfig config=new CSVImportConfig(params);


		try {
			new CSVImportProcessor(config).operate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		params=new HashMap<String,String>();
		params.put(CMD_IMPORT_FILE_PATH,"/home/utkarsh/lntbilltracking/CSV/PO&GRN_Details_20110311.csv");
		params.put(CMD_IMPORT_LOG_FILES_DIRECTORY,"/home/utkarsh/logs");
		params.put(CMD_IMPORT_UNIQUE_KEY,"POnumber,GRNSESNo");
		params.put(CMD_HEADER_AVAILABLE,"false");
		params.put(CMD_HEADER_COLUMN_ORDER,"POnumber,GRNSESNo,status_id,GRNSEScreateddate,GRNRecordupdationdatetime");
		params.put(CMD_COLUMN_EQUALS_FIELD_NAME,"true");
		params.put(CMD_NO_OF_HEADER_ROWS,"");
		params.put(CMD_COLUMN_FIELD_MAPPING,"");
		params.put(CMD_COLUMN_METADATA,"GRNSEScreateddate:yyyyMMdd,GRNRecordupdationdatetime:yyyyMMdd");
		params.put(CMD_IMPORT_DATE_FORMAT,DATE_FORMAT);
		params.put(CMD_SINGLE_TRANSACTION,"yes");
		params.put(CMD_BA_PREFIX,"GRNPOImport");
		params.put(CMD_EMAIL_ID,"just.utkarsh@gmail.com");
		params.put(CMD_LOGGER_USERLOGIN ,"root");
		params.put(JOB_NAME ,"csvGRNPOImportTest");

		config=new CSVImportConfig(params);


		try {
			new CSVImportProcessor(config).operate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
