package transbit.tbits.webapps;

import java.io.OutputStream;
//import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
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

import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.BAUser;
import transbit.tbits.domain.Type;

public class OutlookConfig extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*private OutlookConfig() {
		System.out.println("Creating tBits BA XML Document...");
	}

	public static OutlookConfig getInstance(Document doc) {
		if (instance == null)
			instance = new OutlookConfig();
		return instance;
	}
	
	private static OutlookConfig instance;*/

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
			
			Element tBitsLink = doc.createElement("tBits_link");
			root.appendChild(tBitsLink);
			
			src = new DOMSource(doc);
			
		} catch (DOMException e) {
			System.out.println("Exception while generating xml doc");			
		} 
		return src;		
	}
	
	private Element appendUsers(Document doc)
	{		
		Element usersElement= doc.createElement("users");		
				
		try {
			ArrayList<User> userList = User.getAllUsers();	
			
			ListIterator<User> usrListIterator = userList.listIterator();

			while (usrListIterator.hasNext()) {
				User temp = usrListIterator.next();
				Element usrElement = doc.createElement("user");
				usrElement.setAttribute("id", Integer.toString(temp.getUserId()));

				Element login = doc.createElement("login");
				login.setTextContent(temp.getUserLogin());
				usrElement.appendChild(login);

				Element fName = doc.createElement("first_name");
				fName.setTextContent(temp.getFirstName());
				usrElement.appendChild(fName);

				Element lName = doc.createElement("last_name");
				lName.setTextContent(temp.getLastName());
				usrElement.appendChild(lName);
				
				Element email = doc.createElement("email_id");				
				email.setTextContent(temp.getEmail());
				usrElement.appendChild(email);				
				
				Element defBA = doc.createElement("default_BA");
				defBA.setTextContent(temp.getWebConfigObject().getSystemPrefix());
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
			ArrayList<BusinessArea> baList = BusinessArea.getActiveBusinessAreas();			
		
			ListIterator<BusinessArea> baListIterator = baList.listIterator();
			
			while (baListIterator.hasNext())
			{
				BusinessArea tempBA = baListIterator.next();				
				Element ba = doc.createElement("business_area");
				
				ba.setAttribute("display_name", tempBA.getDisplayName());
				ba.setAttribute("id", String.valueOf(tempBA.getSystemId()));
				ba.setAttribute("name", tempBA.getName());				
				ba.setAttribute("is_private", String.valueOf(tempBA.getIsPrivate()));
				
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
				ba.appendChild (baUsers);				
				
				Element assignees = doc.createElement("assignees");		
				ArrayList<Type> assigneeList= 
					Type.lookupAllBySystemIdAndFieldName(tempBA.getSystemId(), "assignee_ids");
				ListIterator<Type> assigneeIterator=assigneeList.listIterator();
				String assigneeStr ="";
				while(assigneeIterator.hasNext())
				{
					Type assigneeType = assigneeIterator.next();
					System.out.println("assignee: "+assigneeType.getDisplayName());
					assigneeStr= assigneeStr+assigneeType.getDisplayName();					
					if(assigneeIterator.hasNext())
						assigneeStr=assigneeStr+",";					
				}							
				assignees.setTextContent(assigneeStr);
				ba.appendChild(assignees);
				
				Element email = doc.createElement("email");
				email.setTextContent(tempBA.getEmail());
				ba.appendChild(email);
				
				Element category = doc.createElement("category");
				ArrayList<Type> categoryList= 
					Type.lookupAllBySystemIdAndFieldName(tempBA.getSystemId(), "category_id");
				ListIterator<Type> catIterator=categoryList.listIterator();
				String catStr ="";
				while(catIterator.hasNext())
				{
					Type catType = catIterator.next();					
					catStr= catStr+catType.getDisplayName();					
					if(catIterator.hasNext())
						catStr=catStr+",";					
				}
				category.setTextContent(catStr);
				ba.appendChild(category);
				
				Element status = doc.createElement("status");
				ArrayList<Type> statusList= 
					Type.lookupAllBySystemIdAndFieldName(tempBA.getSystemId(), "status_id");
				ListIterator<Type> stIter=statusList.listIterator();
				String statusStr ="";
				while(stIter.hasNext())
				{
					Type type = stIter.next();
					statusStr= statusStr+type.getName();					
					if(stIter.hasNext())
						statusStr=statusStr+",";					
				}				
				status.setTextContent(statusStr);
				ba.appendChild(status);
						
				if (Type.lookupAllBySystemIdAndFieldName(tempBA.getSystemId(), "expensecategory") != null){
					Element expenseCategory = doc.createElement("expenseCategory");
					ArrayList<Type> expCatList= 
						Type.lookupAllBySystemIdAndFieldName(tempBA.getSystemId(), "expensecategory");
					ListIterator<Type> expIter=expCatList.listIterator();
					String expenseStr = "";
					while(expIter.hasNext())
					{
						Type type = expIter.next();
						expenseStr= expenseStr+type.getName();					
						if(expIter.hasNext())
							expenseStr=expenseStr+",";					
					}			
					expenseCategory.setTextContent(expenseStr);
					ba.appendChild(expenseCategory);					
				}
				
				Element prefix = doc.createElement("prefix");
				prefix.setTextContent(tempBA.getSystemPrefix());
				ba.appendChild(prefix);			
				
				bas.appendChild(ba);
				}
			}catch (Exception e){
				System.out.println("Exception while appending business areas");
		}	
		return bas;
	}
	
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) 
	throws ServletException, IOException {
		try {
			System.out.println("Instantiating config");
			
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
			
			//PrintWriter out = aResponse.getWriter();
			
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
		} /*catch(ServletException servletExp){
			System.out.println ("Stack trace:\n"+servletExp.getStackTrace()+ "\nMessage:\n"+servletExp.getMessage());
		} */		
		System.out.println("Done...");
		}
	
	
	 public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
	        doGet(aRequest, aResponse);
	    }	
	 
	 public static void main(String[] args) {
		/*try {
			//Create instance of DocumentBuilderFactory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//Get the DocumentBuilder
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			//Create DOM Document
			Document doc = docBuilder.newDocument();
			OutlookConfigXml testXML = OutlookConfigXml.getTestXmlCreationInstance(doc);
			testXML.generateDoc(doc);			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Done...");*/
		}
	}
