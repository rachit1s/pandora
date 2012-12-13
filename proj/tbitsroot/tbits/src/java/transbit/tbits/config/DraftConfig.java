/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/**
 * DraftConfig.java
 *
 * $Header:
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

//Third party imports.
//Xerces Imports.
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Imports from other packages of TBits.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.Helper.TBitsConstants.PKG_CONFIG;

//static imports
import static transbit.tbits.config.XMLParserUtil.getAttributeValue;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.ByteArrayInputStream;
import java.io.Serializable;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the draft config of a request/action.
 *
 * @author  Vinod Gupta.
 * @version $Id: $
 */
public class DraftConfig implements Serializable {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    //~--- methods ------------------------------------------------------------

    /**
     * main method.
     */
    public static void main(String arg[]) throws Exception {
        Hashtable<String, String> test = new Hashtable<String, String>();

        test.put("description", "<<fhgfdgsdf>>djhjh");
        test.put("subject", "subject.....");
        test.put("due_datetime", "12/12/05 23:45");
        System.out.println(DraftConfig.xmlSerialize(1, test));
        System.out.println(DraftConfig.xmlDeSerialize(DraftConfig.xmlSerialize(1, test)));
        System.out.println("Done!");
        return;
    }

    /**
     * This static method returns map <field,value> pairs from
     * given XML.
     *
     *
     * @param aXml XML that represents the DraftConfig in the database.
     * @return Hashtable of <field,value> corresponding to the given XML.
     * @exception TBitsException of any exception during xml-parsing.
     */
    public static Hashtable<String, String> xmlDeSerialize(String aXml) throws TBitsException {
        Hashtable<String, String> fieldValues = new Hashtable<String, String>();

        if ((aXml == null) || (aXml.trim().equals("") == true)) {
            return fieldValues;
        }

        DocumentBuilderFactory factory         = DocumentBuilderFactory.newInstance();
        DocumentBuilder        documentBuilder = null;
        Document               document        = null;

        try {
            documentBuilder = factory.newDocumentBuilder();
            document        = (org.w3c.dom.Document) documentBuilder.parse(new ByteArrayInputStream(aXml.getBytes("UTF-8")));

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
                	
//                	if(optionsNode.hasChildNodes())
//                	{
                		String strValue = optionsNode.getTextContent();// optionsNode.getChildNodes().item(0).toString();
	                    if (strValue != null) {
	                        fieldValues.put(optionsNode.getNodeName(), strValue);
	                    }
//                	}
                }
            }
        } catch (Exception e) {
            LOG.info("Suppressed Draft exception");
            //LOG.info("",(e));

            throw new TBitsException(e.toString());
        }

        return fieldValues;
    }

    /**
     * This method returns the XML-Serialized representation of field values.
     *
     * @param  aSystemId
     * @param  aFieldValues Hashtable of <field,value>
     *
     * @return XML Serialized string representation of this object.
     */
    public static String xmlSerialize(int aSystemId, Hashtable<String, String> aFieldValues) {
        StringBuilder buffer = new StringBuilder();
        Field         field  = null;
        String        name   = "";
        String        value  = "";

        buffer.append("<DraftConfig>");

        Enumeration<String> keys = aFieldValues.keys();

        while (keys.hasMoreElements()) {
            name = keys.nextElement();

            try {
                field = Field.lookupBySystemIdAndFieldName(aSystemId, name);
            } catch (DatabaseException e) {
                LOG.warn("",(e));

                continue;
            }

            if (field == null) {
                continue;
            }

            value = aFieldValues.get(name);

            if ((field.getDataTypeId() == DataType.TEXT) || (field.getDataTypeId() == DataType.STRING) || field.getDataTypeId() == DataType.ATTACHMENTS ) {
                buffer.append("\n<").append(name).append("><![CDATA[").append(value).append("]]></").append(name).append(">");
            }
            else {
                buffer.append("\n<").append(name).append(" value=\"").append(value).append("\" />");
            }
        }

        buffer.append("\n</DraftConfig>");

        return buffer.toString();
    }
}
