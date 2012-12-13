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
 * ResourceConfig.java
 *
 * $Header:
 */
package transbit.tbits.external;

//~--- non-JDK imports --------------------------------------------------------

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.external.DBResource;
import transbit.tbits.external.Resource;
import transbit.tbits.external.ResourceAttr;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_CONFIG;
import static transbit.tbits.external.Resource.AttributeMode;
import static transbit.tbits.external.Resource.AttributeType;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//~--- classes ----------------------------------------------------------------

/**
 * This class parses the XML config that represents the external resources
 *
 * @author  Vaibhav
 * @version $Id: $
 */
public class ResourceConfig {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    //~--- methods ------------------------------------------------------------

    /**
     * Main method for testing.
     *
     * @param args
     */
    public static int main(String[] args) throws Exception {
        try {
            java.io.BufferedReader br  = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            String                 str = "";
            StringBuilder          xml = new StringBuilder();

            while ((str = br.readLine()) != null) {
                xml.append(str).append("\n");
            }

            Resource res = ResourceConfig.parseResourceConfig(xml.toString());

            if (res instanceof DBResource) {
                DBResource                dbres = (DBResource) res;
                ArrayList<ResourceAttr>   oList = dbres.getOutputAttrList();
                Hashtable<String, Object> iList = new Hashtable<String, Object>();

                iList.put("balance_id", "VAL1OFF070");
                res.realizeResource(iList);

                Iterator<ResourceResultMap> iterator = res;

                while (iterator.hasNext() == true) {
                    ResourceResultMap rrm = iterator.next();

                    LOG.info(oList.toString());

                    for (ResourceAttr attr : oList) {
                        String attrName = attr.getAttrName();

                        try {
                            Object attrValue = rrm.get(attrName);

                            LOG.info("\nName: " + attrName + "\nValue: " + attrValue);
                        } catch (Exception e) {
                            LOG.info("",(e));
                            return 1;
                        }
                    }
                }
            }
        } finally {
            
        }
        return 0;
    }

