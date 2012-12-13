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
 * SyncDependentTypes.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DatabaseException;

//TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.DependencyConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Dependency;
import transbit.tbits.domain.ExternalResource;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.external.Resource;
import transbit.tbits.external.ResourceAttr;
import transbit.tbits.external.ResourceResultMap;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

//~--- classes ----------------------------------------------------------------

/**
 *
 *
 * @author  Vaibhav
 * @version $Id: $
 */
public class SyncDependentTypes {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    private SyncDependentTypes() {
        
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Main method for starting the execution of this class.
     *
     * @param args
     */
    public static int main(String[] args) {
        try {
            SyncDependentTypes sysDepTypes = new SyncDependentTypes();
            sysDepTypes.process();
        } finally {            
        }
        
        return 0;//System.exit(0);
    }

    /**
     *
     * @param aIter
     * @param aPrimAttrList
     * @return
     */
    private Hashtable<String, ResourceResultMap> prepareAttrTable(Iterator<ResourceResultMap> aIter, ArrayList<String> aPrimAttrList) {
        Hashtable<String, ResourceResultMap> attrTable = new Hashtable<String, ResourceResultMap>();

        while (aIter.hasNext()) {
            ResourceResultMap rrm   = aIter.next();
            StringBuffer      key   = new StringBuffer();
            boolean           first = true;

            for (String col : aPrimAttrList) {
                ResourceAttr attr = rrm.get(col);

                if (attr == null) {
                    continue;
                }

                Object temp = attr.getValue();

                if (temp == null) {
                    continue;
                }

                String keyPart = temp.toString();

                if (first == false) {
                    key.append("-");
                } else {
                    first = false;
                }

                key.append(keyPart);
            }

            attrTable.put(key.toString(), rrm);
        }

        // LOG.info(toStringHT(attrTable));
        return attrTable;
    }

    /**
     *
     * @param aList
     * @param aPrimCols
     * @return
     */
    private Hashtable<String, Type> prepareTypeTable(ArrayList<Type> aList, ArrayList<String> aPrimCols) {
        Hashtable<String, Type> typesTable = new Hashtable<String, Type>();

        for (Type type : aList) {
            StringBuffer key   = new StringBuffer();
            boolean      first = true;

            for (String col : aPrimCols) {
                Object temp = type.get(col);

                if (temp == null) {
                    continue;
                }

                String keyPart = temp.toString();

                if (first == false) {
                    key.append("-");
                } else {
                    first = false;
                }

                key.append(keyPart);
            }

            typesTable.put(key.toString(), type);
        }

        return typesTable;
    }

