package transbit.tbits.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsHelper;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.TypeUser;
import transbit.tbits.domain.User;
import transbit.tbits.mail.TBitsMailer;

/**
 * Created by IntelliJ IDEA.
 * User: yes
 * Date: Jun 14, 2007
 * Time: 11:36:42 PM
 * To change this template use File | Settings | File Templates.
 */

/*
 * After the validation of rules on a request object this class gets all the recepients for SMS calls the SMSDevice
 * to send the SMS.This also gets the Message format fom database, & then calls to the SMSDevice class
 * */
public class SMS implements TBitsConstants {
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_API);

	public ArrayList<User> totalUniqueRecepients(Request request) throws Exception{
		int systemId = request.getSystemId();
		ArrayList<User> mergedRequestUsers = new ArrayList<User>();
		ArrayList<TypeUser> categoryUsers = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.CATEGORY, request.getCategoryId().getTypeId());
		ArrayList<TypeUser> statusUsers = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.STATUS, request.getStatusId().getTypeId());
		ArrayList<TypeUser> severityUsers = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.SEVERITY, request.getSeverityId().getTypeId());
		ArrayList<TypeUser> requestTypeUsers = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.REQUEST_TYPE, request.getRequestTypeId().getTypeId());
		ArrayList<TypeUser> globalUsers = TypeUser.getGlobalListBySystemId(systemId);
		ArrayList<TypeUser> uniqueTypeUsers = new ArrayList<TypeUser>();

		if (categoryUsers != null) {
			uniqueTypeUsers.addAll(categoryUsers);
		}

		if (statusUsers != null) {
			uniqueTypeUsers.addAll(statusUsers);
		}

		if (severityUsers != null) {
			uniqueTypeUsers.addAll(severityUsers);
		}

		if (requestTypeUsers != null) {
			uniqueTypeUsers.addAll(requestTypeUsers);
		}

		if (globalUsers != null) {
			uniqueTypeUsers.addAll(globalUsers);
		}

		HashSet<User> typeUsersByNotification = new HashSet<User>() ;
		typeUsersByNotification.addAll(TBitsMailer.getUsersByNotificationRules(uniqueTypeUsers, BusinessArea.lookupBySystemId(request.getSystemId()), request));
		typeUsersByNotification.addAll(getUsersFromRequestUsers(request.getAssignees()));
		typeUsersByNotification.addAll(getUsersFromRequestUsers(request.getSubscribers()));
		typeUsersByNotification.addAll(getUsersFromRequestUsers(request.getCcs()));
//		int noAssignees = request.getAssignees().size();
//		int noCcs = request.getCcs().size();
//		int noSubscribers = request.getSubscribers().size();
//		for (RequestUser ru : request.getAssignees()) 
//		{
//			User user = User.lookupByUserId(ru.getUserId())
//			typeUsersByNotification.add(request.getAssignees().get(i).getUser());
//
//		}

//		
//		for (int i = 0; i < noSubscribers; i++) {
//			typeUsersByNotification.add(request.getSubscribers().get(i).getUser());
//		}
//
//		for (int i = 0; i < noCcs; i++) {
//			typeUsersByNotification.add(request.getCcs().get(i).getUser() );
//		}

