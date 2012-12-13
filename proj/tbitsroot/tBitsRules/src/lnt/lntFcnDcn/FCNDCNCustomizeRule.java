package lntFcnDcn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import commons.com.tbitsGlobal.utils.client.log.Log;

import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.searcher.DqlSearcher;

public class FCNDCNCustomizeRule implements IPostRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntFcnDcn");
	private static final String TBITS_ROOT = "root";
	public static String FCN_SUFFIX = "_FCN";
	public static String DCN_SUFFIX = "_DCN";
	public static String TQ_SUFFIX = "_TECHQUERY";

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		if (ba != null) {
			String baPrefix = ba.getSystemPrefix();
			String dcn_Status = "";
			if(baPrefix.endsWith(FCN_SUFFIX) || baPrefix.endsWith(DCN_SUFFIX))
				dcn_Status= currentRequest.get("dcn_status");
			if (isAddRequest == true) {
				if (baPrefix.endsWith(FCN_SUFFIX)
						&& (dcn_Status.equals("open"))) {
					String description = "FCN Created via  ["
							+ ba.getSystemPrefix() + "#"
							+ currentRequest.getRequestId() + "]";
					updateSourceRequests(connection, currentRequest,
							description, ba);
				} else if (baPrefix.endsWith(DCN_SUFFIX)
						&& (dcn_Status.equals("open"))) {
					String description = "DCN Created via  ["
							+ ba.getSystemPrefix() + "#"
							+ currentRequest.getRequestId() + "]";
					updateSourceRequests(connection, currentRequest,
							description, ba);
				} else if (baPrefix.endsWith(TQ_SUFFIX)
						|| baPrefix.endsWith("_TQ")
						|| baPrefix.endsWith("_TechQuery")
						|| baPrefix.endsWith("_Tech_Query")) {
					String description = "Tech Query  Created via  ["
							+ ba.getSystemPrefix() + "#"
							+ currentRequest.getRequestId() + "]";
					updateSourceRequests(connection, currentRequest,
							description, ba);
				}
			} else if (isAddRequest == false) {
				if (dcn_Status.equals("approved")) {

					if (baPrefix.endsWith(FCN_SUFFIX)) {
						String description = "FCN Approved via  ["
								+ ba.getSystemPrefix() + "#"
								+ currentRequest.getRequestId() + "]";
						updateSourceRequests(connection, currentRequest,
								description, ba);
					} else if (baPrefix.endsWith(DCN_SUFFIX)) {
						String description = "DCN Approved via  ["
								+ ba.getSystemPrefix() + "#"
								+ currentRequest.getRequestId() + "]";
						updateSourceRequests(connection, currentRequest,
								description, ba);
					}

				} else if (dcn_Status.equals("closed")) {
					if (baPrefix.endsWith(FCN_SUFFIX)) {
						String description = "FCN Rejected via  ["
								+ ba.getSystemPrefix() + "#"
								+ currentRequest.getRequestId() + "]";
						updateSourceRequests(connection, currentRequest,
								description, ba);
					} else if (baPrefix.endsWith(DCN_SUFFIX)) {
						String description = "DCN Rejected via  ["
								+ ba.getSystemPrefix() + "#"
								+ currentRequest.getRequestId() + "]";
						updateSourceRequests(connection, currentRequest,
								description, ba);
					}
				}
			}
			return new RuleResult(true, this.getName() + "completed", true);
		} else
			return new RuleResult(true, this.getName()
					+ " : ba passed was null.", false);

	}

	private void updateSourceRequests(Connection connection,
			Request currentRequest, String description, BusinessArea ba) {

		String logger = getUpdateLogger(currentRequest);

		ArrayList<Request> sourceRequestList;
		try {
			sourceRequestList = getSourceRequestsListFromRelatedRequests(
					connection, currentRequest, ba, logger);

			for (Request req : sourceRequestList) {
				Hashtable<String, String> params = new Hashtable<String, String>();
				params.put(Field.DESCRIPTION, description);
				params.put(Field.REQUEST, req.getRequestId() + "");
				params.put(Field.USER, logger);
				params.put(Field.BUSINESS_AREA, req.getSystemId() + "");
				UpdateRequest updater = new UpdateRequest();
				try {
					updater.updateRequest(params);
				} catch (APIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private ArrayList<Request> getSourceRequestsListFromRelatedRequests(
			Connection connection, Request request, BusinessArea ba,
			String logger) throws DatabaseException, TBitsException {

		String srcSysPrefix = null;
		String srcSysPrefixes = null;
		BusinessArea srcBA = null;
		ArrayList<Request> srcReqList = new ArrayList<Request>();
		Request srcAllBARequest = null;
		String relatedRequests = request.getRelatedRequests();
		String docNo = "";
		Field documentNo = null;
		String commaSeperatedListOfBAs = "";
		String[] srcRequestsSmartTags = relatedRequests.split(",");
		for (String srcReqSmartTag : srcRequestsSmartTags) {

			int requestId = 0;
			String[] part = srcReqSmartTag.split("#");
			if (part != null) {
				if (srcSysPrefix == null) {
					srcSysPrefix = part[0];
					srcBA = BusinessArea.lookupBySystemPrefix(srcSysPrefix);
					if (srcBA == null)
						throw new TBitsException("Invalid business area: "
								+ srcSysPrefix);

					requestId = Integer.parseInt(part[1]);

					if (requestId > 0) {
						Request tmpRequest = Request
								.lookupBySystemIdAndRequestId(
										srcBA.getSystemId(), requestId);

						String docNoName = getDocNoName(connection, srcBA);
						int docNum = Integer.parseInt(docNoName);

						documentNo = Field.lookupBySystemIdAndFieldId(
								srcBA.getSystemId(), docNum);
						docNo = tmpRequest.get(documentNo.getName());
						if (documentNo == null) {
							throw new TBitsException("Invalid field: " + docNo);
						}

						srcReqList.add(tmpRequest);
						commaSeperatedListOfBAs = getCommaSeperatedListOfBAs(
								connection, srcBA, ba);
					}
				}

			}

		}
		if ((commaSeperatedListOfBAs != null)
				&& (commaSeperatedListOfBAs.trim().length() != 0)) {
			String[] srcBAs = commaSeperatedListOfBAs.split(",");
			for (String srcSysPrfix : srcBAs) {
				if (!srcSysPrefix.equalsIgnoreCase(srcSysPrefixes)) {
					srcAllBARequest = getRequest(connection, docNo, documentNo,
							srcSysPrfix, logger);

				}

				srcReqList.add(srcAllBARequest);
			}
		}
		return srcReqList;
	}

	private String getDocNoName(Connection connection, BusinessArea srcBA) {
		// select * from trn_process_parameters where src_sys_id = 2 and
		// parameter = 'uniqueTransmittalNumberFieldId'

		String fieldId = "";
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("select * from trn_process_parameters where src_sys_id =? and parameter = 'uniqueTransmittalNumberFieldId'");
			ps.setInt(1, srcBA.getSystemId());
			ResultSet rs;
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next() != false) {
					fieldId = rs.getString("value");
				}
			}
			if (rs != null)
				rs.close();

			if (ps != null)
				ps.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.info("Exception while contacting the database....");
		}
		return fieldId;
	}

	private Request getRequest(Connection connection, String docNo,
			Field documentNo, String srcSysPrfix, String logger)
			throws DatabaseException, TBitsException {

		BusinessArea srcBA = BusinessArea.lookupBySystemPrefix(srcSysPrfix);
		if (srcBA == null)
			throw new TBitsException("Invalid business area: " + srcSysPrfix);

		User superuser = User.lookupByUserLogin(logger);
		Request req = lookUpRequestBySysIDAndDrawingNo(connection, srcBA,
				documentNo, docNo, superuser);

		return req;
	}

	private Request lookUpRequestBySysIDAndDrawingNo(Connection connection,
			BusinessArea srcBA, Field documentNo, String docNo, User superUser)
			throws DatabaseException {
		// SELECT sys_id, request_id WHERE DrawingNo:"45678910" ORDER BY
		// request_id DESC

		String dql = "SELECT sys_id, request_id  WHERE " + documentNo.getName()
				+ ":" + "\"" + docNo + "\" ORDER BY request_id DESC";
		// "dueby:yesterday";
		DqlSearcher searcher = new DqlSearcher(srcBA.getSystemId(),
				superUser.getUserId(), dql);
		try {
			searcher.search();
		} catch (Exception e1) {
			e1.printStackTrace();
			LOG.severe("Exception occurred while searching.");
		}

		ArrayList<Integer> reqIds = new ArrayList<Integer>();
		if (searcher.getResult().containsKey(srcBA.getSystemId())) {
			Collection<Integer> requestIdsFetchedColl = searcher.getResult()
					.get(srcBA.getSystemId()).keySet();
			if (requestIdsFetchedColl != null) {
				reqIds.addAll(requestIdsFetchedColl);
			}
		}

		Request requst = null;
		for (Integer req : reqIds) {
			requst = Request.lookupBySystemIdAndRequestId(srcBA.getSystemId(),
					req);
		}
		return requst;
	}

	private String getCommaSeperatedListOfBAs(Connection connection,
			BusinessArea targetBa, BusinessArea srcBa) {

		String commaSeperatedListOfBAs = "";
		int change_note_id = 0;
		try {

			connection = DataSourcePool.getConnection();
			PreparedStatement ps1 = connection
					.prepareStatement("select change_note_id from trn_change_note_configuration where target_sys_prefix = ? and src_sys_prefix = ?");
			ps1.setString(1, srcBa.getSystemPrefix());
			ps1.setString(2, targetBa.getSystemPrefix());
			ResultSet rs1;
			rs1 = ps1.executeQuery();
			if (rs1 != null) {
				while (rs1.next() != false) {
					change_note_id = rs1.getInt(1);
				}
			}
			PreparedStatement ps = connection
					.prepareStatement("select update_sys_prefix from trn_change_note_configuration where change_note_id = ?");
			ps.setInt(1, change_note_id);
			ResultSet rs;
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next() != false) {
					commaSeperatedListOfBAs = rs.getString(1);
				}
			}
			if (rs != null)
				rs.close();

			if (ps != null)
				ps.close();

			if (rs1 != null)
				rs1.close();

			if (ps1 != null)
				ps1.close();
		} catch (SQLException e) {
			e.printStackTrace();
			LOG.info("Exception while contacting the database....");
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				LOG.info("Exception while closing the connection");
			}
		}
		return commaSeperatedListOfBAs;
	}

	private String getUpdateLogger(Request currentRequest) {

		String logger = new String();
		Collection<RequestUser> loggerList = currentRequest.getLoggers();
		Iterator<RequestUser> i = loggerList.iterator();
		if (i.hasNext()) {
			try {
				logger = i.next().getUser().getUserLogin();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (logger == null)
			logger = TBITS_ROOT;
		return logger;

	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "FCNDCNCustomizeRule";
	}

}
