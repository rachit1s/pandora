package lntFcnDcn;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;
import dcn.com.tbitsGlobal.server.ChangeNoteServiceImpl;
import dcn.com.tbitsGlobal.server.utils.ServerUtilities;
import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

public class FCNDCNAutoGenerationDocRule implements IRule {

	public static String DCN_SUFFIX = "_DCN";

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		String baPrefix = ba.getSystemPrefix();
		if (baPrefix.endsWith(DCN_SUFFIX)) {
			// TODO Auto-generated method stub
			String dcn_Status = currentRequest.get("dcn_status");
			if (dcn_Status.equals("approved") || dcn_Status.equals("closed")) {
				ChangeNoteServiceImpl b = new ChangeNoteServiceImpl();
				ArrayList<ChangeNoteConfig> allBas = new ArrayList<ChangeNoteConfig>();
				allBas = b.lookupAllChangeNoteConfig();
				ChangeNoteConfig cnc = checkForBAS(allBas, ba.getSystemPrefix());
				if (cnc != null) {
					String url = generatePDF(ba, currentRequest.getRequestId(),
							cnc, currentRequest, isAddRequest);
					File f = new File(url);
					transbit.tbits.common.Uploader u = new transbit.tbits.common.Uploader(
							currentRequest.getRequestId(),
							currentRequest.getMaxActionId(),
							ba.getSystemPrefix());
					AttachmentInfo uploadedAttachment = u.moveIntoRepository(f);
					ArrayList<AttachmentInfo> b1 = new ArrayList<AttachmentInfo>();
					b1.add(uploadedAttachment);
					currentRequest.setAttachments(b1);
					return new RuleResult(true);
				} else
					return new RuleResult(true,
							"There is no Configuration for Doc Generation in this "
									+ ba.getSystemPrefix());
			}
		} else
			return new RuleResult(true, "This Rule is Not Applicable to BA: "
					+ ba.getSystemPrefix());
		return new RuleResult(true);
	}

	private String generatePDF(BusinessArea ba, Integer requestId,
			ChangeNoteConfig cnc, Request currentRequest, boolean isAddRequest) {
		// TODO Auto-generated method stub
		ArrayList<Request> srcReqList;
		try {
			srcReqList = ServerUtilities
					.getSourceRequestsListFromRelatedRequests(currentRequest);
			return generatePDFUsingBirt(ba, currentRequest, srcReqList, cnc,
					isAddRequest);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	private String generatePDFUsingBirt(BusinessArea ba,
			Request currentRequest, ArrayList<Request> srcReqList,
			ChangeNoteConfig cnc, boolean isAddrequest) {
		// TODO Auto-generated method stub
		// String pdfUrl = "";
		TBitsReportEngine tBitsEngine;
		try {
			tBitsEngine = TBitsReportEngine.getInstance();

			Map<Object, Object> reportVariables = new HashMap<Object, Object>();
			Map<String, Object> reportParams = new HashMap<String, Object>();
			// ServerUtilities r=new ServerUtilities();
			// reportParams.put(r.re, request.getRequestId());
			reportParams.put("request_id", currentRequest.getRequestId());
			reportParams.put("sys_id", currentRequest.getSystemId());
			int reqid = 0;
			File tempDir = Configuration.findPath("webapps/tmp");
			String outputFileName = cnc.getBaType().replaceAll("[^A-Za-z0-9]+",
					"_");
			String pdfFilePath = tempDir + File.separator + outputFileName
					+ "_" + currentRequest.getRequestId() + ".pdf";
			File outFile = new File(pdfFilePath);
			String reqno = "";
			Formatter format1 = new Formatter();
			if (isAddrequest) {
				try {
					Connection con = DataSourcePool.getConnection();
					String sql = "Select max(request_id) from requests where sys_id=? ";
					PreparedStatement ps = con.prepareStatement(sql);
					ps.setInt(1, currentRequest.getSystemId());
					java.sql.ResultSet rs = ps.executeQuery();
					if (rs.next())
						reqid = rs.getInt(1);

					ps.close();
					con.close();
					format1.format("%04d", reqid + 1);
					reqno = format1.toString();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			reportVariables
					.put(ServerUtilities.REQUEST_HANDLER, currentRequest);
			reportVariables.put("BA", ba);
			reportVariables.put("SourceRequests", srcReqList);
			if (isAddrequest) {
				reportVariables.put("req", reqno);
				// reportVariables.put("impno", format.toString());
			} else {
				format1.format("%04d", currentRequest.getRequestId());
				reportVariables.put("req", format1.toString());
			}
			// reportVariables.put("logger", arg1);
			// String abv=currentRequest.get("due_datetime");
			if ((srcReqList != null) && (srcReqList.size() != 0)) {
				int srcSystemId = srcReqList.get(0).getSystemId();
				BusinessArea srcBA = null;
				try {
					srcBA = BusinessArea.lookupBySystemId(srcSystemId);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				if (srcBA != null) {
					reportVariables.put("Project", srcBA.getDescription());

					Hashtable<String, String> changeNoteFieldMap = ServerUtilities
							.getChangeNoteFieldMap(srcBA.getSystemId());
					if (changeNoteFieldMap != null)
						reportVariables.put("FieldMap", changeNoteFieldMap);
				}

			}

			ArrayList<RequestUser> loggers = (ArrayList<RequestUser>) currentRequest
					.getLoggers();
			if ((loggers != null) && loggers.size() != 0) {
				User logger = null;
				try {
					logger = loggers.get(0).getUser();
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				reportVariables.put("Logger", logger);
			}
			ArrayList<RequestUser> assignees = (ArrayList<RequestUser>) currentRequest
					.getAssignees();
			if ((assignees != null) && assignees.size() != 0) {
				User assignee = null;
				try {
					assignee = assignees.get(0).getUser();
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				reportVariables.put("Assignee", assignee);
			}

			ArrayList<RequestUser> subscribers = (ArrayList<RequestUser>) currentRequest
					.getSubscribers();
			// subscribers.addAll(assignees);

			ArrayList<User> ab = new ArrayList<User>();
			for (RequestUser ru : subscribers) {
				try {
					ab.add(ru.getUser());
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			for (RequestUser ru : assignees) {
				try {

					ab.add(ru.getUser());

				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			reportVariables.put("circulation", ab);

			tBitsEngine.generatePDFFile("auto" + cnc.getTemplateName(),
					reportVariables, reportParams, outFile);
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

		for (ChangeNoteConfig a : allBas) {
			if (a.getTargetSysPrefix().equalsIgnoreCase(systemPrefix))
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
