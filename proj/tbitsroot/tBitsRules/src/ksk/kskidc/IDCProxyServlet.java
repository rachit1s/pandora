package kskidc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kskidc.IDCConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IProxyServlet;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.AddHtmlRequest;
import transbit.tbits.webapps.WebUtil;

public class IDCProxyServlet implements IProxyServlet {
	
    String pgbr="<br><br><br><br>";

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
					"<th>Inward DTN</th>"+
					"<th>Description</th>"+
					"<th>WPCL number</th>"+
					"<th>Document Number</th>"+
					"<th>Revision</th>"+
					"<th>SEPCO Submission File</th>"+
					"<th>Other Attachments</th>"+
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
					String docno=req.get(IDCConstants.SEPCO_DOCUMENT_NUMBER);
					String wpclno=req.get(IDCConstants.SEPCO_WPCL_NUMBER);
					String rev =req.get(IDCConstants.SEPCO_REVISION);
					String desc = req.get(Field.SUBJECT);
					String idtn=req.get(IDCConstants.IDC_INWARD_DTN); 
					String sepSubFiles = req.getExString(IDCConstants.SEPCO_SUBMISSION_FILE);
					String otherFiles = req.get(Field.ATTACHMENTS);
					Collection<AttachmentInfo> subFileList = AttachmentInfo.fromJson(sepSubFiles) ;
					Collection<AttachmentInfo> otherFileList = AttachmentInfo.fromJson(otherFiles) ;

					StringBuilder subAttEntries=new StringBuilder();
					StringBuilder otherAttEntries=new StringBuilder();

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
								"read-attachment/"+IDCConstants.IDC_SYSPREFIX+
								"?filerepoid="+atInfo.repoFileId+"&saveAs=true>"+atInfo.name+"</a></td></tr>");




					}
					subAttEntries.append("</table>");


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
								"read-attachment/"+IDCConstants.IDC_SYSPREFIX+
								"?filerepoid="+atInfo.repoFileId+"&saveAs=true>"+atInfo.name+"</a></td></tr>");
					}
					otherAttEntries.append("</table>");

					tabHtml.append(
							"<tr>"+
							"<td>"+sno+"</td>"+
							"<td>"+idtn+"</td>"+
							"<td>"+desc+"</td>"+
							"<td>"+wpclno+"</td>"+
							"<td>"+docno+"</td>"+
							"<td>"+rev+"</td>"+
							"<td>"+subAttEntries.toString()+"</td>"+
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

			BusinessArea idcBa = BusinessArea.lookupBySystemPrefix(IDCConstants.IDC_SYSPREFIX) ;
			if(null==idcBa)
				throw new TBitsException("invalid BA:"+IDCConstants.IDC_SYSPREFIX);

			int idcSysId = idcBa.getSystemId() ;
			Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(idcSysId, user.getUserId());

			Field field = Field.lookupBySystemIdAndFieldName(idcSysId, IDCConstants.IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME); 
			if(null==field)
				throw new TBitsException("invalid field:" +IDCConstants.IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME);
			attachTable.put(field, allFilesInfo);

			Field field1 = Field.lookupBySystemIdAndFieldName(idcSysId, IDCConstants.IDC_IDC_COMMENTED_FILES);

			attachTable.put(field1, new ArrayList<AttachmentInfo>() ) ;

			String allFilesJson = IDCConstants.getAttachmentJson(idcBa,permTable,attachTable).toString() ;

			Hashtable<String,String> params = new Hashtable<String,String>();
			params.put(Field.DESCRIPTION,tabHtml.toString()) ;
			params.put(Field.ASSIGNEE, assignees );
			params.put("requestFiles", allFilesJson);
			params.put(IDCConstants.IDC_SOURCE_REQUESTS,srs.toString());
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
		//          catch (ServletException e) {
		//		// TODO Auto-generated catch block
		//		e.printStackTrace();
		//	     } catch (IOException e) {
		//		// TODO Auto-generated catch block
		//		e.printStackTrace();
		//	}


	}
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException 
	{

		handleGetRequest(request,response);

	}




	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		doGet(request,response) ;

	}

	public String getName() {
		// TODO Auto-generated method stub
		return "IDCProxyServlet";
	}

}
