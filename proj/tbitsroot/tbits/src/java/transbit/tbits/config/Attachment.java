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
 * Attachment.java
 *
 * $Header:
 *
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

//Third party imports.
//Xerces Imports.
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Imports from the current package.
//Other TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.Helper.TBitsConstants.PKG_CONFIG;

//Static imports if any.
import static transbit.tbits.config.XMLParserUtil.getAttrBooleanValue;
import static transbit.tbits.config.XMLParserUtil.getAttrLongValue;
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
 * This class encapsulates the properties of an attachment.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public class Attachment implements Serializable {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    //~--- fields -------------------------------------------------------------

    private String myDisplayName;

    // Attributes of this Domain Object.
    private String  myId;
    private boolean myIsConverted;
    private boolean myIsExtracted;
    private String  myName;
    private String  mySize;
    private long    mySizeInBytes;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public Attachment() {}

    /**
     * The complete constructor.
     *
     *  @param aId
     *  @param aName
     *  @param aDisplayName
     *  @param aSizeInBytes
     *  @param aSize
     *  @param aIsConverted
     *  @param aIsExtracted
     */
    public Attachment(String aId, String aName, String aDisplayName, long aSizeInBytes, String aSize, boolean aIsConverted, boolean aIsExtracted) {
        myId          = aId;
        myName        = aName;
        myDisplayName = aDisplayName;
        mySizeInBytes = aSizeInBytes;
        mySize        = aSize;
        myIsConverted = aIsConverted;
        myIsExtracted = aIsExtracted;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Main method for testing.
     *
     */
    public static void main(String arg[]) throws Exception {
        try {
            java.io.BufferedReader br  = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            String                 str = "";
            StringBuilder          xml = new StringBuilder();

            while ((str = br.readLine()) != null) {
                xml.append(str);
            }

            ArrayList<Attachment> att = Attachment.getAttachments(xml.toString());

            System.out.println("\n\n" + Attachment.xmlSerialize(att));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * String representation of Attachment object.
     */
    public String toString() {
        return "[ " + this.myName + ", " + this.myDisplayName + ", " + this.mySizeInBytes + ", Converted=" + this.myIsConverted + ", Extracted=" + this.myIsExtracted + " ]";
    }

    /**
     * This method serializes the given list of attachment objects to XML.
     *
     * @param aList List of attachments.
     *
     * @return XML corresponding to the attachments.
     *
     */
    public static String xmlSerialize(ArrayList<Attachment> aList) {
        StringBuffer buffer = new StringBuffer();

        if ((aList == null) || (aList.size() == 0)) {
            return "";
        }

        buffer.append("<AttachmentInfo>");

        for (Attachment x : aList) {
            String Name        = Utilities.htmlEncode(x.getName());
            String displayName = Utilities.htmlEncode(x.getDisplayName());

            buffer.append("\n\t<Attachment id=\"").append(x.getId()).append("\" \n\t\tname=\"").append(Name).append("\" \n\t\tdisplayName=\"").append(displayName).append("\" \n\t\tsize=\"").append(
                x.getSize()).append("\" \n\t\tbytes=\"").append(x.getSizeInBytes()).append("\" \n\t\tconvert=\"").append(x.getIsConverted()).append("\" \n\t\textract=\"").append(
                x.getIsExtracted()).append("\" \n\t/>");
        }

        buffer.append("\n</AttachmentInfo>");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method parses the XML and returns a list of attachment objects.
     *
     *
     * @param aXml XML corresponding to the attachment-info.
     * @return List of attachments.
     * @exception DETBitsException
     */
    public static ArrayList<Attachment> getAttachments(String aXml) throws TBitsException {
        ArrayList<Attachment> list = new ArrayList<Attachment>();

        if ((aXml == null) || (aXml.trim().equals("") == true)) {
            return list;
        }

        DocumentBuilderFactory factory         = DocumentBuilderFactory.newInstance();
        DocumentBuilder        documentBuilder = null;
        Document               document        = null;

        try {
            documentBuilder = factory.newDocumentBuilder();
            document        = (org.w3c.dom.Document) documentBuilder.parse(new ByteArrayInputStream(aXml.getBytes()));

            NodeList rootNodeList  = document.getElementsByTagName("AttachmentInfo");
            Node     rootNode      = rootNodeList.item(0);
            NodeList childNodeList = rootNode.getChildNodes();

            for (int i = 0; i < childNodeList.getLength(); i++) {
                Attachment obj       = new Attachment();
                Node       childNode = childNodeList.item(i);

                /*
                 * <Attachment
                 *             id="1"
                 *             name="att_config.xml"
                 *             displayName="att config.xml"
                 *             size="1KB"
                 *             bytes="1000"
                 *             convert="false"
                 *             extract="true"
                 * />
                 */
                if (childNode.getNodeName().equals("Attachment")) {
                    String  id      = getAttributeValue(childNode, "id");
                    String  name    = getAttributeValue(childNode, "name");
                    String  dName   = getAttributeValue(childNode, "displayName");
                    String  size    = getAttributeValue(childNode, "size");
                    long    bytes   = getAttrLongValue(childNode, "bytes", 0);
                    boolean convert = getAttrBooleanValue(childNode, "convert");
                    boolean extract = getAttrBooleanValue(childNode, "extract");

                    obj.setId(id);
                    obj.setName(Utilities.htmlDecode(name));
                    obj.setDisplayName(Utilities.htmlDecode(dName));
                    obj.setSize(size);
                    obj.setSizeInBytes(bytes);
                    obj.setIsConverted(convert);
                    obj.setIsExtracted(extract);
                    list.add(obj);
                }
            }
        } catch (Exception e) {
            LOG.info("Attachment XML: " + aXml);

            throw new TBitsException(e.toString());
        }

        return list;
    }

    /**
     * Accessor method for DisplayName property.
     *
     * @return Current Value of DisplayName
     *
     */
    public String getDisplayName() {
        return myDisplayName;
    }

    /**
     * Accessor method for Id property.
     *
     * @return Current Value of Id
     *
     */
    public String getId() {
        return myId;
    }

    /**
     * Accessor method for IsConverted property.
     *
     * @return Current Value of IsConverted
     *
     */
    public boolean getIsConverted() {
        return myIsConverted;
    }

    /**
     * Accessor method for IsExtracted property.
     *
     * @return Current Value of IsExtracted
     *
     */
    public boolean getIsExtracted() {
        return myIsExtracted;
    }

    /**
     * Accessor method for Name property.
     *
     * @return Current Value of Name
     *
     */
    public String getName() {
        return myName;
    }

    /**
     * Accessor method for Size property.
     *
     * @return Current Value of Size
     *
     */
    public String getSize() {
        return mySize;
    }

    /**
     * Accessor method for SizeInBytes property.
     *
     * @return Current Value of SizeInBytes
     *
     */
    public long getSizeInBytes() {
        return mySizeInBytes;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for DisplayName property.
     *
     * @param aDisplayName New Value for DisplayName
     *
     */
    public void setDisplayName(String aDisplayName) {
        myDisplayName = aDisplayName;
    }

    /**
     * Mutator method for Id property.
     *
     * @param aId New Value for Id
     *
     */
    public void setId(String aId) {
        myId = aId;
    }

    /**
     * Mutator method for IsConverted property.
     *
     * @param aIsConverted New Value for IsConverted
     *
     */
    public void setIsConverted(boolean aIsConverted) {
        myIsConverted = aIsConverted;
    }

    /**
     * Mutator method for IsExtracted property.
     *
     * @param aIsExtracted New Value for IsExtracted
     *
     */
    public void setIsExtracted(boolean aIsExtracted) {
        myIsExtracted = aIsExtracted;
    }

    /**
     * Mutator method for Name property.
     *
     * @param aName New Value for Name
     *
     */
    public void setName(String aName) {
        myName = aName;
    }

    /**
     * Mutator method for Size property.
     *
     * @param aSize New Value for Size
     *
     */
    public void setSize(String aSize) {
        mySize = aSize;
    }

    /**
     * Mutator method for SizeInBytes property.
     *
     * @param aSizeInBytes New Value for SizeInBytes
     *
     */
    public void setSizeInBytes(long aSizeInBytes) {
        mySizeInBytes = aSizeInBytes;
    }
}
