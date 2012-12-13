package nccText;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.bafield.BAField;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;

public class NccTextNotNullRule implements IRule {

	private static final String PLUGIN_NCC_NOTNULL_RULES_BALIST = "plugin.NccNotNull.baList";

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		// TODO Auto-generated method stub
		String baListStr = PropertiesHandler
				.getProperty(PLUGIN_NCC_NOTNULL_RULES_BALIST);
		boolean isApplicableBA = false;
		if (baListStr != null) {
			List<String> baList = Arrays.asList(baListStr.trim().split(","));
			if (baList.contains(ba.getSystemPrefix()))
				isApplicableBA = true;
		}
		if (isApplicableBA) {
			try {
				int dataType = DataType.STRING;
				ArrayList<Field> textField = Field.lookupBySystemId(
						ba.getSystemId(), dataType);
				Hashtable<String, Integer> myPermTable = RolePermission
						.getPermissionsBySystemIdAndUserId(ba.getSystemId(),
								user.getUserId());
				for (Field field : textField) {

					int perm = myPermTable.get(field.getName());
					int fPerm = field.getPermission();
					if (oldRequest == null) {
						if ((((perm & Permission.ADD) & (field.getPermission() & Permission.ADD)) != 0)) {
							String value = (String) currentRequest
									.getObject(field);
							if (!value.equalsIgnoreCase("")) {

							} else {
								return new RuleResult(false, "The value of :"
										+ field.getDisplayName()
										+ " cannot be empty.", true);
							}
						}

					} else {
						if ((((perm & Permission.CHANGE) & (field
								.getPermission() & Permission.CHANGE)) != 0)) {
							String value = (String) currentRequest
									.getObject(field);
							String value1 = (String) oldRequest
									.getObject(field);
							if (!value.equalsIgnoreCase("")) {

							} else {
								return new RuleResult(false, "The value of :"
										+ field.getDisplayName()
										+ " cannot be empty.", true);
							}
						}

					}
				}
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new RuleResult(true);
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
