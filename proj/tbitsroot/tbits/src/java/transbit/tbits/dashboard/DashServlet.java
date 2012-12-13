package transbit.tbits.dashboard;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.report.TBitsReportEngine;

/**
 * Servlet implementation class Test1
 */
public class DashServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	int mcount;
	Hashtable HashAllFiles = new Hashtable();

	/**
	 * Default constructor.
	 */
	public DashServlet() {
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		int i = 10;// any number, just used to generate randon number fr html
		
		// file gen. by rptdesign
		String reqId = request.getParameter("id"); // tells us what kind of
		// request it is
		// id=0=> info from databse abt gadgets and files and vis, min etc
		// id=1=> we need the content corresponding to an rptdesign
		// id=2=> gets the parameters of a report
		PrintWriter out = response.getWriter();
		if (reqId.compareTo("0") == 0) {
			String user_id = request.getParameter("uid");

			// String login = request.getRemoteUser();
			// User user = User.lookupAllByUserLogin(login);
			// int uid = user.getUserId();

			//
			java.sql.Connection con = null;

			try {
				JSONArray list = new JSONArray();

				String qs1 = "SELECT col,is_visible,is_minimized,caption, report_file,refresh_rate,[dbo].[gadget_user_config].id FROM [dbo].[gadget_user_config],[dbo].[gadgets] where user_id=? and [dbo].[gadget_user_config].id =[dbo].[gadgets].id order by height";
				String qs2 = "SELECT count(*) FROM [dbo].[gadget_user_config],[dbo].[gadgets] where [dbo].[gadget_user_config].id =[dbo].[gadgets].id and user_id=?";

				con = DataSourcePool.getConnection();
				PreparedStatement statement = con.prepareStatement(qs2);
				statement.setString(1, user_id);

				ResultSet rs1 = statement.executeQuery();
				rs1.next();
				mcount = rs1.getInt(1); // number of gadgets for the user
				list.add(new Integer(rs1.getInt(1)));
				statement.close();

				statement = con.prepareStatement(qs1);
				statement.setString(1, user_id);

				ResultSet rs = statement.executeQuery();
				while (rs.next()) {

					list.add(new Integer(rs.getInt(1)));
					list.add(new Integer(rs.getInt(2)));
					list.add(new Integer(rs.getInt(3)));
					list.add((rs.getString(4)));
					list.add((rs.getString(5)));
					list.add(new Integer(rs.getInt(6)));
					list.add(new Integer(rs.getInt(7)));
				}
				out.println(list);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error Trace in getConnection() : "
						+ e.getMessage());
			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else if (reqId.compareTo("1") == 0) {
			String fname = request.getParameter("fname");
			JSONArray list = new JSONArray();
			list.add(request.getParameter("i"));
			list.add(request.getParameter("j"));
			list.add(getContent(request, fname, i)); // gets the content corresponding to
			// a rptdesign
			out.println(list);
		} else if (reqId.compareTo("2") == 0) {
			Hashtable hashFile = new Hashtable();
//			IReportEngine engine = null;
			JSONArray list = new JSONArray();
			String s = request.getParameter("fname");
			String dataType;
			try {
				TBitsReportEngine a = TBitsReportEngine.getInstance();
//				engine = a.getEngine();
				IReportRunnable design = null;
				design = a.getReportDesign(s);
				list.add(request.getParameter("i"));
				list.add(request.getParameter("j"));

				IGetParameterDefinitionTask task2 = a.createGetParameterDefinitionTask(design);
				Collection params = task2.getParameterDefns(true);
				Iterator iter = params.iterator();

				while (iter.hasNext()) {
					IParameterDefnBase param = (IParameterDefnBase) iter.next();
					if (param instanceof IParameterGroupDefn) // if its a group
					{

						IParameterGroupDefn group = (IParameterGroupDefn) param;
						list.add("#");
						list.add(group.getName());
						Iterator i2 = group.getContents().iterator();
						while (i2.hasNext()) // iterates over the group
						{
							IScalarParameterDefn scalar = (IScalarParameterDefn) i2
									.next();
							dataType = getParameterDetails(scalar, task2, list,
									s);
							hashFile.put(scalar.getName(), dataType);
						}
						list.add("#");
					} else {
						IScalarParameterDefn scalar = (IScalarParameterDefn) param;
						dataType = getParameterDetails(scalar, task2, list, s);
						hashFile.put(scalar.getName(), dataType);

					}
				}
				list.add("$");
				HashAllFiles.put(s, hashFile);
				out.println(list);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		int k; // an integer for adding to list, number of parameters per gadget
		int i, col, ht, vis, is_minimized, id;
		String postType = request.getParameter("postType");
		// postType tells us the type of post transaction happening
		// 0: Final post, posts the info of whole Dashboard, which Gadget is
		// min.
		// which is closed etc
		// 1: Updates the Refresh Rate of a Gadget
		// 2: Updates the parameters corresponding to a Gadget

		if (postType.compareTo("0") == 0) {
			String s = request.getParameter("id"); // posted String, it is
			// posted as id=".."
			JSONArray t = JSONArray.fromObject(s);
			k = 5; // number of parameters per Gadget, just used to add
			java.sql.Connection con = null;
			try {
				PrintWriter out = response.getWriter();
				JSONArray list = new JSONArray();
				con = DataSourcePool.getConnection();
				Statement statement = con.createStatement();
				String qs3 = "SELECT count(*) FROM [dbo].[gadget_user_config],[dbo].[gadgets] where [dbo].[gadget_user_config].id =[dbo].[gadgets].id";
				ResultSet rs3 = statement.executeQuery(qs3);
				rs3.next();
				mcount = rs3.getInt(1); // Gadget count
				ResultSet rs1, rs2;
				for (i = 0; i < mcount; i++) {

					col = t.getInt(k * i);
					ht = t.getInt(k * i + 1);
					vis = t.getInt(k * i + 2);
					is_minimized = t.getInt(k * i + 3);
					id = t.getInt(k * i + 4);
					String qs1 = "UPDATE [dbo].[gadget_user_config] SET col=" + col
							+ ",height=" + ht + ",is_visible=" + vis + ",is_minimized="
							+ is_minimized + " WHERE id=" + id;
					statement.executeUpdate(qs1);
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error Trace in getConnection() : "
						+ e.getMessage());

			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if (postType.compareTo("1") == 0) {
			String RefRate = request.getParameter("rr");
			String fidString = request.getParameter("fid");
			id = Integer.parseInt(fidString);
			java.sql.Connection con = null;
			try {
				con = DataSourcePool.getConnection();
				Statement statement = con.createStatement();
				String qs1 = "UPDATE [dbo].[gadget_user_config] SET refresh_rate="
						+ RefRate + " WHERE id=" + id;
				statement.executeUpdate(qs1);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error Trace in getConnection() : "
						+ e.getMessage());

			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else if (postType.compareTo("2") == 0) {
			int user_id = 1;
			JSONArray toSend = new JSONArray();
			String fname = request.getParameter("fname");
			java.sql.Connection con = null;
			List list = new LinkedList();
			list = new ArrayList();
			list.add("$");
			List listType = new LinkedList();		//list which stores the Type    
			listType = new ArrayList();
			listType.add("$");

			try {
				String parName;
				String value = null;
				String qs;
				int netExc = 0;
				String errorMesg = "";
				int exc = 0;
				String type;
				Hashtable temp = new Hashtable();
				temp = (Hashtable) HashAllFiles.get(fname);
				Enumeration pars = temp.keys();
				while (pars.hasMoreElements()) {
					parName = (String) pars.nextElement();
					value = request.getParameter(parName);
					exc = 0;
					type = (String) temp.get(parName);
					if (value != null) {
						try {
							if (value == "") {
							}

							// the further code is for Validation
							else if (type.compareTo("String") == 0)
								;
							else if (type.compareTo("Boolean") == 0
									|| type.compareTo("Integer") == 0) {
								int a = Integer.parseInt(value);
								if (type.compareTo("Boolean") == 0) {
									if (a != 0 && a != 1) {
										exc = 1;
										errorMesg = errorMesg
												.concat("The Param "
														+ parName
														+ " should be of Boolean type (0/1)\n");
									}
								}
							} else if (type.compareTo("Decimal") == 0
									|| type.compareTo("Float") == 0) {
								float a = Float.parseFloat(value);
							} else if (type.compareTo("Date") == 0) {
								DateFormat formatter = new SimpleDateFormat(
										"yyyy-mm-dd");
								Date date = (Date) formatter.parse(value);
							} else if (type.compareTo("Time") == 0) {
								DateFormat formatter = new SimpleDateFormat(
										"hh:mm:ss a");
								Date date = (Date) formatter.parse(value);
							} else if (type.compareTo("DateTime") == 0) {
								DateFormat formatter = new SimpleDateFormat(
										"yyyy-mm-dd hh:mm:ss a");
								Date date = (Date) formatter.parse(value);
							}
						} catch (NumberFormatException e) {
							exc = 1;
							errorMesg = errorMesg.concat("The Parameter "
									+ parName + " should be of type " + type
									+ "\n");
						} catch (ParseException e) {
							exc = 1;
							errorMesg = errorMesg.concat("The Parameter "
									+ parName + " should be of type " + type);
							if (type.compareTo("Date") == 0) {
								errorMesg = errorMesg
										.concat("\n Date must be in the form : yyyy-mm-dd \n");
							} else if (type.compareTo("Time") == 0) {
								errorMesg = errorMesg
										.concat("\n Time must be in the form : hh:mm:ss a\n");
							}
							if (type.compareTo("DateTime") == 0) {
								errorMesg = errorMesg
										.concat("\n DateTime must be in the form : yyyy-mm-dd hh:mm:ss a \n");
							}

						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (exc == 0){
								list.add(parName);
								listType.add(type);
									
							}
							else
								netExc = 1;
						}
					}
				}
				int size = list.size();
				int j = 1;
				String qs1;
				PrintWriter out = response.getWriter();
				toSend.add(new Integer(netExc));
				toSend.add(errorMesg);
				con = DataSourcePool.getConnection();
				Statement statement = con.createStatement();
				while (j < size) {
					parName = list.get(j).toString();
					type = listType.get(j).toString();
					value = request.getParameter(parName);
					qs = "select id from [dbo].[gadgets] where report_file = '" + fname + "'";
					ResultSet rs = statement.executeQuery(qs);
					rs.next();
					id = rs.getInt(1); // Gadget count
					qs = "DELETE FROM [dbo].[gadget_user_params] WHERE id= " + id +" and name= '" + parName
							+ "' and user_id=" + user_id;
					statement.execute(qs);
					qs1 = "INSERT INTO [dbo].[gadget_user_params] VALUES(" + user_id+ ",'" + id + "','" + parName + "', '" + value+ "', '" + type+ "')";
					statement.execute(qs1);
					j++;
				}
				out.println(toSend);
				statement.close();
			}

			catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		// TODO Auto-generated method stub

	}

	// Gets the content of a file given by name s
	protected String getContent(HttpServletRequest request, String s, int i) {// used to get content of an
		// rptdesign file
		String f = null; // creates an html page and gets its content
//		IReportEngine engine = null;
		ServletContext sc = request.getSession().getServletContext();
		// EngineConfig config = null;
		java.sql.Connection con = null;
		try {

			TBitsReportEngine a = TBitsReportEngine.getInstance();
//			engine = a.getEngine();
			IReportRunnable design = null;
			design = a.getReportDesign(s);
			IRunAndRenderTask task = a.createRunAndRenderTask(design);
			task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, DashServlet.class.getClassLoader());
			
			String qs1 = "SELECT name, value,type FROM [dbo].[gadget_user_params] where user_id=1 and [dbo].[gadget_user_params].id = (select id from [dbo].[gadgets] where report_file = '" + s + "')";
			con = DataSourcePool.getConnection();
			Statement statement = con.createStatement();
			ResultSet rs = statement.executeQuery(qs1);
			while (rs.next()) {
				if(rs.getString(2).compareTo("")==0);
				else if(rs.getString(3) == null);
				else if(rs.getString(3).compareTo("Date") == 0)
				{
					DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
					Date date = (Date)formatter.parse(rs.getString(2));
					java.sql.Date sqlDate =  new java.sql.Date(date.getTime());
					System.out.println(sqlDate);
					task.setParameterValue(rs.getString(1),sqlDate);
				}
				else if(rs.getString(3).compareTo("Time") == 0)
				{
					DateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
					Date date = (Date)formatter.parse(rs.getString(2));
					java.sql.Time sqlTime =  new java.sql.Time(date.getTime());
					System.out.println(sqlTime);
					task.setParameterValue(rs.getString(1),sqlTime);
				}
				else if(rs.getString(3).compareTo("DateTime") == 0)
				{
					DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss a");
					Date date = (Date)formatter.parse(rs.getString(2));
					java.sql.Date sqlDate =  new java.sql.Date(date.getTime());
					System.out.println(sqlDate);
					task.setParameterValue(rs.getString(1),sqlDate);
				}
				
				else
					task.setParameterValue(rs.getString(1),rs.getString(2));
						}
			HTMLRenderOption options = new HTMLRenderOption();
			double d = Math.random();
			String fileLocation = getServletContext().getRealPath("/")
					+ "tmp_reports" + d + i + ".html";
			options.setImageHandler(new HTMLServerImageHandler());
			options.setOutputFileName(fileLocation);
			options.setOutputFormat("html");
			//options.setImageDirectory("images");
			options.setBaseImageURL(request.getContextPath()+"/web/images/dashboard_images");
			options.setImageDirectory(sc.getRealPath("/web/images/dashboard_images"));
			task.setRenderOption(options);
			task.run();
			task.close();
			String lineSep = System.getProperty("line.separator");
			BufferedReader br = new BufferedReader(new FileReader(fileLocation));
			String nextLine = "";
			StringBuffer sb = new StringBuffer();
			while ((nextLine = br.readLine()) != null) {
				sb.append(nextLine);
				sb.append(lineSep);
			}
			f = sb.toString();
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return f;

	}

	// Gets the details of a Parameter given by a Scalar
	public String getParameterDetails(IScalarParameterDefn scalar,
			IGetParameterDefinitionTask task2, JSONArray list, String fname) {
		int user_id = 1;
		String dataType = null;
		java.sql.Connection con = null;
		String qs = "SELECT value FROM [dbo].[gadget_user_params] WHERE id = (select id from [dbo].[gadgets] where report_file = '" + fname + "') and name= '" + scalar.getName()
				+ "' and user_id=" + user_id;
		try {
			con = DataSourcePool.getConnection();
			Statement statement = con.createStatement();
			ResultSet rs1 = statement.executeQuery(qs);
			String value = null;
			if (rs1.next())
				value = rs1.getString(1);

			list.add(scalar.getName());
			switch (scalar.getControlType()) {

			case IScalarParameterDefn.TEXT_BOX:
				list.add("TextB");
				if (value != null) {
					list.add(value);
				} else {
					list.add("");
				}

				break;
			case IScalarParameterDefn.LIST_BOX:
				list.add("ListB");
				break;

			case IScalarParameterDefn.RADIO_BUTTON:
				list.add("RadioB");
				break;
			case IScalarParameterDefn.CHECK_BOX:
				list.add("RadioB");
				break;
			}
			switch (scalar.getDataType()) {
			case IScalarParameterDefn.TYPE_STRING:
				dataType = "String";
				break;
			case IScalarParameterDefn.TYPE_FLOAT:
				dataType = "Float";
				break;
			case IScalarParameterDefn.TYPE_INTEGER:
				dataType = "Integer";
				break;
			case IScalarParameterDefn.TYPE_DECIMAL:
				dataType = "Decimal";
				break;
			case IScalarParameterDefn.TYPE_DATE_TIME:
				dataType = "DateTime";
				break;
			case IScalarParameterDefn.TYPE_TIME:
				dataType = "Time";
				break;
			case IScalarParameterDefn.TYPE_DATE:
				dataType = "Date";
				break;
			case IScalarParameterDefn.TYPE_BOOLEAN:
				dataType = "Boolean";
				break;
			default:
				dataType = "any";
			}

			if (scalar.getControlType() != IScalarParameterDefn.TEXT_BOX) {
				Collection selectionList = task2.getSelectionList(scalar
						.getName());
				if (selectionList != null) {
					for (Iterator sliter = selectionList.iterator(); sliter
							.hasNext();) {
						IParameterSelectionChoice selectionItem = (IParameterSelectionChoice) sliter
								.next();
						list.add(selectionItem.getLabel());
						if (value == null)
							list.add("0");
						else {

							if (value.compareTo(selectionItem.getLabel()) == 0)
								list.add("1");
							else
								list.add("0");
						}
					}
				}

			}
			list.add("$");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return dataType;
		
	}

}
