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
 * CustomFormHandler.java
 *
 *
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.MailResourceManager;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.FBAction;
import transbit.tbits.config.FBField;
import transbit.tbits.config.FBField.FBFieldType;
import transbit.tbits.config.FBForm;
import transbit.tbits.config.FBType;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.BAForm;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.BusinessArea.BAColumn;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.User.UserColumn;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * @author Vaibhav
 *
 */
public class CustomFormHandler extends HttpServlet {
    private static final TBitsLogger LOG         = TBitsLogger.getLogger(PKG_WEBAPPS);
    private static final String      LIST_FILE   = "web/tbits-cf-list.htm";
    private static final String      HTML_FILE   = "web/tbits-custom-form.htm";
    private static final String      DATE_FILE   = "web/tbits-cf-date.htm";
    private static final String      CB_FILE     = "web/tbits-cf-checkbox.htm";
    private static final String      BULLET_FILE = "web/tbits-cf-bullets.htm";
    private static final String      ATT_FILE    = "web/tbits-cf-att.htm";
    private static final String      PARA_FILE   = "web/tbits-cf-para.htm";
    private static final String      RADIO_FILE  = "web/tbits-cf-radio.htm";
    private static final String      TA_FILE     = "web/tbits-cf-textarea.htm";
    private static final String      TB_FILE     = "web/tbits-cf-textbox.htm";
    private static final String      USER_FILE   = "web/tbits-cf-user.htm";

    // Constant that holds the form encoding type.
    private static final String      MULTIPART_CONTENT_TYPE = "multipart/form-data";
    private static ArrayList<String> ourTagList             = new ArrayList<String>();

    //~--- static initializers ------------------------------------------------

