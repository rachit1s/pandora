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
 * NotificationConfig.java
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

import transbit.tbits.Helper.TBitsConstants;

//Imports from other packages of TBits.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;

//Static imports if any.
import static transbit.tbits.config.XMLParserUtil.getAttributeValue;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.ByteArrayInputStream;
import java.io.Serializable;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the notification rules config for a particular rule.
 *
 * @author  Vinod Gupta
 * @version $Id: $
 */
public class NotificationConfig implements TBitsConstants, Serializable {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    //~--- fields -------------------------------------------------------------

    private ArrayList<NotifyRule> myNotifyRules;

    // Attributes.
    private String mySendMail;

    //~--- constructors -------------------------------------------------------

    /**
     * Default Private Constructor.
     */
    private NotificationConfig() {
        mySendMail    = "";
        myNotifyRules = new ArrayList<NotifyRule>();
    }

    /**
     * The complete constructor.
     *
     *  @param aSendMail
     *  @param aNotifyRules
     */
    public NotificationConfig(String aSendMail, ArrayList<NotifyRule> aNotifyRules) {
        mySendMail    = aSendMail;
        myNotifyRules = aNotifyRules;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * main method.
     */
    public static void main(String arg[]) throws Exception {
        NotificationConfig nConfig = NotificationConfig.getNotificationConfig("<NotificationConfig>" + "<SendMail value=\"rules\">" + "<Rule id=\"1\" day=\"monday,tuesday\" "
                                         + "startTime=\"9:00\" endTime=\"5:00\" zone=\"est\"/>" + "<Rule id=\"2\" day=\"saturday,sunday\" " + "startTime=\"00:00\" endTime=\"23:59\" zone=\"ist\"/>"
                                         + "</SendMail>" + "</NotificationConfig>");

        LOG.info(nConfig.toString());
    }

    /**
     * This method returns the string representation of the object
     */
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("\nSendMail  : ").append(mySendMail);

        for (NotifyRule nf : myNotifyRules) {
            str.append("\nRule").append(nf.toString());
        }

        return str.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This static method is factory method to build a NotificationConfig
     * object from given XML.
     *
     *
     * @param aXml XML that represents the NotificationConfig in the database.
     * @return NotificationConfig object corresponding to the given XML.
     * @exception DETBitsExceptionncase of any exception during xml-parsing.
     */
    public static NotificationConfig getNotificationConfig(String aXml) throws TBitsException {
        ArrayList<NotifyRule> notifyRules        = new ArrayList<NotifyRule>();
        NotificationConfig    notificationConfig = new NotificationConfig();

        if ((aXml == null) || (aXml.trim().equals("") == true)) {
            return notificationConfig;
        }

        DocumentBuilderFactory factory         = DocumentBuilderFactory.newInstance();
        DocumentBuilder        documentBuilder = null;
        Document               document        = null;

        try {
            documentBuilder = factory.newDocumentBuilder();
            document        = (org.w3c.dom.Document) documentBuilder.parse(new ByteArrayInputStream(aXml.getBytes()));

            NodeList rootNodeList    = document.getElementsByTagName("NotificationConfig");
            Node     rootNode        = rootNodeList.item(0);
            NodeList childNodeList   = rootNode.getChildNodes();
            int      childNodeLength = childNodeList.getLength();

            for (int i = 0; i < childNodeLength; i++) {
                Node childNode = childNodeList.item(i);

                //
                // <SendMail value="always/never/rules">
                // <Rule />
                // <Rule />
                // </SendMail>
                //
                if (childNode.getNodeName().equals("SendMail")) {
                    String sendMail = getAttributeValue(childNode, "value");

                    sendMail = ((sendMail == null)
                                ? ""
                                : sendMail.trim());
                    notificationConfig.setSendMail(sendMail);

                    if (sendMail.equalsIgnoreCase("rules")) {
                        NodeList ruleNodes = childNode.getChildNodes();

                        //
                        // <Rule id="1" day="" startTime=""
                        // endTime="" zone="" />
                        //
                        int ruleNodesLength = ruleNodes.getLength();

                        for (int j = 0; j < ruleNodesLength; j++) {
                            Node aRuleNode = ruleNodes.item(j);

                            if (!aRuleNode.getNodeName().equals("Rule")) {
                                continue;
                            }

                            NotifyRule rc = new NotifyRule();

                            rc.setId(getAttributeValue(aRuleNode, "id"));
                            rc.setDay(getAttributeValue(aRuleNode, "day"));
                            rc.setStartTime(getAttributeValue(aRuleNode, "startTime"));
                            rc.setEndTime(getAttributeValue(aRuleNode, "endTime"));
                            rc.setZone(getAttributeValue(aRuleNode, "zone"));
                            notifyRules.add(rc);
                        }

                        notificationConfig.setNotifyRules(notifyRules);
                    }
                }
            }
        } catch (Exception e) {
            throw new TBitsException(e.toString());
        }

        return notificationConfig;
    }

    /**
     * Accessor method for NotifyRules property.
     *
     * @return Current Value of NotifyRules
     *
     */
    public ArrayList<NotifyRule> getNotifyRules() {
        return myNotifyRules;
    }

    /**
     * Accessor method for SendMail property.
     *
     * @return Current Value of SendMail
     *
     */
    public String getSendMail() {
        return mySendMail;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for NotifyRules property.
     *
     * @param aNotifyRules New Value for NotifyRules
     *
     */
    public void setNotifyRules(ArrayList<NotifyRule> aNotifyRules) {
        myNotifyRules = aNotifyRules;
    }

    /**
     * Mutator method for SendMail property.
     *
     * @param aSendMail New Value for SendMail
     *
     */
    public void setSendMail(String aSendMail) {
        mySendMail = aSendMail;
    }
}
