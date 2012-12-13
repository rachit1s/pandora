package transbit.tbits.sms;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.admin.AdminAllUsers;
import transbit.tbits.admin.AdminUtil;
import transbit.tbits.admin.common.AdminProxyServlet;
import transbit.tbits.admin.common.MenuItem;
import transbit.tbits.admin.common.NavMenu;
import transbit.tbits.admin.common.URLRegistry;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Manish Jethani
 * Date: Jun 12, 2007
 * Time: 9:01:12 AM
 */
/*
 * This servlet is called to show sms logs in admin panel and total no of sms per BA
 * */

public class SmsLogServlet extends HttpServlet {
	 static
	    {
	    	 //urls
	        String url = "smslogs";
	    	String completeURL = url + ".admin";
	    	
	        //Create Mapping
			URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, SmsLogServlet.class);
			
			//Create Menu
			NavMenu nav = NavMenu.getInstance();
			nav.BAMenu.add(new MenuItem("SMS Logs", completeURL, "The details of the sms sent from tBits."));
	    }
	private static final long serialVersionUID = -8777684894516369028L;
	private static final String     HTML_SMSLOGS   = "web/tbits-admin-smslogs.htm";
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		HttpSession session = aRequest.getSession();

		try {
			handleGetRequest(aRequest, aResponse);
		} catch (DatabaseException de) {
			session.setAttribute("ExceptionObject", de);
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
			de.printStackTrace();

			return;
		} catch (TBitsException de) {
			session.setAttribute("ExceptionObject", de);
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
			de.printStackTrace();

			return;
		}

		return;
	}

	public void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException {
		int year;
		int sysId;
		int month;
		User                      user       = WebUtil.validateUser(aRequest);
		GregorianCalendar now = new GregorianCalendar();

		if(aRequest.getParameter("sysId") != null){
			String sysIdString = aRequest.getParameter("sysId");
			sysId = Integer.parseInt(sysIdString.trim());

		}else{
			WebConfig                 userConfig = user.getWebConfigObject();
	        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, 1);
			BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
			if (ba == null) {
	            sysId = 1;
	        }
			else
				sysId = ba.getSystemId();
		}
		 

		if(aRequest.getParameter("month") != null){
			String monthString = aRequest.getParameter("month");
			month = Integer.parseInt(monthString.trim());
		}else{
			month = now.get(Calendar.MONTH) + 1;
		}

		if(aRequest.getParameter("year") != null){
			String yearString = aRequest.getParameter("year");
			year = Integer.parseInt(yearString.trim());
		}else{
			year = now.get(Calendar.YEAR);
		}

		ShowSmsLogs showSmsLogs = new ShowSmsLogs();
		ArrayList<SmsBaCount> smsCountList = showSmsLogs.getSmsCount(month, year);
		ArrayList<SmsLogObject> smsLogsList = showSmsLogs.getlogs(sysId, month, year);
		String smsBaCount = "";
		for(SmsBaCount sba:smsCountList){

			try {
				int systemId = sba.getBa();
				BusinessArea ba =  BusinessArea.lookupBySystemId(systemId);
				String BADisplayName = ba.getDisplayName();
				smsBaCount += "<tr><td>"+BADisplayName+"</td><td>"+sba.getSmsCount()+"</td></tr>";
			} catch (DatabaseException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
		smsBaCount += "</table>";

		String smsLogHistory ="";

		for(SmsLogObject slo:smsLogsList){

			try {
				int systemId = slo.getSysId();
				BusinessArea ba =  BusinessArea.lookupBySystemId(systemId);
				String BADisplayName = ba.getDisplayName();
				String userStr = User.lookupByUserId(slo.getUserId()).getUserLogin();
				smsLogHistory+= "<tr><td class=\"sx\"><a href=\"" + WebUtil.getServletPath(aRequest, "/q/")
				+ ba.getSystemPrefix()
				+ "/" + slo.getRequestId()
				+ "#" + slo.getActionId() + "\">"+BADisplayName+"#"+slo.getRequestId()+"#"+slo.getActionId()+"</a></td><td>"+ userStr +"</td><td>"+slo.getCellNo()+"</td><td>"+slo.getTimestamp()+"</td></tr>";
			} catch (DatabaseException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] stringMonths= dfs.getMonths();
		BusinessArea ba = null;
		String date = stringMonths[month-1]+"-"+year;
		String title = "Sms Logs";
		//System.out.println("date = " + date);
		String previousMonth="<a href=\"" + WebUtil.getServletPath(aRequest, "/admin-smslogs") + "?sysId=1&month=" + (month-1) + "&year=2007\">Previous</a>";
		String nextMonth="<a href=\"" + WebUtil.getServletPath(aRequest, "/admin-smslogs") + "?sysId=1&month=" + (month+1) + "&year=2007\">Next</a>";
		DTagReplacer hp1  = new DTagReplacer(HTML_SMSLOGS);

		ba = BusinessArea.lookupBySystemId(sysId);
		String          baList  = AdminUtil.getSysIdList(sysId, user.getUserId());
		hp1.replace("cssFile", WebUtil.getCSSFile((ba.getSysConfigObject().getWebStylesheet()), ba.getSystemPrefix(), false));
		hp1.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
		hp1.replace("userLogin", user.getUserLogin());
		hp1.replace("bASmsCount",smsBaCount);
		hp1.replace("smsLogHistory",smsLogHistory);
		hp1.replace("previousMonth",previousMonth);
		hp1.replace("monthYear",date);
		hp1.replace("nextMonth",nextMonth);
		hp1.replace("title",title);
		hp1.replace("sys_ids", baList);
		
		//Added by Lokesh to show/hide transmittal tab based on the transmittal property in app-properties
		String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
		if ((trnProperty == null) || Boolean.parseBoolean(trnProperty) == false)
			hp1.replace("trn_display", "none");
		else
			hp1.replace("trn_display", "");

		PrintWriter out     = aResponse.getWriter();
		out.println(hp1.parse(ba.getSystemId()));
	}


}


