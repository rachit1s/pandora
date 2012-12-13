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
 * TBitsPropEnum.java
 *
 * \$Header:
 */
package transbit.tbits.Helper;

/**
 * This interface holds the names of the keys present in the tbits.properties

 * @version $Id: $
 * @author nitiraj
 */
public interface TBitsPropEnum {
	public static final String EMAIL_TO_FIELD = "email.to.field.name";
	public static final String EMAIL_CC_FIELD = "email.cc.field.name";
	public static final String EMAIL_CC_APPEND_IN_REQUEST = "email.cc.field.append";
	public static final String EMAIL_ATTACHMENT_FIELD = "email.attachment.field.name";
	public static final String EMAIL_LOGGER_FIELD = "email.logger.field.name";
	public static final String EMAIL_DESCRIPTION_FIELD = "email.description.field.name";
	
	public static final String SHOW_ATTACHMENT_IN_EMAIL_HEADER = "show.attachment.in.email.header";
	
    public static final String KEY_APP_NAME           = "transbit.app.name";
    public static final String KEY_APP_VERSION        = "transbit.app.version";
    public static final String KEY_DOMAIN             = "transbit.tbits.myDomain";
    public static final String KEY_ALLOW_AUTO_ADD_USER = "transbit.tbits.allowAutoAddUser";
    public static final String KEY_DB_SERVER          = "transbit.database.server";
    public static final String KEY_DB_PASSWORD        = "transbit.database.password";
    public static final String KEY_DB_NAME            = "transbit.database.name";
    public static final String KEY_DB_LOGIN           = "transbit.database.login";
    public static final String KEY_DRIVER_NAME        = "transbit.database.driverName";
    public static final String KEY_DRIVER_TAG         = "transbit.database.driverTag";
    public static final String KEY_DOMAIN_PASSWORD    = "transbit.domain.password";
    public static final String KEY_DOMAIN_LOGIN       = "transbit.domain.login";
    public static final String KEY_PATH_SENDMAIL      = "transbit.tbits.sendMail";
    public static final String KEY_PATH_RTFTOTEXT     = "transbit.tbits.";
    public static final String KEY_PATH_PDFTOTEXT     = "transbit.tbits.pdftotext";
    public static final String KEY_PATH_EXTRACT       = "transbit.tbits.extract";
    public static final String KEY_PATH_ANTIWORD      = "transbit.tbits.antiword";
    public static final String KEY_INDEXDIR           = "transbit.tbits.indexdir";
    public static final String KEY_ATTACHMENTDIR      = "transbit.tbits.attachmentdir";
    public static final String KEY_MARKUPDIR      = "transbit.tbits.markupdir";
    public static final String KEY_TOMCATDIR          = "transbit.tbits.tomcatdir";
    public static final String KEY_TMPDIR             = "transbit.tbits.tmpdir";
    public static final String KEY_REDIRECTION_URL    = "transbit.tbits.redirectionURL";
    public static final String KEY_PORT               = "transbit.tbits.port";
    public static final String KEY_NYCURL             = "transbit.tbits.nycUrl";
    public static final String KEY_NTLM_ENABLED       = "transbit.tbits.ntlmEnabled";
    public static final String KEY_NEAREST_INSTANCE   = "transbit.tbits.nearestInstance";
    public static final String KEY_LOGGER_NOTIFY_TO   = "transbit.logger.notifyFailureTo";
    public static final String KEY_LOGGER_NOTIFY_FROM = "transbit.logger.notifyFailureFrom";
    public static final String KEY_LOGDIR             = "transbit.tbits.logdir";
    public static final String KEY_INSTANCE_LIST      = "transbit.tbits.instanceList";
    public static final String KEY_HYDURL             = "transbit.tbits.hydUrl";
    public static final String KEY_APACHEDIR          = "transbit.tbits.apachedir";
    public static final String KEY_AD_HOST            = "transbit.tbits.adHost";
    public static final String TRANSBIT_TBITS_ADUSERSEARCHQUERY = "transbit.tbits.adUserSearchQuery";
    public static final String TRANSBIT_TBITS_ADGROUPSEARCHQUERY = "transbit.tbits.adGroupSearchQuery";
    public static final String TRANSBIT_TBITS_ADCONTACTSEARCHQUERY = "transbit.tbits.adContactSearchQuery";
    public static final String TRANSBIT_TBITS_ADSEARCHBASE = "transbit.tbits.adSearchBase";
    public static final String IS_SMS_ENABLED	= "transbit.tbits.sms.enabled";
    public static final String KEY_SEVERITY_IMGS	= "transbit.tbits.severityimgs";
    public static final String JVUESERVER      =   "transbit.tbits.jvueserver";
    public static final String JVUECODEBASE =     "transbit.tbits.jvuecodebase";
    public static final String IS_AUTOVUE_ENABLED = "transbit.tbits.isautovueenabled";
    public static final String KEY_INTERNET_PROXY_SERVER = "transbit.tbits.intenet.proxyserver";
	public static final String KEY_INTERNET_PROXY_ISENABLED = "transbit.tbits.intenet.isproxyenabled";
	public static final String KEY_INTERNET_PROXY_PORT = "transbit.tbits.intenet.proxyport";
	public static final String KEY_INTERNET_PROXY_USER = "transbit.tbits.intenet.proxyuser";
	public static final String KEY_INTERNET_PROXY_Password = "transbit.tbits.intenet.proxypassword";
	public static final String KEY_ISCLASSIC_UI = "transbit.tbits.isclassicui";
	public static final String IS_FREEWHEEL_ENABLED = "transbit.tbits.isfreewheelenable";
	public static final String FREEWHEEL_URL="transbit.tbits.freewheelurl"; 
}
