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
 * MigrateUserConfig.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

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
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.Shortcut;
import transbit.tbits.config.WebConfig;

import static transbit.tbits.domain.DataType.BOOLEAN;
import static transbit.tbits.domain.DataType.DATE;
import static transbit.tbits.domain.DataType.DATETIME;
import static transbit.tbits.domain.DataType.INT;
import static transbit.tbits.domain.DataType.USERTYPE;
import static transbit.tbits.domain.DataType.REAL;
import static transbit.tbits.domain.DataType.STRING;
import static transbit.tbits.domain.DataType.TEXT;
import static transbit.tbits.domain.DataType.TIME;
import static transbit.tbits.domain.DataType.TYPE;

//Static imports.
import static transbit.tbits.search.SearchConstants.NORMAL_VIEW;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//~--- classes ----------------------------------------------------------------

/**
 * This class migrates the user config in 5.1 to that in 6.0.
 *
 *
 * @author  : Vaibhav
 * @version : $Id: $
 */
public class MigrateUserConfig implements TBitsConstants {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    //~--- fields -------------------------------------------------------------

    private String                    myDefaultBAPrefix;
    private String                    myDriverClass;
    private String                    myDriverTag;
    private Connection                myNewCon;
    private String                    myNewDB;
    private Hashtable<String, String> myNewDescMap;

    // Maps to be build from the new database.
    private Hashtable<String, Hashtable<String, Integer>> myNewFieldMap;
    private String                                        myNewPassword;

    // Parameters of the new database
    private String                    myNewServer;
    private Hashtable<String, String> myNewTypeMap;
    private String                    myNewUser;

    // Connection objects to connect to the old and new databases
    private Connection myOldCon;
    private String     myOldDB;
    private String     myOldPassword;

    // Maps requried to be built from the old database.
    private Hashtable<Integer, String> myOldPrefixMap;

    // Parameters of the old database
    private String                    myOldServer;
    private String                    myOldUser;
    private Hashtable<String, String> myUserConfigMap;

    //~--- methods ------------------------------------------------------------

