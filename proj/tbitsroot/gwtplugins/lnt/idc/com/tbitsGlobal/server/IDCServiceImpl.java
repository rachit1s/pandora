package idc.com.tbitsGlobal.server;
import static idc.com.tbitsGlobal.server.IDCUtils.IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME;
import static idc.com.tbitsGlobal.server.IDCUtils.IDC_SOURCE_REQUESTS;
import static idc.com.tbitsGlobal.server.IDCUtils.PFIELD_LNT_NO;
import static idc.com.tbitsGlobal.server.IDCUtils.PFIELD_REVISION;
import static idc.com.tbitsGlobal.server.IDCUtils.PFIELD_VENDOR_NO;
import static idc.com.tbitsGlobal.server.IDCUtils.PFIELD_VENDOR_SUBMISSION_FILE;
import static idc.com.tbitsGlobal.server.IDCUtils.PFILED_TITLE;
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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
					"<th>Vendor No.</th>"+
					"<th>L&T No.</th>"+
					"<th>Revision</th>"+
					"<th>Title</th>"+
					"<th>Attachments</th>"+
			"</tr>");


			int sno=0;
			int count = 1 ;
			for (Integer reqId : reqIdList)
			{
				try {
					sno++;
					Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), reqId);
					srs.append(sysPrefix+"#"+reqId+",");
					
					String lntNo="";
					String vendorNo="";
					String revision="";
					String title = "";
					
					try{
					lntNo=req.get(PFIELD_LNT_NO);
					vendorNo=req.get(PFIELD_VENDOR_NO);
					revision=req.get(PFIELD_REVISION);					
					title = req.get(PFILED_TITLE);
					}
					catch(Exception n){
						n.printStackTrace();
					}
					String otherFiles = req.get(PFIELD_VENDOR_SUBMISSION_FILE);
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
								"read-attachment/"+tba+
								"?filerepoid="+atInfo.repoFileId+"&saveAs=true>"+atInfo.name+"</a></td></tr>");
					}
					otherAttEntries.append("</table>");

					tabHtml.append(
							"<tr>"+
							"<td>"+sno+"</td>"+
							"<td>"+lntNo+"</td>"+
							"<td>"+vendorNo+"</td>"+
							"<td>"+revision+"</td>"+
							"<td>"+title+"</td>"+
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
		//	ttrd.set(Field.DESCRIPTION,tabHtml.toString());
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
