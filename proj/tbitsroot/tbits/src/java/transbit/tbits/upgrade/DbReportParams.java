package transbit.tbits.upgrade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.ConnectionProperties;

public class DbReportParams {
	
	private static final String PASSWORD = "password";
	private static final String USER = "user";
	private static final String DRIVER_U_R_L = "driverURL";
	
	public static void main(String[] args) {
//		String folderName = "tbitsreports";
//		String url = "/localhost/CGPL";
//		String user = "sa";
//		String password = "YWJjMTIz";
//		
//		if(args.length < 4){
//			System.err.println("insufficient params");
//			return;
//		}
//		
//		String folderName = args[0];
//		String url = args[1];
//		String user = args[2];
//		String password = args[3];
		
		String folderName 	= "tbitsreports";
		String url 			= ConnectionProperties.getDBPoolProperty(DRIVER_U_R_L);
		String user 		= ConnectionProperties.getDBPoolProperty(USER);
		String password 	= ConnectionProperties.getDBPoolProperty(PASSWORD);
		
		password = new String(Base64.encodeBase64(password.getBytes()));
		
		if(folderName == null || folderName.equals("") ||
				url == null || url.equals("") ||
				user == null || url.equals("") ||
				password == null || password.equals("")){
			System.err.println("inappropriate params");
			return;
		}
		
		File reportsDir = Configuration.findPath(folderName);
		if(reportsDir != null){
			File[] files = reportsDir.listFiles();
			System.out.println("Fixing params of " + files.length + " report files");
			for(File rptFile : files){
				if(rptFile.isFile() && rptFile.getName().substring(rptFile.getName().lastIndexOf(".") + 1).equals("rptdesign"))
					setParamsInAFile(rptFile, url, user, password);
			}
			System.out.println("Fixed report params");
		}
	}
	
	private static void setParamsInAFile(File rptDesignFile, String url, String user, String password){
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			
			DocumentBuilder db = factory.newDocumentBuilder();
			System.out.println("Fixing " + rptDesignFile.getAbsolutePath());
			Document doc = db.parse(rptDesignFile);
			NodeList nodes = doc.getElementsByTagName("oda-data-source");
			
			if(nodes != null){
				Node node = nodes.item(0);
				if(node != null){
					NodeList childNodes = node.getChildNodes();
					for(int i = 0; i < childNodes.getLength(); i++){
						Node childNode = childNodes.item(i);
						if(childNode != null && (childNode.getNodeName().equals("property") || childNode.getNodeName().equals("encrypted-property"))){
							NamedNodeMap map = childNode.getAttributes();
							String propertyName = map.getNamedItem("name").getTextContent();
							if(propertyName != null){
								if(propertyName.trim().equals("odaURL")){
									childNode.setTextContent(url);
								}
								if(propertyName.trim().equals("odaUser")){
									childNode.setTextContent(user);
								}
								if(propertyName.trim().equals("odaPassword")){
									childNode.setTextContent(password);
								}
							}
						}
					}
					
					DOMSource source = new DOMSource(doc);
					Result result = new StreamResult(new FileOutputStream(rptDesignFile));
					
					Transformer xFormer = TransformerFactory.newInstance().newTransformer();
					xFormer.transform(source, result);
				}
			}
		} catch (ParserConfigurationException e) {
			System.err.println("Error in : " + rptDesignFile.getPath());
			e.printStackTrace();
		} catch (SAXException e) {
			System.err.println("Error in : " + rptDesignFile.getPath());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error in : " + rptDesignFile.getPath());
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			System.err.println("Error in : " + rptDesignFile.getPath());
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			System.err.println("Error in : " + rptDesignFile.getPath());
			e.printStackTrace();
		} catch (TransformerException e) {
			System.err.println("Error in : " + rptDesignFile.getPath());
			e.printStackTrace();
		}
	}
}