    /**
     * This method parses the XML and builds a WebConfig object on success but
     * returns null on failure.
     *
     * @param aXmlWebConfig XML String that contains the user configuration.
     * @return WebConfig object.
     */
    public WebConfig deSerialize(String aXmlWebConfig) throws Exception {
        WebConfig                   webConfig       = new WebConfig();
        Hashtable<String, BAConfig> baConfigTable   = new Hashtable<String, BAConfig>();
        DocumentBuilderFactory      factory         = DocumentBuilderFactory.newInstance();
        DocumentBuilder             documentBuilder = null;
        Document                    document        = null;

        try {
            documentBuilder = factory.newDocumentBuilder();
            document        = (org.w3c.dom.Document) documentBuilder.parse(new ByteArrayInputStream(aXmlWebConfig.getBytes()));

            NodeList rootNodeList    = document.getElementsByTagName("TBitsLite");
            Node     rootNode        = rootNodeList.item(0);
            NodeList optionsNodeList = rootNode.getChildNodes();

            for (int i = 0; i < optionsNodeList.getLength(); i++) {
                Node optionsNode = optionsNodeList.item(i);

                // Get the Home Id.
                if (optionsNode.getNodeName().equals("Home")) {
                    NamedNodeMap nnmap  = optionsNode.getAttributes();
                    Node         idNode = nnmap.getNamedItem("id");

                    try {
                        int    sysId  = Integer.parseInt(idNode.getNodeValue());
                        String prefix = myOldPrefixMap.get(sysId);

                        if (prefix != null) {
                            webConfig.setSystemPrefix(prefix);
                        } else {
                            webConfig.setSystemPrefix(myDefaultBAPrefix);
                        }
                    } catch (NumberFormatException nfe) {
                        webConfig.setSystemPrefix(myDefaultBAPrefix);
                    }
                }

                // Get the ResultsPerPage
                if (optionsNode.getNodeName().equals("PageNavigation")) {
                    NamedNodeMap nnmap  = optionsNode.getAttributes();
                    Node         idNode = nnmap.getNamedItem("rowsPerPage");

                    try {
                        int rpp = Integer.parseInt(idNode.getNodeValue());

                        webConfig.setRowsPerPage(rpp);
                    } catch (NumberFormatException nfe) {
                        LOG.info("Invalid Rows Per Page: " + nfe.toString());
                    }
                }

                // Get the IE Window Auto close option.
                if (optionsNode.getNodeName().equals("IEWindowAutoClose")) {
                    NamedNodeMap nnmap  = optionsNode.getAttributes();
                    Node         idNode = nnmap.getNamedItem("value");

                    try {
                        boolean flag = Boolean.valueOf(idNode.getNodeValue()).booleanValue();

                        webConfig.setIEAutoClose(flag);
                    } catch (NumberFormatException nfe) {
                        LOG.info("Invalid value for IEWindowAutoClose: " + nfe.toString());
                    }
                }

                // Get the Single IE Window option.
                if (optionsNode.getNodeName().equals("SingleIEWindow")) {
                    NamedNodeMap nnmap  = optionsNode.getAttributes();
                    Node         idNode = nnmap.getNamedItem("value");

                    try {
                        boolean flag = Boolean.valueOf(idNode.getNodeValue()).booleanValue();

                        webConfig.setSingleIEWindow(flag);
                    } catch (NumberFormatException nfe) {
                        LOG.info("Invalid value for SingleIEWindow: " + nfe.toString());
                    }
                }

                // Get the Action Order.
                if (optionsNode.getNodeName().equals("ActionOrder")) {
                    NamedNodeMap nnmap  = optionsNode.getAttributes();
                    Node         idNode = nnmap.getNamedItem("value");
                    String       value  = idNode.getNodeValue();

                    if ((value == null) || value.trim().equals("") || value.trim().equalsIgnoreCase("asc")) {
                        webConfig.setActionOrder(0);
                    } else {
                        webConfig.setActionOrder(1);
                    }
                }

                // Get the Date Time Format.
                if (optionsNode.getNodeName().equals("DateFormat")) {
                    NamedNodeMap nnmap = optionsNode.getAttributes();
                    Node         aNode = null;

                    aNode = nnmap.getNamedItem("listFormat");
                    webConfig.setListDateFormat(aNode.getNodeValue());
                    aNode = nnmap.getNamedItem("webFormat");
                    webConfig.setWebDateFormat(aNode.getNodeValue());
                    aNode = nnmap.getNamedItem("zone");

                    String value = aNode.getNodeValue();

                    if ((value == null) || value.trim().equals("") || value.trim().equalsIgnoreCase("site")) {
                        webConfig.setPreferredZone(SITE_ZONE);
                    } else if (value.trim().equalsIgnoreCase("local")) {
                        webConfig.setPreferredZone(LOCAL_ZONE);
                    } else if (value.trim().equalsIgnoreCase("gmt")) {
                        webConfig.setPreferredZone(GMT_ZONE);
                    } else {
                        webConfig.setPreferredZone(SITE_ZONE);
                    }
                }

                // Get the MyRequest-Page configuration.
                if (optionsNode.getNodeName().equals("MyRequests")) {
                    NamedNodeMap nnmap = optionsNode.getAttributes();
                    Node         aNode = null;

                    aNode = nnmap.getNamedItem("defaultFilter");

                    String value = aNode.getNodeValue();

                    if ((value == null) || value.trim().equals("")) {
                        webConfig.setFilter(FILTER_LOGGER);
                    } else {
                        value = value.toLowerCase().trim();

                        if (value.equals("logger")) {
                            webConfig.setFilter(FILTER_LOGGER);
                        }

                        if (value.equals("assignee")) {
                            webConfig.setFilter(FILTER_ASSIGNEE);
                        }

                        if (value.equals("subscriber")) {
                            webConfig.setFilter(FILTER_SUBSCRIBER);
                        }

                        if (value.equals("logger/assignee/subscriber")) {
                            webConfig.setFilter(FILTER_LOGGER + FILTER_ASSIGNEE + FILTER_SUBSCRIBER);
                        }
                    }
                }

                // Get the User selected Visual Style
                if (optionsNode.getNodeName().equals("Theme")) {

                    // We are not interested in migrating theme related info.
                }

                // Get the Per BA Configuration.
                if (optionsNode.getNodeName().equals("BusinessArea")) {
                    BAConfig baConfig = getBAConfig(optionsNode);

                    if (baConfig != null) {
                        String prefix = baConfig.getPrefix();

                        webConfig.setBAConfig(prefix, baConfig);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("",(e));
        }

        return webConfig;
    }

    private void establishConnections() throws Exception {

        /*
         * Obtain a Connection object to the old database.
         */
        String olddb = myOldDB + "@" + myOldServer;

        myOldCon = DataSourcePool.getConnection(myOldServer, myOldDB, myOldUser, myOldPassword, myDriverClass, myDriverTag);

        if (myOldCon == null) {
            LOG.error("Connection to " + olddb + "Failed");
            return;
            //System.exit(0);
        }

        LOG.debug("Connection to " + olddb + " established .\n");

        /*
         * Obtain a Connection object to the new database.
         */
        String newdb = myNewDB + "@" + myNewServer;

        myNewCon = DataSourcePool.getConnection(myNewServer, myNewDB, myNewUser, myNewPassword, myDriverClass, myDriverTag);

        if (myNewCon == null) {
            LOG.error("Connection to " + newdb + "Failed");
            return;
            //System.exit(0);
        }
        LOG.debug("Connection to " + newdb + "established .\n");
    }

    /**
     * Main Method For Testing.
     */
    public static void main(String[] args) {
        MigrateUserConfig muc = new MigrateUserConfig();

        muc.migrate();
    }

    public void migrate() {
        try {

            /*
             * Read the configuration file to get the details of the source and
             * target databases and the driver details.
             */
            readConfigurationFile();

            /*
             * Establish connections with the old and new databases.
             */
            establishConnections();

            /*
             * Prepare the maps required from the old database.
             */
            prepareOldMaps();

            /*
             * Prepare the maps required from the new database.
             */
            prepareNewMaps();

            /*
             * Start migrating the user configurations.
             */
            startMigration();
        } catch (Exception e) {
            LOG.info("Exception while migrating the user configurations: " + "",(e));
        }
    }

    private void prepareNewMaps() throws Exception {

        /*
         * Following are the maps to be prepared from the new database.
         */
        StringBuffer query = null;
        Statement    stmt  = null;
        ResultSet    rs    = null;

        myNewFieldMap = new Hashtable<String, Hashtable<String, Integer>>();
        query         = new StringBuffer();
        query.append("\nSELECT ").append("\n    ba.sys_prefix 'sys_prefix', ").append("\n    f.name 'name', ").append("\n    f.data_type_id 'data_type_id' ").append("\nFROM ").append(
            "\n    business_areas ba ").append("\n    JOIN fields f ").append("\n    ON ba.sys_id = f.sys_id ").append("\nWHERE ").append("\n    ba.is_active = 1").append("\n");
        stmt = myNewCon.createStatement();
        rs   = stmt.executeQuery(query.toString());

        if (rs != null) {
            while (rs.next() != false) {
                String                     sysPrefix = rs.getString("sys_prefix").toUpperCase();
                String                     fieldName = rs.getString("name");
                int                        dataType  = rs.getInt("data_type_id");
                Hashtable<String, Integer> value     = myNewFieldMap.get(sysPrefix);

                if (value == null) {
                    value = new Hashtable<String, Integer>();
                }

                value.put(fieldName, dataType);
                myNewFieldMap.put(sysPrefix, value);
            }

            rs.close();
        }

        rs = null;
        stmt.close();
        stmt         = null;
        myNewTypeMap = new Hashtable<String, String>();
        query        = new StringBuffer();
        query.append("\nSELECT ").append("\n    ba.sys_prefix 'sys_prefix', ").append("\n    f.name 'field_name', ").append("\n    t.type_id 'type_id', ").append("\n    t.name 'type_name' ").append(
            "\nFROM ").append("\n    business_areas ba ").append("\n    JOIN fields f ").append("\n    ON ba.sys_id = f.sys_id ").append("\n    JOIN types t ").append(
            "\n    ON t.sys_id = f.sys_id AND t.field_id = f.field_id").append("\nWHERE ").append("\n    ba.is_active = 1 ").append("\n");
        stmt = myNewCon.createStatement();
        rs   = stmt.executeQuery(query.toString());

        if (rs != null) {
            while (rs.next() != false) {
                String sysPrefix = rs.getString("sys_prefix").toUpperCase();
                String fieldName = rs.getString("field_name");
                String typeName  = rs.getString("type_name");
                int    typeId    = rs.getInt("type_id");
                String key       = sysPrefix + "_" + fieldName + "_" + typeId;

                myNewTypeMap.put(key, typeName);
            }

            rs.close();
        }

        rs = null;
        stmt.close();
        stmt         = null;
        myNewDescMap = new Hashtable<String, String>();
        query        = new StringBuffer();
        query.append("\nSELECT ").append("\n\tba.sys_prefix 'sys_prefix', ").append("\n\tf.name 'field_name', ").append("\n\tISNULL(fd.field_descriptor, f.name) 'descriptor'").append(
            "\nFROM ").append("\n\tbusiness_areas ba ").append("\n\tLEFT JOIN fields f ").append("\n\tON ba.sys_id = f.sys_id ").append("\n\tLEFT JOIN field_descriptors fd ").append(
            "\n\tON fd.sys_id = f.sys_id AND ").append("\n\t   fd.field_id = f.field_id AND ").append("\n\t   fd.is_primary = 1").append("\nWHERE ").append("\n\tba.is_active = 1").append("\n");
        stmt = myNewCon.createStatement();
        rs   = stmt.executeQuery(query.toString());

        if (rs != null) {
            while (rs.next() != false) {
                String sysPrefix  = rs.getString("sys_prefix").toUpperCase();
                String fieldName  = rs.getString("field_name");
                String descriptor = rs.getString("descriptor");
                String key        = sysPrefix + "_" + fieldName;

                myNewDescMap.put(key, descriptor);
            }

            rs.close();
        }

        rs = null;
        stmt.close();
        stmt = null;
    }

    private void prepareOldMaps() throws Exception {

        /*
         * Following are the maps to be prepared from the Old Database.
         *      1. SystemId  -> SysPrefix map.
         *      2. UserLogin -> WebConfig map.
         */
        StringBuffer query    = null;
        Statement    stmt     = null;
        ResultSet    rs       = null;
        int          sysCount = 0;
        int          usrCount = 0;

        myOldPrefixMap  = new Hashtable<Integer, String>();
        myUserConfigMap = new Hashtable<String, String>();
        query           = new StringBuffer();
        query.append("\nSELECT ").append("\n       sys_id, ").append("\n       sys_prefix ").append("\nFROM ").append("\n       business_areas ").append("\nWHERE ").append(
            "\n       is_active = 1 ").append("\n");
        stmt = myOldCon.createStatement();
        rs   = stmt.executeQuery(query.toString());

        if (rs != null) {
            while (rs.next() != false) {
                int    systemId  = rs.getInt("sys_id");
                String sysPrefix = rs.getString("sys_prefix").toUpperCase();

                myOldPrefixMap.put(systemId, sysPrefix);
                sysCount = sysCount + 1;
            }

            rs.close();
        }

        rs = null;
        stmt.close();
        stmt            = null;
        myUserConfigMap = new Hashtable<String, String>();
        query           = new StringBuffer();
        query.append("\nSELECT ").append("\n       user_login, ").append("\n       web_config ").append("\nFROM ").append("\n       users ").append("\nWHERE ").append(
            "\n       is_active = 1 AND ").append("\n       user_type_id = 7 ").append("\n");
        stmt = myOldCon.createStatement();
        rs   = stmt.executeQuery(query.toString());

        if (rs != null) {
            while (rs.next() != false) {
                String userLogin = rs.getString("user_login");
                String webConfig = rs.getString("web_config");

                myUserConfigMap.put(userLogin, webConfig);
                usrCount = usrCount + 1;
            }

            rs.close();
        }

        rs = null;
        stmt.close();
        stmt = null;
        LOG.info("Number of active business areas: " + sysCount + "\n");
        LOG.info("Number of active users: " + usrCount + "\n");
    }

    /**
     * This method reads the configuration file "xmigrate.rc"
     */
    private void readConfigurationFile() {
        Properties prop = new Properties();

        try {
            File            migrateConfFile = Configuration.findPath("xmigrate.rc");
            FileInputStream fis             = new FileInputStream(migrateConfFile);

            prop.load(fis);
            fis.close();
            fis = null;
        } catch (IOException ioe) {
            LOG.error("An exception occured while setting config file", ioe);
            return;
            //System.exit(1);
        } catch (NullPointerException npe) {
            LOG.error("\n\tPlease Check if xmigrate.rc is present." + "\n\tExiting Migration Process\n", npe);
            return;
            //System.exit(1);
        }

        // Properties of old database.
        myOldServer   = prop.getProperty("OLD_DB_SERVER_NAME");
        myOldDB       = prop.getProperty("OLD_DB_NAME");
        myOldUser     = prop.getProperty("OLD_DB_LOGIN");
        myOldPassword = prop.getProperty("OLD_DB_PASSWORD");

        // Properties of new database.
        myNewServer   = prop.getProperty("NEW_DB_SERVER_NAME");
        myNewDB       = prop.getProperty("NEW_DB_NAME");
        myNewUser     = prop.getProperty("NEW_DB_LOGIN");
        myNewPassword = prop.getProperty("NEW_DB_PASSWORD");

        // SQL Database Driver properties.
        myDriverClass = prop.getProperty("DRIVER_CLASS");
        myDriverTag   = prop.getProperty("DRIVER_TAG");

        // Default BA Prefix.
        myDefaultBAPrefix = prop.getProperty("DEFAULT_BA_PREFIX");
    }

    private void startMigration() throws Exception {
        Enumeration<String> userList = myUserConfigMap.keys();

        while (userList.hasMoreElements()) {
            try {
                String userLogin    = userList.nextElement();
                String oldWebConfig = myUserConfigMap.get(userLogin);

                if ((oldWebConfig == null) || (oldWebConfig.trim().equals("") == true)) {
                    LOG.info("Profile migration of " + userLogin + " is skipped as it is empty.");

                    continue;
                }

                WebConfig         configObj    = deSerialize(oldWebConfig);
                String            newWebConfig = configObj.xmlSerialize();
                CallableStatement cs           = myNewCon.prepareCall("stp_xmigrate_updateUserConfig ?, ?");

                cs.setString(1, userLogin);
                cs.setString(2, newWebConfig);
                cs.execute();
                cs.close();
                cs = null;
            } catch (Exception e) {
                LOG.info(e.toString());
            }
        }
    }

    /**
     * This method returns the type name corresponding to the given id
     */
    private String toTypeNames(String sysPrefix, String fieldName, String idList) {
        if (idList == null) {
            return "-1";
        }

        String[]     list   = idList.split(",");
        StringBuffer buffer = new StringBuffer();
        String       key    = sysPrefix + "_" + fieldName + "_";

        if (list != null) {
            boolean first = true;

            for (int i = 0; i < list.length; i++) {
                String id   = list[i];
                String name = myNewTypeMap.get(key + id);

                if (name == null) {
                    continue;
                }

                if (first == false) {
                    buffer.append(",");
                } else {
                    first = false;
                }

                buffer.append(name);
            }
        } else {
            buffer.append(",");
        }

        return buffer.toString().trim();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the BAConfig corresponding to the BAConfig Node
     * of 2.1 configuration.
     *
     * @exception Exception Incase of any errors.
     */
    private BAConfig getBAConfig(Node baNode) throws Exception {
        BAConfig     baConfig = new BAConfig();
        NamedNodeMap nnmap    = baNode.getAttributes();
        Node         idNode   = nnmap.getNamedItem("id");

        // Set the System Id.
        String strSystemId = idNode.getNodeValue();
        int    systemId    = -1;

        try {
            systemId = Integer.parseInt(strSystemId);
        } catch (Exception e) {
            LOG.info("Invalid System Id: " + strSystemId);
            systemId = -1;
        }

        String sysPrefix = myOldPrefixMap.get(systemId);

        if (sysPrefix == null) {
            return null;
        }

        baConfig.setPrefix(sysPrefix);

        NodeList baNodeList = baNode.getChildNodes();
        int      length     = baNodeList.getLength();

        for (int i = 0; i < length; i++) {
            Node aNode = baNodeList.item(i);

            if (aNode.getNodeName().equals("IsOnVacation")) {
                nnmap = aNode.getAttributes();

                Node valueNode = nnmap.getNamedItem("value");

                try {
                    boolean flag = Boolean.valueOf(valueNode.getNodeValue()).booleanValue();

                    baConfig.setVacation(flag);
                } catch (NumberFormatException nfe) {}
            }

            if (aNode.getNodeName().equals("ResultHeader")) {

                // Not interested in ResultsHeader.
            }

            if (aNode.getNodeName().equals("DisplayHeader")) {
                nnmap = aNode.getAttributes();

                Node   namesNode = nnmap.getNamedItem("names");
                String dhList    = namesNode.getNodeValue();

                baConfig.setDisplayHeader(Utilities.toArrayList(dhList));
            }

            if (aNode.getNodeName().equals("Sort")) {
                nnmap = aNode.getAttributes();

                Node   columnNode = nnmap.getNamedItem("column");
                Node   orderNode  = nnmap.getNamedItem("order");
                String sortColumn = columnNode.getNodeValue();

                baConfig.setSortField(sortColumn);

                String strSortOrder = orderNode.getNodeValue();

                try {
                    int sortOrder = Integer.parseInt(strSortOrder);

                    baConfig.setSortOrder(sortOrder);
                } catch (Exception e) {
                    LOG.info("Invalid sort order: " + e.toString());
                }
            }

            if (aNode.getNodeName().equals("MyRequests")) {
                ArrayList<String> mrList = getStatusFilters(aNode);

                baConfig.setStatusFilter(mrList);
            }

            if (aNode.getNodeName().equals("Search")) {
                getShortcuts(aNode, sysPrefix, baConfig);
            }
        }

        return baConfig;
    }

    /**
     * This method returns the Descriptor corresponding to the given field
     */
    private String getDescriptor(String sysPrefix, String fieldName) {
        String descriptor = myNewDescMap.get(sysPrefix + "_" + fieldName);

        if ((descriptor == null) || descriptor.trim().equals("")) {
            return fieldName;
        }

        return descriptor;
    }

    /**
     * This method returns the Shortcut corresponding to the Criteria Node
     * of 2.1 configuration.
     *
     * @exception Exception Incase of any errors.
     */
    private void getShortcuts(Node aNode, String sysPrefix, BAConfig baConfig) throws Exception {
        sysPrefix = sysPrefix.toUpperCase();

        Hashtable<String, Integer> fieldTable = myNewFieldMap.get(sysPrefix);

        if (fieldTable == null) {
            return;
        }

        NodeList cNodeList = aNode.getChildNodes();
        int      length    = cNodeList.getLength();

        for (int i = 0; i < length; i++) {
            Node cNode = cNodeList.item(i);

            if (cNode.getNodeName().equals("Criteria")) {
                NamedNodeMap nnmap       = cNode.getAttributes();
                Node         nameNode    = nnmap.getNamedItem("name");
                Node         defaultNode = nnmap.getNamedItem("default");

                // Get the criteria name and its default property.
                String       name         = nameNode.getNodeValue();
                String       def          = defaultNode.getNodeValue();
                boolean      isDefault    = (def.equalsIgnoreCase("true")
                                             ? true
                                             : false);
                String       filterOption = "subject";
                String       filterText   = "";
                StringBuffer query        = new StringBuffer();
                NodeList     children     = cNode.getChildNodes();
                int          cLength      = children.getLength();

                for (int j = 0; j < cLength; j++) {
                    Node node = children.item(j);

                    if (node.getNodeName().equals("Field")) {
                        nnmap = node.getAttributes();

                        Node   nNode      = nnmap.getNamedItem("name");
                        Node   tNode      = nnmap.getNamedItem("type");
                        Node   vNode      = nnmap.getNamedItem("value");
                        String fieldName  = nNode.getNodeValue();
                        String fieldType  = tNode.getNodeValue();
                        String fieldValue = vNode.getNodeValue();

                        if ((fieldName == null) || fieldName.trim().equals("")) {
                            continue;
                        }

                        // Ignore systemId
                        if (fieldName.equals("systemId")) {
                            continue;
                        }

                        Integer temp = fieldTable.get(fieldName);

                        if (temp == null) {
                            fieldName = fieldName.trim().toLowerCase();

                            if (fieldName.equals("sortcolumn")) {
                                query.append(" sortfield:").append(fieldValue);
                            } else if (fieldName.equals("sortorder")) {
                                query.append(" sortorder:").append(fieldValue);
                            } else if (fieldName.equals("subjectoption")) {
                                if (fieldValue.equals("1")) {
                                    filterOption = "subject";
                                } else if (fieldValue.equals("2")) {
                                    filterOption = "alltext";
                                } else if (fieldValue.equals("3")) {
                                    filterOption = "summary";
                                } else if (fieldValue.equals("5")) {
                                    filterOption = "all";
                                }
                            }
                        } else {
                            int    dataType = temp.intValue();
                            String desc     = myNewDescMap.get(fieldName);

                            switch (dataType) {
                            case BOOLEAN :
                                query.append(" ").append(getDescriptor(sysPrefix, fieldName)).append(":").append(fieldValue);

                                break;

                            case DATE :
                            case TIME :
                            case DATETIME :
                                break;

                            case INT :
                            case REAL :
                                query.append(" ").append(getDescriptor(sysPrefix, fieldName)).append(":").append(fieldValue);

                                break;

                            case STRING :
                            case TEXT :
                                query.append(" ").append(getDescriptor(sysPrefix, fieldName)).append(":\"").append(fieldValue).append("\"");

                                break;

                            case TYPE :
                                query.append(" ").append(getDescriptor(sysPrefix, fieldName)).append(":(").append(toTypeNames(sysPrefix, fieldName, fieldValue)).append(")");

                                break;

                            case USERTYPE : {
                                fieldValue = fieldValue.replaceAll(";", ",").trim();

                                if (fieldValue.endsWith(",")) {
                                    fieldValue = fieldValue.substring(0, fieldValue.length() - 1);
                                }

                                fieldValue = "\"" + fieldValue.replace(",", "\",\"") + "\"";
                                query.append(" ").append(getDescriptor(sysPrefix, fieldName)).append(":(").append(fieldValue).append(")");
                            }

                            break;
                            }
                        }
                    }
                }

                Shortcut sc = new Shortcut();

                sc.setName(name);
                sc.setView(NORMAL_VIEW);
                sc.setQuery(query.toString().trim());
                sc.setText(filterText);
                sc.setFilter(filterOption);
                sc.setIsDefault(isDefault);
                sc.setIsPublic(false);
                baConfig.addShortcut(sc);
            }
        }

        return;
    }

    /**
     * This method returns the MyRequests corresponding to that
     * of 2.1 configuration.
     *
     * @exception Exception Incase of any errors.
     */
    private static ArrayList<String> getStatusFilters(Node sfNode) throws Exception {
        ArrayList<String> list       = new ArrayList<String>();
        NamedNodeMap      nnmap      = null;
        NodeList          sfNodeList = sfNode.getChildNodes();
        int               length     = sfNodeList.getLength();

        for (int i = 0; i < length; i++) {
            Node aNode = sfNodeList.item(i);

            if (aNode.getNodeName().equals("Status")) {
                nnmap = aNode.getAttributes();

                Node   idNode = nnmap.getNamedItem("id");
                String value  = idNode.getNodeValue();

                if ((value != null) &&!value.trim().equals("-1")) {
                    Node nameNode = nnmap.getNamedItem("name");

                    value = nameNode.getNodeValue();

                    if ((value != null) && (value.trim().equals("") == false)) {
                        list.add(nameNode.getNodeValue());
                    }
                }
            }
        }

        return list;
    }
}
