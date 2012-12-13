/**
 * 
 */
package ipl;

import ipl.IPLUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

/**
 * @author lokesh
 *
 */
public class DMSAutoAssigneeAndSubscriberInsertionRule implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	private static final String IPL_DMS_AUTO_ASSIGNEE_AND_SUBSCRIBER_INSERTION_RULE_BA_LIST = "ipl.DMSAutoAssigneeAndSubscriberInsertionRule.baList";

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		
		/*
		 * 	Each Request/Document in this BA shall be Private by default.
			Corresponding to each organization there will be a mailing list, all the users of that organization shall be put in this mailing list.
			Subscribers (mailing list of the recipient organization shall be programatically added to the user specified subscriber list based on the Category (Destination).
			Assignee shall be marked based on Destination (Category) and Discipline (status).
			Based on the logger's organization a certain mailing list will be programatically added to the user specified subscriber list.
		 */
		
		String baList = IPLUtils.getProperty(IPL_DMS_AUTO_ASSIGNEE_AND_SUBSCRIBER_INSERTION_RULE_BA_LIST);
		RuleResult rr = new RuleResult();
		rr.setCanContinue(true);
		rr.setMessage("Rule is not applicable for this business area.");
		if ((baList != null) && (!baList.trim().equals(""))){
			if (IPLUtils.isExistsInString(baList, ba.getSystemPrefix()) && isAddRequest){
				// Set as private
				currentRequest.setIsPrivate(true);
				
				// Set assignees based on category and discipline.
				ArrayList<RequestUser> categoryAndDisciplineBasedAssignees = getCategoryAndDisciplineBasedAssignees(connection, ba.getSystemId(), currentRequest.getRepliedToAction(),
																						currentRequest.getCategoryId().getTypeId(), currentRequest.getStatusId().getTypeId());
				if ((categoryAndDisciplineBasedAssignees != null) && (!categoryAndDisciplineBasedAssignees.isEmpty())){
					ArrayList<RequestUser> assigneesList = currentRequest.getAssignees();
					assigneesList.addAll(categoryAndDisciplineBasedAssignees);
				}
				
				ArrayList<RequestUser> aSubscribers = currentRequest.getSubscribers();
				
				String loggerOrganization = user.getFirmCode().trim();
				ArrayList<User> activeUsers = User.getActiveUsers();
				for (User mUser : activeUsers){					
					if (mUser.getFirmCode().trim().equals(loggerOrganization) && (mUser.getUserTypeId() == UserType.INTERNAL_MAILINGLIST)){
						RequestUser ru = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.SUBSCRIBER,
															mUser.getUserId(), 1, false);						
						aSubscribers.add(ru);
						break;
					}
				}
				
				String targetFirmSubscibers = "";
				if (currentRequest.getCategoryId().getName().equals("Indiabulls"))
					targetFirmSubscibers = IPLUtils.getProperty("ipl.DMSAutoAssigneeAndSubscriberInsertionRule.IBMailingList");
				else if (currentRequest.getCategoryId().getName().equals("BHEL"))
					targetFirmSubscibers = IPLUtils.getProperty("ipl.DMSAutoAssigneeAndSubscriberInsertionRule.BHELMailingList");
				else if (currentRequest.getCategoryId().getName().equals("TCE"))
					targetFirmSubscibers = IPLUtils.getProperty("ipl.DMSAutoAssigneeAndSubscriberInsertionRule.TCEMailingList");
				
				if ((targetFirmSubscibers != null) && (!targetFirmSubscibers.trim().equals(""))){
					String[] targetSubList = targetFirmSubscibers.split(",");
					for (String mListName : targetSubList){
						User mList = null;
						try {
							mList = User.lookupAllByUserLogin(mListName);
							if (mList != null){
								RequestUser ru = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.SUBSCRIBER,
										mList.getUserId(), 1, false);
								if (ru != null)
									aSubscribers.add(ru);
							}
						} catch (DatabaseException e) {
							e.printStackTrace();
						}						
					}
				}			
				
				currentRequest.setSubscribers(aSubscribers);
			}
		}
		return rr;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + "- Executing auto insertion of assignees and subscribers to AMR Document Sharing BA request.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		return 0;
	}
	
	
	public static ArrayList<RequestUser> getCategoryAndDisciplineBasedAssignees(Connection connection, int systemId, int requestId, int categoryId, 
																					int disciplineId){
		ArrayList<RequestUser> requestUserList = new ArrayList<RequestUser>();
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM ipl_assignee_mapping where sys_id=? and category_id=? and " +
																	"discipline_id=?");
			ps.setInt(1, systemId);
			ps.setInt(2, categoryId);
			ps.setInt(3, disciplineId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					int userId = rs.getInt(4);
					if (userId > 0){
						User user = User.lookupAllByUserId(userId);
						if (user != null){
							RequestUser ru = new RequestUser(systemId, requestId, UserType.ASSIGNEE, user.getUserId(), 0, false);
							if (ru != null)
								requestUserList.add(ru);
						}
					}
						
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return requestUserList;
	}

}
