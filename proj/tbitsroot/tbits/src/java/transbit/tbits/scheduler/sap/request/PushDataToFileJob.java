package transbit.tbits.scheduler.sap.request;

import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import org.apache.commons.net.ftp.FTPClient;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.scheduler.sap.connector.SapDestinationDataProvider;
import transbit.tbits.scheduler.sap.util.Constants;
import transbit.tbits.scheduler.sap.util.WriteToFile;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;
import transbit.tbits.searcher.DqlSearcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * @author: Mukesh Sharma
 * Date: 22/8/12
 * Time: 11:23 AM
 */
public class PushDataToFileJob {//implements ITBitsJob {

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

    public static final String BaName = "BaName";
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
        throw new IllegalArgumentException(msg);
    }

    public String getDisplayName() {
        return "SAP INTEGRATION JOB TO CREATE FILES";
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
        params.put(Constants.FTP_USERUSER, jdm.getString(Constants.FTP_USERUSER));
        params.put(Constants.FTP_PASSWORD, jdm.getString(Constants.FTP_PASSWORD));
        params.put(Constants.FTP_FILE_LOCATION, jdm.getString(Constants.FTP_FILE_LOCATION));
        params.put(Constants.FTP_HOSTHOST, jdm.getString(SapHost));

        //Write these properties in property file

        String path = Configuration.findAbsolutePath("tmp");
        PropertyObject propertyObject = new PropertyObject(params);

        propertyObject.setSapObjectFilePath(path + Constants.SAP_OBJECT_OUTPUT_FILE);
        propertyObject.setPropertyFilePath(path + Constants.PROP_OBJECT_OUTPUT_FILE);

        WriteToFile.writePropertyObject(propertyObject);

        //now gether other info
        params = new Hashtable<String, String>();
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

        process(params);
    }

    public void process(Hashtable<String, String> params) throws JobExecutionException {

        BusinessArea ba = null;
        try {
            ba = BusinessArea.lookupBySystemPrefix(params.get(BaName));
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        if (null == ba) {
            LOG.severe("Cannot find the BusinessArea with sysprefix : " + params.get(BaName) + ". So skipping the Escalations on this BusinessArea.");
            return;
        }

        DqlSearcher searcher = new DqlSearcher(ba.getSystemId(), params.get(Dql));

        try {
            searcher.search();
        } catch (Exception e1) {
            e1.printStackTrace();
            LOG.severe("Exception occurred while searching. The Escalations might not work correctly.");
        }

        ArrayList<Integer> reqIds = new ArrayList<Integer>();
        if (searcher.getResult().containsKey(ba.getSystemId())) {
            Collection<Integer> requestIdsFetchedColl = searcher.getResult().get(ba.getSystemId()).keySet();
            if (requestIdsFetchedColl != null) {
                reqIds.addAll(requestIdsFetchedColl);
            }
        }

        LOG.info("Following requests will be processed to push RFC and As Built files to SAP for ba=(" + ba.getSystemPrefix() + ") and dql=(" + params.get(Dql) + ")" + " : " + reqIds);

        ArrayList<Request> requestList = null;
        try {
            requestList = Request.lookupBySystemIdAndRequestIdList(ba.getSystemId(), reqIds);
        } catch (DatabaseException e) {
            LOG.info("Exception while retrieving requests.");
            e.printStackTrace();
        }

        if (null != requestList) {
            List<SapObject> sapObjectList = new ArrayList<SapObject>();
            for (Request request : requestList) {
                LOG.info("Processing request : " + ba.getSystemPrefix() + "#" + request.getRequestId() + "#" + request.getMaxActionId());
                if (null == request) {
                    LOG.info("The returned request was null.");
                    continue;
                }
                //check for RFC files and push documents if exists
                Collection<AttachmentInfo> exAttachments = (Collection<AttachmentInfo>) request.getObject(params.get(RFCField));
                if (null != exAttachments && exAttachments.size() > 0) {
                    pushRequestDocument(params, request, RfcFileType, sapObjectList);
                }

                //Check for As Built file and push to transbit.tbits.scheduler.sap if exists any
                exAttachments = (Collection<AttachmentInfo>) request.getObject(params.get(AsBuiltField));

                if (null != exAttachments && exAttachments.size() > 0) {
                    pushRequestDocument(params, request, AsBuiltFileType, sapObjectList);
                }

                WriteToFile.writeSapObjects(sapObjectList);
            }
        } else {
            LOG.debug("No request fount to be process");
        }

    }

    public void pushRequestDocument(Hashtable<String, String> params, Request currentRequest, String fileType, List<SapObject> sapObjectList) {

        String sapDocNumber = null;
        SapObject sapObject = new SapObject();

        try {

            Map<String, String> dataMap = new HashMap<String, String>();

            String authorityGroup = params.get(AuthorityGroup);
            if (null == authorityGroup) {
                throw new JobExecutionException("Could not find filed " + params.get(AuthorityGroup) + " in request with id " + currentRequest.getRequestId());
            }
            dataMap.put("AUTHORITYGROUP", authorityGroup);

            String description = currentRequest.get(params.get(Description)).toString();
            if (null == description) {
                throw new JobExecutionException("Could not find filed " + params.get(Description) + " in request with id " + currentRequest.getRequestId());
            }

            dataMap.put("DESCRIPTION", description);
            dataMap.put("DOCUMENTTYPE", params.get(DocumentType));
            dataMap.put("DOCUMENTPART", params.get(DocumentPart));
            dataMap.put("DOCUMENTVERSION", params.get(DocumentVersion));

            //add vendor number "Documents no" here.
            String edmsDocNumber = currentRequest.get(params.get(EdmsDocNumber)).toString();
            if (null == edmsDocNumber) {
                throw new JobExecutionException("Could not find filed " + params.get(EdmsDocNumber) + " in request with id " + currentRequest.getRequestId());
            }
            dataMap.put("EDMSDOCNO", edmsDocNumber);

            //TODO: check for file and set accordingly
            //check for "RFC" or "AS BUILT" and add accordingly
            if (fileType.equalsIgnoreCase(RfcFileType)) {
                sapObject.setRfc(true);
            } else {
                sapObject.setAsBuilt(true);
            }

            //add Revision no here
            String revision = currentRequest.get(params.get(RevisionField)).toString();
            if (null == revision) {
                throw new JobExecutionException("Could not find filed " + params.get(RevisionField) + " in request with id " + currentRequest.getRequestId());
            }
            dataMap.put("REVISION", revision);

            //add AREA here
            String area = currentRequest.get(params.get(AreaField)).toString();
            if (null == area) {
                throw new JobExecutionException("Could not find filed " + params.get(AreaField) + " in request with id " + currentRequest.getRequestId());
            }
            dataMap.put("AREA", area);

            //add Category_id or Package here
            String packageValue = currentRequest.get(params.get(PackageField));
            if (null == packageValue) {
                throw new JobExecutionException("Could not find filed " + params.get(packageValue) + " in request with id " + currentRequest.getRequestId());
            }
            dataMap.put("PACKAGE", packageValue);

            //add DISCIPLINE here
            String discipline = currentRequest.get(params.get(DisciplineField));
            if (null == discipline) {
                throw new JobExecutionException("Could not find filed " + params.get(discipline) + " in request with id " + currentRequest.getRequestId());
            }
            dataMap.put("DISCIPLINE", discipline);

            //add ORIGINATOR here
            String originator = currentRequest.get(params.get(OriginatorField));
            if (null == originator) {
                throw new JobExecutionException("Could not find filed " + params.get(OriginatorField) + " in request with id " + currentRequest.getRequestId());
            }
            dataMap.put("ORIGINATOR", originator);

            //add Category here
            String inspectionCategory = currentRequest.get(params.get(InspectionCategoryField));
            if (null == inspectionCategory) {
                throw new JobExecutionException("Could not find filed " + params.get(InspectionCategoryField) + " in request with id " + currentRequest.getRequestId());
            }
            dataMap.put("INSPECTIONCATEGORY", inspectionCategory);
            dataMap.put("STORAGECATEGORY", params.get(StorageCategory));

            sapObject.setMappingData(dataMap);

            Collection<AttachmentInfo> attachmentInfos = null;
            if (fileType.equalsIgnoreCase(RfcFileType)) {
                attachmentInfos = (Collection<AttachmentInfo>) currentRequest.getObject(params.get(RFCField));
            } else {
                attachmentInfos = (Collection<AttachmentInfo>) currentRequest.getObject(params.get(AsBuiltField));
            }

            if (null == attachmentInfos || attachmentInfos.size() == 0) {
                LOG.debug("No files to upload");
            }

            Map<String, String> fileMap = new HashMap<String, String>();
            for (AttachmentInfo attachmentInfo : attachmentInfos) {
                fileMap.put(APIUtil.getAttachmentLocation() + "/" + Uploader.getFileLocation(attachmentInfo.getRepoFileId()), attachmentInfo.getName());
            }

            if (fileType.equalsIgnoreCase(RfcFileType)) {
                sapObject.setRfcFiles(fileMap);
            } else {
                sapObject.setAsBuiltFiles(fileMap);
            }

            sapObjectList.add(sapObject);
        } catch (Exception e) {
            LOG.debug(e.getMessage());
        }
    }
}