    static {
        ourTagList.add("formTitle");
        ourTagList.add("nearestPath");
        ourTagList.add("cssFile");
        ourTagList.add("sysPrefix");
        ourTagList.add("sysName");
        ourTagList.add("shortName");
        ourTagList.add("successMessage");
        ourTagList.add("formContent");
        ourTagList.add("hasAttachment");
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method to handle the HTTP - Get requests.
     */
    public void doGet(HttpServletRequest aReq, HttpServletResponse aRes) throws ServletException, IOException {
        HttpSession session = aReq.getSession();

        try {
            Hashtable<String, Object> data = new Hashtable<String, Object>();

            readFormDetailsFromPathInfo(aReq, data);

            BusinessArea ba     = (BusinessArea) data.get("BUSINESS_AREA");
            BAForm       baForm = (BAForm) data.get("BA_FORM");
            PrintWriter  out    = aRes.getWriter();

            aRes.setContentType("text/html");
            doGetHandler(aReq, ba, baForm, out);
        } catch (Exception e) {
            LOG.info("",(e));
            session.setAttribute("ExceptionObject", e);
            aRes.sendRedirect(WebUtil.getServletPath(aReq, "/error"));
        }
    }

    /**
     *
     * @param ba
     * @param baForm
     * @param out
     * @throws FileNotFoundException
     * @throws IOException
     * @throws DatabaseException
     */
    private void doGetHandler(HttpServletRequest req, BusinessArea ba, BAForm baForm, PrintWriter out) throws FileNotFoundException, IOException, DatabaseException, TBitsException {
        User                      user      = WebUtil.validateUser(req);
        SysConfig                 sysConfig = ba.getSysConfigObject();
        String                    sysPrefix = ba.getSystemPrefix();
        FBForm                    form      = baForm.getFormConfigObject();
        ArrayList<FBField>        fieldList = form.getFieldList();
        Hashtable<String, String> tagTable  = new Hashtable<String, String>();

        tagTable.put("formTitle", baForm.getTitle());
        tagTable.put("nearestPath", WebUtil.getNearestPath(req, ""));
        tagTable.put("cssFile", WebUtil.getCSSFile(sysConfig.getWebStylesheet(), sysPrefix, false));
        tagTable.put("sysPrefix", ba.getSystemPrefix());
        tagTable.put("sysName", ba.getDisplayName());
        tagTable.put("shortName", baForm.getShortName());
        tagTable.put("hasAttachment", "false");

        HttpSession session = req.getSession();
        Object      success = session.getAttribute(form.getShortName() + "_FORM_SUBMITTED");

        if (success != null) {
            session.setAttribute(form.getShortName() + "_FORM_SUBMITTED", null);
            tagTable.put("successMessage", "Form submitted successfully.");
        }

        StringBuilder formContent = new StringBuilder();

        for (FBField field : fieldList) {
            FBFieldType fieldType = field.getType();

            switch (fieldType) {
            case ATTACHMENT :
                tagTable.put("hasAttachment", "true");
                renderAttachmentField(field, formContent);

                break;

            case BULLET_LIST :
                renderBulletedList(field, formContent);

                break;

            case CHECKBOX :
                renderCheckboxField(field, formContent);

                break;

            case DATE :
                renderDateField(req, field, formContent);

                break;

            case DYNAMIC_LIST :
                renderDynamicListField(ba, field, formContent);

                break;

            case PARAGRAPH :
                renderParagraphField(field, formContent);

                break;

            case RADIO :
                renderRadioField(field, formContent);

                break;

            case STATIC_LIST :
                renderStaticListField(field, formContent);

                break;

            case TEXT_AREA :
                renderTextAreaField(field, formContent);

                break;

            case TEXT_BOX :
                renderTextBoxField(field, ba, user, formContent);

                break;

            case USER :
                renderUserField(field, formContent);

                break;
            }
        }

        tagTable.put("formContent", formContent.toString());

        DTagReplacer hp = new DTagReplacer(HTML_FILE);

        SearchRenderer.replaceTags(hp, tagTable, ourTagList);
        out.println(hp.parse(ba.getSystemId()));

        return;
    }

    /**
     * Method to handle the HTTP - Post requests.
     */
    public void doPost(HttpServletRequest aReq, HttpServletResponse aRes) throws ServletException, IOException {
        HttpSession session = aReq.getSession();

        try {
            Hashtable<String, Object> data = new Hashtable<String, Object>();

            readFormDetailsFromPathInfo(aReq, data);

            BusinessArea ba     = (BusinessArea) data.get("BUSINESS_AREA");
            BAForm       baForm = (BAForm) data.get("BA_FORM");
            PrintWriter  out    = aRes.getWriter();

            aRes.setContentType("text/html");
            doPostHandler(aReq, aRes, ba, baForm);
        } catch (Exception e) {
            LOG.info("",(e));
            session.setAttribute("ExceptionObject", e);
            aRes.sendRedirect(WebUtil.getServletPath(aReq, "/error"));
        }
    }

    /**
     * This method reads the request parameters and sends out a mail with
     * the form data as the content.
     *
     *
     * @param aReq
     * @param aRes
     * @param aBA
     * @param aBAForm
     * @throws ServletException
     * @throws IOException
     * @throws DETBitsException@throws DatabaseException
     */
    private void doPostHandler(HttpServletRequest aReq, HttpServletResponse aRes, BusinessArea aBA, BAForm aBAForm) throws ServletException, IOException, TBitsException, DatabaseException {
        User                      user       = WebUtil.validateUser(aReq);
        String                    userLogin  = user.getUserLogin();
        int                       systemId   = aBA.getSystemId();
        String                    sysPrefix  = aBA.getSystemPrefix();
        Hashtable<String, String> paramTable = new Hashtable<String, String>();

        AddHtmlRequest.getParamTable(aReq, userLogin, paramTable);

        FBForm             form      = aBAForm.getFormConfigObject();
        ArrayList<FBField> fieldList = form.getFieldList();

        /*
         * Maintain two buffers. One holds the values of form fields that should
         * be mapped to the business area fields through IUCs and the other
         * holds the all fields .
         */
        StringBuilder mapped             = new StringBuilder();
        StringBuilder unmapped           = new StringBuilder();
        boolean       hasAttachments     = false;
        int           maxFieldNameLength = form.getMaxFieldNameLength() + 3;

        for (FBField field : fieldList) {
            FBFieldType fieldType = field.getType();

            // These are not data fields.
            if ((fieldType == FBFieldType.BULLET_LIST) || (fieldType == FBFieldType.PARAGRAPH)) {
                continue;
            }

            if (fieldType == FBFieldType.ATTACHMENT) {

                /*
                 * There are attachment fields in this form. Set the flag to
                 * true. Then, they will be handled later in the process.
                 */
                hasAttachments = true;

                continue;
            }

            String frmFieldName  = field.getName();
            String frmFieldValue = paramTable.get(frmFieldName);

            frmFieldValue = (frmFieldValue == null)
                            ? ""
                            : frmFieldValue.trim();

            /*
             * Ignore this field if no value is supplied.
             */
            if (frmFieldValue.equals("") == true) {
                continue;
            }

            /*
             * Get the label of this field and append as many space as required
             * to align the values in one column.
             */
            String        frmFieldLabel    = field.getLabel();
            int           length           = frmFieldLabel.length();
            StringBuilder fieldLabelBuffer = new StringBuilder();

            fieldLabelBuffer.append(field.getLabel()).append(":");

            for (int i = length; i < maxFieldNameLength; i++) {
                fieldLabelBuffer.append(" ");
            }

            frmFieldLabel = fieldLabelBuffer.toString();

            /*
             * In case of textarea fields, let us not add the field name.
             */
            if (field.getType() != FBFieldType.TEXT_AREA) {
                unmapped.append(frmFieldLabel);
            } else {
                unmapped.append("\n");
            }

            unmapped.append(frmFieldValue).append("\n");

            String baFieldName = field.getBAField();

            if ((baFieldName != null) && (baFieldName.equals("") == false)) {

                /*
                 * This field is mapped to a field in this business area. So,
                 * get the primary descriptor of this field and use it to
                 * form the IUC.
                 */
                mapped.append("/").append(getPrimaryDesc(systemId, baFieldName, baFieldName)).append(": ").append(frmFieldValue).append("\n");
            }
        }    // End For

        /*
         * check if there are any attachments in this form. If so, then get the
         * names of the attached files, if any, from the paramTable using the
         * key Field.Attachments.
         */
        ArrayList<String> attList     = new ArrayList<String>();
        ArrayList<String> attNameList = new ArrayList<String>();

        if (hasAttachments == true) {
            String strAttNameList = paramTable.get(Field.ATTACHMENTS);

            strAttNameList = (strAttNameList == null)
                             ? ""
                             : strAttNameList.trim();

            if (strAttNameList.equals("") == false) {

                /*
                 * the value in strAttNameList is a newline separated list of
                 * attachment records. Each attachment record is a tab separated
                 * list of stored name and the display name of the correspoding
                 * attachment.
                 */
                ArrayList<String> tmpList = Utilities.toArrayList(strAttNameList, "\n");

                for (String attName : tmpList) {
                    String[] parts = attName.split("\t");

                    if (parts.length != 2) {
                        LOG.severe("Incorrect format of attachment record: " + attName);

                        continue;
                    }

                    String filePath = APIUtil.getTMPDir() + "/" + userLogin + "-" + parts[0];    // Stored name of the file.

                    attList.add(filePath);
                    attNameList.add(parts[1]);                                                             // Add the actual file name.
                }
            }
        }                                                                                                  // End IF.

        FBAction action      = form.getAction();
        String   fromAddress = action.getFrom();

        /*
         * Check if from is $user. In such case take the user who invoked this
         * form as the from address.
         */
        if (fromAddress.equalsIgnoreCase("$user") == true) {
            fromAddress = user.getEmail();
        }

        String toAddrList  = Utilities.arrayListToString(action.getToList());
        String ccAddrList  = Utilities.arrayListToString(action.getCcList());
        String subAddrList = Utilities.arrayListToString(action.getSubscriberList());

        /*
         * Add Cc and Subscriber lists as IUCs.
         */
        if ((ccAddrList != null) && (ccAddrList.trim().equals("") == false)) {
            mapped.append("/").append(getPrimaryDesc(systemId, Field.CC, "cc")).append(":").append(ccAddrList);
        }

        if ((subAddrList != null) && (subAddrList.trim().equals("") == false)) {
            mapped.append("/").append(getPrimaryDesc(systemId, Field.SUBSCRIBER, "sub")).append(":").append(subAddrList);
        }

        /*
         * If this is a request to preview the data, then print the unmapped
         * content in text format and return.
         */
        String previewStr = aReq.getParameter("previewSubmit");

        if ((previewStr != null) && previewStr.equals("true")) {
            PrintWriter out = aRes.getWriter();

            aRes.setContentType("text/plain");
            out.println(unmapped.toString());

            return;
        }

        /*
         * Prepare the mail to be sent out. Message content contains the IUCs
         * at the top followed by the actual content.
         */
        StringBuilder messageContent = new StringBuilder();

        messageContent.append(mapped.toString()).append("\n\n").append(unmapped.toString()).append("\n");

        try {
            MimeMultipart mp   = new MimeMultipart();
            MimeBodyPart  part = new MimeBodyPart();

            part.setHeader("MIME-Version", "1.0");
            part.setHeader("Content-Type", "text/plain");
            part.setText(messageContent.toString());

            int partCount = 0;

            mp.addBodyPart(part, partCount++);

            for (String attachment : attList) {
                File file = new File(attachment);

                LOG.info(file.toString());

                FileDataSource fds     = new FileDataSource(file);
                MimeBodyPart   attPart = new MimeBodyPart();

                attPart.setDataHandler(new DataHandler(fds));
                attPart.setDisposition(Part.ATTACHMENT);
                attPart.setFileName(attNameList.get(partCount - 1));
                mp.addBodyPart(attPart, partCount++);
            }
            MailResourceManager mailResMgr = new MailResourceManager();
            Mail.sendHtmlAndAttachments(toAddrList, fromAddress, action.getSubject(), mp);
            mailResMgr.commit();
            HttpSession session = aReq.getSession();

            session.setAttribute(form.getShortName() + "_FORM_SUBMITTED", "true");
            aRes.sendRedirect(WebUtil.getServletPath(aReq, "/cfh/") + sysPrefix + "/" + form.getShortName());

            return;
        } catch (Exception e) {
            StringBuilder message = new StringBuilder();

            message.append("An exception has occurred while mailing the form: \n").append("Form Title: ").append(form.getTitle()).append("Recipients: ").append(toAddrList).append(
                "MessageContent: \n").append(messageContent).append("\n").append(TBitsLogger.getStackTrace(e)).append("");
            LOG.severe("An exception has occurred while mailing the form: ");
        } finally {

            /*
             * Delete attachments if any.
             */
            for (String attachment : attList) {
                File file = new File(attachment);

                file.delete();
            }
        }

        return;
    }

    /**
     *
     *
     * @param aReq
     * @param aData
     * @throws TBitsException
     * @throws DatabaseException
     */
    private void readFormDetailsFromPathInfo(HttpServletRequest aReq, Hashtable<String, Object> aData) throws TBitsException, DatabaseException {

        /**
         * Get the business area prefix and the form name from the path info.
         */
        String pathInfo = aReq.getPathInfo();

        pathInfo = (pathInfo == null)
                   ? ""
                   : pathInfo.trim();

        /*
         * Remove the leading and trailing slashes.
         */
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }

        if (pathInfo.endsWith("/")) {
            pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
        }

        /*
         * Split the pathInfo by forward-slash. The first part will be the
         * system prefix and the next one is the name of the form to be
         * rendered.
         */
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            throw new TBitsException(Messages.getMessage("NO_SUCH_FORM"));
        }

