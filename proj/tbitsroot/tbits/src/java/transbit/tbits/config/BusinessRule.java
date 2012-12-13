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
 * BusinessRule.java
 *
 * $Header:
 *
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Other TBits Imports.
import transbit.tbits.common.TBitsLogger;

//Imports from the current package.
import transbit.tbits.config.RuleAction;
import transbit.tbits.config.RuleCondition;
import transbit.tbits.config.RuleCondition.State;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.Helper.TBitsConstants.PKG_CONFIG;
import static transbit.tbits.config.RuleAction.ActionType;

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
 * This class encapsulates the business rules in TBits.
 * Each business rule has one or more conditions to be checked and
 * when the conditions are satisfied, it results in one or more actions. An
 * action can be validating a field against a value or modifying the current
 * value of a field.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public class BusinessRule implements Serializable {

    // Application logger
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    //~--- fields -------------------------------------------------------------

    private ArrayList<RuleAction> myActionList;

    // Data members.
    private ArrayList<RuleCondition> myConditionList;
    private String                   myMessage;

    //~--- constant enums -----------------------------------------------------

    public enum Operator {
        EQ, NE, GT, GE, LT, LE, IN, PREPEND, APPEND, REPLACE, REMOVE, SET, ADD, MINUS, LIKE
    }

    ;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public BusinessRule() {
        myConditionList = new ArrayList<RuleCondition>();
        myActionList    = new ArrayList<RuleAction>();
        myMessage       = "";
    }

    /**
     * The complete constructor.
     *
     *  @param aConditionList
     *  @param aActionList
     *  @param aMessage
     */
    public BusinessRule(ArrayList<RuleCondition> aConditionList, ArrayList<RuleAction> aActionList, String aMessage) {
        myConditionList = aConditionList;
        myActionList    = aActionList;
        myMessage       = aMessage;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method is mainly for testing.
     *
     */
    public static void main(String arg[]) {
        try {
            java.io.BufferedReader br     = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            StringBuilder          buffer = new StringBuilder();
            String                 str    = "";

            while ((str = br.readLine()) != null) {
                buffer.append(str);
            }

            BusinessRule brule = BusinessRule.parseRule(buffer.toString());

            LOG.info(brule.toString());

            BusinessRule brule1 = BusinessRule.parseRule(brule.toString());

            LOG.info(brule1.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method parses the xml that represents a Business Rule.
     *
     *
     * @param aXml XML that represents a Business Rule.
     * @return BusinessRule object.
     * @exception DETBitsExceptionncase of errors during parsing.
     */
    public static BusinessRule parseRule(String aXml) throws TBitsException {
        BusinessRule bRule = new BusinessRule();

        if ((aXml == null) || (aXml.trim().equals("") == true)) {
            return bRule;
        }

        String                   message = "";
        ArrayList<RuleCondition> rcList  = new ArrayList<RuleCondition>();
        ArrayList<RuleAction>    raList  = new ArrayList<RuleAction>();
        DocumentBuilderFactory   factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder          builder = null;
        Document                 doc     = null;

        try {
            builder = factory.newDocumentBuilder();
            doc     = builder.parse(new ByteArrayInputStream(aXml.getBytes()));

            NodeList rootNodeList = doc.getElementsByTagName("Rule");
            Node     rootNode     = rootNodeList.item(0);
            NodeList children     = rootNode.getChildNodes();
            int      length       = children.getLength();

            for (int i = 0; i < length; i++) {
                Node   child    = children.item(i);
                String nodeName = child.getNodeName();

                if (nodeName.equalsIgnoreCase("Condition")) {
                    NodeList gChildren = child.getChildNodes();
                    int      gLength   = gChildren.getLength();

                    for (int j = 0; j < gLength; j++) {
                        Node gChild = gChildren.item(j);

                        nodeName = gChild.getNodeName();

                        if (nodeName.equalsIgnoreCase("Check")) {

                            // Parse the condition related nodes.
                            RuleCondition rc = getRuleCondition(gChild);

                            rcList.add(rc);
                        }
                    }
                } else if (nodeName.equalsIgnoreCase("Action")) {
                    NodeList gChildren = child.getChildNodes();
                    int      gLength   = gChildren.getLength();

                    for (int j = 0; j < gLength; j++) {
                        Node gChild = gChildren.item(j);

                        nodeName = gChild.getNodeName();

                        if (nodeName.equalsIgnoreCase("Modify") || nodeName.equalsIgnoreCase("Validate")) {

                            // Parse the action related nodes.
                            RuleAction ra = getRuleAction(gChild);

                            raList.add(ra);
                        }
                    }
                } else if (nodeName.equalsIgnoreCase("Message")) {
                    message = getAttributeValue(child, "value");
                    bRule.setMessage(message);
                }
            }
        } catch (Exception e) {
            StringBuffer error = new StringBuffer();

            error.append("An error has occurred while parsing the following ").append("rule:\n").append(aXml).append("\n").append(TBitsLogger.getStackTrace(e));

            throw new TBitsException(error.toString());
        }

        // Prepare the BusinessRule object with the value obtained at the end
        // of parsing.
        bRule.setConditionList(rcList);
        bRule.setActionList(raList);
        bRule.setMessage(message);

        return bRule;
    }

    /**
     * This method returns the string representation of the BusinessRule object
     *
     * @return String.
     *
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n<Rule>").append("\n\t<Condition>");

        for (RuleCondition rc : myConditionList) {
            buffer.append("\n\t\t<Check state=\"").append(rc.getState());
            buffer.append("\" fieldName=\"").append(rc.getFieldName()).append("\">");

            switch (rc.getState()) {
            case CURRENT : {
                buffer.append("\n\t\t\t<Current value=\"").append(rc.getCurrentValue()).append("\" operator=\"").append(rc.getCurrentOperator()).append("\" />");
            }

            break;

            case CHANGE : {
                buffer.append("\n\t\t\t<Old value=\"").append(rc.getOldValue()).append("\" operator=\"").append(rc.getOldOperator()).append("\" />").append("\n\t\t\t<New value=\"").append(
                    rc.getNewValue()).append("\" operator=\"").append(rc.getNewOperator()).append("\" />");
            }

            break;
            }

            buffer.append("\n\t\t</Check>");
        }

        buffer.append("\n\t</Condition>");
        buffer.append("\n\t<Action>");

        for (RuleAction ra : myActionList) {
            buffer.append("\n\t\t<").append(ra.getActionType()).append(" fieldName=\"").append(ra.getFieldName()).append("\" value=\"").append(ra.getValue()).append("\" operator=\"").append(
                ra.getOperator()).append("\" />");
        }

        buffer.append("\n\t</Action>");
        buffer.append("\n\t<Message value=\"").append(myMessage).append("\" />").append("\n</Rule>");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for ActionList property.
     *
     * @return Current Value of ActionList
     *
     */
    public ArrayList<RuleAction> getActionList() {
        return myActionList;
    }

    /**
     * This method returns the RuleCondition object represented by this node
     * whose state is "Change".
     *
     * @param fieldName   Name of the field.
     * @param gChildren   List of rules.
     *
     * @return RuleCondition object.
     */
    private static RuleCondition getChangeCondition(String fieldName, NodeList gChildren) {
        String   oldValue    = "";
        String   newValue    = "";
        Operator oldOperator = Operator.EQ;
        Operator newOperator = Operator.EQ;
        int      glength     = gChildren.getLength();

        // Look for "Old" and "New" nodes.
        for (int j = 0; j < glength; j++) {
            Node   gChild = gChildren.item(j);
            String name   = gChild.getNodeName().toLowerCase();

            // Skip the #text nodes.
            if ((name.equalsIgnoreCase("old") == false) && (name.equalsIgnoreCase("new") == false)) {
                continue;
            }

            String   value    = "";
            Operator operator = Operator.EQ;

            /*
             * Irrespective of the node type, it will have two attributes.
             *    - value
             *    - operator
             * Get the values of these attributes.
             */
            value = getAttributeValue(gChild, "value");

            String optr = getAttributeValue(gChild, "operator");

            operator = Operator.valueOf(optr.toUpperCase());

            /*
             * If the node's name is Old then the value and operator attributes
             * correspond to old state.
             */
            if (name.equalsIgnoreCase("old")) {
                oldValue    = value;
                oldOperator = operator;
            }

            /*
             * If the node's name is New then the value and operator attributes
             * correspond to new state.
             */
            else if (name.equalsIgnoreCase("new")) {
                newValue    = value;
                newOperator = operator;
            }
        }

        /*
         * Form the rule condition object by calling the appropriate factory
         * method.
         */
        RuleCondition rc = RuleCondition.getChangeRuleCondition(fieldName, oldValue, newValue, oldOperator, newOperator);

        return rc;
    }

    /**
     * Accessor method for ConditionList property.
     *
     * @return Current Value of ConditionList
     *
     */
    public ArrayList<RuleCondition> getConditionList() {
        return myConditionList;
    }

    /**
     * This method returns the RuleCondition object represented by this node
     * whose state is "Current".
     *
     * @param fieldName   Name of the field.
     * @param gChildren   List of rules.
     *
     * @return RuleCondition object.
     */
    private static RuleCondition getCurrentCondition(String fieldName, NodeList gChildren) {
        String   currentValue    = "";
        Operator currentOperator = Operator.EQ;
        int      length          = gChildren.getLength();

        if (length == 0) {
            return null;
        }

        for (int i = 0; i < length; i++) {
            Node   gChild = gChildren.item(i);
            String name   = gChild.getNodeName();

            if (name.equalsIgnoreCase("Current")) {
                currentValue = getAttributeValue(gChild, "value");

                String optr = getAttributeValue(gChild, "operator");

                currentOperator = Operator.valueOf(optr.toUpperCase());
            }
        }

        // Form the rule condition object by calling the appropriate factory
        // method.
        RuleCondition rc = RuleCondition.getCurrentRuleCondition(fieldName, currentValue, currentOperator);

        return rc;
    }

    /**
     * Accessor method for Message property.
     *
     * @return Current Value of Message
     *
     */
    public String getMessage() {
        return myMessage;
    }

    /**
     * This method returns the RuleAction object represented by this node.
     *
     * @param gChild Root of RuleAction node.
     *
     * @return RuleAction object.
     */
    private static RuleAction getRuleAction(Node gChild) {
        RuleAction ra = new RuleAction();

        /*
         * Irrespective of the node name, it will have three attributes.
         *   - fieldName
         *   - value
         *   - operator
         */
        String   fieldName = getAttributeValue(gChild, "fieldName");
        String   value     = getAttributeValue(gChild, "value");
        String   tmp       = getAttributeValue(gChild, "operator");
        Operator operator  = Operator.valueOf(tmp.toUpperCase());
        String   nodeName  = gChild.getNodeName();

        ra.setActionType(ActionType.valueOf(nodeName.toUpperCase()));
        ra.setFieldName(fieldName);
        ra.setValue(value);
        ra.setOperator(operator);

        return ra;
    }

    /**
     * This method returns the RuleCondition object represented by this node.
     *
     * @param gChild Root of RuleCondition node.
     *
     * @return RuleCondition object.
     */
    private static RuleCondition getRuleCondition(Node gChild) {
        RuleCondition rc = null;

        // State of the check.
        String strState = getAttributeValue(gChild, "state");
        State  state    = State.valueOf(strState.toUpperCase());

        // Field name.
        String fieldName = getAttributeValue(gChild, "fieldName");

        // Get the children.
        NodeList gChildren = gChild.getChildNodes();

        if (state == State.CURRENT) {
            rc = getCurrentCondition(fieldName, gChildren);
        } else if (state == State.CHANGE) {
            rc = getChangeCondition(fieldName, gChildren);
        }

        return rc;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for ActionList property.
     *
     * @param aActionList New Value for ActionList
     *
     */
    public void setActionList(ArrayList<RuleAction> aActionList) {
        myActionList = aActionList;
    }

    /**
     * Mutator method for ConditionList property.
     *
     * @param aConditionList New Value for ConditionList
     *
     */
    public void setConditionList(ArrayList<RuleCondition> aConditionList) {
        myConditionList = aConditionList;
    }

    /**
     * Mutator method for Message property.
     *
     * @param aMessage New Value for Message
     *
     */
    public void setMessage(String aMessage) {
        myMessage = aMessage;
    }
}
