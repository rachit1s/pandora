package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BAUser;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeDescriptor;
import transbit.tbits.domain.User;

public class OLPluginXML extends HttpServlet {

	/**
	 * Purpose is to provide only the relevant data for user whose information is provided.
	 * 1. Spit out the relevant business areas he is in
	 * 		Attributes  : Name, id, display name, id
	 *      Child nodes: fields 
	 * 		Note: If the business area is_private, has to check if the user has relevant permissions. 
	 * 2. Spit out all the relevant fields based on the field list from the user as the child nodes of a BA.
	 * 		- If field is of data type, "type", then check if any of the type value is_private, 
	 * 		  if so check for permissions
	 * 3. User info name, display name, email-id, id, default_ba   
	 */
	private static final long serialVersionUID = 1L;
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	private String username;
	//private String password;
	private User currentUser;
	private ArrayList <DataType> dataTypeList;
	private String fixedFields;
	private String extendedFields;
	
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) 
	throws ServletException, IOException {
		handleRequest (aRequest, aResponse);
	}
		
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		handleRequest (aRequest, aResponse);
	}	
	
	public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		try {			
			username = aRequest.getParameter("usr");
			username = username.trim();
						
			fixedFields = aRequest.getParameter("fixedFields");
			if ((fixedFields == null) || fixedFields.trim().equals("") || fixedFields.trim().equals("none"))
				fixedFields = "";
			else
				fixedFields = fixedFields.trim();
						
			extendedFields = aRequest.getParameter("extendedFields");
			if ((extendedFields == null) || extendedFields.trim().equals("") || extendedFields.trim().equals("none"))
				extendedFields = "";
			else
				extendedFields = extendedFields.trim();
					
			//Create instance of DocumentBuilderFactory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//Get the DocumentBuilder
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			//Create DOM Document
			Document doc = docBuilder.newDocument();

			//OutlookConfig baXML = OutlookConfig.getInstance(doc);
			Source src = generateDoc(doc);			

			System.out.println("Before spitting out the output");
			aResponse.setContentType("text/xml");
			OutputStream out = aResponse.getOutputStream();
			Result dest = new StreamResult(out);

			TransformerFactory transFactory = TransformerFactory.newInstance();
			transFactory.setAttribute("indent-number", new Integer(4));
			Transformer aTransformer = transFactory.newTransformer();
			aTransformer.setOutputProperty("indent", "yes");
			aTransformer.transform(src, dest);		
		}catch (IOException e) {			
			System.out.println("IO exception");
		} catch (TransformerConfigurationException e) {			
			System.out.println("transConfig exception");
		} catch (TransformerFactoryConfigurationError e) {			
			System.out.println("transFacConfig exception");
		} catch (TransformerException e) {
			System.out.println("transformer Exception");
		} catch (Exception e) {
			System.out.println("Generic Exception");
		} 
		System.out.println("Done...");
	}

	protected Source generateDoc(Document doc) {
		Source src = null;
		try {
			//create the root element			
			Element root = doc.createElement("tbitsconf");
			//add it to the XML tree
			doc.appendChild(root);	

			Element usersElement = appendUsers(doc);
			root.appendChild(usersElement);	
			
			Element bas = appendBAS(doc);
			root.appendChild(bas);		
						
			src = new DOMSource(doc);
			
		} catch (DOMException e) {
			System.out.println("Exception while generating xml doc");			
		} 
		return src;		
	}
	
	private void setCurrentUser(){
		ArrayList<User> userList = User.getAllUsers();			
		ListIterator<User> usrListIterator = userList.listIterator();

		while (usrListIterator.hasNext()) {
			User temp = usrListIterator.next(); 
			if ((temp.getUserLogin().equals(username)) || (temp.getEmail().equals(username))){	
				//Sets the current user
				this.currentUser = temp;
			}
		}
	}
	
	private Element appendUsers(Document doc)
	{		
		Element usersElement= doc.createElement("users");			
		setCurrentUser();
		
		try {
			if ((currentUser != null) && (currentUser.getIsActive())){

				Element usrElement = doc.createElement("user");
				usrElement.setAttribute("id", Integer.toString(currentUser.getUserId()));

				Element login = doc.createElement("login");
				login.setTextContent(currentUser.getUserLogin());
				usrElement.appendChild(login);

				Element fName = doc.createElement("first_name");
				fName.setTextContent(currentUser.getFirstName());
				usrElement.appendChild(fName);

				Element lName = doc.createElement("last_name");
				lName.setTextContent(currentUser.getLastName());
				usrElement.appendChild(lName);

				Element email = doc.createElement("email_id");				
				email.setTextContent(currentUser.getEmail());
				usrElement.appendChild(email);				

				Element defBA = doc.createElement("default_BA");
				defBA.setTextContent(currentUser.getWebConfigObject().getSystemPrefix());
				usrElement.appendChild(defBA);	
				
				usersElement.appendChild(usrElement);
			}

		}catch (Exception e){
			System.out.println("Exception while appending users");
		}	
		return usersElement;
	}		
	
	private Element appendBAS(Document doc)
	{
		Element bas = doc.createElement("bas");	
		try {	
			String[] fieldNamesList = null;
			dataTypeList = DataType.getAllDataTypes();
			ArrayList<BusinessArea> baList = BusinessArea.getActiveBusinessAreas();			
			ListIterator<BusinessArea> baListIterator = baList.listIterator();

			while (baListIterator.hasNext())
			{
				BusinessArea tempBA = baListIterator.next();
				int systemId = tempBA.getSystemId();

				Element baElement = doc.createElement("business_area");				
				baElement.setAttribute("display_name", tempBA.getDisplayName());
				baElement.setAttribute("id", String.valueOf(tempBA.getSystemId()));
				baElement.setAttribute("name", tempBA.getName());				
				baElement.setAttribute("is_private", String.valueOf(tempBA.getIsPrivate()));
				baElement.setAttribute("prefix", tempBA.getSystemPrefix());				

				Element fieldsElement = doc.createElement ("fields");
				if (!fixedFields.equals(""))
					fieldNamesList = fixedFields.split(",");	
								
				//Insert all the fixed fields
				ArrayList <Field> fixedFieldList = Field.getFixedFieldsBySystemId(systemId);				
				insertFields (doc, tempBA, fieldsElement, fixedFieldList, dataTypeList, fieldNamesList);	
				fieldNamesList = null;
				
				if (!extendedFields.equals(""))
					fieldNamesList = extendedFields.split(",");				
				
				//Insert all the extended fields
				ArrayList<Field> extendedFieldList = Field.getExtendedFieldsBySystemId(systemId);
				insertFields (doc, tempBA, fieldsElement, extendedFieldList, dataTypeList, fieldNamesList);
				
				baElement.appendChild(fieldsElement);

				Element baUsers = doc.createElement ("bausers");
				ArrayList<User> baUsrList = BAUser.getBusinessAreaUsers(tempBA.getSystemId());
				ListIterator<User> usrIterator = baUsrList.listIterator();
				String userStr ="";
				while(usrIterator.hasNext()){					
					User tmpUser = usrIterator.next();
					userStr= userStr+tmpUser.getUserId();					
					if(usrIterator.hasNext())
						userStr=userStr+",";
				}
				baUsers.setTextContent(userStr);
				baElement.appendChild (baUsers);				

				Element assignees = doc.createElement("assignees");		
				ArrayList<Type> assigneeList= 
					Type.lookupAllBySystemIdAndFieldName(tempBA.getSystemId(), "assignee_ids");
				ListIterator<Type> assigneeIterator=assigneeList.listIterator();
				String assigneeStr ="";
				while(assigneeIterator.hasNext()){
					Type assigneeType = assigneeIterator.next();
					System.out.println("assignee: "+assigneeType.getDisplayName());
					assigneeStr= assigneeStr+assigneeType.getDisplayName();					
					if(assigneeIterator.hasNext())
						assigneeStr=assigneeStr+",";					
				}							
				assignees.setTextContent(assigneeStr);
				baElement.appendChild(assignees);

				Element email = doc.createElement("email");
				email.setTextContent(tempBA.getEmail());
				baElement.appendChild(email);
				
				bas.appendChild(baElement);
			}
		}
		catch (Exception e){
			System.out.println("Exception while appending business areas" + 
					"\n Message:\n" + e.getMessage() + "\n StackTrace: \n" );
			e.printStackTrace();
		}
		return bas;
	}
	
	private void insertFields (Document doc, BusinessArea ba, Element parentNode, ArrayList <Field> fieldList, ArrayList<DataType> dataTypeList, String[] fieldNamesList){		
		ListIterator <Field> fieldListIterator = fieldList.listIterator();
		FieldDescriptor fd = null;
		while (fieldListIterator.hasNext()){	
			Field curField = fieldListIterator.next();	
			try {
				fd = FieldDescriptor.getPrimaryDescriptor(ba.getSystemId(), curField.getName());
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (curField.getIsActive()){				
				if ((fieldNamesList.length == 1) && (fieldNamesList[0].equals("all"))){				
					insertFieldElement(doc, ba, parentNode, curField, fd, dataTypeList);					
				}
				else{					
					for (String fieldName : fieldNamesList){						
						if ((!(fieldName.equals("")) && fieldName.equals(curField.getName())))
						{								
							insertFieldElement(doc, ba, parentNode, curField, fd, dataTypeList);							
							break;
						}
					}
				}	
			}						
		}			
	}
	
	private void insertFieldElement (Document doc, BusinessArea ba, Element parentNode, Field field, FieldDescriptor fd , ArrayList<DataType> dataTypeList){
		int systemId = ba.getSystemId(); 
		String fieldName = field.getName();
		Type tmpType; 
		String typeName;
		TypeDescriptor td;
		try{
			Element fieldElem = doc.createElement("field");				
			String tempStr = getDataType(field.getDataTypeId(), dataTypeList);
			fieldElem.setAttribute ("field_datatype", tempStr);
			fieldElem.setAttribute ("id", String.valueOf(field.getFieldId()));
			fieldElem.setAttribute ("name", field.getName());
			fieldElem.setAttribute ("display_name", field.getDisplayName());
			fieldElem.setAttribute ("is_extended", field.getIsExtended() + "");
			fieldElem.setAttribute ("is_Private", field.getIsPrivate() + "");
			fieldElem.setAttribute ("field_descriptor", fd.getDescriptor());
			
			if (tempStr.equals("type")){				
				ArrayList <Type> fieldValueList = Type.lookupAllBySystemIdAndFieldName(systemId, fieldName);
				ListIterator <Type> typeIter = fieldValueList.listIterator();				
				
				Element typesElem = doc.createElement("types");
				while (typeIter.hasNext()){
					tmpType = typeIter.next();
					if (tmpType.getIsActive()){
						typeName = tmpType.getName();
						td = TypeDescriptor.getPrimaryDescriptor (systemId, fieldName, typeName);
						
						Element typeElem = doc.createElement ("type");
						typeElem.setAttribute ("name", tmpType.getName());
						typeElem.setAttribute ("display_name", tmpType.getDisplayName());
						typeElem.setAttribute ("id", tmpType.getTypeId() + "");	
						typeElem.setAttribute ("is_private", tmpType.getIsPrivate() + "");
						typeElem.setAttribute ("is_defualt", tmpType.getIsDefault() + "");
						typeElem.setAttribute("type_descriptor", td.getDescriptor());
						
						typesElem.appendChild(typeElem);
					}
				}
				fieldElem.appendChild(typesElem);						 
			} 
			parentNode.appendChild(fieldElem);		
		}catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	 }
	
	private void getBAList (){
		try {
			System.out.println("Member Of : \n" + currentUser.getMemberOf());
			ArrayList <String> baList = BusinessArea.getUserBAList(currentUser.getUserId(), currentUser.getMemberOf(), false);
			System.out.println("ba list\n" + baList.toString());
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}	
		 	 
	private String getDataType (int dataTypeId, ArrayList<DataType> dataTypeList){
		/**
		 * Gets the names list of field data types given the list of data type id list 
		 */
		String dataType = "";
		Iterator<DataType> dataTypeIterator = dataTypeList.listIterator();
		while (dataTypeIterator.hasNext()){			
			DataType tmpDataType = dataTypeIterator.next();
			if (tmpDataType.getDataTypeId() == dataTypeId){
				dataType = tmpDataType.getDataType();
				break;
			}
		}			 
		return dataType;
	}
	 
	public static void main(String[] args) {

		OLPluginXML test = new OLPluginXML ();	
		test.username = "lisa.shah";
		test.setCurrentUser();		 

		try {
			//Create instance of DocumentBuilderFactory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//Get the DocumentBuilder
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			//Create DOM Document
			Document doc = docBuilder.newDocument();
			Source src = test.generateDoc(doc);
			src.toString();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Done and out...");
	}	
}


