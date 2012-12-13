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
 * DependencyConfig.java
 *
 * $Header:
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.TBitsConstants.PKG_CONFIG;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.Dependency.DepLevel;
import transbit.tbits.domain.Dependency.DepType;
import transbit.tbits.exception.TBitsException;

//~--- classes ----------------------------------------------------------------

/**
 *
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class DependencyConfig implements Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    //~--- fields -------------------------------------------------------------

    private Hashtable<String, ArrayList<String>> myAsyncMap;

    /*
     * Five types of configurations are possible.
     *  1. App Synchronize.
     *  2. App Validate
     *  3. App Generate
     *
     *  4. Field Validate
     *  5. Field Generate
     */
    private DepLevel                     myDepLevel;
    private DepType                      myDepType;
    private String                       myDependentField;
    private ArrayList<TypeDependencyMap> myDependentList;
    private String                       myErrorMessage;

    /*
     * App Generate related params.
     */
    private Hashtable<String, String> myInputMap;
    private boolean                   myIsDeterministic;
    private Hashtable<String, String> myOutputMap;
    private ArrayList<String>         myPrimaryAttributeList;
    private ArrayList<String>         myPrimaryColumnList;

    /*
     *  Field Dependencies.
     *
     */

    /*
     * Field Validate related params
     */
    private String myPrimaryField;

    /*
     *  App Dependencies.
     *
     */
    private String myResourceName;

    /*
     * App Synchronize related params
     */
    private String                    mySyncFieldName;
    private Hashtable<String, String> mySyncMap;
    private boolean                   myThrowError;

    //~--- methods ------------------------------------------------------------

    /**
     * @param args
     */
    public static int main(String[] args) {
        try {
            java.io.BufferedReader br  = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            String                 str = "";
            StringBuilder          xml = new StringBuilder();

            while ((str = br.readLine()) != null) {
                xml.append(str).append("\n");
            }

            LOG.info(parse(DepLevel.FIELD_DEPENDENCY, DepType.VALIDATE, xml));
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 1;
    }

    /**
     *
     * @param aLevel
     * @param aType
     * @param aXml
     * @return Dependency config object.
     * @throws Exception
     */
    public static DependencyConfig parse(DepLevel aLevel, DepType aType, CharSequence aXml) throws Exception {
        DependencyConfig config = null;

        if (aXml == null) {
            return config;
        }

        String strXml = aXml.toString();

        if ((strXml == null) || strXml.trim().equals("")) {
            return config;
        }

        DocumentBuilderFactory factory         = DocumentBuilderFactory.newInstance();
        DocumentBuilder        documentBuilder = null;
        Document               document        = null;

        try {
            documentBuilder = factory.newDocumentBuilder();
            document        = (org.w3c.dom.Document) documentBuilder.parse(new ByteArrayInputStream(aXml.toString().getBytes()));

            switch (aLevel) {
            case APP_DEPENDENCY :
                switch (aType) {
                case GENERATE :
                    config = parseAppGenerate(document);
                    config.setDepLevel(DepLevel.APP_DEPENDENCY);
                    config.setDepType(DepType.GENERATE);

                    break;

                case SYNCHRONIZE :
                    config = parseAppSynchronize(document);
                    config.setDepLevel(DepLevel.APP_DEPENDENCY);
                    config.setDepType(DepType.SYNCHRONIZE);

                    break;

                case VALIDATE :
                    break;
                }
                ;

                break;

            case FIELD_DEPENDENCY :
                switch (aType) {
                case GENERATE :
                    break;

                case SYNCHRONIZE :
                    break;

                case VALIDATE :
                    config = parseFieldValidate(document);
                    config.setDepLevel(DepLevel.FIELD_DEPENDENCY);
                    config.setDepType(DepType.VALIDATE);

                    break;
                }
                ;

                break;
            }
        } catch (Exception e) {
            LOG.warn("",(e));
        }

        return config;
    }

    /**
     *
     * @param aXml
     * @return
     */
    private static DependencyConfig parseAppGenerate(Document document) throws Exception {
        DependencyConfig          config          = new DependencyConfig();
        String                    resourceName    = "";
        String                    errorMessage    = "";
        boolean                   isDeterministic = true;
        boolean                   throwError      = false;
        Hashtable<String, String> inputMap        = new Hashtable<String, String>();
        Hashtable<String, String> outputMap       = new Hashtable<String, String>();
        NodeList                  rootList        = document.getElementsByTagName("AppGenerate");
        Node                      root            = rootList.item(0);

        if (root == null) {
            return config;
        }

        /*
         * <AppGenerate resource="">
         *      <Input requestField="" resourceAttr="" />
         *      <Output requestField="" resourceAttr="" />
         *      <Error throw="true" message="" />
         * </AppSynchronize>
         */
        NamedNodeMap nnm      = null;
        Node         attrNode = null;

        /*
         * Get the value of resource attribute which is mandatory.
         */
        nnm          = root.getAttributes();
        resourceName = getAttrValue(nnm, "resource", true);

        String strIsDeterministic = getAttrValue(nnm, "isDeterministic", false);

        if ((strIsDeterministic != null) && strIsDeterministic.trim().equals("false")) {
            isDeterministic = false;
        } else {
            isDeterministic = true;
        }

        /*
         * Process the child nodes.
         */
        NodeList children = root.getChildNodes();
        int      cLength  = children.getLength();

        for (int i = 0; i < cLength; i++) {
            Node child = children.item(i);

            nnm = child.getAttributes();

            String nodeName = child.getNodeName();

            if (nodeName.equals("Input")) {
                String column = getAttrValue(nnm, "requestField", true);
                String attr   = getAttrValue(nnm, "resourceAttr", true);

                inputMap.put(column, attr);
            } else if (nodeName.equals("Output")) {
                String column = getAttrValue(nnm, "requestField", true);
                String value  = getAttrValue(nnm, "resourceAttr", true);

                outputMap.put(column, value);
            } else if (nodeName.equals("Error")) {
                String strThrowError = getAttrValue(nnm, "throw", false);
                String message       = getAttrValue(nnm, "message", false);

                errorMessage = message.trim();

                if (strThrowError == null) {
                    throwError = false;
                } else {
                    strThrowError = strThrowError.trim();

                    if (strThrowError.equals("true") || strThrowError.equals("1") || strThrowError.equals("yes")) {
                        throwError = true;
                    } else {
                        throwError = false;
                    }
                }
            }
        }

        config.setResourceName(resourceName);
        config.setInputMap(inputMap);
        config.setOutputMap(outputMap);
        config.setIsDeterministic(isDeterministic);
        config.setErrorMessage(errorMessage);
        config.setThrowError(throwError);

        return config;
    }

    /**
     *
     * @param aXml
     * @return
     */
    private static DependencyConfig parseAppSynchronize(Document document) throws Exception {
        DependencyConfig                     config       = new DependencyConfig();
        String                               resourceName = "";
        String                               fieldName    = "";
        Hashtable<String, String>            syncMap      = new Hashtable<String, String>();
        Hashtable<String, ArrayList<String>> asyncMap     = new Hashtable<String, ArrayList<String>>();
        ArrayList<String>                    primColList  = new ArrayList<String>();
        ArrayList<String>                    primAttrList = new ArrayList<String>();
        NodeList                             rootList     = document.getElementsByTagName("AppSynchronize");
        Node                                 root         = rootList.item(0);

        if (root == null) {
            return config;
        }

        /*
         * <AppSynchronize resource="" fieldName>
         *      <Primary column="" attr="" />
         *      <Sync column="" attr="" />
         *      <Async column="" asyncValue="" syncValue=""/>
         * </AppSynchronize>
         */
        NamedNodeMap nnm      = null;
        Node         attrNode = null;

        /*
         * Get the value of resource attribute which is mandatory.
         */
        nnm          = root.getAttributes();
        resourceName = getAttrValue(nnm, "resource", true);
        fieldName    = getAttrValue(nnm, "fieldName", true);

        /*
         * Process the child nodes.
         */
        NodeList children = root.getChildNodes();
        int      cLength  = children.getLength();

        for (int i = 0; i < cLength; i++) {
            Node child = children.item(i);

            nnm = child.getAttributes();

            String nodeName = child.getNodeName();

            if (nodeName.equals("Sync")) {
                String column = getAttrValue(nnm, "column", true);
                String attr   = getAttrValue(nnm, "attr", true);

                syncMap.put(column, attr);
            } else if (nodeName.equals("Async")) {
                String            column     = getAttrValue(nnm, "column", true);
                String            asyncValue = getAttrValue(nnm, "asyncValue", true);
                String            syncValue  = getAttrValue(nnm, "syncValue", true);
                ArrayList<String> vList      = new ArrayList<String>();

                vList.add(asyncValue);
                vList.add(syncValue);
                asyncMap.put(column, vList);
            } else if (nodeName.equals("PrimaryColumn")) {
                String list = getAttrValue(nnm, "list", true);

                primColList.addAll(Utilities.toArrayList(list));
            } else if (nodeName.equals("PrimaryAttr")) {
                String list = getAttrValue(nnm, "list", true);

                primAttrList.addAll(Utilities.toArrayList(list));
            }
        }

        config.setResourceName(resourceName);
        config.setSyncFieldName(fieldName);
        config.setSyncMap(syncMap);
        config.setAsyncMap(asyncMap);
        config.setPrimaryAttributeList(primAttrList);
        config.setPrimaryColumnList(primColList);

        return config;
    }

    /**
     *
     * @param aXml
     * @return
     */
    private static DependencyConfig parseFieldValidate(Document document) throws Exception {
        DependencyConfig config   = new DependencyConfig();
        NodeList         rootList = document.getElementsByTagName("FieldValidate");
        Node             root     = rootList.item(0);

        if (root == null) {
            return config;
        }

        /*
         * <FieldValidate resource="">
         *      <Type primary="" dependent="">
         *           <Primary name="" clause="">
         *              <Dependent name="" />
         *           </Primary>
         *      </Type>
         * </FieldValidate>
         */
        NodeList children = root.getChildNodes();
        int      cLength  = children.getLength();

        for (int i = 0; i < cLength; i++) {
            Node   child    = children.item(i);
            String nodeName = child.getNodeName();

            if (nodeName.equals("Type")) {
                parseType(child, config);
            }
        }

        return config;
    }

    
      /* 
       * <Type primary="" dependent="">
       * 	<Primary name="" clause="">
       * 		<Dependent name="" />
       * 	</Primary>
       * </Type>
       */
    private static void parseType(Node root, DependencyConfig config) throws Exception {
        String                       primaryField   = "";
        String                       dependentField = "";
        ArrayList<TypeDependencyMap> dList          = new ArrayList<TypeDependencyMap>();
        NamedNodeMap                 nnm            = null;
        Node                         attrNode       = null;

        /*
         * Get the value of resource attribute which is mandatory.
         */
        nnm            = root.getAttributes();
        primaryField   = getAttrValue(nnm, "primary", true);
        dependentField = getAttrValue(nnm, "dependent", true);

        NodeList children = root.getChildNodes();
        int      cLength  = children.getLength();

        for (int i = 0; i < cLength; i++) {
            Node   child    = children.item(i);
            String nodeName = child.getNodeName();

            if (nodeName.equals("Primary")) {
                TypeDependencyMap obj = new TypeDependencyMap();

                nnm = child.getAttributes();

                String  typeName = getAttrValue(nnm, "name", true);
                String  clause   = getAttrValue(nnm, "clause", false);
                boolean exclude  = false;

                if ((clause == null) || (clause.trim().equals("") == true)) {
                    exclude = false;
                } else {
                    clause = clause.trim().toLowerCase();

                    if (clause.equals("exclude")) {
                        exclude = true;
                    } else {
                        exclude = false;
                    }
                }

                obj.setPrimaryType(typeName);
                obj.setExclude(exclude);

                /*
                 * Get the dependent type names.
                 */
                ArrayList<String> depList       = new ArrayList<String>();
                NodeList          grandChildren = child.getChildNodes();
                int               gcLength      = grandChildren.getLength();

                for (int j = 0; j < gcLength; j++) {
                    Node   grandChild     = grandChildren.item(j);
                    String grandChildName = grandChild.getNodeName();

                    nnm = grandChild.getAttributes();

                    if (grandChildName.equals("Dependent")) {
                        String dependentName = getAttrValue(nnm, "name", true);

                        depList.add(dependentName);
                    }
                }

                obj.setDependentList(depList);
                dList.add(obj);
            }
        }

        config.setPrimaryField(primaryField);
        config.setDependentField(dependentField);
        config.setDependentList(dList);

        return;
    }

    /**
     *
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        switch (myDepLevel) {
        case APP_DEPENDENCY :
            buffer.append("\nResource: ").append(myResourceName);

            switch (myDepType) {
            case GENERATE :
                buffer.append("\nInput Map: ").append(myInputMap.toString()).append("\nOutput Map: ").append(myOutputMap.toString()).append("\nError Message: ").append(myErrorMessage).append(
                    "\nIs Deterministic: ").append(myIsDeterministic).append("\nThrow Error: ").append(myThrowError);

                break;

            case SYNCHRONIZE :
                buffer.append("\nPrimary Column List: ").append(myPrimaryColumnList.toString()).append("\nPrimary Attribute List: ").append(myPrimaryAttributeList.toString()).append(
                    "\nSync Map: ").append(mySyncMap.toString()).append("\nASync Map: ").append(myAsyncMap.toString());

                break;

            case VALIDATE :
                break;
            }

            break;

        case FIELD_DEPENDENCY :
            switch (myDepType) {
            case VALIDATE :
                buffer.append("\nPrimary Field: ").append(myPrimaryField).append("\nDependent Field: ").append(myDependentField).append("\nDependenyc Map: ").append(myDependentList);

                break;
            }

            break;
        }

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for AsyncMap property.
     *
     * @return Current Value of AsyncMap
     *
     */
    public Hashtable<String, ArrayList<String>> getAsyncMap() {
        return myAsyncMap;
    }

    /**
     *
     * @param nnm
     * @param attrName
     * @param required
     * @return
     * @throws Exception
     */
    private static String getAttrValue(NamedNodeMap nnm, String attrName, boolean required) throws Exception {
        String attrValue = "";

        if (nnm == null) {
            if (required == true) {
                throw new TBitsException("No value found for attribute: " + attrName);
            }

            return attrValue;
        }

        Node attrNode = nnm.getNamedItem(attrName);

        if (attrNode == null) {
            if (required == true) {
                throw new TBitsException("No attribute node found with name: " + attrName);
            }

            return attrValue;
        }

        String nodeValue = attrNode.getNodeValue();

        if ((nodeValue == null) || nodeValue.trim().equals("")) {
            if (required == true) {
                throw new TBitsException("No value specified for attribute: " + attrName);
            }

            return attrValue;
        }

        attrValue = nodeValue.trim();

        return attrValue;
    }

    /**
     * Accessor method for DepLevel property.
     *
     * @return Current Value of DepLevel
     *
     */
    public DepLevel getDepLevel() {
        return myDepLevel;
    }

    /**
     * Accessor method for DepType property.
     *
     * @return Current Value of DepType
     *
     */
    public DepType getDepType() {
        return myDepType;
    }

    /**
     * Accessor method for DependentField property.
     *
     * @return Current Value of DependentField
     *
     */
    public String getDependentField() {
        return myDependentField;
    }

    /**
     * Accessor method for DependentList property.
     *
     * @return Current Value of DependentList
     *
     */
    public ArrayList<TypeDependencyMap> getDependentList() {
        return myDependentList;
    }

    /**
     * Accessor method for ErrorMessage property.
     *
     * @return Current Value of ErrorMessage
     *
     */
    public String getErrorMessage() {
        return myErrorMessage;
    }

    /**
     * Accessor method for InputMap property.
     *
     * @return Current Value of InputMap
     *
     */
    public Hashtable<String, String> getInputMap() {
        return myInputMap;
    }

    /**
     * Accessor method for IsDeterministic property.
     *
     * @return Current Value of IsDeterministic
     *
     */
    public boolean getIsDeterministic() {
        return myIsDeterministic;
    }

    /**
     * Accessor method for OutputMap property.
     *
     * @return Current Value of OutputMap
     *
     */
    public Hashtable<String, String> getOutputMap() {
        return myOutputMap;
    }

    /**
     * Accessor method for PrimaryAttributeList property.
     *
     * @return Current Value of PrimaryAttributeList
     *
     */
    public ArrayList<String> getPrimaryAttributeList() {
        return myPrimaryAttributeList;
    }

    /**
     * Accessor method for PrimaryColumnList property.
     *
     * @return Current Value of PrimaryColumnList
     *
     */
    public ArrayList<String> getPrimaryColumnList() {
        return myPrimaryColumnList;
    }

    /**
     * Accessor method for PrimaryField property.
     *
     * @return Current Value of PrimaryField
     *
     */
    public String getPrimaryField() {
        return myPrimaryField;
    }

    /**
     * Accessor method for ResourceName property.
     *
     * @return Current Value of ResourceName
     *
     */
    public String getResourceName() {
        return myResourceName;
    }

    /**
     * Accessor method for SyncFieldName property.
     *
     * @return Current Value of SyncFieldName
     *
     */
    public String getSyncFieldName() {
        return mySyncFieldName;
    }

    /**
     * Accessor method for SyncMap property.
     *
     * @return Current Value of SyncMap
     *
     */
    public Hashtable<String, String> getSyncMap() {
        return mySyncMap;
    }

    /**
     * Accessor method for ThrowError property.
     *
     * @return Current Value of ThrowError
     *
     */
    public boolean getThrowError() {
        return myThrowError;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for AsyncMap property.
     *
     * @param aAsyncMap New Value for AsyncMap
     *
     */
    public void setAsyncMap(Hashtable<String, ArrayList<String>> aAsyncMap) {
        myAsyncMap = aAsyncMap;
    }

    /**
     * Mutator method for DepLevel property.
     *
     * @param aDepLevel New Value for DepLevel
     *
     */
    public void setDepLevel(DepLevel aDepLevel) {
        myDepLevel = aDepLevel;
    }

    /**
     * Mutator method for Type property.
     *
     * @param aDepType New Value for Type
     *
     */
    public void setDepType(DepType aDepType) {
        myDepType = aDepType;
    }

    /**
     * Mutator method for DependentField property.
     *
     * @param aDependentField New Value for DependentField
     *
     */
    public void setDependentField(String aDependentField) {
        myDependentField = aDependentField;
    }

    /**
     * Mutator method for DependentList property.
     *
     * @param aDependentList New Value for DependentList
     *
     */
    public void setDependentList(ArrayList<TypeDependencyMap> aDependentList) {
        myDependentList = aDependentList;
    }

    /**
     * Mutator method for ErrorMessage property.
     *
     * @param aErrorMessage New Value for ErrorMessage
     *
     */
    public void setErrorMessage(String aErrorMessage) {
        myErrorMessage = aErrorMessage;
    }

    /**
     * Mutator method for InputMap property.
     *
     * @param aInputMap New Value for InputMap
     *
     */
    public void setInputMap(Hashtable<String, String> aInputMap) {
        myInputMap = aInputMap;
    }

    /**
     * Mutator method for IsDeterministic property.
     *
     * @param aIsDeterministic New Value for IsDeterministic
     *
     */
    public void setIsDeterministic(boolean aIsDeterministic) {
        myIsDeterministic = aIsDeterministic;
    }

    /**
     * Mutator method for OutputMap property.
     *
     * @param aOutputMap New Value for OutputMap
     *
     */
    public void setOutputMap(Hashtable<String, String> aOutputMap) {
        myOutputMap = aOutputMap;
    }

    /**
     * Mutator method for PrimaryAttributeList property.
     *
     * @param aPrimaryAttributeList New Value for PrimaryAttributeList
     *
     */
    public void setPrimaryAttributeList(ArrayList<String> aPrimaryAttributeList) {
        myPrimaryAttributeList = aPrimaryAttributeList;
    }

    /**
     * Mutator method for PrimaryColumnList property.
     *
     * @param aPrimaryColumnList New Value for PrimaryColumnList
     *
     */
    public void setPrimaryColumnList(ArrayList<String> aPrimaryColumnList) {
        myPrimaryColumnList = aPrimaryColumnList;
    }

    /**
     * Mutator method for PrimaryField property.
     *
     * @param aPrimaryField New Value for PrimaryField
     *
     */
    public void setPrimaryField(String aPrimaryField) {
        myPrimaryField = aPrimaryField;
    }

    /**
     * Mutator method for ResourceName property.
     *
     * @param aResourceName New Value for ResourceName
     *
     */
    public void setResourceName(String aResourceName) {
        myResourceName = aResourceName;
    }

    /**
     * Mutator method for SyncFieldName property.
     *
     * @param aSyncFieldName New Value for SyncFieldName
     *
     */
    public void setSyncFieldName(String aSyncFieldName) {
        mySyncFieldName = aSyncFieldName;
    }

    /**
     * Mutator method for SyncMap property.
     *
     * @param aSyncMap New Value for SyncMap
     *
     */
    public void setSyncMap(Hashtable<String, String> aSyncMap) {
        mySyncMap = aSyncMap;
    }

    /**
     * Mutator method for ThrowError property.
     *
     * @param aThrowError New Value for ThrowError
     *
     */
    public void setThrowError(boolean aThrowError) {
        myThrowError = aThrowError;
    }
}
