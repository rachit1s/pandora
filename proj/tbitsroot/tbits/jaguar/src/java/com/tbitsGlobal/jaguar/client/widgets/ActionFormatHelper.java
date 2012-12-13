package com.tbitsGlobal.jaguar.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.store.ListStore;
import com.tbitsGlobal.jaguar.client.JaguarUtils;
import com.tbitsGlobal.jaguar.client.UserHeaderRecord;
import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sutta
 * 
 * A class that formats the actions to be displayed in View Request
 */
public class ActionFormatHelper {
	
	/**
	 * Formats Header Description in action.
	 * @param aActionLog
	 * @param sysPrefix
	 * @param fieldStore
	 * @param linkMap
	 * @return Formatted String
	 */
	public static String formatActionLog(String aActionLog, String sysPrefix, ListStore<BAField> fieldStore, HashMap<String, ClickableLink> linkMap){
        String  LINE_BREAK   = GlobalConstants.HTML_LINE_BREAK;

        String     headerDesc = "";
        ArrayList<String> hdrLogList = ClientUtils.toArrayList(aActionLog, "\n");
        int               size       = hdrLogList.size();

        try {
            for (int i = 0; i < size; i++) {
                String aLine = hdrLogList.get(i);

                aLine = ClientUtils.htmlEncode(aLine);

                if (aLine.indexOf("##") < 0) {
                    headerDesc += aLine + LINE_BREAK;

                    continue;
                }

                // Header is of format <FieldName>##<FieldId>##trackingInfo
                int    index     = aLine.indexOf("##");
                String fieldName = aLine.substring(0, index).trim();

                aLine = aLine.substring(index + 2).trim();
                index = aLine.indexOf("##");

                String desc    = aLine.substring(index + 2).trim();

                // patch to hyperlink parent Id in action log
                if (fieldName.equals(IFixedFields.PARENT_REQUEST_ID) == true) {
                    desc = desc.replaceAll("'([1-9][0-9]*)'", sysPrefix + "#'$1'");
                }

                /*
                 * Check if this fieldName starts with +/-/*. Such entries
                 * corresponding to the logs for user fields and have to be
                 * dealt with separately.
                 */
                else if (fieldName.startsWith("*") || fieldName.startsWith("+") || fieldName.startsWith("-")) {
                    String actFieldName = fieldName.substring(1);
                    BAField field = fieldStore.findModel(BAField.NAME, actFieldName);
                    if(field != null){
	                    // Skip if user does not have permssion to view this field.
	                    if(hasPermissions(field) == false )
	                    {
	                    	continue;	             
	                    }
                    }
                    
                    
                    HashMap<String, String> eTable = new HashMap<String, String>();

                    eTable.put(fieldName, desc);
                    i = i + 1;

                    while (i < size) {

                        /*
                         * Check if the next one starts with +/-/*.
                         */
                        aLine = hdrLogList.get(i);

                        if (aLine.startsWith("*") || aLine.startsWith("+") || aLine.startsWith("-")) {
                            index     = aLine.indexOf("##");
                            fieldName = aLine.substring(0, index).trim();

                            String curFieldName = fieldName.substring(1);

                            /*
                             * Check if we are dealing with the same field
                             * as actFieldName.
                             */
                            if (curFieldName.equals(actFieldName) == false) {

                                /*
                                 * We are dealing with a different user field.
                                 * This means we are done with processing log
                                 * records corresponding to this user field.
                                 * Its time to come out of the loop and generate
                                 * final log record for this user field. Before
                                 * that put back the item read, i.e. decrement
                                 * the counter and come out of the loop.
                                 */
                                i = i - 1;

                                break;
                            }

                            /*
                             * This log record corresponds to the same field
                             * as actFieldName. Put this in the table and
                             * check the next one, i.e. increment the counter.
                             */
                            aLine   = aLine.substring(index + 2).trim();
                            index   = aLine.indexOf("##");
                            desc    = aLine.substring(index + 2).trim();
                            eTable.put(fieldName, desc);
                            i = i + 1;
                        } else {

                            /*
                             * We are no more dealing with user fields.
                             * Put back the item read, i.e. decrement the
                             * counter and come out of the loop.
                             */
                            i = i - 1;

                            break;
                        }
                    }

                    /*
                     * Form the header description for this user field.
                     */
                    headerDesc += formUserHeader(field, eTable, LINE_BREAK);

                    continue;
                }
                headerDesc += desc + LINE_BREAK;
            }
        } catch (Exception e) {
            Log.error("", e);

            return "";
        }
        return JaguarUtils.hyperSmartLinks(headerDesc, linkMap);
    }
	
