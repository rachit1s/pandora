package transbit.tbits.scheduler.sap.request;

import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import org.apache.commons.net.ftp.FTPClient;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import transbit.tbits.scheduler.sap.connector.SapDestinationDataProvider;
import transbit.tbits.scheduler.sap.util.Constants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
//import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;
import transbit.tbits.searcher.DqlSearcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author: Mukesh Sharma
 * Date: 8/8/12
 * Time: 2:56 PM
 */
public class PushRequestToSAPJob implements ITBitsJob {
    public static final TBitsLogger LOG = TBitsLogger.getLogger("SAPJOB");

    public static final String RfcFileType = "RFC";
    public static final String AsBuiltFileType = "AsBuilt";

    public static final String JcoClient = "JCO_CLIENT";
    public static final String SapUser = "SAP_USER";
    public static final String SapPassword = "SAP_PASSWORD";
    public static final String SapLanguage = "SAP_LANGUAGE";
    public static final String SapHost = "SAP_HOST";
    public static final String SapSystem = "SAP_SYSTEM";
    public static final String SapSysNR = "SAP_SYSNR";

    public static final String BaName = "BA_NAME";
    public static final String DocumentType = "DOCUMENTDATA_DOCUMENTTYPE";
    public static final String DocumentPart = "DOCUMENTDATA_DOCUMENTPART";
    public static final String DocumentVersion = "DOCUMENTDATA_DOCUMENTVERSION";
    public static final String PfFtpDest = "PF_FTP_DEST";
    public static final String CharValueClassType = "CHARACTERISTICVALUES_CLASSTYPE";
    public static final String CharValueClassName = "CHARACTERISTICVALUES_CLASSNAME";
    public static final String AuthorityGroup = "DOCUMENTDATA_AUTHORITYGROUP_MAPPING";
    public static final String Description = "DOCUMENTDATA_DESCRIPTION_MAPPING";
    public static final String EdmsDocNumber = "EDMSDOCNO_MAPPING";
    public static final String RFCField = "RFC_FILETYPE_MAPPING";
    public static final String AsBuiltField = "AS_BUILT_FILETYPE_MAPPING";
    public static final String OriginatorField = "ORIGINATOR_MAPPING";
    public static final String RevisionField = "REVISION_MAPPING";
    public static final String AreaField = "AREA_MAPPING";
    public static final String PackageField = "PACKAGE_MAPPING";
    public static final String DisciplineField = "DISCIPLINE_MAPPING";
    public static final String InspectionCategoryField = "INSPECTIONCATEGORY_MAPPING";
    public static final String StorageCategory = "DOCUMENTFILES_STORAGECATEGORY_MAPPING";
    public static final String Dql = "SINGLE_DQL_FOR_ALL_REQUESTS";


