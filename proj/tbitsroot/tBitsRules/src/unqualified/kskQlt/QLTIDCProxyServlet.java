package kskQlt;

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

import kskQlt.QLTIDCConstants;
import kskQlt.QltConstants;
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

public class QLTIDCProxyServlet implements IProxyServlet {

	private void handleGetRequest(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{
		HttpSession session = request.getSession();
		try 
		{
			// get the request nos. from the httprequest
			String reqIds = request.getParameter("requestList");
			ArrayList<String> reqIdList = Utilities.toArrayList(reqIds) ;
			String sysPrefix = request.getParameter("dcrBA");
            String pgbr="<br><br><br><br>";
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
			tabHtml.append("<table border = '1' style='border-collapse:collapse'>"+
					"<tr>"+
					"<th>S.No.</th>"+
					"<th>Vendor Name</th>"+
					"<th>Inspection Call#</th>"+
					"<th>Lloyds Report#</th>"+
					"<th>Decision</th>"+
					"<th>Inspection Call File</th>"+
					"<th>Lloyds Report File Link</th>"+
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
					String vendor=req.get(Field.LOGGER);
					String inspcallno=req.get(QltConstants.QLT_INSP_NO_FIELD_NAME);
					String lldocno=req.get(QltConstants.QLT_LLOYDS_DOC_NO_FIELD_NAME);
					String decision=req.get(QltConstants.QLT_DECISION_FIELD_NAME);
					String inspfile = req.getExString(QltConstants.QLT_INSP_FILE_FIELD_NAME);
					String llrepfile = req.getExString(QltConstants.QLT_LLOYDS_FILE_FIELD_NAME);
				    Collection<AttachmentInfo> subFileList = AttachmentInfo.fromJson(inspfile) ;
					Collection<AttachmentInfo> otherFileList = AttachmentInfo.fromJson(llrepfile);

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
								"read-attachment/"+QLTIDCConstants.IDC_SYSPREFIX+
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
								"read-attachment/"+QLTIDCConstants.IDC_SYSPREFIX+
								"?filerepoid="+atInfo.repoFileId+"&saveAs=true>"+atInfo.name+"</a></td></tr>");
					}
					otherAttEntries.append("</table>");

					tabHtml.append(
							"<tr>"+
							"<td>"+sno+"</td>"+
							"<td>"+vendor+"</td>"+
							"<td>"+inspcallno+"</td>"+
							"<td>"+lldocno+"</td>"+
							"<td>"+decision+"</td>"+
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

			BusinessArea idcBa = BusinessArea.lookupBySystemPrefix(QLTIDCConstants.IDC_SYSPREFIX) ;
			if(null==idcBa)
				throw new TBitsException("invalid BA:"+QLTIDCConstants.IDC_SYSPREFIX);

			int idcSysId = idcBa.getSystemId() ;
			Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(idcSysId, user.getUserId());

			Field field = Field.lookupBySystemIdAndFieldName(idcSysId, QLTIDCConstants.IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME); 
			if(null==field)
				throw new TBitsException("invalid field:" +QLTIDCConstants.IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME);
			attachTable.put(field, allFilesInfo);

			Field field1 = Field.lookupBySystemIdAndFieldName(idcSysId, QLTIDCConstants.IDC_IDC_COMMENTED_FILES);

			attachTable.put(field1, new ArrayList<AttachmentInfo>() ) ;

			String allFilesJson = QLTIDCConstants.getAttachmentJson(idcBa,permTable,attachTable).toString() ;

			Hashtable<String,String> params = new Hashtable<String,String>();
			params.put(Field.DESCRIPTION,tabHtml.toString()) ;
			params.put(Field.ASSIGNEE, assignees );
			params.put("requestFiles", allFilesJson);
			params.put(QLTIDCConstants.IDC_SOURCE_REQUESTS,srs.toString());
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
		return "QLTIDCProxyServlet";
	}

}
