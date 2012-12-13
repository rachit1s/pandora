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
 * Messages.java
 *
 * $Header:
 *
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

//Java XML DOM Imports
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//TBits Imports.
import transbit.tbits.common.Configuration;
import transbit.tbits.common.TBitsLogger;

//Static Imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.File;
import java.io.IOException;

import java.text.MessageFormat;

import java.util.Enumeration;
import java.util.Hashtable;

//Javax Imports
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//~--- classes ----------------------------------------------------------------

/**
 * The utility class to replace any placeholders in the messages and
 * return them when the key is specified.
 *
 * @author  Vaibhav
 * @version $Id: $
 *
 */
public class Messages {
    public static final TBitsLogger          LOG           = TBitsLogger.getLogger(PKG_UTIL);
    private static Hashtable<String, String> ourMessages   = new Hashtable<String, String>();
    private static String                    ourFilePath   = "";
    private static final String              MESSAGES_FILE = "etc/messages.xml";

    //~--- static initializers ------------------------------------------------

    static {
        try {
            File file = Configuration.findPath(MESSAGES_FILE);

            if (file != null) {
                ourFilePath = file.toString();
                loadMessages(file.toString());
            }
        } catch (Exception e) {
            LOG.severe("Messages file is not found.");
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Gets the Messages as an Iterator
     *
     * @return Enumeration of the messages.
     */
    public static Enumeration Values() {
        return ourMessages.elements();
    }

    /**
     * Loads all the messages in memory.
     *
     * @param filePath String - The file path of the Messages.xml file.
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    private static void loadMessages(String filePath) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder docBuilder = null;

            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document xmlDoc   = docBuilder.parse(filePath);
            Element  root     = xmlDoc.getDocumentElement();
            NodeList nodeList = root.getElementsByTagName("Message");

            if (nodeList != null) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);

                    if (node.getNodeName().equals("Message")) {
                        NamedNodeMap nodeMap = node.getAttributes();
                        String       key     = nodeMap.getNamedItem("key").getNodeValue();

                        if ((key != null) && node.hasChildNodes()) {
                            if (node.getChildNodes().getLength() > 1) {
                                NodeList childNodes = node.getChildNodes();
                                int      chLen      = childNodes.getLength();

                                for (int j = 0; j < chLen; j++) {
                                    Node n = childNodes.item(j);
                                      if (n.getNodeName().equals("Value")) {
                                    	  if(n.hasChildNodes())
                                    	  {
	                                        String value = n.getTextContent();//ChildNodes().item(0).toString();
	                                        if (value != null) {
	                                            ourMessages.put(key, value.trim());
	                                        }
                                    	  }
                                    }
                                }
                            }
                        }
                    }
                }
            } 
            else {}        
    }

    /**
     * Main method to test the Messages utility functions.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            String      key = "Argument_Null_Exception";
            Enumeration e   = Messages.Values();
            int         i   = 1;

            while (e.hasMoreElements()) {
                //LOG.info(i++ + ". " + e.nextElement().toString());
                System.out.println(i++ + ". " + e.nextElement().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloads the messages file.
     *
     */
    public static void reload() {
    		try {
				loadMessages(ourFilePath);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOG.error(e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOG.error(e);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOG.error(e);
			}
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Gets a message for the given message code.
     *
     * @param key String The message code for which the message to be returned.
     * @return String Gets a message for the given message key and uses it to
     *         format the given arguments
     */
    public static String getMessage(String key) {
        return ourMessages.get(key).toString();
    }

    /**
     *
     * @param key String Message key.
     * @param value Object The value to be replaced in the message placeholder.
     * @return String Gets a message for the given message key and uses it to
     *         format the given value argument.
     */
    public static String getMessage(String key, Object value) {
        return getMessage(key, new Object[] { value });
    }

    /**
     * Gets a message for the given message key and uses it to format the given
     * arguments
     *
     * @param key Message code for which the message to be returned.
     * @param values Object[] Array of object arguments to be replaced in the
     *            message
     * @return String Gets a message for the given message key and uses it to
     *         format the given arguments
     */
    public static String getMessage(String key, Object[] values) {
        String message = getMessage(key);

        if (message != null) {
            return MessageFormat.format(message, values);
        }

        return null;
    }

    /**
     *
     * @param key String Message key.
     * @param value1 Object The value1 to be replaced in the message first
     *            placeholder.
     * @param value2 Object The value2 to be replaced in the message second
     *            placeholder.
     * @return String Gets a message for the given message key and uses it to
     *         format the given value arguments.
     */
    public static String getMessage(String key, Object value1, Object value2) {
        return getMessage(key, new Object[] { value1, value2 });
    }

    /**
     *
     * @param key String Message key.
     * @param value1 Object The value1 to be replaced in the message first
     *            placeholder.
     * @param value2 Object The value2 to be replaced in the message second
     *            placeholder.
     * @param value3 Object The value3 to be replaced in the message third
     *            placeholder.
     * @return String Gets a message for the given message key and uses it to
     *         format the given value arguments.
     */
    public static String getMessage(String key, Object value1, Object value2, Object value3) {
        return getMessage(key, new Object[] { value1, value2, value3 });
    }

    /**
     *
     * @param key String Message key.
     * @param value1 Object The value1 to be replaced in the message first
     *            placeholder.
     * @param value2 Object The value2 to be replaced in the message second
     *            placeholder.
     * @param value3 Object The value3 to be replaced in the message third
     *            placeholder.
     * @param value4 Object The value4 to be replaced in the message fourth
     *            placeholder.
     * @return String Gets a message for the given message key and uses it to
     *         format the given value arguments.
     */
    public static String getMessage(String key, Object value1, Object value2, Object value3, Object value4) {
        return getMessage(key, new Object[] { value1, value2, value3, value4 });
    }

    /**
     *
     * @param key String Message key.
     * @param value1 Object The value1 to be replaced in the message first
     *            placeholder.
     * @param value2 Object The value2 to be replaced in the message second
     *            placeholder.
     * @param value3 Object The value3 to be replaced in the message third
     *            placeholder.
     * @param value4 Object The value4 to be replaced in the message fourth
     *            placeholder.
     * @param value5 Object The value5 to be replaced in the message fifth
     *            placeholder.
     * @return String Gets a message for the given message key and uses it to
     *         format the given value arguments.
     */
    public static String getMessage(String key, Object value1, Object value2, Object value3, Object value4, Object value5) {
        return getMessage(key, new Object[] { value1, value2, value3, value4, value5 });
    }
}
