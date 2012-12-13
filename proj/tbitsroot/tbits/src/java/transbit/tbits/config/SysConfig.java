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
 * SysConfig.java
 *
 * $Header:
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

//Third party imports.
//Xerces Imports.
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import transbit.tbits.Helper.TBitsConstants;

//Imports from other packages of TBits.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;

//Imports from current package.
import transbit.tbits.config.CustomLink;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.config.XMLParserUtil.getAttrBooleanValue;
import static transbit.tbits.config.XMLParserUtil.getAttrIntegerValue;
import static transbit.tbits.config.XMLParserUtil.getAttrLongValue;

//Static imports if any.
import static transbit.tbits.config.XMLParserUtil.getAttributeValue;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.ByteArrayInputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the sys config of a business area.
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class SysConfig implements TBitsConstants, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    //~--- fields -------------------------------------------------------------

    private int     myActionNotify;
    private boolean myActionNotifyLoggers;

    // Attributes.
    // Administrator for this Business area.
    private String  myAdministrator;
    private boolean myAllowNullDueDate;

    // Assign related attributes.
    private boolean myAssignToAll;

    // Conversion from Bmp2Png.
    private boolean myBmp2Png;

    // Custom references.
    private ArrayList<CustomLink> myCustomLinks;
    private long                  myDefaultDueDate;
    private int                   myEmailDateFormat;
    private String                myEmailStylesheet;
    private String                myIncomingSeverityHigh;

    // Severity related attributes.
    private String myIncomingSeverityLow;

    // Date related attributes.
    private boolean myIsDueDateDisabled;
    private boolean myIsTimeDisabled;

    // List of legacy prefixes for this BA.
    private ArrayList<String> myLegacyPrefixList;
    private int               myListDateFormat;
    private int               myMailFormat;
    long                      myMaxGrayPeriod;
    private boolean           myNotifyAppender;
    private ArrayList<String> myOutgoingSeverityHigh;
    private ArrayList<String> myOutgoingSeverityLow;

    // Preferred Zone
    private int myPreferredZone;

    // Email related attributes.
    private int     myRequestNotify;
    private boolean myRequestNotifyLoggers;

    // Table of short cuts.
    private Hashtable<String, Shortcut> myShortcuts;
    private int                         myVolunteer;

    // Stylesheet related attributes.
    private String myWebStylesheet;

    //~--- constructors -------------------------------------------------------

    /**
     * Default Constructor but private.
     */
    public SysConfig() {
        myAdministrator        = "TBits Group";
        myWebStylesheet        = "tbits.css";
        myEmailStylesheet      = "tbits_email.css";
        myIsDueDateDisabled    = false;
        myAllowNullDueDate     = false;
        myIsTimeDisabled       = false;
        myDefaultDueDate       = 90;
        myListDateFormat       = 1;
        myEmailDateFormat      = 1;
        myRequestNotify        = NOTIFY_EMAIL;
        myActionNotify         = NOTIFY_EMAIL;
        myRequestNotifyLoggers = true;
        myActionNotifyLoggers  = true;
        myNotifyAppender       = true;
        myMailFormat           = HTML_FORMAT;
        myIncomingSeverityLow  = "low";
        myIncomingSeverityHigh = "critical";

        ArrayList<String> temp = new ArrayList<String>();

        temp.add("low");
        myOutgoingSeverityLow = temp;
        temp                  = null;
        temp                  = new ArrayList<String>();
        temp.add("critical");
        myOutgoingSeverityHigh = temp;
        myAssignToAll          = false;
        myVolunteer            = NO_VOLUNTEER;
        myPreferredZone        = SITE_ZONE;
        myBmp2Png              = true;
        myLegacyPrefixList     = new ArrayList<String>();
        myMaxGrayPeriod        = 90;    // 90 days by default.
        myCustomLinks          = new ArrayList<CustomLink>();
    }

    /**
     * The complete constructor.
     *
     *  @param aAdministrator
     *  @param aWebStylesheet
     *  @param aEmailStylesheet
     *  @param aDefaultDueDate
     *  @param aListDateFormat
     *  @param aEmailDateFormat
     *  @param aRequestNotify
     *  @param aActionNotify
     *  @param aRequestNotifyLoggers
     *  @param aActionNotifyLoggers
     *  @param aNotifyAppender
     *  @param aMailFormat
     *  @param aIncomingSeverityLow
     *  @param aIncomingSeverityHigh
     *  @param aOutgoingSeverityLow
     *  @param aOutgoingSeverityHigh
     *  @param aAssignToAll
     *  @param aVolunteer
     *  @param aPreferredZone
     *  @param aBmp2Png
     *  @param aLegacyPrefixList
     *  @param aCustomLinks
     */
    public SysConfig(String aAdministrator, String aWebStylesheet, String aEmailStylesheet, boolean aIsDueDateDisabled, boolean aIsTimeDisabled, boolean aAllowNullDueDate, long aDefaultDueDate,
                     int aListDateFormat, int aEmailDateFormat, int aRequestNotify, int aActionNotify, boolean aRequestNotifyLoggers, boolean aActionNotifyLoggers, boolean aNotifyAppender,
                     int aMailFormat, String aIncomingSeverityLow, String aIncomingSeverityHigh, ArrayList<String> aOutgoingSeverityLow, ArrayList<String> aOutgoingSeverityHigh, boolean aAssignToAll,
                     int aVolunteer, int aPreferredZone, boolean aBmp2Png, ArrayList<String> aLegacyPrefixList, long aMaxGrayPeriod, ArrayList<CustomLink> aCustomLinks) {
        myAdministrator        = aAdministrator;
        myWebStylesheet        = aWebStylesheet;
        myEmailStylesheet      = aEmailStylesheet;
        myIsDueDateDisabled    = aIsDueDateDisabled;
        myIsTimeDisabled       = aIsTimeDisabled;
        myAllowNullDueDate     = aAllowNullDueDate;
        myDefaultDueDate       = aDefaultDueDate;
        myListDateFormat       = aListDateFormat;
        myEmailDateFormat      = aEmailDateFormat;
        myRequestNotify        = aRequestNotify;
        myActionNotify         = aActionNotify;
        myRequestNotifyLoggers = aRequestNotifyLoggers;
        myActionNotifyLoggers  = aActionNotifyLoggers;
        myNotifyAppender       = aNotifyAppender;
        myMailFormat           = aMailFormat;
        myIncomingSeverityLow  = aIncomingSeverityLow;
        myIncomingSeverityHigh = aIncomingSeverityHigh;
        myOutgoingSeverityLow  = aOutgoingSeverityLow;
        myOutgoingSeverityHigh = aOutgoingSeverityHigh;
        myAssignToAll          = aAssignToAll;
        myVolunteer            = aVolunteer;
        myPreferredZone        = aPreferredZone;
        myBmp2Png              = aBmp2Png;
        myLegacyPrefixList     = aLegacyPrefixList;
        myMaxGrayPeriod        = aMaxGrayPeriod;
        myCustomLinks          = aCustomLinks;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method to add a shortcut to the list of shortcuts.
     *
     * @param aShortcut Shortcut to be added.
     *
     */
    public void addShortcut(Shortcut aShortcut) {
        if (aShortcut == null) {
            return;
        }

        if (myShortcuts == null) {
            myShortcuts = new Hashtable<String, Shortcut>();
        }

        myShortcuts.put(aShortcut.getName().toUpperCase(), aShortcut);
    }

    /**
     * This method removes the entry corresponding to given shortcut name from
     * the shortcut table.
     *
     * @param shortcutName      Name of the shortcut.
     */
    public void deleteShortcut(String shortcutName) {
        myShortcuts.remove(shortcutName.toUpperCase());
    }

    /**
     * main method.
     */
    public static void main(String arg[]) throws Exception {
        SysConfig sysConfig = SysConfig.getSysConfig("<SysConfig><LegacyPrefixes list=\"false\" maxGrayPeriod=\"2\"/>" + "</SysConfig>");

        sysConfig.setIsDueDateDisabled(true);
        LOG.info(sysConfig.xmlSerialize());
    }

    private String serializeAdministrator() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<Administrator name=\"").append(myAdministrator).append("\" />");

        return buffer.toString();
    }

    private String serializeAssign() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<Assign all=\"").append(myAssignToAll).append("\" volunteer=\"").append(myVolunteer).append("\" />");

        return buffer.toString();
    }

    private String serializeCustomLinks() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<CustomLinks>");

        for (CustomLink link : myCustomLinks) {
            buffer.append("\n\t\t<Link name=\"").append(link.getName()).append("\" value=\"").append(link.getValue()).append("\" />");
        }

        buffer.append("\n\t</CustomLinks>");

        return buffer.toString();
    }

    private String serializeDateFormat() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<DateFormat list=\"").append(myListDateFormat).append("\" email=\"").append(myEmailDateFormat).append("\" />");

        return buffer.toString();
    }

    private String serializeDefaultDueDate() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<DefaultDueDate ").append("disabled=\"").append(myIsDueDateDisabled).append("\" disableTime=\"").append(myIsTimeDisabled).append("\" allowNull=\"").append(
            myAllowNullDueDate).append("\" duration=\"").append(myDefaultDueDate).append("\" />");

        return buffer.toString();
    }

    private String serializeLegacyPrefixes() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<Legacy prefixList=\"").append(Utilities.arrayListToString(myLegacyPrefixList)).append("\" maxGrayPeriod=\"").append(myMaxGrayPeriod).append("\" />");

        return buffer.toString();
    }

    private String serializeMailFormat() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<MailFormat format=\"").append(myMailFormat).append("\" />");

        return buffer.toString();
    }

    private String serializeNotify() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<Notify request=\"").append(myRequestNotify).append("\" action=\"").append(myActionNotify).append("\" />");

        return buffer.toString();
    }

    private String serializeNotifyAppender() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<NotifyAppender value=\"").append(myNotifyAppender).append("\" />");

        return buffer.toString();
    }

    private String serializeNotifyLoggers() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<NotifyLogger request=\"").append(myRequestNotifyLoggers).append("\" action=\"").append(myActionNotifyLoggers).append("\" />");

        return buffer.toString();
    }

    private String serializeSeverity() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<Severity>").append("\n\t\t<Incoming highValue=\"").append(myIncomingSeverityHigh).append("\" lowValue=\"").append(myIncomingSeverityLow).append("\" />").append(
            "\n\t\t<Outgoing highValue=\"").append(Utilities.arrayListToString(myOutgoingSeverityHigh)).append("\" lowValue=\"").append(Utilities.arrayListToString(myOutgoingSeverityLow)).append(
            "\" />").append("\n\t</Severity>");

        return buffer.toString();
    }

    /**
     * This method returns the XML-Serialized representation of shortcuts.
     *
     * @return XML Serialized string representation of this object.
     */
    private String serializeShortcuts(Hashtable<String, Shortcut> aTable) {
        StringBuilder xml = new StringBuilder();

        if (aTable == null) {
            return xml.toString();
        }

        ArrayList<Shortcut> list = new ArrayList<Shortcut>(aTable.values());

        for (Shortcut sc : list) {
            xml.append(sc.xmlSerialize());
        }

        return xml.toString();
    }

    private String serializeStylesheet() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<Stylesheet web=\"").append(myWebStylesheet).append("\" email=\"").append(myEmailStylesheet).append("\" />");

        return buffer.toString();
    }

    private String serializeTimezone() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t").append("<Timezone zone=\"").append(myPreferredZone).append("\" />");

        return buffer.toString();
    }

    /**
     * This method returns the XML-Serialized representation of this sysconfig.
     *
     * @return XML Serialized string representation of this object.
     */
    public String xmlSerialize() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n<SysConfig>").append(serializeAdministrator()).append(serializeStylesheet()).append(serializeDefaultDueDate()).append(serializeDateFormat()).append(serializeNotify()).append(
            serializeNotifyLoggers()).append(serializeNotifyAppender()).append(serializeMailFormat()).append(serializeSeverity()).append(serializeAssign()).append(serializeTimezone()).append(
            serializeLegacyPrefixes()).append(serializeCustomLinks()).append(serializeShortcuts(myShortcuts)).append("\n</SysConfig>").append("\n");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for ActionNotify property.
     *
     * @return Current Value of ActionNotify
     *
     */
    public int getActionNotify() {
        return myActionNotify;
    }

    /**
     * Accessor method for ActionNotifyLoggers property.
     *
     * @return Current Value of ActionNotifyLoggers
     *
     */
    public boolean getActionNotifyLoggers() {
        return myActionNotifyLoggers;
    }

    /**
     * Accessor method for Administrator property.
     *
     * @return Current Value of Administrator
     *
     */
    public String getAdministrator() {
        return myAdministrator;
    }

    /**
     * Accessor method for AllowNullDueDate property.
     *
     * @return Current Value of AllowNullDueDate
     *
     */
    public boolean getAllowNullDueDate() {
        return myAllowNullDueDate;
    }

    /**
     * Accessor method for AssignToAll property.
     *
     * @return Current Value of AssignToAll
     *
     */
    public boolean getAssignToAll() {
        return myAssignToAll;
    }

    /**
     * Accessor method for Bmp2Png property.
     *
     * @return Current Value of Bmp2Png
     *
     */
    public boolean getBmp2Png() {
        return myBmp2Png;
    }

    /**
     * Accessor method for CustomLinks property.
     *
     * @return Current Value of CustomLinks
     *
     */
    public ArrayList<CustomLink> getCustomLinks() {
        return myCustomLinks;
    }

    /**
     * Accessor method for DefaultDueDate property.
     *
     * @return Current Value of DefaultDueDate
     *
     */
    public long getDefaultDueDate() {
        return myDefaultDueDate;
    }

    /**
     * Accessor method for EmailDateFormat property.
     *
     * @return Current Value of EmailDateFormat
     *
     */
    public int getEmailDateFormat() {
        return myEmailDateFormat;
    }

    /**
     * Accessor method for EmailStylesheet property.
     *
     * @return Current Value of EmailStylesheet
     *
     */
    public String getEmailStylesheet() {
        return myEmailStylesheet;
    }

    /**
     * Accessor method for IncomingSeverityHigh property.
     *
     * @return Current Value of IncomingSeverityHigh
     *
     */
    public String getIncomingSeverityHigh() {
        return myIncomingSeverityHigh;
    }

    /**
     * Accessor method for IncomingSeverityLow property.
     *
     * @return Current Value of IncomingSeverityLow
     *
     */
    public String getIncomingSeverityLow() {
        return myIncomingSeverityLow;
    }

    /**
     * Accessor method for IsDueDateDisabled property.
     *
     * @return Current Value of IsDueDateDisabled
     *
     */
    public boolean getIsDueDateDisabled() {
        return myIsDueDateDisabled;
    }

    /**
     * Accessor method for IsTimeEnabled property.
     *
     * @return Current Value of IsTimeEnabled
     *
     */
    public boolean getIsTimeDisabled() {
        return myIsTimeDisabled;
    }

    /**
     * Accessor method for LegacyPrefixList property.
     *
     * @return Current Value of LegacyPrefixList
     *
     */
    public ArrayList<String> getLegacyPrefixList() {
        return myLegacyPrefixList;
    }

    /**
     * Accessor method for ListDateFormat property.
     *
     * @return Current Value of ListDateFormat
     *
     */
    public int getListDateFormat() {
        return myListDateFormat;
    }

    /**
     * Accessor method for MailFormat property.
     *
     * @return Current Value of MailFormat
     *
     */
    public int getMailFormat() {
        return myMailFormat;
    }

    /**
     * Accessor method for MaxGrayPeriod property.
     *
     * @return Current Value of MaxGrayPeriod
     *
     */
    public long getMaxGrayPeriod() {
        return myMaxGrayPeriod;
    }

    /**
     * Accessor method for NotifyAppender property.
     *
     * @return Current Value of NotifyAppender
     *
     */
    public boolean getNotifyAppender() {
        return myNotifyAppender;
    }

    /**
     * Accessor method for OutgoingSeverityHigh property.
     *
     * @return Current Value of OutgoingSeverityHigh
     *
     */
    public ArrayList<String> getOutgoingSeverityHigh() {
        return myOutgoingSeverityHigh;
    }

    /**
     * Accessor method for OutgoingSeverityLow property.
     *
     * @return Current Value of OutgoingSeverityLow
     *
     */
    public ArrayList<String> getOutgoingSeverityLow() {
        return myOutgoingSeverityLow;
    }

    /**
     * Accessor method for Preferred Zone property.
     *
     * @return Current Value of Preferred Zone
     *
     */
    public int getPreferredZone() {
        return myPreferredZone;
    }

    /**
     * Accessor method for RequestNotify property.
     *
     * @return Current Value of RequestNotify
     *
     */
    public int getRequestNotify() {
        return myRequestNotify;
    }

    /**
     * Accessor method for RequestNotifyLoggers property.
     *
     * @return Current Value of RequestNotifyLoggers
     *
     */
    public boolean getRequestNotifyLoggers() {
        return myRequestNotifyLoggers;
    }

    /**
     * Method to get a shortcut with specified name.
     *
     * @param aName Shortcut to be added.
     *
     */
    public Shortcut getShortcut(String aName) {
        Shortcut sc = myShortcuts.get(aName.toUpperCase());

        return sc;
    }

    /**
     * Accessor method for myShortcuts property.
     *
     * @return Current Value of myShortcuts
     *
     */
    public Hashtable<String, Shortcut> getShortcuts() {
        return myShortcuts;
    }

    /**
     * This static method is factory method to build a SysConfig object from
     * given XML.
     *
     *
     * @param aXml XML that represents the SysConfig in the database.
     * @return SysConfig object corresponding to the given XML.
     * @exception DETBitsExceptionncase of any exception during xml-parsing.
     */
    public static SysConfig getSysConfig(String aXml) throws TBitsException {
        SysConfig sysConfig = new SysConfig();

        if ((aXml == null) || (aXml.trim().equals("") == true)) {
            return sysConfig;
        }

        DocumentBuilderFactory      factory         = DocumentBuilderFactory.newInstance();
        DocumentBuilder             documentBuilder = null;
        Document                    document        = null;
        ArrayList<CustomLink>       customLinks     = new ArrayList<CustomLink>();
        Hashtable<String, Shortcut> shortcuts       = new Hashtable<String, Shortcut>();

        try {
            documentBuilder = factory.newDocumentBuilder();
            document        = (org.w3c.dom.Document) documentBuilder.parse(new ByteArrayInputStream(aXml.getBytes()));

            NodeList rootNodeList  = document.getElementsByTagName("SysConfig");
            Node     rootNode      = rootNodeList.item(0);
            NodeList childNodeList = rootNode.getChildNodes();

            for (int i = 0; i < childNodeList.getLength(); i++) {
                Node childNode = childNodeList.item(i);

                /*
                 * <Administrator
                 *      name="TBits Group"
                 * />
                 */
                if (childNode.getNodeName().equals("Administrator")) {
                    String admin = getAttributeValue(childNode, "name");

                    sysConfig.setAdministrator(admin);
                }

               
                if (childNode.getNodeName().equals("Stylesheet")) {
                    String cssWeb  = getAttributeValue(childNode, "web");
                    String cssMail = getAttributeValue(childNode, "email");

                    sysConfig.setWebStylesheet(cssWeb);
                    sysConfig.setEmailStylesheet(cssMail);
                }

                /*
                 * <DefaultDueDate
                 *     disabled="false"
                 *     disableTime="false"
                 *     allowNull="false"
                 *     duration="90"
                 * />
                 */
                if (childNode.getNodeName().equals("DefaultDueDate")) {
                    NamedNodeMap nnmap             = childNode.getAttributes();
                    Node         attrNode          = null;
                    boolean      isDueDateDisabled = getAttrBooleanValue(childNode, "disabled");

                    sysConfig.setIsDueDateDisabled(isDueDateDisabled);

                    boolean isTimeDisabled = getAttrBooleanValue(childNode, "disableTime");

                    sysConfig.setIsTimeDisabled(isTimeDisabled);

                    boolean allowNull = getAttrBooleanValue(childNode, "allowNull");

                    sysConfig.setAllowNullDueDate(allowNull);

                    long duration = getAttrLongValue(childNode, "duration", 90);

                    sysConfig.setDefaultDueDate(duration);
                }

                //
                // <DateFormat list="1" email="1" />
                //
                if (childNode.getNodeName().equals("DateFormat")) {
                    int listFormat = getAttrIntegerValue(childNode, "list", 1);
                    int mailFormat = getAttrIntegerValue(childNode, "list", 1);

                    sysConfig.setListDateFormat(listFormat);
                    sysConfig.setEmailDateFormat(mailFormat);
                }

                //
                // <Notify request="1" action="1" />
                //
                if (childNode.getNodeName().equals("Notify")) {
                    int request = getAttrIntegerValue(childNode, "request", 1);
                    int action  = getAttrIntegerValue(childNode, "action", 1);

                    sysConfig.setRequestNotify(request);
                    sysConfig.setActionNotify(action);
                }

                //
                // <NotifyLogger request="1" action="1" />
                //
                if (childNode.getNodeName().equals("NotifyLogger")) {
                    boolean req = getAttrBooleanValue(childNode, "request");
                    boolean act = getAttrBooleanValue(childNode, "action");

                    sysConfig.setRequestNotifyLoggers(req);
                    sysConfig.setActionNotifyLoggers(act);
                }

                //
                // <NotifyAppender value="true" />
                //
                if (childNode.getNodeName().equals("NotifyAppender")) {
                    boolean value = getAttrBooleanValue(childNode, "value");

                    sysConfig.setNotifyAppender(value);
                }

                //
                // <MailFormat value="HTML" />
                //
                if (childNode.getNodeName().equals("MailFormat")) {
                    NamedNodeMap nnmap    = childNode.getAttributes();
                    Node         attrNode = null;
                    int          format   = getAttrIntegerValue(childNode, "format", 1);

                    sysConfig.setMailFormat(format);
                }

                //
                // <Severity>
                // <Incoming highValue="critical" lowValue="low" />
                // <Outgoing highValue="critical" lowValue="low" />
                // </Severity>
                //
                if (childNode.getNodeName().equals("Severity")) {
                    NodeList sevNodes      = childNode.getChildNodes();
                    int      sevNodeLength = sevNodes.getLength();

                    for (int j = 0; j < sevNodeLength; j++) {
                        Node aSevNode = sevNodes.item(j);

                        if (aSevNode.getNodeName().equals("Incoming")) {
                            String high = getAttributeValue(aSevNode, "highValue");
                            String low  = getAttributeValue(aSevNode, "lowValue");

                            sysConfig.setIncomingSeverityHigh(high);
                            sysConfig.setIncomingSeverityLow(low);
                        } else if (aSevNode.getNodeName().equals("Outgoing")) {
                            String high = getAttributeValue(aSevNode, "highValue");
                            String low  = getAttributeValue(aSevNode, "lowValue");

                            sysConfig.setOutgoingSeverityHigh(Utilities.toArrayList(high));
                            sysConfig.setOutgoingSeverityLow(Utilities.toArrayList(low));
                        }
                    }
                }

                //
                // <Assign all="true" volunteer="0" />
                //
                if (childNode.getNodeName().equals("Assign")) {
                    boolean toAll     = getAttrBooleanValue(childNode, "all");
                    int     volunteer = getAttrIntegerValue(childNode, "volunteer", NO_VOLUNTEER);

                    sysConfig.setAssignToAll(toAll);
                    sysConfig.setVolunteer(volunteer);
                }

                /*
                 * <Timezone
                 *      zone="0"
                 * />
                 */
                if (childNode.getNodeName().equals("Timezone")) {
                    int zone = getAttrIntegerValue(childNode, "zone", SITE_ZONE);

                    sysConfig.setPreferredZone(zone);
                }

                //
                // <Legacy
                // prefixList="1"
                // maxGrayPeriod="90"
                // />
                //
                if (childNode.getNodeName().equals("Legacy") || childNode.getNodeName().equals("LegacyPrefixes")) {
                    String pList = getAttributeValue(childNode, "prefixList");
                    long   gp    = getAttrLongValue(childNode, "maxGrayPeriod", 90);

                    sysConfig.setLegacyPrefixList(Utilities.toArrayList(pList));
                    sysConfig.setMaxGrayPeriod(gp);
                }

                //
                // <CustomLinks>
                // <Link name="TBits Help"
                // value="http://tbits.hyd.transbittech.com/web/help.htm"
                // />
                // </CustomLinks>
                //
                if (childNode.getNodeName().equals("CustomLinks")) {
                    NodeList linkNodes      = childNode.getChildNodes();
                    int      linkNodeLength = linkNodes.getLength();

                    for (int j = 0; j < linkNodeLength; j++) {
                        Node aLinkNode = linkNodes.item(j);

                        if (aLinkNode.getNodeName().equals("Link")) {
                            CustomLink cl   = new CustomLink();
                            String     name = getAttributeValue(aLinkNode, "name");
                            String     val  = getAttributeValue(aLinkNode, "value");

                            cl.setName(name);
                            cl.setValue(val);
                            customLinks.add(cl);
                        }
                    }
                }

                if (childNode.getNodeName().equals("Shortcut")) {
                    Shortcut sc = Shortcut.parseShortcutNode(childNode);

                    shortcuts.put(sc.getName().toUpperCase(), sc);
                }
            }

            sysConfig.setCustomLinks(customLinks);
            sysConfig.setShortcuts(shortcuts);
        } catch (Exception e) {
            LOG.info("",(e));

            throw new TBitsException(e.toString());
        }

        return sysConfig;
    }

    /**
     * Accessor method for Volunteer property.
     *
     * @return Current Value of Volunteer
     *
     */
    public int getVolunteer() {
        return myVolunteer;
    }

    /**
     * Accessor method for WebStylesheet property.
     *
     * @return Current Value of WebStylesheet
     *
     */
    public String getWebStylesheet() {
        return myWebStylesheet;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for ActionNotify property.
     *
     * @param aActionNotify New Value for ActionNotify
     *
     */
    public void setActionNotify(int aActionNotify) {
        myActionNotify = aActionNotify;
    }

    /**
     * Mutator method for ActionNotifyLoggers property.
     *
     * @param aActionNotifyLoggers New Value for ActionNotifyLoggers
     *
     */
    public void setActionNotifyLoggers(boolean aActionNotifyLoggers) {
        myActionNotifyLoggers = aActionNotifyLoggers;
    }

    /**
     * Mutator method for Administrator property.
     *
     * @param aAdministrator New Value for Administrator
     *
     */
    public void setAdministrator(String aAdministrator) {
        myAdministrator = aAdministrator;
    }

    /**
     * Mutator method for AllowNullDueDate property.
     *
     * @param aAllowNullDueDate New Value for AllowNullDueDate
     *
     */
    public void setAllowNullDueDate(boolean aAllowNullDueDate) {
        myAllowNullDueDate = aAllowNullDueDate;
    }

    /**
     * Mutator method for AssignToAll property.
     *
     * @param aAssignToAll New Value for AssignToAll
     *
     */
    public void setAssignToAll(boolean aAssignToAll) {
        myAssignToAll = aAssignToAll;
    }

    /**
     * Mutator method for Bmp2Png property.
     *
     * @param aBmp2Png New Value for Bmp2Png
     *
     */
    public void setBmp2Png(boolean aBmp2Png) {
        myBmp2Png = aBmp2Png;
    }

    /**
     * Mutator method for CustomLinks property.
     *
     * @param aCustomLinks New Value for CustomLinks
     *
     */
    public void setCustomLinks(ArrayList<CustomLink> aCustomLinks) {
        myCustomLinks = aCustomLinks;
    }

    /**
     * Mutator method for DefaultDueDate property.
     *
     * @param aDefaultDueDate New Value for DefaultDueDate
     *
     */
    public void setDefaultDueDate(long aDefaultDueDate) {
        myDefaultDueDate = aDefaultDueDate;
    }

    /**
     * Mutator method for EmailDateFormat property.
     *
     * @param aEmailDateFormat New Value for EmailDateFormat
     *
     */
    public void setEmailDateFormat(int aEmailDateFormat) {
        myEmailDateFormat = aEmailDateFormat;
    }

    /**
     * Mutator method for EmailStylesheet property.
     *
     * @param aEmailStylesheet New Value for EmailStylesheet
     *
     */
    public void setEmailStylesheet(String aEmailStylesheet) {
        myEmailStylesheet = aEmailStylesheet;
    }

    /**
     * Mutator method for IncomingSeverityHigh property.
     *
     * @param aIncomingSeverityHigh New Value for IncomingSeverityHigh
     *
     */
    public void setIncomingSeverityHigh(String aIncomingSeverityHigh) {
        myIncomingSeverityHigh = aIncomingSeverityHigh;
    }

    /**
     * Mutator method for IncomingSeverityLow property.
     *
     * @param aIncomingSeverityLow New Value for IncomingSeverityLow
     *
     */
    public void setIncomingSeverityLow(String aIncomingSeverityLow) {
        myIncomingSeverityLow = aIncomingSeverityLow;
    }

    /**
     * Mutator method for IsDueDateDisabled property.
     *
     * @param aIsDueDateDisabled New Value for IsDueDateDisabled
     *
     */
    public void setIsDueDateDisabled(boolean aIsDueDateDisabled) {
        myIsDueDateDisabled = aIsDueDateDisabled;
    }

    /**
     * Mutator method for IsTimeEnabled property.
     *
     * @param aIsTimeDisabled New Value for IsTimeEnabled.
     *
     */
    public void setIsTimeDisabled(boolean aIsTimeDisabled) {
        myIsTimeDisabled = aIsTimeDisabled;
    }

    /**
     * Mutator method for LegacyPrefixList property.
     *
     * @param aLegacyPrefixList New Value for LegacyPrefixList
     *
     */
    public void setLegacyPrefixList(ArrayList<String> aLegacyPrefixList) {
        myLegacyPrefixList = aLegacyPrefixList;
    }

    /**
     * Mutator method for ListDateFormat property.
     *
     * @param aListDateFormat New Value for ListDateFormat
     *
     */
    public void setListDateFormat(int aListDateFormat) {
        myListDateFormat = aListDateFormat;
    }

    /**
     * Mutator method for MailFormat property.
     *
     * @param aMailFormat New Value for MailFormat
     *
     */
    public void setMailFormat(int aMailFormat) {
        myMailFormat = aMailFormat;
    }

    /**
     * Mutator method for MaxGrayPeriod property.
     *
     * @param aMaxGrayPeriod New Value for MaxGrayPeriod
     *
     */
    public void setMaxGrayPeriod(long aMaxGrayPeriod) {
        myMaxGrayPeriod = aMaxGrayPeriod;
    }

    /**
     * Mutator method for NotifyAppender property.
     *
     *  @param aNotifyAppender New Value for NotifyAppender
     *
     */
    public void setNotifyAppender(boolean aNotifyAppender) {
        myNotifyAppender = aNotifyAppender;
    }

    /**
     * Mutator method for OutgoingSeverityHigh property.
     *
     * @param aOutgoingSeverityHigh New Value for OutgoingSeverityHigh
     *
     */
    public void setOutgoingSeverityHigh(ArrayList<String> aOutgoingSeverityHigh) {
        myOutgoingSeverityHigh = aOutgoingSeverityHigh;
    }

    /**
     * Mutator method for OutgoingSeverityLow property.
     *
     * @param aOutgoingSeverityLow New Value for OutgoingSeverityLow
     *
     */
    public void setOutgoingSeverityLow(ArrayList<String> aOutgoingSeverityLow) {
        myOutgoingSeverityLow = aOutgoingSeverityLow;
    }

    /**
     * Mutator method for PreferredZone property.
     *
     * @param aPreferredZone New Value for Preferred Zone.
     *
     */
    public void setPreferredZone(int aPreferredZone) {
        myPreferredZone = aPreferredZone;
    }

    /**
     * Mutator method for RequestNotify property.
     *
     * @param aRequestNotify New Value for RequestNotify
     *
     */
    public void setRequestNotify(int aRequestNotify) {
        myRequestNotify = aRequestNotify;
    }

    /**
     * Mutator method for RequestNotifyLoggers property.
     *
     * @param aRequestNotifyLoggers New Value for RequestNotifyLoggers
     *
     */
    public void setRequestNotifyLoggers(boolean aRequestNotifyLoggers) {
        myRequestNotifyLoggers = aRequestNotifyLoggers;
    }

    /**
     * Mutator method for myShortcuts property.
     *
     * @param aShortcuts New Value for myShortcuts
     *
     */
    public void setShortcuts(Hashtable<String, Shortcut> aShortcuts) {
        myShortcuts = aShortcuts;
    }

    /**
     * Mutator method for Volunteer property.
     *
     * @param aVolunteer New Value for Volunteer
     *
     */
    public void setVolunteer(int aVolunteer) {
        myVolunteer = aVolunteer;
    }

    /**
     * Mutator method for WebStylesheet property.
     *
     * @param aWebStylesheet New Value for WebStylesheet
     *
     */
    public void setWebStylesheet(String aWebStylesheet) {
        myWebStylesheet = aWebStylesheet;
    }
}
