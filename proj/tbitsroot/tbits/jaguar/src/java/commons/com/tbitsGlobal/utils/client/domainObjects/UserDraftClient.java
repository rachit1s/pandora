package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.Date;
import java.util.HashMap;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

//pojo for UserDraft
public class UserDraftClient extends TbitsModelData {
	
	private TbitsTreeRequestData model;

	// default constructor
	public UserDraftClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String DRAFT = "draft";
	public static String REQUEST_ID = "request_id";
	public static String SYSTEM_ID = "system_id";
	public static String DATE = "Date";
	public static String DRAFT_ID = "draft_id";
	public static String USER_ID = "user_id";

	// getter and setter methods for variable myDraft
	public String getDraft() {
		return (String) this.get(DRAFT);
	}

	public void setDraft(String myDraft) {
		this.set(DRAFT, myDraft);
	}

	// getter and setter methods for variable myRequestId
	public int getRequestId() {
		return (Integer) this.get(REQUEST_ID);
	}

	public void setRequestId(int myRequestId) {
		this.set(REQUEST_ID, myRequestId);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable myDate
	public Date getDate (){
		return (Date) this.get(DATE);
	}
	public void setDate(Date myDate) {
		this.set(DATE, myDate);
	}

	// getter and setter methods for variable myDraftId
	public int getDraftId() {
		return (Integer) this.get(DRAFT_ID);
	}

	public void setDraftId(int myDraftId) {
		this.set(DRAFT_ID, myDraftId);
	}

	// getter and setter methods for variable myUserId
	public int getUserId() {
		return (Integer) this.get(USER_ID);
	}

	public void setUserId(int myUserId) {
		this.set(USER_ID, myUserId);
	}
	
	public TbitsTreeRequestData xmlDeserialize(){
		HashMap<String, String> fieldValues = new HashMap<String, String>();
		
		Document document = XMLParser.parse(this.getDraft());
		
		NodeList rootNodeList    = document.getElementsByTagName("DraftConfig");
        Node     rootNode        = rootNodeList.item(0);
        NodeList optionsNodeList = rootNode.getChildNodes();

        for (int i = 0; i < optionsNodeList.getLength(); i++) {
            Node optionsNode = optionsNodeList.item(i);

            if (optionsNode.getNodeName().equals("#text") == true) {
                continue;
            }

            if (optionsNode.hasAttributes() == true) {
                String strValue = getAttributeValue(optionsNode, "value");

                if (strValue != null) {
                    fieldValues.put(optionsNode.getNodeName(), strValue);
                }
            } else {
        		String strValue = optionsNode.getNodeValue();//TextContent();// optionsNode.getChildNodes().item(0).toString();
                if (strValue != null) {
                    fieldValues.put(optionsNode.getNodeName(), strValue);
                }
            }
        }
        
        
        
        return null;
	}
	
	public String getAttributeValue(Node node, String attrName) {
        String attrValue = "";

        if (node == null) {
            return attrValue;
        }

        NamedNodeMap nnmap = node.getAttributes();

        /*
         * Check if there are any attributes to this node.
         */
        if (nnmap == null) {
            return attrValue;
        }

        Node attrNode = nnmap.getNamedItem(attrName);

        if (attrNode != null) {
            attrValue = attrNode.getNodeValue();
            attrValue = (attrValue == null)
                        ? ""
                        : attrValue.trim();
        }

        return attrValue;
    }

	public void setModel(TbitsTreeRequestData model) {
		this.model = model;
	}

	public TbitsTreeRequestData getModel() {
		return model;
	}

}