/**
 * 
 */
package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.CaptionsProps;
import transbit.tbits.domain.BAMailAccount;
import transbit.tbits.domain.BARule;
import transbit.tbits.domain.BAUser;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DisplayGroup;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.WorkflowRule;

/**
 * @author Lokesh
 */
public class BusinessAreaExporter implements Serializable{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);
		
	private BusinessArea businessArea;
	private ArrayList<BAMailAccount> baMailAccountsList;
	private ArrayList<BARule> baRulesList;
	private ArrayList<WorkflowRule> wfRulesList = new ArrayList<WorkflowRule>();;
	private ArrayList<Field> fixedFieldsList;
	private ArrayList<Field> extendedFieldsList;	
	private ArrayList<Role> rolesList;	
	private ArrayList<User> baUsersList;
	private ArrayList<User> allUsersList;
	private ArrayList<DisplayGroup> displayGroupList; 
	private Hashtable<String, ArrayList<RoleUser>> rolesUserTable = new Hashtable<String, ArrayList<RoleUser>>();
	private Hashtable<String, ArrayList<FieldDescriptor>> fieldDescriptorsTable = new Hashtable<String, ArrayList<FieldDescriptor>>();
	private Hashtable<String, ArrayList<Type>> fieldTypes = new Hashtable<String, ArrayList<Type>>();
	private Hashtable<String, Hashtable <String, RolePermission>> rolesPermissionsTable = new Hashtable<String, Hashtable<String,RolePermission>>();
	private HashMap<String, String> baCaptionsMap = new HashMap<String, String>();
	private int systemId = -1;
		
	public BusinessAreaExporter(){}	
			
	public void initializeValues(String aSysPrefix){		
		setExportBusinessArea (aSysPrefix);		
		systemId  = businessArea.getSystemId();
		setBAMailAccounts(aSysPrefix);
		setDisplayGroups();
		setExportFixedFieldsAndTypes(systemId );
		setExportExtendedFieldsAndTypes(systemId );
		setBARules(systemId );
		setExportRoles(systemId );	
		setExportBAUsers(systemId );		
		setBACaptions(systemId );
		setAllUsers();				
	}
		
	public void setExportBusinessArea(String sysPrefix){			
		try {
			businessArea =BusinessArea.lookupBySystemPrefix(sysPrefix);
		} catch (DatabaseException e) { 
			e.printStackTrace();
		}					
	}
	
	public void setExportFixedFieldsAndTypes(int aSystemId){	
		String fieldName = null;
		try {
			fixedFieldsList = Field.getFixedFieldsBySystemId(aSystemId);			
			for (Field field : fixedFieldsList){
				fieldName = field.getName();
				addFDescriptorIntoTable(aSystemId, field.getFieldId(), fieldName);
				fieldTypes.put(fieldName, Type.lookupBySystemIdAndFieldName(aSystemId,fieldName));
			}			
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	
	public void setExportExtendedFieldsAndTypes(int aSystemId){
		String fieldName = null;
		try {			
			extendedFieldsList = Field.getExtendedFieldsBySystemId(aSystemId);	
			for (Field field : extendedFieldsList){
				fieldName = field.getName();
				addFDescriptorIntoTable(aSystemId, field.getFieldId(), fieldName);
				fieldTypes.put(fieldName, Type.lookupBySystemIdAndFieldName(aSystemId, fieldName));				
			}			
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}	
		
	public void setBARules (int aSystemId){		
		try {			
			baRulesList = BARule.lookupBySystemId (aSystemId);
			for (BARule baRule : baRulesList){
				wfRulesList.add(WorkflowRule.lookupByRuleId(baRule.getRuleId()));
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}		
	}
	
	public void setBAMailAccounts(String aSysPrefix){
		try {
			baMailAccountsList = BAMailAccount.lookupByBA(aSysPrefix);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	
	public void setDisplayGroups(){
		try {
			displayGroupList = DisplayGroup.lookupBySystemId(systemId);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	
	public void setExportRoles(int aSystemId){
		try {
			rolesList = Role.getRolesBySysId(aSystemId);
			for (Role role : rolesList){
				addRolePermissions(aSystemId, role.getRoleId(), role.getRoleName());
				addRoleUsers (aSystemId, role.getRoleId(), role.getRoleName());
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}		
	}
	
	public void setBACaptions(int aSystemId){
		try{
			baCaptionsMap = CaptionsProps.getInstance().getOnlyNonDefaultCaptions(aSystemId);
		}catch (DatabaseException e){
			e.printStackTrace();
		}
	}
	
	public void addFDescriptorIntoTable (int aSystemId, int aFieldId, String aFieldName){
		try {
			fieldDescriptorsTable.put(aFieldName, FieldDescriptor.lookupFDListBySystemIdAndFieldId(aSystemId, aFieldId));			
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	
	public void addRolePermissions(int aSystemId, int aRoleId, String aRoleName){
		try {
			rolesPermissionsTable.put(aRoleName, RolePermission.getPermissionsBySystemIdAndRoleId(aSystemId, aRoleId));
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	
	public void addRoleUsers (int aSystemId, int aRoleId, String aRolename){
		try {
			ArrayList <RoleUser> roleUserList = RoleUser.lookupBySystemIdAndRoleId(aSystemId, aRoleId);
			if(roleUserList != null)
				rolesUserTable.put(aRolename, roleUserList);
		} catch (DatabaseException e) {
			e.printStackTrace();
		} 
	}
		
	public void setExportBAUsers(int aSystemId){
		try {
			baUsersList = BAUser.getBusinessAreaUsers(aSystemId) ;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}	
	
	public void setAllUsers(){
		allUsersList = User.getActiveUsers();
	}	
	
	public BusinessArea getExportBA(){
		return businessArea;
	}	
		
	public ArrayList<BAMailAccount> getBAMailAccounts(){
		return baMailAccountsList;
	}
	
	public ArrayList<Field> getExportFixedFields(){
		return fixedFieldsList;
	}
	
	public ArrayList<Field> getExportExtendedFields(){
		return extendedFieldsList;
	}
	
	public ArrayList<DisplayGroup> getDisplayGroups(){
		return displayGroupList;
	}
	
	public ArrayList<FieldDescriptor> getFieldDescriptorList (String fieldName){
		ArrayList<FieldDescriptor> fieldDescList = fieldDescriptorsTable.get(fieldName);
		return fieldDescList;
	}
	
	public ArrayList<Type> getTypeValues(String fieldName){
		ArrayList<Type> typeValuesList = fieldTypes.get(fieldName);
		return typeValuesList;
	}
	
	public ArrayList<BARule> getBARules(){
		return baRulesList;
	}
	
	public ArrayList<WorkflowRule> getWfRules(){
		return wfRulesList;
	}
	
	public ArrayList<Role> getExportRoles(){
		return rolesList;
	}
	
	public Hashtable<String, RolePermission> getRolePermission(String roleName){
		Hashtable<String, RolePermission> rpTable = rolesPermissionsTable.get(roleName);
		return rpTable;
	}
	
	public ArrayList<RoleUser> getRoleUsers(String roleName){
		ArrayList<RoleUser> roleUsersList = rolesUserTable.get(roleName);
		return roleUsersList;
	}
	
	public ArrayList<User> getExportBAUsers(){
		return baUsersList;
	}	
	
	public ArrayList<User> getAllUsers(){
		return allUsersList;
	}
		
	public HashMap<String, String> getBusinessAreaCaptions(){
		return baCaptionsMap;	
	}

	public void getProperties(){
		//TODO get tbits properties
	}

	public void getCaptions(){
		//TODO get tbits captions
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub			

	}
}