        String       sysPrefix = parts[0];
        String       formName  = parts[1];
        BusinessArea ba        = BusinessArea.lookupBySystemPrefix(sysPrefix);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int    systemId = ba.getSystemId();
        BAForm baForm   = BAForm.lookupBySystemIdAndName(systemId, formName);

        if (baForm == null) {
            throw new TBitsException(Messages.getMessage("NO_SUCH_FORM"));
        }

        aData.put("BUSINESS_AREA", ba);
        aData.put("BA_FORM", baForm);
    }

    /**
     *
     * @param field
     * @param buffer
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void renderAttachmentField(FBField field, StringBuilder buffer) throws FileNotFoundException, IOException {
        String       label = field.getLabel();
        DTagReplacer hp    = new DTagReplacer(ATT_FILE);

        hp.replace("label", Utilities.htmlEncode(label));
        buffer.append(hp.parse(0));

        return;
    }

    /**
     *
     * @param field
     * @param buffer
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void renderBulletedList(FBField field, StringBuilder buffer) throws FileNotFoundException, IOException {
        StringBuilder temp = new StringBuilder();

        temp.append("<UL>");

        ArrayList<String> pointList = field.getPointsList();

        for (String str : pointList) {
            temp.append("<LI>").append(str).append("</LI>");
        }

        temp.append("</UL>");

        DTagReplacer hp = new DTagReplacer(BULLET_FILE);

        hp.replace("bullets", temp.toString());
        buffer.append(hp.parse(0));

        return;
    }

    /**
     *
     * @param field
     * @param buffer
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void renderCheckboxField(FBField field, StringBuilder buffer) throws FileNotFoundException, IOException {
        DTagReplacer hp = new DTagReplacer(CB_FILE);

        hp.replace("label", Utilities.htmlEncode(field.getLabel()));
        hp.replace("name", Utilities.htmlEncode(field.getName()));
        buffer.append(hp.parse(0));

        return;
    }

    /**
     *
     * @param aRequest TODO
     * @param field
     * @param buffer
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void renderDateField(HttpServletRequest aRequest, FBField field, StringBuilder buffer) throws FileNotFoundException, IOException {
        DTagReplacer hp = new DTagReplacer(DATE_FILE);

        hp.replace("name", Utilities.htmlEncode(field.getName()));
        hp.replace("label", Utilities.htmlEncode(field.getLabel()));

        String toolTip   = field.getToolTip();
        String helpClass = "";

        if ((toolTip != null) && (toolTip.trim().equals("") == false)) {
            toolTip   = toolTip.trim();
            helpClass = "help";
        } else {
            toolTip = "";
        }

        hp.replace("help", helpClass);
        hp.replace("toolTip", Utilities.htmlEncode(toolTip));

        String width = field.getWidth();

        if ((width == null) || (width.trim().equals("") == true)) {
            width = "100px;";
        }

        hp.replace("width", width);
        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        buffer.append(hp.parse(0));

        return;
    }

    /**
     *
     * @param ba
     * @param field
     * @param buffer
     * @throws FileNotFoundException
     * @throws IOException
     * @throws DatabaseException
     */
    private void renderDynamicListField(BusinessArea ba, FBField field, StringBuilder buffer) throws FileNotFoundException, IOException, DatabaseException {
        DTagReplacer    hp        = new DTagReplacer(LIST_FILE);
        int             systemId  = ba.getSystemId();
        String          fieldName = field.getBAField();
        ArrayList<Type> typeList  = Type.lookupBySystemIdAndFieldName(systemId, fieldName);
        StringBuilder   temp      = new StringBuilder();

        for (Type type : typeList) {
            String  value     = type.getDisplayName();
            boolean isDefault = type.getIsDefault();

            temp.append("\n<OPTION value=\"").append(value).append("\"");

            if (isDefault == true) {
                temp.append(" SELECTED ");
            }

            temp.append(">").append(value).append("</OPTION>");
        }

        String width = field.getWidth();

        if ((width == null) || (width.trim().equals("") == true)) {
            width = "auto;";
        }

        hp.replace("width", width);
        hp.replace("label", Utilities.htmlEncode(field.getLabel()));
        hp.replace("name", Utilities.htmlEncode(field.getName()));

        String toolTip   = field.getToolTip();
        String helpClass = "";

        if ((toolTip != null) && (toolTip.trim().equals("") == false)) {
            toolTip   = toolTip.trim();
            helpClass = "help";
        } else {
            toolTip = "";
        }

        hp.replace("help", helpClass);
        hp.replace("toolTip", Utilities.htmlEncode(toolTip));
        hp.replace("optionList", temp.toString());
        buffer.append(hp.parse(0));

        return;
    }

    /**
     *
     * @param field
     * @param buffer
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void renderParagraphField(FBField field, StringBuilder buffer) throws FileNotFoundException, IOException {
        String       paragraph = field.getParagraph();
        DTagReplacer hp        = new DTagReplacer(PARA_FILE);

        hp.replace("paragraph", Utilities.htmlEncode(paragraph));
        buffer.append(hp.parse(0));

        return;
    }

    /**
     *
     * @param field
     * @param buffer
     */
    private void renderRadioField(FBField field, StringBuilder buffer) throws FileNotFoundException, IOException {
        ArrayList<String> radioList      = field.getRadioList();
        StringBuilder     temp           = new StringBuilder();
        String            radioGroupName = field.getName();
        int               ctr            = 0;

        for (String str : radioList) {
            String id    = "radio_" + ctr;
            String value = Utilities.htmlEncode(str);

            ctr++;
            temp.append("\n<INPUT type='radio' name='").append(radioGroupName).append("' id='").append(id).append("' value='").append(value).append("'>").append(
                "<SPAN onclick=\"document.getElementById('").append(id).append("').click()\">").append(value).append("</SPAN><BR>").append("");
        }

        DTagReplacer hp = new DTagReplacer(RADIO_FILE);

        hp.replace("label", Utilities.htmlEncode(field.getLabel()));
        hp.replace("radioList", temp.toString());
        buffer.append(hp.parse(0));

        return;
    }

    /**
     *
     * @param field
     * @param buffer
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void renderStaticListField(FBField field, StringBuilder buffer) throws FileNotFoundException, IOException {
        ArrayList<FBType> typeList = field.getTypeList();
        StringBuilder     temp     = new StringBuilder();

        for (FBType type : typeList) {
            String  value     = type.getValue();
            boolean isDefault = type.getIsDefault();

            temp.append("\n<OPTION value=\"").append(value).append("\"");

            if (isDefault == true) {
                temp.append(" SELECTED ");
            }

            temp.append(">").append(value).append("</OPTION>");
        }

        DTagReplacer hp    = new DTagReplacer(LIST_FILE);
        String       width = field.getWidth();

        if ((width == null) || (width.trim().equals("") == true)) {
            width = "auto;";
        }

        hp.replace("label", Utilities.htmlEncode(field.getLabel()));
        hp.replace("name", Utilities.htmlEncode(field.getName()));

        String toolTip   = field.getToolTip();
        String helpClass = "";

        if ((toolTip != null) && (toolTip.trim().equals("") == false)) {
            toolTip   = toolTip.trim();
            helpClass = "help";
        } else {
            toolTip = "";
        }

        hp.replace("help", helpClass);
        hp.replace("toolTip", Utilities.htmlEncode(toolTip));
        hp.replace("width", width);
        hp.replace("optionList", temp.toString());
        buffer.append(hp.parse(0));

        return;
    }

    /**
     *
     * @param field
     * @param buffer
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void renderTextAreaField(FBField field, StringBuilder buffer) throws FileNotFoundException, IOException {
        DTagReplacer hp = new DTagReplacer(TA_FILE);

        hp.replace("name", Utilities.htmlEncode(field.getName()));
        hp.replace("label", Utilities.htmlEncode(field.getLabel()));

        String toolTip   = field.getToolTip();
        String helpClass = "";

        if ((toolTip != null) && (toolTip.trim().equals("") == false)) {
            toolTip   = toolTip.trim();
            helpClass = "help";
        } else {
            toolTip = "";
        }

        hp.replace("help", helpClass);
        hp.replace("toolTip", Utilities.htmlEncode(toolTip));

        String width = field.getWidth();

        if ((width == null) || (width.trim().equals("") == true)) {
            width = "100%;";
        }

        hp.replace("width", width);

        String rowCount = field.getRowCount();

        if ((rowCount == null) || (rowCount.trim().equals("") == true)) {
            rowCount = "15";
        }

        hp.replace("rowCount", rowCount);
        buffer.append(hp.parse(0));

        return;
    }

    /**
     *
     * @param field
     * @param buffer
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void renderTextBoxField(FBField field, BusinessArea ba, User user, StringBuilder buffer) throws FileNotFoundException, IOException {
        DTagReplacer hp        = new DTagReplacer(TB_FILE);
        String       label     = field.getLabel();
        String       name      = field.getName();
        String       toolTip   = field.getToolTip();
        String       helpClass = "";

        if ((toolTip != null) && (toolTip.trim().equals("") == false)) {
            toolTip   = toolTip.trim();
            helpClass = "help";
        } else {
            toolTip = "";
        }

        String value = "";

        if (field.getPrePopulate() == false) {
            value = "";
        } else {
            value = field.getValue();

            /*
             * The value obtained from the field object can be a constant or a
             * variable that represents the $DomainObject.attribute.
             * DomainObject can be a
             *   - BusinessArea
             *   - User
             */
            if (value.startsWith("$") == true) {

                // Remove the $
                value = value.substring(1);

                String domainObject = "";
                String attrName     = "";
                String paramList    = "";
                int    index        = value.indexOf(".");

                if (index > 0) {
                    domainObject = value.substring(0, index);
                    attrName     = value.substring(index + 1);

                    if (domainObject.equalsIgnoreCase("user")) {
                        UserColumn column = null;

                        try {
                            column = UserColumn.valueOf(attrName.toUpperCase());
                            value  = user.get(column);
                        } catch (Exception e) {
                            LOG.severe("",(e));
                            value = "";
                        }
                    } else if (domainObject.equalsIgnoreCase("businessarea")) {
                        BAColumn column = null;

                        try {
                            column = BAColumn.valueOf(attrName.toUpperCase());
                            value  = ba.get(column);
                        } catch (Exception e) {
                            LOG.severe("",(e));
                            value = "";
                        }
                    } else {
                        LOG.severe("Unknown domain object " + "configured for pre-poulation: " + domainObject);
                        value = "";
                    }
                } else {
                    LOG.severe("Incorrect syntax used to generate a value " + "for pre-populating " + name + ": " + value);
                    value = "";
                }
            }
        }

        String width = field.getWidth();

        if ((width == null) || (width.trim().equals("") == true)) {
            width = "100px;";
        }

        hp.replace("width", width);
        hp.replace("name", Utilities.htmlEncode(name));
        hp.replace("label", Utilities.htmlEncode(label));
        hp.replace("toolTip", Utilities.htmlEncode(field.getToolTip()));
        hp.replace("help", helpClass);
        hp.replace("toolTip", toolTip);
        hp.replace("value", Utilities.htmlEncode(value));

        if (field.getIsReadOnly() == true) {
            hp.replace("readOnly", "readonly");
        } else {
            hp.replace("readOnly", "");
        }

        buffer.append(hp.parse(0));

        return;
    }

    /**
     *
     * @param field
     * @param buffer
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void renderUserField(FBField field, StringBuilder buffer) throws FileNotFoundException, IOException {
        DTagReplacer hp    = new DTagReplacer(USER_FILE);
        String       width = field.getWidth();

        if ((width == null) || (width.trim().equals("") == true)) {
            width = "100px;";
        }

        hp.replace("width", width);
        hp.replace("name", Utilities.htmlEncode(field.getName()));
        hp.replace("label", Utilities.htmlEncode(field.getLabel()));

        String toolTip   = field.getToolTip();
        String helpClass = "";

        if ((toolTip != null) && (toolTip.trim().equals("") == false)) {
            toolTip   = toolTip.trim();
            helpClass = "help";
        } else {
            toolTip = "";
        }

        hp.replace("help", helpClass);
        hp.replace("toolTip", Utilities.htmlEncode(toolTip));
        buffer.append(hp.parse(0));

        return;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the primary descriptor of the given field in the
     * specified business area. If there is no primary descriptor or an
     * exception occurred during the process, then it will return the default
     * value specified.
     *
     * @param systemId          System ID.
     * @param fieldName         Field Name
     * @param defaultValue      Default Value.
     * @return  Primary Field Descriptor.
     */
    private static String getPrimaryDesc(int systemId, String fieldName, String defaultValue) {
        FieldDescriptor fd         = null;
        String          descriptor = defaultValue;

        try {
            fd = FieldDescriptor.getPrimaryDescriptor(systemId, fieldName);

            if (fd != null) {
                descriptor = fd.getDescriptor();
            }
        } catch (DatabaseException de) {
            LOG.severe("",(de));
        }

        return descriptor;
    }
}
