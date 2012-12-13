/**
 * 
 */
package transbit.tbits.webapps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.ibm.icu.util.Calendar;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.SysPrefixes;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.Mapper;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
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
import transbit.tbits.exception.TBitsException;
import wox.serial.Easy;
import wox.serial.SimpleWriter;
import wox.serial.XMLUtil;

/**
 * @author Lokesh
 *
 */
public class BusinessAreaExportImportHandler extends HttpServlet {

	/**
	 * Exports the Business Area information: properties, fields, roles, captions,
	 * and optionally BAUsers and their roles. 
	 * Business areas you want to export
	 * Business Area Properties	 
	 * Captions
	 * Fields with Types & Display Groups, descriptors, type_users, Tracking Options, Dependencies
	 * Roles & Permissions
	 * Users(logins plus complete info and no passwords) - Check for login/conflicts
	 * Rules
	 * BA Users (logins not the ids)
	 * Escalation Condition
	 * Escalation Hierarchy 
	 */
	private static final long serialVersionUID = 1L;
	private static final TBitsLogger LOG   = TBitsLogger.getLogger(TBitsConstants.PKG_WEBAPPS);
	/**
	 * 
	 */		
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) 
	throws ServletException, IOException {
		handleRequest(aRequest, aResponse);		
	}
		
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		handleRequest (aRequest, aResponse);				
	}
	
	public void handleRequest (HttpServletRequest aRequest, HttpServletResponse aResponse)throws ServletException, IOException {
		User user = null;		
		HttpSession aSession = aRequest.getSession(true);
		try {
			user = WebUtil.validateUser(aRequest);
			ServletOutputStream out = aResponse.getOutputStream();
			if (RoleUser.isSuperUser(user.getUserId())){				
				String baPrefix = aRequest.getParameter("ba");			
				if ((baPrefix == null) || (baPrefix.trim().equals("")))
					out.println("Invalid business area provided");
	
				String actionType = aRequest.getParameter("actionType");
				if ((actionType == null) || (actionType.trim().equals("")))
					out.println("Choose either to export/import");
	
				if (actionType.equals("export")){
					String contentDisposition = "attachment;fileName= \"" + baPrefix + "-info.xml\"";
					aResponse.setHeader("Content-Disposition", contentDisposition);
					aResponse.setContentType("text/xml");
					String xmlString = getExportXML(aRequest.getParameter("ba"));					
					out.write(xmlString.getBytes("UTF8"));							
					out.flush();
					out.close();
					return;
				}
	
				if (actionType.equals("import")){
					MultipartParser parser = null;
					try {
			            parser = new MultipartParser(aRequest, 1024 * 1024 * 1024);    // 1GB
			        } catch (IOException e) {
			            LOG.severe("",(e));
			            out.println("Unable to read the files.");
			            out.close();
			            return;
			        }
			        Hashtable<String, String> paramTable = new Hashtable<String, String>();
			        if (parser != null) {
			            Part part = null;            
			            FilePart fPart = null;
			            
			            // Iterate the parts in the parser and process them accordingly
			            while ((part = parser.readNextPart()) != null) {
			            	 if (part instanceof ParamPart) {
			                     ParamPart pp         = (ParamPart) part;
			                     String    paramName  = pp.getName();
			                     String    paramValue = pp.getStringValue();
			                     paramTable.put(paramName, paramValue);
			                 }
			            	 String baName = paramTable.get("sysName");
			            	 String sysPrefix = paramTable.get("sysPrefix");
			            	 String baEmail = paramTable.get("sysEmail");
			            	 
			            	/* StringBuffer sb = new StringBuffer();			            	 
			            	 if (BusinessArea.lookupByName(baName) != null)
			            		 sb.append("BA Name: ").append(baEmail).append(" already exists.\n");
			            	 if (BusinessArea.lookupByName(sysPrefix) != null)
			            		 sb.append("System Prefix: ").append(baEmail).append(" already exists.\n");
			            	 if (BusinessArea.lookupByEmail("sysEmail")!= null)
			            		 sb.append("Email id: ").append(baEmail).append(" already exists.\n");
			            	 
			            	 if ((sb.toString() == null) || (sb.toString().trim().equals("")))*/
			            	 
			            	 if (part instanceof FilePart) {
			            		 fPart = (FilePart)part;			            		 
			            		 BusinessAreaExporter baExportInfo = read(fPart.getInputStream(), sysPrefix);  
			            		 importAll(baExportInfo, baName, sysPrefix, baEmail);
			            	 }
			            }     
			            out.println("Finished importing business area \"" + paramTable.get("sysPrefix") + "\"");
			        }
				}
			}
			else{
				throw new TBitsException(Messages.getMessage("ADMIN_NO_PERMISSION"));
			}
		} catch (DatabaseException e) {
			LOG.error(e);
			aSession.setAttribute("ExceptionObject", e);
			aResponse.sendRedirect(WebUtil.getServletPath("/error"));
		} catch (TBitsException e) {LOG.error(e);
			e.printStackTrace();
			aSession.setAttribute("ExceptionObject", e);
			aResponse.sendRedirect(WebUtil.getServletPath("/error"));
		} catch (IOException e) {LOG.error(e);
			aSession.setAttribute("ExceptionObject", e);
			aResponse.sendRedirect(WebUtil.getServletPath("/error"));
		} 
	}
			
	private String getExportXML (String aSysPrefix) throws TBitsException{
		/*String filePath =  Configuration.findAbsolutePath(PropertiesHandler.getProperty(
				transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR)) + "\\" + systemPrefix + "-info.xml";*/
		BusinessAreaExporter exportInfo = new BusinessAreaExporter();				
		exportInfo.initializeValues(aSysPrefix);					
		//Easy.save(exportInfo, filePath);	
		SimpleWriter writer = new SimpleWriter();
		org.jdom.Element jdom = writer.write(exportInfo);
		
		try {
			return (XMLUtil.element2String(jdom));
		} catch (Exception e) {
			e.printStackTrace();
			throw new TBitsException(e);
		}
	}	
	
	public static String xmlFile2String(String filePath)
	{
		try{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();			
			Document document = documentBuilderFactory.newDocumentBuilder().parse(filePath);
			StringWriter sw = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.transform(new DOMSource(document), new StreamResult(sw));
			return sw.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param args
	 */
	private static BusinessAreaExporter read (InputStream inputStream, String baPrefix){	
		String filePath = APIUtil.getTMPDir() + "/" + baPrefix + "-info.xml";
		File xmlFile = new File (filePath);
		try {
			byte[] buf = new byte[1024];
	        int len;
			FileOutputStream fileOutputStream = new FileOutputStream(xmlFile);
			OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);			
	        while ((len = inputStream.read(buf)) > 0) {
	            fileOutputStream.write(buf, 0, len);
	        }
	        inputStream.close();
	        writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BusinessAreaExporter rObj = (BusinessAreaExporter)Easy.load(xmlFile.getAbsolutePath());
		return rObj;
	}
	
	private static BusinessAreaExporter read (String filename){
		BusinessAreaExporter rObj = (BusinessAreaExporter)Easy.load(filename);
		return rObj;
	}
	
	/**
	 * @param args
	 * 
	*/
	private static int importBA (BusinessAreaExporter baExporter, String aBAName, String sysPrefix, String aBAEmail){
		int newSysId = -1;
		BusinessArea tempBA = baExporter.getExportBA();		
		//String tempSysPrefix = tempBA.getSystemPrefix();		
		try {
			if (BusinessArea.lookupBySystemPrefix(sysPrefix) == null){				
				tempBA.setSystemPrefix(sysPrefix);
				tempBA.setName(aBAName);
				tempBA.setEmail(aBAEmail);
				tempBA.setMaxRequestId(0);

				tempBA.setDateCreated(new Timestamp( Calendar.getInstance().getTimeInMillis()));
				
				newSysId = BusinessArea.insert(tempBA);
//				Mapper.refreshBOMapper();
			}
		} catch (DatabaseException dbe) {
			LOG.error(dbe);
		}
		return newSysId;
	}
	
	/**
	 * @param args
	 */
	private static void importBARules (BusinessAreaExporter baExportInfo, int aSystemId){
		ArrayList<BARule> baRules = baExportInfo.getBARules();
		ArrayList<WorkflowRule> workflowRules = baExportInfo.getWfRules();
		WorkflowRule wfRule = null;
		int wrId = -1;
		for (BARule baRule : baRules){
			try {
				wfRule = getWfRule(workflowRules, baRule.getRuleId());
				if (wfRule == null)
					return;
				wrId = WorkflowRule.insert(wfRule);
				if (wrId == -1)
					System.out.println("Invalid workflow id : " + wrId + ". Rule name: " + wfRule.getName());
				else{					
					baRule.setSystemId(aSystemId);
					baRule.setRuleId(wrId);
					BARule.insert(baRule);
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}	
	
	private static WorkflowRule getWfRule(ArrayList<WorkflowRule> wfRules, int ruleId){
		for (WorkflowRule wfRule : wfRules){
			if(wfRule != null)
				if (wfRule.getRuleId() == ruleId){
					return wfRule;
				}
		}
		return null;
	}
	
	/**
	 * @param args
	 */
	private static void importBAUsers (BusinessAreaExporter baExportInfo, int aSystemId){		
		try{
			ArrayList<User> baUserList = baExportInfo.getExportBAUsers();
			for (User usr : baUserList){
				if (User.doesUserAlreadyExist(usr.getUserLogin())){	
					BAUser.insert(new BAUser (aSystemId, usr.getUserId(), true));
				}
				else
					System.out.println("Skipped BAUser: " + usr.getUserLogin() + ". User not found in the user list. Please import all users and then update BAUsers");
//				Mapper.refreshBOMapper();
			}
		}catch(DatabaseException dbe){
			LOG.warn(dbe);
		}
	}
	
	/**
	 * @param args
	 */
	private static void importBAMailAccounts(ArrayList<BAMailAccount> aBAMailAccountsList, String aSysPrefix){
		if (aBAMailAccountsList == null)
			return;
		for (BAMailAccount baMailAcc : aBAMailAccountsList){
			baMailAcc.setMyBAPrefix(aSysPrefix);
			try {
				baMailAcc.SaveToDB();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	private static void importDisplayGroups (int aSystemId, ArrayList<DisplayGroup> displayGrpList) throws DatabaseException{
		for (DisplayGroup dGroup: displayGrpList){
			if (dGroup == null)
				continue;
			else{
				DisplayGroup tempDGroup = new DisplayGroup(aSystemId, dGroup.getDisplayName(), dGroup.getDisplayOrder(), dGroup.getIsActive(),dGroup.getIsDefault());	
				DisplayGroup.insert(tempDGroup);
				tempDGroup = null;
			}
		}
	}

	/**
	 * @param args
	 */
	private static void importRolesAndRoleUsers(BusinessAreaExporter baExportInfo, int aSystemId){
		ArrayList<Role> rolesList = baExportInfo.getExportRoles();
		for(Role role : rolesList){
			role.setSystemId(aSystemId);
			try {
				Role.insertExistingRole(role);				
			} catch (DatabaseException dbe) {
				LOG.error(dbe);
			}
			String roleName = role.getRoleName();
			//Export RoleUsers
			ArrayList <RoleUser> roleUsersList = baExportInfo.getRoleUsers(roleName);
			if (roleUsersList != null){			
				for (RoleUser roleUser : roleUsersList){
					roleUser.setSystemId(aSystemId);
					RoleUser.update(roleUser);
				}
			}
		}		
	}
	
	private static void importRolePermissions(
			BusinessAreaExporter baExportInfo, int aSystemId) {
		ArrayList<Role> rolesList = baExportInfo.getExportRoles();
		for (Role role : rolesList) {
			String roleName = role.getRoleName();
			// Export RolePermissions
			Hashtable<String, RolePermission> rolePermissionTable = baExportInfo
					.getRolePermission(roleName);
			Enumeration<RolePermission> rpEnum = rolePermissionTable.elements();
			while (rpEnum.hasMoreElements()) {
				RolePermission rp = rpEnum.nextElement();
				rp.setSystemId(aSystemId);
				try {
					RolePermission.update(rp);
				} catch (DatabaseException dbe) {
					LOG.error(dbe);
				}
			}
		}
	}
	/**
	 * @param args
	 */
	private static void importFieldsWithTypesAndDescriptors (BusinessAreaExporter baExportInfo, int aSystemId){		
		ArrayList<Field> fixedFieldsList = baExportInfo.getExportFixedFields();
		addFieldsWithDescriptorsAndTypes(baExportInfo, fixedFieldsList, aSystemId);
		ArrayList<Field> extendedFieldsList = baExportInfo.getExportExtendedFields();		
		addFieldsWithDescriptorsAndTypes(baExportInfo, extendedFieldsList, aSystemId);
	}
	
	/**
	 * @param args
	 */
	private static void addFieldsWithDescriptorsAndTypes(BusinessAreaExporter baExportInfo, ArrayList<Field> fieldsList, int aSystemId){
		String fieldName;
		String tmpString;
		DisplayGroup dg;	
		ArrayList<DisplayGroup> prevDGList = baExportInfo.getDisplayGroups(); 
		
		for(Field field : fieldsList){
			fieldName = field.getName();
			field.setSystemId(aSystemId);				
			try {
				//Check if the display group of fields are mapped properly with imported/existing display group
				int displayGroupId = field.getDisplayGroup();
				if (displayGroupId > 1){
					tmpString = getDisplayGroupName(prevDGList, displayGroupId);
					dg = DisplayGroup.lookupBySystemIdAndDisplayName(aSystemId, tmpString);
					if (dg != null)
						field.setDisplayGroup(dg.getId());
				}
				
				Field.insertWithExistingFieldId(field);				
				
				//Add corresponding field descriptors and type values
				importFieldDescriptors(aSystemId, baExportInfo.getFieldDescriptorList(fieldName));	
				importTypeValues (aSystemId, baExportInfo.getTypeValues(fieldName));
				tmpString = null;
				dg = null;
			} catch (DatabaseException dbe) {
				LOG.error(dbe);
			}
		}	
//		Mapper.refreshBOMapper();
	}	
	
	/**
	 * @param args
	 */
	private static String getDisplayGroupName (ArrayList<DisplayGroup>dgList, int aDisplayGrpId){
		for (DisplayGroup dg : dgList){
			if (dg.getId() == aDisplayGrpId){
				return dg.getDisplayName();
			}
		}
		return "";
	}		
	
	/**
	 * @param args
	 */
	private static void importFieldDescriptors(int aSystemId, ArrayList<FieldDescriptor> fDescList){
		for (FieldDescriptor fd : fDescList){
			if (fd == null)
				return;
			else{
				fd.setSystemId(aSystemId);
				FieldDescriptor.insert(fd);
			}
		}
//		Mapper.refreshBOMapper();
	}
	
	/**
	 * @param args
	 */
	private static void importTypeValues (int aSystemId, ArrayList<Type> typeValues){
		if (typeValues == null)
			return;
		for (Type type : typeValues){
			type.setSystemId(aSystemId);
			Type.insert(type);
		}
//		Mapper.refreshBOMapper();
	}
	
	/**
	 * @param args
	 */
	private static void importBACaptions(BusinessAreaExporter baExportInfo, int newSysId) {
		HashMap<String, String> baCaptionsMap = baExportInfo.getBusinessAreaCaptions();
		if (baCaptionsMap == null)
			return;
		else{
			Set<String> captionSet = baCaptionsMap.keySet();
			for (String caption : captionSet){
				String value = baCaptionsMap.get(caption);
				try {
					CaptionsProps.insert(newSysId, caption, value);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	private static void importAll(BusinessAreaExporter baExporter,String aBAName, String aSysPrefix, String aBAEmail) throws DatabaseException{		
		int newSysId = importBA(baExporter, aBAName, aSysPrefix, aBAEmail);		
		if (newSysId > 0){
			importBAMailAccounts(baExporter.getBAMailAccounts(), aSysPrefix);
			
			importDisplayGroups(newSysId, baExporter.getDisplayGroups());
			importRolesAndRoleUsers(baExporter, newSysId);
			importBAUsers(baExporter, newSysId);
			// Since field addition also adds the default permissions wrt role so the field addition should be 
			// done after the creation of roles.
			importFieldsWithTypesAndDescriptors(baExporter, newSysId);	
			importRolePermissions(baExporter, newSysId);
			
			importBARules (baExporter, newSysId);
			importBACaptions (baExporter,newSysId);
			refreshEntries();
		}
	}		
	
	/**
	 * @param args
	 */
	private static void refreshEntries(){	
        SysPrefixes.reload(); 
        Mapper.refreshBOMapper();
		Mapper.refreshUserMapper();
        PropertiesHandler.reload();
        CaptionsProps.reloadCaptions();
        BAMailAccount.refreshAccounts();
	}
	
	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException {
		//ExportImportInfo.write("Design", "E:/temp/exportBA2.xml");
		BusinessAreaExporter expInfo = BusinessAreaExportImportHandler.read("/Users/sandeepgiri/dls/KDI_LNT-info.xml");		
		BusinessAreaExportImportHandler.importAll(expInfo,"kdicopy11", "kdicopy11", "tmom@localhost");
	}
}
