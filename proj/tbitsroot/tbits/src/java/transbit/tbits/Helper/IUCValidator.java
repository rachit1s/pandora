/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.DateTimeParser;

//Package Imports
//TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

//Java imports
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.HtmlCleaner;

//~--- classes ----------------------------------------------------------------

/**
 * This class is used to validate inline update commands.
 *
 * @author Vinod Gupta
 * @version $Id: $
 */
public class IUCValidator {

    // Application Logger.
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    //~--- methods ------------------------------------------------------------

    /*
     *
     */
    private static void addEucError(Field aField, String aOperand, boolean aIsPrivate, ArrayList<String> aFailedEUCs, ArrayList<String> aBadSyntaxEUCs, ArrayList<String> aNotAllowedPrivateEUCs) {
        String          fieldName = aField.getName();
        FieldDescriptor fd        = null;

        try {
            fd = FieldDescriptor.getPrimaryDescriptor(aField.getSystemId(), aField.getName());
        } catch (Exception ex1) {
            LOG.severe("",(ex1));
        }

        if (fd != null) {
            fieldName = fd.getDescriptor();
        }

        if ((aField.getDataTypeId() == DataType.USERTYPE) && (aIsPrivate == true)) {
            aNotAllowedPrivateEUCs.add("/" + fieldName + " " + aOperand + "\n");
        } else {
            aBadSyntaxEUCs.add("/" + fieldName + " " + aOperand + "\n");
        }

        aFailedEUCs.add("/" + fieldName + " " + aOperand + "\n");
    }

    /*
     *
     */
    public static String checkUserInAuthList(String aLogin, ArrayList<String> aAuthUsersForRequest) {
        if (aAuthUsersForRequest == null) {
            return null;
        }

        if (aAuthUsersForRequest.contains(aLogin) == true) {
            return aLogin;
        } else {
            return null;
        }
    }

    /**
     * Main method.
     *
     */
    public static void main(String[] args) {}

    /*
     * Check if line matches Euc pattern and valid descriptors.
     */
    private static boolean matchLineForEUCPattern(Pattern aPattern, String aLine, int aCounter1, int aCounter2, Hashtable<Field, String> aMetaData, boolean aAllowTypeEUC, boolean aIsPrivate,
            boolean aIsPrivateByBA, BusinessArea aBusinessArea, ArrayList<String> aFailedEUCs, ArrayList<String> aAllEUCs, ArrayList<String> aBadDescriptorEUCs, ArrayList<String> aNotAllowedEUCs,
            ArrayList<String> aNotAllowedPrivateEUCs)
            throws DatabaseException {
        Field   field    = null;
        int     systemId = aBusinessArea.getSystemId();
        Matcher m        = aPattern.matcher(aLine);

        //
        // If line matches pattern
        //
        if (m.find() == true) {
            String desc = ((m.group(2) == null)
                           ? ""
                           : m.group(2)).trim();
            String op   = ((m.group(7) == null)
                           ? ""
                           : m.group(7)).trim();

            field = FieldDescriptor.lookupBySystemIdAndDescriptor(systemId, desc);

            if ((field == null) && op.equals("")) {
                Type type = Type.lookupBySystemIdAndMinimalMatch(systemId, desc);

                if (type != null) {
                    field = Field.lookupBySystemIdAndFieldId(systemId, type.getFieldId());
                    op    = type.getName();
                }
            }

            if ((field == null) && (desc.length() >= 3)) {
                field = Field.lookupBySystemIdAndMinimalMatch(systemId, desc);
            }

            //
            // If descriptor not valid, and
            // 1) if its first pattern, exit with description as is
            // 2) else add to the FailedEUCs list and look for
            // next pattern and
            //
            if (field == null) {
                if (aCounter1 == aCounter2) {
                    return false;
                } else {
                    aFailedEUCs.add(aLine + "\n");
                    aAllEUCs.add(aLine + "\n");
                    aBadDescriptorEUCs.add(aLine + "\n");

                    return true;
                }
            }

            //
            // If D-action not allowed on this field,
            // add to the FailedEUCs list and look for
            // next pattern
            //
            else if ((field.getPermission() & Permission.D_ACTION) == 0) {
                aFailedEUCs.add(aLine + "\n");
                aAllEUCs.add(aLine + "\n");
                aNotAllowedEUCs.add(aLine + "\n");

                return true;
            }

            //
            // If type EUC not allowed
            // add to the FailedEUCs list and look for
            // next pattern
            //
//            else if ((field.getDataTypeId() == DataType.TYPE) && (aAllowTypeEUC == false)) {
//                aFailedEUCs.add(aLine + "\n");
//                aAllEUCs.add(aLine + "\n");
//                aNotAllowedPrivateEUCs.add(aLine + "\n");
//
//                return true;
//            }

            //
            // private EUC not allowed on private requests
            //
            else if ((field.getName().equals(Field.IS_PRIVATE) == true) && (aIsPrivate == true)) {
                aFailedEUCs.add(aLine + "\n");
                aAllEUCs.add(aLine + "\n");
                aNotAllowedPrivateEUCs.add(aLine + "\n");

                return true;
            } else {
                aAllEUCs.add(aLine + "\n");

                if (field.getDataTypeId() == DataType.USERTYPE) {

                    //
                    // multiple eucs allowed for multi-value fields
                    // joining them together using delim "/###/"
                    //
                    String prev = aMetaData.get(field);

                    if (prev != null) {
                        op = prev + "/###/" + op;
                    }
                }

                aMetaData.put(field, op);

                return true;
            }
        }

        // If line doesn't match pattern, it marks the end of d-actions
        // parsing.
        else {
            return false;
        }
    }

