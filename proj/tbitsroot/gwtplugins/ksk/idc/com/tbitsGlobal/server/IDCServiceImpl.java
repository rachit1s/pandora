package idc.com.tbitsGlobal.server;
import static idc.com.tbitsGlobal.server.IDCUtils.IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME;
import static idc.com.tbitsGlobal.server.IDCUtils.IDC_SOURCE_REQUESTS;
import static idc.com.tbitsGlobal.server.IDCUtils.PGBR;
import static idc.com.tbitsGlobal.server.IDCUtils.getAllSrcBAs;
import static idc.com.tbitsGlobal.server.IDCUtils.getAllTargetBAs;
import static idc.com.tbitsGlobal.server.IDCUtils.getTargetBa;
import static idc.com.tbitsGlobal.server.IDCUtils.toAttList;
import idc.com.tbitsGlobal.client.IDCService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.tbitsGlobal.jaguar.client.serializables.RequestData;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.webapps.WebUtil;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

public class IDCServiceImpl extends TbitsRemoteServiceServlet implements IDCService {
	public RequestData getIDCRequest(ArrayList<Integer> param,String sysPrefix) {
		try{
			// TODO Auto-generated method stub

			ArrayList<Integer> reqIdList = param;
			BusinessArea ba = null ;
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if(ba==null)
				throw new TBitsException("invalid BA");
			HttpServletRequest request=this.getRequest();
			User user  = WebUtil.validateUser(request);

			String tba=getTargetBa(sysPrefix);
			BusinessArea baidc=BusinessArea.lookupBySystemPrefix(tba);

			// create a hashtable<String,String> params for all the parameters in the request you want to fill
			HashSet<String> assSet = new HashSet<String>() ;           
			ArrayList<AttachmentInfo> allFilesInfo = new ArrayList<AttachmentInfo>() ;
			StringBuilder tabHtml= new StringBuilder();
			StringBuilder srs=new StringBuilder();
			tabHtml.append(PGBR);
			
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
			for (Integer reqId : reqIdList)
			{
				try {
					sno++;
					Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), reqId);
					srs.append(sysPrefix+"#"+reqId+",");
					
					String docno="";
					String wpclno="";
					String rev="";
					String desc="";
					String idtn="";
					String sepSubFiles="";
					String otherFiles="";
					
					
					try{
					 docno=req.get(IDCUtils.SEPCO_DOCUMENT_NUMBER);
					 wpclno=req.get(IDCUtils.SEPCO_WPCL_NUMBER);
					 rev =req.get(IDCUtils.SEPCO_REVISION);
					 desc = req.get(Field.SUBJECT);
					 idtn=req.get(IDCUtils.IDC_INWARD_DTN); 
					 sepSubFiles = req.get(IDCUtils.SEPCO_SUBMISSION_FILE);
					 otherFiles = req.get(Field.ATTACHMENTS);
					
					}
					catch(Exception n){
						n.printStackTrace();
					}
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
								"read-attachment/"+IDCUtils.IDC_SYSPREFIX+
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
								"read-attachment/"+IDCUtils.IDC_SYSPREFIX+
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

			tabHtml.append("</table>").append(PGBR);

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


			TbitsTreeRequestData ttrd = new TbitsTreeRequestData() ;
			ttrd.set(Field.DESCRIPTION,tabHtml.toString());
			ttrd.set(Field.ASSIGNEE,assignees);
			ttrd.set(IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME,toAttList(allFilesInfo));
			ttrd.set(IDC_SOURCE_REQUESTS,srs.toString());  
			ArrayList<BAField> baf= new ArrayList<BAField>();
			try {
				baf=(ArrayList<BAField>)GWTServiceHelper.getFields(baidc, user);
			} catch (TbitsExceptionClient e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ArrayList<DisplayGroupClient> dgc = new ArrayList<DisplayGroupClient>();
			try {
				dgc=(ArrayList<DisplayGroupClient>)GWTServiceHelper.getDisplayGroups(baidc);
			} catch (TbitsExceptionClient e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			RequestData rd = new RequestData(tba,0,ttrd,dgc,baf);
			return rd;


		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();


		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return null;

	}

	public ArrayList<String> getValidBAs() throws TbitsExceptionClient {
		try {
			return getAllTargetBAs();
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
	public ArrayList<String> getValidSrcBAs() throws TbitsExceptionClient {
		try {
			return getAllSrcBAs();
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

};
