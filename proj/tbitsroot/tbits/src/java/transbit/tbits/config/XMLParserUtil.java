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
 * XMLParserUtil.java
 *
 *
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import transbit.tbits.common.TBitsLogger;

import static transbit.tbits.Helper.TBitsConstants.PKG_CONFIG;

//~--- classes ----------------------------------------------------------------

/**
 * This class provides utility methods to read value of attributes of nodes
 * while parsing xml.
 *
 * @author Vaibhav
 * @verion $Id: $
 */
public class XMLParserUtil {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the value of an attribute as a boolean.
     *
     * @param node              Node object.
     * @param attrName          Name of the attribute.
     * @return Value of the attribute.
     */
    public static boolean getAttrBooleanValue(Node node, String attrName) {
        String  attrValue = "";
        boolean value     = false;

        if (node == null) {
            return value;
        }

        NamedNodeMap nnmap = node.getAttributes();

        /*
         * Check if there are any attributes to this node.
         */
        if (nnmap == null) {
            LOG.info("No attributes are present for the node: " + node.getNodeName());

            return value;
        }

        Node attrNode = nnmap.getNamedItem(attrName);

        if (attrNode != null) {
            attrValue = attrNode.getNodeValue();
            attrValue = (attrValue == null)
                        ? ""
                        : attrValue.trim();
        }

        if (attrValue.equals("true") || attrValue.equals("1")) {
            value = true;
        } else {
            value = false;
        }

        return value;
    }

    /**
     * This method returns the value of an attribute as an integer.
     *
     * @param node              Node object.
     * @param attrName          Name of the attribute.
     * @param defaultValue      Default value to be returned in case no value
     *                          is found.
     * @return Value of the attribute.
     */
    public static int getAttrIntegerValue(Node node, String attrName, int defaultValue) {
        int attrValue = defaultValue;

        if (node == null) {
            return attrValue;
        }

        NamedNodeMap nnmap = node.getAttributes();

        /*
         * Check if there are any attributes to this node.
         */
        if (nnmap == null) {
            LOG.info("No attributes are present for the node: " + node.getNodeName());

            return attrValue;
        }

        Node attrNode = nnmap.getNamedItem(attrName);

        if (attrNode != null) {
            String strValue = attrNode.getNodeValue();

            try {
                attrValue = Integer.parseInt(strValue);
            } catch (NumberFormatException nfe) {
                attrValue = defaultValue;
            }
        }

        return attrValue;
    }

    /**
     * This method returns the value of the attribute as a long.
     *
     * @param node              Node object.
     * @param attrName          Name of the attribute.
     * @param defaultValue      Default value to be returned in case no value
     *                          is found.
     * @return Value of the attribute.
     */
    public static long getAttrLongValue(Node node, String attrName, long defaultValue) {
        long attrValue = defaultValue;

        if (node == null) {
            return attrValue;
        }

        NamedNodeMap nnmap = node.getAttributes();

        /*
         * Check if there are any attributes to this node.
         */
        if (nnmap == null) {
            LOG.info("No attributes are present for the node: " + node.getNodeName());

            return attrValue;
        }

        Node attrNode = nnmap.getNamedItem(attrName);

        if (attrNode != null) {
            String strValue = attrNode.getNodeValue();

            try {
                attrValue = Long.parseLong(strValue);
            } catch (NumberFormatException nfe) {
                attrValue = defaultValue;
            }
        }

        return attrValue;
    }

    /**
     * This method returns the value of specified attribute of the node.
     *
     * @param node              Node object.
     * @param attrName          Name of the attribute.
     * @return Value of the attribute.
     */
    public static String getAttributeValue(Node node, String attrName) {
        String attrValue = "";

        if (node == null) {
            return attrValue;
        }

        NamedNodeMap nnmap = node.getAttributes();

        /*
         * Check if there are any attributes to this node.
         */
        if (nnmap == null) {
            LOG.info("No attributes are present for the node: " + node.getNodeName());

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
}
