package lancoCorres;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IProxyServlet;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.AddHtmlRequest;
import transbit.tbits.webapps.WebUtil;

public class IDPrefillProxyServlet implements IProxyServlet 
{
	public static String servletName = "IDPrefill" ;
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{		
		handleRequest(request,response) ;
	}
	
	private void handleRequest(HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException 
	{
		try	
		{
			User user = null ;
			try 
			{
				user = WebUtil.validateUser(request);
				if( null == user )
					throw new TBitsException("User not authorized for this action.") ;
			} 
			catch (DatabaseException e1) 
			{
				e1.printStackTrace();
				throw new TBitsException("User not authorized for this action.") ;
			}
			

			BusinessArea diba = BusinessArea.lookupBySystemPrefix(DIConstants.DI_SYSPREFIX) ;
			
			String sys_id = request.getParameter(DIConstants.CORR_SYS_ID) ;
			String request_id = request.getParameter(DIConstants.CORR_REQUEST_ID) ;
			BusinessArea clientba = null ;
			Request req = null ;
			try
			{
				int sysId = Integer.parseInt(sys_id) ;
				int requestId = Integer.parseInt(request_id) ;
				clientba = BusinessArea.lookupBySystemId(sysId) ;
				req = Request.lookupBySystemIdAndRequestId(sysId, requestId) ;
			}
			catch(NumberFormatException e )
			{
				e.printStackTrace() ;
				throw new TBitsException("Not found request with sys_id = " + sys_id + " and request_id = " + request_id ) ;
			}
		
			
			if( req == null || null == clientba )
			{
				throw new TBitsException("Request/BusinessArea not found with sys_id = " + sys_id + " and request_id = " + request_id ) ;
			}
			
            Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(diba.getSystemId(), user.getUserId(), true);
            
			String comProt = req.get(KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME) ;
			String corrType = req.get(KskConstants.CORR_TYPE_FIELD_NAME) ;
			String corrNo = req.get(KskConstants.CORR_CORRESPONDANCE_NUMBER_FIELD);
			Type corrStatus = Type.lookupBySystemIdAndFieldNameAndTypeName(diba.getSystemId(), DIConstants.DI_CORR_STATUS_FIELD_NAME, DIConstants.DI_CORR_STATUS_OPEN) ;
			
		//	System.out.println("\n\nMyMapToFieldValues:\n" + req.myMapFieldToValues + "\n\n************************************");
			
			String corrFile = req.getExString(KskConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;			
			String otherFile = req.get(KskConstants.CORR_OTHER_ATTACHMENTS_FIELD_NAME) ;
			if( null == corrFile || corrFile.trim().equals(""))
				corrFile = "[]" ;
			if( null == otherFile || otherFile.trim().equals(""))
				otherFile = "[]" ;
			
			System.out.println("Corr Files : " + corrFile + "\nOther Files : " + otherFile );
						
			String subject = req.get(Field.SUBJECT) ;
			
			String loggers = req.get(KskConstants.CORR_LOGGER_FIELD_NAME) ;
			ArrayList<String> loggerList = Utilities.toArrayList(loggers) ;
			
			String loggerLogin = "" ;
			if(loggerList != null && loggerList.size() > 0)
			 loggerLogin = loggerList.get(0) ;
			
			User loggerUser = User.lookupAllByUserLogin(loggerLogin) ;
			String initiator = null ;
			if( null != loggerUser )
			{
				Hashtable<String,String> loginInfo = UserInfoManager.getUserInfo(loggerUser.getUserId()) ;
				Field initField = Field.lookupBySystemIdAndFieldName(diba.getSystemId(), DIConstants.DI_CORR_SENT_BY_FIELD_NAME) ;
				if( null != initField  && null != loginInfo)
				{						
					ArrayList<Type> initTypes = Type.lookupAllBySystemIdAndFieldName(diba.getSystemId(), DIConstants.DI_CORR_SENT_BY_FIELD_NAME) ;
					for(Type t : initTypes)
					{				
			    		String name = t.getName() ;
			    		if( name.indexOf(loginInfo.get(UserInfoManager.FIRM)) >= 0  && name.substring(name.trim().length()-2).equals(loginInfo.get(UserInfoManager.LOCATION)))
			    		{
			    			initiator = t.getName() ;
			    			break ;
			    		}				    
					}
				}
			}
			
			String relatedReq =  clientba.getSystemPrefix() + "#" + req.getRequestId() + "#" + req.getMaxActionId() ;
			
			Collection<AttachmentInfo> corrAttach = AttachmentInfo.fromJson(corrFile);
			Collection<AttachmentInfo> otherAttach = AttachmentInfo.fromJson(otherFile) ;
			
			Field corrFileField = Field.lookupBySystemIdAndFieldName(diba.getSystemId(), DIConstants.DI_CORR_FILE_FIELD_NAME) ;
			Field otherFileField = Field.lookupBySystemIdAndFieldName(diba.getSystemId(), DIConstants.DI_OTHER_FILE_FIELD_NAME) ;
			Hashtable<Field,Collection<AttachmentInfo>> attachTable = new Hashtable<Field,Collection<AttachmentInfo>>() ;
			attachTable.put(corrFileField, corrAttach) ;
			attachTable.put(otherFileField,	otherAttach ) ;
			
			JsonObject rootNode = DIConstants.getAttachmentJson(diba, permTable, attachTable);
			String requestFiles = "[]" ;
			if( null != rootNode )
				 requestFiles = rootNode.toString() ;
			else
			{
				throw new TBitsException("Cannot add attachments to the prefill form") ;
			}
			
			Hashtable<String,String> prefillTable = new Hashtable<String,String>() ;
			if( null == comProt || null == corrType || null == initiator)
				throw new TBitsException("Cannot create prefilled request for sys_id =" + sys_id + " and request_id=" + request_id) ;
			
			
			prefillTable.put(DIConstants.DI_COMM_PROT_FIELD_NAME, comProt) ;
			prefillTable.put(DIConstants.DI_CORR_TYPE_FIELD_NAME, corrType ) ;
			prefillTable.put(DIConstants.DI_CORR_STATUS_FIELD_NAME, corrStatus.getName() ) ;
			prefillTable.put(DIConstants.DI_CORR_SENT_BY_FIELD_NAME, initiator) ;
			prefillTable.put(DIConstants.DI_SUBJECT_FIELD_NAME, subject) ;
			prefillTable.put(DIConstants.DI_RELATED_REQUEST_FIELD_NAME, relatedReq) ;
			prefillTable.put(DIConstants.DI_CORR_NUM_FIELD_NAME, corrNo);
			                     
			prefillTable.put("requestFiles", requestFiles) ;
			
			
			request.setAttribute(AddHtmlRequest.PREFILL_TABLE, prefillTable) ;
			RequestDispatcher rd = request.getRequestDispatcher(WebUtil.getNearestPath(request, "add-request/" + diba.getSystemPrefix() )) ;
			rd.forward(request, response);
			return ;
		}
		catch( TBitsException e)
		{
			e.printStackTrace() ;
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getDescription() ) ;
			return ;
		}
		catch( Exception e)
		{
			e.printStackTrace() ;
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage() ) ;
			return ;
		}	
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException			
	{
		handleRequest(request,response) ;
	}

	public String getName() {

		return servletName;
	}

}