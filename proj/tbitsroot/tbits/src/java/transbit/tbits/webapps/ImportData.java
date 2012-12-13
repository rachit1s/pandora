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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.json.JSONArray;
import org.json.JSONException;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BAUser;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.gson.Gson;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

/**
 * @author rohit
 * 
 * 
 * 
 */
public class ImportData extends HttpServlet {
	// The Logger that is used to log messages to the application log.
	public static final String DATETIME_FORMAT = "MM/dd/yyyy HH:mm";
	public static final String DATE_FORMAT = "MM/dd/yyyy";
	private static final TBitsLogger LOG = TBitsLogger
			.getLogger(TBitsConstants.PKG_WEBAPPS);

	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("HelloWorldSwing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add the ubiquitous "Hello World" label.
		JLabel label = new JLabel("Hello World");
		frame.getContentPane().add(label);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// testTemplate();
		for (int i = 0; i < 50; i++) {
			System.out.println("LOOP " + i);
			testImport(
					"/Users/sandeepgiri/dls/excelimporttemplateofsepcoba/DBR_1_to_3.csv",
					"SEPCO");
			testImport(
					"/Users/sandeepgiri/dls/excelimporttemplateofsepcoba/DBR_4_to_11.csv",
					"SEPCO");
			testImport(
					"/Users/sandeepgiri/dls/excelimporttemplateofsepcoba/DBR_12_to_16.csv",
					"SEPCO");
			testImport(
					"/Users/sandeepgiri/dls/excelimporttemplateofsepcoba/DBR_17_to_19.csv",
					"SEPCO");
			testImport(
					"/Users/sandeepgiri/dls/excelimporttemplateofsepcoba/DBR_20.csv",
					"SEPCO");

		}
		System.out.println("Finished.!!");
		// testImport("/Users/sandeepgiri/Desktop/for-update.csv", "IL");
	}

