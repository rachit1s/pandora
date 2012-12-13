package corrGeneric.com.tbitsGlobal.server.adm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.el.Coercions;
import org.jfree.util.Log;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
//import transbit.tbits.exception.CorrException;

import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

import corrGeneric.com.tbitsGlobal.client.modelData.BAFieldMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrNumberConfigClient;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrNumberKeyClient;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrPropertiesClient;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrProtocolClient;
import corrGeneric.com.tbitsGlobal.client.modelData.FieldNameMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfTypeClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportNameClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportParamsClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportTypeClient;
import corrGeneric.com.tbitsGlobal.client.modelData.UserMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.UserMapTypeClient;
import corrGeneric.com.tbitsGlobal.server.managers.BaFieldManager;
import corrGeneric.com.tbitsGlobal.server.managers.CorrNumberManager;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.OnBehalfManager;
import corrGeneric.com.tbitsGlobal.server.managers.PropertyManager;
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.managers.ReportManager;
import corrGeneric.com.tbitsGlobal.server.managers.ReportNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.ReportParamsManager;
import corrGeneric.com.tbitsGlobal.server.managers.UserMapManager;
import corrGeneric.com.tbitsGlobal.shared.domain.BaFieldEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportParamEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry;
import corrGeneric.com.tbitsGlobal.shared.key.BaFieldKey;
import corrGeneric.com.tbitsGlobal.shared.key.CorrNumberKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

/**
 * This class manipulates correspondence properties from corr_properties and
 * converts them from PropertyEntry to CorrPropertiesClient and vice versa so as
 * to be usable by the grid(which requires model data) for displaying the
 * values.
 * 
 * @author devashish
 * 
 */
public class CorrProperties {

	protected TypeClient invalidType;
	protected FieldClient invalidField;

	/**
	 * Constructor
	 */
	public CorrProperties() {
		invalidType = new TypeClient();
		invalidField = new FieldClient();
		buildInvalidType();
		buildInvalidField();
	}

	protected void buildInvalidField() {
		invalidField.setName("NULL");
		invalidField.setDescription("NULL");
		invalidField.setDisplayName("NULL");
		invalidField.setSystemId(-1);
		invalidField.setFieldId(-1);
	}

	protected void buildInvalidType() {
		invalidType.setName("NULL");
		invalidType.setDescription("NULL");
		invalidType.setDisplayName("NULL");
		invalidType.setSystemId(-1);
		invalidType.setTypeId(-1);
	}

	// -------------------User Map
	// Properties------------------------------------//
	/**
	 * Delete user map properties from database and return the count of
	 * properties successfully deleted.
	 */
	public Integer deleteUserMapProperties(List<UserMapClient> properties) {
		Integer deletedPropertyCount = 0;
		for (UserMapClient property : properties) {

			Long id = Long.valueOf(property.getId());
			if (-1 == id)
				continue;
			String sysPrefix = property.getSysprefix();
			String user = property.getUser().getUserLogin();
			String userLoginValue = property.getUserLoginValue().getUserLogin();
			String type1 = property.getType1().getName();
			String type2 = property.getType2().getName();
			String type3 = property.getType3().getName();

			String userTypeField = property.getUserTypeField().getName();
			Integer strictness = Integer.valueOf(property.getStrictness());

			UserMapEntry propertyToBeDeleted = new UserMapEntry(id, user,
					sysPrefix, type1, type2, type3, userTypeField,
					userLoginValue, strictness);

			try {
				UserMapManager.getInstance().deleteEntry(propertyToBeDeleted);
				deletedPropertyCount++;
			} catch (CorrException e) {
				Log.error(
						"Could not delete user map properties from database...",
						e);
				e.printStackTrace();
			} catch (Exception e) {
				Log.error(
						"Could not delete user map properties from database...",
						e);
				e.printStackTrace();
			}
		}

		return deletedPropertyCount;
	}

