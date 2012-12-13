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
 * LinkFormatter.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.SysPrefixes;

//TBits Imports
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;

import static transbit.tbits.Helper.TBitsConstants.DYNAMIC_TOOLTIP;
import static transbit.tbits.Helper.TBitsConstants.NO_TOOLTIP;

//Static Imports
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;
import static transbit.tbits.Helper.TBitsConstants.SOURCE_WEB;
import static transbit.tbits.Helper.TBitsConstants.STATIC_TOOLTIP;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_HYDURL;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_NYCURL;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//~--- classes ----------------------------------------------------------------

/**
 * This class provides utility services to transform and render links and
 * smart links.
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class LinkFormatter {
    public static final TBitsLogger  LOG                    = TBitsLogger.getLogger(PKG_UTIL);
    public static String[]           PUNCTUATIONS           = {
        ".", ",", "?", "!", "\"", "'", ":", "[", "]", "{", "}", "(", ")", "-", "<", ">", ";"
    };
    public static String[]           UNLINK_EXTENSIONS      = { "pl", "exe", "bat" };
    public static final List<String> UNLINK_EXTENSIONS_LIST = Arrays.asList(UNLINK_EXTENSIONS);

    //~--- methods ------------------------------------------------------------

    /*
     * Append a space before smartlink for proper rendering
     * of text like  http://www/,dflow#10
     */
    public static void appendSpace(StringBuilder aBuffer) {
        if ((aBuffer == null) || (aBuffer.length() == 0)) {
            return;
        }

        String temp = aBuffer.toString().toLowerCase().replace("&nbsp;", " ").replace("<br>", "\n");

        //
        // if space or new line is present before smartlink
        // we are safe, do nothing and return.
        //
        if (temp.endsWith(" ") || temp.endsWith("\n")) {
            return;
        }

        //
        // Get the line containing the smartline.
        //
        int index = temp.lastIndexOf("\n");

        if (index != -1) {
            temp = temp.substring(index + 1);
        }

        //
        // if no hyperlink present before smartlink
        // we are safe, do nothing and return.
        //
        if (temp.indexOf("http:") == -1) {
            return;
        }

        //
        // Now we are sure there is a hyperlink before smartline
        // if index of space is after the hyperlink index ,
        // we are safe, do nothing and return.
        // else add a space before smartlink.
        //
        int index1 = temp.lastIndexOf("http:");
        int index2 = temp.lastIndexOf(" ");

        if (index2 > index1) {
            return;
        }

        aBuffer.append(" ");

        return;
    }

    /**
     */
    private static String checkFileExtension(String aHref) {
        if ((aHref == null) || (aHref.trim().equals("") == true)) {
            return aHref;
        }

        String href = aHref.replace("\\", "/").trim();

        //
        // If linked to a directory, simply return
        //
        if (href.endsWith("/") == true) {
            return aHref;
        }

        String file  = href;
        int    index = href.lastIndexOf("/");

        if (index != -1) {
            file = href.substring(index + 1);
        }

        index = file.indexOf(".");

        //
        // If linked to a file with no extension, return as directory
        //
        if (index == -1) {
            return (aHref + "/");
        }

        String extension = file.substring(index + 1);

        if (UNLINK_EXTENSIONS_LIST.contains(extension.toLowerCase()) == true) {
            return aHref.substring(0, href.lastIndexOf("/") + 1);
        } else {
            return aHref;
        }
    }

    /**
     * Method to replace the substring containing http://,ftp: etc with the
     * corresponding hyperlinks.
     *
     * @param  aString the String in which the links has to be replaced
     *                 with the hyperlinks
     *
     * @return the String with the links replaced.
     */
    public static String hyperLinks(String aString) {
        return hyperLinks(aString, null);
    }

    /**
     * Method to replace the substring containing http://,ftp: etc with the
     * corresponding hyperlinks and highlighting specified text ignore ones
     * part of the link.
     *
     * @param  aString the String in which the links has to be replaced
     *                 with the hyperlinks
     * @return the String with the links replaced.
     */
    public static String hyperLinks(String aString, ArrayList<String> aSearchTextList) {
        if ((aString == null) || aString.trim().equals("")) {
            return "";
        }

        String hydUrl = PropertiesHandler.getProperty(KEY_HYDURL);

        hydUrl = ((hydUrl == null)
                  ? ""
                  : hydUrl);

        String nycUrl = PropertiesHandler.getProperty(KEY_NYCURL);

        nycUrl = ((nycUrl == null)
                  ? ""
                  : nycUrl);

        if (hydUrl.endsWith("/") == false) {
            hydUrl = hydUrl + "/";
        }

        if (nycUrl.endsWith("/") == false) {
            nycUrl = nycUrl + "/";
        }

        String href = "";
        String str  = "";

        try {
            StringBuilder buffer = new StringBuilder();

            //
            // line breaks,<br> gets HTML encoded,
            // when patternStr is encoded back later.
            // hence coverting to line breaks again
            // and will be replaced by \n<br> before returning.
            //
            str = aString.replaceAll("\\n<br>", "\n");

            String  patternStr = Utilities.htmlDecode(aString.replace("&nbsp;&nbsp;", "  ").replaceAll("\\n<br>", "\n"));
            Pattern p          = Pattern.compile(

            //
            // Pattern for link enclosed between <>. All
            // special chars/spaces allowed in enclosed link.
            //
            "(<(http:|ftp:|tftp:|file:|https:|" + "outlook:\\\\\\\\public folders|" + "outlook:\\\\\\\\public%20folders)([^\r\n>]*)>)" + "|" +

            //
            // Pattern for link enclosed between "". All
            // special chars/spaces allowed in enclosed link.
            //
            "(\"(http:|ftp:|tftp:|file:|https:|" + "outlook:\\\\\\\\public folders|" + "outlook:\\\\\\\\public%20folders)([^\r\n\"]*)\")" + "|" +

            //
            // Pattern for link,terminated by space or new line
            // whole word prefixes are looked for.
            //
            "((^|[^a-z0-9])(http:|ftp:|tftp:|file:|https:)" + "([^\\s\r\n]+))" + "|" +

            //
            // Pattern for UNC links/shared dirs enclosed between <>.
            // All special chars/spaces allowed in enclosed link.
            // These are linked as file://
            //
            "(<(\\\\\\\\([a-z]+\\.)*transbittech.com|[pnztx]:)([/\\\\])" + "([^\r\n>]*)>)" + "|" +

            //
            // Pattern for UNC links/shared dirs enclosed between "".
            // All special chars/spaces allowed in enclosed link.
            // These are linked as file://
            //
            "(\"(\\\\\\\\([a-z]+\\.)*transbittech.com|[pnztx]:)([/\\\\])" + "([^\r\n\"]*)\")" + "|" +

            //
            // Pattern for UNC links/shared dirs,terminated by
            // space or new line or chars not allowed in filenames.
            // These are linked as file://
            // whole word prefixes are looked for.
            //
            "((\\\\\\\\([a-z]+\\.)*transbittech.com" + "|(^|[^a-z0-9])([pnztx]:))" + "([/\\\\])([^\\s\r\n,;:*?\"<>|]*))" + "|" +

            //
            // Pattern for www(xyz.)+ or www/
            // These are linked as http://
            //
            "((^|[^a-z0-9/\\\\])(www(\\.[a-z]+)+|www[/\\\\])" + "([^\\s\r\n,;:*?\"<>|]*))" + "|" +

            //
            // Pattern for ftp.*.transbittech.com
            // These are linked as ftp://
            //
            "((^|[^a-z0-9])(ftp[\\.]([a-z]+\\.)*transbit\\.com)" + "([^\\s\r\n,;:*?\"<>|]*))", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(patternStr);

            while (m.find() == true) {
                href = m.group();
                href = Utilities.htmlEncode(href).replace("  ", "&nbsp;&nbsp;");

                String replaceString = href;

                //
                // If smart links hrefs, ignore
                //
                if (m.group().toLowerCase().matches("\"http://([^ \n\r:/]*)(:([0-9]+))?/?/q/([a-zA-Z0-9]+)/" + "([0-9]+)(#([0-9]+))?\"") == true) {
                    href          = m.group();
                    replaceString = href;
                }

                //
                // If matches with mouse-over tooltip link, ignore.
                // tooltip links are of form
                // 1. getSubject('link,'prefix'...);">linkText</a>text
                // 2. text followed by link can be quotes etc which needs
                // to be htmlEncoded as they are part of description
                // and not inserted for tooltips.
                // 3. linkText is made to pass through highlighting since
                // all text now supports smartlinks search.
                //
                else if ((m.group().indexOf(hydUrl + "','") >= 0) || (m.group().indexOf(nycUrl + "','") >= 0)) {
                    href = m.group();

                    int index = href.indexOf("</a>");

                    //
                    // html encode text after </a>
                    //
                    if (index != -1) {
                        href = href.substring(0, index + 4) + Utilities.htmlEncode(href.substring(index + 4));
                    }

                    //
                    // Make linkText i.e between ">linkText</a>
                    // to pass through highlighting.
                    //
                    int index1 = href.indexOf("\">");

                    if ((index1 != -1) && (index1 < index)) {
                        replaceString = href.substring(0, index1 + 2) + ActionHelper.highLightText(href.substring(index1 + 2, index + 4), aSearchTextList) + href.substring(index + 4);
                    } else {
                        replaceString = href;
                    }
                }

                // else go ahead with normal linking
                else if (m.group(1) != null) {
                    if (linkNotEmpty(m.group(3)) == true) {
                        href = m.group(2) + m.group(3);
                        href = Utilities.htmlEncode(href).replace("  ", "&nbsp;&nbsp;");

                        if (href.trim().toLowerCase().startsWith("file:")) {
                            String text = checkFileExtension(href);

                            if (text.length() <= href.length()) {
                                href          = href.substring(0, text.length());
                                replaceString = getLinkHtml(href.replace("\\", "/"), href);
                            } else {
                                replaceString = getLinkHtml(href.replace("\\", "/") + "/", href);
                            }
                        } else {
                            replaceString = getLinkHtml(href);
                        }
                    }
                } else if (m.group(4) != null) {
                    if (linkNotEmpty(m.group(6)) == true) {
                        href = m.group(5) + m.group(6);
                        href = Utilities.htmlEncode(href).replace("  ", "&nbsp;&nbsp;");

                        if (href.trim().toLowerCase().startsWith("file:")) {
                            String text = checkFileExtension(href);

                            if (text.length() <= href.length()) {
                                href          = href.substring(0, text.length());
                                replaceString = getLinkHtml(href.replace("\\", "/"), href);
                            } else {
                                replaceString = getLinkHtml(href.replace("\\", "/") + "/", href);
                            }
                        } else {
                            replaceString = getLinkHtml(href);
                        }
                    }
                } else if (m.group(7) != null) {
                    if (linkNotEmpty(m.group(10)) == true) {
                        String hrefText = m.group(10);

                        for (int i = 0; i < PUNCTUATIONS.length; i++) {
                            if (hrefText.endsWith(PUNCTUATIONS[i])) {
                                hrefText = hrefText.substring(0, hrefText.length() - 1);
                                i        = -1;
                            }
                        }

                        if (linkNotEmpty(hrefText) == false) {
                            href          = m.group();
                            href          = Utilities.htmlEncode(href).replace("  ", "&nbsp;&nbsp;");
                            replaceString = m.group();
                        } else {
                            href = m.group(9) + Utilities.htmlEncode(hrefText).replace("  ", "&nbsp;&nbsp;");

                            if (href.trim().toLowerCase().startsWith("file:")) {
                                String text = checkFileExtension(href);

                                if (text.length() <= href.length()) {
                                    href          = href.substring(0, text.length());
                                    replaceString = getLinkHtml(href.replace("\\", "/"), href);
                                } else {
                                    replaceString = getLinkHtml(href.replace("\\", "/") + "/", href);
                                }
                            } else {
                                replaceString = getLinkHtml(href);
                            }
                        }
                    }
                } else if (m.group(11) != null) {
                    if ((m.group(12).toLowerCase().indexOf("transbit") == -1) || (linkNotEmpty(m.group(15)) == true)) {
                        href = m.group(12) + m.group(14) + m.group(15);
                        href = Utilities.htmlEncode(href).replace("  ", "&nbsp;&nbsp;");

                        String text = checkFileExtension(href);

                        if (text.length() <= href.length()) {
                            href          = href.substring(0, text.length());
                            replaceString = getLinkHtml("file://" + href.replace("\\", "/"), href);
                        } else {
                            replaceString = getLinkHtml("file://" + href.replace("\\", "/") + "/", href);
                        }
                    }
                } else if (m.group(16) != null) {
                    if ((m.group(17).toLowerCase().indexOf("transbit") == -1) || (linkNotEmpty(m.group(20)) == true)) {
                        href = m.group(17) + m.group(19) + m.group(20);
                        href = Utilities.htmlEncode(href).replace("  ", "&nbsp;&nbsp;");

                        String text = checkFileExtension(href);

                        if (text.length() <= href.length()) {
                            href          = href.substring(0, text.length());
                            replaceString = getLinkHtml("file://" + href.replace("\\", "/"), href);
                        } else {
                            replaceString = getLinkHtml("file://" + href.replace("\\", "/") + "/", href);
                        }
                    }
                } else if (m.group(21) != null) {
                    if ((m.group(22).toLowerCase().indexOf("transbit") == -1) || (linkNotEmpty(m.group(27)) == true)) {
                        String hrefText = m.group(27);

                        for (int i = 0; i < PUNCTUATIONS.length; i++) {
                            if (hrefText.endsWith(PUNCTUATIONS[i])) {
                                hrefText = hrefText.substring(0, hrefText.length() - 1);
                                i        = -1;
                            }
                        }

                        if ((m.group(22).toLowerCase().indexOf("transbit") == -1) || (linkNotEmpty(hrefText) == true)) {
                            href = ((m.group(22).toLowerCase().indexOf("transbit") == -1)
                                    ? m.group(25)
                                    : m.group(22)) + m.group(26) + hrefText;
                            href = Utilities.htmlEncode(href).replace("  ", "&nbsp;&nbsp;");

                            String text = checkFileExtension(href);

                            if (text.length() <= href.length()) {
                                href          = href.substring(0, text.length());
                                replaceString = getLinkHtml("file://" + href.replace("\\", "/"), href);
                            } else {
                                replaceString = getLinkHtml("file://" + href.replace("\\", "/") + "/", href);
                            }
                        }
                    }
                } else if (m.group(28) != null) {
                    href = m.group(30) + m.group(32);

                    for (int i = 0; i < PUNCTUATIONS.length; i++) {
                        if (href.endsWith(PUNCTUATIONS[i])) {
                            href = href.substring(0, href.length() - 1);
                            i    = -1;
                        }
                    }

                    href = Utilities.htmlEncode(href).replace("  ", "&nbsp;&nbsp;");

                    String text = checkFileExtension(href);

                    if (text.length() <= href.length()) {
                        href          = href.substring(0, text.length());
                        replaceString = getLinkHtml("http://" + href.replace("\\", "/"), href);
                    } else {
                        replaceString = getLinkHtml("http://" + href.replace("\\", "/") + "/", href);
                    }
                } else if (m.group(33) != null) {
                    href = m.group(35) + m.group(37);

                    for (int i = 0; i < PUNCTUATIONS.length; i++) {
                        if (href.endsWith(PUNCTUATIONS[i])) {
                            href = href.substring(0, href.length() - 1);
                            i    = -1;
                        }
                    }

                    href          = Utilities.htmlEncode(href).replace("  ", "&nbsp;&nbsp;");
                    replaceString = getLinkHtml("ftp://" + href.replace("\\", "/"), href);
                }

                String temp = ActionHelper.highLightText(str.substring(0, str.indexOf(href)), aSearchTextList);

                buffer.append(temp).append(replaceString);
                str = str.substring(str.indexOf(href) + href.length());
            }

            str = ActionHelper.highLightText(str, aSearchTextList);
            buffer.append(str);

            return buffer.toString().replaceAll("\\n", "\n<br>");
        } catch (RuntimeException e) {
            LOG.severe(str + "\n\n" + href + "\n\n" + "",(e));

            return aString;
        }
    }

    /**
     * Method to replace the substring containing BusinessArea#Number
     * with the corresponding hyperlinks and highlighting them.
     *
     * @param  aString the String in which the # sign has to be replaced
     *                 with the hyperlinks
     * @param  aSameRequestLink this requests smart link text
     * @param  aMaxActionId Max action Id of the request
     * @param  aServer to form full url
     *
     * @return the String with the # replaced with the hyper links
     */
    public static String hyperSmartLinks(String aString, String aSameRequestLink, int aMaxActionId, int aMaxEmailActionId, String aServer, int aAppendInterface, int aToolTipType) {
        if ((aString == null) || aString.trim().equals("")) {
            return "";
        }

        aServer = ((aServer == null)
                   ? ""
                   : aServer);

        String hydUrl = PropertiesHandler.getProperty(KEY_HYDURL);

        hydUrl = ((hydUrl == null)
                  ? ""
                  : hydUrl);

        String nycUrl = PropertiesHandler.getProperty(KEY_NYCURL);

        nycUrl = ((nycUrl == null)
                  ? ""
                  : nycUrl);

        // Check if aServer ends with a slash.
        if (aServer.endsWith("/") == false) {
            aServer = aServer + "/";
        }

        if (hydUrl.endsWith("/") == false) {
            hydUrl = hydUrl + "/";
        }

        if (nycUrl.endsWith("/") == false) {
            nycUrl = nycUrl + "/";
        }

        try {
            BusinessArea  ba     = null;
            String        str    = aString;
            StringBuilder buffer = new StringBuilder();
            Pattern       p      = Pattern.compile("([a-zA-Z0-9_]+)([ ]?)#([ ]?)([0-9]+)(#([0-9]+))?", Pattern.CASE_INSENSITIVE);
            Matcher       m      = p.matcher(aString);

            while (m.find() == true) {
                String hrefLink      = "";
                String hrefText      = "";
                String replaceString = "";
                String smartLink     = m.group();
                String sysPrefix     = m.group(1);
                String strRequestId  = m.group(4);
                String strActionId   = ((m.group(6) != null)
                                        ? m.group(6)
                                        : "");
                int    requestId     = 0;
                int    actionId      = 0;

                // Checking if the value after the # is a number of not.
                // If it is not a number, not showing the hyperlink.
                try {
                    requestId = Integer.parseInt(strRequestId);
                } catch (NumberFormatException e) {
                    buffer.append(str.substring(0, str.indexOf(smartLink) + smartLink.length()));
                    str = str.substring(str.indexOf(smartLink) + smartLink.length());

                    continue;
                }

                //
                // Parse action Id if present
                //
                try {
                    actionId = Integer.parseInt(strActionId);
                } catch (NumberFormatException e) {
                    actionId = 0;
                }

                //
                // If referring to some action within same request using
                // pattern Action#NN or this#NN or Update#NN or append#NN,
                // link to the named action tag.
                //
                if (sysPrefix.equalsIgnoreCase("Action") || sysPrefix.equalsIgnoreCase("this") || sysPrefix.equalsIgnoreCase("Update") || sysPrefix.equalsIgnoreCase("Append")) {

                    //
                    // If referred action doesn't exist, don't hyperlink and
                    // look for next pattern.
                    //
                    if (requestId > aMaxActionId) {
                        buffer.append(str.substring(0, str.indexOf(smartLink) + smartLink.length()));
                        str = str.substring(str.indexOf(smartLink) + smartLink.length());

                        continue;
                    }

                    //
                    // If action referred will not be part of email
                    // link to the request
                    //
                    if ((requestId > 1) && (requestId <= (aMaxActionId - aMaxEmailActionId + 1))) {
                        hrefLink      = aServer + "Q/" + aSameRequestLink.replace("#", "/") + "#" + requestId;
                        replaceString = "<a class=\"l cb\" href = \"" + hrefLink + "\" target=\"" + aSameRequestLink.replace("#", "_") + "\"" + "' >" + "Update" + "#" + requestId + "</a>"
                                        + (((strActionId != null) &&!strActionId.equals(""))
                                           ? ("#" + strActionId)
                                           : "");
                    }

                    //
                    // Else link to tag within the body itself.
                    //
                    else {
                        hrefLink      = "#" + ((requestId <= aMaxActionId)
                                               ? requestId
                                               : aMaxActionId);
                        replaceString = "<a class=\"l cb\" href = \"" + hrefLink + "\"" + " >" + "Update" + "#" + requestId + "</a>" + (((strActionId != null) &&!strActionId.equals(""))
                                ? ("#" + strActionId)
                                : "");
                    }

                    buffer.append(str.substring(0, str.indexOf(smartLink)));

                    //
                    // Append a space before smartlink for proper rendering
                    // of text like  http://www/,dflow#10
                    //
                    appendSpace(buffer);
                    buffer.append(replaceString);
                    str = str.substring(str.indexOf(smartLink) + smartLink.length());

                    continue;
                }

                //
                // If Refferring to same request without action# then
                // 1) link to max action Id tag from web.
                // And link to request from email.
                // 2) If action# also present, retreat as for pattern
                // "Action#NN" above
                //
                else if ((sysPrefix + "#" + strRequestId).equalsIgnoreCase(aSameRequestLink)) {
                    if (actionId == 0) {
                        hrefText = aSameRequestLink + (((strActionId != null) &&!strActionId.equals(""))
                                                       ? ("#" + strActionId)
                                                       : "");
                    } else {
                        hrefText = "Update#" + actionId;
                    }

                    if (actionId == 0) {
                        if (aAppendInterface != SOURCE_WEB) {
                            hrefLink      = aServer + "Q/" + aSameRequestLink.replace("#", "/");
                            replaceString = "<a class=\"l cb\" href = \"" + hrefLink + "\" target=\"" + aSameRequestLink.replace("#", "_") + "\"" + ">" + hrefText + "</a>";
                        } else {
                            hrefLink      = "#Top";
                            replaceString = "<a class=\"l cb\" href = \"" + hrefLink + "\"" + ">" + hrefText + "</a>";
                        }
                    } else if ((actionId != 1) && (actionId <= (aMaxActionId - aMaxEmailActionId + 1))) {
                        hrefLink      = aServer + "Q/" + aSameRequestLink.replace("#", "/") + (((strActionId != null) &&!strActionId.equals(""))
                                ? ("#" + strActionId)
                                : "");
                        replaceString = "<a class=\"l cb\" href = \"" + hrefLink + "\" target=\"" + aSameRequestLink.replace("#", "_") + "\" " + ">" + hrefText + "</a>";
                    } else {
                        hrefLink      = "#" + ((actionId <= aMaxActionId)
                                               ? actionId
                                               : aMaxActionId);
                        replaceString = "<a class=\"l cb\" href = \"" + hrefLink + "\"" + ">" + hrefText + "</a>";
                    }

                    buffer.append(str.substring(0, str.indexOf(smartLink)));

                    //
                    // Append a space before smartlink for proper rendering
                    // of text like  http://www/,dflow#10
                    //
                    appendSpace(buffer);
                    buffer.append(replaceString);
                    str = str.substring(str.indexOf(smartLink) + smartLink.length());

                    continue;
                } else {
                    try {

                        // Lookup Business Area by prefix
                        ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
                    } catch (DatabaseException e) {

                        // Lets look in Prefix file
                    }

                    // else look up in the prefixes file
                    if (ba == null) {
                        if (SysPrefixes.getPrefix(sysPrefix) != null) {
                            ba = new BusinessArea();
                            ba.setSystemPrefix(SysPrefixes.getPrefix(sysPrefix));
                            ba.setLocation(SysPrefixes.getLocation(sysPrefix));
                        }
                    }

                    if (ba == null) {
                        if (sysPrefix.equalsIgnoreCase("req") || sysPrefix.equalsIgnoreCase("request")) {
                            String prefix = aSameRequestLink.substring(0, aSameRequestLink.indexOf("#"));

                            try {

                                // Lookup Business Area by prefix
                                ba = BusinessArea.lookupBySystemPrefix(prefix);
                            } catch (DatabaseException e) {
                                LOG.severe("",(e));
                            }
                        }
                    }

                    // If BA not in prefixes file also, look for next #
                    if (ba == null) {
                        buffer.append(str.substring(0, str.indexOf(smartLink) + smartLink.length()));
                        str = str.substring(str.indexOf(smartLink) + smartLink.length());

                        continue;
                    }

                    hrefLink = aServer + "Q/" + ba.getSystemPrefix() + "/" + requestId + (((strActionId != null) &&!strActionId.equals(""))
                            ? ("#" + strActionId)
                            : "");
                    hrefText = ba.getSystemPrefix() + "#" + requestId + (((strActionId != null) &&!strActionId.equals(""))
                            ? ("#" + strActionId)
                            : "");

                    if (aToolTipType == DYNAMIC_TOOLTIP) {
                        String baServer = aServer;
                        String location = ((ba.getLocation() == null)
                                           ? ""
                                           : ba.getLocation().trim().toLowerCase());

                        if (location.equals("hyd") == true) {
                            baServer = hydUrl;
                        } else if (location.equals("nyc") == true) {
                            baServer = nycUrl;
                        }

                        replaceString = "<a class=\"l cb\" href = \"" + hrefLink + "\" target=\"" + ba.getSystemPrefix() + "_" + requestId + "\" onmouseover=\"this.T_HIDE_ONMOUSEOUT='true';"
                                        + "var event = (document.all)? " + "window.event : event;" + "return escape(getSubject('" + baServer + "','" + ba.getSystemPrefix() + "','" + requestId
                                        + "'));\"" + ">" + hrefText + "</a>";
                    } else if (aToolTipType == STATIC_TOOLTIP) {
                        String  requestSubject = null;
                        boolean requestPrivacy = false;

                        try {
                            requestSubject = Request.lookupSubject(ba.getSystemPrefix(), requestId, 0, true);
                        } catch (Exception sqle) {
                            LOG.severe("",(sqle));
                        }

                        if (requestSubject != null) {
                            requestSubject = " title='" + requestSubject + "' ";
                        } else {
                            requestSubject = "";
                        }

                        replaceString = "<a class=\"l cb\" href = \"" + hrefLink + "\" target=\"" + ba.getSystemPrefix() + "_" + requestId + "\"" + requestSubject + ">" + hrefText + "</a>";
                    } else if (aToolTipType == NO_TOOLTIP) {
                        replaceString = "<a class=\"l cb\" href = \"" + hrefLink + "\" target=\"" + ba.getSystemPrefix() + "_" + requestId + "\">" + hrefText + "</a>";
                    }

                    buffer.append(str.substring(0, str.indexOf(smartLink)));

                    //
                    // Append a space before smartlink for proper rendering
                    // of text like  http://www/,dflow#10
                    //
                    appendSpace(buffer);
                    buffer.append(replaceString);
                    str = str.substring(str.indexOf(smartLink) + smartLink.length());
                }
            }

            buffer.append(str);

            return buffer.toString();
        } catch (RuntimeException e) {
            LOG.severe("",(e));

            return aString;
        }
    }

    /**
     */
    private static boolean linkNotEmpty(String aLink) {
        if (aLink == null) {
            return false;
        }

        if (aLink.trim().equals("") == true) {
            return false;
        }

        if ((aLink.trim().equals("/") == true) || (aLink.trim().equals("//") == true) || (aLink.trim().equals("\\") == true) || (aLink.trim().equals("\\\\") == true)) {
            return false;
        }

        return true;
    }

    /**
     */
    public static void main(String[] args) throws Exception {

        // Setting email invocation
        System.setProperty(TBitsConstants.PROP_BA_NAME, "tbits");

        String str = "(http://sx86pr1od1.hyd.transbittech.com:4141/q/tbits/48)";

        str = "(TBITS#48)";
        str = replaceHrefWithSmartLinks(str);
        LOG.info(str);
        str = LinkFormatter.hyperSmartLinks(str, "bmw#10", 10, 10, "", SOURCE_WEB, DYNAMIC_TOOLTIP);
        LOG.info("Output: " + str);
        str = LinkFormatter.hyperLinks(str);
        LOG.info(str);
    }

    /**
     * Method to parse description and replaces any Href reference to a
     * request detail new links  by smart link
     *
     * @param  aString the String in which the links has to be replaced
     *                 with the hyperlinks
     * @return the String with the links replaced.
     */
    public static String replaceHrefWithSmartLinks(String aString) {
        if ((aString == null) || aString.trim().equals("")) {
            return "";
        }

        try {
            String        str    = aString;
            StringBuilder buffer = new StringBuilder();
            BusinessArea  ba     = null;

            //
            // Pattern for Request references followed/delimited by either
            // punctautions,spaces,newline or end of text.
            // -- (?) is allowed as a delim only if not followed by a alphanum
            // to avoid translation of links with query patterns.
            // -- in normal http links parsing, punctuations if followed by
            // alphanums can be part of links, but since request link
            // patterns are predictable, so for user ease punctations
            // are directly considered as delims else cases like
            // "http://tbits/q/dflw/10.The text blah blah" will not
            // be reverse linked.
            //
            Pattern p = Pattern.compile("http://([^ \n\r:/]*)(:([0-9]+))?/?/q/([a-zA-Z0-9]+)/" + "([0-9]+)(#([0-9]+))?" + "(([\\(\\)\\{\\}\\[\\]!<>,; .\"])|" + "(\\?([^a-z0-9]|[ ]|[\r?\n]|$))|"
                                        + "\r?\n|$)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(str);

            while (m.find() == true) {
                String link         = m.group();
                String sysPrefix    = m.group(4);
                String strRequestId = m.group(5);
                String strActionId  = ((m.group(7) != null)
                                       ? m.group(7)
                                       : "");
                String separator    = ((m.group(8) != null)
                                       ? m.group(8)
                                       : "");
                long   requestId    = 0;

                //
                // Checking if the value after the # is a number of not.
                // If it is not a number, not showing the hyperlink.
                //
                try {
                    requestId = Long.parseLong(strRequestId);
                } catch (NumberFormatException e) {
                    buffer.append(str.substring(0, str.indexOf(link) + link.length()));
                    str = str.substring(str.indexOf(link) + link.length());

                    continue;
                }

                try {

                    // Lookup Business Area by prefix
                    ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
                } catch (DatabaseException e) {

                    // Lets look in Prefix file
                }

                // else look up in the prefixes file
                if (ba == null) {
                    if (SysPrefixes.getPrefix(sysPrefix) != null) {
                        ba = new BusinessArea();
                        ba.setSystemPrefix(SysPrefixes.getPrefix(sysPrefix));
                    }
                }

                // If BA not in prefixes file also, look for next #
                if (ba == null) {
                    buffer.append(str.substring(0, str.indexOf(link) + link.length()));
                    str = str.substring(str.indexOf(link) + link.length());

                    continue;
                }

                String replaceString = ba.getSystemPrefix() + "#" + requestId + (((strActionId != null) &&!strActionId.equals(""))
                        ? ("#" + strActionId)
                        : "") + separator;

                buffer.append(str.substring(0, str.indexOf(link))).append(replaceString);
                str = str.substring(str.indexOf(link) + link.length());
            }

            buffer.append(str);

            return buffer.toString();
        } catch (RuntimeException e) {
            LOG.severe("",(e));

            return aString;
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     */
    private static String getLinkHtml(String aHref) {
        return getLinkHtml(aHref, aHref);
    }

    /**
     */
    private static String getLinkHtml(String aHref, String aText) {
        if ((aText == null) || (aText.trim().equals("") == true)) {
            return aText;
        }

        return ("<a class=\"l cb\" " + "href = \"" + aHref + "\"" + " target=\"_blank\">" + aText + "</a>");
    }
}