    /**
     *
     */
    public int process() {
        try {

            /*
             * Get the App-Sync dependencies in the system.
             */
            ArrayList<Dependency> list = Dependency.getAppSyncDependencies();

            for (Dependency dep : list) {
                if (dep == null) {
                    continue;
                }

                DependencyConfig dc = dep.getDepConfigObject();

                if (dc == null) {
                    continue;
                }

                /*
                 * For each App-Sync dependency in the system:
                 *
                 * 1. Get the BusinessArea and Field objects corresponding
                 *    to this dependency.
                 *
                 * 2. Get the name of the external resource that is used for
                 *    sync'ing purpose.
                 *
                 * 3.
                 */
                int          systemId  = dep.getSystemId();
                String       fieldName = dc.getSyncFieldName();
                BusinessArea ba        = null;
                Field        field     = null;

                try {
                    ba    = BusinessArea.lookupBySystemId(systemId);
                    field = Field.lookupBySystemIdAndFieldName(systemId, fieldName);
                } catch (DatabaseException de) {
                    LOG.severe("",(de));

                    continue;
                }

                int              fieldId = field.getFieldId();
                ExternalResource eres    = null;
                Resource         res     = null;
                String           resName = dc.getResourceName();

                if (resName == null) {
                    continue;
                }

                ExternalResource er = ExternalResource.lookupByName(resName);

                if (er == null) {
                    throw new TBitsException("Invalid resource: " + resName);
                }

                Resource resource = er.getResource();

                resource.realizeResource(null);
                LOG.info("Sync'ing " + field.getDisplayName() + " in " + ba.getDisplayName() + " using " + resName + " resource.");

                Hashtable<String, String>            syncMap      = dc.getSyncMap();
                Hashtable<String, ArrayList<String>> asyncMap     = dc.getAsyncMap();
                ArrayList<String>                    primColList  = dc.getPrimaryColumnList();
                ArrayList<String>                    primAttrList = dc.getPrimaryAttributeList();

                /*
                 * Sync mapping and primary key mapping are required.
                 */
                if ((syncMap == null) || (syncMap.size() == 0)) {
                    throw new TBitsException("Sync map is empty.");
                }

                if ((primColList == null) || (primColList.size() == 0) || (primAttrList == null) || (primAttrList.size() == 0)) {
                    throw new TBitsException("Primary Key map is empty.");
                }

                StringBuffer            info       = new StringBuffer();
                ArrayList<Type>         typeList   = Type.lookupBySystemIdAndFieldName(systemId, fieldName);
                Hashtable<String, Type> typesTable = null;

                if ((typeList == null) || (typeList.size() == 0)) {
                    info.append("[ No types initially ]");
                    typeList   = new ArrayList<Type>();
                    typesTable = new Hashtable<String, Type>();
                } else {
                    typesTable = prepareTypeTable(typeList, primColList);
                }

                Iterator<ResourceResultMap>          iter      = resource;
                Hashtable<String, ResourceResultMap> attrTable = null;

                attrTable = prepareAttrTable(iter, primAttrList);

                ArrayList<Type> newTypes = new ArrayList<Type>();
                ArrayList<Type> updTypes = new ArrayList<Type>();

                /*
                 * Iterate the Types table
                 */
                Enumeration<String> keys = typesTable.keys();

                while (keys.hasMoreElements()) {
                    String key  = keys.nextElement();
                    Type   type = typesTable.get(key);

                    if (type == null) {
                        LOG.info("Strange!!! No value for key: " + key);

                        continue;
                    }

                    // Get the ResourceResultMap corresponding to this key.
                    ResourceResultMap rrm = attrTable.get(key);

                    if (rrm == null) {
                        LOG.info("No Result for key: " + key);

                        /*
                         * There is no row corresponding to this type in the
                         * external resource. Run all async mappings on this
                         * type and add it to the list of types to be updated.
                         */
                        runAsyncTypeMap(asyncMap, type, 0);
                        updTypes.add(type);
                    } else {

                        /*
                         * rrm is the map corresponding to this type. Run all
                         * Sync mappings on this type and the map.
                         */
                        boolean changed = syncTypeAndMap(rrm, type, syncMap, asyncMap);

                        // Add this to the list of types to be updated, if
                        // changed.
                        if (changed == true) {
                            updTypes.add(type);
                        }

                        // Remove it from the map.
                        attrTable.remove(key);
                    }
                }    // End While Type Keys.

                /*
                 * Anything left in attrTable has to be inserted as a new type.
                 */
                ArrayList<ResourceResultMap> newRows = new ArrayList<ResourceResultMap>(attrTable.values());

                for (ResourceResultMap rrm : newRows) {
                    Type type = new Type();

                    type.setSystemId(systemId);
                    type.setFieldId(fieldId);
                    syncTypeAndMap(rrm, type, syncMap, asyncMap);
                    newTypes.add(type);
                }

                // Sort the types to be inserted on their display name.
                Type.setSortParams(Type.DISPLAYNAME, 0);
                newTypes = Type.sort(newTypes);

                /*
                 * Insert the types.
                 */
                for (Type type : newTypes) {
                    Type.insert(type);
                }

                if (newTypes.size() == 0) {
                    info.append("[ No new entries found ]");
                } else {
                    info.append("[ New Entries: " + newTypes.size() + " ]");
                }

                /*
                 * Update the types.
                 */
                for (Type type : updTypes) {
                    Type.update(type);
                }

                if (updTypes.size() != 0) {
                    info.append("[ Updated: " + updTypes.size() + " entries ]");
                }

                LOG.info(info);
            }
        } catch (Exception e) {
            LOG.warn("",(e));
            return 1;//System.exit(1);
        }
        return 0;
    }

    /**
     *
     * @param asyncMap
     * @param type
     * @param index
     */
    private void runAsyncTypeMap(Hashtable<String, ArrayList<String>> asyncMap, Type type, int index) {
        if ((index != 0) && (index != 1)) {
            return;
        }

        Enumeration<String> asyncKeys = asyncMap.keys();

        while (asyncKeys.hasMoreElements()) {
            String            asyncKey = asyncKeys.nextElement();
            ArrayList<String> list     = asyncMap.get(asyncKey);

            if ((list == null) || (list.size() != 2)) {
                continue;
            }

            String asyncValue = list.get(index);

            type.set(asyncKey, asyncValue);
        }

        return;
    }

    /**
     *
     * @param rrm
     * @param type
     * @param syncMap
     * @return
     */
    private boolean syncTypeAndMap(ResourceResultMap rrm, Type type, Hashtable<String, String> syncMap, Hashtable<String, ArrayList<String>> asyncMap) {
        Enumeration<String> syncKeys = syncMap.keys();
        boolean             changed  = false;

        while (syncKeys.hasMoreElements()) {
            String       syncKey   = syncKeys.nextElement();
            String       syncValue = syncMap.get(syncKey);
            ResourceAttr attr      = rrm.get(syncValue);

            if (attr != null) {
                String newValue = attr.getValue().toString();
                String oldValue = type.get(syncKey).toString();

                if (newValue.equals(oldValue) == false) {
                    changed = true;
                    type.set(syncKey, newValue);
                }
            }
        }

        Enumeration<String> asyncKeys = asyncMap.keys();

        while (asyncKeys.hasMoreElements()) {
            String            asyncKey = asyncKeys.nextElement();
            ArrayList<String> list     = asyncMap.get(asyncKey);

            if ((list == null) || (list.size() != 2)) {
                continue;
            }

            String asyncValue = list.get(1);
            String typeValue  = type.get(asyncKey).toString();

            if (asyncValue.equals(typeValue) == false) {
                type.set(asyncKey, asyncValue);
                changed = true;
            }
        }

        return changed;
    }
}
