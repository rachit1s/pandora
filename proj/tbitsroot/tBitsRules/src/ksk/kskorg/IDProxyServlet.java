package kskorg;

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

import static kskorg.ORGConstants.*;
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

public class IDProxyServlet implements IProxyServlet {
	
	private String pgbr = "<br><br><br><br>";

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		 handleRequest(request,response);

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		 handleRequest(request,response);

	}

    private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    	
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
			tabHtml.append("<table border ='2' style='border-collapse:collapse'>"+
					"<tr>"+
					"<th>S.No.</th>"+
					"<th>Name</th>"+
					"<th>Designation</th>"+
					"<th>DOB</th>"+
					"<th>CV File</th>"+
			        "</tr>");


			int sno=0;
			int count = 1 ;
			for (String reqId : reqIdList)
			{
				try {
					sno++;
					Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), Integer.parseInt(reqId));
                    srs.append(sysPrefix+"#"+reqId+",");
                    //columns in table
					String name=req.get(CV_Name);
					String Designation=req.get(CV_Designation);
					String dob = Timestamp.toCustomFormat((Date)req.getObject(CV_DOB),"yyyy-MM-dd");
					
					if(dob==null)
						dob=" ";
										
					String otherFiles = req.get(Field.ATTACHMENTS);
					Collection<AttachmentInfo> otherFileList = AttachmentInfo.fromJson(otherFiles) ;
					StringBuilder otherAttEntries=new StringBuilder();

					otherAttEntries.append("<table>");
					for( AttachmentInfo atInfo : otherFileList )
					{  
						AttachmentInfo nai = new AttachmentInfo() ;
						nai.name = atInfo.name ;
						nai.repoFileId = atInfo.repoFileId ;
						nai.requestFileId = count++ ;
						nai.size = atInfo.size ;
						allFilesInfo.add(nai) ;
						otherAttEntries.append("<tr><td><a href ="+WebUtil.getNearestPath(request,"")+
								"read-attachment/"+ID_sysprefix+
								"?filerepoid="+atInfo.repoFileId+"&saveAs=true>"+atInfo.name+"</a></td></tr>");
					}
					otherAttEntries.append("</table>");

					tabHtml.append(
							"<tr>"+
							"<td>"+sno+"</td>"+
							"<td>"+name+"</td>"+
							"<td>"+Designation+"</td>"+
							"<td>"+dob+"</td>"+
							"<td>"+otherAttEntries.toString()+"</td>"+
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

			BusinessArea idcBa = BusinessArea.lookupBySystemPrefix(ID_sysprefix) ;
			if(null==idcBa)
				throw new TBitsException("invalid BA:"+ID_sysprefix);

			int idcSysId = idcBa.getSystemId() ;
			Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(idcSysId, user.getUserId());

			Field field = Field.lookupBySystemIdAndFieldName(idcSysId, ID_CV); 
			if(null==field)
				throw new TBitsException("invalid field:" + ID_CV);
			attachTable.put(field, allFilesInfo);

		   String allFilesJson = getAttachmentJson(idcBa,permTable,attachTable).toString() ;

			Hashtable<String,String> params = new Hashtable<String,String>();
			params.put(Field.DESCRIPTION,tabHtml.toString()) ;
			params.put(Field.ASSIGNEE, assignees );
			params.put("requestFiles", allFilesJson);
			params.put(ID_Source_Requests,srs.toString());
			params.put(ID_Linked_Requests, srs.toString());
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
		// TODO Auto-generated method stub
		return "IDProxyServlet";
	}

}