	/**
	 * Add/Update user map properties and return the same list of properties
	 * with the status which indicates whether the property has been
	 * successfully saved or not. If not, then the status message indicates
	 * where the error has occured.
	 * 
	 * @param properties
	 * @return list of modified properties
	 */
	public ArrayList<UserMapClient> setUserMapProperties(
			ArrayList<UserMapClient> properties) {
		ArrayList<UserMapClient> savedPropertiesList = new ArrayList<UserMapClient>();

		for (UserMapClient property : properties) {
			boolean errorFlag = false;
			String statusMessage = "";

			Long id = Long.valueOf(property.getId());
			String sysPrefix = property.getSysprefix();
			String user = property.getUser().getUserLogin();
			String userLoginValue = property.getUserLoginValue().getUserLogin();

			String type1 = "";
			TypeClient type1Client = property.getType1();
			if ((type1Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.UserMapType1, sysPrefix))) {
				type1 = null;
			} else if ((type1Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.UserMapType1, sysPrefix)))) {
				statusMessage = statusMessage + " [UMT:1] ";
				errorFlag = true;
			} else if ((!type1Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.UserMapType1, sysPrefix))) {
				statusMessage = statusMessage + " [UMT:1] ";
				errorFlag = true;
			} else if ((!type1Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.UserMapType1, sysPrefix))) {
				if (null == getType(GenericParams.UserMapType1,
						type1Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [UMT:1] ";
					errorFlag = true;
				} else {
					type1 = type1Client.getName();
				}
			}

			String type2 = "";
			TypeClient type2Client = property.getType2();
			if ((type2Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.UserMapType2, sysPrefix))) {
				type2 = null;
			} else if ((type2Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.UserMapType2, sysPrefix)))) {
				statusMessage = statusMessage + " [UMT:2] ";
				errorFlag = true;
			} else if ((!type2Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.UserMapType2, sysPrefix))) {
				statusMessage = statusMessage + " [OBT:2] ";
				errorFlag = true;
			} else if ((!type2Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.UserMapType2, sysPrefix))) {
				if (null == getType(GenericParams.UserMapType2,
						type2Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [UMT:2] ";
					errorFlag = true;
				} else {
					type2 = type2Client.getName();
				}
			}

			String type3 = "";
			TypeClient type3Client = property.getType3();
			if ((type3Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.UserMapType3, sysPrefix))) {
				type3 = null;
			} else if ((type3Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.UserMapType3, sysPrefix)))) {
				statusMessage = statusMessage + " [UMT:3] ";
				errorFlag = true;
			} else if ((!type3Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.UserMapType3, sysPrefix))) {
				statusMessage = statusMessage + " [UMT:3] ";
				errorFlag = true;
			} else if ((!type3Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.UserMapType3, sysPrefix))) {
				if (null == getType(GenericParams.UserMapType3,
						type3Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [UMT:3] ";
					errorFlag = true;
				} else {
					type3 = type3Client.getName();
				}
			}

			String userTypeField = property.getUserTypeField().getName();

			Integer strictness = Integer.valueOf(property.getStrictness());

			if (!errorFlag) {
				UserMapEntry propertyToBeModified = new UserMapEntry(id, user,
						sysPrefix, type1, type2, type3, userTypeField,
						userLoginValue, strictness);
				try {
					UserMapManager.getInstance().persistEntry(
							propertyToBeModified);
					statusMessage = "OK";
					property.setStatus(statusMessage);
				} catch (CorrException e) {
					statusMessage = e.getMessage();
					property.setStatus(statusMessage);
					Log.error(
							"Could not get set on User Map properties to database...",
							e);
					e.printStackTrace();
				} catch (Exception e) {
					statusMessage = e.getMessage();
					property.setStatus(statusMessage);
					Log.error(
							"Could not get set on User Map properties to database...",
							e);
					e.printStackTrace();
				}
			} else {
				property.setStatus(statusMessage);
			}
			savedPropertiesList.add(property);
		}
		return savedPropertiesList;
	}

	/**
	 * Gather user map properties from database corresponding to a ba and user.
	 * 
	 * @param sysPrefix
	 * @param userLogin
	 * @return properties list
	 */
	public ArrayList<UserMapClient> gatherUserMap(String sysPrefix,
			String userLogin) {
		ArrayList<UserMapClient> userMapPropertiesMD = new ArrayList<UserMapClient>();
		ArrayList<UserMapEntry> userMapProperties = new ArrayList<UserMapEntry>();

		try {

			if (null != UserMapManager.lookupUserMap(sysPrefix, userLogin)) {
				userMapProperties.addAll(UserMapManager.lookupUserMap(
						sysPrefix, userLogin));
			} else
				return null;

			for (UserMapEntry entry : userMapProperties) {
				String statusMessage = "";
				boolean errorFlag = false;

				UserMapClient temp = new UserMapClient();
				temp.setID(Long.toString(entry.getId()));
				temp.setSysprefix(entry.getSysPrefix());

				UserClient user = new UserClient();
				if (null != entry.getUserLogin()) {
					user.setUserLogin(entry.getUserLogin());
					temp.setUser(user);
				} else {
					user.setUserLogin("NULL");
					temp.setUser(user);
				}

				TypeClient type1Client = new TypeClient();
				if (null != getType(GenericParams.UserMapType1,
						entry.getType1(), entry.getSysPrefix())) {
					type1Client = getType(GenericParams.UserMapType1,
							entry.getType1(), entry.getSysPrefix());
				} else
					type1Client = null;
				if ((null == entry.getType1())
						&& (!isTypeSet(GenericParams.UserMapType1, sysPrefix))) {
					temp.setType1(invalidType);
				} else if ((null == entry.getType1())
						&& (isTypeSet(GenericParams.UserMapType1, sysPrefix))) {
					temp.setType1(invalidType);
					statusMessage = statusMessage + " [UMT:1] ";
					errorFlag = true;
				} else if ((null != entry.getType1()) && (null == type1Client)) {
					invalidType.setName(entry.getType1());
					temp.setType1(invalidType);
					statusMessage = statusMessage + " [UMT:1] ";
					errorFlag = true;
				} else if ((null != entry.getType1()) && (null != type1Client)) {
					temp.setType1(type1Client);
				}

				TypeClient type2Client = new TypeClient();
				if (null != getType(GenericParams.UserMapType2,
						entry.getType2(), entry.getSysPrefix())) {
					type2Client = getType(GenericParams.UserMapType2,
							entry.getType2(), entry.getSysPrefix());
				} else
					type2Client = null;
				if ((null == entry.getType2())
						&& (!isTypeSet(GenericParams.UserMapType2, sysPrefix))) {
					temp.setType2(invalidType);
				} else if ((null == entry.getType2())
						&& (isTypeSet(GenericParams.UserMapType2, sysPrefix))) {
					temp.setType2(invalidType);
					statusMessage = statusMessage + " [UMT:2] ";
					errorFlag = true;
				} else if ((null != entry.getType2()) && (null == type2Client)) {
					invalidType.setName(entry.getType2());
					temp.setType2(invalidType);
					statusMessage = statusMessage + " [UMT:2] ";
					errorFlag = true;
				} else if ((null != entry.getType2()) && (null != type2Client)) {
					temp.setType2(type2Client);
				}

				TypeClient type3Client = new TypeClient();
				if (null != getType(GenericParams.UserMapType3,
						entry.getType3(), entry.getSysPrefix())) {
					type3Client = getType(GenericParams.UserMapType3,
							entry.getType3(), entry.getSysPrefix());
				} else
					type3Client = null;
				if ((null == entry.getType3())
						&& (!isTypeSet(GenericParams.UserMapType3, sysPrefix))) {
					temp.setType3(invalidType);
				} else if ((null == entry.getType3())
						&& (isTypeSet(GenericParams.UserMapType3, sysPrefix))) {
					temp.setType3(invalidType);
					statusMessage = statusMessage + " [UMT:3] ";
					errorFlag = true;
				} else if ((null != entry.getType3()) && (null == type3Client)) {
					invalidType.setName(entry.getType3());
					temp.setType3(invalidType);
					statusMessage = statusMessage + " [UMT:3] ";
					errorFlag = true;
				} else if ((null != entry.getType3()) && (null != type3Client)) {
					temp.setType3(type3Client);
				}

				UserClient userLoginValue = new UserClient();
				if (null != entry.getUserLoginValue()) {
					userLoginValue.setUserLogin(entry.getUserLoginValue());
					temp.setUserLoginValue(userLoginValue);
				} else {
					userLoginValue.setUserLogin("NULL");
					temp.setUserLoginValue(userLoginValue);
				}

				FieldClient userTypeField = new FieldClient();
				userTypeField.setName(entry.getUserTypeFieldName());
				temp.setUserTypeField(userTypeField);

				String strictness = Integer.toString(entry.getStrictNess());
				temp.setStrictness(strictness);

				if (!errorFlag)
					temp.setStatus("OK");
				else
					temp.setStatus(statusMessage);

				userMapPropertiesMD.add(temp);

			}
		} catch (CorrException e) {
			Log.error("Error fetching user map properties from database..", e);
			e.printStackTrace();
		} catch (Exception e) {
			Log.error("Error fetching user map properties from database..", e);
			e.printStackTrace();
		}

		return userMapPropertiesMD;
	}

	/**
	 * Get the type values used in user map corresponding to each user map type.
	 * 
	 * @param sysprefix
	 * @return list of all the types
	 */
	public UserMapTypeClient getUserMapTypes(String sysprefix) {
		UserMapTypeClient userMapTypes = new UserMapTypeClient();

		if (getTypeValues(GenericParams.UserMapType1, sysprefix) != null)
			userMapTypes.addUserMapTypeList(GenericParams.UserMapType1,
					getTypeValues(GenericParams.UserMapType1, sysprefix));
		if (getTypeValues(GenericParams.UserMapType2, sysprefix) != null)
			userMapTypes.addUserMapTypeList(GenericParams.UserMapType2,
					getTypeValues(GenericParams.UserMapType2, sysprefix));
		if (getTypeValues(GenericParams.UserMapType3, sysprefix) != null)
			userMapTypes.addUserMapTypeList(GenericParams.UserMapType3,
					getTypeValues(GenericParams.UserMapType3, sysprefix));

		return userMapTypes;
	}

	// ----------------------On Behalf map
	// properties---------------------------//

	/**
	 * Delete on behalf map properties and return the count of properties that
	 * were successfully deleted.
	 */
	public Integer deleteOnBehalfMapProperties(
			List<OnBehalfMapClient> properties) {
		Integer deletedPropertyCount = 0;

		for (OnBehalfMapClient property : properties) {
			Long id = Long.valueOf(property.getId());
			if (-1 == id)
				continue;
			String sysPrefix = property.getSysprefix();
			String type1 = property.getType1().getName();
			String type2 = property.getType2().getName();
			String type3 = property.getType3().getName();
			String user = property.getUser().getUserLogin();
			String onBehalfUser = property.getOnBehalfUser().getUserLogin();

			OnBehalfEntry entryToBeDeleted = new OnBehalfEntry(id, sysPrefix,
					user, type1, type2, type3, onBehalfUser);

			try {
				OnBehalfManager.getInstance().deleteEntry(entryToBeDeleted);
				deletedPropertyCount++;
			} catch (CorrException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return deletedPropertyCount;
	}

	/**
	 * Set/modify on behalf map properties and return the list of same
	 * properties with appropriate status message stating whether the property
	 * was successfully saved or not.
	 * 
	 * @param properties
	 * @return list of properties
	 */
	public ArrayList<OnBehalfMapClient> setOnBehalfMapProperties(
			ArrayList<OnBehalfMapClient> properties) {
		ArrayList<OnBehalfMapClient> savedPropertiesList = new ArrayList<OnBehalfMapClient>();

		for (OnBehalfMapClient property : properties) {
			boolean errorFlag = false;
			String statusMessage = "";

			Long id = Long.valueOf(property.getId());
			String sysPrefix = property.getSysprefix();
			String user = property.getUser().getUserLogin();
			String onBehalfUser = property.getOnBehalfUser().getUserLogin();

			String type1 = "";
			TypeClient type1Client = property.getType1();
			if ((type1Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.OnBehalfType1, sysPrefix))) {
				type1 = null;
			} else if ((type1Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.OnBehalfType1, sysPrefix)))) {
				statusMessage = statusMessage + " [OBT:1] ";
				errorFlag = true;
			} else if ((!type1Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.OnBehalfType1, sysPrefix))) {
				statusMessage = statusMessage + " [OBT:1] ";
				errorFlag = true;
			} else if ((!type1Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.OnBehalfType1, sysPrefix))) {
				if (null == getType(GenericParams.OnBehalfType1,
						type1Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [OBT:1] ";
					errorFlag = true;
				} else {
					type1 = type1Client.getName();
				}
			}

			String type2 = "";
			TypeClient type2Client = property.getType2();
			if ((type2Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.OnBehalfType2, sysPrefix))) {
				type2 = null;
			} else if ((type2Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.OnBehalfType2, sysPrefix)))) {
				statusMessage = statusMessage + " [OBT:2] ";
				errorFlag = true;
			} else if ((!type2Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.OnBehalfType2, sysPrefix))) {
				statusMessage = statusMessage + " [OBT:2] ";
				errorFlag = true;
			} else if ((!type2Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.OnBehalfType2, sysPrefix))) {
				if (null == getType(GenericParams.OnBehalfType2,
						type2Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [OBT:2] ";
					errorFlag = true;
				} else {
					type2 = type2Client.getName();
				}
			}

			String type3 = "";
			TypeClient type3Client = property.getType3();
			if ((type3Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.OnBehalfType3, sysPrefix))) {
				type3 = null;
			} else if ((type3Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.OnBehalfType3, sysPrefix)))) {
				statusMessage = statusMessage + " [OBT:3] ";
				errorFlag = true;
			} else if ((!type3Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.OnBehalfType3, sysPrefix))) {
				statusMessage = statusMessage + " [OBT:3] ";
				errorFlag = true;
			} else if ((!type3Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.OnBehalfType3, sysPrefix))) {
				if (null == getType(GenericParams.OnBehalfType3,
						type3Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [OBT:3] ";
					errorFlag = true;
				} else {
					type3 = type3Client.getName();
				}
			}

			if (!errorFlag) {
				OnBehalfEntry propertyToBeModified = new OnBehalfEntry(id,
						sysPrefix, user, type1, type2, type3, onBehalfUser);
				try {
					OnBehalfManager.getInstance().persistEntry(
							propertyToBeModified);
					statusMessage = "OK";
					property.setStatus(statusMessage);
				} catch (CorrException e) {
					statusMessage = e.getMessage();
					property.setStatus(statusMessage);
					Log.error(
							"Could not get set on behalf properties to database...",
							e);
					e.printStackTrace();
				} catch (Exception e) {
					statusMessage = e.getMessage();
					property.setStatus(statusMessage);
					Log.error(
							"Could not get set on behalf properties to database...",
							e);
					e.printStackTrace();
				}
			} else {
				property.setStatus(statusMessage);
			}
			savedPropertiesList.add(property);
		}
		return savedPropertiesList;
	}

	/**
	 * 
	 * @param sysprefix
	 * @param userLogin
	 * @return
	 */
	public ArrayList<OnBehalfMapClient> gatherOnBehalfMap(String sysprefix,
			String userLogin) {
		ArrayList<OnBehalfMapClient> onBehalfMapPropertiesMD = new ArrayList<OnBehalfMapClient>();
		ArrayList<OnBehalfEntry> onBehalfMapProperties = new ArrayList<OnBehalfEntry>();

		try {
			if (null != OnBehalfManager
					.lookupOnBehalfList(sysprefix, userLogin)) {
				onBehalfMapProperties.addAll(OnBehalfManager
						.lookupOnBehalfList(sysprefix, userLogin));
			} else
				return null;

			for (OnBehalfEntry entry : onBehalfMapProperties) {
				String statusMessage = "";
				boolean errorFlag = false;

				OnBehalfMapClient temp = new OnBehalfMapClient();
				temp.setID(Long.toString(entry.getId()));
				temp.setSysprefix(entry.getSysPrefix());

				UserClient onBehalfUser = new UserClient();
				onBehalfUser.setUserLogin(entry.getOnBehalfUser());
				temp.setOnBehalfUser(onBehalfUser);

				UserClient user = new UserClient();
				user.setUserLogin(entry.getUserLogin());
				temp.setUser(user);

				TypeClient type1Client = new TypeClient();
				if (null != getType(GenericParams.OnBehalfType1,
						entry.getType1(), entry.getSysPrefix())) {
					type1Client = new TypeClient();
					type1Client = getType(GenericParams.OnBehalfType1,
							entry.getType1(), entry.getSysPrefix());
				} else
					type1Client = null;
				if ((null == entry.getType1())
						&& (!isTypeSet(GenericParams.OnBehalfType1, sysprefix))) {
					temp.setType1(invalidType);
				} else if ((null == entry.getType1())
						&& (isTypeSet(GenericParams.OnBehalfType1, sysprefix))) {
					temp.setType1(invalidType);
					statusMessage = statusMessage + " [OBT:1] ";
					errorFlag = true;
				} else if ((null != entry.getType1()) && (null == type1Client)) {
					invalidType.setName(entry.getType1());
					temp.setType1(invalidType);
					statusMessage = statusMessage + " [OBT:1] ";
					errorFlag = true;
				} else if ((null != entry.getType1()) && (null != type1Client)) {
					temp.setType1(type1Client);
				}

				TypeClient type2Client = new TypeClient();
				if (null != getType(GenericParams.OnBehalfType2,
						entry.getType2(), entry.getSysPrefix())) {
					type2Client = new TypeClient();
					type2Client = getType(GenericParams.OnBehalfType2,
							entry.getType2(), entry.getSysPrefix());
				} else
					type2Client = null;
				if ((null == entry.getType2())
						&& (!isTypeSet(GenericParams.OnBehalfType2, sysprefix))) {
					temp.setType2(invalidType);
				} else if ((null == entry.getType2())
						&& (isTypeSet(GenericParams.OnBehalfType2, sysprefix))) {
					temp.setType2(invalidType);
					statusMessage = statusMessage + " [OBT:2] ";
					errorFlag = true;
				} else if ((null != entry.getType2()) && (null == type2Client)) {
					invalidType.setName(entry.getType2());
					temp.setType2(invalidType);
					statusMessage = statusMessage + " [OBT:2] ";
					errorFlag = true;
				} else if ((null != entry.getType2()) && (null != type2Client)) {
					temp.setType2(type2Client);
				}

				TypeClient type3Client = new TypeClient();
				if (null != getType(GenericParams.OnBehalfType3,
						entry.getType3(), entry.getSysPrefix())) {
					type3Client = new TypeClient();
					type3Client = getType(GenericParams.OnBehalfType3,
							entry.getType3(), entry.getSysPrefix());
				} else
					type3Client = null;
				if ((null == entry.getType3())
						&& (!isTypeSet(GenericParams.OnBehalfType3, sysprefix))) {
					temp.setType3(invalidType);
				} else if ((null == entry.getType3())
						&& (isTypeSet(GenericParams.OnBehalfType3, sysprefix))) {
					temp.setType3(invalidType);
					statusMessage = statusMessage + " [OBT:3] ";
					errorFlag = true;
				} else if ((null != entry.getType3()) && (null == type3Client)) {
					invalidType.setName(entry.getType3());
					temp.setType3(invalidType);
					statusMessage = statusMessage + " [OBT:3] ";
					errorFlag = true;
				} else if ((null != entry.getType3()) && (null != type3Client)) {
					temp.setType3(type3Client);
				}

				if (!errorFlag)
					temp.setStatus("OK");
				else
					temp.setStatus(statusMessage);

				onBehalfMapPropertiesMD.add(temp);

			}
		} catch (CorrException e) {
			Log.error("Error fetching on behalf properties from database..", e);
			e.printStackTrace();
		} catch (Exception e) {
			Log.error("Error fetching on behalf properties from database..", e);
			e.printStackTrace();
		}

		return onBehalfMapPropertiesMD;
	}

	public OnBehalfTypeClient getOnBehalfTypes(String sysprefix) {
		OnBehalfTypeClient onBehalfTypes = new OnBehalfTypeClient();

		if (getTypeValues(GenericParams.OnBehalfType1, sysprefix) != null)
			onBehalfTypes.addOnBehalfTypeList(GenericParams.OnBehalfType1,
					getTypeValues(GenericParams.OnBehalfType1, sysprefix));
		if (getTypeValues(GenericParams.OnBehalfType2, sysprefix) != null)
			onBehalfTypes.addOnBehalfTypeList(GenericParams.OnBehalfType2,
					getTypeValues(GenericParams.OnBehalfType2, sysprefix));
		if (getTypeValues(GenericParams.OnBehalfType3, sysprefix) != null)
			onBehalfTypes.addOnBehalfTypeList(GenericParams.OnBehalfType3,
					getTypeValues(GenericParams.OnBehalfType3, sysprefix));

		return onBehalfTypes;
	}

	// --------------------------Field Name Map
	// Properties--------------------------------//
	public Integer deleteFieldNameMapProperties(
			List<FieldNameMapClient> properties) {
		Integer deletedPropertiesCount = 0;

		for (FieldNameMapClient property : properties) {
			Long id = Long.valueOf(property.getId());
			if (-1 == id)
				continue;
			String corrFieldName = property.getCorrFieldName();
			String fieldName = property.getField().getName();
			String sysprefix = property.getSysprefix();

			FieldNameEntry entryToBeDeleted = new FieldNameEntry(id,
					corrFieldName, sysprefix, fieldName);
			try {
				FieldNameManager.getInstance().deleteEntry(entryToBeDeleted);
				deletedPropertiesCount++;
			} catch (CorrException e) {
				Log.error("Could not save properties to database", e);
				e.printStackTrace();
			} catch (Exception e) {
				Log.error("Could not save properties to database", e);
				e.printStackTrace();
			}
		}
		return deletedPropertiesCount;
	}

	public Integer setFieldNameMapProperties(
			ArrayList<FieldNameMapClient> properties) {
		Integer savedPropertiesCount = 0;

		for (FieldNameMapClient property : properties) {
			Long id = Long.valueOf(property.getId());
			String corrFieldName = property.getCorrFieldName();
			String fieldName = property.getField().getName();
			String sysprefix = property.getSysprefix();

			FieldNameEntry entryToBeModified = new FieldNameEntry(id,
					corrFieldName, sysprefix, fieldName);
			try {
				FieldNameManager.getInstance().persistEntry(entryToBeModified);
				savedPropertiesCount++;
			} catch (CorrException e) {
				Log.error("Could not save properties to database", e);
				e.printStackTrace();
			} catch (Exception e) {
				Log.error("Could not save properties to database", e);
				e.printStackTrace();
			}
		}
		return savedPropertiesCount;
	}

	public ArrayList<FieldNameMapClient> gatherFieldNameMapProperties(
			String sysprefix) {
		ArrayList<FieldNameMapClient> fieldNameMapPropertiesList = new ArrayList<FieldNameMapClient>();
		HashMap<String, FieldNameEntry> fieldNameMap = new HashMap<String, FieldNameEntry>();
		try {
			if (null == FieldNameManager.lookupFieldNameMap(sysprefix))
				return null;
			fieldNameMap.putAll(FieldNameManager.lookupFieldNameMap(sysprefix));
			for (String property : fieldNameMap.keySet()) {

				FieldNameMapClient client = new FieldNameMapClient();
				client.setID(Long.toString(fieldNameMap.get(property).getId()));
				client.setSysprefix(fieldNameMap.get(property).getSysPrefix());
				client.setCorrFieldName(fieldNameMap.get(property)
						.getCorrFieldName());

				FieldClient baField = new FieldClient();
				baField.setName(fieldNameMap.get(property).getBaFieldName());
				client.setField(baField);

				fieldNameMapPropertiesList.add(client);
			}

		} catch (CorrException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fieldNameMapPropertiesList;
	}

	public Integer deleteBaFieldProperties(List<BAFieldMapClient> properties) {

		Integer deletedPropertiesCount = 0;
		for (BAFieldMapClient property : properties) {
			Integer id = Integer.valueOf(property.getId());
			if (-1 == id)
				continue;
			String fromSysprefix = property.getFromSysprefix();
			String fromField = property.getFromField().getName();
			String toSysprefix = property.getToSysprefix();
			String toField = property.getToField().getName();

			BaFieldEntry entryToBeDeleted = new BaFieldEntry(id, fromSysprefix,
					fromField, toSysprefix, toField);
			try {
				BaFieldManager.getInstance().deleteEntry(entryToBeDeleted);
				deletedPropertiesCount++;
			} catch (CorrException e) {
				Log.error("Could not save properties to database", e);
				e.printStackTrace();
			} catch (Exception e) {
				Log.error("Could not save properties to database", e);
				e.printStackTrace();
			}
		}
		return deletedPropertiesCount;
	}

	public Integer setBAFieldMap(ArrayList<BAFieldMapClient> properties) {
		Integer savedPropertiesCount = 0;
		for (BAFieldMapClient property : properties) {
			Integer id = Integer.valueOf(property.getId());
			String fromSysprefix = property.getFromSysprefix();
			String fromField = property.getFromField().getName();
			String toSysprefix = property.getToSysprefix();
			String toField = property.getToField().getName();

			BaFieldEntry entryToBeModified = new BaFieldEntry(id,
					fromSysprefix, fromField, toSysprefix, toField);
			try {
				BaFieldManager.getInstance().persistEntry(entryToBeModified);
				savedPropertiesCount++;
			} catch (CorrException e) {
				Log.error("Could not save properties to database", e);
				e.printStackTrace();
			} catch (Exception e) {
				Log.error("Could not save properties to database", e);
				e.printStackTrace();
			}
		}

		return savedPropertiesCount;
	}

	/**
	 * Get the list of fields of a business area
	 * 
	 * @param sysPrefix
	 * @return list of fields
	 */
	public ArrayList<FieldClient> getFields(String sysprefix) {
		ArrayList<FieldClient> fieldsList = new ArrayList<FieldClient>();
		ArrayList<Field> fields = new ArrayList<Field>();
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysprefix);

			if (null != Field.lookupBySystemId(ba.getSystemId())) { // getFieldsBySystemId(ba.getSystemId())){
				fields.addAll(Field.lookupBySystemId(ba.getSystemId()));
			} else {
				fieldsList.add(invalidField);
				return fieldsList;
			}
			for (Field field : fields) {
				FieldClient fieldClient = new FieldClient();

				fieldClient.setFieldId(field.getFieldId());
				fieldClient.setName(field.getName());
				fieldClient.setDisplayName(field.getDisplayName());
				fieldClient.setDescription(field.getDescription());
				fieldClient.setSystemId(field.getSystemId());

				fieldsList.add(fieldClient);
			}
		} catch (DatabaseException e) {
			Log.error("Could not load fields from business area..", e);
			e.printStackTrace();
		} catch (Exception e) {
			Log.error("Could not load fields from business area..", e);
			e.printStackTrace();
		}
		return fieldsList;
	}

	/**
	 * 
	 * @param key
	 * @return list of ba field map properties
	 */
	public ArrayList<BAFieldMapClient> gatherBAFieldMap(String fromBA,
			String toBA) {
		BaFieldKey key = new BaFieldKey(fromBA, toBA);

		ArrayList<BAFieldMapClient> baFieldMapProperties = new ArrayList<BAFieldMapClient>();
		HashMap<String, BaFieldEntry> baFieldMap = new HashMap<String, BaFieldEntry>();

		try {
			if (null == BaFieldManager.getBaFieldMapFromDB(key))
				return null;
			baFieldMap.putAll(BaFieldManager.getBaFieldMapFromDB(key));// if
																		// null?????
			for (String sysPrefix : baFieldMap.keySet()) {
				String id = Long.toString(baFieldMap.get(sysPrefix).getId());
				String fromSysprefix = baFieldMap.get(sysPrefix)
						.getFromSysPrefix();
				String fromField = baFieldMap.get(sysPrefix).getFromFieldName();
				String toSysprefix = baFieldMap.get(sysPrefix).getToSysPrefix();
				String toField = baFieldMap.get(sysPrefix).getToFieldName();

				BAFieldMapClient client = new BAFieldMapClient();
				client.setID(id);

				FieldClient fromClient = new FieldClient();
				fromClient.setName(fromField);
				client.setFromField(fromClient);
				client.setFromSysprefix(fromSysprefix);

				FieldClient toClient = new FieldClient();
				toClient.setName(toField);
				client.setToField(toClient);
				client.setToSysPrefix(toSysprefix);

				baFieldMapProperties.add(client);
			}

		} catch (CorrException e) {
			Log.error("Could not fetch ba field properties from database", e);
			e.printStackTrace();
		} catch (Exception e) {
			Log.error("Could not fetch ba field properties from database", e);
			e.printStackTrace();
		}

		return baFieldMapProperties;
	}

	public Integer deleteReportParamProperties(
			List<ReportParamsClient> properties) {
		Integer deletedPropertiesCount = 0;

		for (ReportParamsClient property : properties) {
			Long id = Long.valueOf(property.getId());
			if (-1 == id)
				continue;
			int reportId = Integer.valueOf(property.getReportId());
			String paramName = property.getParamName();
			String paramType = property.getParamType();
			String paramValueType = property.getParamValueType();
			String paramValue = property.getParamValue();

			ReportParamEntry entryToBeDeleted = new ReportParamEntry(id,
					reportId, paramType, paramName, paramValueType, paramValue);

			try {
				ReportParamsManager.getInstance().deleteEntry(entryToBeDeleted);
				deletedPropertiesCount++;
			} catch (CorrException e) {
				Log.error("Could not delete report param properties from database...");
				e.printStackTrace();
			} catch (Exception e) {
				Log.error("Could not delete report param properties from database...");
				e.printStackTrace();
			}
		}

		return deletedPropertiesCount;
	}

	public Integer setReportParamProperties(
			ArrayList<ReportParamsClient> properties) {
		Integer savedPropertiesCount = 0;
		for (ReportParamsClient property : properties) {
			Long id = Long.valueOf(property.getId());
			Integer reportId = Integer.valueOf(property.getReportId());
			String paramName = property.getParamName();
			String paramType = property.getParamType();
			String paramValueType = property.getParamValueType();
			String paramValue = property.getParamValue();

			ReportParamEntry entryToBeSaved = new ReportParamEntry(id,
					reportId, paramType, paramName, paramValueType, paramValue);

			try {
				ReportParamsManager.getInstance().persistEntry(entryToBeSaved);
				savedPropertiesCount++;
			} catch (CorrException e) {
				Log.error("Could not save report param properties to database",
						e);
				e.printStackTrace();
			} catch (Exception e) {
				Log.error("Could not save report param properties to database",
						e);
				e.printStackTrace();
			}
		}
		return savedPropertiesCount;
	}

	public ArrayList<ReportParamsClient> gatherReportParamProperties() {
		ArrayList<ReportParamsClient> reportParamsListMD = new ArrayList<ReportParamsClient>();
		ArrayList<ReportParamEntry> reportParamsList = new ArrayList<ReportParamEntry>();

		try {
			ArrayList<ReportNameEntry> rne = ReportNameManager
					.getReportParamMapId();
			for (ReportNameEntry reportNameEntry : rne) {
				int rId = reportNameEntry.getReportId();
				Hashtable<String, ReportParamEntry> map = 
						ReportParamsManager.getReportParamMapFromCache(rId);
				 if (map == null)
				 {
					 continue;
				 }
				for (String key : map.keySet()) {

					ReportParamsClient paramClient = new ReportParamsClient();
					paramClient.setId(Long.toString(map.get(key).getId()));
					paramClient.setReportId(Integer.toString(map.get(key)
							.getReportId()));
					paramClient.setParamName(map.get(key).getParamName());
					paramClient.setParamType(map.get(key).getParamType());
					paramClient.setParamValueType(map.get(key)
							.getParamValueType());
					paramClient.setParamValue(map.get(key).getParamValue());

					reportParamsListMD.add(paramClient);
				}
			}

		} catch (CorrException e) {
			Log.error("Could not load report param properties from database", e);
			e.printStackTrace();
		} catch (Exception e) {
			Log.error("Could not load report param properties from database", e);
			e.printStackTrace();
		}
		return reportParamsListMD;
	}

	public ArrayList<ReportNameClient> gatherReportNameMapProperties() {
		ArrayList<ReportNameClient> reportNameListMD = new ArrayList<ReportNameClient>();
		ArrayList<ReportNameEntry> reportNameList = new ArrayList<ReportNameEntry>();

		try {
			if (null != ReportNameManager.lookupCompleteReportNameMap()) {
				reportNameList.addAll(ReportNameManager
						.lookupCompleteReportNameMap());
			} else
				return null;

			for (ReportNameEntry entry : reportNameList) {
				ReportNameClient property = new ReportNameClient();
				property.setId(Long.toString(entry.getId()));
				property.setReportId(Integer.toString(entry.getReportId()));
				property.setReportFileName(entry.getReportFileName());

				reportNameListMD.add(property);
			}

		} catch (CorrException e) {
			Log.error("Could not fetch correspondence report map properties ",
					e);
			e.printStackTrace();
		}

		return reportNameListMD;
	}

	public Integer deleteReportNameMapProperties(
			List<ReportNameClient> properties) {
		Integer deletedPropertyCount = 0;

		for (ReportNameClient property : properties) {
			Long id = Long.valueOf(property.getId());
			if (-1 == id)
				continue;
			Integer reportId = Integer.valueOf(property.getReportId());
			String reportFileName = property.getReportFileName();

			ReportNameEntry entryToBeDeleted = new ReportNameEntry(id,
					reportId, reportFileName);

			try {
				ReportNameManager.getInstance().deleteEntry(entryToBeDeleted);
				deletedPropertyCount++;
			} catch (CorrException e) {
				Log.error("Could not set correspondence report name property",
						e);
				e.printStackTrace();
			} catch (Exception e) {
				Log.error("Could not set correspondence report name property",
						e);
				e.printStackTrace();
			}
		}

		return deletedPropertyCount;
	}

	public Integer saveReportNameMapProperties(
			ArrayList<ReportNameClient> properties) {
		Integer savedPropertiesCount = 0;

		for (ReportNameClient property : properties) {
			Long id = Long.valueOf(property.getId());
			Integer reportId = Integer.valueOf(property.getReportId());
			String reportFileName = property.getReportFileName();

			ReportNameEntry entryToBeAdded = new ReportNameEntry(id, reportId,
					reportFileName);

			try {
				ReportNameManager.getInstance().persistEntry(entryToBeAdded);
				savedPropertiesCount++;
			} catch (CorrException e) {
				Log.error("Could not set correspondence report name property",
						e);
				e.printStackTrace();
			} catch (Exception e) {
				Log.error("Could not set correspondence report name property",
						e);
				e.printStackTrace();
			}
		}

		return savedPropertiesCount;
	}

	/**
	 * Deletes correspondence report map properties from the table
	 * corr_report_map
	 * 
	 * @param properties
	 *            : list of properties to be deleted
	 * @return count : number of properties successfully deleted from database
	 */
	public Integer deleteReportMapProperties(List<ReportMapClient> properties) {
		Integer deletedPropertyCount = 0;
		for (ReportMapClient property : properties) {
			Long id = Long.valueOf(property.getId());
			if (-1 == id)
				continue;
			Integer reportId = Integer.valueOf(property.getReportId());
			String sysPrefix = property.getSysPrefix();
			String reportType1 = property.getType1().getName();
			String reportType2 = property.getType2().getName();
			String reportType3 = property.getType3().getName();
			String reportType4 = property.getType4().getName();
			String reportType5 = property.getType5().getName();

			ReportEntry entryToBeDeleted = new ReportEntry(id, sysPrefix,
					reportType1, reportType2, reportType3, reportType4,
					reportType5, reportId);

			try {
				ReportManager.getInstance().deleteEntry(entryToBeDeleted);
				deletedPropertyCount++;
			} catch (CorrException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return deletedPropertyCount;
	}

	/**
	 * Returns types corresponding to report type columns
	 * 
	 * @param sysPrefix
	 * @return Collection : of types for respective report types in specified
	 *         business area
	 */
	public ReportTypeClient getReportTypes(String sysPrefix) {
		ReportTypeClient reportTypes = new ReportTypeClient();
		if (getTypeValues(GenericParams.ReportType1, sysPrefix) != null)
			reportTypes.addReportTypeList(GenericParams.ReportType1,
					getTypeValues(GenericParams.ReportType1, sysPrefix));
		if (getTypeValues(GenericParams.ReportType2, sysPrefix) != null)
			reportTypes.addReportTypeList(GenericParams.ReportType2,
					getTypeValues(GenericParams.ReportType2, sysPrefix));
		if (getTypeValues(GenericParams.ReportType3, sysPrefix) != null)
			reportTypes.addReportTypeList(GenericParams.ReportType3,
					getTypeValues(GenericParams.ReportType3, sysPrefix));
		if (getTypeValues(GenericParams.ReportType4, sysPrefix) != null)
			reportTypes.addReportTypeList(GenericParams.ReportType4,
					getTypeValues(GenericParams.ReportType4, sysPrefix));
		if (getTypeValues(GenericParams.ReportType5, sysPrefix) != null)
			reportTypes.addReportTypeList(GenericParams.ReportType5,
					getTypeValues(GenericParams.ReportType5, sysPrefix));
		return reportTypes;
	}

	protected ArrayList<TypeClient> getTypeValues(String typeName,
			String sysPrefix) {
		ArrayList<TypeClient> typeValues = new ArrayList<TypeClient>();
		try {
			FieldNameEntry field = FieldNameManager.lookupFieldNameEntry(
					sysPrefix, typeName);

			if (field == null) {
				typeValues.add(invalidType);
				return typeValues;
			} else {
				BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
				Field f = Field.lookupBySystemIdAndFieldName(ba.getSystemId(),
						field.getBaFieldName());
				ArrayList<Type> typeList = Type
						.lookupAllBySystemIdAndFieldName(ba.getSystemId(),
								f.getName());
				for (Type type : typeList) {
					TypeClient typeClient = new TypeClient();

					typeClient.setName(type.getName());
					typeClient.setDisplayName(type.getDisplayName());
					typeClient.setDescription(type.getDescription());
					typeClient.setSystemId(type.getSystemId());
					typeClient.setTypeId(type.getTypeId());
					typeValues.add(typeClient);
				}
			}
		} catch (CorrException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return typeValues;
	}

	/**
	 * Save the properties in corr_report_map
	 * 
	 * @param properties
	 *            : list of properties to be deleted
	 * @return : properties with status indicating which properties have been
	 *         deleted and which not
	 */
	public ArrayList<ReportMapClient> setReportMapProperties(
			ArrayList<ReportMapClient> properties) {

		ArrayList<ReportMapClient> savedPropertiesList = new ArrayList<ReportMapClient>();
		Integer savedPropertiesCount = 0;

		for (ReportMapClient property : properties) {
			boolean errorFlag = false;
			String statusMessage = "";

			Long id = Long.valueOf(property.getId());
			Integer reportId = Integer.valueOf(property.getReportId());
			String sysPrefix = property.getSysPrefix();

			if (-1 == reportId) {
				statusMessage = statusMessage + " [ID] ";
				errorFlag = true;
			}

			String reportType1 = "";
			TypeClient type1Client = property.getType1();
			if ((type1Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.ReportType1, sysPrefix))) {
				reportType1 = null;
			} else if ((type1Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.ReportType1, sysPrefix)))) {
				statusMessage = statusMessage + " [RT:1] ";
				errorFlag = true;
			} else if ((!type1Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.ReportType1, sysPrefix))) {
				statusMessage = statusMessage + " [RT:1] ";
				errorFlag = true;
			} else if ((!type1Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.ReportType1, sysPrefix))) {
				if (null == getType(GenericParams.ReportType1,
						type1Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [RT:1] ";
					errorFlag = true;
				} else {
					reportType1 = type1Client.getName();
				}
			}

			String reportType2 = "";
			TypeClient type2Client = property.getType2();
			if ((type2Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.ReportType2, sysPrefix))) {
				reportType2 = null;
			} else if ((type2Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.ReportType2, sysPrefix)))) {
				statusMessage = statusMessage + " [RT:2] ";
				errorFlag = true;
			} else if ((!type2Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.ReportType2, sysPrefix))) {
				statusMessage = statusMessage + " [RT:2] ";
				errorFlag = true;
			} else if ((!type2Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.ReportType2, sysPrefix))) {
				if (null == getType(GenericParams.ReportType2,
						type2Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [RT:2] ";
					errorFlag = true;
				} else {
					reportType2 = type2Client.getName();
				}
			}

			String reportType3 = "";
			TypeClient type3Client = property.getType3();
			if ((type3Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.ReportType3, sysPrefix))) {
				reportType3 = null;
			} else if ((type3Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.ReportType3, sysPrefix)))) {
				statusMessage = statusMessage + " [RT:3] ";
				errorFlag = true;
			} else if ((!type3Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.ReportType3, sysPrefix))) {
				statusMessage = statusMessage + " [RT:3] ";
				errorFlag = true;
			} else if ((!type3Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.ReportType3, sysPrefix))) {
				if (null == getType(GenericParams.ReportType3,
						type3Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [RT:3] ";
					errorFlag = true;
				} else {
					reportType3 = type3Client.getName();
				}
			}

			String reportType4 = "";
			TypeClient type4Client = property.getType4();
			if ((type4Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.ReportType4, sysPrefix))) {
				reportType4 = null;
			} else if ((type4Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.ReportType4, sysPrefix)))) {
				statusMessage = statusMessage + " [RT:4] ";
				errorFlag = true;
			} else if ((!type4Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.ReportType4, sysPrefix))) {
				statusMessage = statusMessage + " [RT:4] ";
				errorFlag = true;
			} else if ((!type4Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.ReportType4, sysPrefix))) {
				if (null == getType(GenericParams.ReportType4,
						type4Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [RT:4] ";
					errorFlag = true;
				} else {
					reportType4 = type4Client.getName();
				}
			}

			String reportType5 = "";
			TypeClient type5Client = property.getType5();
			if ((type5Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.ReportType5, sysPrefix))) {
				reportType5 = null;
			} else if ((type5Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.ReportType5, sysPrefix)))) {
				statusMessage = statusMessage + " [RT:5] ";
				errorFlag = true;
			} else if ((!type5Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.ReportType5, sysPrefix))) {
				statusMessage = statusMessage + " [RT:5] ";
				errorFlag = true;
			} else if ((!type5Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.ReportType5, sysPrefix))) {
				if (null == getType(GenericParams.ReportType5,
						type5Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [RT:5] ";
					errorFlag = true;
				} else {
					reportType5 = type5Client.getName();
				}
			}
			if (!errorFlag) {
				try {
					ReportEntry propToBeSet = new ReportEntry(id, sysPrefix,
							reportType1, reportType2, reportType3, reportType4,
							reportType5, reportId);
					ReportManager.getInstance().persistEntry(propToBeSet);
					property.setStatus("OK");

					savedPropertiesCount++;
				} catch (CorrException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				property.setStatus(statusMessage);
			}

			savedPropertiesList.add(property);
		}
		return savedPropertiesList;
	}

	/**
	 * Fetch the report map properties from corr_report_map table
	 * 
	 * @param sysPrefix
	 *            of the BA whose properties are to be fetched
	 * @return List of properties corresponding to that BA
	 */
	public ArrayList<ReportMapClient> gatherReportMapProperties(String sysPrefix) {

		ArrayList<ReportEntry> reportMapList = new ArrayList<ReportEntry>();
		ArrayList<ReportMapClient> reportMapPropListMD = new ArrayList<ReportMapClient>();

		try {
			if (ReportManager.getReportMapFromCache(sysPrefix) != null)
				reportMapList.addAll(ReportManager
						.getReportMapFromCache(sysPrefix));
			else
				return null;
		} catch (CorrException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (ReportEntry entry : reportMapList) {

			String statusMessage = "";
			boolean errorFlag = false;

			ReportMapClient temp = new ReportMapClient();

			temp.setId(Long.toString(entry.getId()));
			temp.setReportId(Long.toString(entry.getReportId()));
			temp.setSysPrefix(entry.getSysPrefix());

			TypeClient type1Client = new TypeClient();
			if (null != getType(GenericParams.ReportType1, entry.getType1(),
					entry.getSysPrefix())) {
				type1Client = new TypeClient();
				type1Client = getType(GenericParams.ReportType1,
						entry.getType1(), entry.getSysPrefix());

			} else
				type1Client = null;
			if ((null == entry.getType1())
					&& (!isTypeSet(GenericParams.ReportType1, sysPrefix))) {
				temp.setType1(invalidType);
			} else if ((null == entry.getType1())
					&& (isTypeSet(GenericParams.ReportType1, sysPrefix))) {
				temp.setType1(invalidType);
				statusMessage = statusMessage + " [RT:1] ";
				errorFlag = true;
			} else if ((null != entry.getType1()) && (null == type1Client)) {
				invalidType.setName(entry.getType1());
				temp.setType1(invalidType);
				statusMessage = statusMessage + " [RT:1] ";
				errorFlag = true;
			} else if ((null != entry.getType1()) && (null != type1Client)) {
				temp.setType1(type1Client);
			}

			TypeClient type2Client = new TypeClient();
			if (null != getType(GenericParams.ReportType2, entry.getType2(),
					entry.getSysPrefix())) {
				type2Client = new TypeClient();
				type2Client = getType(GenericParams.ReportType2,
						entry.getType2(), entry.getSysPrefix());

			} else
				type2Client = null;
			if ((null == entry.getType2())
					&& (!isTypeSet(GenericParams.ReportType2, sysPrefix))) {
				temp.setType2(invalidType);
			} else if ((null == entry.getType2())
					&& (isTypeSet(GenericParams.ReportType2, sysPrefix))) {
				temp.setType2(invalidType);
				statusMessage = statusMessage + " [RT:2] ";
				errorFlag = true;
			} else if ((null != entry.getType2()) && (null == type2Client)) {
				invalidType.setName(entry.getType2());
				temp.setType2(invalidType);
				statusMessage = statusMessage + " [RT:2] ";
				errorFlag = true;
			} else if ((null != entry.getType2()) && (null != type2Client)) {
				temp.setType2(type2Client);
			}

			TypeClient type3Client = new TypeClient();
			if (null != getType(GenericParams.ReportType3, entry.getType3(),
					entry.getSysPrefix())) {
				type3Client = new TypeClient();
				type3Client = getType(GenericParams.ReportType3,
						entry.getType3(), entry.getSysPrefix());
			} else
				type3Client = null;
			if ((null == entry.getType3())
					&& (!isTypeSet(GenericParams.ReportType3, sysPrefix))) {
				temp.setType3(invalidType);
			} else if ((null == entry.getType3())
					&& (isTypeSet(GenericParams.ReportType3, sysPrefix))) {
				temp.setType3(invalidType);
				statusMessage = statusMessage + " [RT:3] ";
				errorFlag = true;
			} else if ((null != entry.getType3()) && (null == type3Client)) {
				invalidType.setName(entry.getType3());
				temp.setType3(invalidType);
				statusMessage = statusMessage + " [RT:3] ";
				errorFlag = true;
			} else if ((null != entry.getType3()) && (null != type3Client)) {
				temp.setType3(type3Client);
			}

			TypeClient type4Client = new TypeClient();
			if (null != getType(GenericParams.ReportType4, entry.getType4(),
					entry.getSysPrefix())) {
				type4Client = new TypeClient();
				type4Client = getType(GenericParams.ReportType4,
						entry.getType4(), entry.getSysPrefix());
			} else
				type4Client = null;
			if ((null == entry.getType4())
					&& (!isTypeSet(GenericParams.ReportType4, sysPrefix))) {
				temp.setType4(invalidType);
			} else if ((null == entry.getType4())
					&& (isTypeSet(GenericParams.ReportType4, sysPrefix))) {
				temp.setType4(invalidType);
				statusMessage = statusMessage + " [RT:4] ";
				errorFlag = true;
			} else if ((null != entry.getType4()) && (null == type4Client)) {
				invalidType.setName(entry.getType4());
				temp.setType4(invalidType);
				statusMessage = statusMessage + " [RT:4] ";
				errorFlag = true;
			} else if ((null != entry.getType4()) && (null != type4Client)) {
				temp.setType4(type4Client);
			}

			TypeClient type5Client = new TypeClient();
			if (null != getType(GenericParams.ReportType5, entry.getType5(),
					entry.getSysPrefix())) {
				type5Client = new TypeClient();
				type5Client = getType(GenericParams.ReportType5,
						entry.getType5(), entry.getSysPrefix());
			} else
				type5Client = null;
			if ((null == entry.getType5())
					&& (!isTypeSet(GenericParams.ReportType5, sysPrefix))) {
				temp.setType5(invalidType);
			} else if ((null == entry.getType5())
					&& (isTypeSet(GenericParams.ReportType5, sysPrefix))) {
				temp.setType5(invalidType);
				statusMessage = statusMessage + " [RT:5] ";
				errorFlag = true;
			} else if ((null != entry.getType5()) && (null == type5Client)) {
				invalidType.setName(entry.getType5());
				temp.setType5(invalidType);
				statusMessage = statusMessage + " [RT:5] ";
				errorFlag = true;
			} else if ((null != entry.getType5()) && (null != type5Client)) {
				temp.setType5(type5Client);
			}

			if (!errorFlag)
				temp.setStatus("OK");
			else
				temp.setStatus(statusMessage);

			reportMapPropListMD.add(temp);
		}

		return reportMapPropListMD;
	}

	/**
	 * @param reportType
	 *            : one of the report type fields in corr_report_map
	 * @param sysPrefix
	 * @return Whether the report type field in the specified BA is set
	 */
	protected boolean isTypeSet(String reportType, String sysPrefix) {
		try {
			FieldNameEntry field = FieldNameManager.lookupFieldNameEntry(
					sysPrefix, reportType);
			if (null != field)
				return true;
		} catch (CorrException e) {
			Log.error("Error getting report type field", e);
			e.printStackTrace();
		} catch (Exception e) {
			Log.error("Error getting report type field", e);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Searches the types for the specified report type field against the
	 * reportTypeValue parameter if
	 * 
	 * @param reportType
	 *            : one of the report type fields in corr_report_types
	 * @param reportTypeValue
	 *            : one of the types in the report type field
	 * @param sysPrefix
	 *            : specified business area
	 * @return The 'TypeClient' object corresponding to reportTypeValue, null if
	 *         no match found
	 */
	protected TypeClient getType(String typeName, String typeValue,
			String sysPrefix) {

		if (null == typeValue)
			return null;
		try {
			FieldNameEntry field = FieldNameManager.lookupFieldNameEntry(
					sysPrefix, typeName);
			if (field == null) {
				return null; // field has not been configured in field map table
			} else {
				BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
				Field f = Field.lookupBySystemIdAndFieldName(ba.getSystemId(),
						field.getBaFieldName());
				ArrayList<Type> typeList = Type
						.lookupAllBySystemIdAndFieldName(ba.getSystemId(),
								f.getName());
				for (Type type : typeList) {
					if (type.getName().equals(typeValue)) {
						TypeClient tempClient = new TypeClient();
						tempClient.setName(type.getName());
						tempClient.setDisplayName(type.getDisplayName());
						tempClient.setDescription(type.getDescription());
						tempClient.setSystemId(type.getSystemId());
						tempClient.setTypeId(type.getTypeId());

						return tempClient;
					}
				}
			}
		} catch (CorrException e) {
			Log.error("Error getting report type value", e);
			e.printStackTrace();
		} catch (Exception e) {
			Log.error("Error getting report type value", e);
			e.printStackTrace();
		}
		return null; // no entry found in types list
	}

	/**
	 * Delete the correspondence protocol properties
	 * 
	 * @param properties
	 *            : to be deleted
	 * @return number of properties successfully deleted
	 */
	public Integer deleteCorrProtocolProperties(
			List<CorrProtocolClient> properties) {
		Integer deletedCount = 0;
		for (CorrProtocolClient property : properties) {
			String sysPrefix = property.getSysPrefix();
			Long id = Long.valueOf(property.getId());
			String prop = property.getProperty();
			String value = property.getPropertyValue();
			String desc = property.getDescription();

			ProtocolOptionEntry propertyToBeDeleted = new ProtocolOptionEntry(
					id, sysPrefix, prop, value, desc);

			try {
				ProtocolOptionsManager.getInstance().deleteEntry(
						propertyToBeDeleted);
				deletedCount++;
			} catch (CorrException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return deletedCount;
	}

	/**
	 * 
	 * @param list
	 *            of properties to be set
	 * @return Number of properties successfully set in the db
	 */
	public Integer setCorrProtocolProperties(List<CorrProtocolClient> properties) {
		Integer propertyCount = 0;
		for (CorrProtocolClient property : properties) {

			String sysPrefix = property.getSysPrefix();
			Long id = Long.valueOf(property.getId());
			String prop = property.getProperty();
			String value = property.getPropertyValue();
			String desc = property.getDescription();

			ProtocolOptionEntry propertyToBeSet = new ProtocolOptionEntry(id,
					sysPrefix, prop, value, desc);

			try {
				ProtocolOptionsManager.getInstance().persistEntry(
						propertyToBeSet);
				propertyCount++;
			} catch (CorrException e) {
				e.printStackTrace();
			}
		}
		return propertyCount;
	}

	/**
	 * 
	 * @param sysPrefix
	 *            of the ba for which the properties are to be fetched
	 * @return List of correspondence protocol properties after being converted
	 *         from protocolOptionEntry to CorrProtocolClient type
	 */
	public ArrayList<CorrProtocolClient> gatherCorrProtocolProperties(
			String sysPrefix) {
		HashMap<String, ProtocolOptionEntry> corrProtocolPropMap = new HashMap<String, ProtocolOptionEntry>();
		ArrayList<CorrProtocolClient> corrProtocolPropListMD = new ArrayList<CorrProtocolClient>();

		try {
			if (ProtocolOptionsManager.lookupAllProtocolEntry(sysPrefix) == null)
				return null;
			corrProtocolPropMap.putAll(ProtocolOptionsManager
					.lookupAllProtocolEntry(sysPrefix));
		} catch (CorrException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String entry : corrProtocolPropMap.keySet()) {
			CorrProtocolClient temp = new CorrProtocolClient();

			temp.setSysPrefix(corrProtocolPropMap.get(entry).getSysPrefix());
			temp.setId(Long.toString(corrProtocolPropMap.get(entry).getId()));
			temp.setProperty(corrProtocolPropMap.get(entry).getName());
			temp.setPropertyValue(corrProtocolPropMap.get(entry).getValue());
			temp.setDescription(corrProtocolPropMap.get(entry).getDescription());

			corrProtocolPropListMD.add(temp);
		}

		return corrProtocolPropListMD;
	}

	/**
	 * @param list
	 *            of properties to be deleted
	 * @return number of properties successfully deleted from database
	 */
	public Integer deleteCorrProperty(List<CorrPropertiesClient> properties) {
		Integer deletedCount = 0;
		for (CorrPropertiesClient property : properties) {

			long id = Long.valueOf(property.getId());
			/*
			 * Implies that the property was not saved before being deleted.
			 * Hence, no entry is actually present in the database corresponding
			 * to that property.
			 */
			if (-1 == id)
				continue;
			String prop = property.getProperty();
			String value = property.getPropertyValue();
			String description = property.getDescription();

			PropertyEntry propToBeDeleted = new PropertyEntry(id, prop, value,
					description);

			try {
				PropertyManager.getInstance().deleteEntry(propToBeDeleted);
				deletedCount++;
			} catch (CorrException e) {
				e.printStackTrace();
				return 0;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
		return deletedCount;
	}

	/**
	 * This receives a list of properties from the client and sets those
	 * properties in the db
	 * 
	 * @param properties
	 * @return count of the properties that were set in the db
	 */
	public Integer setCorrProperties(List<CorrPropertiesClient> properties) {
		Integer propertyCount = 0;
		for (CorrPropertiesClient temp : properties) {
			long id = Long.valueOf(temp.getId());
			String property = temp.getProperty();
			String value = temp.getPropertyValue();
			String description = temp.getDescription();

			PropertyEntry propToBeSet = new PropertyEntry(id, property, value,
					description);

			try {
				PropertyManager.getInstance().persistEntry(propToBeSet);
				propertyCount++;
			} catch (CorrException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return propertyCount;
	}

	/**
	 * Gathers the correspondence properties by using predefined methods.
	 * Converts the available values so as to be usable by the grid later on.
	 */
	public ArrayList<CorrPropertiesClient> gatherCorrProperties() {
		ArrayList<PropertyEntry> corrPropertiesList = new ArrayList<PropertyEntry>();
		ArrayList<CorrPropertiesClient> corrPropertiesListMD = new ArrayList<CorrPropertiesClient>();
		try {
			corrPropertiesList.addAll(PropertyManager.lookupAllProperties());
		} catch (CorrException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (PropertyEntry property : corrPropertiesList) {
			CorrPropertiesClient corrPropertyMD = new CorrPropertiesClient();

			corrPropertyMD.setId(Long.toString(property.getId()));
			corrPropertyMD.setProperty(property.getName());
			corrPropertyMD.setPropertyValue(property.getValue());
			corrPropertyMD.setDescription(property.getDescription());

			corrPropertiesListMD.add(corrPropertyMD);
		}

		return corrPropertiesListMD;
	}

	/**
	 * Fetch the corr number config from corr_number_config
	 * 
	 * @param sysPrefix
	 *            of the BA whose properties are to be fetched
	 * @return List of properties corresponding to that BA
	 */

	public ArrayList<CorrNumberConfigClient> gatherCorrNumberConfigProperties(
			String sysPrefix) {
		HashMap<CorrNumberKey, CorrNumberEntry> corrNumberConfigList = new HashMap<CorrNumberKey, CorrNumberEntry>();
		ArrayList<CorrNumberConfigClient> corrNumberConfigListMD = new ArrayList<CorrNumberConfigClient>();

		try {

			if (null != CorrNumberManager.getCorrNumberFromDB(sysPrefix)) {
				corrNumberConfigList.putAll(CorrNumberManager
						.getCorrNumberFromDB(sysPrefix));
			} else
				return null;

			for (CorrNumberEntry entry : corrNumberConfigList.values()) {
				
				String statusMessage = "";
				boolean errorFlag = false;

				CorrNumberConfigClient temp = new CorrNumberConfigClient();
				temp.setId(Integer.toString(entry.getId()));
				temp.setSysPrefix(entry.getSysPrefix());

				TypeClient type1Client = new TypeClient();
				if (null != getType(GenericParams.NumType1,
						entry.getNumType1(), entry.getSysPrefix())) {
					type1Client = new TypeClient();
					type1Client = getType(GenericParams.NumType1,
							entry.getNumType1(), entry.getSysPrefix());
				} else
					type1Client = null;
				if ((null == entry.getNumType1())
						&& (!isTypeSet(GenericParams.NumType1,
								entry.getSysPrefix()))) {
					temp.setNumType1(invalidType);
				} else if ((null == entry.getNumType1())
						&& (isTypeSet(GenericParams.NumType1,
								entry.getSysPrefix()))) {
					temp.setNumType1(invalidType);
					statusMessage = statusMessage + " [OBT:1] ";
					errorFlag = true;
				} else if ((null != entry.getNumType1())
						&& (null == type1Client)) {
					invalidType.setName(entry.getNumType1());
					temp.setNumType1(invalidType);
					statusMessage = statusMessage + " [OBT:1] ";
					errorFlag = true;
				} else if ((null != entry.getNumType1())
						&& (null != type1Client)) {
					temp.setNumType1(type1Client);
				}

				TypeClient type2Client = new TypeClient();
				if (null != getType(GenericParams.NumType2,
						entry.getNumType2(), entry.getSysPrefix())) {
					type2Client = new TypeClient();
					type2Client = getType(GenericParams.NumType2,
							entry.getNumType2(), entry.getSysPrefix());
				} else
					type2Client = null;
				if ((null == entry.getNumType2())
						&& (!isTypeSet(GenericParams.NumType2,
								entry.getSysPrefix()))) {
					temp.setNumType2(invalidType);
				} else if ((null == entry.getNumType2())
						&& (isTypeSet(GenericParams.NumType2,
								entry.getSysPrefix()))) {
					temp.setNumType2(invalidType);
					statusMessage = statusMessage + " [OBT:2] ";
					errorFlag = true;
				} else if ((null != entry.getNumType2())
						&& (null == type2Client)) {
					invalidType.setName(entry.getNumType2());
					temp.setNumType2(invalidType);
					statusMessage = statusMessage + " [OBT:2] ";
					errorFlag = true;
				} else if ((null != entry.getNumType2())
						&& (null != type2Client)) {
					temp.setNumType2(type2Client);
				}

				TypeClient type3Client = new TypeClient();
				if (null != getType(GenericParams.NumType3,
						entry.getNumType3(), entry.getSysPrefix())) {
					type3Client = new TypeClient();
					type3Client = getType(GenericParams.NumType3,
							entry.getNumType3(), entry.getSysPrefix());
				} else
					type3Client = null;
				if ((null == entry.getNumType3())
						&& (!isTypeSet(GenericParams.NumType3,
								entry.getSysPrefix()))) {
					temp.setNumType3(invalidType);
				} else if ((null == entry.getNumType3())
						&& (isTypeSet(GenericParams.NumType3,
								entry.getSysPrefix()))) {
					temp.setNumType3(invalidType);
					statusMessage = statusMessage + " [OBT:3] ";
					errorFlag = true;
				} else if ((null != entry.getNumType3())
						&& (null == type3Client)) {
					invalidType.setName(entry.getNumType3());
					temp.setNumType3(invalidType);
					statusMessage = statusMessage + " [OBT:3] ";
					errorFlag = true;
				} else if ((null != entry.getNumType3())
						&& (null != type3Client)) {
					temp.setNumType3(type3Client);
				}

				temp.setNumFormat(entry.getNumberFormat());

				/*
				  FieldClient numField = new FieldClient();
				  numField.setName(entry.getNumberFields());
				 */
				temp.setNumFields(entry.getNumberFields());

				temp.setMaxIdFormat(entry.getMaxIdFormat());

				/*
				  FieldClient maxIdField = new FieldClient();
				  maxIdField.setName(entry.getMaxIdFields());
				 */
				temp.setMaxIdFields(entry.getMaxIdFields());
				
				if (!errorFlag)
					temp.setStatus("OK");
				else
					temp.setStatus(statusMessage);


				corrNumberConfigListMD.add(temp);

			}
		} catch (CorrException e) {
			Log.error("Error fetching user map properties from database..", e);
			e.printStackTrace();
		} catch (Exception e) {
			Log.error("Error fetching user map properties from database..", e);
			e.printStackTrace();
		}

		return corrNumberConfigListMD;
	}

	public ArrayList<CorrNumberConfigClient> setCorrNumberConfigProperties(
			ArrayList<CorrNumberConfigClient> properties) {
	//	Integer savedPropertiesCount = 0;
		 ArrayList<CorrNumberConfigClient> savedPropertiesList = new ArrayList<CorrNumberConfigClient>();
		for (CorrNumberConfigClient property : properties) {
			boolean errorFlag = false;
			String statusMessage = "";

			Integer id = Integer.valueOf(property.getId());
			String sysPrefix = property.getSysPrefix();

			String type1 = "";
			TypeClient type1Client = property.getNumType1();
			if ((type1Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.NumType1, sysPrefix))) {
				type1 = null;
			} else if ((type1Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.NumType1, sysPrefix)))) {
				statusMessage = statusMessage + " [OBT:1] ";
				errorFlag = true;
			} else if ((!type1Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.NumType1, sysPrefix))) {
				statusMessage = statusMessage + " [OBT:1] ";
				errorFlag = true;
			} else if ((!type1Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.NumType1, sysPrefix))) {
				if (null == getType(GenericParams.NumType1,
						type1Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [OBT:1] ";
					errorFlag = true;
				} else {
					type1 = type1Client.getName();
				}
			}

			String type2 = "";
			TypeClient type2Client = property.getNumType2();
			if ((type2Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.NumType2, sysPrefix))) {
				type2 = null;
			} else if ((type2Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.NumType2, sysPrefix)))) {
				statusMessage = statusMessage + " [OBT:2] ";
				errorFlag = true;
			} else if ((!type2Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.NumType2, sysPrefix))) {
				statusMessage = statusMessage + " [OBT:2] ";
				errorFlag = true;
			} else if ((!type2Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.NumType2, sysPrefix))) {
				if (null == getType(GenericParams.NumType2,
						type2Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [OBT:2] ";
					errorFlag = true;
				} else {
					type2 = type2Client.getName();
				}
			}

			String type3 = "";
			TypeClient type3Client = property.getNumType3();
			if ((type3Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.NumType3, sysPrefix))) {
				type3 = null;
			} else if ((type3Client.getName().equals("NULL") && (isTypeSet(
					GenericParams.NumType3, sysPrefix)))) {
				statusMessage = statusMessage + " [OBT:3] ";
				errorFlag = true;
			} else if ((!type3Client.getName().equals("NULL"))
					&& (!isTypeSet(GenericParams.NumType3, sysPrefix))) {
				statusMessage = statusMessage + " [OBT:3] ";
				errorFlag = true;
			} else if ((!type3Client.getName().equals("NULL"))
					&& (isTypeSet(GenericParams.NumType3, sysPrefix))) {
				if (null == getType(GenericParams.NumType3,
						type3Client.getName(), sysPrefix)) {
					statusMessage = statusMessage + " [OBT:3] ";
					errorFlag = true;
				} else {
					type3 = type3Client.getName();
				}
			}
			/*
			  String type1 = property.getNumType1().getName();
			  if(type1.equals("NULL")) type1 = null; String type2 =
			  property.getNumType2().getName(); if(type2.equals("NULL")) type2
			  = null; String type3 = property.getNumType3().getName();
			  if(type3.equals("NULL")) type3 = null;
			 */
			String numFormat = property.getNumFormat();
			String numField = property.getNumFields();
			/*if (numField.equals("NULL"))
				numField = null;*/
			String maxIdFormat = property.getMaxIdFormat();
			/*
			  if(maxIdFormat.equals("NULL")) maxIdFormat= null;
			 */
			String maxField = property.getMaxIdFields();
			/*
			  if(maxField.equals("NULL")||maxField.equals("")) maxField = null;
			 */
			if (!errorFlag) {
				CorrNumberEntry propertyToBeModified = new CorrNumberEntry(id,
						sysPrefix, type1, type2, type3, numFormat, numField,
						maxIdFormat, maxField);
				try {
					CorrNumberManager.getInstance().persistEntry(
							propertyToBeModified);
					statusMessage = "OK";
					property.setStatus(statusMessage);
			//		savedPropertiesCount++;
				} catch (CorrException e) {
					statusMessage = e.getMessage();
					property.setStatus(statusMessage);
					Log.error(
							"Could not get set corr number config properties to database...",
							e);
					e.printStackTrace();
				} catch (Exception e) {
					statusMessage = e.getMessage();
					property.setStatus(statusMessage);
					Log.error(
							"Could not get set corr number config properties to database...",
							e);
					e.printStackTrace();
				}
			} else {
				property.setStatus(statusMessage);
			}

			savedPropertiesList.add(property);
		}
		return savedPropertiesList;
	}

	public CorrNumberKeyClient getCorrNumberKey(String sysPrefix) {
		CorrNumberKeyClient corrNumberKey = new CorrNumberKeyClient();

		if (getTypeValues(GenericParams.NumType1, sysPrefix) != null)
			corrNumberKey.addCorrNumberKeyList(GenericParams.NumType1,
					getTypeValues(GenericParams.NumType1, sysPrefix));
		if (getTypeValues(GenericParams.NumType2, sysPrefix) != null)
			corrNumberKey.addCorrNumberKeyList(GenericParams.NumType2,
					getTypeValues(GenericParams.NumType2, sysPrefix));
		if (getTypeValues(GenericParams.NumType3, sysPrefix) != null)
			corrNumberKey.addCorrNumberKeyList(GenericParams.NumType3,
					getTypeValues(GenericParams.NumType3, sysPrefix));

		return corrNumberKey;
	}

	public Integer deleteCorrNumber(List<CorrNumberConfigClient> properties) {
		Integer deletedPropertyCount = 0;
		for (CorrNumberConfigClient property : properties) {

			Integer id = Integer.valueOf(property.getId());
			if (-1 == id)
				continue;
			String sysPrefix = property.getSysPrefix();
			String numType1 = property.getNumType1().getName();
			String numType2 = property.getNumType2().getName();
			String numType3 = property.getNumType3().getName();
			String numFormat = property.getNumFormat();
			String numFields = property.getNumFields();
			String maxIdFormat = property.getMaxIdFormat();
			String maxIdFields = property.getMaxIdFields();

			CorrNumberEntry propertyToBeDeleted = new CorrNumberEntry(id,
					sysPrefix, numType1, numType2, numType3, numFormat,
					numFields, maxIdFormat, maxIdFields);

			try {
				CorrNumberManager.getInstance()
						.deleteEntry(propertyToBeDeleted);
				deletedPropertyCount++;
			} catch (CorrException e) {
				Log.error(
						"Could not delete user map properties from database...",
						e);
				e.printStackTrace();
			} catch (Exception e) {
				Log.error(
						"Could not delete user map properties from database...",
						e);
				e.printStackTrace();
			}
		}

		return deletedPropertyCount;
	}
}
