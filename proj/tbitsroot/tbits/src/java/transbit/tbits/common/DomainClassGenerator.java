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
 * DomainClassGenerator.java
 */
package transbit.tbits.common;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

//~--- classes ----------------------------------------------------------------

/**
 *
 */
class DataMember {
    private String myEnum;
    private String myName;
    private int    myOrder;
    private String myType;

    //~--- constructors -------------------------------------------------------

    /**
     *
     */
    public DataMember(String aType, String aName, int aOrder) {
        myType  = aType;
        myName  = aName;
        myOrder = aOrder;
        myEnum  = aName.toUpperCase();
    }

    //~--- get methods --------------------------------------------------------

    /**
     *
     */
    public String getComparisionLogic() {
        StringBuilder buffer    = new StringBuilder();
        String        aDataType = myType.toLowerCase().trim();

        if (aDataType.equals("int") == true) {
            buffer.append("\ncase ").append(myEnum).append(":").append("\n    {").append("\n        Integer i1 = my").append(myName).append(";").append("\n        Integer i2 = aObject.my").append(
                myName).append(";").append("\n        return i1.compareTo(i2);").append("\n    }");
        }

        if (aDataType.equals("long") == true) {
            buffer.append("\ncase ").append(myEnum).append(":").append("\n    {").append("\n        Long l1 = my").append(myName).append(";").append("\n        Long l2 = aObject.my").append(
                myName).append(";").append("\n        return l1.compareTo(l2);").append("\n    }");
        } else if (aDataType.equals("double") == true) {
            buffer.append("\ncase ").append(myEnum).append(":").append("\n    {").append("\n        Double d1 = my").append(myName).append(";").append("\n        Double d2 = aObject.my").append(
                myName).append(";").append("\n        return d1.compareTo(d2);").append("\n    }");
        } else if (aDataType.equals("string") == true) {
            buffer.append("\ncase ").append(myEnum).append(":").append("\n    {").append("\n        return my").append(myName).append(".compareTo(").append("aObject.my").append(myName).append(
                ");").append("\n    }");
        } else if (aDataType.equals("boolean") == true) {
            buffer.append("\ncase ").append(myEnum).append(":").append("\n    {").append("\n        Boolean b1 = my").append(myName).append(";").append("\n        Boolean b2 = aObject.my").append(
                myName).append(";").append("\n        return b1.compareTo(b2);").append("\n    }");
        } else if (aDataType.equals("timestamp") == true) {
            buffer.append("\ncase ").append(myEnum).append(":").append("\n    {").append("\n        return my").append(myName).append(".compareTo(").append("aObject.my").append(myName).append(
                ");").append("\n    }");
        }

        return buffer.toString();
    }

    /**
     *
     */
    public String getEnum() {
        return myEnum;
    }

    /**
     *
     */
    public String getGetter() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n").append("\n/**").append("\n * Accessor method for ").append(myName).append(" property.").append("\n *").append("\n * @return Current Value of ").append(myName).append(
            "\n *").append("\n */").append("\npublic ").append(myType).append(" get").append(myName).append("()").append("\n{").append("\n    return my").append(myName).append(";\n}");

        return buffer.toString();
    }

    /**
     *
     */
    public String getName() {
        return myName;
    }

    /**
     *
     */
    public int getOrder() {
        return myOrder;
    }

    /**
     *
     */
    public String getSetter() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n").append("\n/**").append("\n * Mutator method for ").append(myName).append(" property.").append("\n *").append("\n * @param a").append(myName).append(
            " New Value for ").append(myName).append("\n *").append("\n */").append("\npublic void set").append(myName).append("(").append(myType).append(" a").append(myName).append(")\n{").append(
            "\n    my").append(myName).append(" = a").append(myName).append(";\n}");

        return buffer.toString();
    }

    /**
     *
     */
    public String getType() {
        return myType;
    }
}