	private static void testTemplate() {
		try {
			System.out.println(getCSVTemplate("tbits", User
					.lookupAllByUserLogin("root")));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void testImport(String file, String sysPrefix) {
		try {
			System.out.println("FILES: " + file);
			importCSVData(new FileInputStream(new File(file)), User
					.lookupAllByUserLogin("root"), BusinessArea
					.lookupBySystemPrefix(sysPrefix), null, System.out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
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

	public void doPost(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, IOException {
		// Utilities.registerMDCParams(aRequest);
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
			// Utilities.clearMDCParams();
		}
	}

	public void handlePostRequest(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException,
			IOException, DatabaseException, TBitsException, ParseException {
		User user = WebUtil.validateUser(aRequest);

		ServletOutputStream out = aResponse.getOutputStream();

		MultipartParser parser = null;
		try {
			parser = new MultipartParser(aRequest, 1024 * 1024 * 1024); // 1GB
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
					ParamPart pp = (ParamPart) part;
					String paramName = pp.getName();
					String paramValue = pp.getStringValue();
					paramTable.put(paramName, paramValue);
				}
				if (part instanceof FilePart) {
					fPart = (FilePart) part;
					String baPrefix = paramTable.get("ba");
					if (baPrefix == null) {
						out.println("BA not specified.");
						return;
					}
					// else
					// out.println("Ba: " + baPrefix);

					String flag = paramTable.get("flag");

					BusinessArea ba = BusinessArea
							.lookupBySystemPrefix(baPrefix);
					// Boolean flag = false;
					if (flag == null || !flag.equals("true")) {
						String contentDisposition = "attachment;fileName=\"import-results.csv\"";
						aResponse.setHeader("Content-Disposition",
								contentDisposition);
						aResponse.setContentType("text/csv");
						importCSVData(fPart.getInputStream(), user, ba,
								aRequest.getContextPath(), aResponse
										.getOutputStream());

					} else {

						StringBuilder error = new StringBuilder();
						StringBuilder jsonString = new StringBuilder();
						boolean canContinue = checkForErrors(fPart
								.getInputStream(), user, ba, aRequest
								.getContextPath(), error, jsonString);

						fPart.getInputStream().close();

						if (canContinue) {

							out.write(jsonString.toString().getBytes());

						} else {

							String contentDisposition = "attachment;fileName=\"error-logs.csv\"";
							aResponse.setHeader("Content-Disposition",
									contentDisposition);
							aResponse.setContentType("text/csv");

							OutputStreamWriter outputWriter = new OutputStreamWriter(
									aResponse.getOutputStream(),
									TBitsConstants.CHARSET);

							CSVWriter csvw = new CSVWriter(outputWriter);

							String[] arr = new String[1];
							arr[0] = error.toString();
							csvw.writeNext(arr);

							try {
								csvw.close();
							} catch (IOException e) {
								LOG.error("Unable to close the csv writer.", e);
							}

						}
						out.flush();
						out.close();

					}
					aResponse.flushBuffer();
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private static void importCSVData(InputStream inputStream, User u,
			BusinessArea ba, String contextPath, OutputStream os)
			throws DatabaseException, UnsupportedEncodingException {
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(inputStream, TBitsConstants.CHARSET);
		} catch (UnsupportedEncodingException e3) {
			throw e3;
		}
		OutputStreamWriter outputWriter = new OutputStreamWriter(os,
				TBitsConstants.CHARSET);
		// StreamW outputWriter = new StringWriter();
		CSVWriter csvw = new CSVWriter(outputWriter);
		CSVReader csvr = new CSVReader(isr);
		String[] colNames = null;
		try {
			colNames = csvr.readNext();
			csvw.writeNext(colNames);
		} catch (IOException e2) {
			String s = "Unable to read the column names";
			csvw.writeNext(new String[] { s });
			LOG.error(s, e2);
		}
		if ((colNames == null) || (colNames.length == 0))
			throw new IllegalArgumentException("The column headers not found.");

		// jump the datatype and display name rows

		HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();

		String[] dName = null;
		try {
			dName = csvr.readNext();

			csvw.writeNext(dName);
		} catch (IOException e1) {
			String s = "Unable to read the display names";
			csvw.writeNext(new String[] { s });
			LOG.error(s, e1);

		}
		if (dName == null)
			throw new IllegalArgumentException("Display Name row is not found.");
		String[] row = null;

		boolean allowAll = false;
		try {
			Hashtable<Integer, Integer> requestIdMapping = new Hashtable<Integer, Integer>();

			while ((row = csvr.readNext()) != null) {
				Hashtable<String, ArrayList<String>> addedFiles = new Hashtable<String, ArrayList<String>>();
				Hashtable<String, ArrayList<String>> deletedFiles = new Hashtable<String, ArrayList<String>>();

				Hashtable<String, String> params = new Hashtable<String, String>();
				int currentRelativeRequestId = 0;
				for (int i = 0; (i < colNames.length) && (i < row.length); i++) {

					String colName = colNames[i];
					String value = row[i];
					String allowedValues = dName[i];

					Field f = Field.lookupBySystemIdAndFieldName(ba
							.getSystemId(), colName);
					if (f == null) {
						LOG.warn("Unable to find field for column '" + colName
								+ "'");
						continue;
					}
					if (f.getName() == Field.RELATED_REQUESTS) {
						if ((value != null) && (value.trim().length() > 0)) {
							String[] relReqs = value.split(",");
							StringBuilder finalReqStr = new StringBuilder();
							for (String relreq : relReqs) {
								relreq = relreq.trim();
								if (relreq.startsWith("#")) {
									relreq = relreq.substring(1);
									Integer reqId = requestIdMapping
											.get(Integer.parseInt(relreq));
									if (reqId == null)
										throw new IllegalArgumentException(
												"Request id could not be found wrt to relative request '"
														+ relreq + "'");
									relreq = reqId + "";
								}
								finalReqStr.append(relreq).append(",");
							}
							if (finalReqStr.length() > 0)
								finalReqStr
										.deleteCharAt(finalReqStr.length() - 1);
							value = finalReqStr.toString();
						}
					}
					if ((f.getDataTypeId() == DataType.DATE)
							|| (f.getDataTypeId() == DataType.DATETIME)) {
						Date d;
						String webDateFormat = u.getWebConfigObject()
								.getWebDateFormat();
						try {
							SimpleDateFormat sdf = new SimpleDateFormat(
									webDateFormat);// DATETIME_FORMAT);

							d = sdf.parse(value);
							SimpleDateFormat sdf1 = new SimpleDateFormat(
									TBitsConstants.API_DATE_FORMAT);
							value = sdf1.format(d);
						} catch (ParseException e) {
							e.printStackTrace();
							value = "";
						}
						LOG.debug("After parsing: " + value);
					}
					if (f.getDataTypeId() == DataType.ATTACHMENTS) {
						// continue;
						if (value.trim().length() == 0)
							continue;
						String[] values = null;
						try {
							if (value.indexOf('"') > -1)
								values = splitCommaSeparatedString(value);
							else
								values = value.split(",");
						} catch (Exception e) {
							e.printStackTrace();
						}
						if ((values == null) || (values.length == 0))
							continue;
						for (String v : values) {
							if ((v == null) || (v.length() == 0))
								continue;
							if (v.startsWith("D ")) {
								ArrayList<String> files = deletedFiles.get(f
										.getName());
								if (files == null) {
									files = new ArrayList<String>();
									deletedFiles.put(f.getName(), files);
								}
								files.add(v.substring(2));
							} else {
								ArrayList<String> files = addedFiles.get(f
										.getName());
								if (files == null) {
									files = new ArrayList<String>();
									addedFiles.put(f.getName(), files);
								}
								files.add(v);
							}
						}
						continue;
					}
					if (f.getDataTypeId() == DataType.INT) {
						try {
							value = value.trim();
							if (value.startsWith("#")) {
								value = value.substring(1);
								int relativeRequestId = Integer.parseInt(value);
								Integer actualRequestId = requestIdMapping
										.get(relativeRequestId);
								if (colName.equals(Field.REQUEST)) {
									if (actualRequestId == null) {
										currentRelativeRequestId = relativeRequestId;
										value = null;
									} else {
										value = actualRequestId + "";
									}
								} else if (colName
										.equals(Field.PARENT_REQUEST_ID)) {
									if (actualRequestId == null)
										throw new IllegalArgumentException(
												"Relative request id "
														+ relativeRequestId
														+ " is not found");
									else
										value = actualRequestId + "";
								} else
									throw new NumberFormatException(
											"Invalid value'" + value
													+ "' for field: " + colName
													+ "");
							} else
								value = Integer.toString(Integer
										.parseInt(value));
						} catch (Exception e) {
							LOG.error("Unable to parse integer value: '"
									+ value + "' for field: " + colName);
							continue;
						}
					}
					if (f.getDataTypeId() == DataType.REAL) {
						try {
							value = Double.toString(Double.parseDouble(value));
						} catch (Exception e) {
							LOG.error("Unable to parse real value: '" + value
									+ "' for field: " + colName);
							continue;
						}
					}
					if (f.getDataTypeId() == DataType.TYPE) {

						if (!value.equals("")) {
							int indexOfBraces = allowedValues.indexOf('(');
							if (indexOfBraces < 0) {
								throw new IllegalArgumentException(
										"The DropDown values are not found");
							}
							String ddownlist = allowedValues.substring(
									indexOfBraces + 1,
									allowedValues.length() - 1);

							String[] list = ddownlist.split("/");

							Boolean flag = false;

							for (String eachValue : list) {
								if (eachValue.equals(value)) {
									flag = true;
									break;
								}
							}
							if (!flag) {
								System.out
										.println("The Value entered for"
												+ f.getName()
												+ "dropdown field doesnot match the expected values");
								LOG
										.error("The Value entered for"
												+ f.getName()
												+ "dropdown field doesnot match the expected values");
								throw new IllegalArgumentException(
										"The Value entered for"
												+ f.getName()
												+ "dropdown field doesnot match the expected values");
							}
						}
					}
					if (value != null)
						params.put(colName, value);
				}
				/*
				 * end of for loop
				 */
				if (params.size() == 0) {
					continue;
				}
				String baStr = params.get(Field.BUSINESS_AREA);
				if ((baStr == null) || (baStr.length() == 0)) {
					params.put(Field.BUSINESS_AREA, ba.getSystemPrefix());
				}

				if (!params.containsKey(Field.USER)
						|| !RoleUser.isSuperUser(u.getUserId())) {
					params.put(Field.USER, u.getUserLogin());
				}

				String extraCol = "";
				Request req = null;

				// Upload files
				Hashtable<String, ArrayList<AttachmentInfo>> addedRepoFiles = new Hashtable<String, ArrayList<AttachmentInfo>>();
				for (String field : addedFiles.keySet()) {
					ArrayList<AttachmentInfo> files = addedRepoFiles.get(field);
					if (files == null) {
						files = new ArrayList<AttachmentInfo>();
						addedRepoFiles.put(field, files);
					}
					ArrayList<String> strFiles = addedFiles.get(field);
					for (String s : strFiles) {
						Uploader uploader = new Uploader();
						AttachmentInfo attInfo = uploader
								.copyIntoRepository(new File(s));
						files.add(attInfo);
					}
				}
				String reqIdStr = params.get(Field.REQUEST);
				try {
					if ((reqIdStr == null) || (reqIdStr.length() == 0)) {
						// Process Attachments
						for (String field : addedRepoFiles.keySet()) {
							ArrayList<AttachmentInfo> addRF = addedRepoFiles
									.get(field);
							if (addRF == null)
								continue;
							for (AttachmentInfo ai : addRF) {
								ai.requestFileId = 0;
							}
							params.put(field, AttachmentInfo.toJson(addRF));
						}

						AddRequest addRequest = new AddRequest();
						addRequest.setSource(TBitsConstants.SOURCE_WEB);
						if (contextPath != null)
							addRequest.setContext(contextPath);
						req = addRequest.addRequest(params);
						extraCol = "Added " + req.getRequestId()
								+ " successfully.";
						if (currentRelativeRequestId != 0) {
							requestIdMapping.put(currentRelativeRequestId, req
									.getRequestId());
						}
					} else {

						Request oldRequest = Request
								.lookupBySystemIdAndRequestId(ba.getSystemId(),
										Integer.parseInt(reqIdStr));

						ArrayList<AttachmentInfo> finalAttachments = new ArrayList<AttachmentInfo>();
						for (String field : addedRepoFiles.keySet()) {
							Collection<AttachmentInfo> newAtts = addedRepoFiles
									.get(field);
							Collection<AttachmentInfo> oldAtts = new ArrayList<AttachmentInfo>();
							if (field.equals(Field.ATTACHMENTS)) {
								oldAtts = oldRequest.getAttachments();
							} else {
								String attString = oldRequest
										.getExString(field);
								try {
									oldAtts = AttachmentInfo
											.fromJson(attString);
								} catch (Exception exp) {
									System.out.println("error while parsing: "
											+ attString);
									exp.printStackTrace();
								}
							}
							finalAttachments.addAll(oldAtts);
							for (AttachmentInfo newAttInfo : newAtts) {
								AttachmentInfo foundOld = null;
								for (AttachmentInfo oldAttInfo : oldAtts) {
									if (oldAttInfo.name.equals(newAttInfo.name)) {
										newAttInfo.requestFileId = oldAttInfo.requestFileId;
										foundOld = oldAttInfo;
										break;
									}
								}
								if (foundOld != null) {
									finalAttachments.remove(foundOld);
								}
								finalAttachments.add(newAttInfo);
							}
							params.put(field, AttachmentInfo
									.toJson(finalAttachments));
						}

						UpdateRequest updateRequest = new UpdateRequest();
						updateRequest.setSource(TBitsConstants.SOURCE_WEB);
						if (contextPath != null)
							updateRequest.setContext(contextPath);
						req = updateRequest.updateRequest(params);
						extraCol = "Updated " + req.getRequestId()
								+ " successfully.";

					}
				} catch (APIException e) {
					LOG.error("",(e));
					if (e.toString().contains(TBitsConstants.API_DATE_FORMAT))
						extraCol = "FAILED: Due date format should be: "
								+ u.getWebConfigObject().getWebDateFormat();
					else
						extraCol = "FAILED:" + e.toString();
					e.printStackTrace();
				} catch (Exception e) {
					LOG.error("",(e));
					extraCol = "FAILED:" + e.getMessage();
					e.printStackTrace();
				}
				// copy the input to output with one extra column -
				// that will contain either the request number or the error
				// message
				String[] output = new String[row.length + 1];
				int i = 0;
				for (; i < row.length; i++) {
					if ((i < colNames.length) && (colNames[i] != null)) {
						String colName = colNames[i];

						if ((req != null) && (req.get(colName) != null))
							output[i] = req.get(colName);
						else
							output[i] = row[i];
					}
				}
				output[i] = extraCol;
				csvw.writeNext(output);
			}
		} catch (IOException e) {
			csvw
					.writeNext(new String[] { "Unable to read from the input file. The file may not be in the CSV format." });
			LOG.error(e);
		}
		try {
			csvw.close();
		} catch (IOException e) {
			LOG.error("Unable to close the csv writer.", e);
		}

		// return outputWriter.toString();
	}

	private static String getCSVTemplate(String baPrefix, User u)
			throws DatabaseException {
		BusinessArea ba = BusinessArea.lookupBySystemPrefix(baPrefix);
		// User u = User.lookupAllByUserLogin(login);
		Hashtable<String, Integer> myPerms = new Hashtable<String, Integer>();
		ArrayList<Field> fields = new ArrayList<Field>();
		myPerms = RolePermission.getPermissionsBySystemIdAndUserId(
				ba.getSystemId(), u.getUserId());
		fields = Field.getFieldsBySystemIdAndUserId(ba.getSystemId(),
				u.getUserId());

		List<User> mailList = MailListUser.getMailListsByDirectMembership(u
				.getUserId());
		if (mailList!=null ) {
			for (User mailListUser : mailList) {
				if( BAUser.isBAUser(ba.getSystemId(),mailListUser.getUserId()))
				{
				myPerms = RolePermission.getPermissionsBySystemIdAndUserId(
						ba.getSystemId(), mailListUser.getUserId());
				fields = Field.getFieldsBySystemIdAndUserId(ba.getSystemId(),
						mailListUser.getUserId());
				}
			}
		}
		StringWriter sw = new StringWriter();
		CSVWriter csvWrite = new CSVWriter(sw);
		ArrayList<String> cols = new ArrayList<String>();
		// ArrayList<String> types = new ArrayList<String>();
		ArrayList<String> displayNames = new ArrayList<String>();
		int i = 0;
		for (Field f : fields) {
			if (!f.getIsActive())
				continue;
			String name = f.getName();
			if (name.equals(Field.BUSINESS_AREA))
				continue;
			int perm = myPerms.get(name);
			if (((perm & Permission.ADD) == 0)
					&& ((perm & Permission.CHANGE) == 0))
				continue;

			cols.add(name);
			// String dtName = getDataTypeName(dts, f.getDataTypeId());
			// if(dtName == null)
			// dtName = "";
			// types.add(dtName);
			String format = "";
			if ((f.getDataTypeId() == DataType.DATETIME)
					|| (f.getDataTypeId() == DataType.TIME)
					|| (f.getDataTypeId() == DataType.DATE)) {
				format = "(" + u.getWebConfigObject().getWebDateFormat() + ")";// DATETIME_FORMAT
			} else if (f.getDataTypeId() == DataType.BOOLEAN) {
				format = "(true/false/yes/no/1/0)";
			} else if (f.getDataTypeId() == DataType.TYPE) {
				ArrayList<Type> list = Type.lookupAllBySystemIdAndFieldName(f
						.getSystemId(), f.getName());
				StringBuilder sb = new StringBuilder();
				boolean isFirst = true;
				for (Type t : list) {
					if (isFirst)
						isFirst = false;
					else
						sb.append("/");
					sb.append(t.getName());
				}
				format = "(" + sb.toString() + ")";
			} else if (f.getDataTypeId() == DataType.INT) {
				format = "(Integer)";
			} else if (f.getDataTypeId() == DataType.REAL) {
				format = "(Real Number)";
			}
			displayNames.add(f.getDisplayName() + format);
			i++;
		}
		csvWrite.writeNext(cols.toArray(new String[0]));
		csvWrite.writeNext(displayNames.toArray(new String[0]));
		// csvWrite.writeNext(types.toArray(new String[0]));
		return sw.toString();
	}

	private static String getDataTypeName(ArrayList<DataType> dts, int id) {
		for (DataType dt : dts) {
			if (dt.getDataTypeId() == id) {
				return dt.getDataType();
			}
		}
		return null;
	}

	/**
	 * Splits the data keeping in mind that the comma might exist inside quotes
	 * or commented. Example: "a,", "b", "c" "a\"", "b", "c"
	 */
	public static String[] splitCommaSeparatedString(String s) {
		Gson gs = new Gson();
		String[] vals = gs.fromJson("[" + s + "]", String[].class);
		return vals;
	}

	private static JSONArray getJsonString(
			ArrayList<HashMap<String, Object>> arr) throws JSONException {

		JSONArray tableJson = new JSONArray();

		int MasterCount = 0;

		for (HashMap<String, Object> model : arr) {

			String reqid = (String) model.get("requestID");

			int count = 1;
			JSONArray tempJsonArrray = new JSONArray();

			tempJsonArrray.put(0, reqid);
			JSONArray tempJsonArrray1 = null;
			for (String keyOfHashMap : model.keySet()) {

				if (model.get(keyOfHashMap) instanceof ArrayList<?>) {
					ArrayList<?> value = (ArrayList<?>) model.get(keyOfHashMap);
					tempJsonArrray1 = new JSONArray();
					tempJsonArrray1.put(0, keyOfHashMap);
					JSONArray attArray = new JSONArray();
					JSONArray attArray1 = null;

					int catt = 0;

					for (Object val : value) {

						attArray1 = new JSONArray();

						if (val instanceof AttachmentInfo) {
							AttachmentInfo temp = (AttachmentInfo) val;
							String name = temp.getName();
							String repo = ((Integer) temp.getRepoFileId())
									.toString();

							attArray1.put(0, name);
							attArray1.put(1, repo);
							attArray1.put(2, ((Integer) temp.getSize())
									.toString());
							attArray1.put(3,
									((Integer) temp.getRequestFileId())
											.toString());
							attArray1.put(4, reqid);
							attArray.put(catt++, attArray1);
						}
					}
					tempJsonArrray1.put(1, attArray);

					tempJsonArrray.put(count++, tempJsonArrray1);

				}

				else {
					String value = (String) model.get(keyOfHashMap);
					tempJsonArrray1 = new JSONArray();
					tempJsonArrray1.put(0, keyOfHashMap);
					tempJsonArrray1.put(1, value);
					tempJsonArrray.put(count++, tempJsonArrray1);

				}
			}
			tableJson.put(MasterCount++, tempJsonArrray);

		}
		return tableJson;
	}

	private static String importCSVDataInJson(InputStream inputStream, User u,
			BusinessArea ba, String contextPath) throws DatabaseException,
			UnsupportedEncodingException {
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(inputStream, TBitsConstants.CHARSET);
		} catch (UnsupportedEncodingException e3) {
			throw e3;
		}
		// OutputStreamWriter outputWriter = new
		// OutputStreamWriter(os,TBitsConstants.CHARSET);
		// StreamW outputWriter = new StringWriter();
		// CSVWriter csvw = new CSVWriter(outputWriter);
		CSVReader csvr = new CSVReader(isr);
		String[] colNames = null;
		try {
			colNames = csvr.readNext();
			// csvw.writeNext(colNames);
		} catch (IOException e2) {
			String s = "Unable to read the column names";
			// csvw.writeNext(new String[] { s });
			LOG.error(s, e2);
		}
		if ((colNames == null) || (colNames.length == 0))
			throw new IllegalArgumentException("The column headers not found.");

		// jump the datatype and display name rows

		ArrayList<HashMap<String, Object>> arr = new ArrayList<HashMap<String, Object>>();

		String[] dName = null;
		try {
			dName = csvr.readNext();

			// Request(Integer),"Agency(null/SEPCOPaharpur/KMPCL/SEPCO/Others)"
			// ,"Status(Open/Closed/Suspended/Reopen/Information)","Priority(medium/critical/low)","Meeting

			// csvw.writeNext(dName);
		} catch (IOException e1) {
			String s = "Unable to read the display names";
			// csvw.writeNext(new String[] { s });
			LOG.error(s, e1);

		}
		if (dName == null)
			throw new IllegalArgumentException("Display Name row is not found.");
		String[] row = null;

		boolean allowAll = false;
		try {
			Hashtable<Integer, Integer> requestIdMapping = new Hashtable<Integer, Integer>();

			while ((row = csvr.readNext()) != null) {
				Hashtable<String, ArrayList<String>> addedFiles = new Hashtable<String, ArrayList<String>>();
				Hashtable<String, ArrayList<String>> deletedFiles = new Hashtable<String, ArrayList<String>>();

				HashMap<String, Object> params = new HashMap<String, Object>();
				int currentRelativeRequestId = 0;
				for (int i = 0; (i < colNames.length) && (i < row.length); i++) {

					String colName = colNames[i];
					String value = row[i];
					String allowedValues = dName[i];

					Field f = Field.lookupBySystemIdAndFieldName(ba
							.getSystemId(), colName);
					if (f == null) {
						LOG.warn("Unable to find field for column '" + colName
								+ "'");
						continue;
					}
					if (f.getName() == Field.RELATED_REQUESTS) {
						if ((value != null) && (value.trim().length() > 0)) {
							String[] relReqs = value.split(",");
							StringBuilder finalReqStr = new StringBuilder();
							for (String relreq : relReqs) {
								relreq = relreq.trim();
								if (relreq.startsWith("#")) {
									relreq = relreq.substring(1);
									Integer reqId = requestIdMapping
											.get(Integer.parseInt(relreq));
									if (reqId == null)
										throw new IllegalArgumentException(
												"Request id could not be found wrt to relative request '"
														+ relreq + "'");
									relreq = reqId + "";
								}
								finalReqStr.append(relreq).append(",");
							}
							if (finalReqStr.length() > 0)
								finalReqStr
										.deleteCharAt(finalReqStr.length() - 1);
							value = finalReqStr.toString();
						}
					}
					if ((f.getDataTypeId() == DataType.DATE)
							|| (f.getDataTypeId() == DataType.DATETIME)) {
						Date d;
						String webDateFormat = u.getWebConfigObject()
								.getWebDateFormat();
						try {
							SimpleDateFormat sdf = new SimpleDateFormat(
									webDateFormat);// DATETIME_FORMAT);

							if (!value.equals("")) {
								d = sdf.parse(value);
								SimpleDateFormat sdf1 = new SimpleDateFormat(
										"MM/dd/yyyy");
								value = sdf1.format(d);
							}

						} catch (ParseException e) {
							e.printStackTrace();
							/*
							 * Date curr = new Date(); SimpleDateFormat sdf =new
							 * SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
							 * String currDataTime= curr.toString(); d =
							 * sdf.parse(currDataTime); SimpleDateFormat sdf1 =
							 * new SimpleDateFormat("MM/dd/yyyy"); value =
							 * sdf1.format(d);
							 */
							LOG.debug("After parsing: " + value);
						}

					}
					if (f.getDataTypeId() == DataType.ATTACHMENTS) {
						// continue;
						if (value.trim().length() == 0)
							continue;
						String[] values = null;
						try {
							if (value.indexOf('"') > -1)
								values = splitCommaSeparatedString(value);
							else
								values = value.split(",");
						} catch (Exception e) {
							e.printStackTrace();
						}
						if ((values == null) || (values.length == 0))
							continue;
						for (String v : values) {
							if ((v == null) || (v.length() == 0))
								continue;
							if (v.startsWith("D ")) {
								ArrayList<String> files = deletedFiles.get(f
										.getName());
								if (files == null) {
									files = new ArrayList<String>();
									deletedFiles.put(f.getName(), files);
								}
								files.add(v.substring(2));
							} else {
								ArrayList<String> files = addedFiles.get(f
										.getName());
								if (files == null) {
									files = new ArrayList<String>();
									addedFiles.put(f.getName(), files);
								}
								files.add(v);
							}
						}
						continue;
					}
					if (f.getDataTypeId() == DataType.INT) {
						try {
							String tempValueOfRequestWithHash = value;
							value = value.trim();
							if (value.startsWith("#")) {
								value = value.substring(1);
								int relativeRequestId = Integer.parseInt(value);
								Integer actualRequestId = requestIdMapping
										.get(relativeRequestId);
								if (colName.equals(Field.REQUEST)) {
									if (actualRequestId == null) {
										currentRelativeRequestId = relativeRequestId;
										value = tempValueOfRequestWithHash;
									} else {
										value = actualRequestId + "";
									}
								} else if (colName
										.equals(Field.PARENT_REQUEST_ID)) {
									if (actualRequestId == null)
										throw new IllegalArgumentException(
												"Relative request id "
														+ relativeRequestId
														+ " is not found");
									else
										value = tempValueOfRequestWithHash + "";
								} else
									throw new NumberFormatException(
											"Invalid value'" + value
													+ "' for field: " + colName
													+ "");
							} else if (value.equals("")) {
								if (colName.equals(Field.REQUEST))
									value = "0";
							}

							else {
								value = Integer.toString(Integer
										.parseInt(value));
							}
						} catch (Exception e) {
							LOG.error("Unable to parse integer value: '"
									+ value + "' for field: " + colName);
							continue;
						}
					}
					if (f.getDataTypeId() == DataType.REAL) {
						try {
							if (!value.equals("")) {
								value = Double.toString(Double
										.parseDouble(value));
							}
						} catch (Exception e) {
							LOG.error("Unable to parse real value: '" + value
									+ "' for field: " + colName);
							continue;
						}
					}
					if (f.getDataTypeId() == DataType.TYPE) {

						if (!value.equals("")) {
							int indexOfBraces = allowedValues.indexOf('(');
							if (indexOfBraces < 0) {
								throw new IllegalArgumentException(
										"The DropDown values are not found");
							}
							String ddownlist = allowedValues.substring(
									indexOfBraces + 1,
									allowedValues.length() - 1);

							String[] list = ddownlist.split("/");

							Boolean flag = false;

							for (String eachValue : list) {
								if (eachValue.equals(value)) {
									flag = true;
									break;
								}
							}
							if (!flag) {
								System.out
										.println("The Value entered for"
												+ f.getName()
												+ "dropdown field doesnot match the expected values");
								LOG
										.error("The Value entered for"
												+ f.getName()
												+ "dropdown field doesnot match the expected values");
								throw new IllegalArgumentException(
										"The Value entered for"
												+ f.getName()
												+ "dropdown field doesnot match the expected values");
							}
						}
					} else if (f.DATATYPEID == DataType.BOOLEAN) {
						if (!value.equals("")) {
							int indexOfBraces = allowedValues.indexOf('(');
							if (indexOfBraces < 0) {
								throw new IllegalArgumentException(
										"The DropDown values are not found");
							}
							String ddownlist = allowedValues.substring(
									indexOfBraces + 1,
									allowedValues.length() - 1);

							String[] list = ddownlist.split("/");

							Boolean flag = false;

							for (String eachValue : list) {
								if (eachValue.equals(value)) {
									flag = true;
									break;
								}
							}
							if (!flag) {
								System.out
										.println("The Value entered for"
												+ f.getName()
												+ "CheckBox field doesnot match the expected values");
								LOG
										.error("The Value entered for"
												+ f.getName()
												+ "CheckBox field doesnot match the expected values");
								throw new IllegalArgumentException(
										"The Value entered for"
												+ f.getName()
												+ "CheckBox field doesnot match the expected values");
							}
						}
					}
					if (value != null)
						params.put(colName, value);
				}
				/*
				 * end of for loop
				 */
				if (params.size() == 0) {
					continue;
				}
				String baStr = (String) params.get(Field.BUSINESS_AREA);
				if ((baStr == null) || (baStr.length() == 0)) {
					params.put(Field.BUSINESS_AREA, ba.getSystemPrefix());
				}

				if (!params.containsKey(Field.USER)
						|| !RoleUser.isSuperUser(u.getUserId())) {
					params.put(Field.USER, u.getUserLogin());
				}

				String extraCol = "";
				Request req = null;

				// Upload files
				Hashtable<String, ArrayList<AttachmentInfo>> addedRepoFiles = new Hashtable<String, ArrayList<AttachmentInfo>>();
				for (String field : addedFiles.keySet()) {
					ArrayList<AttachmentInfo> files = addedRepoFiles.get(field);
					if (files == null) {
						files = new ArrayList<AttachmentInfo>();
						addedRepoFiles.put(field, files);
					}
					ArrayList<String> strFiles = addedFiles.get(field);
					for (String s : strFiles) {
						Uploader uploader = new Uploader();
						AttachmentInfo attInfo = uploader
								.copyIntoRepository(new File(s));
						files.add(attInfo);
					}
				}
				String reqIdStr = (String) params.get(Field.REQUEST);

				for (String field : addedRepoFiles.keySet()) {
					ArrayList<AttachmentInfo> addRF = addedRepoFiles.get(field);
					if (addRF == null)
						continue;
					for (AttachmentInfo ai : addRF) {
						ai.requestFileId = 0;
					}
					params.put(field, addRF);
				}

				if (currentRelativeRequestId == 0 && reqIdStr.equals("0")) {
					params.put("requestID", "0");

				} else if (reqIdStr.startsWith("#")) {
					params.put("requestID", reqIdStr);
					requestIdMapping.put(currentRelativeRequestId, Integer
							.parseInt(reqIdStr.substring(1)));

				}

				else {
					Request oldRequest = Request.lookupBySystemIdAndRequestId(
							ba.getSystemId(), Integer.parseInt(reqIdStr));

					if (oldRequest == null) {
						throw new IllegalArgumentException("Given Request Id "
								+ reqIdStr + " is not found in the system");
					}
					params.put("requestID", reqIdStr);

					ArrayList<AttachmentInfo> finalAttachments = new ArrayList<AttachmentInfo>();

					for (String field : addedRepoFiles.keySet()) {

						Collection<AttachmentInfo> newAtts = addedRepoFiles
								.get(field);
						Collection<AttachmentInfo> oldAtts = new ArrayList<AttachmentInfo>();

						if (field.equals(Field.ATTACHMENTS)) {
							oldAtts = oldRequest.getAttachments();
						} else {
							String attString = oldRequest.getExString(field);

							try {
								oldAtts = AttachmentInfo.fromJson(attString);
							} catch (Exception exp) {
								System.out.println("error while parsing: "
										+ attString);
								exp.printStackTrace();
							}

						}
						finalAttachments.addAll(oldAtts);

						for (AttachmentInfo newAttInfo : newAtts) {
							AttachmentInfo foundOld = null;

							for (AttachmentInfo oldAttInfo : oldAtts) {
								if (oldAttInfo.name.equals(newAttInfo.name)) {
									newAttInfo.requestFileId = oldAttInfo.requestFileId;
									foundOld = oldAttInfo;
									break;
								}
							}
							if (foundOld != null) {
								finalAttachments.remove(foundOld);
							}
							finalAttachments.add(newAttInfo);
						}
						params.put(field, finalAttachments);
					}

				}

				arr.add(params);

				/*
				 * try {
				 * 
				 * AddRequest addRequest = new AddRequest();
				 * addRequest.setSource(TBitsConstants.SOURCE_WEB); if
				 * (contextPath != null) addRequest.setContext(contextPath); req
				 * = addRequest.addRequest(params); extraCol = "Added " +
				 * req.getRequestId() + " successfully."; else {
				 * 
				 * 
				 * 
				 * UpdateRequest updateRequest = new UpdateRequest();
				 * updateRequest.setSource(TBitsConstants.SOURCE_WEB); if
				 * (contextPath != null) updateRequest.setContext(contextPath);
				 * req = updateRequest.updateRequest(params); extraCol =
				 * "Updated " + req.getRequestId() + " successfully.";
				 * 
				 * } } catch (APIException e) {
				 * LOG.error("",(e)); if
				 * (e.toString().contains(TBitsConstants.API_DATE_FORMAT))
				 * extraCol = "FAILED: Due date format should be: " +
				 * u.getWebConfigObject().getWebDateFormat(); else extraCol =
				 * "FAILED:" + e.toString(); e.printStackTrace(); } catch
				 * (Exception e) { LOG.error("",(e));
				 * extraCol = "FAILED:" + e.getMessage(); e.printStackTrace(); }
				 */
				// copy the input to output with one extra column -
				// that will contain either the request number or the error
				// message
				/*
				 * String[] output = new String[row.length + 1]; int i = 0; for
				 * (; i < row.length; i++) { if ((i < colNames.length) &&
				 * (colNames[i] != null)) { String colName = colNames[i];
				 * 
				 * if ((req != null) && (req.get(colName) != null)) output[i] =
				 * req.get(colName); else output[i] = row[i]; } } output[i] =
				 * extraCol;
				 */

				// csvw.writeNext(output);
			}
		} catch (IOException e) {
			// csvw.writeNext(new String[] {
			// "Unable to read from the input file. The file may not be in the CSV format."
			// });
			LOG.error(e);
		}
		JSONArray jsonString = new JSONArray();
		try {
			jsonString = getJsonString(arr);
		} catch (JSONException e) {

			e.printStackTrace();
		}
		return jsonString.toString();

		// return outputWriter.toString();
	}

	boolean checkForErrors(InputStream inputStream, User u, BusinessArea ba,
			String contextPath, StringBuilder error, StringBuilder jsonString)
			throws DatabaseException, UnsupportedEncodingException {

		InputStreamReader isr;
		try {
			isr = new InputStreamReader(inputStream, TBitsConstants.CHARSET);
		} catch (UnsupportedEncodingException e3) {
			throw e3;
		}

		CSVReader csvr = new CSVReader(isr);
		String[] colNames = null;
		try {
			colNames = csvr.readNext();
		} catch (IOException e2) {
			String s = "Unable to read the column names" + "\n";
			error.append(s);
			LOG.error(s, e2);
			return false;
		}
		if ((colNames == null) || (colNames.length == 0)) {
			error.append("The column headers not found." + "\n");
			return false;
		}
		// jump the datatype and display name rows

		String[] dName = null;
		try {
			dName = csvr.readNext();

		} catch (IOException e1) {
			String s = "Unable to read the display names" + "\n";
			error.append(s);
			LOG.error(s, e1);

		}
		if (dName == null) {
			error.append("Display Name row is not found." + "\n");
			return false;
		}
		String[] row = null;
		ArrayList<HashMap<String, Object>> arr = new ArrayList<HashMap<String, Object>>();

		boolean allowAll = false;
		try {
			Hashtable<Integer, Integer> requestIdMapping = new Hashtable<Integer, Integer>();
			int rowno = 0;

			while ((row = csvr.readNext()) != null) {
				Hashtable<String, ArrayList<String>> addedFiles = new Hashtable<String, ArrayList<String>>();
				Hashtable<String, ArrayList<String>> deletedFiles = new Hashtable<String, ArrayList<String>>();

				HashMap<String, Object> params = new HashMap<String, Object>();
				int currentRelativeRequestId = 0;
				++rowno;
				error.append("Errors in row: " + rowno + "\n");

				for (int i = 0; (i < colNames.length) && (i < row.length); i++) {

					String colName = colNames[i];
					String value = row[i];
					String allowedValues = dName[i];

					Field f = Field.lookupBySystemIdAndFieldName(ba
							.getSystemId(), colName);
					if (f == null) {
						LOG.warn("Unable to find field for column '" + colName
								+ "'");
						continue;
					}
					if (f.getName() == Field.RELATED_REQUESTS) {
						if ((value != null) && (value.trim().length() > 0)) {
							String[] relReqs = value.split(",");
							StringBuilder finalReqStr = new StringBuilder();
							for (String relreq : relReqs) {
								relreq = relreq.trim();
								if (relreq.startsWith("#")) {
									relreq = relreq.substring(1);
									Integer reqId = requestIdMapping
											.get(Integer.parseInt(relreq));
									if (reqId == null)
										throw new IllegalArgumentException(
												"Request id could not be found wrt to relative request '"
														+ relreq + "'");
									relreq = reqId + "";
								}
								finalReqStr.append(relreq).append(",");
							}
							if (finalReqStr.length() > 0)
								finalReqStr
										.deleteCharAt(finalReqStr.length() - 1);
							value = finalReqStr.toString();
						}
					}
					if ((f.getDataTypeId() == DataType.DATE)
							|| (f.getDataTypeId() == DataType.DATETIME)) {
						Date d;
						String webDateFormat = u.getWebConfigObject()
								.getWebDateFormat();
						try {
							SimpleDateFormat sdf = new SimpleDateFormat(
									webDateFormat);// DATETIME_FORMAT);

							if (!value.equals("")) {
								d = sdf.parse(value);
								SimpleDateFormat sdf1 = new SimpleDateFormat(
										"MM/dd/yyyy");
								value = sdf1.format(d);
							}

						} catch (ParseException e) {
							e.printStackTrace();
							/*
							 * Date curr = new Date(); SimpleDateFormat sdf =new
							 * SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
							 * String currDataTime= curr.toString(); d =
							 * sdf.parse(currDataTime); SimpleDateFormat sdf1 =
							 * new SimpleDateFormat("MM/dd/yyyy"); value =
							 * sdf1.format(d);
							 */
							LOG.debug("After parsing: " + value);
						}

					}
					if (f.getDataTypeId() == DataType.ATTACHMENTS) {
						// continue;
						if (value.trim().length() == 0)
							continue;
						String[] values = null;
						try {
							if (value.indexOf('"') > -1)
								values = splitCommaSeparatedString(value);
							else
								values = value.split(",");
						} catch (Exception e) {
							e.printStackTrace();
						}
						if ((values == null) || (values.length == 0))
							continue;
						for (String v : values) {
							if ((v == null) || (v.length() == 0))
								continue;
							if (v.startsWith("D ")) {
								ArrayList<String> files = deletedFiles.get(f
										.getName());
								if (files == null) {
									files = new ArrayList<String>();
									deletedFiles.put(f.getName(), files);
								}
								files.add(v.substring(2));
							} else {
								ArrayList<String> files = addedFiles.get(f
										.getName());
								if (files == null) {
									files = new ArrayList<String>();
									addedFiles.put(f.getName(), files);
								}
								files.add(v);
							}
						}
						continue;
					}
					if (f.getDataTypeId() == DataType.INT) {
						try {
							String tempValueOfRequestWithHash = value;
							value = value.trim();
							if (value.startsWith("#")) {
								value = value.substring(1);
								int relativeRequestId = Integer.parseInt(value);
								Integer actualRequestId = requestIdMapping
										.get(relativeRequestId);
								if (colName.equals(Field.REQUEST)) {
									if (actualRequestId == null) {
										currentRelativeRequestId = relativeRequestId;
										value = tempValueOfRequestWithHash;
									} else {
										value = actualRequestId + "";
									}
								} else if (colName
										.equals(Field.PARENT_REQUEST_ID)) {
									if (actualRequestId == null)
										throw new IllegalArgumentException(
												"Relative request id "
														+ relativeRequestId
														+ " is not found");
									else
										value = tempValueOfRequestWithHash + "";
								} else
									throw new NumberFormatException(
											"Invalid value'" + value
													+ "' for field: " + colName
													+ "");
							} else if (value.equals("")) {
								if (colName.equals(Field.REQUEST))
									value = "0";
							}

							else {
								try {
									value = Integer.toString(Integer
											.parseInt(value));
								} catch (NumberFormatException e) {
									e.printStackTrace();
									error
											.append(" Please provide a proper value request field  "
													+ "\n");
									return false;
								}
							}
						} catch (Exception e) {
							LOG.error("Unable to parse integer value: '"
									+ value + "' for field: " + colName);
							continue;
						}
					}
					if (f.getDataTypeId() == DataType.REAL) {
						try {
							if (!value.equals("")) {
								value = Double.toString(Double
										.parseDouble(value));
							}
						} catch (Exception e) {
							LOG.error("Unable to parse real value: '" + value
									+ "' for field: " + colName);
							continue;
						}
					}
					if (f.getDataTypeId() == DataType.TYPE) {

						if (!value.equals("")) {
							int indexOfBraces = allowedValues.indexOf('(');
							if (indexOfBraces < 0) {
								throw new IllegalArgumentException(
										"The DropDown values are not found");
							}
							String ddownlist = allowedValues.substring(
									indexOfBraces + 1,
									allowedValues.length() - 1);

							String[] list = ddownlist.split("/");

							Boolean flag = false;

							for (String eachValue : list) {
								if (eachValue.equals(value)) {
									flag = true;
									break;
								}
							}
							if (!flag) {
								System.out
										.println("The Value entered for"
												+ f.getName()
												+ "dropdown field doesnot match the expected values");
								LOG
										.error("The Value entered for"
												+ f.getName()
												+ "dropdown field doesnot match the expected values");
								error
										.append("The Value ("+ value + " ) entered for  "
												+ f.getName()
												+ " dropdown field doesnot match the expected values "
												+ "\n");
								return false;
							}
						}
					} else if (f.getDataTypeId() == DataType.BOOLEAN) {
						if (!value.equals("")) {
							int indexOfBraces = allowedValues.indexOf('(');
							if (indexOfBraces < 0) {
								throw new IllegalArgumentException(
										"The DropDown values are not found");
							}
							String ddownlist = allowedValues.substring(
									indexOfBraces + 1,
									allowedValues.length() - 1);

							String[] list = ddownlist.split("/");

							Boolean flag = false;

							for (String eachValue : list) {
								if (eachValue.equals(value)) {
									flag = true;
									break;
								}
							}
							if (!flag) {
								System.out
										.println("The Value entered for"
												+ f.getName()
												+ "CheckBox field doesnot match the expected values");
								LOG
										.error("The Value entered for"
												+ f.getName()
												+ "CheckBox field doesnot match the expected values");
								error
										.append("The Value entered for "
												+ f.getName()
												+ " CheckBox field doesnot match the expected values"
												+ "\n");
								return false;
							}
						}
					}
					if (value != null)
						params.put(colName, value);
				}
				/*
				 * end of for loop
				 */
				if (params.size() == 0) {
					continue;
				}

				String baStr = (String) params.get(Field.BUSINESS_AREA);
				if ((baStr == null) || (baStr.length() == 0)) {
					params.put(Field.BUSINESS_AREA, ba.getSystemPrefix());
				}

				if (!params.containsKey(Field.USER)
						|| !RoleUser.isSuperUser(u.getUserId())) {
					params.put(Field.USER, u.getUserLogin());
				}

				String extraCol = "";
				Request req = null;

				// Upload files
				Hashtable<String, ArrayList<AttachmentInfo>> addedRepoFiles = new Hashtable<String, ArrayList<AttachmentInfo>>();
				for (String field : addedFiles.keySet()) {
					ArrayList<AttachmentInfo> files = addedRepoFiles.get(field);
					if (files == null) {
						files = new ArrayList<AttachmentInfo>();
						addedRepoFiles.put(field, files);
					}
					ArrayList<String> strFiles = addedFiles.get(field);
					for (String s : strFiles) {
						Uploader uploader = new Uploader();
						try {
							File file = new File(s);
							if (file.exists()) {
								AttachmentInfo attInfo = uploader
										.copyIntoRepository(new File(s));
								files.add(attInfo);
							} else {
								error.append("The file \"" + s
										+ "\" present in row no " + rowno
										+ " in the field " + field
										+ "  does not exist");
								throw new FileNotFoundException();
							}
						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					}
				}

				String reqIdStr = (String) params.get(Field.REQUEST);

				for (String field : addedRepoFiles.keySet()) {
					ArrayList<AttachmentInfo> addRF = addedRepoFiles.get(field);
					if (addRF == null)
						continue;
					for (AttachmentInfo ai : addRF) {
						ai.requestFileId = 0;
					}
					params.put(field, addRF);
				}

				if (currentRelativeRequestId == 0 && reqIdStr.equals("0")) {
					params.put("requestID", "0");

				} else if (reqIdStr.startsWith("#")) {
					params.put("requestID", reqIdStr);
					requestIdMapping.put(currentRelativeRequestId, Integer
							.parseInt(reqIdStr.substring(1)));

				} else {
					Request oldRequest = Request.lookupBySystemIdAndRequestId(
							ba.getSystemId(), Integer.parseInt(reqIdStr));

					if (oldRequest == null) {
						error.append("Given Request Id " + reqIdStr
								+ " is not found in the system" + "\n");
						return false;
					}

					params.put("requestID", reqIdStr);

					ArrayList<AttachmentInfo> finalAttachments = new ArrayList<AttachmentInfo>();

					for (String field : addedRepoFiles.keySet()) {

						Collection<AttachmentInfo> newAtts = addedRepoFiles
								.get(field);
						Collection<AttachmentInfo> oldAtts = new ArrayList<AttachmentInfo>();

						if (field.equals(Field.ATTACHMENTS)) {
							oldAtts = oldRequest.getAttachments();
						} else {
							String attString = oldRequest.getExString(field);

							try {
								oldAtts = AttachmentInfo.fromJson(attString);
							} catch (Exception exp) {
								System.out.println("error while parsing: "
										+ attString);
								exp.printStackTrace();
							}

						}
						finalAttachments.addAll(oldAtts);

						for (AttachmentInfo newAttInfo : newAtts) {
							AttachmentInfo foundOld = null;

							for (AttachmentInfo oldAttInfo : oldAtts) {
								if (oldAttInfo.name.equals(newAttInfo.name)) {
									newAttInfo.requestFileId = oldAttInfo.requestFileId;
									foundOld = oldAttInfo;
									break;
								}
							}
							if (foundOld != null) {
								finalAttachments.remove(foundOld);
							}
							finalAttachments.add(newAttInfo);
						}
						params.put(field, finalAttachments);
					}

				}

				arr.add(params);

			}

			try {
				JSONArray json = getJsonString(arr);
				jsonString.append(json.toString());
			} catch (JSONException e) {

				e.printStackTrace();
				return false;
			}

		} catch (IOException e) {
			error
					.append("Unable to read from the input file. The file may not be in the CSV format."
							+ "\n");
			LOG.error(e);
			return false;
		}

		return true;

	}
}
