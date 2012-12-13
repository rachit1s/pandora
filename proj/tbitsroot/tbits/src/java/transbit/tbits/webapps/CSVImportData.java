package transbit.tbits.webapps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;


//import org.apache.batik.dom.util.HashTable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.CSVImportConfig;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Uploader;
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
import transbit.tbits.exception.TBitsException;
import transbit.tbits.search.Searcher;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.gson.Gson;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;


public class CSVImportData extends HttpServlet {
	// The Logger that is used to log messages to the application log.
	public static final String DATETIME_FORMAT = "MM/dd/yyyy HH:mm";
	public static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final TBitsLogger LOG   = TBitsLogger.getLogger(TBitsConstants.PKG_WEBAPPS);
   
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		aResponse.setCharacterEncoding(TBitsConstants.CHARSET);
    	aRequest.setCharacterEncoding(TBitsConstants.CHARSET);
    	
		HttpSession aSession = aRequest.getSession(true);
		try {
			User user = WebUtil.validateUser(aRequest);
			String baPrefix = aRequest.getParameter("ba");
			String contentDisposition = "attachment;fileName=\"template.csv\"";
			aResponse.setHeader("Content-Disposition", contentDisposition);
			aResponse.setContentType("text/csv");
			ServletOutputStream out = aResponse.getOutputStream();

			String templateString = getCSVTemplate(baPrefix, user);
			String enc = "UTF8";
			out.write(templateString.getBytes(enc));
			out.flush();
			out.close();
		} catch (DatabaseException de) {
			LOG.error(de);
			aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
        } catch (TBitsException de) {
        	LOG.error(de);
        	aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
        }
	}
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		//Utilities.registerMDCParams(aRequest);
		HttpSession aSession = aRequest.getSession(true);
		try {
			aResponse.setCharacterEncoding(TBitsConstants.CHARSET);
	    	aRequest.setCharacterEncoding(TBitsConstants.CHARSET);
	    	
			handlePostRequest(aRequest, aResponse);
		} catch (DatabaseException de) {
			aSession.setAttribute("ExceptionObject", de);
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
			LOG.error(de);
		} catch (TBitsException de) {
			aSession.setAttribute("ExceptionObject", de);
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
			LOG.error(de);
		} catch (ParseException e) {
			aSession.setAttribute("ExceptionObject", e);
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
			e.printStackTrace();
		} finally {
			//Utilities.clearMDCParams();
		}
	}
	public void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException, ParseException 
	{
		User user = WebUtil.validateUser(aRequest);
		
		ServletOutputStream out = aResponse.getOutputStream();
		
		MultipartParser parser = null;
		try {
            parser = new MultipartParser(aRequest, 1024 * 1024 * 1024);    // 1GB
        } catch (IOException e) {
            LOG.severe("",(e));
            out.println("Unable to read the files.");
            out.close();
            return;
        }
        Hashtable<String, String> paramTable = new Hashtable<String, String>();
        if (parser != null) {
            Part part = null;
            String output = null;
            FilePart fPart = null;
            // Iterate the parts in the parser and process them accordingly
            while ((part = parser.readNextPart()) != null) {
            	 if (part instanceof ParamPart) {
                     ParamPart pp         = (ParamPart) part;
                     String    paramName  = pp.getName();
                     String    paramValue = pp.getStringValue();
                     paramTable.put(paramName, paramValue);
                 }
                if (part instanceof FilePart) {
                	fPart = (FilePart)part;
                	String baPrefix = paramTable.get("ba");
					if(baPrefix == null)
					{
						out.println("BA not specified.");
						return;
					}
					//else
					//	out.println("Ba: " + baPrefix);
					BusinessArea ba = BusinessArea.lookupBySystemPrefix(baPrefix);
					
					String contentDisposition = "attachment;fileName=\"import-results.csv\"";
	    			aResponse.setHeader("Content-Disposition", contentDisposition);
	    			aResponse.setContentType("text/csv");
	    			
					importCustomCSVData(new CSVImportConfig(),fPart.getInputStream(), user, ba, aRequest.getContextPath(), aResponse.getOutputStream());
					aResponse.flushBuffer();
                }
            }
        }
	}
	
	
	public static void importCustomCSVData(CSVImportConfig importConfig,InputStream inputStream, User u, BusinessArea ba, String contextPath, OutputStream os) throws DatabaseException, UnsupportedEncodingException {
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(inputStream, TBitsConstants.CHARSET);
		} catch (UnsupportedEncodingException e3) {
			throw e3;
		}
		OutputStreamWriter outputWriter = new OutputStreamWriter(os, TBitsConstants.CHARSET); 
		//StreamW outputWriter = new StringWriter();
		CSVWriter csvw = new CSVWriter(outputWriter);
		CSVReader csvr = new CSVReader(isr);
		
		String[] colNames =null;
		//colNames=Arrays.toString(importConfig.getColumnsOrderList());
		csvw.writeNext(colNames);
		
		if((colNames == null) || (colNames.length == 0))
			throw new IllegalArgumentException("The column headers not found.");
		
		String[] row = null;
		
		boolean allowAll = false;
		try {
			Hashtable<Integer, Integer> requestIdMapping = new Hashtable<Integer, Integer>();
			
			while( (row = csvr.readNext()) != null )
			{
				Hashtable<String, ArrayList<String>> addedFiles = new Hashtable<String, ArrayList<String>>(); 
				Hashtable<String, ArrayList<String>> deletedFiles = new Hashtable<String, ArrayList<String>>(); 

				Hashtable<String, String> params = new Hashtable<String, String>();
				int currentRelativeRequestId = 0;
				for(int i=0; (i < colNames.length) && (i < row.length); i++)
				{
					String colName = colNames[i];
					String value = row[i];
					Field f = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), colName);
					if(f == null)
					{
						LOG.warn("Unable to find field for column '" + colName + "'");
						continue;
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
						String webDateFormat = u.getWebConfigObject().getWebDateFormat();
						try {
							SimpleDateFormat sdf = new SimpleDateFormat(webDateFormat);//DATETIME_FORMAT);
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
								if(colName.equals(Field.REQUEST))
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
								else if(colName.equals(Field.PARENT_REQUEST_ID))
								{
									if(actualRequestId == null)
										throw new IllegalArgumentException("Relative request id " + relativeRequestId + " is not found");
									else
										value = actualRequestId +"";
								}
								else
									throw new NumberFormatException("Invalid value'" + value + "' for field: " + colName + "");
							}
							else
								value = Integer.toString(Integer.parseInt(value));
						}
						catch (Exception e) {
							LOG.error("Unable to parse integer value: '" + value + "' for field: " + colName);
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
							LOG.error("Unable to parse real value: '" + value + "' for field: " + colName);
							continue;
						}
					}
					if(value != null)
						params.put(colName, value);			
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
				
				//String reqIdStr = params.get(Field.REQUEST);
				String uniqueFieldName=null;
				//	importConfig.getUniqueColumn();
				String uniqueFieldValue=params.get(uniqueFieldName);
				String reqIdStr=getRequestIdByUniqueFieldValue(uniqueFieldName,uniqueFieldValue,ba,u);
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
						
						AddRequest addRequest = new AddRequest();
						addRequest.setSource(TBitsConstants.SOURCE_WEB);
						if(contextPath != null)
						addRequest.setContext(contextPath);
						req = addRequest.addRequest(params);
						extraCol = "Added " + req.getRequestId() + " successfully.";
						if(currentRelativeRequestId != 0)
						{
							requestIdMapping.put(currentRelativeRequestId, req.getRequestId());
						}
					}
					else
					{
						
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
						
						UpdateRequest updateRequest = new UpdateRequest();
						updateRequest.setSource(TBitsConstants.SOURCE_WEB);
						if(contextPath != null)
							updateRequest.setContext(contextPath);
						req = updateRequest.updateRequest(params);
						extraCol = "Updated " + req.getRequestId() + " successfully.";
						
					}
				}
				catch (APIException e) {
					LOG.error("",(e));
					if (e.toString().contains(TBitsConstants.API_DATE_FORMAT))
						extraCol = "FAILED: Due date format should be: " + u.getWebConfigObject().getWebDateFormat();
					else
						extraCol = "FAILED:" + e.toString();
					e.printStackTrace();
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
					if((i < colNames.length) && (colNames[i] != null))
					{
						String colName = colNames[i];
						
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
		
		//return outputWriter.toString();
	}

	private static String getRequestIdByUniqueFieldValue(String uniqueFieldName,String uniqueFieldValue,BusinessArea ba,User u) {
        
		String dql=uniqueFieldName+":"+uniqueFieldValue;
		Searcher searcher = new Searcher(ba.getSystemId(),u.getUserId(),dql);
		try {
			searcher.search();
		} catch (Exception e) {
			e.printStackTrace();
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
	private static String getCSVTemplate(String baPrefix, User u) throws DatabaseException
	{
		BusinessArea ba = BusinessArea.lookupBySystemPrefix(baPrefix);
		//User u = User.lookupAllByUserLogin(login);
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

	
}