//		for(User user:typeUsersByNotification){
//			if (!mergedRequestUsers.contains(user)){
//				mergedRequestUsers.add(user);
//			}
//		}
		typeUsersByNotification.addAll(mergedRequestUsers);
		mergedRequestUsers.clear();
		mergedRequestUsers.addAll(typeUsersByNotification);
		return mergedRequestUsers;
	}

	public Collection<User> getUsersFromRequestUsers(Collection<RequestUser> reqUsers)
	{
		ArrayList<User> alu = new ArrayList<User>() ;
		if( null == reqUsers )
			return alu ;
		
		for( RequestUser ru : reqUsers )
		{
			try {
				alu.add(User.lookupByUserId(ru.getUserId()));
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return alu ;
	}
	public HashMap<Integer, String> getMessageFormat()throws SQLException
	{
		HashMap<Integer, String> msg_format = new HashMap<Integer, String>();
		ResultSet resultSet;
		Connection con = null;
		try{
			con = DataSourcePool.getConnection();
			Statement preparedStatement = con.createStatement();
			resultSet = preparedStatement.executeQuery("SELECT * from msg_format");

			while(resultSet.next()){
				msg_format.put(resultSet.getInt(1), resultSet.getString(2));
			}

		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
				}
			}
		}
		return msg_format;
	}

	public void sendSMS(Request request)throws SQLException {

		boolean isSMSEnabled = TBitsHelper.isSMSEnabled();
		if(!isSMSEnabled)
		{
			LOG.debug("SMS service is disabled");
			return;
		}
		
		RequestModifier requestModifier = new RequestModifier();
		ArrayList<User>totalUniqueRecepients = null;
		try {
			totalUniqueRecepients = totalUniqueRecepients(request);
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		Connection aCon = null;
		try {
			aCon = DataSourcePool.getConnection();
			PreparedStatement preparedStatement = aCon
					.prepareStatement("SELECT * from post_process_rules where user_id = ?");
			HashMap<Integer, String> msg_format = getMessageFormat();

			for (User requestUser : totalUniqueRecepients) {
				if (requestUser == null) {
					continue;
				}
				Request requestClone = null;
				ArrayList<RuleObject> ruleObjectList = new ArrayList<RuleObject>();
				try {
					int uId = requestUser.getUserId();
					preparedStatement.setInt(1, uId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				ResultSet resultSet = preparedStatement.executeQuery();

				while (resultSet.next()) {
					RuleObject ruleObject = new RuleObject(resultSet
							.getInt("sys_id"), resultSet.getInt("rule_id"),
							resultSet.getInt("user_id"), resultSet
									.getInt("priority"), resultSet
									.getString("xml_string"), resultSet
									.getString("description"), resultSet
									.getBoolean("enabled"));
					ruleObjectList.add(ruleObject);
				}

				requestClone = (Request) request.clone();
				for (RuleObject ro : ruleObjectList) {

					PostProcessXmlRule postProcessXmlRule = null;
					try {

						postProcessXmlRule = PostProcessXmlRule
								.createFromXML(ro.getXmlString());
					} catch (InstantiationException e) {
						LOG.error(
								"Error while creating post processXMLRule check ruleid "
										+ ro.getRuleId(), e);
						continue;
					}
					try {
						boolean isModified = requestModifier.modifyRequest(
								requestClone, postProcessXmlRule);
						// requsetClone.setRelatedRequests("tbits#6");
						// System.out.println("requsetClone.getRelatedRequests()
						// = " +
						// requsetClone.getRelatedRequests().get(Field.RELATED_REQUESTS));
					} catch (DatabaseException e) {
						LOG
								.error(
										"Unable to send sms. Probably database problem. ",
										e);
					}
				}

				if (Boolean.parseBoolean(requestClone.get("SendSMS"))) {
					int sms_id = Integer.parseInt(requestClone.get("sms_id"));
					String str = msg_format.get(sms_id);
					RequestTemplate requestTemplate = new RequestTemplate(
							requestClone);
					SMSDevice smsDevice = new SMSDevice();
					boolean smsSuccessful = smsDevice.doSms(requestUser
							.getMobile(), requestTemplate.getText(str));
					if (smsSuccessful) {
						SMSLogs smsLogs = new SMSLogs(requestClone
								.getSystemId(), requestClone.getRequestId(),
								requestUser.getMobile(), requestUser
										.getUserId(), requestClone
										.getMaxActionId()); // sysId, requsetId,
															// mobile
						smsLogs.logSms();
					}
				}
			}
		} catch (SQLException e) {
			throw e;
		}
		finally {
			if (aCon != null) {
				try {
					aCon.close();
				} catch (SQLException e) {
					LOG
							.warn(
									"Unable to close connection after reading post process rules",
									e);
				}
			}
		}
	}

	public static void main(String[] args) {
		SMS sms = new SMS();
		try {
			sms.getMessageFormat().get(1);
			sms.getMessageFormat().get(2);
			SMSTemplate template = new SMSTemplate(sms.getMessageFormat().get(1));
		} catch (SQLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}
