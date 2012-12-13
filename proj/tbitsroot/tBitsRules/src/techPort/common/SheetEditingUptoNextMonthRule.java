package common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class SheetEditingUptoNextMonthRule implements IRule {
	public static TBitsLogger LOG = TBitsLogger.getLogger("techPort");

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		if (!isAddRequest) {

			try {

				ArrayList<Request> request = lookUpBySysIdAndUserId(
						ba.getSystemId(), user.getUserId(), connection);
				for (Request req : request) {
					if (req.getRequestId() == currentRequest.getRequestId()) {
						Date currDueDate = currentRequest.getDueDate();
					//	Calendar newCurrDueDate = (Calendar) currDueDate
								//.clone();
						Calendar newCurrDueDate= Calendar.getInstance();
						newCurrDueDate.setTime(currDueDate);
						Date firstDate = null;
						String query = " select ae.due_datetime from actions ae"
								+ " where ae.action_id = "
								+ " 	( select min(action_id) from actions mae "
								+ " 		where mae.sys_id = ? and mae.request_id = ? and mae.due_datetime IS NOT NULL) "
								+ " and ae.sys_id = ? and ae.request_id = ? ";

						PreparedStatement ps = connection
								.prepareStatement(query);
						ps.setInt(1, oldRequest.getSystemId());
						ps.setInt(2, oldRequest.getRequestId());
						ps.setInt(3, oldRequest.getSystemId());
						ps.setInt(4, oldRequest.getRequestId());
						ResultSet rs = ps.executeQuery();
						if (rs.next())
							firstDate = rs.getDate(1);
						if ((firstDate == null) && currDueDate != null) {
							firstDate = currentRequest.getDueDate();
						}

					//Calendar firstDueDate  = (Calendar) firstDate.clone();
					Calendar firstDueDate= Calendar.getInstance();
					firstDueDate.setTime(firstDate);
					
					Calendar escalationDate = getEscalationDate(firstDueDate);
					
					if( newCurrDueDate.after(escalationDate))
					{
						return  new RuleResult(false, "You cannnot update the time sheet. Please contact admin for the same ", true);
					}
					}

				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return new RuleResult(true);
	}

	private Calendar getEscalationDate(Calendar firstDate) {
		int month=firstDate.get(Calendar.MONTH);
		
		firstDate.set(firstDate.get(Calendar.YEAR), month+1, 5);
		
		return firstDate;
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

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