    private static DBResource parseDBResource(Node dbNode) {
        DBResource              resource = new DBResource();
        ArrayList<ResourceAttr> iList    = new ArrayList<ResourceAttr>();
        ArrayList<ResourceAttr> oList    = new ArrayList<ResourceAttr>();

        /*
         * <Database poolName="frPool">
         *     <Procedure name="stp_myProcedure" />
         *     <Attr mode="input"  type="" value="" sequence=""/>
         *     <Attr mode="output" type="" value="" sequence=""/>
         * </Database>
         *
         */
        NamedNodeMap nnmap    = null;
        Node         attrNode = null;

        // Get the poolName attribute.
        nnmap    = dbNode.getAttributes();
        attrNode = nnmap.getNamedItem("poolName");

        if (attrNode != null) {
            String poolName = attrNode.getNodeValue();

            resource.setDBPoolName(poolName);
        }

        /*
         * Now iterate through the children of this node.
         */
        NodeList children = dbNode.getChildNodes();
        int      count    = children.getLength();

        for (int i = 0; i < count; i++) {
            Node   childNode = children.item(i);
            String nodeName  = childNode.getNodeName();

            if (nodeName.equals("Procedure")) {

                // get the value of name attribute.
                nnmap    = childNode.getAttributes();
                attrNode = nnmap.getNamedItem("name");

                if (attrNode != null) {
                    String procName = attrNode.getNodeValue();

                    resource.setProcName(procName);
                }
            } else if (nodeName.equals("InputAttr")) {
                ResourceAttr attribute = new ResourceAttr();

                attribute.setAttrMode(AttributeMode.INPUT);

                /*
                 * Get the following attributes of the Input Attribute Node.
                 *   - type
                 *   - name
                 *   - value
                 *   - sequence
                 */
                nnmap = childNode.getAttributes();

                /*
                 * Type:
                 *   BOOLEAN / DATETIME / INTEGER / NUMERIC / STRING / VARIABLE
                 */
                attrNode = nnmap.getNamedItem("type");

                if (attrNode != null) {
                    String nodeValue = attrNode.getNodeValue();

                    if (nodeValue != null) {
                        nodeValue = nodeValue.trim().toUpperCase();
                        attribute.setAttrType(AttributeType.valueOf(nodeValue));
                    }    // End If Node Value not null
                }        // End If Node not null

                // Name
                attrNode = nnmap.getNamedItem("name");

                if (attrNode != null) {
                    String nodeValue = attrNode.getNodeValue();

                    if (nodeValue != null) {
                        attribute.setAttrName(nodeValue);
                    }
                }

                // value
                attrNode = nnmap.getNamedItem("value");

                if (attrNode != null) {
                    String nodeValue = attrNode.getNodeValue();

                    if (nodeValue != null) {
                        nodeValue = nodeValue.trim();
                        attribute.setValue(attribute.getAttrType(), nodeValue);
                    }    // End If Node Value Not Null
                }        // End If Node Not Null

                // Sequence of input.
                attrNode = nnmap.getNamedItem("sequence");

                if (attrNode != null) {
                    String nodeValue = attrNode.getNodeValue();

                    if (nodeValue != null) {
                        int sequence = 1;

                        try {
                            sequence = Integer.parseInt(nodeValue);
                        } catch (Exception e) {
                            LOG.info("Exception while parsing the sequence.");

                            /*
                             * Use the loop counter to get unique sequence
                             * numbers
                             */
                            sequence = i + 1;
                        }

                        attribute.setAttrSequence(sequence);
                    }
                }    // End If.

                iList.add(attribute);
            } else if (nodeName.equals("OutputAttr")) {
                ResourceAttr attribute = new ResourceAttr();

                attribute.setAttrMode(AttributeMode.OUTPUT);

                /*
                 * Get the following attributes of the Attribute Node.
                 *   - type
                 *   - name
                 */
                nnmap = childNode.getAttributes();

                /*
                 * Type:
                 *   BOOLEAN / DATETIME / INTEGER / NUMERIC / STRING / VARIABLE
                 */
                attrNode = nnmap.getNamedItem("type");

                if (attrNode != null) {
                    String nodeValue = attrNode.getNodeValue();

                    if (nodeValue != null) {
                        nodeValue = nodeValue.trim().toUpperCase();
                        attribute.setAttrType(AttributeType.valueOf(nodeValue));
                    }    // End If Node Value not null
                }        // End If Node not null

                // Name
                attrNode = nnmap.getNamedItem("name");

                if (attrNode != null) {
                    String nodeValue = attrNode.getNodeValue();

                    if (nodeValue != null) {
                        attribute.setAttrName(nodeValue);
                    }
                }

                oList.add(attribute);
            }    // End Else If Attr
        }        // End For

        resource.setInputAttrList(iList);
        resource.setOutputAttrList(oList);

        return resource;
    }

    public static Resource parseResourceConfig(String aXml) throws TBitsException {
        Resource resource = null;

        if ((aXml == null) || (aXml.trim().equals("") == true)) {
            return resource;
        }

        DocumentBuilderFactory factory         = DocumentBuilderFactory.newInstance();
        DocumentBuilder        documentBuilder = null;
        Document               document        = null;

        try {
            documentBuilder = factory.newDocumentBuilder();
            document        = (org.w3c.dom.Document) documentBuilder.parse(new ByteArrayInputStream(aXml.getBytes()));

            // <Resource> is the root element.
            NodeList rootNodeList = document.getElementsByTagName("Resource");
            Node     rootNode     = rootNodeList.item(0);
            NodeList nodeList     = rootNode.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node   node     = nodeList.item(i);
                String nodeName = node.getNodeName();

                // Check if this a DBResource
                if (nodeName.equals("Database")) {

                    // Parse this as a Database Resource.
                    resource = parseDBResource(node);
                }

                // Need to come up with other kind of resources.
                else if (nodeName.equals("")) {}
            }    // End For
        }        // End Try
                catch (Exception e) {
            LOG.warn("",(e));

            throw new TBitsException(e.toString());
        }

        return resource;
    }
}
