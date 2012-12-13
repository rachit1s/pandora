/**
 * 
 */
package com.tbitsGlobal.admin.server;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.http.HttpServletRequest;
import javax.swing.text.StyledEditorKit.BoldAction;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import transbit.tbits.Escalation.EscalationCommonUtils;
import transbit.tbits.Escalation.EscalationConditionParameters;
import transbit.tbits.Escalation.EscalationCondition;
import transbit.tbits.Escalation.EscalationHierarchies;
import transbit.tbits.Escalation.EscalationHierarchyValues;


import transbit.tbits.Helper.AD2TBitsSync;
import transbit.tbits.admin.AdminAppProperties;
import transbit.tbits.admin.AdminCaptions;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.Mapper;
import transbit.tbits.authentication.AuthUtils;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.CaptionsProps;
import transbit.tbits.config.CustomLink;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.BAMailAccount;
import transbit.tbits.domain.BAMenu;
import transbit.tbits.domain.BAUser;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DisplayGroup;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.NotificationRule;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Report;
import transbit.tbits.domain.ReportRoles;
import transbit.tbits.domain.ReportSpecificUsers;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.events.EventFailureException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.mail.MailAuthenticator;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.rules.RuleClass;
import transbit.tbits.rules.RulesManager;
import transbit.tbits.scheduler.CronExpression;
import transbit.tbits.scheduler.JobSchedulingUtil;
import transbit.tbits.scheduler.TBitsScheduler;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobClass;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.webapps.MailingListHandler;
import transbit.tbits.webapps.ReportsTableModifier;
import transbit.tbits.webapps.WebUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tbitsGlobal.admin.client.APService;
import com.tbitsGlobal.admin.client.modelData.EscalationConditionDetailClient;

import com.tbitsGlobal.admin.client.modelData.EscalationConditionParametersClient;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchiesClient;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchyValuesClient;
import com.tbitsGlobal.admin.client.modelData.HolidayClient;
import com.tbitsGlobal.admin.client.modelData.MailingListUserClient;
import com.tbitsGlobal.admin.client.modelData.ReportParamClient;
import com.tbitsGlobal.admin.client.modelData.RolePermissionModel;
import com.tbitsGlobal.admin.client.modelData.SysInfoClient;
import com.tbitsGlobal.admin.client.permTool.PermissionInfo;
import com.tbitsGlobal.admin.client.services.JobActionService;
import com.tbitsGlobal.admin.client.utils.EscalationUtils;

import com.tbitsGlobal.admin.client.widgets.pages.UsersPage;
import com.tbitsGlobal.jaguar.server.utils.EscalationServerUtils;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMailAccountClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMenuClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.CustomLinkClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobClassClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobDetailClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobParameterClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.NotificationRuleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.ReportClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleUserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RulesClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeDependency;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeUserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.rules.RuleDef;
import commons.com.tbitsGlobal.utils.server.AdminUtilServiceImpl;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;
import commons.com.tbitsGlobal.utils.server.rules.ClassWriter;
import commons.com.tbitsGlobal.utils.server.rules.RulesTemplateRegistry;

/**
 * @author
 * 
 */
public class APServiceImpl extends AdminUtilServiceImpl implements APService {

	private static final long serialVersionUID = 1L;

	static {
		PluginManager.getInstance().loadJaguarPlugins();
	}

	// service helper class///////////////////////////////////////////////////
	private static class ServiceHelper {

		public static User validateUser(HttpServletRequest req)
				throws TbitsExceptionClient {
			User user = null;
			try {
				user = WebUtil.validateUser(req);
				return user;
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TbitsExceptionClient(e);
			} catch (TBitsException e) {
				e.printStackTrace();
				throw new TbitsExceptionClient(e);
			}
		}
	}// //////////////////////////////////////////////////////////////////

	public Integer getInt() {
		return null;
	}

	public Boolean updateBA(BusinessAreaClient baToUpdate)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		BusinessArea ba = new BusinessArea();
		GWTServiceHelper.setValuesInDomainObject(baToUpdate, ba);

		SysConfig sysConf = new SysConfig();
		GWTServiceHelper.setValuesInDomainObject(baToUpdate
				.getSysConfigObject(), sysConf);
		ba.setSysConfigObject(sysConf);

		ArrayList<CustomLink> customLinks = new ArrayList<CustomLink>();
		CustomLink customLink;
		ArrayList<CustomLinkClient> customLinkClients = baToUpdate
				.getSysConfigObject().getCustomLinks();
		if (customLinkClients != null) {
			for (CustomLinkClient customLinkClient : customLinkClients) {
				customLink = new CustomLink();
				GWTServiceHelper.setValuesInDomainObject(customLinkClient,
						customLink);
				customLinks.add(customLink);
			}
			sysConf.setCustomLinks(customLinks);
		}

