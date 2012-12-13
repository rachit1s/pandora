package imp;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.google.gwt.gears.client.database.ResultSet;

import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

import dcn.com.tbitsGlobal.client.ChangeNoteConstants;
import dcn.com.tbitsGlobal.client.plugins.form.ChangeNoteViewRequestForm;
import dcn.com.tbitsGlobal.client.utils.ChangeNoteClientUtils;
import dcn.com.tbitsGlobal.server.ChangeNoteServiceImpl;
import dcn.com.tbitsGlobal.server.utils.ServerUtilities;
import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.authentication.RequestWrapper;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;


public class ImpAutoGenerationDoc  implements IRule{
	public static String TQ_SUFFIX="_TechQuery"; 
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		// TODO Auto-generated method stub
			
			ChangeNoteServiceImpl b=new ChangeNoteServiceImpl();
			ArrayList<ChangeNoteConfig> allBas=new ArrayList<ChangeNoteConfig>();
			allBas=b.lookupAllChangeNoteConfig();
			ChangeNoteConfig cnc=checkForBAS(allBas,ba.getSystemPrefix());
			if(cnc!=null){
			String url=generateDOC(ba.getSystemId(),currentRequest.getRequestId(),cnc,currentRequest,isAddRequest);
			File f=new File(url);
			transbit.tbits.common.Uploader u=new transbit.tbits.common.Uploader(currentRequest.getRequestId(),currentRequest.getMaxActionId(),ba.getSystemPrefix());
			AttachmentInfo uploadedAttachment=u.copyIntoRepository(f);
			ArrayList<AttachmentInfo> b1=new ArrayList<AttachmentInfo>();
			b1.add(uploadedAttachment);
			currentRequest.setAttachments(b1);		
			return new RuleResult(true);
			}
			else
				return new RuleResult(true);
	}

	private String generateDOC(int systemId, Integer requestId,
			ChangeNoteConfig cnc,Request currentRequest,boolean isAddRequest) {
		// TODO Auto-generated method stub
		ArrayList<Request> srcReqList;
		try {
			srcReqList = ServerUtilities.getSourceRequestsListFromRelatedRequests(currentRequest);
			 return generateDOCUsingBirt(currentRequest, 
					srcReqList, cnc,isAddRequest);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		
		
	return "";	
	}

	private String generateDOCUsingBirt(Request currentRequest,
			ArrayList<Request> srcReqList, ChangeNoteConfig cnc,boolean isAddrequest) {
		// TODO Auto-generated method stub
		String pdfUrl = "";
		TBitsReportEngine tBitsEngine;
		try {
			tBitsEngine = TBitsReportEngine.getInstance();
		
		Map<Object, Object> reportVariables = new HashMap<Object, Object>();
		Map<String, Object> reportParams = new HashMap<String, Object>();
		ServerUtilities r=new ServerUtilities();
		//reportParams.put(r.re, request.getRequestId());
		reportParams.put("request_id", currentRequest.getRequestId());
		reportParams.put("sys_id", currentRequest.getSystemId());
		int reqid=0;
  		File tempDir = Configuration.findPath("webapps/tmp");
		String outputFileName = cnc.getBaType().replaceAll("[^A-Za-z0-9]+", "_");		
		String pdfFilePath = tempDir + File.separator + outputFileName + "_" + currentRequest.getRequestId() + ".doc";
		File outFile = new File(pdfFilePath);
		if(isAddrequest){
		try {
			Connection con=DataSourcePool.getConnection();
		
		String sql="Select max(request_id) from requests where sys_id=? ";
		PreparedStatement ps=con.prepareStatement(sql);
		ps.setInt(1, currentRequest.getSystemId());
		java.sql.ResultSet rs=ps.executeQuery();
		if(rs.next())
			reqid=rs.getInt(1);
		ps.close();
		con.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		}
		String  reqno=reqid+1 + "";
		reportVariables.put(ServerUtilities.REQUEST_HANDLER, currentRequest);
		reportVariables.put("SourceRequests", srcReqList);
		if(isAddrequest)
		reportVariables.put("req", reqno);
		else
			reportVariables.put("req", currentRequest.getRequestId()+"");
		//reportVariables.put("logger", arg1);
		String abv=currentRequest.get("due_datetime");
		if ((srcReqList != null) && (srcReqList.size() != 0)) {
			int srcSystemId = srcReqList.get(0).getSystemId();
			BusinessArea srcBA = null;
			try {
				srcBA = BusinessArea.lookupBySystemId(srcSystemId);				
			} catch (DatabaseException e) {
				e.printStackTrace();				
			}
			if (srcBA != null){
				reportVariables.put("Project", srcBA.getDescription());
				
				Hashtable<String, String> changeNoteFieldMap = ServerUtilities.getChangeNoteFieldMap(srcBA.getSystemId());
				if (changeNoteFieldMap != null)
					reportVariables.put("FieldMap", changeNoteFieldMap);
			}
			
		}
				
		ArrayList<RequestUser> assignees = (ArrayList<RequestUser>)currentRequest.getAssignees();
		if ((assignees != null) && assignees.size() != 0){
			User assignee = null;
			try {
				assignee = assignees.get(0).getUser();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			reportVariables.put("Assignee", assignee);
		}
		ArrayList<RequestUser> subscribers=(ArrayList<RequestUser>) currentRequest.getSubscribers();
		//subscribers.addAll(assignees);
		
		ArrayList<User> ab=new ArrayList<User>();
		for(RequestUser ru:subscribers){
			try {
				ab.add(ru.getUser());
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		for(RequestUser ru:assignees){
			try {
				
				ab.add(ru.getUser());
				
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("adjkasbdfjsf"+currentRequest.getDueDate());
		System.out.println(ab);
		reportVariables.put("circulation", ab);
	
			File generatedPDFFile = tBitsEngine.generateDOCFile("utotechnical_query.rptdesign", reportVariables,
					reportParams, outFile);
			return pdfFilePath;
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	return "";
	}

	private ChangeNoteConfig checkForBAS(ArrayList<ChangeNoteConfig> allBas,
			String systemPrefix) {
		// TODO Auto-generated method stub
		for(ChangeNoteConfig a:allBas){
			if (a.getTargetSysPrefix().trim().equals(systemPrefix))
				return a;
		}
		return null;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