    /*
     * This method looks for Eucs from Bottom
     */
    private static boolean parseEUCFromBottom(StringBuilder aSb, String aContent, Hashtable<Field, String> aMetaData, boolean aIsAppend, boolean aIsPrivate, boolean aIsPrivateByBA,
            BusinessArea aBusinessArea, ArrayList<String> aFailedEUCs, ArrayList<String> aAllEUCs, ArrayList<String> aBadDescriptorEUCs, ArrayList<String> aNotAllowedEUCs,
            ArrayList<String> aNotAllowedPrivateEUCs)
            throws DatabaseException {
        boolean allowTypeEUC = true;

        // Allow type EUCs for public requests or all requests within
        // private BA
        if ((aIsPrivate == false) || (aIsPrivateByBA == true)) {
            allowTypeEUC = true;
        }
        
        HtmlCleaner cleaner = new HtmlCleaner();
        String textContent = "";
        try {
			textContent = cleaner.clean(aContent).getText().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        Pattern  p1        = Pattern.compile("\r?\n");
        String[] lines     = p1.split(textContent);
        Pattern  p         = Pattern.compile("^( *)/([^ :\r\n]+)(( *)([ |:])( *)(.*))?");
        boolean  foundEucs = false;

        // Looking Euc from end by setting i = lines.length -1.
        int len = lines.length - 1;
        int i   = len;

        //
        // This will conatin eucs, parsed from bottom of email
        // These then needs to be looked from top to bottom order.
        //
        ArrayList<String> eucs = new ArrayList<String>();

        //
        // get lines that match pattern.
        //
        for (; i >= 0; i--) {
            if (i == len) {
                boolean matched = matchLineForEUCPattern(p, lines[i], len, i, new Hashtable<Field, String>(), allowTypeEUC, aIsPrivate, aIsPrivateByBA, aBusinessArea, new ArrayList<String>(),
                                      new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());

                if (matched == false) {
                    aSb.append(aContent);

                    return false;
                } else {
                    eucs.add(lines[i]);
                    String iuc = lines[i];
                    if(aContent.indexOf(iuc) != -1){
                    	aContent = aContent.replace(iuc, "");
                    }
                }
            } else {
                Matcher m = p.matcher(lines[i]);

                //
                // If line matches pattern
                //
                if (m.find() == true) {
                    eucs.add(lines[i]);
                    String iuc = lines[i];
                    if(aContent.indexOf(iuc) != -1){
                    	aContent = aContent.replace(iuc, "");
                    }
                } else {
                    break;
                }
            }
        }
        
        aSb.append(aContent);

//        //
//        // Ignore blank lines after D-actions
//        //
//        for (; i >= 0; i--) {
//            if (lines[i].trim().equals("") == false) {
//                break;
//            }
//        }

//        //
//        // Rest is all description
//        //
//        for (int j = 0; j <= i; j++) {
//            aSb.append(lines[j]).append("\n");
//        }

        //
        // Parse lines to match pattern.
        //
        int size = eucs.size();

        for (int k = size - 1; k >= 0; k--) {
            boolean matched = matchLineForEUCPattern(p, eucs.get(k), len, i, aMetaData, allowTypeEUC, aIsPrivate, aIsPrivateByBA, aBusinessArea, aFailedEUCs, aAllEUCs, aBadDescriptorEUCs,
                                  aNotAllowedEUCs, aNotAllowedPrivateEUCs);

            if (matched == false) {
                break;
            } else if (matched == true) {
                foundEucs = true;
            }
        }

        return foundEucs;
    }

    /*
     * This method looks for Eucs from Top
     */
    private static boolean parseEUCFromTop(StringBuilder aSb, String aContent, Hashtable<Field, String> aMetaData, boolean aIsAppend, boolean aIsPrivate, boolean aIsPrivateByBA,
            BusinessArea aBusinessArea, ArrayList<String> aFailedEUCs, ArrayList<String> aAllEUCs, ArrayList<String> aBadDescriptorEUCs, ArrayList<String> aNotAllowedEUCs,
            ArrayList<String> aNotAllowedPrivateEUCs)
            throws DatabaseException {
        boolean allowTypeEUC = false;

        // Allow type EUCs for public requests or all requests within
        // private BA
        if ((aIsPrivate == false) || (aIsPrivateByBA == true)) {
            allowTypeEUC = true;
        }
        
        HtmlCleaner cleaner = new HtmlCleaner();
        String textContent = "";
        try {
			textContent = cleaner.clean(aContent).getText().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Pattern  p1        = Pattern.compile("\r?\n");
        String[] lines     = p1.split(textContent);
        Pattern  p         = Pattern.compile("^( *)/([^ :\r\n]+)(( *)([ |:])( *)(.*))?");
        boolean  foundEucs = false;
        
        // Looking Euc from begining by setting i = 0.
        int i = 0;

        //
        // Parse lines to match pattern.
        //
        for (; i < lines.length; i++) {
            boolean matched = matchLineForEUCPattern(p, lines[i], 0, i, aMetaData, allowTypeEUC, aIsPrivate, aIsPrivateByBA, aBusinessArea, aFailedEUCs, aAllEUCs, aBadDescriptorEUCs, aNotAllowedEUCs,
                                  aNotAllowedPrivateEUCs);

            if ((i == 0) && (matched == false)) {
                aSb.append(aContent);

                return false;
            } else if (matched == false) {
                break;
            } else if (matched == true) {
                foundEucs = true;
                String iuc = lines[i];
                if(aContent.indexOf(iuc) != -1){
                	aContent = aContent.replace(iuc, "");
                }
            }
        }
        
        aSb.append(aContent);

//        //
//        // Ignore blank lines after D-actions
//        //
//        for (; i < lines.length; i++) {
//            if (lines[i].trim().equals("") == false) {
//                break;
//            }
//        }

//        //
//        // Rest is all description
//        //
//        for (; i < lines.length; i++) {
//            aSb.append(lines[i]).append("\n");
//        }

        return foundEucs;
    }

    /**
     * This method parse the description to extract all d-actions patterns and
     * return a list of valid d-action directives from the patterns list.
     *
     * @param aContent  the description string
     * @param aMetaData table which will have parsed d-actions
     * @param aIsAppend boolean true, if mail is an append
     *
     * @return the description string after removing all d-actions
     *
     * @exception DatabaseException
     */
    public static String parseEUCMetaData(String aContent, Hashtable<Field, String> aMetaData, boolean aIsAppend, BusinessArea aBusinessArea) throws DatabaseException {
        aContent = parseEUCMetaData(aContent, aMetaData, aIsAppend, false, false, aBusinessArea, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(),
                                    new ArrayList<String>());

        return aContent;
    }

    /**
     * This method parse the description to extract all d-actions patterns and
     * return a list of valid d-action directives from the patterns list.
     *
     * @param aContent  the description string
     * @param aMetaData table which will have parsed d-actions
     * @param aIsAppend boolean true, if mail is an append
     *
     * @return the description string after removing all d-actions
     *
     * @exception DatabaseException
     */
    public static String parseEUCMetaData(String aContent, Hashtable<Field, String> aMetaData, boolean aIsAppend, boolean aIsPrivate, boolean aIsPrivateByBA, BusinessArea aBusinessArea,
            ArrayList<String> aFailedEUCs, ArrayList<String> aAllEUCs, ArrayList<String> aBadDescriptorEUCs, ArrayList<String> aNotAllowedEUCs, ArrayList<String> aNotAllowedPrivateEUCs)
            throws DatabaseException {
        if ((aContent == null) || (aContent.trim().equals("") == true)) {
            return "";
        }

        // trimming spaces and lines from beginning and end
        aContent = aContent.trim();
        
        StringBuilder sb = new StringBuilder();

        parseEUCFromTop(sb, aContent, aMetaData, aIsAppend, aIsPrivate, aIsPrivateByBA, aBusinessArea, aFailedEUCs, aAllEUCs, aBadDescriptorEUCs, aNotAllowedEUCs, aNotAllowedPrivateEUCs);
        aContent = sb.toString();
        sb       = new StringBuilder();
        parseEUCFromBottom(sb, aContent, aMetaData, aIsAppend, aIsPrivate, aIsPrivateByBA, aBusinessArea, aFailedEUCs, aAllEUCs, aBadDescriptorEUCs, aNotAllowedEUCs, aNotAllowedPrivateEUCs);

        return sb.toString();
    }

    public static String validateBooleanField(String aOperand) {
        String validOp = null;

        if ((aOperand == null) || (aOperand.trim().equals("") == true)) {
            return "true";
        }

        Pattern booleanPattern = Pattern.compile("^(yes|y|no|n|true|t|false|f|0|1)([,;])?");
        Matcher m              = booleanPattern.matcher(aOperand);

        if (m.matches()) {
            validOp = m.group(1).toLowerCase();

            if (validOp.equals("yes") || validOp.equals("y") || validOp.equals("true") || validOp.equals("t") || validOp.equals("1")) {
                validOp = "true";
            } else if (validOp.equals("no") || validOp.equals("n") || validOp.equals("false") || validOp.equals("f") || validOp.equals("0")) {
                validOp = "false";
            }
        }

        return validOp;
    }

    public static String validateDateField(String aOperand, boolean aIsAppend, String aOldValue, String aFieldName) {
        String validOp = null;
        String dStr    = "";
        int    yy      = 0;
        int    MM      = 0;
        int    dd      = 0;
        int    hh      = 23;
        int    mm      = 59;
        int    ss      = 0;

        if ((aOldValue != null) &&!aOldValue.equals("")) {
            ArrayList<String> tokens = Utilities.toArrayList(aOldValue, "- :");

            yy = Integer.parseInt(tokens.get(0));
            MM = Integer.parseInt(tokens.get(1));
            dd = Integer.parseInt(tokens.get(2));
            hh = Integer.parseInt(tokens.get(3));
            mm = Integer.parseInt(tokens.get(4));
            ss = Integer.parseInt(tokens.get(5));

            if (MM != 0) {
                MM = MM - 1;
            }
        }

        if (aOperand.equals("") || aOperand.equals("=") || aOperand.equalsIgnoreCase("null") || aOperand.equalsIgnoreCase("none") || aOperand.equalsIgnoreCase("\"\"")) {
            return "";
        }

        if (aOperand.startsWith("+") || aOperand.startsWith("=")) {
            dStr = aOperand.substring(1).trim();
        } else {
            dStr = aOperand;
        }

        Hashtable<String, Date> dateWithFormat = DateTimeParser.parse(dStr);

        if (dateWithFormat != null) {
            String format = dateWithFormat.keys().nextElement();
            Date   date   = dateWithFormat.elements().nextElement();

            LOG.debug(format);
            LOG.debug(date);

            if (format.equals("SHORT_DATE_TIME")) {
                if (date.before(new Date()) == true) {
                    Calendar cal  = Calendar.getInstance();
                    int      year = cal.get(Calendar.YEAR) + 1;

                    cal.setTime(date);
                    cal.set(Calendar.YEAR, year);
                    date = cal.getTime();
                }
            }

            if (format.equals("SHORT_DATE")) {
                Calendar cal  = Calendar.getInstance();
                int      year = cal.get(Calendar.YEAR) + 1;

                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, hh);
                cal.set(Calendar.MINUTE, mm);
                cal.set(Calendar.SECOND, ss);
                date = cal.getTime();

                if (date.before(new Date()) == true) {
                    cal.set(Calendar.YEAR, year);
                    date = cal.getTime();
                }
            }

            if (format.equals("TIME")) {
                Calendar cal   = Calendar.getInstance();
                int      year  = cal.get(Calendar.YEAR);
                int      month = cal.get(Calendar.MONTH);
                int      day   = cal.get(Calendar.DAY_OF_MONTH);

                cal.setTime(date);

                if ((aOldValue != null) &&!aOldValue.equals("")) {
                    int year1  = cal.get(Calendar.YEAR);
                    int month1 = cal.get(Calendar.MONTH);
                    int day1   = cal.get(Calendar.DAY_OF_MONTH);

                    cal.set(Calendar.YEAR, year1 + (yy - year));
                    cal.set(Calendar.MONTH, month1 + (MM - month));
                    cal.set(Calendar.DAY_OF_MONTH, day1 + (dd - day));
                    date = cal.getTime();
                } else if (date.before(new Date()) == true) {
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    date = cal.getTime();
                }
            }

            if (format.equals("DATE")) {
                Calendar cal = Calendar.getInstance();

                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, hh);
                cal.set(Calendar.MINUTE, mm);
                cal.set(Calendar.SECOND, ss);
                date = cal.getTime();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            validOp = sdf.format(date);

            return validOp;
        } else {
            if ((aOperand.indexOf("=") != -1) && ((aOperand.indexOf("+") != -1) || (aOperand.indexOf("-") != -1))) {
                return null;
            }

            ArrayList<String> tokenList = Utilities.toArrayList(aOperand.toLowerCase(), ",; ");
            Calendar          cal       = Calendar.getInstance();

            if ((aOldValue != null) &&!aOldValue.equals("")) {
                cal.set(Calendar.YEAR, yy);
                cal.set(Calendar.MONTH, MM);
                cal.set(Calendar.DAY_OF_MONTH, dd);
                cal.set(Calendar.HOUR_OF_DAY, hh);
                cal.set(Calendar.MINUTE, mm);
                cal.set(Calendar.SECOND, ss);
            }

            Pattern p = Pattern.compile("([-+])?([0-9]+)([a-zA-Z]+)");

            for (String token : tokenList) {
                if (token.equals("")) {
                    continue;
                }

                Matcher m = p.matcher(token);

                if (m.matches()) {
                    int    val1 = Integer.parseInt(m.group(2));
                    String val2 = m.group(3);
                    char   op   = ((m.group(1) == null)
                                   ? '+'
                                   : m.group(1).charAt(0));

                    LOG.debug(op + " " + val1 + " " + val2);

                    if (val2.equals("m") || val2.equals("min") || val2.equals("minute") || val2.equals("minutes")) {
                        int min = cal.get(Calendar.MINUTE);

                        if (op == '+') {
                            cal.set(Calendar.MINUTE, min + val1);
                        } else if (op == '-') {
                            cal.set(Calendar.MINUTE, min - val1);
                        }
                    } else if (val2.equals("h") || val2.equals("hr") || val2.equals("hour") || val2.equals("hours")) {
                        int hr = cal.get(Calendar.HOUR_OF_DAY);

                        if (op == '+') {
                            cal.set(Calendar.HOUR_OF_DAY, hr + val1);
                        } else if (op == '-') {
                            cal.set(Calendar.HOUR_OF_DAY, hr - val1);
                        }
                    } else if (val2.equals("d") || val2.equals("day") || val2.equals("days")) {
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        if (op == '+') {
                            cal.set(Calendar.DAY_OF_MONTH, day + val1);
                        } else if (op == '-') {
                            cal.set(Calendar.DAY_OF_MONTH, day - val1);
                        }
                    } else if (val2.equals("w") || val2.equals("wk") || val2.equals("week") || val2.equals("weeks")) {
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        if (op == '+') {
                            cal.set(Calendar.DAY_OF_MONTH, day + val1 * 7);
                        } else if (op == '-') {
                            cal.set(Calendar.DAY_OF_MONTH, day - val1 * 7);
                        }
                    } else if (val2.equals("mth") || val2.equals("mon") || val2.equals("month") || val2.equals("months")) {
                        int mon = cal.get(Calendar.MONTH);

                        if (op == '+') {
                            cal.set(Calendar.MONTH, mon + val1);
                        } else if (op == '-') {
                            cal.set(Calendar.MONTH, mon - val1);
                        }
                    } else if (val2.equals("y") || val2.equals("yr") || val2.equals("year") || val2.equals("years")) {
                        int year = cal.get(Calendar.YEAR);

                        if (op == '+') {
                            cal.set(Calendar.YEAR, year + val1);
                        } else if (op == '-') {
                            cal.set(Calendar.YEAR, year - val1);
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            validOp = sdf.format(cal.getTime());

            return validOp;
        }
    }

    /**
     * This method validate EUCs pattern based on the field type
     *
     * @param aRequestHash The Hashtable to store <field-name, value>
     * @param aDactionMetaData the d-action <field,operand> values to be parsed
     * @param aIsAppend boolean true, if mail is an append
     */
    public static void validateEUCs(Hashtable<String, String> aRequestHash, Hashtable<Field, String> aDactionMetaData, boolean aIsAppend, Request aRequest, BusinessArea aBusinessArea,
                                    boolean aIsPrivate, boolean aIsPrivateByBA, ArrayList<String> aFailedEUCs, ArrayList<String> aBadSyntaxEUCs, ArrayList<String> aNotAllowedPrivateEUCs,
                                    ArrayList<String> aAuthUsersForRequest) {
        LOG.info("Eucs to be processed:\n" + aDactionMetaData);

        Field              field    = null;
        String             validOp  = null;
        String             aOperand = null;
        String             oldValue = null;
        Enumeration<Field> e        = aDactionMetaData.keys();

        while (e.hasMoreElements()) {
            validOp  = null;
            field    = e.nextElement();
            aOperand = aDactionMetaData.get(field).trim();

            if (aIsAppend == true) {
                oldValue = aRequest.get(field.getName());
            } else {
                oldValue = null;
            }

            try {
                switch (field.getDataTypeId()) {
                case DataType.BOOLEAN :
                    validOp = validateBooleanField(aOperand.toLowerCase());

                    break;

                case DataType.DATE :
                case DataType.TIME :
                case DataType.DATETIME :
                    validOp = validateDateField(aOperand.toLowerCase(), aIsAppend, oldValue, field.getName());

                    break;

                case DataType.INT :
                    validOp = validateIntField(aOperand, aIsAppend, oldValue, field, aBusinessArea);

                    break;

                case DataType.REAL :
                    validOp = validateRealField(aOperand, aIsAppend, oldValue, field);

                    break;

                case DataType.STRING :
                case DataType.TEXT :
                    if (field.getName().equals(Field.RELATED_REQUESTS)) {
                        validOp = validateSmartLinksField(aOperand, aIsAppend, oldValue, field, aBusinessArea);
                    } else {
                        validOp = validateTextField(aOperand);
                    }

                    break;

                case DataType.TYPE :
                    validOp = validateTypeField(aOperand.toLowerCase());

                    break;

                case DataType.USERTYPE :
                    String[] operand = aOperand.split("/###/");

                    for (int a = 0; a < operand.length; a++) {
                        String previousValue = aRequestHash.get(field.getName());

                        if ((aIsPrivate == true) && (aIsAppend == true)) {
                            validOp = validatePrivateMultiValueField(operand[a].toLowerCase(), oldValue, field, previousValue, aAuthUsersForRequest);
                        } else {
                            validOp = validateMultiValueField(operand[a].toLowerCase(), aIsAppend, oldValue, field, previousValue);
                        }

                        if (validOp != null) {
                            aRequestHash.put(field.getName(), validOp);
                        } else {
                            addEucError(field, operand[a], aIsPrivate, aFailedEUCs, aBadSyntaxEUCs, aNotAllowedPrivateEUCs);
                        }
                    }

                    continue;
                }

                if (validOp != null) {
                    aRequestHash.put(field.getName(), validOp);
                } else {
                    addEucError(field, aOperand, aIsPrivate, aFailedEUCs, aBadSyntaxEUCs, aNotAllowedPrivateEUCs);
                }
            } catch (Exception ex) {
                LOG.info("",(ex));
                addEucError(field, aOperand, aIsPrivate, aFailedEUCs, aBadSyntaxEUCs, aNotAllowedPrivateEUCs);
            }
        }
    }

    public static String validateIntField(String aOperand, boolean aIsAppend, String aOldValue, Field aField, BusinessArea aBusinessArea) {
        boolean isSet   = ((aField.getPermission() & Permission.SET) != 0);
        String  validOp = null;

        if (aField.getName().equals(Field.PARENT_REQUEST_ID)) {
            Pattern intPattern = Pattern.compile("(" + aBusinessArea.getSystemPrefix() + "#)?([0-9]+)([,;])?", Pattern.CASE_INSENSITIVE);
            Matcher m          = intPattern.matcher(aOperand);

            if (m.matches()) {
                validOp = m.group(2);
            }

            return validOp;
        } else if ((aIsAppend == false) || (isSet == false)) {
            Pattern intPattern = Pattern.compile("^([+=])?([0-9]+)([,;])?");
            Matcher m          = intPattern.matcher(aOperand);

            if (m.matches()) {
                validOp = m.group(2);
            }
        } else {
            if (aOperand.equals("") || aOperand.equals("=") || aOperand.equals("\"\"")) {
                validOp = "0";
            } else if ((aOperand.indexOf("=") != -1) && ((aOperand.indexOf("+") != -1) || (aOperand.indexOf("-") != -1))) {
                validOp = null;
            } else if (aOperand.indexOf("=") != -1) {
                Pattern p = Pattern.compile("(=)([0-9]+)([,;])?");
                Matcher m = p.matcher(aOperand);

                if (m.matches()) {
                    validOp = ((m.group(2) == null)
                               ? ""
                               : m.group(2));
                }
            } else {
                ArrayList<String> tokenList  = Utilities.toArrayList(aOperand, ",; ");
                Pattern           intPattern = Pattern.compile("([-+])?([0-9]+)");

                if (aOldValue == null) {
                    aOldValue = "0";
                }

                for (String token : tokenList) {
                    if (token.equals("")) {
                        continue;
                    }

                    Matcher m = intPattern.matcher(token);

                    if (m.matches() == true) {
                        char op = ((m.group(1) == null)
                                   ? '+'
                                   : m.group(1).charAt(0));

                        switch (op) {
                        case '+' :
                            validOp = "" + (Integer.parseInt((validOp == null)
                                                             ? "0"
                                                             : validOp) + Integer.parseInt(m.group(2)));

                            break;

                        case '-' :
                            validOp = "" + (Integer.parseInt((validOp == null)
                                                             ? "0"
                                                             : validOp) - Integer.parseInt(m.group(2)));

                            break;
                        }
                    } else {
                        return null;
                    }
                }

                validOp = "" + (Integer.parseInt(aOldValue) + Integer.parseInt(validOp));
            }
        }

        return validOp;
    }

    public static String validateMultiValueField(String aOperand, boolean aIsAppend, String aOldValue, Field aField, String aPreviousEuc) {
        boolean isSet   = ((aField.getPermission() & Permission.SET) != 0);
        String  validOp = null;

        if (aOperand.equals("") || aOperand.equals("=") || aOperand.equals("\"\"")) {
            validOp = "";

            return validOp;
        }

        /*
         * Check if "=" is mentioned with "+" or "-"
         * If yes, return null since its invalid to assign and do
         * relative operation in one IUC statement.
         */
        if (isOperatorsValid(aOperand) == false) {
            validOp = null;

            return validOp;
        }

        if (isSet == false) {
            aOldValue = "";
        }

        if (aOperand.indexOf("=") != -1) {
            Pattern p = Pattern.compile("(=)([^=]+)");
            Matcher m = p.matcher(aOperand);

            if (m.matches()) {
                validOp = ((m.group(2) == null)
                           ? ""
                           : m.group(2));
            }
        } else {
            ArrayList<String> tokenList         = Utilities.toArrayList(aOperand, ",; ");
            Pattern           multiValuePattern = Pattern.compile("([+-])?(\\*?[a-zA-Z][a-zA-Z0-9._@-]*)");

            if (aOldValue == null) {
                aOldValue = "";
            }

            ArrayList<String> List = Utilities.toArrayList(aOldValue, ",;");

            if (aPreviousEuc != null) {
                List = Utilities.toArrayList(aPreviousEuc, ",;");
            }

            for (String token : tokenList) {
                if (token.equals("")) {
                    continue;
                }

                Matcher m = multiValuePattern.matcher(token);

                if (m.find() == true) {
                    char   op  = ((m.group(1) == null)
                                  ? '+'
                                  : m.group(1).charAt(0));
                    String val = ((m.group(2) == null)
                                  ? ""
                                  : m.group(2));

                    switch (op) {
                    case '+' :

                        /*
                         * add user token to the list as is, validation
                         * of token happens in the API.
                         */
                        List.add(val);

                        break;

                    case '-' :

                        /*
                         * For removing user from list, validation of user
                         * token is done here since mailing-lists token
                         * may have been mentioned without domain and
                         * we need full logins to match and remove
                         * them from the list.
                         *
                         * 1. So if login(normal or as primary with '*')
                         *    is present, remove else validate to
                         *    get User.
                         * 2. If Token is invalid return null,
                         *    which is reported as invalid operand.
                         * 3. If Token is valid but not present in the list,
                         *    ignore as no-op.
                         */
                        if ((val.charAt(0) == '*') && (val.length() > 1)) {
                            val = val.substring(1);
                        }

                        if (List.contains(val) == true) {
                            List.remove(val);
                        } else if (List.contains("*" + val) == true) {
                            List.remove("*" + val);
                        } else {
                            User user = null;

                            try {
                                user = TBitsHelper.validateEmailUser(val);
                            } catch (DatabaseException dbe) {
                                LOG.severe("",(dbe));
                            } catch (IOException ioe) {
                                LOG.severe("",(ioe));
                            }

                            /*
                             * if token is a valid existing user and not
                             * to be newly added, remove from the list.
                             * (userId = -1 means external user to be added)
                             */
                            if ((user != null) && (user.getUserId() != -1)) {
                                val = user.getUserLogin();

                                if (List.contains(val) == true) {
                                    List.remove(val);
                                } else if (List.contains("*" + val) == true) {
                                    List.remove("*" + val);
                                } else {
                                    val = val.replace(".transbittech.com", "");
                                    List.remove(val);
                                    List.remove("*" + val);
                                }
                            } else {
                                return null;
                            }
                        }

                        break;
                    }
                } else {
                    return null;
                }
            }

            validOp = Utilities.arrayListToString(List);
        }

        return validOp;
    }

    public static String validatePrivateMultiValueField(String aOperand, String aOldValue, Field aField, String aPreviousEuc, ArrayList<String> aAuthUsersForRequest) throws DatabaseException {
        boolean isSet   = ((aField.getPermission() & Permission.SET) != 0);
        String  validOp = null;

        if (aOperand.equals("") || aOperand.equals("=") || aOperand.equals("\"\"")) {
            validOp = "";

            return validOp;
        }

        /*
         * Check if "=" is mentioned with "+" or "-"
         * If yes, return null since its invalid to assign and do
         * relative operation in one IUC statement.
         */
        if (isOperatorsValid(aOperand) == false) {
            validOp = null;

            return validOp;
        }

        if (isSet == false) {
            aOldValue = "";
        }

        if (aOperand.indexOf("=") != -1) {
            Pattern p = Pattern.compile("(=?)([^=]+)");
            Matcher m = p.matcher(aOperand);

            if (m.matches()) {
                String            op        = ((m.group(2) == null)
                                               ? ""
                                               : m.group(2));
                ArrayList<String> tokenList = Utilities.toArrayList(op, ",; ");

                for (String token : tokenList) {
                    if (token.equals("")) {
                        continue;
                    }

                    token = checkUserInAuthList(token, aAuthUsersForRequest);

                    if (token == null) {
                        return null;
                    }

                    validOp = ((validOp == null)
                               ? ""
                               : validOp + ",") + token;
                }
            }

            return validOp;
        } else {
            ArrayList<String> tokenList         = Utilities.toArrayList(aOperand, ",; ");
            Pattern           multiValuePattern = Pattern.compile("([+-])?(\\*?[a-zA-Z][a-zA-Z0-9._@-]*)");

            if (aOldValue == null) {
                aOldValue = "";
            }

            ArrayList<String> List = Utilities.toArrayList(aOldValue, ",;");

            if (aPreviousEuc != null) {
                List = Utilities.toArrayList(aPreviousEuc, ",;");
                LOG.severe("prev" + aPreviousEuc);
            }

            for (String token : tokenList) {
                if (token.equals("")) {
                    continue;
                }

                Matcher m = multiValuePattern.matcher(token);

                if (m.find() == true) {
                    char   op  = ((m.group(1) == null)
                                  ? '+'
                                  : m.group(1).charAt(0));
                    String val = ((m.group(2) == null)
                                  ? ""
                                  : m.group(2));

                    switch (op) {
                    case '+' :
                        val = checkUserInAuthList(val, aAuthUsersForRequest);

                        if (val == null) {
                            return null;
                        }

                        List.add(val);

                        break;

                    case '-' :

                        /*
                         * For removing user from list, validation of user
                         * token is done here since mailing-lists token
                         * may have been mentioned without domain and
                         * we need full logins to match and remove
                         * them from the list.
                         *
                         * 1. So if login(normal or as primary with '*')
                         *    is present, remove else validate to
                         *    get User.
                         * 2. If Token is invalid return null,
                         *    which is reported as invalid operand.
                         * 3. If Token is valid but not present in the list,
                         *    ignore as no-op.
                         */
                        if ((val.charAt(0) == '*') && (val.length() > 1)) {
                            val = val.substring(1);
                        }

                        if (List.contains(val) == true) {
                            List.remove(val);
                        } else if (List.contains("*" + val) == true) {
                            List.remove("*" + val);
                        } else {
                            User user = null;

                            try {
                                user = TBitsHelper.validateEmailUser(val);
                            } catch (DatabaseException dbe) {
                                LOG.severe("",(dbe));
                            } catch (IOException ioe) {
                                LOG.severe("",(ioe));
                            }

                            /*
                             * if token is a valid existing user and not
                             * to be newly added, remove from the list.
                             * (userId = -1 means external user to be added)
                             */
                            if ((user != null) && (user.getUserId() != -1)) {
                                val = user.getUserLogin();

                                if (List.contains(val) == true) {
                                    List.remove(val);
                                } else if (List.contains("*" + val) == true) {
                                    List.remove("*" + val);
                                } else {

                                    // val = val.replace(".transbittech.com", ""); //ritesh commented it
                                    List.remove(val);
                                    List.remove("*" + val);
                                }
                            } else {
                                return null;
                            }
                        }

                        break;
                    }
                } else {
                    return null;
                }
            }

            validOp = Utilities.arrayListToString(List);
        }

        LOG.severe("validOp:" + validOp);

        return validOp;
    }

    public static String validateRealField(String aOperand, boolean aIsAppend, String aOldValue, Field aField) {
        boolean isSet   = ((aField.getPermission() & Permission.SET) != 0);
        String  validOp = null;

        if ((aIsAppend == false) || (isSet == false)) {
            Pattern realPattern = Pattern.compile("^([+=])?([0-9]*)(.[0-9]+)?([,;])?");
            Matcher m           = realPattern.matcher(aOperand);

            if (m.matches()) {
                validOp = ((m.group(2) == null)
                           ? "0"
                           : m.group(2)) + ((m.group(3) == null)
                                            ? ".0"
                                            : m.group(3));
            }
        } else {
            if (aOperand.equals("") || aOperand.equals("=") || aOperand.equals("\"\"")) {
                validOp = "0.0";
            } else if ((aOperand.indexOf("=") != -1) && ((aOperand.indexOf("+") != -1) || (aOperand.indexOf("-") != -1))) {
                validOp = null;
            } else if (aOperand.indexOf("=") != -1) {
                Pattern p = Pattern.compile("(=)([0-9]*)(.[0-9]+)?([,;])?");
                Matcher m = p.matcher(aOperand);

                if (m.matches()) {
                    validOp = ((m.group(2) == null)
                               ? "0"
                               : m.group(2)) + ((m.group(3) == null)
                                                ? ".0"
                                                : m.group(3));
                }
            } else {
                ArrayList<String> tokenList   = Utilities.toArrayList(aOperand, ",; ");
                Pattern           realPattern = Pattern.compile("([-+])?([0-9]*)(.[0-9]+)?");

                if (aOldValue == null) {
                    aOldValue = "0.0";
                }

                for (String token : tokenList) {
                    if (token.equals("")) {
                        continue;
                    }

                    Matcher m = realPattern.matcher(token);

                    if (m.matches() == true) {
                        char   op  = ((m.group(1) == null)
                                      ? '+'
                                      : m.group(1).charAt(0));
                        String val = ((m.group(2) == null)
                                      ? "0"
                                      : m.group(2)) + ((m.group(3) == null)
                                                       ? ".0"
                                                       : m.group(3));

                        switch (op) {
                        case '+' :
                            validOp = "" + (Double.parseDouble((validOp == null)
                                                               ? "0"
                                                               : validOp) + Double.parseDouble(val));

                            break;

                        case '-' :
                            validOp = "" + (Double.parseDouble((validOp == null)
                                                               ? "0"
                                                               : validOp) - Double.parseDouble(val));

                            break;
                        }
                    } else {
                        return null;
                    }
                }

                validOp = "" + (Double.parseDouble(aOldValue) + Double.parseDouble(validOp));
            }
        }

        return validOp;
    }

    public static String validateSmartLinksField(String aOperand, boolean aIsAppend, String aOldValue, Field aField, BusinessArea aBusinessArea) {
        boolean isSet   = ((aField.getPermission() & Permission.SET) != 0);
        String  validOp = null;

        if (aOperand.equals("") || aOperand.equals("=") || aOperand.equals("\"\"")) {
            validOp = "";

            return validOp;
        }

        /*
         * Check if "=" is mentioned with "+" or "-"
         * If yes, return null since its invalid to assign and do
         * relative operation in one IUC statement.
         */
        if (isOperatorsValid(aOperand) == false) {
            validOp = null;

            return validOp;
        }

        if ((aIsAppend == false) || (isSet == false) || (aOperand.indexOf("=") != -1)) {
            if (aOperand.startsWith("=")) {
                aOperand = aOperand.substring(1);
            }

            Pattern           smartLinkPattern = Pattern.compile("([a-zA-Z0-9_]+#)?([0-9]+)(#([0-9]+))?", Pattern.CASE_INSENSITIVE);
            Matcher           m                = null;
            ArrayList<String> tokenList        = Utilities.toArrayList(aOperand, ",; ");

            for (String token : tokenList) {
                if (token.equals("")) {
                    continue;
                }

                token = LinkFormatter.replaceHrefWithSmartLinks(token);
                m     = smartLinkPattern.matcher(token);

                if (m.matches()) {
                    validOp = ((validOp == null)
                               ? ""
                               : validOp + ",") + m.group();
                } else {
                    return null;
                }
            }
        } else {
            if (aOldValue == null) {
                aOldValue = "";
            }

            ArrayList<String> List             = Utilities.toArrayList(aOldValue.toLowerCase(), ",;");
            Pattern           smartLinkPattern = Pattern.compile("([+-])?\\s*([a-zA-Z0-9_]+#)?([0-9]+)(#([0-9]+))?", Pattern.CASE_INSENSITIVE);
            Matcher           m                = null;
            ArrayList<String> tokenList        = Utilities.toArrayList(aOperand, ",; ");

            for (String token : tokenList) {
                if (token.equals("")) {
                    continue;
                }

                token = LinkFormatter.replaceHrefWithSmartLinks(token);
                m     = smartLinkPattern.matcher(token);

                if (m.matches()) {
                    char   op  = ((m.group(1) == null)
                                  ? '+'
                                  : m.group(1).charAt(0));
                    String val = ((m.group(2) == null)
                                  ? aBusinessArea.getSystemPrefix() + "#"
                                  : m.group(2)) + m.group(3) + ((m.group(4) == null)
                            ? ""
                            : m.group(4));

                    val = val.toLowerCase();

                    switch (op) {
                    case '+' :
                        List.add(val);

                        break;

                    case '-' :
                        List.remove(val);

                        break;
                    }
                } else {
                    return null;
                }
            }

            validOp = Utilities.arrayListToString(List);
        }

        return validOp;
    }

    public static String validateTextField(String aOperand) {
        String  validOp     = null;
        Pattern textPattern = Pattern.compile("^([+=])?(.*)([,;])?");
        Matcher m           = textPattern.matcher(aOperand);

        if (m.matches()) {
            validOp = (m.group(2) == null)
                      ? ""
                      : m.group(2).trim();
        }

        return validOp;
    }

    public static String validateTypeField(String aOperand) {
        String  validOp     = null;
        Pattern typePattern = Pattern.compile("^\"?([+=])?([a-zA-Z].*)([,;\\.]?)\"?");
        Matcher m           = typePattern.matcher(aOperand);

        if (m.matches()) {
            validOp = (m.group(2) == null)
                      ? ""
                      : m.group(2).trim();
        }

        return validOp;
    }

    //~--- get methods --------------------------------------------------------

    /*
     *  This method tokenizes the operand string with ;,space and checks
     * if "=" is mentioned with "+" or "-" operators
     *
     * @param aOperand Operand String
     *
     * @return true, if "=" in not mentioned along with "+" or "-" else false.
     */
    public static boolean isOperatorsValid(String aOperand) {
        ArrayList<String> opsList     = Utilities.toArrayList(aOperand, ";, ");
        boolean           equalsFound = false;
        boolean           othersFound = false;
        boolean           first       = true;

        for (String str : opsList) {

            /*
             * If equals is found inbetween, its invalid because
             * 1. =abc,=xyz is not valid. "=" should be mentioned once for
             *    assignement.
             * 2. abc,=xyz means +abc,=xyz i.e "+" is implicit if nothing
             *    mentioned.
             * 3. =xyz,abc is valid assignment.
             */
            if (str.trim().startsWith("=") == true) {
                if (first == false) {
                    return false;
                } else {
                    equalsFound = true;
                }
            } else if ((str.trim().startsWith("+") == true) || (str.trim().startsWith("-") == true)) {
                othersFound = true;
            }

            first = false;
        }

        if ((equalsFound == true) && (othersFound == true)) {
            return false;
        } else {
            return true;
        }
    }
}
