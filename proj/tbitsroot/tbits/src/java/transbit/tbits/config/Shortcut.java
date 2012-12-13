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
 * Shortcut.java
 *
 * $Header:
 *
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import transbit.tbits.Helper.Messages;

//Imports from other packages in TBits
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_CONFIG;
import static transbit.tbits.config.XMLParserUtil.getAttrBooleanValue;

//Static imports.
import static transbit.tbits.config.XMLParserUtil.getAttributeValue;
//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the information that makes a search shortcut.
 *
 * @author  Vaibhav.
 * @version $Id: $
 *
 */
public class Shortcut implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    // Enum sort of fields for Attributes.
    public static final int NAME         = 1;
    public static final int VIEW         = 2;
    public static final int TEXT         = 4;
    public static final int QUERY        = 3;
    public static final int IsBASHORTUCT = 9;
    public static final int ISPUBLIC     = 7;
    public static final int ISLISTALL    = 8;
    public static final int ISDEFAULT    = 6;
    public static final int FILTER       = 5;

    // Static attributes related to sorting.
    private static int ourSortField = NAME;
    private static int ourSortOrder = ASC_ORDER;

    //~--- fields -------------------------------------------------------------

    private String  myFilter;
    private boolean myIsBAShortcut;
    private boolean myIsDefault;
    private boolean myIsListAll;
    private boolean myIsPublic;

    // Attributes of this Domain Object.
    private String myName;
    private String myQuery;
    private String myText;
    private int    myView;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public Shortcut() {}

    /**
     * The complete constructor.
     *
     *  @param aName
     *  @param aView
     *  @param aQuery
     *  @param aText
     *  @param aFilter
     *  @param aIsDefault
     *  @param aIsPublic
     *  @param aIsListAll
     */
    public Shortcut(String aName, int aView, String aQuery, String aText, String aFilter, boolean aIsDefault, boolean aIsPublic, boolean aIsListAll, boolean aIsBAShortcut) {
        myName         = aName;
        myView         = aView;
        myQuery        = aQuery;
        myText         = aText;
        myFilter       = aFilter;
        myIsDefault    = aIsDefault;
        myIsPublic     = aIsPublic;
        myIsListAll    = aIsListAll;
        myIsBAShortcut = aIsBAShortcut;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method that compares this object with the one passed W.R.T the
     * ourSortField.
     *
     * @param aObject  Object to be compared.
     *
     * @return 0 - If they are equal.
     *         1 - If this is greater.
     *        -1 - If this is smaller.
     *
     */
    public int compareTo(Shortcut aObject) {
        switch (ourSortField) {
        case NAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myName.compareTo(aObject.myName);
            } else {
                return aObject.myName.compareTo(myName);
            }
        }

        case VIEW : {
            Integer i1 = new Integer(myView);
            Integer i2 = new Integer(aObject.myView);

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            } else {
                return i2.compareTo(i1);
            }
        }

        case QUERY : {
            if (ourSortOrder == ASC_ORDER) {
                return myQuery.compareTo(aObject.myQuery);
            } else {
                return aObject.myQuery.compareTo(myQuery);
            }
        }

        case ISDEFAULT : {
            Boolean b1 = myIsDefault;
            Boolean b2 = aObject.myIsDefault;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            } else {
                return b2.compareTo(b1);
            }
        }

        case ISPUBLIC : {
            Boolean b1 = myIsPublic;
            Boolean b2 = aObject.myIsPublic;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            } else {
                return b2.compareTo(b1);
            }
        }

        case ISLISTALL : {
            Boolean b1 = myIsListAll;
            Boolean b2 = aObject.myIsListAll;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            } else {
                return b2.compareTo(b1);
            }
        }

        case IsBASHORTUCT : {
            Boolean b1 = myIsBAShortcut;
            Boolean b2 = aObject.myIsBAShortcut;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            } else {
                return b2.compareTo(b1);
            }
        }
        }

        return 0;
    }

    /**
     * This method deletes the shortcut from the specified business araa config
     * of the given user.
     *
     * @param aUser       user to whom the shortcut belongs to.
     * @param aSysPrefix  business area prefix the shortcut belongs to.
     * @param aName       Name of the shortcut.
     *
     * @return True  if the shortcut is successfully deleted.
     *         False otherwise.
     */
    public static boolean deleteShortcut(User aUser, String aSysPrefix, String aName) throws TBitsException {
        try {

            // Get the user web config.
            WebConfig webConfig = aUser.getWebConfigObject();

            // Get the BAConfig for this BA.
            BAConfig baConfig = webConfig.getBAConfig(aSysPrefix);

            // Get the shortcut corresponding to the given name.
            Shortcut shortcut = baConfig.getShortcut(aName);

            if (shortcut == null) {
                throw new TBitsException(Messages.getMessage("INVALID_SHORTCUT"));
            }

            Hashtable<String, Shortcut> sctable = baConfig.getShortcuts();

            sctable.remove(aName.toUpperCase());
            baConfig.setShortcuts(sctable);
            webConfig.setBAConfig(aSysPrefix, baConfig);

            String xml = webConfig.xmlSerialize();

            aUser.setWebConfig(xml);
            User.update(aUser);

            return true;
        } catch (Exception e) {
            LOG.info("",(e));
        }

        return false;
    }

    /**
     * This method parses the Shortcut node and returns a Shortcut object.
     *
     * @param aNode  Node pointing to the Shortcut node XML.
     *
     * @return Shortcut object.
     */
    public static Shortcut parseShortcutNode(Node aNode) {
        Shortcut     sc    = new Shortcut();
        NamedNodeMap nnmap = aNode.getAttributes();
        String       name  = getAttributeValue(aNode, "name");

        sc.setName(Utilities.htmlDecode(name));

        String strView = getAttributeValue(aNode, "view");
        int view = 0;
        try {
            view = Integer.parseInt(strView);
        } catch (NumberFormatException nfe) {
            
        }
        finally
        {
            sc.setView(view);
        }
        
        String query = getAttributeValue(aNode, "query");

        sc.setQuery(Utilities.htmlDecode(query));

        String text = getAttributeValue(aNode, "text");

        sc.setText(Utilities.htmlDecode(text));

        String filter = getAttributeValue(aNode, "filter");

        sc.setFilter(Utilities.htmlDecode(filter));

        boolean isDefault = getAttrBooleanValue(aNode, "isDefault");

        sc.setIsDefault(isDefault);

        boolean isPublic = getAttrBooleanValue(aNode, "isPublic");

        sc.setIsPublic(isPublic);

        boolean listAll = getAttrBooleanValue(aNode, "listAll");

        sc.setIsListAll(listAll);

        boolean baShortcut = getAttrBooleanValue(aNode, "baShortcut");

        sc.setIsBAShortcut(baShortcut);

        return sc;
    }

    /**
     * This method creates a shortcut object and stores it in the specified
     * user's webconfig.
     *
     * @param aUser       user to whom the shortcut belongs to.
     * @param aSysPrefix  business area prefix the shortcut belongs to.
     * @param aName       Name of the shortcut.
     * @param aQuery      Query definition of the shortcut.
     * @param aDesc       Text part if any present in the shortcut.
     * @param aFilter     Filter value when the shortcut was saved.
     * @param aView       View of the search page when the shortcut is saved.
     * @param aIsDefault  Default property of the shortcut.
     * @param aIsPublic   Public property of the shortcut.
     * @param aIsListAll  ListAll property of the shortcut.
     *
     * @return True  if the shortcut is successfully deleted.
     *         False otherwise.
     */
    public static boolean saveShortcut(User aUser, BusinessArea aBA, String aSysPrefix, String aName, String aQuery, String aDesc, String aFilter, int aView, boolean aIsDefault, boolean aIsPublic,
                                       boolean aIsListAll, boolean aIsBAShortcut) {
        try {

            // Create and compose a shortcut object.
            Shortcut shortcut = new Shortcut();

            shortcut.setName(aName);
            shortcut.setView(aView);
            shortcut.setQuery(aQuery);
            shortcut.setText(aDesc);
            shortcut.setFilter(aFilter);
            shortcut.setIsDefault(aIsDefault);
            shortcut.setIsPublic(aIsPublic);
            shortcut.setIsListAll(aIsListAll);
            shortcut.setIsBAShortcut(aIsBAShortcut);

            /*
             * If this is a BA shortcut store this in the SysConfig.
             */
            if (aIsBAShortcut == false) {

                // Get the user web config.
                WebConfig webConfig = aUser.getWebConfigObject();

                // Get the BAConfig for this BA.
                BAConfig baConfig = webConfig.getBAConfig(aSysPrefix);

                /*
                 * Check if this new shortcut is marked the default one.
                 */
                if (aIsDefault == true) {

                    /*
                     * get the default one and mark the default property of that
                     * to false.
                     */
                    Hashtable<String, Shortcut> list = baConfig.getShortcuts();

                    if (list != null) {
                        Shortcut def = baConfig.getDefaultShortcut();

                        if (def != null) {
                            def.setIsDefault(false);
                            list.put(def.getName().toUpperCase(), def);
                            baConfig.setShortcuts(list);
                        }
                    }
                }

                baConfig.addShortcut(shortcut);
                webConfig.setBAConfig(aSysPrefix, baConfig);

                String xml = webConfig.xmlSerialize();

                aUser.setWebConfig(xml);
                User.update(aUser);
            } else {
                SysConfig sysConfig = aBA.getSysConfigObject();

                sysConfig.addShortcut(shortcut);

                String xml = sysConfig.xmlSerialize();

                aBA.setSysConfig(xml);
                BusinessArea.update(aBA);
            }

            return true;
        } catch (Exception e) {
            LOG.info("",(e));
        }

        return false;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the Shortcut objects in sorted order
     */
    public static ArrayList<Shortcut> sort(ArrayList<Shortcut> source) {
        int        size     = source.size();
        Shortcut[] srcArray = new Shortcut[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new ShortcutComparator());

        ArrayList<Shortcut> target = new ArrayList<Shortcut>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    public String xmlSerialize() {
        StringBuilder xml = new StringBuilder();

        xml.append("\n\t\t<Shortcut ").append("\n\t\t\tname=\"").append(Utilities.htmlEncode(myName)).append("\"").append("\n\t\t\tview=\"").append(myView).append("\"").append(
            "\n\t\t\tquery=\"").append(Utilities.htmlEncode(myQuery)).append("\"").append("\n\t\t\ttext=\"").append(Utilities.htmlEncode(myText)).append("\"").append("\n\t\t\tfilter=\"").append(
            myFilter).append("\"").append("\n\t\t\tisDefault=\"").append(myIsDefault).append("\"").append("\n\t\t\tisPublic=\"").append(myIsPublic).append("\"").append("\n\t\t\tlistAll=\"").append(
            myIsListAll).append("\"").append("\n\t\t\tbaShortcut=\"").append(myIsBAShortcut).append("\"").append("\n\t\t/>");

        return xml.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for Filter property.
     *
     * @return Current Value of Filter
     *
     */
    public String getFilter() {
        return myFilter;
    }

    /**
     * Accessor method for IsBAShortcut property.
     *
     * @return Current Value of IsBAShortcut
     */
    public boolean getIsBAShortcut() {
        return myIsBAShortcut;
    }

    /**
     *  Accessor method for IsDefault property.
     *
     *  @return Current Value of IsDefault
     *
     */
    public boolean getIsDefault() {
        return myIsDefault;
    }

    /**
     * Accessor method for IsListAll property.
     *
     * @return Current Value of IsListAll
     */
    public boolean getIsListAll() {
        return myIsListAll;
    }

    /**
     * Accessor method for IsPublic property.
     *
     * @return Current Value of IsPublic
     *
     */
    public boolean getIsPublic() {
        return myIsPublic;
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
     * Accessor method for Query property.
     *
     * @return Current Value of Query
     *
     */
    public String getQuery() {
        return myQuery;
    }

    /**
     * This method returns the shortcut object if present in the BAConfig of
     * of specified prefix of given user's config or otherwise null.
     *
     * @param aReqUser    User who requested for this shortcut.
     * @param aUserLogin  user to whom the shortcut belongs to.
     * @param aSysPrefix  business area prefix the shortcut belongs to.
     * @param aName       Name of the shortcut.
     *
     * @return Shortcut  if the shortcut is present
     *         null otherwise.
     */
    public static Shortcut getShortcut(String aReqUser, String aUserLogin, String aSysPrefix, String aName) {
        Shortcut shortcut = null;

        try {
            User aUser = User.lookupByUserLogin(aUserLogin);

            if (aUser != null) {

                // Get the user web config.
                WebConfig webConfig = aUser.getWebConfigObject();

                // Get the BAConfig for this BA.
                BAConfig baConfig = webConfig.getBAConfig(aSysPrefix);

                // Get the shortcut corresponding to the given name.
                shortcut = baConfig.getShortcut(aName);

                if (shortcut != null) {
                    if (shortcut.getIsPublic() == false) {
                        if (aReqUser.equalsIgnoreCase(aUserLogin) == false) {
                            shortcut = null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.info("",(e));
        }

        return shortcut;
    }

    /**
     * Accessor method for Text property.
     *
     * @return Current Value of Text
     *
     */
    public String getText() {
        return myText;
    }

    /**
     * Accessor method for View property.
     *
     * @return Current Value of View
     *
     */
    public int getView() {
        return myView;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for Filter property.
     *
     * @param aFilter New Value for Filter
     *
     */
    public void setFilter(String aFilter) {
        myFilter = aFilter;
    }

    /**
     * Mutator method for IsBAShortcut property.
     *
     * @param aIsBAShortcut New Value for IsBAShortcut
     */
    public void setIsBAShortcut(boolean aIsBAShortcut) {
        myIsBAShortcut = aIsBAShortcut;
    }

    /**
     * Mutator method for IsDefault property.
     *
     * @param aIsDefault New Value for IsDefault
     *
     */
    public void setIsDefault(boolean aIsDefault) {
        myIsDefault = aIsDefault;
    }

    /**
     * Mutator method for IsListAll property.
     *
     * @param aIsListAll New Value for IsListAll
     */
    public void setIsListAll(boolean aIsListAll) {
        myIsListAll = aIsListAll;
    }

    /**
     * Mutator method for IsPublic property.
     *
     * @param aIsPublic New Value for IsPublic
     *
     */
    public void setIsPublic(boolean aIsPublic) {
        myIsPublic = aIsPublic;
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
     * Mutator method for Query property.
     *
     * @param aQuery New Value for Query
     *
     */
    public void setQuery(String aQuery) {
        myQuery = aQuery;
    }

    /**
     * Mutator method for ourSortField and ourSortOrder properties.
     *
     * @param aSortField New Value of SortField
     * @param aSortOrder New Value of SortOrder
     *
     */
    public static void setSortParams(int aSortField, int aSortOrder) {
        ourSortField = aSortField;
        ourSortOrder = aSortOrder;
    }

    /**
     * Mutator method for Text property.
     *
     * @param aText New Value for Text
     *
     */
    public void setText(String aText) {
        myText = aText;
    }

    /**
     * Mutator method for View property.
     *
     * @param aView New Value for View
     *
     */
    public void setView(int aView) {
        myView = aView;
    }
}


/**
 * This class is the comparator for domain object corresponding to the  table
 * in the database.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
class ShortcutComparator implements Comparator<Shortcut>, Serializable {
    public int compare(Shortcut obj1, Shortcut obj2) {
        return ((Shortcut) obj1).compareTo(obj2);
    }
}