    public Hashtable<String, JobParameter> getParameters() throws SQLException {
        Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();

        JobParameter jp = new JobParameter();
        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(JcoClient);
        jp.setMandatory(true);
        params.put(JcoClient, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(SapUser);
        jp.setMandatory(true);
        params.put(SapUser, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(SapPassword);
        jp.setMandatory(true);
        params.put(SapPassword, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(SapLanguage);
        jp.setMandatory(false);
        params.put(SapLanguage, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(SapHost);
        jp.setMandatory(true);
        params.put(SapHost, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(SapSystem);
        jp.setMandatory(true);
        params.put(SapSystem, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(SapSysNR);
        jp.setMandatory(true);
        params.put(SapSysNR, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(Constants.FTP_USERUSER);
        jp.setMandatory(true);
        params.put(Constants.FTP_USERUSER, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(Constants.FTP_PASSWORD);
        jp.setMandatory(true);
        params.put(Constants.FTP_PASSWORD, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(Constants.FTP_FILE_LOCATION);
        jp.setMandatory(true);
        params.put(Constants.FTP_FILE_LOCATION, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(BaName);
        jp.setMandatory(true);
        params.put(BaName, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(Dql);
        jp.setMandatory(true);
        params.put(Dql, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(DocumentType);
        jp.setMandatory(true);
        params.put(DocumentType, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(DocumentPart);
        jp.setMandatory(true);
        params.put(DocumentPart, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(DocumentVersion);
        jp.setMandatory(true);
        params.put(DocumentVersion, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(PfFtpDest);
        jp.setMandatory(true);
        params.put(PfFtpDest, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(CharValueClassType);
        jp.setMandatory(true);
        params.put(CharValueClassType, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(CharValueClassName);
        jp.setMandatory(true);
        params.put(CharValueClassName, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(EdmsDocNumber);
        jp.setMandatory(true);
        params.put(EdmsDocNumber, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(RFCField);
        jp.setMandatory(true);
        params.put(RFCField, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(AsBuiltField);
        jp.setMandatory(true);
        params.put(AsBuiltField, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(OriginatorField);
        jp.setMandatory(true);
        params.put(OriginatorField, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(RevisionField);
        jp.setMandatory(true);
        params.put(RevisionField, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(AreaField);
        jp.setMandatory(true);
        params.put(AreaField, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(PackageField);
        jp.setMandatory(true);
        params.put(PackageField, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(DisciplineField);
        jp.setMandatory(true);
        params.put(DisciplineField, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(InspectionCategoryField);
        jp.setMandatory(true);
        params.put(InspectionCategoryField, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(StorageCategory);
        jp.setMandatory(true);
        params.put(StorageCategory, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(AuthorityGroup);
        jp.setMandatory(true);
        params.put(AuthorityGroup, jp);

        jp = new JobParameter();
        jp.setType(ParameterType.Text);
        jp.setName(Description);
        jp.setMandatory(true);
        params.put(Description, jp);

        return params;

    }

    private boolean illegal(String msg) {
        LOG.info(msg);
        System.out.println(msg);
        throw new IllegalArgumentException(msg);
    }

    public String getDisplayName() {
        return "SAP INTEGRATION JOB";
    }

    public boolean validateParams(Hashtable<String, String> params) {
        try {
            if (null == params.get(BaName)) {
                illegal(BaName + " can not be null");
            }

            if (null == params.get(JcoClient)) {
                illegal(JcoClient + " can not be null");
            }

            if (null == params.get(SapUser)) {
                illegal(SapUser + " can not be null");
            }

            if (null == params.get(SapPassword)) {
                illegal(SapPassword + " can not be null");
            }

//Not needed if not there, we can use en as default language
/*
            if (null == params.get(SapLanguage)) {
                illegal(SapLanguage + " can not be null");
            }
*/

            if (null == params.get(SapHost)) {
                illegal(SapHost + " can not be null");
            }

            if (null == params.get(SapSystem)) {
                illegal(SapSystem + " can not be null");
            }

            if (null == params.get(SapSysNR)) {
                illegal(SapSysNR + " can not be null");
            }

            if (null == params.get(Constants.FTP_USERUSER)) {
                illegal(Constants.FTP_USERUSER + " can not be null");
            }

            if (null == params.get(Constants.FTP_PASSWORD)) {
                illegal(Constants.FTP_PASSWORD + " can not be null");
            }

            if (null == params.get(Constants.FTP_FILE_LOCATION)) {
                illegal(Constants.FTP_FILE_LOCATION + " can not be null");
            }

            if (null == params.get(DocumentType)) {
                illegal(DocumentType + " can not be null");
            }

            if (null == params.get(DocumentPart)) {
                illegal(DocumentPart + " can not be null");
            }

            if (null == params.get(DocumentVersion)) {
                illegal(DocumentVersion + " can not be null");
            }

            if (null == params.get(PfFtpDest)) {
                illegal(PfFtpDest + " can not be null");
            }
            if (null == params.get(CharValueClassType)) {
                illegal(CharValueClassType + " can not be null");
            }
            if (null == params.get(CharValueClassName)) {
                illegal(CharValueClassName + " can not be null");
            }
            if (null == params.get(AuthorityGroup)) {
                illegal(AuthorityGroup + " can not be null");
            }
            if (null == params.get(Description)) {
                illegal(Description + " can not be null");
            }
            if (null == params.get(EdmsDocNumber)) {
                illegal(EdmsDocNumber + " can not be null");
            }
            if (null == params.get(RFCField)) {
                illegal(RFCField + " can not be null");
            }
            if (null == params.get(AsBuiltField)) {
                illegal(AsBuiltField + " can not be null");
            }
            if (null == params.get(OriginatorField)) {
                illegal(OriginatorField + " can not be null");
            }
            if (null == params.get(RevisionField)) {
                illegal(RevisionField + " can not be null");
            }

            if (null == params.get(AreaField)) {
                illegal(AreaField + " can not be null");
            }

            if (null == params.get(PackageField)) {
                illegal(PackageField + " can not be null");
            }

            if (null == params.get(DisciplineField)) {
                illegal(DisciplineField + " can not be null");
            }

            if (null == params.get(InspectionCategoryField)) {
                illegal(InspectionCategoryField + " can not be null");
            }

            if (null == params.get(StorageCategory)) {
                illegal(StorageCategory + " can not be null");
            }

            if (null == params.get(Dql)) {
                illegal(Dql + " can not be null");
            }
        } catch (IllegalArgumentException iae) {
            return false;
        }

        return true;
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDetail jd = jobExecutionContext.getJobDetail();
        JobDataMap jdm = jd.getJobDataMap();
        Hashtable<String, String> params = new Hashtable<String, String>();

        params.put(JcoClient, jdm.getString(JcoClient));
        params.put(SapUser, jdm.getString(SapUser));
        params.put(SapPassword, jdm.getString(SapPassword));
        params.put(SapLanguage, jdm.getString(SapLanguage));
        params.put(SapHost, jdm.getString(SapHost));
        params.put(SapSystem, jdm.getString(SapSystem));
        params.put(SapSysNR, jdm.getString(SapSysNR));
        params.put(BaName, jdm.getString(BaName));
        params.put(DocumentType, jdm.getString(DocumentType));
        params.put(DocumentPart, jdm.getString(DocumentPart));
        params.put(DocumentVersion, jdm.getString(DocumentPart));
        params.put(PfFtpDest, jdm.getString(PfFtpDest));
        params.put(CharValueClassType, jdm.getString(CharValueClassType));
        params.put(CharValueClassName, jdm.getString(CharValueClassName));
        params.put(AuthorityGroup, jdm.getString(AuthorityGroup));
        params.put(Description, jdm.getString(Description));
        params.put(EdmsDocNumber, jdm.getString(EdmsDocNumber));
        params.put(RFCField, jdm.getString(RFCField));
        params.put(AsBuiltField, jdm.getString(AsBuiltField));
        params.put(OriginatorField, jdm.getString(OriginatorField));
        params.put(RevisionField, jdm.getString(RevisionField));
        params.put(AreaField, jdm.getString(AreaField));
        params.put(PackageField, jdm.getString(PackageField));
        params.put(DisciplineField, jdm.getString(DisciplineField));
        params.put(InspectionCategoryField, jdm.getString(InspectionCategoryField));
        params.put(StorageCategory, jdm.getString(StorageCategory));
        params.put(Dql, jdm.getString(Dql));

        //These constants are being used in file upload too hence kept in Constants.java file
        params.put(Constants.FTP_USERUSER, jdm.getString(Constants.FTP_USERUSER));
        params.put(Constants.FTP_PASSWORD, jdm.getString(Constants.FTP_PASSWORD));
        params.put(Constants.FTP_FILE_LOCATION, jdm.getString(Constants.FTP_FILE_LOCATION));
        params.put(Constants.FTP_HOSTHOST, jdm.getString(SapHost));

        process(params);
        System.out.println("Execution done: SAP Integration Job");
    }

    public void process(Hashtable<String, String> params) throws JobExecutionException {

        if (!validateParams(params)) {
            LOG.severe("Job Parameters for Job class =  " + this.getClass().getName() + " : job name = " + this.getDisplayName() + " : could not be validated. Hence NOT executing the job.");
            System.out.println("Job Parameters for Job class =  " + this.getClass().getName() + " : job name = " + this.getDisplayName() + " : could not be validated. Hence NOT executing the job.");
            return;
        }

        BusinessArea ba = null;
        try {
            ba = BusinessArea.lookupBySystemPrefix(params.get(BaName));
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        if (null == ba) {
            LOG.severe("Cannot find the BusinessArea with sysprefix : " + params.get(BaName) + ". So skipping the Escalations on this BusinessArea.");
            System.out.println("Cannot find the BusinessArea with sysprefix : " + params.get(BaName) + ". So skipping the Escalations on this BusinessArea.");
            return;
        }

        DqlSearcher searcher = new DqlSearcher(ba.getSystemId(), params.get(Dql));

        try {
            searcher.search();
        } catch (Exception e1) {
            e1.printStackTrace();
            LOG.severe("Exception occurred while searching. The Escalations might not work correctly.");
            System.out.println("Exception occurred while searching. The Escalations might not work correctly.");
        }

        ArrayList<Integer> reqIds = new ArrayList<Integer>();
        if (searcher.getResult().containsKey(ba.getSystemId())) {
            Collection<Integer> requestIdsFetchedColl = searcher.getResult().get(ba.getSystemId()).keySet();
            if (requestIdsFetchedColl != null) {
                reqIds.addAll(requestIdsFetchedColl);
            }
        }

        LOG.info("Following requests will be processed to push RFC and As Built files to SAP for ba=(" + ba.getSystemPrefix() + ") and dql=(" + params.get(Dql) + ")" + " : " + reqIds);

        System.out.println("tbits ID \t SAP ID");
        LOG.info("tbits ID \t SAP ID");
            for (Integer requestId : reqIds) {
                try {
                    Request request = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
                    if (null != request) {

                        LOG.info("Processing request : " + ba.getSystemPrefix() + "#" + request.getRequestId() + "#" + request.getMaxActionId());
                        // System.out.println("Processing request : " + ba.getSystemPrefix() + "#" + request.getRequestId() + "#" + request.getMaxActionId());
                        if (null == request) {
                            LOG.info("The returned request was null.");
                            //System.out.println("The returned request was null.");
                            continue;
                        }
                        //check for RFC files and push documents if exists
                        Collection<AttachmentInfo> exAttachments = (Collection<AttachmentInfo>) request.getObject(params.get(RFCField));
                        if (null != exAttachments && exAttachments.size() > 0) {
                            String sapDocNumber = pushRequestDocument(params, request, RfcFileType);
                            if (null != sapDocNumber) {
                                LOG.debug(requestId + " : " + sapDocNumber);
                                System.out.println(requestId + " : " + sapDocNumber);
                            } else {
                                LOG.debug("Could not push document number " + request.getSystemId() + ", Please refer logs for more info");
                                System.out.println("Could not push document number " + request.getSystemId() + ", Please refer logs for more info");
                            }

                        }

                        //Check for As Built file and push to transbit.tbits.scheduler.sap if exists any
                        exAttachments = (Collection<AttachmentInfo>) request.getObject(params.get(AsBuiltField));

                        if (null != exAttachments && exAttachments.size() > 0) {
                            String sapDocNumber = pushRequestDocument(params, request, AsBuiltFileType);
                            if (null != sapDocNumber) {
                                LOG.debug(requestId + " : " + sapDocNumber);
                                System.out.println(requestId + " : " + sapDocNumber);
                            } else {
                                throw new JobExecutionException("Error pushing document to transbit.tbits.scheduler.sap");
                            }

                        }
                    }
                } catch (Exception e) {
                    LOG.info("Exception while retrieving requests." + e);
                    //e.printStackTrace();
                    System.out.println(e.getMessage());

                }
            }
    }

    public String pushRequestDocument(Hashtable<String, String> params, Request currentRequest, String fileType) {

        String sapDocNumber = null;
        Collection<AttachmentInfo> attachmentInfos = null;
/*
        SapDestinationDataProvider sapDestinationDataProvider = new SapDestinationDataProvider();
        com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(sapDestinationDataProvider);
        sapDestinationDataProvider.setDestinationName(Constants.DESTINATION_NAME);
*/
        Properties connectionProperties = getConnectionProperties(params);
        SapDestinationDataProvider sapDestinationDataProvider = new SapDestinationDataProvider();
        sapDestinationDataProvider.setDestinationName(Constants.DESTINATION_NAME);
        sapDestinationDataProvider.changeProperties(connectionProperties);
        Environment.registerDestinationDataProvider(sapDestinationDataProvider);

        try {

            JCoDestination destination = JCoDestinationManager.getDestination(Constants.DESTINATION_NAME);
            JCoRepository jCoRepository = destination.getRepository();
            JCoFunction functionToCallBAPI = jCoRepository.getFunction(Constants.SAP_FUNCTION_NAME);
            if (null == functionToCallBAPI) {
                throw new RuntimeException(Constants.SAP_FUNCTION_NAME + " not found in SAP.");
            }

            JCo.setMiddlewareProperty("jco.middleware.allow_start_of_programs", "ftp;sapftp;sapftpa;sapkprotp;http;saphttp;saphttpa");

            //JCoFunction is container for function values. Each function contains separate
            //containers for import, export, changing and table parameters.
            //To set or get the parameters use the APIS setValue() and getXXX().
            JCoParameterList jCoParameterList = functionToCallBAPI.getImportParameterList();

            jCoParameterList.setValue("PF_FTP_DEST", "SAPFTPA");
            jCoParameterList.setValue("PF_HTTP_DEST", "SAPHTTPA");

            //Set values for DOCUMENTDATA structure
            JCoStructure documentData = jCoParameterList.getStructure("DOCUMENTDATA");
            String authorityGroup = params.get(AuthorityGroup).toString();
            if (null == authorityGroup) {
                throw new JobExecutionException("Could not find filed " + params.get(AuthorityGroup) + " in request with id " + currentRequest.getRequestId());
            }
            documentData.setValue("AUTHORITYGROUP", authorityGroup);

            String description = currentRequest.get(params.get(Description)).toString();
            if (null == description) {
                throw new JobExecutionException("Could not find filed " + params.get(Description) + " in request with id " + currentRequest.getRequestId());
            }

            documentData.setValue("DESCRIPTION", description);
            documentData.setValue("DOCUMENTTYPE", params.get(DocumentType));
            documentData.setValue("DOCUMENTPART", params.get(DocumentPart));
            documentData.setValue("DOCUMENTVERSION", params.get(DocumentVersion));
            documentData.setValue("STATUSEXTERN", "CR");

            String charValueClassType = params.get(CharValueClassType);
            String charValueClassName = params.get(CharValueClassName);

            JCoParameterList jCoTableParameterList = functionToCallBAPI.getTableParameterList();//.getTable("BAPI_DOC_FILES2");
            JCoTable jCoCharValueTable = jCoTableParameterList.getTable("CHARACTERISTICVALUES");
            //add a row to table
            jCoCharValueTable.appendRow();

            //add values to row
            jCoCharValueTable.setValue("CLASSTYPE", charValueClassType);
            jCoCharValueTable.setValue("CLASSNAME", charValueClassName);
            jCoCharValueTable.setValue("CHARNAME", "EDMSDOCNO");
            //add vendor number "Documents no" here.
            String edmsDocNumber = currentRequest.get(params.get(EdmsDocNumber)).toString();
            if (null == edmsDocNumber) {
                throw new JobExecutionException("Could not find filed " + params.get(EdmsDocNumber) + " in request with id " + currentRequest.getRequestId());
            }

            jCoCharValueTable.setValue("CHARVALUE", edmsDocNumber);

            //TODO: check for file and set accordingly
            jCoCharValueTable.appendRow();
            jCoCharValueTable.setValue("CLASSTYPE", charValueClassType);
            jCoCharValueTable.setValue("CLASSNAME", charValueClassName);
            jCoCharValueTable.setValue("CHARNAME", "FILETYPE");
            //check for "RFC" or "AS BUILT" and add accordingly
            if (fileType.equalsIgnoreCase(RfcFileType)) {
                jCoCharValueTable.setValue("CHARVALUE", "RFC");
            } else {
                jCoCharValueTable.setValue("CHARVALUE", "As Built");
            }

            jCoCharValueTable.appendRow();
            jCoCharValueTable.setValue("CLASSTYPE", charValueClassType);
            jCoCharValueTable.setValue("CLASSNAME", charValueClassName);
            jCoCharValueTable.setValue("CHARNAME", "REVISION");
            //add Revision no here
            String revision = currentRequest.get(params.get(RevisionField)).toString();
            if (null == revision) {
                throw new JobExecutionException("Could not find filed " + params.get(RevisionField) + " in request with id " + currentRequest.getRequestId());
            }

            jCoCharValueTable.setValue("CHARVALUE", revision);

            jCoCharValueTable.appendRow();
            jCoCharValueTable.setValue("CLASSTYPE", charValueClassType);
            jCoCharValueTable.setValue("CLASSNAME", charValueClassName);
            jCoCharValueTable.setValue("CHARNAME", "AREA");
            //add AREA here
            String area = currentRequest.get(params.get(AreaField)).toString();
            if (null == area) {
                throw new JobExecutionException("Could not find filed " + params.get(AreaField) + " in request with id " + currentRequest.getRequestId());
            }

            jCoCharValueTable.setValue("CHARVALUE", area);

            jCoCharValueTable.appendRow();
            jCoCharValueTable.setValue("CLASSTYPE", charValueClassType);
            jCoCharValueTable.setValue("CLASSNAME", charValueClassName);
            jCoCharValueTable.setValue("CHARNAME", "PACKAGE");
            //add Category_id or Package here
            String packageValue = currentRequest.get(params.get(PackageField));
            if (null == packageValue) {
                throw new JobExecutionException("Could not find filed " + params.get(packageValue) + " in request with id " + currentRequest.getRequestId());
            }

            jCoCharValueTable.setValue("CHARVALUE", packageValue);

            jCoCharValueTable.appendRow();
            jCoCharValueTable.setValue("CLASSTYPE", charValueClassType);
            jCoCharValueTable.setValue("CLASSNAME", charValueClassName);
            jCoCharValueTable.setValue("CHARNAME", "DISCIPLINE");
            //add DISCIPLINE here
            String discipline = currentRequest.get(params.get(DisciplineField));
            if (null == discipline) {
                throw new JobExecutionException("Could not find filed " + params.get(discipline) + " in request with id " + currentRequest.getRequestId());
            }

            jCoCharValueTable.setValue("CHARVALUE", discipline);

            jCoCharValueTable.appendRow();
            jCoCharValueTable.setValue("CLASSTYPE", charValueClassType);
            jCoCharValueTable.setValue("CLASSNAME", charValueClassName);
            jCoCharValueTable.setValue("CHARNAME", "ORIGINATOR");
            //add ORIGINATOR here
            String originator = currentRequest.get(params.get(OriginatorField));
            if (null == originator) {
                throw new JobExecutionException("Could not find filed " + params.get(OriginatorField) + " in request with id " + currentRequest.getRequestId());
            }
            jCoCharValueTable.setValue("CHARVALUE", originator);

            jCoCharValueTable.appendRow();
            jCoCharValueTable.setValue("CLASSTYPE", charValueClassType);
            jCoCharValueTable.setValue("CLASSNAME", charValueClassName);
            jCoCharValueTable.setValue("CHARNAME", "INSPECTIONCATEGORY");
            //add Category here
            String inspectionCategory = currentRequest.get(params.get(InspectionCategoryField));
            if (null == inspectionCategory) {
                throw new JobExecutionException("Could not find filed " + params.get(InspectionCategoryField) + " in request with id " + currentRequest.getRequestId());
            }
            jCoCharValueTable.setValue("CHARVALUE", inspectionCategory);

            /*
           * File details are to be set here
           * DOCUMENTFILES-STORAGECATEGORY = ' ZRQ2100'.
           * DOCUMENTFILES-WSAPPLICATION = 'PDF' or 'TXT' etc
           * DOCUMENTFILES-DOCPATH = for example 'C:\DOCUMENTS AND SETTINGS\CNLT404\DESKTOP\' or '/tmp/'.
           * DOCUMENTFILES-DOCFILE = for example  'Test.TXT'
           * DOCUMENTFILES-DESCRIPTION = provide attachment description
           *
           * */

            JCoTable jCoTableDocFiles = jCoTableParameterList.getTable("DOCUMENTFILES");

            jCoTableDocFiles.deleteAllRows();

            //Upload files attached using FTP

            if (fileType.equalsIgnoreCase(RfcFileType)) {
                attachmentInfos = (Collection<AttachmentInfo>) currentRequest.getObject(params.get(RFCField));
            } else {
                attachmentInfos = (Collection<AttachmentInfo>) currentRequest.getObject(params.get(AsBuiltField));
            }

            if (null == attachmentInfos || attachmentInfos.size() == 0) {
                LOG.debug("No files to upload");
                //System.out.println("No files to upload");
                return sapDocNumber;
            }

            upload(attachmentInfos, params, jCoTableDocFiles);

            //function to commit transaction. Need to run this after BAPI_DOCUMENT_CREATE2 to make changes persistent.
            JCoFunction commitFunction = jCoRepository.getFunction("BAPI_TRANSACTION_COMMIT");
            JCoParameterList commitImportList = commitFunction.getImportParameterList();

            commitImportList.setValue("WAIT", 'X');

            //start the transaction from here
            JCoContext.begin(destination);

            functionToCallBAPI.execute(destination);

            JCoStructure returnStructure = functionToCallBAPI.getExportParameterList().getStructure("RETURN");
            if (!(returnStructure.getString("TYPE").equals("") || returnStructure.getString("TYPE").equals("S"))) {
                throw new RuntimeException(returnStructure.getString("MESSAGE"));
            }

            commitFunction.execute(destination);
            JCoContext.end(destination);
            //end a transaction here.

            returnStructure = commitFunction.getExportParameterList().getStructure("RETURN");
            if (!(returnStructure.getString("TYPE").equals("") || returnStructure.getString("TYPE").equals("S"))) {
                throw new RuntimeException(returnStructure.getString("MESSAGE"));
            }

            JCoParameterList exportParamsList = functionToCallBAPI.getExportParameterList();
            sapDocNumber = exportParamsList.getString("DOCUMENTNUMBER");


        } catch (Exception e) {
            LOG.info(e.getMessage());
            System.out.println(e);
        } finally {
            Environment.unregisterDestinationDataProvider(sapDestinationDataProvider);
            try {
                if (attachmentInfos != null) {
                    deleteAllFiles(attachmentInfos, params);
                }
            } catch (Exception e) {
                LOG.info(e.getMessage());
                System.out.println(e.getMessage());
            }

        }

        return sapDocNumber;
    }

    Properties getConnectionProperties(Hashtable<String, String> params) {
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty(DestinationDataProvider.JCO_ASHOST,
                params.get(SapHost));
        connectionProperties.setProperty(DestinationDataProvider.JCO_SYSNR, params.get(SapSysNR));
        connectionProperties.setProperty(DestinationDataProvider.JCO_CLIENT, params.get(JcoClient));
        connectionProperties.setProperty(DestinationDataProvider.JCO_USER, params.get(SapUser));
        connectionProperties.setProperty(DestinationDataProvider.JCO_PASSWD, params.get(SapPassword));

        String language = params.get(SapLanguage);
        if (null == language) {
            language = "en";
        }
        connectionProperties.setProperty(DestinationDataProvider.JCO_LANG, language);

        return connectionProperties;
    }


    public static void upload(Collection<AttachmentInfo> exAttachments, Hashtable<String, String> params, JCoTable jCoTableDocFiles) throws IOException, DatabaseException, JobExecutionException {

        //Clean the directory, in case deletion was not successful last time files were uploaded
        deleteAllFiles(exAttachments, params);

        for (AttachmentInfo attachmentInfo : exAttachments) {
            File file = new File(APIUtil.getAttachmentLocation() + "/" + Uploader.getFileLocation(attachmentInfo.getRepoFileId()));
            String ftpServer = params.get(Constants.FTP_HOSTHOST);
            String user = params.get(Constants.FTP_USERUSER);
            String password = params.get(Constants.FTP_PASSWORD);

            FTPClient ftp = new FTPClient();
            ftp.connect(ftpServer);
            boolean success = ftp.login(user, password);
            if (!success) {
                LOG.info("Could not connect to FTP. Please check the credentials");
                //System.out.println("Could not connect to FTP. Please check the credentials");
                throw new JobExecutionException("Could not connect to FTP. Please check the credentials");
            }

            FileInputStream in = new FileInputStream(file);
            ftp.storeFile(attachmentInfo.getName(), in);

            jCoTableDocFiles.appendRow();
            jCoTableDocFiles.setValue("STORAGECATEGORY", params.get(StorageCategory));
            jCoTableDocFiles.setValue("WSAPPLICATION", attachmentInfo.getName().substring(attachmentInfo.getName().lastIndexOf(".") + 1).toUpperCase());
//            jCoTableDocFiles.setValue("DOCPATH", "/TBITS/");
            jCoTableDocFiles.setValue("DOCPATH", params.get(Constants.FTP_FILE_LOCATION).toString() + "/");
            jCoTableDocFiles.setValue("DOCFILE", attachmentInfo.getName());
            jCoTableDocFiles.setValue("CHECKEDIN", 'X');
            jCoTableDocFiles.setValue("DESCRIPTION", "");

        }
        //deleteAllFiles(exAttachments, params);
    }

    /**
     * Download a file from a FTP server. A FTP URL is generated with the
     * following syntax:
     * ftp://user:password@host:port/filePath;type=i.
     *
     * @throws java.net.MalformedURLException,
     *          IOException on error.
     */

    public static void deleteAllFiles(Collection<AttachmentInfo> exAttachments, Hashtable<String, String> params) throws JobExecutionException {
        String ftpServer = params.get(Constants.FTP_HOSTHOST);
        String user = params.get(Constants.FTP_USERUSER);
        String password = params.get(Constants.FTP_PASSWORD);

        try {
            FTPClient client = new FTPClient();
            client.connect(ftpServer);
            boolean success = client.login(user, password);
            if (!success) {
                LOG.info("Could not connect to FTP. Please check the credentials");
                throw new JobExecutionException("Could not connect to FTP. Please check the credentials");
            }

            for (AttachmentInfo attachmentInfo : exAttachments) {

                Boolean deleted = client.deleteFile(params.get(Constants.FTP_FILE_LOCATION) + "/" + attachmentInfo.getName());
                if (deleted) {
                    LOG.info("Directory cleaned");
                    //System.out.println("Directory cleaned");
                }
            }
            client.disconnect();
        } catch (Exception e) {
            LOG.info(e);
            System.out.println(e);
        }
    }
}