		try {
			BusinessArea.update(ba);
			Mapper.refreshBOMapper();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	// public String getTypeDescriptors(String sys_prefix, int field_id) throws
	// TbitsExceptionClient {
	// User user = null;
	// try {
	// user = WebUtil.validateUser(this.getRequest());
	// } catch (DatabaseException e) {
	// e.printStackTrace();
	// throw new TbitsExceptionClient(e);
	// } catch (TBitsException e) {
	// e.printStackTrace();
	// throw new TbitsExceptionClient(e);
	// }
	// GWTServiceHelper.getTypeDesCriptor(sys_prefix, user, field_id);
	// return null;
	// }

	public FieldClient createNewField(String sys_prefix, String field_name,
			int field_type) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return null;

			ArrayList<Field> fList = Field.lookupBySystemId(ba.getSystemId());
			for (Field field : fList) {
				if (field.getName().equalsIgnoreCase(field_name)) {
					throw new TbitsExceptionClient("Field already exists");
				}
			}
			Field newField = new Field(ba.getSystemId(), 1, field_name,
					field_name, field_name, field_type, true, true, false, 0,
					47, "",false);
			try {
				newField = Field.insert(newField);
			} catch (TBitsException e) {
				throw new TbitsExceptionClient(e);
			}
			if (newField != null) {
				FieldClient fc = new FieldClient();
				GWTServiceHelper.setValuesInDomainObject(newField, fc);
				Mapper.refreshBOMapper();
				return fc;
			} else
				return null;

		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean deleteField(FieldClient field) throws TbitsExceptionClient {
		if (ServiceHelper.validateUser(this.getRequest()) == null)
			throw new TbitsExceptionClient("user not validated");
		Field f = new Field();
		GWTServiceHelper.setValuesInDomainObject(field, f);
		try {
			if (Field.delete(f) != null) {
				Mapper.refreshBOMapper();
				return true;
			}
			return false;

		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e.getMessage());
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e.getMessage());
		}
	}

	public boolean deleteFields(List<FieldClient> fields)
			throws TbitsExceptionClient {
		if (ServiceHelper.validateUser(this.getRequest()) == null)
			throw new TbitsExceptionClient("user not validated");
		for (FieldClient field : fields) {
			Field f = new Field();
			GWTServiceHelper.setValuesInDomainObject(field, f);
			try {
				if (Field.delete(f) == null) {
					return false;
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TbitsExceptionClient(e);
			} catch (TBitsException e) {
				e.printStackTrace();
				throw new TbitsExceptionClient(e);
			}
		}

		Mapper.refreshBOMapper();
		return true;
	}

	public FieldClient updateField(String sys_prefix, FieldClient fieldClient)
			throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return null;

			Field field = new Field();
			GWTServiceHelper.setValuesInDomainObject(fieldClient, field);
			if (Field.update(field) != null) {
				Mapper.refreshBOMapper();
				return fieldClient;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		return null;
	}

	public List<FieldClient> updateFields(String sysPrefix,
			List<FieldClient> fields) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if (ba == null)
				return null;

			List<FieldClient> resp = new ArrayList<FieldClient>();
			for (FieldClient fieldClient : fields) {
				Field field = new Field();
				GWTServiceHelper.setValuesInDomainObject(fieldClient, field);
				if (field.getFieldId() != 0) {
					
					field = Field.update(field);
				} else {
					field.setSystemId(ba.getSystemId());
					field.setIsExtended(true);
					field = Field.insert(field);
				}

				if (field != null) {
					FieldClient model = new FieldClient();
					GWTServiceHelper.setValuesInDomainObject(field, model);
					resp.add(model);
				}
			}

			Mapper.refreshBOMapper();

			return resp;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean createNewTypes(String sys_prefix, String field_name,
			int field_id, ArrayList<String> types_name)
			throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return false;

			ArrayList<String> toBeAdded = new ArrayList<String>();
			for (String type_name : types_name) {
				boolean repeat = false;
				for (String s : toBeAdded) {
					if (s.trim().equals(type_name.trim())) {
						repeat = true;
						break;
					}
				}
				if (!repeat) {
					toBeAdded.add(type_name);
				}
			}
			ArrayList<Type> tlist = Type.lookupAllBySystemIdAndFieldName(ba
					.getSystemId(), field_name);
			for (String type_name : toBeAdded) {
				for (Type t : tlist) {
					if (t.getName().equalsIgnoreCase(type_name)) {
						System.out.println("Type already exists");
						throw new TbitsExceptionClient("Type already exists");
					}

				}
				Type type = new Type(ba.getSystemId(), field_id, 1, type_name,
						type_name, type_name, 0, true, false, true, false,
						false);
				Type.insert(type);
			}

			Mapper.refreshBOMapper();
			return true;

		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public TypeClient createNewType(String sys_prefix, String field_name,
			int field_id, String type_name) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return null;

			ArrayList<Type> tlist = Type.lookupAllBySystemIdAndFieldName(ba
					.getSystemId(), field_name);
			boolean isDefaultPresent = false;
			for (Type t : tlist) {
				if (t.getName().equalsIgnoreCase(type_name)) {
					System.out.println("Type already exists");
					throw new TbitsExceptionClient("Type already exists");
				}
				isDefaultPresent = t.getIsDefault();
			}
			boolean isDefault = !isDefaultPresent;

			Type type = new Type(ba.getSystemId(), field_id, 1, type_name,
					type_name, type_name, 0, true, false, true, false,
					false);
			if (Type.insert(type) != null) {
				Mapper.refreshBOMapper();
				TypeClient tc = new TypeClient();
				type = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba
						.getSystemId(), field_name, type_name);
				GWTServiceHelper.setValuesInDomainObject(type, tc);
				return tc;
			}
			return null;

		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public TypeClient deleteType(String sys_prefix, String fieldname,
			TypeClient typeClient) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return null;

			Type type = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba
					.getSystemId(), fieldname, typeClient.getName());
			Type deletedType = Type.delete(type);
			if (deletedType == null)
				return null;
			Mapper.refreshBOMapper();
			return typeClient;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean deleteTypes(String sys_prefix, String fieldname,
			List<TypeClient> types) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return false;

			for (TypeClient typeClient : types) {
				Type type = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba
						.getSystemId(), fieldname, typeClient.getName());
				Type.delete(type);
			}
			Mapper.refreshBOMapper();
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public TypeClient updateType(String sys_prefix, String fieldname,
			TypeClient typeClient) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return null;

			Type type = new Type();
			GWTServiceHelper.setValuesInDomainObject(typeClient, type);
			Type.update(type);
			Mapper.refreshBOMapper();

			return typeClient;
			// TODO something about type descriptor
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public List<TypeClient> updateTypes(String sys_prefix, String fieldname,
			List<TypeClient> types) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return null;

			Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(),
					fieldname);
			if (field == null)
				return null;

			List<TypeClient> resp = new ArrayList<TypeClient>();

			for (TypeClient typeClient : types) {
				Type type = new Type();
				GWTServiceHelper.setValuesInDomainObject(typeClient, type);
				int index = types.indexOf(typeClient);
				type.setOrdering(index);

				if (type.getTypeId() != 0)
					type = Type.update(type);
				else {
					type.setSystemId(ba.getSystemId());
					type.setFieldId(field.getFieldId());
					type = Type.insert(type);
				}

				if (type != null) {
					TypeClient model = new TypeClient();
					GWTServiceHelper.setValuesInDomainObject(type, model);
					resp.add(model);
				}
			}

			Mapper.refreshBOMapper();

			return resp;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public List<FieldClient> getFieldClients(String sys_prefix)
			throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return null;

			ArrayList<Field> fields = Field.lookupBySystemId(ba.getSystemId());
			if (fields == null)
				return null;

			ArrayList<FieldClient> response = new ArrayList<FieldClient>();
			FieldClient fc;

			for (Field f : fields) {
				fc = new FieldClient();
				GWTServiceHelper.setValuesInDomainObject(f, fc);
				response.add(fc);
			}
			return response;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public ArrayList<TypeClient> getTypeList(String sys_prefix,
			String field_name) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return null;

			ArrayList<Type> tlist = null;
			tlist = Type.lookupAllBySystemIdAndFieldName(ba.getSystemId(),
					field_name);
			if (tlist == null) {
				return null;
			}
			ArrayList<TypeClient> response = new ArrayList<TypeClient>();
			for (Type t : tlist) {
				TypeClient tc = new TypeClient();
				GWTServiceHelper.setValuesInDomainObject(t, tc);
				response.add(tc);
			}
			return response;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public HashMap<String, HashMap<Integer, TypeClient>> getTypeList(
			String sys_prefix) throws TbitsExceptionClient {
		try {
			HashMap<String, HashMap<Integer, TypeClient>> response = new HashMap<String, HashMap<Integer, TypeClient>>();
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			if (ba == null)
				return null;

			ArrayList<Field> fields = null;
			fields = Field.lookupBySystemId(ba.getSystemId());
			for (Field f : fields) {
				ArrayList<Type> tlist = null;
				tlist = Type.lookupAllBySystemIdAndFieldName(ba.getSystemId(),
						f.getName());
				if (tlist == null) {
					continue;
				}
				HashMap<Integer, TypeClient> tclist = new HashMap<Integer, TypeClient>();
				for (Type t : tlist) {
					TypeClient tc = new TypeClient();
					GWTServiceHelper.setValuesInDomainObject(t, tc);
					tclist.put(tc.getTypeId(), tc);
				}
				response.put(f.getName(), tclist);
			}
			return response;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public HashMap<Integer, TypeUserClient> getTypeUser(String sysPrefix,
			int fieldId, int typeId) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			User user = ServiceHelper.validateUser(this.getRequest());
			if (ba == null || user == null)
				return null;
			HashMap<Integer, TypeUserClient> response = new HashMap<Integer, TypeUserClient>();
			ArrayList<TypeUser> tuList = TypeUser
					.lookupBySystemIdAndFieldIdAndTypeId(ba.getSystemId(),
							fieldId, typeId);
			if (tuList != null) {
				for (TypeUser tu : tuList) {
					TypeUserClient tuc = new TypeUserClient();
					GWTServiceHelper.setValuesInDomainObject(tu, tuc);
					// tuc.set(TypeUserClient.USER,
					// tu.getUser().getUserLogin());
					UserClient userClient = new UserClient();
					GWTServiceHelper.setValuesInDomainObject(tu.getUser(),
							userClient);
					tuc.setUser(userClient);

					response.put(tuc.getUserId(), tuc);
				}
			}
			return response;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	/**
	 * 
	 * 
	 */
	public BusinessAreaClient getBAClient(String sys_prefix)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
			return GWTServiceHelper.getBAClientBySysPrefix(sys_prefix);
	}

	public ArrayList<DisplayGroupClient> getDisplayGroups(String sys_prefix)
			throws TbitsExceptionClient {
		BusinessArea ba;
		try {
			ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			ArrayList<DisplayGroup> displayGroups = DisplayGroup
					.lookupIncludingDefaultForSystemId(ba.getSystemId());
			ArrayList<DisplayGroupClient> response = new ArrayList<DisplayGroupClient>();
			for (DisplayGroup displayGroup : displayGroups) {
				if (displayGroup == null)
					continue;
				DisplayGroupClient displayGroupClient = new DisplayGroupClient();
				GWTServiceHelper.setValuesInDomainObject(displayGroup,
						displayGroupClient);

				response.add(displayGroupClient);
			}
			return response;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public DisplayGroupClient updateDisplayGroup(String sysPrefix,
			DisplayGroupClient group) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if (ba == null)
				return null;

			DisplayGroup dg = new DisplayGroup();
			GWTServiceHelper.setValuesInDomainObject(group, dg);
			DisplayGroup.update(dg);

			return group;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public List<DisplayGroupClient> updateDisplayGroups(String sysPrefix,
			List<DisplayGroupClient> groups) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if (ba == null)
				return null;

			for (DisplayGroupClient dgc : groups) {
				DisplayGroup dg = new DisplayGroup();
				GWTServiceHelper.setValuesInDomainObject(dgc, dg);
				DisplayGroup.update(dg);
			}
			return groups;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public DisplayGroupClient insertDisplayGroup(String sysPrefix,
			String displayName, int order, boolean isActive, boolean isDefault)
			throws TbitsExceptionClient {
		BusinessArea ba;
		try {
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if (ba == null)
				return null;
			// Note that the id the second params is ignored.
			DisplayGroup dg = new DisplayGroup(ba.getSystemId(), 0,
					displayName, order, isActive, isDefault);
			if (DisplayGroup.insert(dg)) {
				Mapper.refreshBOMapper();
				dg = DisplayGroup.lookupBySystemIdAndDisplayName(ba
						.getSystemId(), displayName);
				DisplayGroupClient dgc = new DisplayGroupClient();
				GWTServiceHelper.setValuesInDomainObject(dg, dgc);
				return dgc;
			}
			return null;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public DisplayGroupClient deleteDisplayGroup(String sysPrefix,
			DisplayGroupClient dgc) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if (ba == null)
				return null;
			DisplayGroup dg = new DisplayGroup();
			GWTServiceHelper.setValuesInDomainObject(dgc, dg);
			dg = DisplayGroup.delete(dg);
			if (dg != null) {
				Mapper.refreshBOMapper();
				GWTServiceHelper.setValuesInDomainObject(dg, dgc);
				return dgc;
			}
			return null;

		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean deleteDisplayGroups(String sysPrefix,
			List<DisplayGroupClient> groups) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if (ba == null)
				return false;

			for (DisplayGroupClient dgc : groups) {
				DisplayGroup dg = new DisplayGroup();
				GWTServiceHelper.setValuesInDomainObject(dgc, dg);
				DisplayGroup.delete(dg);
			}

			Mapper.refreshBOMapper();

			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public List<FieldClient> getFieldsByDisplayGroup(
			DisplayGroupClient displayGroup) throws TbitsExceptionClient {
		int sysId = displayGroup.getSystemId();
		try {
			BusinessArea ba = BusinessArea.lookupBySystemId(sysId);
			if (ba != null) {
				List<FieldClient> allFields = getFieldClients(ba
						.getSystemPrefix());
				List<FieldClient> fields = new ArrayList<FieldClient>();
				for (FieldClient field : allFields) {
					if (field.getDisplayGroup() == displayGroup.getId())
						fields.add(field);
				}

				return fields;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		return null;
	}

	public List<RoleClient> getRoleBySysPrefix(String sysPrefix)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if (ba != null) {
				ArrayList<Role> roles = Role.getRolesBySysId(ba.getSystemId());
				ArrayList<RoleClient> roleClients = new ArrayList<RoleClient>();
				RoleClient rClient;
				for (Role role : roles) {
					rClient = new RoleClient();
					GWTServiceHelper.setValuesInDomainObject(role, rClient);
					assert rClient != null;
					assert rClient.getRoleId() != 0;
					roleClients.add(rClient);
				}
				return roleClients;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		return null;
	}

	public boolean insertUserhierarchy(int aSystemId, int aChildUserId,
			int aParentUserId) throws TbitsExceptionClient {
		BusinessArea ba;
		try {
			ba = BusinessArea.lookupBySystemId(aSystemId);
			if (ba == null)
				return false;
			String temp = transbit.tbits.Escalation.EscalationUtils.insertUserHierarchy(aSystemId,
					aChildUserId, aParentUserId);
			if (temp.startsWith("Matters"))
				return true;
			else
				return false;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean deleteUserhierarchy(int sysId, int userId, int parentId)
			throws TbitsExceptionClient {
		try {
			transbit.tbits.Escalation.EscalationUtils.deleteUserHierarchy(sysId, userId, parentId);
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public ArrayList<NotificationRuleClient> getNotifcation()
			throws TbitsExceptionClient {
		ArrayList<NotificationRuleClient> nlist = new ArrayList<NotificationRuleClient>();
		try {
			for (NotificationRule n : NotificationRule
					.getAllNotificationRules()) {
				NotificationRuleClient nc = new NotificationRuleClient();
				GWTServiceHelper.setValuesInDomainObject(n, nc);
				nlist.add(nc);
			}
			return nlist;
		} catch (DatabaseException e) {
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean deleteBAUsers(int sysId, List<UserClient> users) {
		for (UserClient user : users) {
			BAUser ob = new BAUser();
			ob.setIsActive(true);
			ob.setSystemId(sysId);
			ob.setUserId(user.getUserId());
			BAUser.delete(ob);
		}
		Mapper.refreshBOMapper();
		return true;
	}

	public ArrayList<UserClient> getBAUsers(String sysPrefix)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		ArrayList<UserClient> userClients = new ArrayList<UserClient>();
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			for (User u : BAUser.getBusinessAreaUsers(ba.getSystemId())) {
				UserClient uc = new UserClient();
				GWTServiceHelper.setValuesInDomainObject(u, uc);
				userClients.add(uc);
			}
			return userClients;

		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public ArrayList<RoleClient> getRolesbySysIDandUserID(int sysID, int userID)
			throws TbitsExceptionClient {
		BusinessArea ba;
		try {
			ba = BusinessArea.lookupBySystemId(sysID);
			if (ba == null)
				return null;
			ArrayList<Role> rolelist = Role.lookupRolesBySystemIdAndUserId(
					sysID, userID);
			ArrayList<RoleClient> list = new ArrayList<RoleClient>();

			for (Role role : rolelist) {
				RoleClient rc = new RoleClient();
				GWTServiceHelper.setValuesInDomainObject(role, rc);
				list.add(rc);
			}

			return list;

		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean updateRoleUser(int sysID, int userId,
			ArrayList<RoleUserClient> rucList) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemId(sysID);

			ArrayList<UserClient> userOfMailingList = new ArrayList<UserClient>();

			if (ba == null)
				return false;

			Boolean isMailingList = false;
			if (checkIfUserIsMailingList(userId, sysID)) {
				isMailingList = true;
			}

			if (isMailingList) {
				userOfMailingList = getUserForMailingList(userId);
			}
			ArrayList<Role> rolelist = Role.lookupRolesBySystemIdAndUserId(
					sysID, userId);

			for (Role r : rolelist) {
				RoleUser ru = new RoleUser();
				ru.setSystemId(sysID);
				ru.setUserId(userId);
				ru.setRoleId(r.getRoleId());
				if (!(r.getRoleName().equals("BAUsers")))
					RoleUser.delete(ru);

			}
			for (RoleUserClient ruc : rucList) {
				RoleUser ru = new RoleUser();
				GWTServiceHelper.setValuesInDomainObject(ruc, ru);
				RoleUser.insert(ru);
			}

			for (UserClient uc : userOfMailingList)

			{
				ArrayList<Role> rolelist1 = Role
						.lookupRolesBySystemIdAndUserId(sysID, uc.getUserId());

				for (Role r : rolelist1) {

					RoleUser ru = new RoleUser();
					ru.setSystemId(sysID);
					ru.setUserId(uc.getUserId());
					ru.setRoleId(r.getRoleId());
					if (!(r.getRoleName().equals("BAUsers")))
						RoleUser.delete(ru);

				}

				for (RoleUserClient ruc : rucList) {
					ruc.setUserId(uc.getUserId());
					RoleUser ru = new RoleUser();
					GWTServiceHelper.setValuesInDomainObject(ruc, ru);
					RoleUser.insert(ru);

				}
			}

			Mapper.refreshBOMapper();
			return true;

		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean updateRoleUsers(String sysPrefix, RoleClient role,
			List<UserClient> users) throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			List<RoleUser> currentRoleUsers = RoleUser
					.lookupBySystemIdAndRoleId(ba.getSystemId(), role
							.getRoleId());
			HashMap<Integer, RoleUser> roleUserMap = new HashMap<Integer, RoleUser>();
			if (currentRoleUsers != null) {
				for (RoleUser roleUser : currentRoleUsers) {
					roleUserMap.put(roleUser.getUserId(), roleUser);
				}
			}
			for (UserClient user : users) {
				if (roleUserMap.containsKey(user.getUserId())) {
					roleUserMap.remove(user.getUserId());
				} else {
					RoleUser roleUser = new RoleUser();
					roleUser.setIsActive(true);
					roleUser.setRoleId(role.getRoleId());
					roleUser.setSystemId(ba.getSystemId());
					roleUser.setUserId(user.getUserId());
					RoleUser.insert(roleUser);
				}
			}
			for (RoleUser roleUser : roleUserMap.values()) {
				RoleUser.delete(roleUser);
			}
			Mapper.refreshBOMapper();
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	/**
	 * Get the list of BA Mail Accounts configured for the specified ba NOTE:
	 * GWTServiceHelper will not work for converting the data types because the
	 * naming convention in 'BAMailAccount' class are inconsistent with the
	 * naming convention used for conversion of types in GWTServiceHelper
	 */
	public ArrayList<BAMailAccountClient> getBAMailAccount(String sysPrefix)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		ArrayList<BAMailAccount> mailAccounts = new ArrayList<BAMailAccount>();
		ArrayList<BAMailAccountClient> mailAccountClients = new ArrayList<BAMailAccountClient>();
		try {
			mailAccounts = BAMailAccount.lookupByBA(sysPrefix);
			for (BAMailAccount mailAccount : mailAccounts) {
				BAMailAccountClient mailAccountClient = new BAMailAccountClient();

				mailAccountClient.setBAMailAcId(mailAccount.getMyBAMailAcId());
				mailAccountClient.setEmailID(mailAccount.getMyEmailID());
				mailAccountClient.setPassward(mailAccount.getMyPassward());
				mailAccountClient.setMailServer(mailAccount.getMyMailServer());
				mailAccountClient.setBAPrefix(mailAccount.getMyBAPrefix());
				mailAccountClient.setProtocol(mailAccount.getMyProtocol());
				mailAccountClient.setPort(mailAccount.getPort());
				mailAccountClient.setIsActive(mailAccount.isActive());
				mailAccountClient.setCategoryId(mailAccount.getCategoryId());
				mailAccountClient.setBAEmailAddress(mailAccount
						.getBAEmailAddress());

				mailAccountClients.add(mailAccountClient);
			}
			return mailAccountClients;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	/**
	 * Update Mail Account settings in 'ba_mail_accounts' table NOTE:
	 * GWTServiceHelper will not work for converting the data types because the
	 * naming convention in 'BAMailAccount' class are inconsistent with the
	 * naming convention used for conversion of types in GWTServiceHelper
	 */
	public boolean updateMailAccounts(String sysPrefix,
			List<BAMailAccountClient> mailAccountClients)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		ArrayList<BAMailAccount> mailAccounts = new ArrayList<BAMailAccount>();
		for (BAMailAccountClient mailAccountClient : mailAccountClients) {
			BAMailAccount mailAccount = new BAMailAccount(mailAccountClient
					.getBAMailAcId(), mailAccountClient.getEmailID(),
					mailAccountClient.getPassward(), mailAccountClient
							.getMailServer(), mailAccountClient.getBAPrefix(),
					mailAccountClient.getProtocol(), mailAccountClient
							.getPort(), mailAccountClient.getIsActive(),
					mailAccountClient.getCategoryId(), mailAccountClient
							.getBAEmailAddress());
			mailAccounts.add(mailAccount);
		}
		try {
			return BAMailAccount.updateBAMailAccounts(sysPrefix, mailAccounts);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public ArrayList<ReportClient> getReports() throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		ArrayList<Report> reports = new ArrayList<Report>();
		ArrayList<ReportClient> reportClients = new ArrayList<ReportClient>();
		try {
			reports = Report.lookupByUserlogin(this.getRequest()
					.getRemoteUser());
			for (Report report : reports) {
				ReportClient reportClient = new ReportClient();
				GWTServiceHelper.setValuesInDomainObject(report, reportClient);
				reportClients.add(reportClient);
			}
			reports = Report.lookupPublicReports();
			for (Report report : reports) {
				ReportClient reportClient = new ReportClient();
				GWTServiceHelper.setValuesInDomainObject(report, reportClient);
				reportClients.add(reportClient);
			}
			return reportClients;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public HashMap<Integer, TypeUserClient> updateTypeUser(String sysPrefix,
			int fieldId, int typeId, List<TypeUserClient> tucList)
			throws TbitsExceptionClient {
		for (TypeUserClient tuc : tucList) {
			TypeUser tu = new TypeUser();
			GWTServiceHelper.setValuesInDomainObject(tuc, tu);
			TypeUser.delete(tu);
			if (tu.getUserTypeId() == 3 || tu.getNotificationId() != 1) {
				TypeUser.insert(tu);
			}
		}
		Mapper.refreshBOMapper();
		return getTypeUser(sysPrefix, fieldId, typeId);
	}

	public HashMap<String, String> getAllBACaptionsbySysId(int sysId)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		HashMap<String, String> captions = new HashMap<String, String>();
		HashMap<Integer, HashMap<String, String>> allCaptions = new HashMap<Integer, HashMap<String, String>>();
		allCaptions = GWTServiceHelper.getAllBACaptions();
		captions = allCaptions.get(sysId);
		return captions;
	}

	public boolean updateCaptions(HashMap<String, String> map, int sysId)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		HashMap<String, String> oldCaptions = getAllBACaptionsbySysId(0);
		System.out.println("Attempting to insert the captions into database");
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DataSourcePool.getConnection();
			ps = conn.prepareStatement("DELETE FROM "
					+ AdminCaptions.CAPTION_TABLE_NAME + " WHERE "
					+ AdminCaptions.CAPTION_COL_SYS_ID + " = " + sysId);
			if (null != ps) {
				ps.execute();
				ps.close();
			}

			for (String str : map.keySet()) {
				String value = map.get(str);
				System.out.println(str + ":" + value + ":"
						+ oldCaptions.get(str));

				if ((null == value) || (value.trim().equals("")))
					continue;
				if (oldCaptions.containsKey(str)) {
					if ((value.trim().equals(oldCaptions.get(str).trim())))
						continue;
				}
				ps = conn.prepareStatement("INSERT INTO "
						+ AdminCaptions.CAPTION_TABLE_NAME + "("
						+ AdminCaptions.CAPTION_COL_NAME + ","
						+ AdminCaptions.CAPTION_COL_VALUE + ","
						+ AdminCaptions.CAPTION_COL_SYS_ID + ") VALUES('" + str
						+ "','" + value + "'," + sysId + ")");
				if (null != ps) {
					ps.execute();
					ps.close();
				}
			}
			ps = null;
			CaptionsProps.reloadCaptions();
			return true;

		} catch (SQLException e) {
			System.out.println("Unable to load captions.");
			e.printStackTrace();
			throw new TbitsExceptionClient();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.out
						.println("----------------Exception while closing the connection----------");
				System.out.println(e);
			}
			conn = null;
		}
	}

	public boolean addCaptions(HashMap<String, String> map, int sysId)
			throws TbitsExceptionClient {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DataSourcePool.getConnection();

			ps = conn.prepareStatement("INSERT INTO "
					+ AdminCaptions.CAPTION_TABLE_NAME + "("
					+ AdminCaptions.CAPTION_COL_NAME + ","
					+ AdminCaptions.CAPTION_COL_VALUE + ","
					+ AdminCaptions.CAPTION_COL_SYS_ID + ") VALUES('"
					+ map.get("name") + "','" + map.get("value") + "'," + sysId
					+ ")");
			if (null != ps) {
				ps.execute();
				ps.close();
			}
			ps = null;

			CaptionsProps.reloadCaptions();
			return true;

		} catch (SQLException e) {
			System.out.println("Unable to load captions.");
			e.printStackTrace();
			throw new TbitsExceptionClient();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.out
						.println("----------------Exception while closing the connection----------");
				System.out.println(e);
			}
			conn = null;
		}
	}

	public boolean deletecaption(String captionname, String captionvalue,
			int sysId) throws TbitsExceptionClient {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DataSourcePool.getConnection();

			ps = conn
					.prepareStatement("DELETE FROM "
							+ AdminCaptions.CAPTION_TABLE_NAME + " WHERE "
							+ AdminCaptions.CAPTION_COL_SYS_ID + "=" + sysId
							+ " AND " + AdminCaptions.CAPTION_COL_NAME + "='"
							+ captionname + "'");// AND
			// " + AdminCaptions.CAPTION_COL_VALUE + "='"
			// + captionvalue + "'");
			if (null != ps) {
				ps.execute();
				ps.close();
			}
			ps = null;
			CaptionsProps.reloadCaptions();
			return true;

		} catch (SQLException e) {
			System.out.println("Unable to load captions.");
			e.printStackTrace();
			throw new TbitsExceptionClient();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.out
						.println("----------------Exception while closing the connection----------");
				System.out.println(e);
			}
			conn = null;
		}
	}

	public HashMap<String, String> getAppProperties()
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		HashMap<String, String> map = new HashMap<String, String>();
		Properties temp = new Properties();
		temp = PropertiesHandler.getAppProperties();
		for (Object o : temp.keySet()) {
			map.put((String) o, (String) temp.getProperty((String) o));
		}
		return map;
	}

	public HashMap<String, ArrayList<RoleClient>> getRoleTree()
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		HashMap<String, ArrayList<RoleClient>> roleTree = new HashMap<String, ArrayList<RoleClient>>();
		try {
			for (BusinessArea ba : BusinessArea.getActiveBusinessAreas()) {
				ArrayList<RoleClient> roleClients = new ArrayList<RoleClient>();
				for (Role r : Role.getRolesBySysId(ba.getSystemId())) {
					RoleClient roleClient = new RoleClient();
					GWTServiceHelper.setValuesInDomainObject(r, roleClient);
					roleClients.add(roleClient);
				}
				roleTree.put(ba.getSystemPrefix(), roleClients);
			}
			return roleTree;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean updateReportSpecificUsers(List<String> userLogins,
			int reportId, boolean includeOrExclude) throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		try {
			return ReportSpecificUsers.updateReportSpecificUser(userLogins,
					reportId, includeOrExclude);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean updateReportRoles(
			HashMap<String, ArrayList<RoleClient>> reportRoleClients,
			int reportId) throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		HashMap<String, ArrayList<Role>> reportRoles = new HashMap<String, ArrayList<Role>>();
		for (String sysPrefix : reportRoleClients.keySet()) {
			ArrayList<Role> roles = new ArrayList<Role>();
			for (RoleClient roleClient : reportRoleClients.get(sysPrefix)) {
				Role role = new Role();
				GWTServiceHelper.setValuesInDomainObject(roleClient, role);
				roles.add(role);
			}
			reportRoles.put(sysPrefix, roles);
		}
		try {
			return ReportRoles.updateReportRoles(reportRoles, reportId);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	/**
	 * Create and add a new report entry if it does not exist
	 */
	public int addReport(ReportClient reportClient) throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		Report report = new Report();
		GWTServiceHelper.setValuesInDomainObject(reportClient, report);
		try {
			return Report.insert(report);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	/**
	 * Update the values of existing report and return the updated report after
	 * making relevant entries in the database.
	 */
	public ReportClient updateReport(ReportClient reportClient)
			throws TbitsExceptionClient {

		ServiceHelper.validateUser(this.getRequest());
		Report report = new Report();
		GWTServiceHelper.setValuesInDomainObject(reportClient, report);
		try {
			report = Report.update(report);
			GWTServiceHelper.setValuesInDomainObject(report, reportClient);
			return reportClient;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public HashMap<Integer, ArrayList<Integer>> getReportRoles(int reportId)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		try {
			return ReportRoles.getReportRoles(reportId);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public HashMap<Integer, Boolean> getReportSpecificUsers(int reportId)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		try {
			return ReportSpecificUsers.getReportSpecificUsers(reportId);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public ArrayList<Integer> deleteReports(ArrayList<Integer> reportIds)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		ArrayList<Integer> retVals = new ArrayList<Integer>();
		for (Integer reportId : reportIds) {
			int i = ReportsTableModifier.handleDeleteReport(reportId);
			if (i == 0) {
				retVals.add(reportId);
			}
		}
		return retVals;
	}

	public RoleClient addRole(RoleClient roleClient)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		RoleClient updatedRoleClient = new RoleClient();
		Role role = new Role();
		GWTServiceHelper.setValuesInDomainObject(roleClient, role);
		Role updatedRole = new Role();
		try {
			updatedRole = Role.insert(roleClient.getSystemId(), roleClient
					.getRoleName(), roleClient.getDescription(), roleClient
					.getFieldId(), roleClient.getCanBeDeleted());
			GWTServiceHelper.setValuesInDomainObject(updatedRole,
					updatedRoleClient);
			return updatedRoleClient;
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public List<UserClient> addBAUsers(int sysId, String userStr) {
		String[] loginArr = userStr.split(",");
		List<User> users = new ArrayList<User>();
		for (String login : loginArr) {
			try {
				User user = User.lookupAllByUserLogin(login);
				if (user != null && !users.contains(user)) {
					users.add(user);
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		List<UserClient> userClients = new ArrayList<UserClient>();
		for (User user : users) {
			BAUser ob = new BAUser();
			ob.setSystemId(sysId);
			ob.setIsActive(true);
			ob.setUserId(user.getUserId());
			BAUser.insert(ob);
			try {
				UserClient userClient = GWTServiceHelper.fromUser(user);
				if (userClient != null)
					userClients.add(userClient);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}

		}
		Mapper.refreshBOMapper();
		return userClients;
	}

	public boolean deleteRole(RoleClient roleClient)
			throws TbitsExceptionClient {
		Role role = new Role();
		GWTServiceHelper.setValuesInDomainObject(roleClient, role);
		try {
			Role.delete(role);
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean deleteRoles(List<RoleClient> roles)
			throws TbitsExceptionClient {
		for (RoleClient role : roles) {
			if (!deleteRole(role))
				return false;
		}
		return true;
	}

	/**
	 * Get the permissions by sysid and role id
	 */
	public List<RolePermissionModel> getPermissionsbysysIdandRoleId(int sysId,
			int roleId) throws TbitsExceptionClient {
		try {
			Hashtable<String, RolePermission> permissions = RolePermission
					.getPermissionsBySystemIdAndRoleId(sysId, roleId);
			List<RolePermissionModel> rolePermissions = new ArrayList<RolePermissionModel>();

			for (String key : permissions.keySet()) {
				Field field = Field.lookupBySystemIdAndFieldName(sysId, key);
				RolePermissionModel model = new RolePermissionModel();
				model.setFieldName(field.getName());
				model.setDisplayName(field.getDisplayName());
				int perm = permissions.get(key).getPermission();

				if ((perm & PermissionClient.ADD) != 0)
					model.setAdd(true);
				else
					model.setAdd(false);

				if ((perm & PermissionClient.CHANGE) != 0)
					model.setUpdate(true);
				else
					model.setUpdate(false);

				if ((perm & PermissionClient.VIEW) != 0)
					model.setView(true);
				else
					model.setView(false);

				if ((perm & PermissionClient.EMAIL_VIEW) != 0)
					model.setEMail(true);
				else
					model.setEMail(false);

				rolePermissions.add(model);
			}
			return rolePermissions;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	/**
	 * Get the Field Controls for all the fields in the specified BA
	 * 
	 * @param sysId
	 * @return List of field controls
	 * @throws TbitsExceptionClient
	 */
	public List<RolePermissionModel> getFieldControls(int sysId)
			throws TbitsExceptionClient {
		List<RolePermissionModel> rolePermissions = new ArrayList<RolePermissionModel>();
		try {
			List<Field> fieldList = new ArrayList<Field>(Field
					.lookupBySystemId(sysId));

			for (Field field : fieldList) {
				RolePermissionModel model = new RolePermissionModel();
				model.setFieldName(field.getName());
				model.setDisplayName(field.getDisplayName());

				for (String controlName : PermissionClient.FIELD_CONTROLS) {
					int a = PermissionClient.PERMISSIONMAP.get(controlName);
					model.set(controlName, (field.getPermission() & a) != 0);
				}

				rolePermissions.add(model);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		return rolePermissions;
	}

	/**
	 * Update the permissons of the fields from Field Controls Page
	 * 
	 * @param sysId
	 *            - BA whose field controls are to be changed
	 * @param fieldControls
	 *            - List of fields with field control permissions
	 * @return - Updated list of Fields
	 * @throws TbitsExceptionClient
	 */
	public List<RolePermissionModel> updateFieldControls(int sysId,
			List<RolePermissionModel> fieldControls)
			throws TbitsExceptionClient {
		List<RolePermissionModel> fieldControlList = new ArrayList<RolePermissionModel>();

		try {
			for (RolePermissionModel entry : fieldControls) {
				Field field = Field.lookupBySystemIdAndFieldName(sysId, entry
						.getFieldName());

				field.setPermission(0);
				for (String controlName : PermissionClient.FIELD_CONTROLS) {
					if (entry.get(controlName) != null
							&& (Boolean) entry.get(controlName)) {
						int a = PermissionClient.PERMISSIONMAP.get(controlName);
						field.setPermission(field.getPermission() | a);
					}
				}
				Field.update(field);
				fieldControlList.add(entry);
			}
			Mapper.refreshBOMapper();
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		return fieldControlList;
	}

	public UserClient insertUser(UserClient uc) throws TbitsExceptionClient {
		try {
			// temporary usr
			User usr = new User();
			GWTServiceHelper.setValuesInDomainObject(uc, usr);

			boolean doesUserExist = User.doesUserAlreadyExist(usr
					.getUserLogin());

			if (doesUserExist) {
				return null;
			}

			try {
				AD2TBitsSync.insertUser(usr);
				if(uc.get("password")!=null && !((String)uc.get("password")).trim().equals(""))
				{
					this.setPassword(uc.getUserLogin(),((String) uc.get("password")).trim());
				}
			} catch (Exception exp) {
				exp.printStackTrace();
				return null;
			}
			Mapper.refreshUserMapper();

			UserClient usrclient = new UserClient();

			usr = User.lookupAllByUserLogin(usr.getUserLogin());
			GWTServiceHelper.setValuesInDomainObject(usr, usrclient);
			
		
			
			return usrclient;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient();
		}
	}

	public boolean setPassword(String userlogin, String password)
			throws TbitsExceptionClient {
		try {
			AuthUtils.setPassword(userlogin, password);
			Mapper.refreshUserMapper();
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (EventFailureException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean updateAppProperties(HashMap<String, String> map)
			throws TbitsExceptionClient {
		HashMap<String, String> oldProperties = new HashMap<String, String>();
		oldProperties = getAppProperties();
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DataSourcePool.getConnection();
			for (String str : oldProperties.keySet()) {
				String oldValue = oldProperties.get(str);
				String value = map.get(str);
				if ((null == value) || (value.trim().equals(""))
						|| (value.trim().equals(oldValue)))
					continue;

				ps = conn.prepareStatement("UPDATE "
						+ AdminAppProperties.APP_TABLE_NAME + " SET "
						+ AdminAppProperties.APP_COL_VALUE + " = '" + value
						+ "' WHERE " + AdminAppProperties.APP_COL_NAME + " = '"
						+ str + "'");
				if (null != ps) {
					ps.execute();
					ps.close();
				}
				ps = null;
			}
			PropertiesHandler.reload();
			return true;
		} catch (SQLException sqle) {
			System.out.println("Error while updating tbits.");
			throw new TbitsExceptionClient();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.out
						.println("----------------Exception while closing the connection----------");
				System.out.println(e);
			}
			conn = null;
		}

	}

	public UserClient updateUser(UserClient uClient)
			throws TbitsExceptionClient {
		UserClient upClient = new UserClient();
		try {
			User usr = null;
			usr = User.lookupByUserLogin(uClient.getUserLogin());
			GWTServiceHelper.setValuesInDomainObject(uClient, usr);
			usr = User.update(usr);
			GWTServiceHelper.setValuesInDomainObject(usr, upClient);
			return upClient;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient();
		}
	}

	public boolean insertEscalationCondition(int sysId, TbitsModelData tb)
			throws TbitsExceptionClient {
		try {
			int severity = (Integer) tb.get("severity_id");
			int span = (Integer) tb.get("span");
			int category = (Integer) tb.get("category_id");
			int status = (Integer) tb.get("status_id");
			int type = (Integer) tb.get("type_id");
			EscalationCondition ec = new EscalationCondition(sysId, severity,
					span, category, status, type);
			EscalationCondition.insert(ec);

			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public ArrayList<TbitsModelData> getEscalationCondition(int sysId)
			throws TbitsExceptionClient {
		BusinessArea ba;
		try {
			ba = BusinessArea.lookupBySystemId(sysId);
			if (ba == null)
				return null;

			ArrayList<TbitsModelData> arr = new ArrayList<TbitsModelData>();
			ArrayList<EscalationCondition> ec = EscalationCondition
					.lookupEscConditionBySysId(sysId);
			for (EscalationCondition e : ec) {
				TbitsModelData tb = new TbitsModelData();
				tb.set("severity_id", e.getSeverityId());
				tb.set("category_id", e.getCategoryId());
				tb.set("type_id", e.getTypeId());
				tb.set("status_id", e.getStatusId());
				tb.set("span", e.getSpan());
				arr.add(tb);
			}
			return arr;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public HashMap<Integer, ArrayList<Integer>> getAllParentChildMapping(
			int sysID) throws TbitsExceptionClient {
		BusinessArea ba;
		try {
			ba = BusinessArea.lookupBySystemId(sysID);
			if (ba == null)
				return null;
			HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
			map = transbit.tbits.Escalation.EscalationUtils.getAllParentChildUsers(sysID);
			if (map != null)
				return map;
			return null;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean deleteMailingLists(List<MailingListUserClient> mailingLists)
			throws TbitsExceptionClient {
		try {
			if (mailingLists != null) {
				for (MailingListUserClient mailingList : mailingLists) {
					MailingListHandler.delete(mailingList.getMailListUser()
							.getUserLogin());
				}
			}
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw (new TbitsExceptionClient());
		}
	}

	public boolean updateMailingList(String mailListName,
			List<UserClient> mailListMembers) throws TbitsExceptionClient {
		if (mailListName != null) {
			try {
				User mlUser = User.lookupAllByUserLogin(mailListName);
				if (mlUser != null) {
					MailingListHandler.delete(mailListName);
					for (UserClient user : mailListMembers)
						MailingListHandler.insert(mlUser.getUserId(), user
								.getUserId());

					Mapper.refreshUserMapper();
					return true;
				} else {
					UserClient uc = new UserClient();
					uc.setUserLogin(mailListName);
					uc.setEmail("");
					uc.setUserTypeId(UserType.INTERNAL_MAILINGLIST);
					uc.setIsActive(true);
					uc = insertUser(uc);
					mlUser = User.lookupAllByUserLogin(mailListName);
					for (UserClient user : mailListMembers)
						MailingListHandler.insert(mlUser.getUserId(), user
								.getUserId());

					Mapper.refreshUserMapper();
					return true;
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw (new TbitsExceptionClient());
			}
		}
		return false;
	}

	public boolean deleteEscalationCondition(int sysId, TbitsModelData tb)
			throws TbitsExceptionClient {
		try {
			int severity = (Integer) tb.get("severity_id");
			int span = (Integer) tb.get("span");
			int category = (Integer) tb.get("category_id");
			int status = (Integer) tb.get("status_id");
			int type = (Integer) tb.get("type_id");
			EscalationCondition ec = new EscalationCondition(sysId, severity,
					span, category, status, type);
			EscalationCondition.delete(ec);
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public List<UserClient> getMailingListByUserId(int userId)
			throws TbitsExceptionClient {
		List<User> users = Mapper.ourMailListUserMap.get(userId);

		if (users != null) {
			ArrayList<UserClient> userclients = new ArrayList<UserClient>();
			for (User u : users) {
				UserClient uc = new UserClient();
				GWTServiceHelper.setValuesInDomainObject(u, uc);
				userclients.add(uc);
			}
			return userclients;
		}

		return null;
	}

	public List<MailingListUserClient> getAllMailingLists()
			throws TbitsExceptionClient {
		try {
			List<MailingListUserClient> mlist = new ArrayList<MailingListUserClient>();

			for (Integer i : Mapper.ourMailListUserMap.keySet()) {
				List<UserClient> userclients = getMailingListByUserId(i);

				User mailListUser = User.lookupByUserId(i);
				if (userclients != null && mailListUser != null) {
					UserClient mailListUserClient = new UserClient();
					GWTServiceHelper.setValuesInDomainObject(mailListUser,
							mailListUserClient);

					MailingListUserClient mailingList = new MailingListUserClient();
					mailingList.setMailListUser(mailListUserClient);
					mailingList.setMailListMembers(userclients);
					mlist.add(mailingList);
				}
			}
			return mlist;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public ArrayList<UserClient> getUsersByRoleId(int sysId, int roleId)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		try {
			ArrayList<RoleUser> roleUsers = RoleUser.lookupBySystemIdAndRoleId(
					sysId, roleId);
			if (roleUsers != null) {
				ArrayList<UserClient> users = new ArrayList<UserClient>();
				for (RoleUser roleUser : roleUsers) {
					User u = User.lookupByUserId(roleUser.getUserId());
					UserClient uClient = GWTServiceHelper.fromUser(u);
					users.add(uClient);
				}
				return users;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		return null;
	}

	public String[] getReportFileNames() throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		try {
			String ourReportsLocation = Configuration.getAppHome()
					+ "/tbitsreports";
			File dir = new File(ourReportsLocation);
			String reportFiles[] = dir.list();
			return reportFiles;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public BusinessAreaClient createNewBA(String sysPrefix, String BAName)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		if (sysPrefix == null || sysPrefix.trim().equals("") || BAName == null
				|| BAName.trim().equals(""))
			return null;
		try {
			BusinessArea ba1 = BusinessArea.lookupBySystemPrefix(BAName);
			BusinessArea ba2 = BusinessArea.lookupBySystemPrefix(sysPrefix);

			if ((ba1 == null) && (ba2 == null)) {
				BusinessArea.createBusinessArea(BAName, sysPrefix);
				Mapper.refreshBOMapper();
				return getBAClient(sysPrefix);
			} else if (ba1 != null) {
				throw new TbitsExceptionClient(
						"Business area already exists with same name");
			} else if (ba2 != null) {
				throw new TbitsExceptionClient(
						"Business area already exists with same system prefix");
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient("Database exception", e);
		}
		return null;
	}

	public boolean updateDefaultCaptions(HashMap<String, String> map)
			throws TbitsExceptionClient {
		ServiceHelper.validateUser(this.getRequest());
		System.out.println("Attempting to insert the captions into database");
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DataSourcePool.getConnection();
			ps = conn.prepareStatement("DELETE FROM "
					+ AdminCaptions.CAPTION_TABLE_NAME + " WHERE "
					+ AdminCaptions.CAPTION_COL_SYS_ID + " = " + 0);
			if (null != ps) {
				ps.execute();
				ps.close();
			}

			for (String str : map.keySet()) {
				ps = conn.prepareStatement("INSERT INTO "
						+ AdminCaptions.CAPTION_TABLE_NAME + "("
						+ AdminCaptions.CAPTION_COL_NAME + ","
						+ AdminCaptions.CAPTION_COL_VALUE + ","
						+ AdminCaptions.CAPTION_COL_SYS_ID + ") VALUES('" + str
						+ "','" + map.get(str) + "'," + 0 + ")");
				if (null != ps) {
					ps.execute();
					ps.close();
				}
			}
			ps = null;
			CaptionsProps.reloadCaptions();
			return true;

		} catch (SQLException e) {
			System.out.println("Unable to load captions.");
			e.printStackTrace();
			throw new TbitsExceptionClient();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.out
						.println("----------------Exception while closing the connection----------");
				System.out.println(e);
			}
			conn = null;
		}
	}

	/**
	 * Update the role permissions
	 */
	public boolean updateRolePermissions(int sysId, int roleId,
			List<RolePermissionModel> rolePermissions)
			throws TbitsExceptionClient {
		try {
			for (RolePermissionModel model : rolePermissions) {
				RolePermission rolePermission = new RolePermission();
				String fName = model.getFieldName();
				Field field = Field.lookupBySystemIdAndFieldName(sysId, fName);
				int perm = (model.isAdd() ? PermissionClient.ADD : 0)
						+ (model.isEMail() ? PermissionClient.EMAIL_VIEW : 0)
						+ (model.isUpdate() ? PermissionClient.CHANGE : 0)
						+ (model.isView() ? PermissionClient.VIEW : 0);
				rolePermission.setPermission(perm);

				rolePermission.setSystemId(sysId);
				rolePermission.setFieldId(field.getFieldId());
				rolePermission.setRoleId(roleId);
				RolePermission.update(rolePermission);

			}
			Mapper.refreshBOMapper();
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient();
		}
	}

	public boolean updateRolePermissions(int sysId, List<Integer> roleIds,
			List<RolePermissionModel> rolePermissions)
			throws TbitsExceptionClient {
		for (int roleId : roleIds) {
			updateRolePermissions(sysId, roleId, rolePermissions);
		}
		return true;
	}

	public boolean insertAppProperties(String name, String value)
			throws TbitsExceptionClient {
		try {
			return PropertiesHandler.insertAppProperties(name, value);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient();
		}
	}

	public boolean deleteAppProperties(String name) throws TbitsExceptionClient {
		try {
			return (PropertiesHandler.deleteAppProperties(name));
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient();
		}
	}

	public ArrayList<UserClient> getAllUsers() {
		ArrayList<UserClient> allUsers = new ArrayList<UserClient>();
		List<User> users = User.getAllUsers();
		for (User user : users) {
			UserClient userClient = new UserClient();
			GWTServiceHelper.setValuesInDomainObject(user, userClient);
			if (user.getWebConfigObject() != null)
				userClient.setWebDateFormat(user.getWebConfigObject()
						.getWebDateFormat());

			allUsers.add(userClient);
		}
		return allUsers;
	}

	public UsersPage getAllUsersPage(int page, int pageSize) {
		ArrayList<UserClient> allUsers = getAllUsers();
		if (allUsers != null) {
			List<UserClient> users = new ArrayList<UserClient>();
			for (int i = Math.max(0, (page - 1) * pageSize); i < Math.min(
					allUsers.size(), (page * pageSize)); i++) {
				users.add(allUsers.get(i));
			}
			return new UsersPage(users, allUsers.size());
		}
		return null;
	}

	public UsersPage fetchQueriedUsers(String filter, String value) {

		// Format value
		int searchFrom = 0;
		int specialIndex = 0;
		while ((specialIndex = value.indexOf("'", searchFrom)) >= 0) {
			value = value.substring(0, specialIndex) + "'"
					+ value.substring(specialIndex);
			searchFrom = specialIndex + 2;
		}
		searchFrom = 0;
		specialIndex = 0;
		ArrayList<String> specials = new ArrayList<String>();
		specials.add("%");
		specials.add("_");
		specials.add("[");
		for (String special : specials) {
			while ((specialIndex = value.indexOf(special, searchFrom)) >= 0) {
				value = value.substring(0, specialIndex) + "\\"
						+ value.substring(specialIndex);
				searchFrom = specialIndex + 1 + special.length();
			}
		}

		Connection conn = null;
		ArrayList<UserClient> users = new ArrayList<UserClient>();
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			String query = "select user_id from users where " + filter
					+ " like '%" + value + "%' {escape '\\'}";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				User user = User.lookupAllByUserId(rs.getInt("user_id"));
				UserClient userClient = new UserClient();
				GWTServiceHelper.setValuesInDomainObject(user, userClient);
				if (user.getWebConfigObject() != null)
					userClient.setWebDateFormat(user.getWebConfigObject()
							.getWebDateFormat());

				users.add(userClient);
			}

			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return new UsersPage(users, users.size());
	}

	public Boolean testMailSetting(String server, String port, String login,
			String password, String protocol) throws TbitsExceptionClient {
		try {
			connectToServer(server, port, login, password, protocol);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		return true;
	}

	private void connectToServer(String emailServer, String port, String login,
			String password, String protocol) throws TBitsException {

		if ((emailServer.equals("")) || (emailServer == null))
			throw new TBitsException("Please Fill in the Name of Email Server");

		if ((login == null) || (login.equals("")))
			throw new TBitsException("Fill in the Username");

		if ((password == null) || (password.equals("")))
			throw new TBitsException("Fill in the Password");

		Properties prop = PropertiesHandler.getAppAndSysProperties();

		prop.setProperty("mail." + protocol + ".host", emailServer);
		prop.setProperty("mail." + protocol + ".port", port);

		MailAuthenticator passwordAuthentication = new MailAuthenticator(login,
				password);

		try {
			Session session = Session.getInstance(prop, passwordAuthentication);
			Store store = session.getStore(protocol);
			store.connect();
		}

		catch (AuthenticationFailedException a) {
			throw new TBitsException("Authentication Failed");
		} catch (MessagingException m) {
			throw new TBitsException("Could not connect to Mail Server");
		} catch (IllegalStateException i) {
			throw new TBitsException("Already Connected to Server");
		}

	}

	public ArrayList<JobDetailClient> getJobDetails()
			throws TbitsExceptionClient {
		ArrayList<JobDetailClient> response = new ArrayList<JobDetailClient>();
		try {

			Scheduler myScheduler = TBitsScheduler.getScheduler();

			ArrayList<JobDetail> jdList = new ArrayList<JobDetail>();
			String[] jobGroups = myScheduler.getJobGroupNames();
			if (jobGroups != null) {
				for (int i = 0; i < jobGroups.length; i++) {
					String[] jobNames = myScheduler.getJobNames(jobGroups[i]);
					if (jobNames != null) {
						for (int j = 0; j < jobNames.length; j++) {
							JobDetail tempJob = myScheduler.getJobDetail(
									jobNames[j], jobGroups[i]);
							if (tempJob != null)
								jdList.add(tempJob);
						}
					}
				}
			}

			// ArrayList<JobDetail> jdList = JobSchedulingUtil.getJobs();
			for (JobDetail jd : jdList) {
				JobDetailClient jdc = new JobDetailClient();
				jdc.setJobName(jd.getName());
				jdc.setDescription(jd.getDescription());
				jdc.setJobGroup(jd.getGroup());
				jdc.setJobClassName(jd.getJobClass().getName());
				jdc.setDurability(jd.isDurable());
				jdc.setVolatility(jd.isVolatile());
				jdc.setRequestRecovery(jd.requestsRecovery());

				ArrayList<JobParameterClient> jpcList = new ArrayList<JobParameterClient>();
				for (Object jpName : jd.getJobDataMap().keySet()) {
					JobParameterClient jpc = new JobParameterClient();
					jpc.setName((String) jpName);
					jpc.setValues(jd.getJobDataMap().get(jpName));
					jpcList.add(jpc);
				}
				jdc.setJobParameters(jpcList);
				jdc.setCronExpression(JobSchedulingUtil.getCronExpression(jd
						.getName(), jd.getGroup()));
				jdc.setStartDate(JobSchedulingUtil.getJobDates(jd.getName(), jd
						.getGroup())[0]);
				jdc.setEndDate(JobSchedulingUtil.getJobDates(jd.getName(), jd
						.getGroup())[1]);
				HashMap<Integer, String> triggerState = new HashMap<Integer, String>();
				triggerState.put(Trigger.STATE_BLOCKED, "Blocked");
				triggerState.put(Trigger.STATE_COMPLETE, "Complete");
				triggerState.put(Trigger.STATE_ERROR, "Error");
				triggerState.put(Trigger.STATE_NONE, "none");
				triggerState.put(Trigger.STATE_NORMAL, "Normal");
				triggerState.put(Trigger.STATE_PAUSED, "Paused");
				jdc.setJobState(triggerState.get(JobSchedulingUtil.myScheduler
						.getTriggerState(jd.getName(), jd.getGroup())));
				// if(
				// JobSchedulingUtil.myScheduler.getTriggerState(jd.getName(),
				// jd.getGroup()) == Trigger.STATE_PAUSED)
				// jdc.setJobState("Pause");
				// else
				// jdc.setJobState("Running");

				response.add(jdc);
			}
			return response;
			// return new BasePagingLoadResult<JobDetailClient>
			// (response,config.getOffset(), config.getLimit());
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tbitsGlobal.admin.client.JobActionService#pauseJob(java.lang.String,
	 * java.lang.String)
	 */

	public boolean pauseJob(String jobName, String jobGroup)
			throws TbitsExceptionClient {
		try {

			JobSchedulingUtil.myScheduler.pauseJob(jobName, jobGroup);
			return true;
		} catch (SchedulerException e) {
			throw new TbitsExceptionClient(
					"Unable to pause this job with details " + jobName + ":"
							+ jobGroup);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tbitsGlobal.admin.client.JobActionService#resumeJob(java.lang.String,
	 * java.lang.String)
	 */
	public boolean resumeJob(String jobName, String jobGroup)
			throws TbitsExceptionClient {
		try {

			JobSchedulingUtil.myScheduler.resumeJob(jobName, jobGroup);
			return true;
		} catch (SchedulerException e) {
			throw new TbitsExceptionClient(
					"Unable to resume this job with details " + jobName + ":"
							+ jobGroup);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tbitsGlobal.admin.client.JobActionService#getJobParams(java.lang.
	 * String, java.lang.String)
	 */
	public ArrayList<JobParameterClient> getJobParams(String jobClassStr,
			String jobParamsStr) {
		ArrayList<JobParameter> jpList = JobSchedulingUtil
				.getJobParams(jobClassStr);

		ArrayList<JobParameterClient> response = new ArrayList<JobParameterClient>();
		for (JobParameter jp : jpList) {
			JobParameterClient jpc = new JobParameterClient();
			GWTServiceHelper.setValuesInDomainObject(jp, jpc);
			jpc.setType(jp.getType().name());
			jpc.setValues(jp.getValues());
			response.add(jpc);
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbitsGlobal.admin.client.JobActionService#getJobClasses()
	 */
	public ArrayList<JobClassClient> getJobClasses() {
		ArrayList<JobClassClient> response = new ArrayList<JobClassClient>();
		for (JobClass jc : JobSchedulingUtil.getJobClasses()) {
			JobClassClient jcc = new JobClassClient();
			GWTServiceHelper.setValuesInDomainObject(jc, jcc);
			response.add(jcc);
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tbitsGlobal.admin.client.JobActionService#deleteJob(java.lang.String,
	 * java.lang.String)
	 */

	public boolean saveJob(String mode, JobDetailClient jobDetail,
			ArrayList<JobParameterClient> jpcList, String preJobName,
			String preJobGroup) throws TbitsExceptionClient {

		if (mode == JobActionService.EDIT_JOB) {
			if (preJobName == null || preJobName == "" || preJobGroup == null
					|| preJobGroup == "")
				throw new TbitsExceptionClient(
						"Unable to identify Job to be edited");
		}
		Class jobClass = null;
		String jobName = jobDetail.getJobName();
		String jobGroup = jobDetail.getJobGroup();
		String jobClassName = jobDetail.getJobClassName();
		String jobDescription = jobDetail.getDescripton();
		// String sysPrefix = aRequest.getPathInfo();
		// if(null == sysPrefix) sysPrefix = "";

		if ((null == jobName) || (jobName.trim().length() == 0))
			throw new TbitsExceptionClient("Job Name is Required ");

		if ((null == jobGroup) || (jobGroup.trim().length() == 0))
			throw new TbitsExceptionClient("Job Group is Required ");

		try {
			jobClass = Class.forName(jobClassName);
		} catch (ClassNotFoundException cnfe) {
			// if not found search in plugins
			jobClass = PluginManager.getInstance().findPluginsByClassName(jobClassName);
			
			if( null == jobClass )
				throw new TbitsExceptionClient(jobClassName + " :Class Not Found ",
					cnfe);
		}

		try {
			if (mode == JobActionService.CREATE_JOB
					&& (null != JobSchedulingUtil.myScheduler.getJobDetail(
							jobName, jobGroup))) {
				throw new TbitsExceptionClient(
						"Job with given name and group Already exists: "
								+ jobName + ":" + jobGroup);
			}

		} catch (SchedulerException e1) {
			e1.printStackTrace();
		}

		JobDataMap jdm = new JobDataMap();
		boolean isRecoverable = false;
		boolean isDurable = false;
		boolean isVolatile = false;
		isRecoverable = new Boolean(jobDetail.requestRecovery());
		isDurable = new Boolean(jobDetail.isDurable());
		isVolatile = new Boolean(jobDetail.isVolatile());
		Hashtable<String, String> params = new Hashtable<String, String>();

		// for(JobParameterClient jpc : jobDetail.getJobParameters()){
		for (JobParameterClient jpc : jpcList) {
			if (jpc.getValues() == null)
				continue;
			if (jpc.getName().trim().length() > 0
					&& ((String) jpc.getValues()).trim().length() > 0) {

				jdm
						.put(jpc.getName().trim(), ((String) jpc.getValues())
								.trim());
				params.put(jpc.getName().trim(), ((String) jpc.getValues())
						.trim()); // GET THE PARAMETES FOR VALIDATION
			}
		}

		ITBitsJob itbitsjob;
		try {
			itbitsjob = (ITBitsJob) jobClass.newInstance();
			if (itbitsjob.validateParams(params)) {
				System.out.println("All the parameters of JOB are correct.");
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e.getMessage(), e);
		} catch (InstantiationException e1) {
			e1.printStackTrace();
			throw new TbitsExceptionClient(
					"Error : Cannot Create an instance of " + jobClass
							+ " type.", e1);

		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
			throw new TbitsExceptionClient(
					"Error : Illegal Access Exception while accessing "
							+ jobClass, e1);
		}

		JobDetail mainJob = new JobDetail();
		mainJob.setName(jobName);
		mainJob.setGroup(jobGroup);
		mainJob.setJobClass(jobClass);
		mainJob.setDescription(jobDescription);
		mainJob.setRequestsRecovery(isRecoverable);
		mainJob.setDurability(isDurable);
		mainJob.setVolatility(isVolatile);
		mainJob.setJobDataMap(jdm);
		try {
			if (mode.trim().equalsIgnoreCase(JobActionService.EDIT_JOB.trim())) {
				JobSchedulingUtil.myScheduler
						.deleteJob(preJobName, preJobGroup);
			}
			JobSchedulingUtil.myScheduler.addJob(mainJob, true);
		} catch (SchedulerException e) {
			throw new TbitsExceptionClient("Job Could not be added", e);
		}

		scheduleJob(jobDetail);
		return true;
	}

	private void scheduleJob(JobDetailClient jobDetail)
			throws TbitsExceptionClient {
		String jobName = jobDetail.getJobName();
		String jobGroup = jobDetail.getJobGroup();
		String triggerName = jobName;
		String triggerGroup = jobGroup;
		Date endTime = jobDetail.getEndDate();
		Date startTime = jobDetail.getStartDate();
		String cronExpression = jobDetail.getCronExpression();
		System.out.println(cronExpression);
		if ((triggerName.length() == 0) || (triggerGroup.length() == 0))
			throw new TbitsExceptionClient("TriggerName Cannot be null");

		CronTrigger trigger = null;
		try {
			trigger = new CronTrigger(triggerName, triggerGroup, jobName,
					jobGroup, cronExpression);
			if (startTime != null)
				trigger.setStartTime(startTime);
			if (endTime != null)
				trigger.setEndTime(endTime);
			JobSchedulingUtil.myScheduler.unscheduleJob(triggerName,
					triggerGroup); // remove the trigger if already present
			JobSchedulingUtil.myScheduler.scheduleJob(trigger);
		} catch (ParseException e) {
			throw new TbitsExceptionClient(
					"\nError in parsing Cron Expression for " + triggerName
							+ ". Please edit this later.\n");
		} catch (SchedulerException e) {

		}

	}

	public boolean deleteJob(String jobName, String jobGroup)
			throws TbitsExceptionClient {
		try {
			JobSchedulingUtil.deleteJob(jobName, jobGroup);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient("Unable to delete job"
					+ e.getMessage());
		}
		return true;
	}

	public boolean executeJob(String jobName, String jobGroup)
			throws TbitsExceptionClient {
		try {
			JobSchedulingUtil.executeJob(jobName, jobGroup);
			return true;
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e.getMessage());
		}
	}

	public ArrayList<Date> getNextExecutions(String cron, Date start, Date end,
			int numExecutions) throws TbitsExceptionClient {
		if (!CronExpression.isValidExpression(cron)) {
			throw new TbitsExceptionClient("invalid cron expression");
		}
		CronExpression cronExpression;
		try {
			cronExpression = new CronExpression(cron);
		} catch (ParseException e) {
			throw new TbitsExceptionClient(e.getMessage());
		}

		ArrayList<Date> execs = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance();
		Date time = cal.getTime();
		int n = 0;
		while ((n < numExecutions))// && (nextDate.before(finalFireTime)||
		// nextDate.equals(finalFireTime)))
		{
			time = cronExpression.getNextValidTimeAfter(time);

			if (time != null) {
				if (start != null)
					if (time.compareTo(start) < 0)
						continue;
				if (end != null)
					if (time.compareTo(end) > 0)
						return execs;
				execs.add(time);
			} else
				return execs;
			n++;
		}
		return execs;
	}

	public ArrayList<SysInfoClient> getSysInfo() {
		ArrayList<SysInfoClient> sysProperties = new ArrayList<SysInfoClient>();
		GatherSysInfo sysinfo = new GatherSysInfo();
		sysProperties.addAll(sysinfo.getSysInfoList());
		return sysProperties;
	}

	public boolean updateUsers(List<UserClient> users)
			throws TbitsExceptionClient {
		if (users != null) {
			for (UserClient userClient : users) {
				User usr = new User();
				GWTServiceHelper.setValuesInDomainObject(userClient, usr);

				try {
					boolean doesUserExist = User.doesUserAlreadyExist(usr
							.getUserLogin());

					if (doesUserExist) {
						User.update(usr);
					} else {
						AD2TBitsSync.insertUser(usr);
					}
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(e);
				}
			}
			Mapper.refreshUserMapper();
		}
		return true;
	}

	public boolean updateRoles(List<RoleClient> roles)
			throws TbitsExceptionClient {
		for (RoleClient roleClient : roles) {
			Role role = new Role();
			GWTServiceHelper.setValuesInDomainObject(roleClient, role);
			if (role.getRoleId() != 0) {
				try {
					Role.update(role);
				} catch (TBitsException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(e);
				}
			} else {
				try {
					Role.insert(role);
				} catch (TBitsException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(e);
				}
			}
		}
		Mapper.refreshBOMapper();
		return true;
	}

	// =====================================================================================
	// vv Rules vv

	/**
	 * Get all the existing rules from the database. The rules can be deployed
	 * or undeployed.
	 * 
	 * @return ArrayList of RulesClient
	 */

	public ArrayList<RulesClient> getExistingRules() {

		Connection conn = null;
		ArrayList<RulesClient> rules = new ArrayList<RulesClient>();
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			String query = "select * from rules_definitions";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				RulesClient rc = new RulesClient();
				rc.setName(rs.getString("name"));
				rc.setType(rs.getString("type"));
				rc.setSeq(rs.getFloat("seq_number"));
				rc.setId(rs.getInt("id"));
				rules.add(rc);
			}

			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return rules;
	}

	// ================================================================================

	/**
	 * Get all the templates for the rules. The templates are the interfaces
	 * that can be implemented by the java rules.
	 */
	public ArrayList<RuleDef> getRuleTemplates() {

		return RulesTemplateRegistry.getInstance().getTemplates();
	}

	// ================================================================================

	/**
	 * Compile the rule and return the compilation result.
	 */
	public String compileRule(RuleDef ruleDef) {

		if (ruleDef == null)
			return null;
		ClassWriter cw = new ClassWriter();
		cw.constructJavaFile(ruleDef);
		try {
			cw.constructClassFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cw.sanitise();

		return cw.getCompilerOutput();
	}

	// ================================================================================

	/**
	 * Save the code of the rule in the database.
	 * 
	 * @throws Exception
	 */
	public boolean saveRule(RuleDef ruleDef) throws TbitsExceptionClient {

		if (ruleDef == null)
			return false;
		ClassWriter cw = new ClassWriter();
		cw.constructJavaFile(ruleDef);
		ruleDef.setRuleCode(cw.getJavaCode());
		cw.sanitise();

		return saveRuleInDb(ruleDef);
	}

	// ================================================================================

	/**
	 * Save the rule code in the database. Does not change the class file of the
	 * rule if it already exists.
	 * 
	 * @param ruleDef
	 * @throws Exception
	 */
	public boolean saveRuleInDb(RuleDef ruleDef) throws TbitsExceptionClient {

		Connection conn = null;

		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			int id = getNextRuleId(conn);
			RulesClient rc = getRuleDetails(conn, ruleDef.getName());
			if (rc != null) {
				id = rc.id;
				String query = "update rules_storage set code = ? where id = ? and name = ?";
				PreparedStatement stmt = conn.prepareStatement(query);
				stmt.setString(1, ruleDef.getRuleCode());
				stmt.setInt(2, id);
				stmt.setString(3, ruleDef.getName());
				stmt.executeUpdate();
				stmt.close();
			} else {
				String query = "insert into rules_storage (id, name, code) values (?, ?, ?)";
				PreparedStatement stmt = conn.prepareStatement(query);
				stmt.setInt(1, id);
				stmt.setString(2, ruleDef.getName());
				stmt.setString(3, ruleDef.getRuleCode());
				stmt.executeUpdate();
				stmt.close();

				query = "insert into rules_definitions values (?, ?, ?, ?)";
				stmt = conn.prepareStatement(query);
				stmt.setInt(1, id);
				stmt.setString(2, ruleDef.getName());
				stmt.setString(3, ruleDef.getType());
				stmt.setDouble(4, ruleDef.getSeqNo());
				stmt.executeUpdate();
				stmt.close();
			}

			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new TbitsExceptionClient("Unable to save rule", e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return true;
	}

	// ================================================================================

	/**
	 * Deploy the rule after checking its compilation result.
	 */
	public boolean deployRule(RuleDef ruleDef) {

		if (ruleDef == null)
			return false;
		ClassWriter cw = new ClassWriter();
		cw.constructJavaFile(ruleDef);
		ruleDef.setRuleCode(cw.getJavaCode());
		byte[] classbytes;
		try {
			if (!cw.constructClassFile()) {
				cw.sanitise();
				return false;
			}
			classbytes = cw.getClassBytes();
			if (classbytes == null)
				throw new IOException("Could not retrieve classbytes.");
		} catch (IOException e) {
			e.printStackTrace();
			cw.sanitise();
			return false;
		}

		cw.sanitise();
		return deployRule(ruleDef, classbytes);
	}

	// ================================================================================

	/**
	 * Deploy the rule. Save the code, class and the details in the database.
	 * Set a deault sequence number. TODO get the sequence number and the name
	 * of the rule by running the class methods.
	 * 
	 * @param ruleDef
	 * @param classbytes
	 * @return true if the rule was deployed successfully. False otherwise.
	 */
	public boolean deployRule(RuleDef ruleDef, byte[] classbytes) {

		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			if (ruleDef.getSeqNo() == -1.0) {
				double sequenceNumber = 1.0;
				String query = "select max(seq_number) from rules_definitions where type=?";
				PreparedStatement stmt = conn.prepareStatement(query);
				stmt.setString(1, ruleDef.getType());
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					sequenceNumber = rs.getDouble(1);
				}
				if (sequenceNumber < 0.0)
					sequenceNumber = 0.0;
				sequenceNumber++;
				ruleDef.setSeqNo(sequenceNumber);
			}

			addRule(conn, ruleDef.getName(), ruleDef.getType(), ruleDef
					.getSeqNo(), classbytes, ruleDef.getRuleCode());

			conn.commit();

			// Add rule to the ruleManager
			RuleClass rc = new RuleClass(ruleDef.getName(), ruleDef.getType(),
					ruleDef.getSeqNo());
			RulesManager.getInstance().putRule(rc, classbytes);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return false;
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}

	}

	// ================================================================================

	/**
	 * Get the details of the rule from the database
	 * 
	 * @param name
	 * @return RuleClass instance without the class set.
	 */
	public RulesClient getRuleDetails(String name) {

		RulesClient rc = null;
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			rc = getRuleDetails(conn, name);

			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return rc;
	}

	/**
	 * fetch the code of the rule from the database
	 */
	public String getRuleCode(String name) {

		String ruleCode = null;
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			String query = "select code from rules_storage where name = ?";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				ruleCode = rs.getString("code");
			}
			rs.close();
			stmt.close();

			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return ruleCode;
	}

	// ================================================================================

	/**
	 * Delete the rule from the database
	 */
	public boolean deleteRule(RuleDef ruleDef) throws TbitsExceptionClient {

		Connection conn = null;

		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			RulesClient rc = getRuleDetails(conn, ruleDef.getName());
			if (rc == null) {
				conn.commit();
				return false;
			}

			int id = rc.id;
			String query = "delete from rules_definitions where id=?";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setInt(1, id);
			stmt.executeUpdate();
			stmt.close();

			query = "delete from rules_storage where id=?";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, id);
			stmt.executeUpdate();
			stmt.close();

			conn.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new TbitsExceptionClient("Unable to delete rule", e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	// ================================================================================

	/**
	 * Undeploy the rule. Set the sequence number to -1
	 */
	public boolean undeployRule(RuleDef ruleDef) throws TbitsExceptionClient {

		Connection conn = null;

		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			RulesClient rc = getRuleDetails(conn, ruleDef.getName());
			if (rc == null || rc.seq_no < 0.0) {
				conn.commit();
				return false;
			}

			int id = rc.id;
			String query = "update rules_definitions set seq_number=? where id=?";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setInt(1, -1);
			stmt.setInt(2, id);
			stmt.executeUpdate();
			stmt.close();

			conn.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new TbitsExceptionClient("Unable to undeploy rule", e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	// ================================================================================

	// Utility Functions

	/**
	 * Get the details of the rule from the database
	 * 
	 * @param name
	 * @return RuleClass instance without the class set.
	 * @throws Exception
	 */
	private RulesClient getRuleDetails(Connection conn, String name)
			throws SQLException {

		RulesClient rc = null;
		String query = "select * from rules_definitions where name = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, name);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			rc = new RulesClient();
			rc.setName(rs.getString("name"));
			rc.setType(rs.getString("type"));
			rc.setSeq(rs.getFloat("seq_number"));
			rc.setId(rs.getInt("id"));
		}
		return rc;
	}

	/**
	 * Add a new rule with specified details to the database
	 * 
	 * @param name
	 * @param type
	 * @param sequenceNumber
	 * @param classbytes
	 * @param source
	 * @throws SQLException
	 */
	private void addRule(Connection conn, String name, String type,
			double sequenceNumber, byte[] classbytes, String source)
			throws SQLException {

		RulesClient rc = getRuleDetails(conn, name);
		if (rc != null) {

			String query = "";
			PreparedStatement stmt = null;

			query = "update rules_definitions set seq_number = ? where id = ? and name = ?";
			stmt = conn.prepareStatement(query);
			stmt.setInt(2, rc.id);
			stmt.setString(3, rc.name);
			stmt.setDouble(1, sequenceNumber);
			stmt.executeUpdate();
			stmt.close();

			query = "update rules_storage set code = ?, class = ? where id = ? and name = ?";
			stmt = conn.prepareStatement(query);
			stmt.setInt(3, rc.id);
			stmt.setString(4, name);
			stmt.setString(1, source);
			stmt.setBytes(2, classbytes);
			stmt.executeUpdate();
			stmt.close();

		} else {

			String query = "select max(id) from rules_definitions";
			int max_id = 0;
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				max_id = rs.getInt(1);
			max_id++;
			rs.close();
			stmt.close();

			query = "insert into rules_storage values (?,?,?,?)";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, max_id);
			stmt.setString(2, name);
			stmt.setString(3, source);
			stmt.setBytes(4, classbytes);
			stmt.executeUpdate();
			stmt.close();

			query = "insert into rules_definitions values (?,?,?,?)";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, max_id);
			stmt.setString(2, name);
			stmt.setString(3, type);
			stmt.setDouble(4, sequenceNumber);
			stmt.executeUpdate();
			stmt.close();
		}

	}

	/**
	 * @return the url of the documentation of the given class
	 */
	public String getClassDocumentationUrl(String iClass) {

		ArrayList<String> tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(iClass, ".");
		while (st.hasMoreTokens()) {
			tokens.add(st.nextToken());
		}

		String url = WebUtil.getServletPath(this.getRequest(), "/javadoc/");
		for (int i = 0; i < tokens.size() - 1; i++) {
			url += tokens.get(i) + "/";
		}
		url += "webapps/" + tokens.get(tokens.size() - 1) + ".html";

		return url;
	}

	/**
	 * Return the id for the next rule
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private int getNextRuleId(Connection conn) throws SQLException {

		String query = "select max(id) from rules_definitions";
		Statement stmt = conn.createStatement();
		stmt.execute(query);
		ResultSet rs = stmt.getResultSet();
		if (rs.next())
			return (rs.getInt(1) + 1);
		return 1;
	}

	// =====================================================================================
	// ^^ Rules ^^

	/**
	 * Gets the {@link TbitsTreeRequestData} for a particular request_id and
	 * user_id
	 * 
	 * @param sysPrefix
	 * @param userId
	 * @param requestId
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public TbitsTreeRequestData getDataByRequestId(String sysPrefix,
			int userId, int requestId) throws TbitsExceptionClient {

		User user = null;
		BusinessArea ba = null;
		try {
			user = User.lookupAllByUserId(userId);
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		return GWTServiceHelper.getDataByRequestId(user, ba, requestId);
	}

	// ====================================================================================
	// vv Perm Tool vv

	/**
	 * Fetches the {@link PermissionInfo} for the given parameters.
	 */
	public PermissionInfo fetchPermissionInformation(int sysId, int userId,
			int reqId) {

		PermissionInfo permInfo = new PermissionInfo();

		// Get the list of mailing list ids
		ArrayList<User> mailingListsUser = MailListUser
				.getMailListsByRecursiveMembership(userId);
		ArrayList<UserClient> mailingListsClient = new ArrayList<UserClient>();
		ArrayList<Integer> allUserIds = new ArrayList<Integer>();
		allUserIds.add(userId);
		for (User u : mailingListsUser) {
			try {
				mailingListsClient.add(GWTServiceHelper.fromUser(u));
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			allUserIds.add(u.getUserId());
		}
		permInfo.setMailingLists(mailingListsClient);

		// Get all the roles for the user
		ArrayList<Integer> roleIds = new ArrayList<Integer>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			String query = "("
					+ getQueryForUserRole(sysId)
					+ ")\n"
					+ "union\n("
					+ getQueryForStaticRoles(sysId, allUserIds)
					+ ")\n"
					+ ((reqId <= 0) ? ""
							: ("union\n"
									+ "("
									+ getQueryForDynamicRoles(sysId, reqId,
											allUserIds) + ")\n"));
			Statement stmt = conn.createStatement();
			stmt.execute(query);
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				roleIds.add(rs.getInt(1));
			}

			conn.commit();
		} catch (SQLException e) {
			// ignore
			e.printStackTrace();
		}
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				// ignore
				e.printStackTrace();
			}

		ArrayList<RoleClient> list = new ArrayList<RoleClient>();
		for (int roleId : roleIds) {
			try {
				RoleClient rc = new RoleClient();
				GWTServiceHelper.setValuesInDomainObject(Role
						.lookupBySystemIdAndRoleId(sysId, roleId), rc);
				list.add(rc);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		permInfo.setRoles(list);

		// Get the field vs. permission mapping
		HashMap<String, HashMap<String, Boolean>> fieldPermMap = new HashMap<String, HashMap<String, Boolean>>();
		try {
			Hashtable<String, Integer> permissions = RolePermission
					.getPermissionsBySystemIdAndRequestIdAndUserId(sysId,
							reqId, userId);
			for (String fName : permissions.keySet()) {
				HashMap<String, Boolean> perms = new HashMap<String, Boolean>();
				int perm = permissions.get(fName);
				perms.put(RolePermissionModel.IS_ADD,
						((perm & Permission.ADD) != 0));
				perms.put(RolePermissionModel.IS_UPDATE,
						((perm & Permission.CHANGE) != 0));
				perms.put(RolePermissionModel.IS_VIEW,
						((perm & Permission.VIEW) != 0));
				perms.put(RolePermissionModel.IS_EMAIL,
						((perm & Permission.EMAIL_VIEW) != 0));
				fieldPermMap.put(fName, perms);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		permInfo.setFieldMap(fieldPermMap);

		return permInfo;
	}

	// ====================================================================================

	/**
	 * Generate the query for getting hardcoded user role
	 * 
	 * @param user_ids
	 * @return query
	 */
	public static String getQueryForUserRole(int sysId) {

		String query = "select role_id from roles where sys_id = " + sysId
				+ " and rolename like 'user'";

		return (query);
	}

	// ====================================================================================

	/**
	 * Generate the query for getting the static roles that the user belongs to
	 * 
	 * @param user_ids
	 * @return query
	 */
	public static String getQueryForStaticRoles(int sysId,
			ArrayList<Integer> user_ids) {

		String query = "select distinct role_id \n" + "from roles_users \n"
				+ "where sys_id=" + sysId + " \n";

		String userCondition = "";
		for (int uid : user_ids) {
			if (!userCondition.equals(""))
				userCondition += " or";
			userCondition += " user_id=" + uid;
		}
		if (!userCondition.equals(""))
			userCondition = " and (" + userCondition + ")\n";

		return (query + userCondition);
	}

	// ====================================================================================

	/**
	 * Generate the query for getting the roles of the list of users by virtue
	 * of them being in the request users list
	 * 
	 * @param user_ids
	 * @return query
	 */
	public static String getQueryForDynamicRoles(int sysId, int reqId,
			ArrayList<Integer> user_ids) {

		String query = "select distinct r.role_id \n"
				+ "from request_users ru \n"
				+ "join roles r on ru.field_id=r.field_id and ru.sys_id=r.sys_id \n"
				+ "and ru.sys_id=" + sysId + " and ru.request_id=" + reqId
				+ " \n";

		String userCondition = "";
		for (int uid : user_ids) {
			if (!userCondition.equals(""))
				userCondition += " or";
			userCondition += " ru.user_id=" + uid;
		}
		if (!userCondition.equals(""))
			userCondition = " and (" + userCondition + ")\n";

		return (query + userCondition);
	}

	// ====================================================================================

	/**
	 * Fetch all the roles affecting the given RolePermissionModel and existing
	 * in the list of roles provided.
	 */
	public HashMap<String, List<String>> fetchRolesAffecting(int sysId,
			RolePermissionModel rpm, List<Integer> roles)
			throws TbitsExceptionClient {

		HashMap<String, List<String>> result = new HashMap<String, List<String>>();

		result.put(RolePermissionModel.IS_VIEW, getRolesFor(sysId, rpm
				.getFieldName(), roles, RolePermissionModel.IS_VIEW));
		result.put(RolePermissionModel.IS_ADD, getRolesFor(sysId, rpm
				.getFieldName(), roles, RolePermissionModel.IS_ADD));
		result.put(RolePermissionModel.IS_UPDATE, getRolesFor(sysId, rpm
				.getFieldName(), roles, RolePermissionModel.IS_UPDATE));
		result.put(RolePermissionModel.IS_EMAIL, getRolesFor(sysId, rpm
				.getFieldName(), roles, RolePermissionModel.IS_EMAIL));

		return result;
	}

	// ====================================================================================

	/**
	 * Get the roles affecting the given field and existing in the provided list
	 * of roles.
	 * 
	 * @param sysId
	 * @param fieldName
	 * @param roles
	 * @param permType
	 * @return List of role names
	 * @throws TbitsExceptionClient
	 */
	private List<String> getRolesFor(int sysId, String fieldName,
			List<Integer> roles, String permType) throws TbitsExceptionClient {

		String permCol = "";
		if (permType.equals(RolePermissionModel.IS_VIEW)) {
			permCol = "pview";
		} else if (permType.equals(RolePermissionModel.IS_ADD)) {
			permCol = "padd";
		} else if (permType.equals(RolePermissionModel.IS_UPDATE)) {
			permCol = "pchange";
		} else if (permType.equals(RolePermissionModel.IS_EMAIL)) {
			permCol = "pEmailView";
		} else {
			throw new TbitsExceptionClient(
					"Unknown type of permission requested");
		}

		String roleStr = "";
		for (int rid : roles) {
			if (!roleStr.equals(""))
				roleStr += " or ";
			roleStr += "rp.role_id=" + rid;
		}
		String query = "select r.* from roles_permissions rp \n"
				+ "join roles r on rp.role_id=r.role_id and r.sys_id=rp.sys_id \n"
				+ "join fields f on f.sys_id=rp.sys_id and f.field_id=rp.field_id and f.name='"
				+ fieldName + "' and rp.sys_id=" + sysId + "\n"
				+ "join permissions p on rp.gpermissions=p.permission and p."
				+ permCol + "<>0 \n" + " and (" + roleStr + ")";

		ArrayList<String> affectingRoles = new ArrayList<String>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute(query);
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				affectingRoles.add(rs.getString("rolename"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return affectingRoles;
	}

	@Override
	public List<ReportParamClient> getReportParams(int reportId)
			throws TbitsExceptionClient {
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();

			String sql = "SELECT * FROM report_params where report_id = ?";

			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, reportId);
			ResultSet rs = stmt.executeQuery();
			if (rs != null) {
				List<ReportParamClient> models = new ArrayList<ReportParamClient>();
				while (rs.next()) {
					String name = rs.getString("param_name");
					String value = rs.getString("param_value");

					ReportParamClient model = new ReportParamClient();
					model.setReportId(reportId);
					model.setName(name);
					model.setValue(value);
					models.add(model);
				}

				return models;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(e);
				}
			}
		}

		return null;
	}

	@Override
	public boolean updateReportParams(int reportId,
			List<ReportParamClient> models) throws TbitsExceptionClient {
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();

			for (ReportParamClient model : models) {
				String sql = "SELECT * FROM report_params where report_id = ? and param_name = ?";
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setInt(1, reportId);
				ps.setString(2, model.getName());
				ResultSet rs = ps.executeQuery();
				if (rs != null && rs.next()) {
					sql = "UPDATE report_params SET param_value = ? where report_id = ? and param_name = ?";
					ps = conn.prepareStatement(sql);
					ps.setString(1, model.getValue());
					ps.setInt(2, reportId);
					ps.setString(3, model.getName());
					ps.execute();
				} else {
					sql = "INSERT INTO report_params(report_id, param_name, param_value) "
							+ "VALUES(?, ?, ?)";
					ps = conn.prepareStatement(sql);
					ps.setInt(1, reportId);
					ps.setString(2, model.getName());
					ps.setString(3, model.getValue());
					ps.execute();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(e);
				}
			}
		}

		return true;
	}

	@Override
	public List<HolidayClient> getHolidayList() throws TbitsExceptionClient {
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();

			String sql = "SELECT * FROM holidays_list";

			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			if (rs != null) {
				List<HolidayClient> models = new ArrayList<HolidayClient>();
				while (rs.next()) {
					String office = rs.getString("office");
					String date = rs.getString("holiday_date");
					String zone = rs.getString("office_zone");
					String desciption = rs.getString("description");

					HolidayClient model = new HolidayClient();
					model.setOffice(office);
					model.setDate(date);
					model.setZone(zone);
					model.setDescription(desciption);
					models.add(model);
				}

				return models;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(e);
				}
			}
		}

		return null;
	}

	@Override
	public boolean updateHolidayList(List<HolidayClient> models)
			throws TbitsExceptionClient {
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();

			String sql = "DELETE FROM holidays_list";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.execute();

			for (HolidayClient model : models) {
				sql = "INSERT INTO holidays_list(office, holiday_date, office_zone, description) "
						+ "VALUES(?, ?, ?, ?)";
				ps = conn.prepareStatement(sql);
				ps.setString(1, model.getOffice());
				ps.setString(2, model.getDate());
				ps.setString(3, model.getZone());
				ps.setString(4, model.getDescription());
				ps.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(e);
				}
			}
		}

		return true;
	}

	public List<TypeDependency> getTypeDependencies(String sysPrefix)
			throws TbitsExceptionClient {
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			List<Field> fields = Field.lookupBySystemId(ba.getSystemId());

			List<TypeDependency> dependencies = new ArrayList<TypeDependency>();
			for (Field field : fields) {
				if (GWTServiceHelper.isTypeField(field)) {
					dependencies.addAll(GWTServiceHelper
							.getTypeDependenciesForField(field));
				}
			}

			return dependencies;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean updateTypeDependencies(TypeClient type,
			List<TypeDependency> dependencies) throws TbitsExceptionClient {
		try {
			Connection conn = DataSourcePool.getConnection();
			String sql = "DELETE from type_dependency where sys_id = ? and src_field_id = ? and src_type_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, type.getSystemId());
			statement.setInt(2, type.getFieldId());
			statement.setInt(3, type.getTypeId());
			statement.execute();

			for (TypeDependency dependency : dependencies) {
				sql = "INSERT INTO type_dependency(sys_id, src_field_id, src_type_id, dest_field_id, dest_type_id) "
						+ "VALUES(?, ?, ?, ?, ?)";
				statement = conn.prepareStatement(sql);
				statement.setInt(1, dependency.getSysId());
				statement.setInt(2, dependency.getSrcFieldId());
				statement.setInt(3, dependency.getSrcTypeId());
				statement.setInt(4, dependency.getDestFieldId());
				statement.setInt(5, dependency.getDestTypeId());
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		return true;
	}

	@Override
	public List<TypeDependency> getTypeDependenciesForType(TypeClient type)
			throws TbitsExceptionClient {
		List<TypeDependency> dependencies = new ArrayList<TypeDependency>();

		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();

			String sql = "select * from type_dependency where sys_id = ? and src_field_id = ? and src_type_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, type.getSystemId());
			statement.setInt(2, type.getFieldId());
			statement.setInt(3, type.getTypeId());

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				int sysId = rs.getInt(TypeDependency.SYS_ID);
				int srcFieldId = rs.getInt(TypeDependency.SRC_FIELD_ID);
				int srcTypeId = rs.getInt(TypeDependency.SRC_TYPE_ID);
				int destFieldId = rs.getInt(TypeDependency.DEST_FIELD_ID);
				int destTypeId = rs.getInt(TypeDependency.DEST_TYPE_ID);

				TypeDependency td = new TypeDependency();
				td.setSysId(sysId);
				td.setSrcFieldId(srcFieldId);
				td.setSrcTypeId(srcTypeId);
				td.setDestFieldId(destFieldId);
				td.setDestTypeId(destTypeId);

				dependencies.add(td);
			}
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.info(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
		}
		return dependencies;
	}

	@Override
	public SysConfigClient getSysConfigClient(SysConfigClient sysconfig) {
		return null;
	}

	@Override
	public List<BAMenuClient> getBAMenus() throws TbitsExceptionClient {
		List<BAMenuClient> baMenuClients = new ArrayList<BAMenuClient>();
		try {
			List<BAMenu> baMenus = BAMenu.getAllBAMenus();
			for (BAMenu baMenu : baMenus) {
				BAMenuClient menuClient = new BAMenuClient();
				GWTServiceHelper.setValuesInDomainObject(baMenu, menuClient);
				baMenuClients.add(menuClient);
			}
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}

		return baMenuClients;
	}

	@Override
	public boolean deleteMenus(List<BAMenuClient> models)
			throws TbitsExceptionClient {
		for (BAMenuClient baMenu : models) {
			try {
				if (!BAMenu.delete(baMenu.getMenuId()))
					return false;
			} catch (TBitsException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			} catch (DatabaseException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
		}

		return true;
	}

	@Override
	public List<BAMenuClient> updateMenus(List<BAMenuClient> models)
			throws TbitsExceptionClient {
		List<BAMenuClient> resp = new ArrayList<BAMenuClient>();
		for (BAMenuClient baMenu : models) {
			try {
				BAMenu menu = new BAMenu();
				GWTServiceHelper.setValuesInDomainObject(baMenu, menu);
				if (menu.getMenuId() != 0) {
					menu = BAMenu.update(menu);
				} else {
					menu = BAMenu.insert(menu);
				}

				if (menu == null) {
					throw new TbitsExceptionClient("Could not update menus");
				} else {
					BAMenuClient model = new BAMenuClient();
					GWTServiceHelper.setValuesInDomainObject(menu, model);
					resp.add(model);
				}
			} catch (TBitsException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			} catch (DatabaseException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
		}

		return resp;
	}

	@Override
	public boolean updateBAMenuMapping(int menuId, List<BusinessAreaClient> bas)
			throws TbitsExceptionClient {
		for (BusinessAreaClient ba : bas) {
			int sysId = ba.getSystemId();
			try {
				if (!BAMenu.updateMapping(menuId, sysId))
					return false;
			} catch (DatabaseException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
		}
		return true;
	}

	@Override
	public List<Integer> getBAMenuMapping(int menuId)
			throws TbitsExceptionClient {
		try {
			return BAMenu.getMapping(menuId);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}

	@Override
	public List<BusinessAreaClient> getAllBAList() throws TbitsExceptionClient {
		List<BusinessAreaClient> bas = new ArrayList<BusinessAreaClient>();
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
			if ((user != null) && RoleUser.isSuperUser(user.getUserId())) {
				for (BusinessArea ba : BusinessArea.getAllBusinessAreas()) {
					BusinessAreaClient baClient = new BusinessAreaClient();
					GWTServiceHelper.getBAClientByBA(ba, baClient);
					bas.add(baClient);
				}
				return bas;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		return null;
	}

	public boolean checkIfUserIsMailingList(int user_id, int sysID)
			throws SQLException, TbitsExceptionClient, DatabaseException {

		User user;
		Connection conn = null;
		conn = DataSourcePool.getConnection();

		int type = 0;
		String sql = "select user_type_id as type from users where user_id = ?";
		PreparedStatement statement = conn.prepareStatement(sql);
		statement.setInt(1, user_id);

		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			type = rs.getInt("type");

			if (type == 8) {
				return true;
			} else {
				return false;
			}

		}
		return false;

	}

	ArrayList<UserClient> getUserForMailingList(int userId)
			throws TbitsExceptionClient {

		ArrayList<UserClient> UserList = new ArrayList<UserClient>();

		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			String sql = "SELECT B.USER_ID as ID FROM USERS A,MAIL_LIST_USERS B WHERE A.USER_ID = B.MAIL_LIST_ID AND A.USER_ID = ?";
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setInt(1, userId);

			ResultSet rs = ps.executeQuery();
			if (null != rs) {
				while (rs.next()) {
					int UserId = rs.getInt("ID");

					User uc = User.lookupAllByUserId(UserId);
					UserClient temp = new UserClient();

					GWTServiceHelper.setValuesInDomainObject(uc, temp);

					UserList.add(temp);
				}
			}

			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return UserList;

	}

	@Override
	public List<FieldClient> fetchQueriedFields(String filter, String value,int sysid) {
		int searchFrom = 0;
		int specialIndex = 0;
		while ((specialIndex = value.indexOf("'", searchFrom)) >= 0) {
			value = value.substring(0, specialIndex) + "'"
					+ value.substring(specialIndex);
			searchFrom = specialIndex + 2;
		}
		searchFrom = 0;
		specialIndex = 0;
		ArrayList<String> specials = new ArrayList<String>();
		specials.add("%");
		specials.add("_");
		specials.add("[");
		for (String special : specials) {
			while ((specialIndex = value.indexOf(special, searchFrom)) >= 0) {
				value = value.substring(0, specialIndex) + "\\"
						+ value.substring(specialIndex);
				searchFrom = specialIndex + 1 + special.length();
			}
		}

		Connection conn = null;
		ArrayList<FieldClient> fields = new ArrayList<FieldClient>();
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			String query = "select field_id from fields where sys_id = " + sysid + " and " + filter
					+ " like '%" + value + "%' {escape '\\'}" ;
			
			

		//	String query = "select user_id from fields where sys_id = ? and ? like '%?%' {escape '\\'}" ;
			/*
			PreparedStatement ps = conn.prepareStatement(query);

			ps.setInt(1, sysid);
			ps.setString(2, filter);
			ps.setString(3, value);
			*/
			
		//	ResultSet rs =ps.executeQuery(query);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				Field field = Field.lookupBySystemIdAndFieldId(sysid, rs.getInt("field_id"));
				FieldClient fieldClient = new FieldClient();
				GWTServiceHelper.setValuesInDomainObject(field, fieldClient);
			/*
				if (user.getWebConfigObject() != null)
					userClient.setWebDateFormat(user.getWebConfigObject()
							.getWebDateFormat());
*/
				fields.add(fieldClient);
			}

			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return fields;
	}



	@Override
	public List<EscalationHierarchiesClient> getEscalationHierarchies()
			throws TbitsExceptionClient {
		
		
		List<EscalationHierarchies> escHieServerList=EscalationCommonUtils.getEscalationHierarchies();
		
		List<EscalationHierarchiesClient> escHieClientList=new ArrayList<EscalationHierarchiesClient>();
		
		if(escHieServerList != null)
		for(EscalationHierarchies eh:escHieServerList)
		{
			EscalationHierarchiesClient ehc=new EscalationHierarchiesClient();
			GWTServiceHelper.setValuesInDomainObject(eh,ehc);
			System.out.println("server :" + eh);
			System.out.println("client : " + ehc);
			escHieClientList.add(ehc);
		}

		return escHieClientList;

	}

	@Override
	public List<EscalationHierarchyValuesClient> saveEscalationHierarchyValues(
			EscalationHierarchiesClient hiearachyClient,
			List<EscalationHierarchyValuesClient> valuesClient) throws TbitsExceptionClient {
		
		List<EscalationHierarchyValuesClient> escHierarValuesClient = new ArrayList<EscalationHierarchyValuesClient>();
		List<EscalationHierarchyValues>  escHierarValuesServer=new ArrayList<EscalationHierarchyValues>();
		for(EscalationHierarchyValuesClient ehvc:valuesClient)
		{
			EscalationHierarchyValues ehv=new EscalationHierarchyValues();
			GWTServiceHelper.setValuesInDomainObject(ehvc,ehv );
			System.out.println("server values :"+ ehv);
			System.out.println("clinet values" + ehvc);
			escHierarValuesServer.add(ehv);
		}
		
		int cycleUserId=EscalationServerUtils.checkCycleInEscalationHierarchy(valuesClient);
		if(cycleUserId != 0)
		{
			throw new TbitsExceptionClient("Their is a cycle in hierarchy for the user id : "+ cycleUserId);
		}
		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			String sql = "DELETE FROM escalation_hierarchy where esc_id = ?";

			PreparedStatement cs = connection.prepareStatement(sql);
			cs.setInt(1, hiearachyClient.getEscId());

			cs.execute();
			cs.close();

			for (EscalationHierarchyValuesClient value : valuesClient) {
				escHierarValuesClient.add(saveEscalationHierarchyValues(connection,hiearachyClient, value));
			}
			connection.commit();
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}

				connection = null;
			}
		}

		return escHierarValuesClient;
	}
	
	private static EscalationHierarchyValuesClient saveEscalationHierarchyValues(Connection connection,EscalationHierarchiesClient hierarchy,
			EscalationHierarchyValuesClient value) throws TbitsExceptionClient, SQLException{
		
		String sql = "INSERT INTO escalation_hierarchy "
				+ "(esc_id, user_id, parent_id) "
				+ "VALUES(?,?,?)";

		PreparedStatement ps = connection.prepareStatement(sql);

		ps.setInt(1, hierarchy.getEscId());
		ps.setInt(2, value.getChlidUser().getUserId());
		ps.setInt(3, value.getParentUser().getUserId());
		
		ps.execute();
		ps.close();

		
		value.setEscId(hierarchy.getEscId());
		
		value.set(IBulkUpdateConstants.RESPONSE_STATUS,IBulkUpdateConstants.UPDATED);

		return value;
		
	}

	@Override
	public List<EscalationHierarchyValuesClient> getEscalationHierarchiesValues(
			EscalationHierarchiesClient hiearachy) throws TbitsExceptionClient {
		List<EscalationHierarchyValuesClient> escalationHierarchyValuesList=new ArrayList<EscalationHierarchyValuesClient>();
		Connection connection=null;
		try {
			connection=DataSourcePool.getConnection();
			String sql="select * from escalation_hierarchy where esc_id= ?";
			PreparedStatement ps=connection.prepareStatement(sql);
			ps.setInt(1, hiearachy.getEscId());
			ResultSet rs=ps.executeQuery();
			
			if(null != rs)
			{
				while(rs.next())
				{
					int esc_Id=rs.getInt("esc_id");
					int child_Id=rs.getInt("user_id");
					int parent_Id=rs.getInt("parent_id");
					
					EscalationHierarchyValuesClient hierarchyValue=new EscalationHierarchyValuesClient();
					hierarchyValue.setEscId(esc_Id);
					if(null == getUserClientbyUserId(child_Id))
					continue;
					else
						hierarchyValue.setChildUser(getUserClientbyUserId(child_Id));
					if(null == getUserClientbyUserId(parent_Id))
						continue;
						else
							hierarchyValue.setParentUser(getUserClientbyUserId(parent_Id));
						escalationHierarchyValuesList.add(hierarchyValue);
					
				}
				
			}
			
			
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient();
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
                connection = null;
            }
        }
		return escalationHierarchyValuesList;
	}

	@Override
	public EscalationHierarchiesClient insertEscalationHierarchies(
			EscalationHierarchiesClient hierarchies) throws TbitsExceptionClient {
		
		EscalationHierarchies hierarchyServer=new EscalationHierarchies();
		GWTServiceHelper.setValuesInDomainObject(hierarchies, hierarchyServer);
		System.out.println("escalation server: "+ hierarchyServer);//just for conformation
		System.out.println("escaltuion client :"+ hierarchies);
		
		Connection connection= null;
		
		try{
			connection=DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			String checkSql="select * from escalation_hierarchy_details where name=?";
			PreparedStatement ps=connection.prepareStatement(checkSql);
			ps.setString(1, hierarchyServer.getName());
			ResultSet rs=ps.executeQuery();
			if (rs.next()) {
				rs.close();
				ps.close();
				return null;
			}

			else
			{
				String sql="insert into escalation_hierarchy_details (name,display_name,description) values(?,?,?)";
				             
				PreparedStatement pres=connection.prepareStatement(sql);
				pres.setString(1, hierarchyServer.getName());
				pres.setString(2, hierarchyServer.getDisplayName());
				pres.setString(3, hierarchyServer.getDescription());
				
				pres.execute();
				connection.commit();
								
			}
			
			
			
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}

				connection = null;
			}
		}
		return hierarchies;
		
	}

	@Override
	public UserClient getUserClientbyUserId(int userid)
			throws TbitsExceptionClient {
		
		User user;

		try {
			user = User.lookupByUserId(userid);
			if (null == user)
				return null;

			UserClient userClient = new UserClient();
			GWTServiceHelper.setValuesInDomainObject(user, userClient);
			return userClient;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean deleteEscCondtion(Integer escCondId)
			throws TbitsExceptionClient {
			
		Connection connection= null;
		
		try{
			connection=DataSourcePool.getConnection();
			connection.setAutoCommit(false);
				String sql="delete from escalation_conditions where esc_cond_id=?";
				             
				PreparedStatement ps=connection.prepareStatement(sql);
				ps.setInt(1,escCondId);
				ps.execute();
				connection.commit();
								
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
					
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}

				connection = null;
				return false;
			}
		}	
		return true;
	}

	@Override
	public ArrayList<EscalationConditionDetailClient> getAllEscCondition()
			throws TbitsExceptionClient {
		
		
		ArrayList<EscalationConditionDetailClient> escClientsList=new ArrayList<EscalationConditionDetailClient>();
		Gson gson = new Gson();
		Collection<EscalationConditionParameters> serverParamList=new ArrayList<EscalationConditionParameters>();
		ArrayList<EscalationConditionParametersClient> paramlist=new ArrayList<EscalationConditionParametersClient>();
		
		Connection connection=null;
		try{
			connection=DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			String sql="select * from escalation_conditions";
			PreparedStatement ps=connection.prepareStatement(sql);
			ResultSet rs=ps.executeQuery();
			
			if(null !=rs)
			{
			while(rs.next())
			{
				EscalationConditionDetailClient detailClient=new EscalationConditionDetailClient();
				detailClient.setEscCondId(rs.getInt("esc_cond_id"));
				detailClient.setDisName(rs.getString("display_name"));
				detailClient.setDescription(rs.getString("descripton"));
				EscalationHierarchiesClient hierarchy=new EscalationHierarchiesClient();
				hierarchy.setEscId(rs.getInt("esc_id"));
				detailClient.setEscHierarchy(hierarchy);
				detailClient.setSrcBa(rs.getString("src_ba_prefix"));
				detailClient.setSrcDateField(rs.getString("src_date_field_name"));
				detailClient.setSrcUserField(rs.getString("src_user_field_name"));
				detailClient.setDesDateField(rs.getString("dest_date_field_name"));
				detailClient.setDesUserField(rs.getString("dest_user_field_name"));
				detailClient.setSpan(rs.getString("span"));
				detailClient.setDql(rs.getString("dql"));
				detailClient.setOnBehalfUser(rs.getString("on_behalf_user"));
				detailClient.setIsActive(rs.getBoolean("isActive"));
			
				String jsonString=rs.getString("param_json");
				java.lang.reflect.Type collectionType = new TypeToken<Collection<EscalationConditionParameters>>(){}.getType();
				serverParamList=gson.fromJson(jsonString,collectionType);
				for(EscalationConditionParameters serParam:serverParamList)
				{
					EscalationConditionParametersClient param=new EscalationConditionParametersClient();
					param.setName(serParam.getName());
					param.setValue(serParam.getValue());
					paramlist.add(param);
					
				}
				detailClient.setParams(paramlist);
				
				escClientsList.add(detailClient);
				
			}
			
			return escClientsList;
			
			}
		}
		catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
					
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}

				connection = null;
				
			}
		}	
		
		
		return escClientsList;
	}

	@Override
	public boolean saveCondition(String mode,
			EscalationConditionDetailClient condDetail,
			ArrayList<EscalationConditionParametersClient> ecpcList)
			throws TbitsExceptionClient {
		
		Collection<EscalationConditionParameters> serverParams=new ArrayList<EscalationConditionParameters>();
		for(EscalationConditionParametersClient parameObj:ecpcList)
		{
			EscalationConditionParameters param=new EscalationConditionParameters();
			String name=parameObj.getName();
			String value=parameObj.getValues();
			param.setName(name);
			param.setValue(value);
			serverParams.add(param);
			System.out.println(parameObj.getName() + "\t" + parameObj.getValues());
		}
		
		Connection connection=null;
		Gson gson = new Gson();
		String jsonString=gson.toJson(serverParams);
		
		try
		{
			connection=DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			String sql="";
			PreparedStatement ps;
		if(mode.equalsIgnoreCase("Edit"))
		{
			sql="update escalation_conditions set display_name=?,descripton=?,esc_id=?,src_ba_prefix=?,src_user_field_name=?,src_date_field_name=?,dest_user_field_name=?,dest_date_field_name=?,dql=?,span=?,on_behalf_user=?,param_json=?,isActive=? where esc_cond_id=?";
			ps=connection.prepareStatement(sql);
			ps.setString(1,condDetail.getDisName());
			ps.setString(2,condDetail.getDescription());
			ps.setInt(3,condDetail.getEscHierarchy().getEscId());
			ps.setString(4,condDetail.getSrcBa());
			ps.setString(5,condDetail.getSrcUserField());
			ps.setString(6,condDetail.getSrcDateField());
			ps.setString(7,condDetail.getDesDateField());
			ps.setString(8,condDetail.getDesUserField());
			ps.setString(9,condDetail.getDql());
			ps.setString(10,condDetail.getSpan());
			ps.setString(11,condDetail.getOnBehalfUser());
			
			ps.setString(12,jsonString);
			
			ps.setBoolean(13,condDetail.getIsActive());
			ps.setInt(14, condDetail.getEscCondId());
			ps.execute();
			connection.commit();
			return true;
		}
		
		if(mode.equalsIgnoreCase("Create Condition"))
		{
			    sql="insert into escalation_conditions (display_name,descripton,esc_id,src_ba_prefix,src_user_field_name,src_date_field_name,dest_user_field_name,dest_date_field_name,dql,span,on_behalf_user,param_json,isActive) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
				ps=connection.prepareStatement(sql);
				ps.setString(1,condDetail.getDisName());
				ps.setString(2,condDetail.getDescription());
				ps.setInt(3,condDetail.getEscHierarchy().getEscId());
				ps.setString(4,condDetail.getSrcBa());
				ps.setString(5,condDetail.getSrcUserField());
				ps.setString(6,condDetail.getSrcDateField());
				ps.setString(7,condDetail.getDesDateField());
				ps.setString(8,condDetail.getDesUserField());
				ps.setString(9,condDetail.getDql());
				ps.setString(10,condDetail.getSpan());
				ps.setString(11,condDetail.getOnBehalfUser());
				ps.setString(12,jsonString);
				ps.setBoolean(13,condDetail.getIsActive());
				ps.execute();
				
				
				
				connection.commit();
				return true;
			
		}
		}
		 catch (SQLException e) {
				try {
					if (connection != null)
						connection.rollback();
				} catch (SQLException e1) {
					// TODO: Log it --SG
					e1.printStackTrace();
					throw new TbitsExceptionClient(e1);
				}
				e.printStackTrace();
				throw new TbitsExceptionClient(e);
			} catch (Exception e) {
				try {
					if (connection != null)
						connection.rollback();
				} catch (SQLException e1) {
					// TODO: Log it --SG
					e1.printStackTrace();
					throw new TbitsExceptionClient(e1);
				}
				throw new TbitsExceptionClient(e);
			} finally {
				if (connection != null) {
					try {
						connection.close();
						
					} catch (SQLException sqle) {
						sqle.printStackTrace();
						throw new TbitsExceptionClient(sqle);
					}

					connection = null;
				}
			}	
		
		return false;
	}
	
	
	public EscalationHierarchiesClient getEscalationHierarchy(int escId) throws TbitsExceptionClient{
		
		EscalationHierarchies hierarchy=new EscalationHierarchies();
		EscalationHierarchiesClient hierarchyClient=new EscalationHierarchiesClient();
		
           Connection connection= null;
		
		try{
			connection=DataSourcePool.getConnection();
			connection.setAutoCommit(false);
				String sql="select * from escalation_hierarchy_details where esc_id=?";
				             
				PreparedStatement ps=connection.prepareStatement(sql);
				ps.setInt(1,escId);
				ResultSet rs=ps.executeQuery();
				if(null!=rs && rs.next())	
				{
					hierarchy.setEscId(escId);
					connection.commit();
				
					GWTServiceHelper.setValuesInDomainObject(hierarchy, hierarchyClient);
					System.out.println("escalation client:"+hierarchyClient);
					System.out.println("ecalation server :"+hierarchy);
					return hierarchyClient;
				}
				
								
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
					
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}

				connection = null;
			}
		}	
		return hierarchyClient;
	}

	@Override
	public List<EscalationHierarchiesClient> updateEscalationHierarchies(
			List<EscalationHierarchiesClient> hierarchiesList)
			throws TbitsExceptionClient {
		
		//EscalationHierarchies hierarchy=new EscalationHierarchies();
		
		List<EscalationHierarchiesClient> updatedHierarchies=new ArrayList<EscalationHierarchiesClient>();
		ArrayList<Integer> prevEscIds=new ArrayList<Integer>();
		ArrayList<Integer> currentEscIds=new ArrayList<Integer>();
		HashMap<Integer,EscalationHierarchiesClient> currentEscIdMap=new HashMap<Integer, EscalationHierarchiesClient>();
		for(EscalationHierarchiesClient hierarchy:hierarchiesList )
		{
			currentEscIdMap.put(hierarchy.getEscId(),hierarchy);
			
		}
		
		Connection connection=null;
		
		try
		{
			connection=DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			String sql="select esc_id from escalation_hierarchy_details";
			PreparedStatement ps=connection.prepareStatement(sql);
			ResultSet rs=ps.executeQuery();
			if(rs !=null)
			while(rs.next())
			{
				prevEscIds.add(rs.getInt("esc_id"));
			}
			
			prevEscIds.removeAll(currentEscIdMap.keySet());
			
			if(!prevEscIds.isEmpty())
			{
				String delSql="delete from escalation_hierarchy_details where esc_id=?";
				PreparedStatement delPs=connection.prepareStatement(delSql);
				for(Integer preInt:prevEscIds)
				{
					delPs.setInt(1, preInt);
					delPs.execute();
				}
				delPs.close();
			}
			
			String curSql="update escalation_hierarchy_details set display_name=?,description=? where esc_id=?";
			PreparedStatement curPs=connection.prepareStatement(curSql);
			
			for(Integer curInt:currentEscIdMap.keySet())
			{
				curPs.setString(1,currentEscIdMap.get(curInt).getDisplayName());
				curPs.setString(2,currentEscIdMap.get(curInt).getDescription());
				curPs.setInt(3, currentEscIdMap.get(curInt).getEscId());
				curPs.execute();
				
			}
			
			curPs.close();
			connection.commit();
			return hierarchiesList;
			
		}
		catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
					
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}

				connection = null;
			}
		}
		
		
		
	}

}
