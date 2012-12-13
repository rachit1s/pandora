package common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.dql.antlr.DQLParser.fieldName_return;

public class LoggerAndDateRule implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("techPort");

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		if (isAddRequest) {
			ArrayList<RequestUser> requestUser = lookUpBySysId(
					ba.getSystemId(), connection);

			for (RequestUser ru : requestUser) {
				int user_id = ru.getUserId();
				// String userId = currentRequest.get(Field.LOGGER);
				// int ui = Integer.parseInt(userId);
				int currentUser = user.getUserId();
				if (user_id == currentUser) {
					ArrayList<Request> request = lookUpBySysIdAndUserId(
							ba.getSystemId(), currentUser, connection);
					for (Request req : request) {
						Date dueDate = req.getDueDate();
						// Calendar newDueDate = (Calendar) dueDate.clone();
						Calendar newDueDate = Calendar.getInstance();
						newDueDate.setTime(dueDate);
						Date currDueDate = currentRequest.getDueDate();
						if (currDueDate == null) {
							try {
								Field due_date = Field
										.lookupBySystemIdAndFieldName(
												ba.getSystemId(),
												Field.DUE_DATE);

								return new RuleResult(false,
										"Please enter value for field "
												+ due_date.getDisplayName(),
										true);
							} catch (DatabaseException e) {

								e.printStackTrace();
							}
						}
						// Calendar newCurrDueDate = (Calendar)
						// currDueDate.clone();
						Calendar newCurrDueDate = Calendar.getInstance();
						newCurrDueDate.setTime(currDueDate);
						if (newDueDate != null && newCurrDueDate != null) {
							int date1 = newDueDate.get(Calendar.DATE);
							int date2 = newCurrDueDate.get(Calendar.DATE);
							if (date1 == date2) {
								return new RuleResult(
										false,
										"You already have a time sheet of the same date, So please update the same one",
										true);
							}
						}
					}
				}
			}
		}
		return new RuleResult(true);

	}

	private ArrayList<Request> lookUpBySysIdAndUserId(int systemId,
			int currentUser, Connection connection) {
		Request request;
		ArrayList<Request> requestList = new ArrayList<Request>();
		connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("select * from requests where sys_id = ? and user_id = ?");
			ps.setInt(1, systemId);
			ps.setInt(2, currentUser);
			ResultSet rs;
			rs = ps.executeQuery();

			if (rs != null) {
				while (rs.next() != false) {
					try {
						request = Request.createFromResultSet(rs);
						requestList.add(request);
					} catch (DatabaseException e) {
						LOG.info("Exception while contacting the database....");
						e.printStackTrace();
					}

				}
				if (rs != null)
					rs.close();
			}
			if (ps != null)
				ps.close();
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
		return requestList;

	}

	private ArrayList<RequestUser> lookUpBySysId(int systemId,
			Connection connection) {
		RequestUser user;
		ArrayList<RequestUser> userList = new ArrayList<RequestUser>();
		connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("select * from request_users where sys_id = ?");
			ps.setInt(1, systemId);
			ResultSet rs;
			rs = ps.executeQuery();

			if (rs != null) {
				while (rs.next() != false) {
					user = RequestUser.createFromResultSet(rs);
					userList.add(user);
				}
				if (rs != null)
					rs.close();
			}
			if (ps != null)
				ps.close();
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
		return userList;
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
