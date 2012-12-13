package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import transbit.tbits.domain.BusinessArea;
import transmittal.com.tbitsGlobal.client.models.TrnEditableColumns;

import com.google.gwt.core.client.GWT;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class TransmittalWizardConstants {

	private static TransmittalWizardConstants st = null;

	private TransmittalWizardConstants() {
		editableColumns = new ArrayList<TrnEditableColumns>();
		transmittalProcessParams = new TbitsModelData();
		attachmentTableColumnsList = new ArrayList<TbitsModelData>();
		requests = new HashMap<Integer, TbitsTreeRequestData>();
		requestList = new ArrayList<TbitsTreeRequestData>();
		distributionTableColumnsList = new ArrayList<TbitsModelData>();
		baFieldList = new ArrayList<BAField>();
		trnWizardExtendedFields = new ArrayList<TbitsModelData>();
		editableAttachmentColumns = new ArrayList<TrnEditableColumns>();
		currentBA = new BusinessAreaClient();
		trnDistributionData = new ArrayList<String[]>();
		dataOfPage1 = new HashMap<String, String>();
		dataOfPage2 = new HashMap<String, String>();
		dataforAbruptFinish = new HashMap<String, String>();
	}
	
	public HashMap<String, String> getDataforAbruptFinish() {
		return dataforAbruptFinish;
	}

	public void setDataforAbruptFinish(HashMap<String, String> dataforAbruptFinish) {
		this.dataforAbruptFinish = dataforAbruptFinish;
	}

	public Boolean getFlagForFirstPage2AbruptFinish() {
		return flagForFirstPage2AbruptFinish;
	}

	public void setFlagForFirstPage2AbruptFinish(
			Boolean flagForFirstPage2AbruptFinish) {
		this.flagForFirstPage2AbruptFinish = flagForFirstPage2AbruptFinish;
	}

	public Boolean getnewVersion() {
		return newVersion;
	}

	public void setnewVersion(Boolean version) {
		this.newVersion = version;
	}

	public static TransmittalWizardConstants getInstance() {
		if (st == null) {
			st = new TransmittalWizardConstants();
		}
		return st;
	}

	public BusinessAreaClient getcurrentBaClient() {
		return currentBA;
	}

	public void setcurrentBaClient(BusinessAreaClient businessAreaClient) {
		this.currentBA = businessAreaClient;
	}

	public HashMap<Integer, TbitsTreeRequestData> getRequests() {
		return requests;
	}

	public void setRequests(HashMap<Integer, TbitsTreeRequestData> requests) {
		this.requests = requests;
	}

	public ArrayList<TrnEditableColumns> getEditableCoumns() {
		return this.editableColumns;
	}

	public void setEditableColumns(ArrayList<TrnEditableColumns> editableCoumns) {
		this.editableColumns = editableCoumns;
	}

	public List<TbitsTreeRequestData> getRequestList() {
		return requestList;
	}

	public void setRequestList(List<TbitsTreeRequestData> requestList) {
		this.requestList = requestList;
	}

	public TbitsModelData getTransmittalProcessParams() {
		return transmittalProcessParams;
	}

	public void setTransmittalProcessParams(
			TbitsModelData transmittalProcessParams) {
		this.transmittalProcessParams = transmittalProcessParams;
	}

	public ArrayList<TbitsModelData> getAttachmentTableColumnsList() {
		return attachmentTableColumnsList;
	}

	public void setAttachmentTableColumnsList(
			ArrayList<TbitsModelData> attachmentTableColumnsList) {
		this.attachmentTableColumnsList = attachmentTableColumnsList;
	}

	public ArrayList<TbitsModelData> getDistributionTableColumnsList() {
		return distributionTableColumnsList;
	}

	public void setDistributionTableColumnsList(
			ArrayList<TbitsModelData> arrayList) {
		this.distributionTableColumnsList = arrayList;
	}

	public boolean getInApprovalCycle() {
		return inApprovalCycle;
	}

	public void setInApprovalCycle(boolean inApprovalCycle) {
		this.inApprovalCycle = inApprovalCycle;
	}

	public ArrayList<BAField> getBaFieldList() {
		return baFieldList;
	}

	public void setBaFieldList(ArrayList<BAField> baFieldList) {
		this.baFieldList = baFieldList;
	}

	public List<String[]> getTrnDistributionData() {
		return trnDistributionData;
	}

	public void setTrnDistributionData(
			List<String[]> trnDistributionData) {
		this.trnDistributionData = trnDistributionData;
	}

	public ArrayList<TbitsModelData> getTrnwizardextendedfields() {
		return trnWizardExtendedFields;
	}

	public void setTrnWizardExtendedFields(
			ArrayList<TbitsModelData> trnWizardExtendedFields) {
		this.trnWizardExtendedFields = trnWizardExtendedFields;

	}
	public ArrayList<TrnEditableColumns> getEditableAttchmentColumns() {
		return editableAttachmentColumns;
	}

	public void setEditableAttchmentColumns(
			ArrayList<TrnEditableColumns> editableAttchmentColumns) {
		this.editableAttachmentColumns = editableAttchmentColumns;
	}
   public Boolean getFlagForFirstPage1AbruptFinish() {
		return flagForFirstPage1AbruptFinish;
	}

	public void setFlagForFirstPage1AbruptFinish(
			Boolean flagForFirstPage1AbruptFinish) {
		this.flagForFirstPage1AbruptFinish = flagForFirstPage1AbruptFinish;
	}
	public HashMap<String, String> getDataOfPage1() {
		return dataOfPage1;
	}

	public void setDataOfPage1(HashMap<String, String> dataOfPage1) {
		this.dataOfPage1 = dataOfPage1;
	}

	public HashMap<String, String> getDataOfPage2() {
		return dataOfPage2;
	}

	public void setDataOfPage2(HashMap<String, String> dataOfPage2) {
		this.dataOfPage2 = dataOfPage2;
	}

	List<TbitsTreeRequestData> requestList;

	FieldCache fieldCache = CacheRepository.getInstance().getCache(
			FieldCache.class);

	TbitsModelData transmittalProcessParams;

	ArrayList<TbitsModelData> attachmentTableColumnsList;

	ArrayList<TbitsModelData> distributionTableColumnsList;

	ArrayList<BAField> baFieldList;
	ArrayList<TbitsModelData> trnWizardExtendedFields;

	List<String[]>trnDistributionData;

	public Boolean inApprovalCycle;
	private BusinessAreaClient currentBA;
    Boolean newVersion;//whether transient data is of old or new type
    Boolean flagForFirstPage1AbruptFinish=false;
 	Boolean flagForFirstPage2AbruptFinish=false;
	HashMap<Integer, TbitsTreeRequestData> requests;
	ArrayList<TrnEditableColumns> editableColumns;
	ArrayList<TrnEditableColumns> editableAttachmentColumns;
	HashMap<String,String>dataOfPage1;
	HashMap<String,String>dataOfPage2;
	HashMap<String,String>dataforAbruptFinish;
}
