/**
 * 
 */
package kskMom;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bouncycastle.jce.provider.JDKKeyFactory.RSA;

import com.google.gwt.dev.cfg.RuleReplaceWith;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author paritosh
 * 
 */
public class SepceficRoleStatusClosedPermission implements IRule {

	private static final String ROLE_NAME = "ProjectManager";
	private static final String STATUS = "status_id";
	private static final String CLOSED = "Closed";
	private static final String RECORD_TYPE = "recordtype";
	private static final String ACTION_ITEM = "Action Item";
	private static final String SYS_PREFIX = "KMOM";

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		int currentUserId = user.getUserId();
		boolean flag = false;
		String baSysPrefix = ba.getSystemPrefix();
		int systemId = ba.getSystemId();

		RuleResult ruleResult = new RuleResult();

		if (baSysPrefix.equalsIgnoreCase(SYS_PREFIX)) {

			System.out.println("role permission for PM executed here");

			Type statusType = (Type) currentRequest.getObject(STATUS);
			Type RecordType = (Type) currentRequest.getObject(RECORD_TYPE);
			Type preStatusType = (Type) oldRequest.getObject(STATUS);
			boolean PreStatusClosed = preStatusType.getName().equalsIgnoreCase(CLOSED);
			System.out.println("previous Closed Stat:" + PreStatusClosed);

			ArrayList<String> roleList = new ArrayList<String>();
			try {
				roleList = lookupRoleNameBySystemIdAndUserId(systemId,currentUserId);
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (String roles : roleList) {

				if (roles.equalsIgnoreCase(ROLE_NAME)) {
					flag = true;
					System.out.println("User has Project manager role:-");
				}
			}

			if (flag == false) {
				System.out.println("User does't has Project manager role:-");
			}

			if (RecordType.getName().equalsIgnoreCase(ACTION_ITEM)
					&& statusType.getName().equalsIgnoreCase(CLOSED)
					&& PreStatusClosed == false) {
				if (flag == false) {
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("you are not authorised to close a Action Item");
				}
			}
		}
		// TODO Auto-generated method stub
		return ruleResult;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 1;
	}

	public static ArrayList<String> lookupRoleNameBySystemIdAndUserId(
			int aSystemId, int aUserId) throws DatabaseException, SQLException {

		ArrayList<String> roleList = new ArrayList<String>();
		Connection connection = null;
		Role role;

		try {

			connection = DataSourcePool.getConnection();
			CallableStatement cs = connection.prepareCall("stp_admin_getRolesBySysIdAndUserId ?, ?");

			cs.setInt(1, aSystemId);
			cs.setInt(2, aUserId);
			cs.execute();
			ResultSet rs = cs.getResultSet();
			while (rs.next()) {

				role = Role.lookupBySystemIdAndRoleId(aSystemId, rs.getInt(2));
				roleList.add(role.getRoleName());
				System.out.println("users existing Role:" + role.getRoleName());

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			connection.close();
		}

		return roleList;

	}

}