	private static String formUserHeader(BAField field, HashMap<String, String> aTable, String aLineBreak) {
		String aHeaderDesc = "";
        ArrayList<UserHeaderRecord> recList = new ArrayList<UserHeaderRecord>();

        // Check for common list.
        String commonList = aTable.get("*" + field.getName());

        if ((commonList != null) && (commonList.trim().equals("") == false)) {
            processUserHeader(recList, commonList, UserHeaderRecord.EntryType.COMMON);
        }

        // Check for added list.
        String addedList = aTable.get("+" + field.getName());

        if ((addedList != null) && (addedList.trim().equals("") == false)) {
            processUserHeader(recList, addedList, UserHeaderRecord.EntryType.ADDED);
        }

        /*
         * In DFlow#2726#7, Peter wanted us to sort the existing and the
         * newly added entries to be sorted based on their ordering but
         * display the deleted entries at the end.
         *
         * So let us sort the recList before adding the deleted entries.
         */
        UserHeaderRecord.setSortParams(UserHeaderRecord.ORDERING, GlobalConstants.ASC_ORDER);
        recList = UserHeaderRecord.sort(recList);

        // Check for deleted list.
        String deletedList = aTable.get("-" + field.getName());

        if ((deletedList != null) && (deletedList.trim().equals("") == false)) {
            processUserHeader(recList, deletedList, UserHeaderRecord.EntryType.DELETED);
        }

        if (recList.size() == 0) {
            return aHeaderDesc;
        }

        String        displayName   = field.getDisplayName();
        StringBuilder formattedList = new StringBuilder();
        boolean       first         = true;

        for (UserHeaderRecord record : recList) {
            String userLogin = record.getUserLogin();

            userLogin = userLogin.replace(".transbittech.com", "");

            if (first == false) {
                formattedList.append(", ");
            } else {
                first = false;
            }

            switch (record.getEntryType()) {
            case COMMON :
                formattedList.append(userLogin);

                break;

            case ADDED :
                formattedList.append("<b>").append(userLogin).append("</b>");

                break;

            case DELETED :
                formattedList.append("<s>").append(userLogin).append("</s>");

                break;
            }
        }

        aHeaderDesc += "[ " + displayName + ": " + formattedList + " ]" + aLineBreak;
        
        return aHeaderDesc;
    }
	
	private static void processUserHeader(ArrayList<UserHeaderRecord> aList, String aStrList, UserHeaderRecord.EntryType aType) {

        /*
         * remove the leading and trailing square brackets.
         */
        if (aStrList.startsWith("[")) {
            aStrList = aStrList.substring(1);
        }

        if (aStrList.endsWith("]")) {
            aStrList = aStrList.substring(0, aStrList.length() - 1);
        }

        // Split them on comma.
        ArrayList<String>           entries   = ClientUtils.toArrayList(aStrList, ",");
        ArrayList<UserHeaderRecord> localList = new ArrayList<UserHeaderRecord>();

        for (String entry : entries) {

            // Each entry is of the form Ordering:Login.
            String[] arr = entry.split(":");

            if ((arr == null) || (arr.length != 2)) {
                Log.warn("Invalid format of user header entry: " + entry);

                continue;
            }

            int    ordering  = 0;
            String userLogin = arr[1];

            try {
                ordering = Integer.parseInt(arr[0]);
            } catch (NumberFormatException nfe) {
            	Log.warn("Exception while parsing the ordering field: " + nfe);
                ordering = 0;
            }

            UserHeaderRecord record = new UserHeaderRecord(aType, ordering, userLogin);

            localList.add(record);
        }

        /*
         * If these are DELETED entries, then sort them before adding them to
         * aList.
         */
        if (aType == UserHeaderRecord.EntryType.DELETED) {
            UserHeaderRecord.setSortParams(UserHeaderRecord.ORDERING, GlobalConstants.ASC_ORDER);
            localList = UserHeaderRecord.sort(localList);
        }

        // Finally add the localList formed to aList.
        aList.addAll(localList);
        localList = null;

        return;
    }
	
	/**
    *
    * @param permTable
    * @param fieldName
    * @return
    */
   private static boolean hasPermissions(BAField field) {

       //
       // User can view request only if view on private is granted
       // Hence we can always show its action_log. This a is done to avoid
       // False header message "Few fields not shown..etc" in emails.
       if (field.getName().equals(IFixedFields.IS_PRIVATE)) {
           return true;
       }

       return field.isCanViewInBA();
   }
}
