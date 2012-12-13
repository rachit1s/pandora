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
 * WebConfig.java
 *
 * $Header:
 *
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

//Imports from the current package.
//Other TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.config.XMLParserUtil.getAttrBooleanValue;
import static transbit.tbits.config.XMLParserUtil.getAttrIntegerValue;
import static transbit.tbits.config.XMLParserUtil.getAttributeValue;

//Static Imports.
import static transbit.tbits.search.SearchConstants.NORMAL_VIEW;

//Rendering types.
import static transbit.tbits.search.SearchConstants.RenderType;

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
 * This class encapsulates the configuration properties of a user.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class WebConfig implements TBitsConstants, Serializable {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    //~--- fields -------------------------------------------------------------

    private boolean                     myActionHeader;
    private int                         myActionOrder;
    private int                         myAutoSaveRate;
    private Hashtable<String, BAConfig> myBAConfigs;
    private int                         myDefaultView;
    private int                         myFilter;
    private boolean                     myIEAutoClose;
    private String                      myListDateFormat;
    private int                         myPreferredZone;
    private int                         myRefreshRate;
    private RenderType                  myRenderType;
    private int                         myRowsPerPage;
    private boolean                     mySimpleView;
    private boolean                     mySingleIEWindow;

    // Attributes of this Object.
    private String  mySystemPrefix;
    private boolean myThreadView;
    private String  myWebDateFormat;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public WebConfig() {
        mySystemPrefix   = "TBits";
        myFilter         = FILTER_LOGGER;
        myRowsPerPage    = 100;
        myRefreshRate    = -1;
        myRenderType     = RenderType.RENDER_HIER;
        mySimpleView     = false;
        myDefaultView    = NORMAL_VIEW;
        myIEAutoClose    = false;
        mySingleIEWindow = false;
        myActionOrder    = DESC_ORDER;
        myActionHeader   = true;
        myThreadView     = false;
        myListDateFormat = DEFAULT_LIST_FORMAT;
        myWebDateFormat  = DEFAULT_WEB_FORMAT;
        myPreferredZone  = SITE_ZONE;
        myAutoSaveRate   = DEFAULT_AUTO_SAVE_RATE;
        myBAConfigs      = new Hashtable<String, BAConfig>();
    }

    /**
     * The complete constructor.
     *
     *  @param aSystemPrefix
     *  @param aFilter
     *  @param aRowsPerPage
     *  @param aRefreshRate
     *  @param aRenderType
     *  @param aDefaultView
     *  @param aIEAutoClose
     *  @param aSingleIEWindow
     *  @param aActionOrder
     *  @param aActionHeader
     *  @param aThreadView
     *  @param aListDateFormat
     *  @param aWebDateFormat
     *  @param aPreferredZone
     *  @param aAutoSaveRate
     *  @param aBAConfigs
     */
    public WebConfig(String aSystemPrefix, int aFilter, int aRowsPerPage, int aRefreshRate, RenderType aRenderType, boolean aSimpleView, int aDefaultView, boolean aIEAutoClose,
                     boolean aSingleIEWindow, int aActionOrder, boolean aActionHeader, boolean aThreadView, String aListDateFormat, String aWebDateFormat, int aPreferredZone, int aAutoSaveRate,
                     Hashtable<String, BAConfig> aBAConfigs) {
        mySystemPrefix   = aSystemPrefix;
        myFilter         = aFilter;
        myRowsPerPage    = aRowsPerPage;
        myRefreshRate    = aRefreshRate;
        myRenderType     = aRenderType;
        mySimpleView     = aSimpleView;
        myDefaultView    = aDefaultView;
        myIEAutoClose    = aIEAutoClose;
        mySingleIEWindow = aSingleIEWindow;
        myActionOrder    = aActionOrder;
        myActionHeader   = aActionHeader;
        myThreadView     = aThreadView;
        myListDateFormat = aListDateFormat;
        myWebDateFormat  = aWebDateFormat;
        myPreferredZone  = aPreferredZone;
        myAutoSaveRate   = aAutoSaveRate;
        myBAConfigs      = aBAConfigs;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * main method.
     */
    public static void main(String arg[]) {
        try {
            java.io.BufferedReader br  = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            String                 str = "";
            StringBuilder          xml = new StringBuilder();

            while ((str = br.readLine()) != null) {
                xml.append(str);
            }

            WebConfig webConfig = WebConfig.getWebConfig(xml.toString());

            System.out.println("\n\n" + webConfig.xmlSerialize());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns the XML-Serialized representation of this webconfig.
     *
     * @return XML Serialized string representation of this object.
     */
    public String xmlSerialize() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n<TBitsLite>").append("\n\t<Home prefix=\"").append(mySystemPrefix).append("\" />").append("\n\t<MyRequests filter=\"").append(myFilter).append("\" />\n\t").append(
            "<Search rowsPerPage=\"").append(myRowsPerPage).append("\" refreshRate=\"").append(myRefreshRate).append("\" renderType=\"").append(myRenderType).append("\" simpleView=\"").append(
            mySimpleView).append("\" />").append("\n\t<IEWindow autoClose=\"").append(myIEAutoClose).append("\" single=\"").append(mySingleIEWindow).append("\" view=\"").append(myDefaultView).append(
            "\" />").append("\n\t<Action sortOrder=\"").append(myActionOrder).append("\" showHeader=\"").append(myActionHeader).append("\" threadView=\"").append(myThreadView).append("\" />").append(
            "\n\t<DateFormat listFormat=\"").append(myListDateFormat).append("\" webFormat=\"").append(myWebDateFormat).append("\" zone=\"").append(myPreferredZone).append("\" />").append(
            "\n\t<Draft autoSaveRate=\"").append(myAutoSaveRate).append("\" />").append(xmlSerializeBAConfig()).append("\n</TBitsLite>");

        return buffer.toString();
    }

    /**
     * This method returns the XML-Serialized representation of this BAConfigs.
     *
     * @return XML Serialized string representation of this object.
     */
    public String xmlSerializeBAConfig() {
        StringBuilder xml = new StringBuilder();

        if (myBAConfigs == null) {
            return xml.toString();
        }

        ArrayList<BAConfig> list = new ArrayList<BAConfig>(myBAConfigs.values());

        for (BAConfig config : list) {
            xml.append("\n\t<BusinessArea prefix=\"").append(config.getPrefix().toUpperCase()).append("\">").append("\n\t\t<Vacation value=\"").append(config.getVacation()).append("\" />").append(
                "\n\t\t").append("<EnableVE enable=\"").append(config.getEnableVE()).append("\" />\n\t\t").append("<Notify notify=\"").append(config.getNotify()).append("\" />\n\t\t").append(
                "<DisplayHeader list=\"").append(Utilities.toCSS(config.getDisplayHeader())).append("\" />").append("\n\t\t<MyRequests show=\"").append(config.getShowBA()).append(
                "\" filter=\"").append(config.getRoleFilter()).append("\" collapse=\"").append(config.getCollapseBA()).append("\">").append(xmlSerializeStatusFilter(config.getStatusFilter())).append(
                "\n\t\t</MyRequests>").append("\n\t\t<Sort field=\"").append(config.getSortField()).append("\" order=\"").append(config.getSortOrder()).append("\" />").append(
                "\n\t\t<DefaultShortcut value=\"").append(Utilities.htmlEncode(config.getDefaultShortcutName())).append("\" />").append(xmlSerializeShortcuts(config.getShortcuts())).append(
                "\n\t</BusinessArea>");
        }

        return xml.toString();
    }

    /**
     * This method returns the XML-Serialized representation of shortcuts.
     *
     * @return XML Serialized string representation of this object.
     */
    private String xmlSerializeShortcuts(Hashtable<String, Shortcut> aTable) {
        StringBuilder xml = new StringBuilder();

        if (aTable == null) {
            return xml.toString();
        }

        Hashtable<String, Shortcut> table = new Hashtable<String, Shortcut>();

        table.putAll(aTable);

        ArrayList<Shortcut> list = new ArrayList<Shortcut>(table.values());

        for (Shortcut sc : list) {
            xml.append(sc.xmlSerialize());
        }

        return xml.toString();
    }

    /**
     * This method returns the XML-Serialized representation of shortcuts.
     *
     * @return XML Serialized string representation of this object.
     */
    private String xmlSerializeStatusFilter(ArrayList<String> aList) {
        StringBuilder xml = new StringBuilder();

        if ((aList == null) || (aList.size() == 0)) {
            return xml.toString();
        }

        for (String filter : aList) {
            xml.append("\n\t\t\t<Status value=\"").append(Utilities.htmlEncode(filter)).append("\" />");
        }

        return xml.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for ActionHeader property.
     *
     * @return Current Value of ActionHeader
     *
     */
    public boolean getActionHeader() {
        return myActionHeader;
    }

    /**
     * Accessor method for ActionOrder property.
     *
     * @return Current Value of ActionOrder
     *
     */
    public int getActionOrder() {
        return myActionOrder;
    }

    /**
     * Accessor method for AutoSaveRate property.
     *
     * @return Current Value of AutoSaveRate
     *
     */
    public int getAutoSaveRate() {
        return myAutoSaveRate;
    }

    /**
     * This method parses the BAConfig portion of the XML and returns the
     * object.
     *
     * @param aNode Node that points to BusinessArea Node.
     * x
     */
    private static BAConfig getBAConfig(Node aNode) {
        BAConfig baConfig = new BAConfig();
        String   prefix   = getAttributeValue(aNode, "prefix");

        baConfig.setPrefix(prefix);

        Hashtable<String, Shortcut> shortcuts = new Hashtable<String, Shortcut>();

        try {
            NodeList children = aNode.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node childNode = children.item(i);

                // <Vacation value="true" />
                if (childNode.getNodeName().equals("Vacation")) {
                    boolean vacation = getAttrBooleanValue(childNode, "value");

                    baConfig.setVacation(vacation);
                } else if (childNode.getNodeName().equals("EnableVE")) {
                    boolean enable = getAttrBooleanValue(childNode, "enable");

                    baConfig.setEnableVE(enable);
                } else if (childNode.getNodeName().equals("Notify")) {
                    boolean notify = getAttrBooleanValue(childNode, "notify");

                    baConfig.setNotify(notify);
                } else if (childNode.getNodeName().equals("DisplayHeader")) {
                    String list = getAttributeValue(childNode, "list");

                    baConfig.setDisplayHeader(Utilities.toArrayList(list));
                } else if (childNode.getNodeName().equals("MyRequests")) {
                    int filter = getAttrIntegerValue(childNode, "filter", FILTER_LOGGER);

                    baConfig.setRoleFilter(filter);

                    boolean show = getAttrBooleanValue(childNode, "show");

                    baConfig.setShowBA(show);

                    boolean collapse = getAttrBooleanValue(childNode, "collapse");

                    baConfig.setCollapseBA(collapse);

                    ArrayList<String> sfList = getStatusFilter(childNode);

                    baConfig.setStatusFilter(sfList);
                } else if (childNode.getNodeName().equals("Sort")) {
                    String field = getAttributeValue(childNode, "field");

                    baConfig.setSortField(field);

                    int sortOrder = getAttrIntegerValue(childNode, "order", DESC_ORDER);

                    baConfig.setSortOrder(sortOrder);
                } else if (childNode.getNodeName().equals("Shortcut")) {
                    Shortcut sc = Shortcut.parseShortcutNode(childNode);

                    shortcuts.put(sc.getName().toUpperCase(), sc);

                    if (sc.getIsDefault() == true) {

                        // Set this with this key for easy access.
                        baConfig.setDefaultShortcutName(sc.getName());
                    }
                } else if (childNode.getNodeName().equals("DefaultShortcut")) {
                    String name = getAttributeValue(childNode, "value");

                    baConfig.setDefaultShortcutName(Utilities.htmlDecode(name));
                }
            }

            baConfig.setShortcuts(shortcuts);
        } catch (Exception e) {
            LOG.warn("",(e));
        }

        return baConfig;
    }

    /**
     * This method returns the BAConfig corresponding to the given sys prefix
     * if it exists or the default one otherwise.
     *
     * @param  aPrefix
     * @return BAConfig
     *
     */
    public BAConfig getBAConfig(String aPrefix) {
        BAConfig baConfig = myBAConfigs.get(aPrefix.toUpperCase());

        if (baConfig == null) {
            baConfig = new BAConfig();
            baConfig.setPrefix(aPrefix.toUpperCase());
        }

        return baConfig;
    }

    /**
     * Accessor method for BAConfigs property.
     *
     * @return Current Value of BAConfigs
     *
     */
    public Hashtable<String, BAConfig> getBAConfigs() {
        return myBAConfigs;
    }

    /**
     * Accessor method for DefaultView property.
     *
     * @return Current Value of DefaultView
     *
     */
    public int getDefaultView() {
        return myDefaultView;
    }

    /**
     * Accessor method for Filter property.
     *
     * @return Current Value of Filter
     *
     */
    public int getFilter() {
        return myFilter;
    }

    /**
     * Accessor method for IEAutoClose property.
     *
     * @return Current Value of IEAutoClose
     *
     */
    public boolean getIEAutoClose() {
        return myIEAutoClose;
    }

    /**
     * Accessor method for ListDateFormat property.
     *
     * @return Current Value of ListDateFormat
     *
     */
    public String getListDateFormat() {
        return myListDateFormat;
    }

    /**
     * Accessor method for PreferredZone property.
     *
     * @return Current Value of PreferredZone
     *
     */
    public int getPreferredZone() {
        return myPreferredZone;
    }

    /**
     * Accessor method for RefreshRate property.
     *
     * @return Current Value of RefreshRate
     *
     */
    public int getRefreshRate() {
        return myRefreshRate;
    }

    /**
     * Accessor method for RenderType property.
     *
     * @return Current Value of RenderType
     *
     */
    public RenderType getRenderType() {
        return myRenderType;
    }

    /**
     * Accessor method for RowsPerPage property.
     *
     * @return Current Value of RowsPerPage
     *
     */
    public int getRowsPerPage() {
        return myRowsPerPage;
    }

    /**
     * Accessor method for SimpleView property.
     *
     * @return Current Value of SimpleView
     *
     */
    public boolean getSimpleView() {
        return mySimpleView;
    }

    /**
     * Accessor method for SingleIEWindow property.
     *
     * @return Current Value of SingleIEWindow
     *
     */
    public boolean getSingleIEWindow() {
        return mySingleIEWindow;
    }

    /**
     * This method parses the MyRequests portion of the XML and returns the
     * object.
     *
     * @param aNode Node that points to MyRequests Node.
     * x
     */
    private static ArrayList<String> getStatusFilter(Node aNode) {
        ArrayList<String> sfList = new ArrayList<String>();

        try {
            NodeList children = aNode.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node childNode = children.item(i);

                /*
                 * <Status
                 *      value="true"
                 * />
                 */
                if (childNode.getNodeName().equals("Status")) {
                    String value = getAttributeValue(childNode, "value");

                    sfList.add(value);
                }
            }
        } catch (Exception e) {
            LOG.warn("",(e));
        }

        return sfList;
    }

    /**
     * Accessor method for SystemPrefix property.
     *
     * @return Current Value of SystemPrefix
     *
     */
    public String getSystemPrefix() {
        return mySystemPrefix;
    }

    /**
     * Accessor method for ThreadView property.
     *
     * @return Current Value of ThreadView
     *
     */
    public boolean getThreadView() {
        return myThreadView;
    }

    /**
     * This static method is factory method to build a webConfig object from
     * given XML.
     *
     *
     * @param aXml XML that represents the WebConfig in the database.
     * @return WebConfig object corresponding to the given XML.
     * @exception DETBitsExceptionncase of any exception during xml-parsing.
     */
    public static WebConfig getWebConfig(String aXml) throws TBitsException {
        WebConfig webConfig = new WebConfig();

        if ((aXml == null) || (aXml.trim().equals("") == true)) {
            return webConfig;
        }

        Hashtable<String, BAConfig> configs         = new Hashtable<String, BAConfig>();
        DocumentBuilderFactory      factory         = DocumentBuilderFactory.newInstance();
        DocumentBuilder             documentBuilder = null;
        Document                    document        = null;

        try {
            documentBuilder = factory.newDocumentBuilder();
            if(!aXml.startsWith("<?xml"))
            	aXml = "<?xml version='1.0' encoding='ISO-8859-1'?>" + aXml;
            
            document        = (org.w3c.dom.Document) documentBuilder.parse(new ByteArrayInputStream(aXml.getBytes()));

            // <TBitsLite> is the root element.
            NodeList rootNodeList = document.getElementsByTagName("TBitsLite");
            Node     rootNode     = rootNodeList.item(0);
            NodeList children     = rootNode.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node childNode = children.item(i);

                /*
                 * <Home prefix="TBits" />
                 */
                if (childNode.getNodeName().equals("Home")) {
                    NamedNodeMap nnmap    = childNode.getAttributes();
                    Node         attrNode = null;
                    String       prefix   = getAttributeValue(childNode, "prefix");

                    webConfig.setSystemPrefix(prefix.trim());
                }

                /*
                 * <MyRequests filter="1" />
                 * FILTER_LOGGER                    = 1;
                 * FILTER_ASSIGNEE                  = 2;
                 * FILTER_SUBSCRIBER                = 4;
                 * FILTER_PRIMARY_ASSIGNEE          = 8;
                 */
                else if (childNode.getNodeName().equals("MyRequests")) {
                    int filter = getAttrIntegerValue(childNode, "filter", FILTER_LOGGER);

                    webConfig.setFilter(filter);
                }

                /*
                 * <Search
                 *      rowsPerPage="20"
                 *      refreshRate="-1"
                 *      renderType="2"
                 *      simpleView='true'
                 *  />
                 */
                else if (childNode.getNodeName().equals("PageNavigation") || childNode.getNodeName().equals("Search")) {
                    int rows = getAttrIntegerValue(childNode, "rowsPerPage", 20);

                    webConfig.setRowsPerPage(rows);

                    int rate = getAttrIntegerValue(childNode, "refreshRate", -1);

                    webConfig.setRefreshRate(rate);

                    boolean simpleView = getAttrBooleanValue(childNode, "simpleView");

                    webConfig.setSimpleView(simpleView);

                    String     strRenderType = getAttributeValue(childNode, "renderType");
                    RenderType renderType    = RenderType.RENDER_HIER;

                    try {
                        renderType = RenderType.toRenderType(strRenderType);
                    } catch (Exception e) {
                        renderType = RenderType.RENDER_HIER;
                    }

                    webConfig.setRenderType(renderType);
                }

                /*
                 * <IEWindow
                 *      autoClose="false"
                 *      single="false"
                 *      view="2"
                 * />
                 */
                else if (childNode.getNodeName().equals("IEWindow")) {
                    boolean autoClose = getAttrBooleanValue(childNode, "autoClose");
                    boolean single    = getAttrBooleanValue(childNode, "single");
                    int     view      = getAttrIntegerValue(childNode, "view", NORMAL_VIEW);

                    webConfig.setDefaultView(view);
                    webConfig.setIEAutoClose(autoClose);
                    webConfig.setSingleIEWindow(single);
                }

                /*
                 * <Action
                 *         sortOrder="desc"
                 *         showHeader="false"
                 *         threadView="true"
                 * />
                 */
                else if (childNode.getNodeName().equals("Action")) {
                    int     sortOrder  = getAttrIntegerValue(childNode, "sortOrder", DESC_ORDER);
                    boolean showHeader = getAttrBooleanValue(childNode, "showHeader");
                    boolean threadView = getAttrBooleanValue(childNode, "threadView");

                    webConfig.setActionOrder(sortOrder);
                    webConfig.setActionHeader(showHeader);
                    webConfig.setThreadView(threadView);
                }

                /*
                 * <DateFormat
                 *         listFormat="MM/dd/yyyy"
                 *         webFormat="MM/dd/yyyy hh:mm Z"
                 *         zone="1"
                 *  />
                 */
                else if (childNode.getNodeName().equals("DateFormat")) {
                    String listFormat = getAttributeValue(childNode, "listFormat");

                    webConfig.setListDateFormat(listFormat);

                    String webFormat = getAttributeValue(childNode, "webFormat");

                    webConfig.setWebDateFormat(webFormat);

                    int zone = getAttrIntegerValue(childNode, "zone", SITE_ZONE);

                    webConfig.setPreferredZone(zone);
                }

                /*
                 * <Draft autoSaveRate="5" />
                 */
                else if (childNode.getNodeName().equals("Draft")) {
                    int rate = getAttrIntegerValue(childNode, "autoSaveRate", -1);

                    webConfig.setAutoSaveRate(rate);
                }

                /*
                 * <BusinessArea>
                 */
                else if (childNode.getNodeName().equals("BusinessArea")) {
                    BAConfig config = getBAConfig(childNode);

                    configs.put(config.getPrefix().toUpperCase(), config);
                }
            }

            webConfig.setBAConfigs(configs);
        } catch (Exception e) {
            LOG.warn("",(e));

            throw new TBitsException(e.toString());
        }

        return webConfig;
    }

    /**
     * Accessor method for WebDateFormat property.
     *
     * @return Current Value of WebDateFormat
     *
     */
    public String getWebDateFormat() {
        return myWebDateFormat;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for ActionHeader property.
     *
     * @param aActionHeader New Value for ActionHeader
     *
     */
    public void setActionHeader(boolean aActionHeader) {
        myActionHeader = aActionHeader;
    }

    /**
     * Mutator method for ActionOrder property.
     *
     * @param aActionOrder New Value for ActionOrder
     *
     */
    public void setActionOrder(int aActionOrder) {
        myActionOrder = aActionOrder;
    }

    /**
     * Mutator method for AutoSaveRate property.
     *
     * @param aAutoSaveRate New Value for AutoSaveRate
     *
     */
    public void setAutoSaveRate(int aAutoSaveRate) {
        myAutoSaveRate = aAutoSaveRate;
    }

    /**
     * This method updates the BAConfig corresponding to the given sys prefix
     * in this object
     *
     * @param  aPrefix
     * @param  aBAConfig
     *
     */
    public void setBAConfig(String aPrefix, BAConfig aBAConfig) {
        if (aBAConfig != null) {
            myBAConfigs.put(aPrefix.toUpperCase(), aBAConfig);
        }

        return;
    }

    /**
     * Mutator method for BAConfigs property.
     *
     * @param aBAConfigs New Value of BAConfigs.
     */
    public void setBAConfigs(Hashtable<String, BAConfig> aBAConfigs) {
        myBAConfigs = aBAConfigs;
    }

    /**
     * Mutator method for DefaultView property.
     *
     * @param aDefaultView New Value for DefaultView
     *
     */
    public void setDefaultView(int aDefaultView) {
        myDefaultView = aDefaultView;
    }

    /**
     * Mutator method for Filter property.
     *
     * @param aFilter New Value for Filter
     *
     */
    public void setFilter(int aFilter) {
        myFilter = aFilter;
    }

    /**
     * Mutator method for IEAutoClose property.
     *
     * @param aIEAutoClose New Value for IEAutoClose
     *
     */
    public void setIEAutoClose(boolean aIEAutoClose) {
        myIEAutoClose = aIEAutoClose;
    }

    /**
     * Mutator method for ListDateFormat property.
     *
     * @param aListDateFormat New Value for ListDateFormat
     *
     */
    public void setListDateFormat(String aListDateFormat) {
        myListDateFormat = aListDateFormat;
    }

    /**
     * Mutator method for PreferredZone property.
     *
     * @param aPreferredZone New Value for PreferredZone
     *
     */
    public void setPreferredZone(int aPreferredZone) {
        myPreferredZone = aPreferredZone;
    }

    /**
     * Mutator method for RefreshRate property.
     *
     * @param aRefreshRate New Value for RefreshRate
     *
     */
    public void setRefreshRate(int aRefreshRate) {
        myRefreshRate = aRefreshRate;
    }

    /**
     * Mutator method for RenderType property.
     *
     * @param aRenderType New Value for RenderType
     *
     */
    public void setRenderType(RenderType aRenderType) {
        myRenderType = aRenderType;
    }

    /**
     * Mutator method for RowsPerPage property.
     *
     * @param aRowsPerPage New Value for RowsPerPage
     *
     */
    public void setRowsPerPage(int aRowsPerPage) {
        myRowsPerPage = aRowsPerPage;
    }

    /**
     * Mutator method for SimpleView property.
     *
     * @param aSimpleView New Value for SimpleView
     *
     */
    public void setSimpleView(boolean aSimpleView) {
        mySimpleView = aSimpleView;
    }

    /**
     * Mutator method for SingleIEWindow property.
     *
     * @param aSingleIEWindow New Value for SingleIEWindow
     *
     */
    public void setSingleIEWindow(boolean aSingleIEWindow) {
        mySingleIEWindow = aSingleIEWindow;
    }

    /**
     * Mutator method for SystemPrefix property.
     *
     * @param aSystemPrefix New Value for SystemPrefix
     *
     */
    public void setSystemPrefix(String aSystemPrefix) {
        mySystemPrefix = aSystemPrefix;
    }

    /**
     * Mutator method for ThreadView property.
     *
     * @param aThreadView New Value for ThreadView
     *
     */
    public void setThreadView(boolean aThreadView) {
        myThreadView = aThreadView;
    }

    /**
     * Mutator method for WebDateFormat property.
     *
     * @param aWebDateFormat New Value for WebDateFormat
     *
     */
    public void setWebDateFormat(String aWebDateFormat) {
        myWebDateFormat = aWebDateFormat;
    }
}
