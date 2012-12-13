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
 * FBForm.java
 *
 * $Header:
 *
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

//Third party imports.
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Static imports.
import static transbit.tbits.config.XMLParserUtil.getAttrBooleanValue;
import static transbit.tbits.config.XMLParserUtil.getAttributeValue;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//~--- classes ----------------------------------------------------------------

public class FBForm implements Serializable{
    private FBAction           myAction;
    private ArrayList<FBField> myFieldList;
    private int                myMaxFieldNameLength;
    private String             myName;
    private String             myShortName;
    private String             myTitle;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    private FBForm() {
        myName      = "";
        myShortName = "";
        myTitle     = "";
        myFieldList = new ArrayList<FBField>();
        myAction    = new FBAction();
    }

    /**
     * The complete constructor.
     *
     *  @param aName
     *  @param aTitle
     *  @param aFieldList
     *  @param aAction
     */
    public FBForm(String aName, String aShortName, String aTitle, ArrayList<FBField> aFieldList, FBAction aAction) {
        myName      = aName;
        myShortName = aShortName;
        myTitle     = aTitle;
        myFieldList = aFieldList;
        myAction    = aAction;
    }

    //~--- methods ------------------------------------------------------------

    public void addToFieldList(FBField field) {
        if (field != null) {
            myFieldList.add(field);
        }
    }

