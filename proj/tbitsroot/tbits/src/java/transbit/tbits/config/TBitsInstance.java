/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/*
 * TBitsInstance.java
 * $Header:
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

//Third party imports.
//Xerces Imports.
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static transbit.tbits.config.XMLParserUtil.getAttrBooleanValue;

//Java Imports.
import static transbit.tbits.config.XMLParserUtil.getAttributeValue;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the properties of a TBits Instance.
 *
 *
 * @author : Vaibhav.
 * @version : $Id: $
 */
public class TBitsInstance implements Serializable{
    private String  myDBName;
    private String  myDBServer;
    private boolean myIsPrimary;

    // Attributes of this Domain Object.
    private String myLocation;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public TBitsInstance() {}

    /**
     * The complete constructor.
     *
     * @param aLocation
     * @param aDBServer
     * @param aDBName
     * @param aIsPrimary
     */
    public TBitsInstance(String aLocation, String aDBServer, String aDBName, boolean aIsPrimary) {
        myLocation  = aLocation;
        myDBServer  = aDBServer;
        myDBName    = aDBName;
        myIsPrimary = aIsPrimary;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method is used to compare two TBitsInstance objects.
     *
     *
     * @param o TBitsInstance object.
     * @return True if equal and False otherwise.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        TBitsInstance aTBitsInstance = null;

        try {
            aTBitsInstance = (TBitsInstance) o;
        } catch (ClassCastException cce) {
            return false;
        }

        return ((myLocation.equalsIgnoreCase(aTBitsInstance.myLocation)) && (myDBServer.equalsIgnoreCase(aTBitsInstance.myDBServer)) && (myDBName.equalsIgnoreCase(aTBitsInstance.myDBName)));
    }

    /**
     * This method is used to compare two TBitsInstance objects.
     * 
     * 
     * 
     * @param aTBitsInstance TBitsInstance object.
     * @return True if equal and False otherwise.
     */
    public boolean equals(TBitsInstance aTBitsInstance) {
        if (aTBitsInstance == null) {
            return false;
        }

        return ((myLocation.equalsIgnoreCase(aTBitsInstance.myLocation)) && (myDBServer.equalsIgnoreCase(aTBitsInstance.myDBServer)) && (myDBName.equalsIgnoreCase(aTBitsInstance.myDBName)));
    }

    public static void main(String arg[]) {
        try {
            java.io.BufferedReader br     = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            StringBuilder          buffer = new StringBuilder();
            String                 str    = "";

            while ((str = br.readLine()) != null) {
                buffer.append(str);
            }

            ArrayList<TBitsInstance> diList = TBitsInstance.xmlDeSerialize(buffer.toString());

            for (TBitsInstance di : diList) {
                System.out.println(di.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the string representation of this object.
     */
    public String toString() {
        return "[ " + myLocation + ", " + myDBServer + ", " + myDBName + ", " + myIsPrimary + " ]\n";
    }

    /**
     * This method parses the XML and returns a list of attachment objects.
     *
     * @param aXml XML corresponding to the attachment-info.
     * @return List of attachments.
     * @exception Exception.
     */
    public static ArrayList<TBitsInstance> xmlDeSerialize(String aXml) throws Exception {
        ArrayList<TBitsInstance> list = new ArrayList<TBitsInstance>();

        if ((aXml == null) || (aXml.trim().equals("") == true)) {
            return list;
        }

        DocumentBuilderFactory factory         = DocumentBuilderFactory.newInstance();
        DocumentBuilder        documentBuilder = null;
        Document               document        = null;

        try {
            documentBuilder = factory.newDocumentBuilder();
            document        = (org.w3c.dom.Document) documentBuilder.parse(new ByteArrayInputStream(aXml.getBytes()));

            NodeList rootNodeList  = document.getElementsByTagName("TBitsInstances");
            Node     rootNode      = rootNodeList.item(0);
            NodeList childNodeList = rootNode.getChildNodes();

            for (int i = 0; i < childNodeList.getLength(); i++) {
                TBitsInstance obj       = new TBitsInstance();
                Node          childNode = childNodeList.item(i);

                /*
                 * <Instance
                 * location=""
                 * server=""
                 * name=""
                 * isPrimary=""
                 * />
                 */
                if (childNode.getNodeName().equals("Instance")) {
                    String  location  = getAttributeValue(childNode, "location");
                    String  server    = getAttributeValue(childNode, "server");
                    String  name      = getAttributeValue(childNode, "name");
                    boolean isPrimary = getAttrBooleanValue(childNode, "isPrimary");

                    obj.setLocation(location);
                    obj.setDBServer(server);
                    obj.setDBName(name);
                    obj.setIsPrimary(isPrimary);
                    list.add(obj);
                }
            }
        } catch (Exception e) {
            throw e;
        }

        return list;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for DBName property.
     *
     * @return Current Value of DBName
     */
    public String getDBName() {
        return myDBName;
    }

    /**
     * Accessor method for DBServer property.
     *
     * @return Current Value of DBServer
     */
    public String getDBServer() {
        return myDBServer;
    }

    /**
     * Accessor method for IsPrimary property.
     *
     * @return Current Value of IsPrimary
     */
    public boolean getIsPrimary() {
        return myIsPrimary;
    }

    /**
     * Accessor method for Location property.
     *
     * @return Current Value of Location
     */
    public String getLocation() {
        return myLocation;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for DBName property.
     *
     * @param aDBName New Value for DBName
     */
    public void setDBName(String aDBName) {
        myDBName = aDBName;
    }

    /**
     * Mutator method for DBServer property.
     *
     * @param aDBServer New Value for DBServer
     */
    public void setDBServer(String aDBServer) {
        myDBServer = aDBServer;
    }

    /**
     * Mutator method for IsPrimary property.
     *
     * @param aIsPrimary New Value for IsPrimary
     */
    public void setIsPrimary(boolean aIsPrimary) {
        myIsPrimary = aIsPrimary;
    }

    /**
     * Mutator method for Location property.
     *
     * @param aLocation New Value for Location
     */
    public void setLocation(String aLocation) {
        myLocation = aLocation;
    }
}