/**
 * This class generates a java class file that contains the following:
 * <ol>
 *      <li> The Prologue about the copyright.
 *      <li> The Required Import Statements.
 *      <li> Class Declaration.
 *      <li> Sorting Related Parameters.
 *      <li> General Attributes of the class.
 *      <li> Setter and Getter methods for these general attributes.
 *      <li> Insert, Update methods for an object of this class into database.
 *      <li> CompareTo method
 *      <li> Comparator Class for this domain class.
 * </ol>
 * in that order.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class DomainClassGenerator {
    private String myAttrList;    // Comma-separated list of columns.
    private String myClass;       // Name of the class.

    // List of DataMembers
    private ArrayList<DataMember> myDMList;
    private boolean               myHasTimestamp;
    private int                   myMaxNameLength;

    // Other util members.
    private int myMaxTypeLength;

    // Attributes.
    private String myPackage;    // Package Name.
    private String myTable;      // Table corresponding to this.

    //~--- constructors -------------------------------------------------------

    /**
     * The only constructor of the class that takes the mandatory params.
     *
     * @param aPackage   Name of the package where the generated class resides.
     * @param aTable     Name of the table this class represents.
     * @param aClass     Name of the generated class.
     * @param aAttrList  Comma-separated list of colon-separated
     *                   dataType,member records.
     * <br><br>
     * <code>
     * e.g. <br>
     * DomainClassGenerator transbit.tbits.domain exclusion_list
     * ExclusionList "int:SystemId,int:UserId,boolean:IsActive"
     * </code>
     */
    public DomainClassGenerator(String aPackage, String aTable, String aClass, String aAttrList) {
        myPackage  = aPackage;
        myTable    = aTable;
        myClass    = aClass;
        myAttrList = aAttrList;
        
    }
    
    public boolean generate()
    {
        if(!populateDMList())
            return false;
        return buildClass();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Builds the class by calling the methods in order.
     */
    private boolean buildClass() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(getFileDocAndCopyRight()).append(getPackageDecl()).append(getImportStmts()).append(openClassDef()).append(getStaticDecl()).append(getAttributeDecl()).append(
            getConstructors()).append(getStaticFunctions()).append(getMemberFunctions()).append(closeClassDef()).append(getComparatorClass()).append("");

        // Write into a file.
        try {
            String fileName = myClass + ".java";
            File   file     = new File("./" + fileName);
            System.out.println("Writing to : " + file.getAbsolutePath());
            if (file.exists() == true) {
                System.out.println(fileName + " already exists!");
                return false;
            }

            FileOutputStream fos = new FileOutputStream(file.toString());

            fos.write(buffer.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     *
     */
    private String closeClassDef() {
        return "\n}";
    }

    /**
     *
     */
    public static void main(String arg[]) {
        if (arg.length != 4) {
            System.err.println("Usage: DomainClassGenerator <package> " + "<table> <class> <datamembers>");
            return;
        }

        DomainClassGenerator app = new DomainClassGenerator(arg[0], arg[1], arg[2], arg[3]);
        boolean isSuccess = app.generate();
        //return (isSuccess? 0:1);
    }

    /**
     * Returns the class declaration statements.
     */
    private String openClassDef() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("/**\n").append(" * This class is the domain object corresponding to the ").append(myTable).append(" table\n").append(" * in the database.\n * \n").append(
            " * @author  : \n").append(" * @version : $Id: $\n").append(" * \n").append(" */\n").append("public class ").append(myClass).append(" implements Comparable<").append(myClass).append(
            ">\n").append("{");

        return buffer.toString();
    }

    /**
     * This method parses the attribute list passed from command-line and
     * transforms this information into DataMember objects.
     */
    private boolean populateDMList() {
        myDMList        = new ArrayList<DataMember>();
        myMaxTypeLength = 0;

        if ((myAttrList == null) || (myAttrList.trim().equals("") == true)) {
            myAttrList = "";

            return false;
        }

        // Comma-separated list of attributes.
        StringTokenizer st    = new StringTokenizer(myAttrList, ",");
        int             order = 0;

        while (st.hasMoreTokens() == true) {
            String   token     = st.nextToken();
            String[] subTokens = token.split(":");

            if (subTokens.length != 2) {
                System.out.println("Illegal Token: " + token);

                continue;
            }

            order++;

            DataMember dm = new DataMember(subTokens[0], subTokens[1], order);

            myDMList.add(dm);

            if (subTokens[0].trim().toLowerCase().equals("timestamp")) {
                myHasTimestamp = true;
            }

            int typeLength = subTokens[0].length();
            int nameLength = subTokens[1].length();

            myMaxTypeLength = (myMaxTypeLength > typeLength)
                              ? myMaxTypeLength
                              : typeLength;
            myMaxNameLength = (myMaxNameLength > nameLength)
                              ? myMaxNameLength
                              : nameLength;
        }
        return true;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Assignment statements
     */
    private String getAssignmentStmts() {
        StringBuilder buffer = new StringBuilder();
        int           size   = myDMList.size();

        for (int i = 0; i < size; i++) {
            DataMember dm       = myDMList.get(i);
            String     name     = dm.getName();
            int        stuffLen = myMaxNameLength - name.length();
            String     stuff    = "";

            for (int j = 0; j < stuffLen; j++) {
                stuff = stuff + " ";
            }

            name = name + stuff;
            buffer.append("\nmy").append(name).append(" = a").append(dm.getName()).append(";");
        }

        return buffer.toString();
    }

    /**
     * Returns the enum declaration statements for data members.
     */
    private String getAttributeDecl() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n    // Attributes of this Domain Object.").append(getDMDecl().replaceAll("\n", "\n    ")).append("\n");

        return buffer.toString();
    }

    /**
     *
     */
    private String getCSFunction() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n/**").append("\n * This method sets the parameters in the ").append("CallableStatement.").append("\n *").append("\n * @param aCS     CallableStatement whose params ").append(
            "should be set.").append("\n *").append("\n * @exception SQLException ").append("\n */").append("\npublic void setCallableParameters").append("(CallableStatement aCS)").append(
            "\n    throws SQLException").append("\n{");

        int size = myDMList.size();

        for (int i = 0; i < size; i++) {
            DataMember dm    = myDMList.get(i);
            String     aType = dm.getType();
            String     aEnum = dm.getEnum();
            String     aName = "my" + dm.getName();

            if (aType.equalsIgnoreCase("int") == true) {
                aType = "Int";
            } else if (aType.equalsIgnoreCase("long") == true) {
                aType = "Long";
            } else if (aType.equalsIgnoreCase("boolean") == true) {
                aType = "Boolean";
            } else if (aType.equalsIgnoreCase("double") == true) {
                aType = "Double";
            } else if (aType.equalsIgnoreCase("timestamp") == true) {
                aType = "Timestamp";
                aName = aName + ".toSqlTimestamp()";
            }

            buffer.append("\n    aCS.set").append(aType).append("(").append(aEnum).append(", ").append(aName).append(");");
        }

        buffer.append("\n}").append("\n");

        return buffer.toString();
    }

    /**
     *
     */
    private String getComparatorClass() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n").append("\n/**").append("\n * This class is the comparator for domain object ").append("corresponding to the ").append(myTable).append(" table").append(
            "\n * in the database.").append("\n * ").append("\n * @author  : ").append("\n * @version : $Id: $").append("\n * ").append("\n */").append("\nclass ").append(myClass).append(
            "Comparator implements Comparator<").append(myClass).append(">").append("\n{").append("\n    public int compare(").append(myClass).append(" obj1, ").append(myClass).append(
            " obj2)").append("\n    {").append("\n        return ").append("obj1.compareTo(obj2);").append("\n    }").append("\n    ").append("\n    public boolean equals(").append(myClass).append(
            " o)").append("\n    {").append("\n        return this.equals(o);").append("\n    }").append("\n}\n");

        return buffer.toString();
    }

    /**
     *
     */
    private String getComparisonLogic() {
        StringBuilder buffer = new StringBuilder();
        int           size   = myDMList.size();

        for (int i = 0; i < size; i++) {
            DataMember dm = myDMList.get(i);

            buffer.append(dm.getComparisionLogic());
        }

        return buffer.toString();
    }

    /**
     * Returns the constructor definitions
     */
    private String getConstructors() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n    /**").append("\n     * The default constructor.").append("\n     */").append("\n    public ").append(myClass).append("()").append("\n    {").append("\n    }").append(
            "\n").append(getParamConstructor());

        return buffer.toString();
    }

    /**
     * Returns the data member declarations.
     */
    private String getDMDecl() {
        StringBuilder buffer = new StringBuilder();
        int           size   = myDMList.size();

        for (int i = 0; i < size; i++) {
            DataMember dm       = myDMList.get(i);
            String     type     = dm.getType();
            int        stuffLen = myMaxTypeLength - type.length();
            String     stuff    = "";

            for (int j = 0; j < stuffLen; j++) {
                stuff = stuff + " ";
            }

            type = type + stuff;
            buffer.append("\n").append("private ").append(type).append(" my").append(dm.getName()).append(";");
        }

        // System.out.println(buffer);
        return buffer.toString();
    }

    /**
     *
     */
    private String getDMDeclForCon() {
        StringBuilder buffer = new StringBuilder();
        int           size   = myDMList.size();

        for (int i = 0; i < size; i++) {
            DataMember dm       = myDMList.get(i);
            String     type     = dm.getType();
            int        stuffLen = myMaxTypeLength - type.length();
            String     stuff    = "";

            for (int j = 0; j < stuffLen; j++) {
                stuff = stuff + " ";
            }

            type = type + stuff;
            buffer.append(type).append(" a").append(dm.getName()).append(",\n");
        }

        String value = buffer.toString();

        value = value.substring(0, value.lastIndexOf(","));

        return value;
    }

    /**
     *
     */
    private String getDatabaseFunctions() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(getInsertMethod()).append(getUpdateMethod());

        return buffer.toString();
    }

    /**
     *
     */
    private String getDocParams() {
        StringBuilder buffer = new StringBuilder();
        int           size   = myDMList.size();

        for (int i = 0; i < size; i++) {
            DataMember dm = myDMList.get(i);

            buffer.append("\n @param a").append(dm.getName());
        }

        return buffer.toString();
    }

    /**
     * Returns the Header and Copy Right info.
     */
    private String getFileDocAndCopyRight() {
        StringBuilder buffer   = new StringBuilder();
        String        fileName = myClass + ".java";

        buffer.append("/*\n * ").append(fileName).append("\n *\n * $Header:").append("\n/*").append("\n * Copyright (c) 2005 Transbit Technologies Pvt. Ltd.  ").append("All rights reserved.").append(
            "\n *").append("\n * This software is the confidential and proprietary ").append("information").append("\n * of Transbit Technologies Pvt. Ltd. (\"Confidential Information\").").append(
            "  You").append("\n * shall not disclose such Confidential Information ").append("and shall use").append("\n * it only in accordance with the terms of the ").append(
            "license agreement").append("\n * you entered into with Transbit Technologies Pvt. Ltd.").append("\n */");

        return buffer.toString();
    }

    /**
     *
     */
    private String getGetters() {
        StringBuilder buffer = new StringBuilder();
        int           size   = myDMList.size();

        for (int i = 0; i < size; i++) {
            DataMember dm = myDMList.get(i);

            buffer.append(dm.getGetter());
        }

        return buffer.toString();
    }

    /**
     * Returns the necessary import statements.
     */
    private String getImportStmts() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("// Imports from the current package.\n").append("\n").append("// Other TBits Imports.\n").append("import transbit.tbits.common.DataSourcePool;\n");

        if (myHasTimestamp == true) {
            buffer.append("import transbit.tbits.common.Timestamp;\n");
        }

        buffer.append("\n").append("// Java Imports.\n").append("import java.sql.CallableStatement;\n").append("import java.sql.Connection;\n").append("import java.sql.ResultSet;\n").append(
            "import java.sql.SQLException;\n").append("\n").append("import java.util.ArrayList;\n").append("import java.util.Arrays;\n").append("import java.util.Comparator;\n").append("\n").append(
            "import java.util.logging.Logger;\n").append("\n").append("// Third party imports.\n\n");

        return buffer.toString();
    }

    /**
     *
     */
    private String getInsertMethod() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n").append("\n    /**").append("\n     * Method to insert a ").append(myClass).append(" object into database.").append("\n     *").append(
            "\n     * @param aObject Object to be inserted").append("\n     *").append("\n     */").append("\n    public static boolean insert(").append(myClass).append(" aObject)").append(
            "\n    {").append("\n        // Insert logic here.").append("\n        if (aObject == null) return false;").append("\n        ").append("\n        Connection aCon = null;").append(
            "\n        boolean returnValue = false;").append("\n        try").append("\n        {").append("\n            aCon = DataSourcePool.getConnection();").append(
            "\n            CallableStatement cs = ").append("aCon.prepareCall(\"stp_").append(myTable).append("_insert ").append(getPlaceHolders()).append("\");").append(
            "\n            aObject.setCallableParameters(cs);").append("\n            cs.execute();").append("\n            cs.close();").append("\n            returnValue = true;").append(
            "\n        }").append("\n        catch (SQLException sqle)").append("\n        {").append("\n            returnValue = false;").append("\n        }").append("\n        finally").append(
            "\n        {").append("\n            if (aCon != null)").append("\n            {").append("\n                try").append("\n                {").append(
            "\n                    aCon.close();").append("\n                }").append("\n                catch(SQLException sqle)").append("\n                {").append(
            "\n                    // Should this be logged.?").append("\n                }").append("\n            }").append("\n        }").append("\n        return returnValue;").append(
            "\n    }").append("\n");

        return buffer.toString();
    }

    /**
     *
     */
    private String getMemberFunctions() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(getGetters().replaceAll("\n", "\n    ")).append(getSetters().replaceAll("\n", "\n    ")).append(getCSFunction().replaceAll("\n", "\n    ")).append("");

        return buffer.toString();
    }

    /**
     * Returns the package declartion statement.
     */
    private String getPackageDecl() {
        StringBuilder buffer = new StringBuilder();

        if ((myPackage != null) && (myPackage.equals("") == false)) {
            buffer.append("\npackage ").append(myPackage).append(";\n\n");
        }

        return buffer.toString();
    }

    /**
     * Returns the parameterized constructor definition
     */
    private String getParamConstructor() {
        StringBuilder buffer   = new StringBuilder();
        String        conDecl  = "     public " + myClass + "(";
        String        stuff    = "";
        int           stuffLen = conDecl.length();

        for (int i = 0; i < stuffLen; i++) {
            stuff = stuff + " ";
        }

        buffer.append("\n    /**").append("\n     * The complete constructor.").append("\n     * ").append(getDocParams().replaceAll("\n",
                "\n     * ")).append("\n     */").append("\n").append(conDecl).append(getDMDeclForCon().replaceAll("\n",
                    "\n" + stuff)).append(")").append("\n    {").append(getAssignmentStmts().replaceAll("\n", "\n        ")).append("\n    }").append("\n");

        return buffer.toString();
    }

    /**
     *
     */
    private String getPlaceHolders() {
        StringBuilder buffer = new StringBuilder();
        int           size   = myDMList.size();

        for (int i = 0; i < size; i++) {
            buffer.append("?, ");
        }

        String value = buffer.toString();

        value = value.substring(0, value.lastIndexOf(","));

        return value;
    }

    /**
     *
     */
    private String getSetters() {
        StringBuilder buffer = new StringBuilder();
        int           size   = myDMList.size();

        for (int i = 0; i < size; i++) {
            DataMember dm = myDMList.get(i);

            buffer.append(dm.getSetter());
        }

        return buffer.toString();
    }

    /**
     *
     */
    private String getSortFunctions() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n    /**").append("\n     * Mutator method for SortField property.").append("\n     *").append("\n     * @param aSortField New Value of SortField").append("\n     *").append(
            "\n     */").append("\n    public static void setSortField(int aSortField)").append("\n    {").append("\n        ourSortField = aSortField;").append("\n    }").append("\n").append(
            "\n    /**").append("\n     * Mutator method for SortOrder property.").append("\n     *").append("\n     * @param aSortOrder New Value of SortOrder").append("\n     *").append(
            "\n     */").append("\n    public static void setSortOrder(int aSortOrder)").append("\n    {").append("\n        ourSortOrder = aSortOrder;").append("\n    }").append("\n").append(
            "\n    /**").append("\n     * Mutator method for ourSortField and ").append("ourSortOrder properties.").append("\n     *").append(
            "\n     * @param aSortField New Value of SortField").append("\n     * @param aSortOrder New Value of SortOrder").append("\n     *").append("\n     */").append(
            "\n    public static void setSortParams(int aSortField, ").append("int aSortOrder)").append("\n    {").append("\n        ourSortField = aSortField;").append(
            "\n        ourSortOrder = aSortOrder;").append("\n    }").append("\n").append("\n").append("\n    /**").append("\n     * Method to return the source arraylist in the ").append(
            "sorted order").append("\n     *").append("\n     * @param  source the array list of Type objects").append("\n     * @return the ArrayList of the ").append(myClass).append(
            " objects in sorted order").append("\n     */").append("\n    public static ArrayList<").append(myClass).append("> sort(ArrayList<").append(myClass).append("> source)").append(
            "\n    {").append("\n        int size = source.size();").append("\n        ").append(myClass).append(" [] srcArray = new ").append(myClass).append("[size];").append(
            "\n        for (int i=0;i<size;i++)").append("\n            srcArray[i] = source.get(i);").append("\n        Arrays.sort(srcArray, new ").append(myClass).append("Comparator());").append(
            "\n        ArrayList<").append(myClass).append("> target = new ArrayList<").append(myClass).append(">();").append("\n        for (int i=0;i<size;i++)").append(
            "\n            target.add(srcArray[i]);").append("\n        return target;").append("\n    }").append("\n    /**").append("\n     * Method that compares this object with the one ").append(
            "passed W.R.T the ourSortField.").append("\n     *").append("\n     * @param aObject  Object to be compared.").append("\n     *").append("\n     * @return 0 - If they are equal.").append(
            "\n     *         1 - If this is greater.").append("\n     *        -1 - If this is smaller.").append("\n     *").append("\n     */").append("\n    public int compareTo(").append(
            myClass).append(" aObject)").append("\n    {").append("\n        switch(ourSortField)").append("\n        {").append(getComparisonLogic().replaceAll("\n", "\n        ")).append(
            "\n        }").append("\n        return 0;").append("\n    }");

        return buffer.toString();
    }

    /**
     * Returns the static declaration statements.
     */
    private String getStaticDecl() {
        StringBuilder buffer = new StringBuilder();

        buffer.

        // Add the logger object.
        append("\n    public static final Logger LOG = ").append("\n        Logger.getLogger(\"").append(myPackage).append("\");\n").append("\n    // Static attributes related to sorting.").append(
            "\n    private static int ourSortField;").append("\n    private static int ourSortOrder;").append("\n    // Enum sort of fields for Attributes.");

        int size = myDMList.size();

        for (int i = 0; i < size; i++) {
            DataMember dm       = myDMList.get(i);
            String     aEnum    = dm.getEnum();
            int        aOrder   = dm.getOrder();
            int        stuffLen = myMaxNameLength - aEnum.length();
            String     stuff    = "";

            for (int j = 0; j < stuffLen; j++) {
                stuff = stuff + " ";
            }

            aEnum = aEnum + stuff;
            buffer.append("\n    private static final int ").append(aEnum).append(" = ").append((size > 9)
                    ? " "
                    : "").append(aOrder).append(";");
        }

        buffer.append("\n");

        return buffer.toString();
    }

    /**
     *
     */
    private String getStaticFunctions() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(getSortFunctions()).append(getDatabaseFunctions());

        return buffer.toString();
    }

    /**
     *
     */
    private String getUpdateMethod() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n").append("\n    /**").append("\n     * Method to update the corresponding ").append(myClass).append(" object in the database.").append("\n     *").append(
            "\n     * @param aObject Object to be updated").append("\n     *").append("\n     * @return Update domain object.").append("\n     *").append("\n     */").append(
            "\n    public static ").append(myClass).append(" update(").append(myClass).append(" aObject)").append("\n    {").append("\n        // Update logic here.").append(
            "\n        if (aObject == null) return aObject;").append("\n        ").append("\n        Connection aCon = null;").append("\n        try").append("\n        {").append(
            "\n            aCon = DataSourcePool.getConnection();").append("\n            CallableStatement cs = ").append("aCon.prepareCall(\"stp_").append(myTable).append("_update ").append(
            getPlaceHolders()).append("\");").append("\n            aObject.setCallableParameters(cs);").append("\n            cs.execute();").append("\n            cs.close();").append(
            "\n        }").append("\n        catch (SQLException sqle)").append("\n        {").append("\n        }").append("\n        finally").append("\n        {").append(
            "\n            if (aCon != null)").append("\n            {").append("\n                try").append("\n                {").append("\n                    aCon.close();").append(
            "\n                }").append("\n                catch(SQLException sqle)").append("\n                {").append("\n                    // Should this be logged.?").append(
            "\n                }").append("\n            }").append("\n        }").append("\n        return aObject;").append("\n    }").append("\n");

        return buffer.toString();
    }
}