    /**
     * @param args
     */
    public static int main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: FBForm <XML>");
            return 1;
        }

        try {
            BufferedReader br     = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
            StringBuffer   buffer = new StringBuffer();
            String         str    = "";

            while ((str = br.readLine()) != null) {
                buffer.append(str).append("\n");
            }

            br.close();

            // LOG.info("XML: " + buffer.toString());
            FBForm form = FBForm.parseFormConfig(buffer.toString());

            System.out.println(form.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    /**
     * This method traverses the tree below the given node to form an FBAction
     * object.
     *
     * @param actionNode The tree of this node corresponds to Action.
     *
     * @return FBAction object.
     */
    private static FBAction parseFBAction(Node actionNode) {
        FBAction action = new FBAction();

        // Return the empty action if the node is null.
        if (actionNode == null) {
            return null;
        }

        NodeList childNodes = actionNode.getChildNodes();
        int      length     = childNodes.getLength();

        for (int i = 0; i < length; i++) {
            Node   childNode = childNodes.item(i);
            String nodeName  = childNode.getNodeName();

            /*
             * <From email="" />
             */
            if (nodeName.equals("From")) {
                String email = getAttributeValue(childNode, "email");

                if ((email != null) && (email.trim().equals("") == false)) {
                    action.setFrom(email);
                }
            }

            /*
             * <To email="" />
             */
            else if (nodeName.equals("To")) {
                String email = getAttributeValue(childNode, "email");

                if ((email != null) && (email.trim().equals("") == false)) {
                    action.addtoToList(email);
                }
            }

            /*
             * <Cc email="" />
             */
            else if (nodeName.equals("Cc")) {
                String email = getAttributeValue(childNode, "email");

                if ((email != null) && (email.trim().equals("") == false)) {
                    action.addtoCcList(email);
                }
            }

            /*
             * <Subscriber email="" />
             */
            else if (nodeName.equals("Subscriber")) {
                String email = getAttributeValue(childNode, "email");

                if ((email != null) && (email.trim().equals("") == false)) {
                    action.addtoSubscriberList(email);
                }
            }

            /*
             * <Subject> #cdata-section </Subject>
             */
            else if (nodeName.equals("Subject")) {
                String subject = getCDATASection(childNode);

                subject = ((subject == null)
                           ? ""
                           : subject.trim());
                action.setSubject(subject);
            }

            /*
             * <Description> #cdata-section </Description>
             */
            else if (nodeName.equals("Description")) {
                String description = getCDATASection(childNode);

                description = (description == null)
                              ? ""
                              : description.trim();
                action.setDescription(description);
            }
        }

        return action;
    }

    /**
     *
     * @param fieldNode
     * @return
     */
    private static FBField parseFBField(Node fieldNode) {
        FBField field = null;

        if (fieldNode == null) {
            return field;
        }

        String fieldNodeName = fieldNode.getNodeName();

        if ((fieldNodeName == null) || fieldNodeName.trim().equals("")) {
            return null;
        }

        fieldNodeName = fieldNodeName.trim();

        if (fieldNodeName.equals("Attachment")) {
            String label = getAttributeValue(fieldNode, "label");

            field = FBField.getAttachmentField(label);
        } else if (fieldNodeName.equals("BulletedList")) {

            /*
             * <BulletedList>
             *    <Heading><![CDATA[ .....]]></Heading>
             *    <Bullet><![CDATA[ .....]]></Bullet>
             *    <Bullet><![CDATA[ .....]]></Bullet>
             * </BulletedList>
             */
            NodeList          children  = fieldNode.getChildNodes();
            int               length    = children.getLength();
            String            heading   = "";
            ArrayList<String> pointList = new ArrayList<String>();

            for (int i = 0; i < length; i++) {
                Node   node     = children.item(i);
                String nodeName = node.getNodeName();

                if (nodeName.equals("Heading")) {
                    heading = getCDATASection(node);
                } else if (nodeName.equals("Bullet")) {
                    String point = getCDATASection(node);

                    pointList.add(point);
                }
            }

            field = FBField.getBulletListField(heading, pointList);
        } else if (fieldNodeName.equals("CheckBox")) {

            /*
             * <CheckBox label="" baField=""/>
             */
            String label   = getAttributeValue(fieldNode, "label");
            String name    = getAttributeValue(fieldNode, "name");
            String baField = getAttributeValue(fieldNode, "baField");

            field = FBField.getCheckBoxField(name, label, baField);
        } else if (fieldNodeName.equals("Date")) {

            /*
             * <Date name="" label="" toolTip="" validate="" baField=""/>
             */
            String  name     = getAttributeValue(fieldNode, "name");
            String  label    = getAttributeValue(fieldNode, "label");
            String  width    = getAttributeValue(fieldNode, "width");
            String  toolTip  = getAttributeValue(fieldNode, "toolTip");
            String  baField  = getAttributeValue(fieldNode, "baField");
            boolean validate = getAttrBooleanValue(fieldNode, "validate");

            field = FBField.getDateField(name, label, width, toolTip, baField, validate);
        } else if (fieldNodeName.equals("DynamicList")) {

            /*
             * <DynamicList name="" label="" toolTip="" baField="" />
             */
            String name    = getAttributeValue(fieldNode, "name");
            String label   = getAttributeValue(fieldNode, "label");
            String width   = getAttributeValue(fieldNode, "width");
            String toolTip = getAttributeValue(fieldNode, "toolTip");
            String baField = getAttributeValue(fieldNode, "baField");

            field = FBField.getDynamicListField(name, label, width, toolTip, baField);
        } else if (fieldNodeName.equals("Paragraph")) {

            /*
             * <Paragraph>
             *     <![CDATA[.....]]>
             * </Paragraph>
             */
            String paragraph = getCDATASection(fieldNode);

            paragraph = (paragraph == null)
                        ? ""
                        : paragraph.trim();
            field     = FBField.getParagraphField(paragraph);
        } else if (fieldNodeName.equals("RadioGroup")) {

            /*
             * <RadioGroup label="" groupName="groupName" baField="">
             *     <Radio label="" />
             * </RadioGroup>
             */
            String            groupName = getAttributeValue(fieldNode, "groupName");
            String            baField   = getAttributeValue(fieldNode, "baField");
            String            rLabel    = getAttributeValue(fieldNode, "label");
            ArrayList<String> radioList = new ArrayList<String>();
            NodeList          children  = fieldNode.getChildNodes();
            int               length    = children.getLength();

            for (int i = 0; i < length; i++) {
                Node   node = children.item(i);
                String name = node.getNodeName();

                if (name.equals("Radio") == true) {
                    String label = getAttributeValue(node, "label");

                    radioList.add(label);
                }
            }

            field = FBField.getRadioField(groupName, rLabel, baField, radioList);
        } else if (fieldNodeName.equals("StaticList")) {

            /*
             * <StaticList name="" label="" toolTip="" baField="">
             *          <Default value="IT/Techincal" />
             *          <Option value="SG" />
             *          <Option value="HR" />
             * </StaticList>
             */
            String            name        = getAttributeValue(fieldNode, "name");
            String            label       = getAttributeValue(fieldNode, "label");
            String            width       = getAttributeValue(fieldNode, "width");
            String            toolTip     = getAttributeValue(fieldNode, "toolTip");
            String            baField     = getAttributeValue(fieldNode, "baField");
            ArrayList<FBType> typeList    = new ArrayList<FBType>();
            NodeList          optionNodes = fieldNode.getChildNodes();
            int               length      = optionNodes.getLength();

            for (int i = 0; i < length; i++) {
                Node   node     = optionNodes.item(i);
                String nodeName = node.getNodeName();

                if (nodeName.equals("Default")) {
                    String value = getAttributeValue(node, "value");
                    FBType type  = new FBType();

                    type.setValue(value);
                    type.setIsDefault(true);
                    typeList.add(type);
                } else if (nodeName.equals("Option")) {
                    String value = getAttributeValue(node, "value");
                    FBType type  = new FBType();

                    type.setValue(value);
                    type.setIsDefault(false);
                    typeList.add(type);
                }
            }

            field = FBField.getStaticListField(name, label, width, toolTip, baField, typeList);
        } else if (fieldNodeName.equals("TextArea")) {

            /*
             * <TextArea
             *          name=""
             *          label=""
             *          toolTip=""
             *  />
             */
            String name    = getAttributeValue(fieldNode, "name");
            String label   = getAttributeValue(fieldNode, "label");
            String width   = getAttributeValue(fieldNode, "width");
            String rows    = getAttributeValue(fieldNode, "rows");
            String toolTip = getAttributeValue(fieldNode, "toolTip");

            field = FBField.getTextAreaField(name, label, width, rows, toolTip);
        } else if (fieldNodeName.equals("TextBox")) {

            /*
             * <TextBox
             *          name=""
             *          label=""
             *          toolTip=""
             *          validate=""
             *  />
             */
            String  baField     = getAttributeValue(fieldNode, "baField");
            String  name        = getAttributeValue(fieldNode, "name");
            String  label       = getAttributeValue(fieldNode, "label");
            String  width       = getAttributeValue(fieldNode, "width");
            String  toolTip     = getAttributeValue(fieldNode, "toolTip");
            boolean validate    = getAttrBooleanValue(fieldNode, "validate");
            boolean prePopulate = getAttrBooleanValue(fieldNode, "prePopulate");
            String  value       = getAttributeValue(fieldNode, "value");
            boolean readOnly    = getAttrBooleanValue(fieldNode, "readOnly");

            field = FBField.getTextBoxField(name, label, width, toolTip, baField, prePopulate, value, readOnly);
        } else if (fieldNodeName.equals("User")) {

            /*
             * <MultiUser
             *          name=""
             *          label=""
             *          toolTip=""
             *          baListField=""
             *          multiple=""
             *  />
             */
            String  name     = getAttributeValue(fieldNode, "name");
            String  label    = getAttributeValue(fieldNode, "label");
            String  width    = getAttributeValue(fieldNode, "width");
            String  toolTip  = getAttributeValue(fieldNode, "toolTip");
            String  baField  = getAttributeValue(fieldNode, "baField");
            boolean multiple = getAttrBooleanValue(fieldNode, "multiple");

            field = FBField.getUserField(name, label, width, toolTip, baField, multiple);
        } else {
            field = null;
        }

        return field;
    }

    /**
     * This method parses the XML and returns the FBForm object.
     *
     * @param xml XML corresponding to the Form
     *
     * @return Form object.
     *
     * @exception Exception.
     */
    public static FBForm parseFormConfig(String xml) throws Exception {
        FBForm fbForm = new FBForm();

        if ((xml == null) || (xml.trim().equals("") == true)) {
            return fbForm;
        }

        try {

            // Convert the xml into a byte stream.
            ByteArrayInputStream bs = new ByteArrayInputStream(xml.getBytes());

            // Get a factory object of DocumentBuilder
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // Instantiate DocumentBuilder using the factory.
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

            // Parse the xml byte stream and get the Document.
            Document document           = documentBuilder.parse(bs);
            NodeList rootNodeList       = document.getElementsByTagName("Form");
            Node     rootNode           = rootNodeList.item(0);
            NodeList childNodeList      = rootNode.getChildNodes();
            int      maxFieldNameLength = 0;

            for (int i = 0; i < childNodeList.getLength(); i++) {
                Node childNode = childNodeList.item(i);

                // Get the name of the node.
                String nodeName = childNode.getNodeName();

                nodeName = ((nodeName == null)
                            ? ""
                            : nodeName.trim());

                /*
                 * <Name value="" />
                 */
                if (nodeName.equals("Name")) {
                    String name = getAttributeValue(childNode, "value");

                    fbForm.setName(name);
                }

                /*
                 * <ShortName value="" />
                 */
                if (nodeName.equals("ShortName")) {
                    String name = getAttributeValue(childNode, "value");

                    fbForm.setShortName(name);
                }

                /*
                 * <Title value="" />
                 */
                else if (nodeName.equals("Title")) {
                    String title = getAttributeValue(childNode, "value");

                    fbForm.setTitle(title);
                }

                /*
                 * <Action>
                 *      <From email="" />
                 *      <To email="" />
                 *      <To email="" />
                 *      <Cc email="" />
                 *      <Cc email="" />
                 *      <Subject><![CDATA[#text]]></Subject>
                 *      <Description><![CDATA[#text]]></Description>
                 * </Action>
                 */
                else if (nodeName.equals("Action")) {
                    FBAction action = parseFBAction(childNode);

                    if (action != null) {
                        fbForm.setAction(action);
                    }
                } else if (nodeName.equals("FieldInfo")) {
                    NodeList fieldNodes = childNode.getChildNodes();
                    int      fLength    = fieldNodes.getLength();

                    for (int j = 0; j < fLength; j++) {
                        Node    fieldNode     = fieldNodes.item(j);
                        String  fieldNodeName = fieldNode.getNodeName();
                        FBField field         = parseFBField(fieldNode);

                        if (field != null) {
                            fbForm.addToFieldList(field);

                            String fieldName = field.getName();

                            if (fieldName != null) {
                                int length = field.getName().length();

                                maxFieldNameLength = (maxFieldNameLength < length)
                                                     ? length
                                                     : maxFieldNameLength;
                            }
                        }
                    }
                }
            }

            fbForm.setMaxFieldNameLength(maxFieldNameLength);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fbForm;
    }

    /**
     * String representation of FormConfig object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\nName: ").append(myName).append("\nShortName: ").append(myShortName).append("\nTitle: ").append(myTitle).append("\nAction: ").append(myAction);

        for (FBField field : myFieldList) {
            buffer.append("\n").append(field.toString());
        }

        buffer.append("\n");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for Action property.
     *
     * @return Current Value of Action
     *
     */
    public FBAction getAction() {
        return myAction;
    }

    private static String getCDATASection(Node parentNode) {
        String   cdata      = "";
        NodeList childNodes = parentNode.getChildNodes();
        int      length     = childNodes.getLength();

        for (int i = 0; i < length; i++) {
            Node   childNode = childNodes.item(i);
            String nodeName  = childNode.getNodeName();

            if (nodeName.indexOf("cdata") >= 0) {
                cdata = childNode.getNodeValue();
            }
        }

        return cdata;
    }

    /**
     * Accessor method for FieldList property.
     *
     * @return Current Value of FieldList
     *
     */
    public ArrayList<FBField> getFieldList() {
        return myFieldList;
    }

    /**
     * Accessor method for myMaxFieldNameLength property.
     *
     * @return Current value of myMaxFieldNameLength
     */
    public int getMaxFieldNameLength() {
        return myMaxFieldNameLength;
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
     * Accessor method for ShortName property.
     *
     * @return Current Value of ShortName
     *
     */
    public String getShortName() {
        return myShortName;
    }

    /**
     * Accessor method for Title property.
     *
     * @return Current Value of Title
     *
     */
    public String getTitle() {
        return myTitle;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for Action property.
     *
     * @param aAction New Value for Action
     *
     */
    public void setAction(FBAction aAction) {
        myAction = aAction;
    }

    /**
     * Mutator method for FieldList property.
     *
     * @param aFieldList New Value for FieldList
     *
     */
    public void setFieldList(ArrayList<FBField> aFieldList) {
        myFieldList = aFieldList;
    }

    /**
     * Mutator method for myMaxFieldNameLength property.
     *
     * @param aMaxFieldNameLength Current value of myMaxFieldNameLength
     */
    public void setMaxFieldNameLength(int aMaxFieldNameLength) {
        myMaxFieldNameLength = aMaxFieldNameLength;
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
     * Mutator method for ShortName property.
     *
     * @param aShortName New Value for ShortName
     *
     */
    public void setShortName(String aShortName) {
        myShortName = aShortName;
    }

    /**
     * Mutator method for Title property.
     *
     * @param aTitle New Value for Title
     *
     */
    public void setTitle(String aTitle) {
        myTitle = aTitle;
    }
}
