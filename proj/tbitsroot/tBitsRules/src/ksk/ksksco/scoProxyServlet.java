package ksksco;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IProxyServlet;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.AddHtmlRequest;
import transbit.tbits.webapps.WebUtil;
import  static ksksco.scoConstants.*;

public class scoProxyServlet implements IProxyServlet {
	private String pgbr = "<br><br><br><br>";

	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handleGetRequest(request, response);

	}

	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handleGetRequest(request, response);
}

	private void handleGetRequest(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{
		HttpSession session = request.getSession();
		try 
		{
			// get the request nos. from the httprequest
			String reqIds = request.getParameter("requestList");
			ArrayList<String> reqIdList = Utilities.toArrayList(reqIds) ;
			String sysPrefix = request.getParameter("dcrBA");

			BusinessArea ba = null ;
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if(ba==null)
				throw new TBitsException("invalid BA");

			User user  = WebUtil.validateUser(request);
			// create a hashtable<String,String> params for all the parameters in the request you want to fill
			HashSet<String> assSet = new HashSet<String>() ;           
			ArrayList<AttachmentInfo> allFilesInfo = new ArrayList<AttachmentInfo>() ;
			StringBuilder tabHtml= new StringBuilder();
			StringBuilder srs=new StringBuilder();
			tabHtml.append(pgbr);
			tabHtml.append("<table border = '2' style='border-collapse:collapse'>"+
					"<tr>"+
					"<th>S.No.</th>"+
					"<th>SCO#</th>"+
					"<th>SCO Date</th>"+
					"<th>Subject</th>"+
					"<th>Unit Code</th>"+
					"<th>Category</th>"+
					"<th>SCO File</th>"+
			"</tr>");


			int sno=0;
			int count = 1 ;
			String scodate;
			for (String reqId : reqIdList)
			{
				try {
					sno++;
					Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), Integer.parseInt(reqId));
                    srs.append(sysPrefix+"#"+reqId+",");
                    //columns in table
					String scorder=req.get(SCO_SCO_Order__);
				    scodate=Timestamp.toCustomFormat((Date)req.getObject(SCO_SCO_Date),"yyyy-MM-dd");
					if(scodate==null) 
					scodate=" ";
				
					String sub =req.get(SCO_Subject);
					String uc =req.get(SCO_Unit_Code);
					String cat = req.get(SCO_Category);
					String sepSubFiles = req.get(SCO_SCO_File);
					String sepSubFiles1 = req.get(SCO_Affected_Clauses);
					String otherFiles = req.get(SCO_Attachments);
					
					Collection<AttachmentInfo> subFileList = AttachmentInfo.fromJson(sepSubFiles) ;
					Collection<AttachmentInfo> subFileList1 = AttachmentInfo.fromJson(sepSubFiles1) ;
					Collection<AttachmentInfo> otherFileList = AttachmentInfo.fromJson(otherFiles) ;

					StringBuilder subAttEntries=new StringBuilder();
					

					subAttEntries.append("<table>");
					for( AttachmentInfo atInfo : subFileList )
					{  
						AttachmentInfo nai = new AttachmentInfo() ;
						nai.name = atInfo.name ;
						nai.repoFileId = atInfo.repoFileId ;
						nai.requestFileId = count++ ;
						nai.size = atInfo.size ;
						allFilesInfo.add(nai) ;

						subAttEntries.append("<tr><td><a href ="+WebUtil.getNearestPath(request,"")+
								"read-attachment/"+SCO_sysprefix+
								"?filerepoid="+atInfo.repoFileId+"&saveAs=true>"+atInfo.name+"</a></td></tr>");




					}
					subAttEntries.append("</table>");


					for( AttachmentInfo atInfo : otherFileList )
					{  
						AttachmentInfo nai = new AttachmentInfo() ;
						nai.name = atInfo.name ;
						nai.repoFileId = atInfo.repoFileId ;
						nai.requestFileId = count++ ;
						nai.size = atInfo.size ;
						allFilesInfo.add(nai) ;
						
					}
					


					for( AttachmentInfo atInfo : subFileList1 )
					{  
						AttachmentInfo nai = new AttachmentInfo() ;
						nai.name = atInfo.name ;
						nai.repoFileId = atInfo.repoFileId ;
						nai.requestFileId = count++ ;
						nai.size = atInfo.size ;
						allFilesInfo.add(nai) ;

						



					} 
					
					tabHtml.append(
							"<tr>"+
							"<td>"+sno+"</td>"+
							"<td>"+scorder+"</td>"+
							"<td>"+scodate+"</td>"+
							"<td>"+sub+"</td>"+
							"<td>"+uc+"</td>"+
							"<td>"+cat+"</td>"+
							"<td>"+subAttEntries.toString()+"</td>"+
							"</tr>"

					);

					String assStr = req.get(Field.SUBSCRIBER) ;
					ArrayList<String> assList = Utilities.toArrayList(assStr) ;
					assSet.addAll(assList) ;

				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				catch( Exception e )
				{
					e.printStackTrace() ;                    
				}                
			}
			tabHtml.append("</table>").append(pgbr);
			String assignees = "" ; 
			boolean isFirst = true ;
			for( Iterator<String> iter = assSet.iterator() ; iter.hasNext() ; )
			{
				String a = iter.next() ;

				if( !isFirst )
					assignees +=  "," + a ;
				else
				{
					assignees = a ;
					isFirst = false ;
				}
			}
			
			Hashtable<Field,ArrayList<AttachmentInfo>> attachTable = new Hashtable<Field,ArrayList<AttachmentInfo>>() ;

			BusinessArea idcBa = BusinessArea.lookupBySystemPrefix(CORR_sysprefix) ;
			if(null==idcBa)
				throw new TBitsException("invalid BA:"+CORR_sysprefix);

			int idcSysId = idcBa.getSystemId() ;
			Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(idcSysId, user.getUserId());

			Field field = Field.lookupBySystemIdAndFieldName(idcSysId,CORR_Other_Attachments); 
			if(null==field)
				throw new TBitsException("invalid field:" +CORR_Other_Attachments);
			attachTable.put(field, allFilesInfo);

			Field field1 = Field.lookupBySystemIdAndFieldName(idcSysId,CORR_Correspondence_File);

			attachTable.put(field1, new ArrayList<AttachmentInfo>() ) ;

			String allFilesJson = getAttachmentJson(idcBa,permTable,attachTable).toString() ;

			Hashtable<String,String> params = new Hashtable<String,String>();
			params.put(CORR_Description,tabHtml.toString()) ;
			params.put(CORR_Assignee, assignees );
			params.put(CORR_Generate_Correspondance,"false");
			params.put(AddHtmlRequest.REQUEST_FILES, allFilesJson);
			params.put(CORR_Communication_Protocol,"WPCLSEPCO");
			params.put(CORR_Source_Requests, srs.toString());
			params.put(CORR_Linked_Requests,srs.toString());
			// redirect the request to AddHtmlRequest
			// find nearest path
			request.setAttribute(AddHtmlRequest.PREFILL_TABLE, params);
			RequestDispatcher rd = request.getRequestDispatcher( WebUtil.getServletPath(request,"/add-request/" + idcBa.getSystemPrefix()) ) ;

			rd.forward(request, response) ;

		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			session.setAttribute("ExceptionObject",e);
			response.sendRedirect(WebUtil.getServletPath(request, "/error"));
			return;

		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			session.setAttribute("ExceptionObject",e);
			response.sendRedirect(WebUtil.getServletPath(request, "/error"));
			return;
		}
		
	}
	

	public String getName() {
		
		return "scoProxyServlet";
	}

}
