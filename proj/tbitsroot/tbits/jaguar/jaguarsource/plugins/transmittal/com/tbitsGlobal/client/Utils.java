package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.Window;

import transbit.tbits.exception.TBitsException;
import transmittal.com.tbitsGlobal.client.models.AttachmentModel;
import transmittal.com.tbitsGlobal.client.models.Attachmentinfo;
import transmittal.com.tbitsGlobal.client.models.DrawinglistModel;
import transmittal.com.tbitsGlobal.client.models.TrnEditableColumns;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

public class Utils {

	WizardData data;

	public WizardData getDataObject() {
		return data;
	}

	public void setDataObject(WizardData data) {
		this.data = data;
	}

	Utils(WizardData data) {
		this.data = data;
	}

	/**
	 * method to get the BA field object corresponding to a field_id in that
	 * particular business area,method takes the arraylist of the fields of that
	 * BA as the parameter and checks the parameter field_id in the list
	 */
	public static BAField getBAFieldById(int fieldId,
			ArrayList<BAField> fieldList) {
		BAField baField = null;

		if (fieldList != null) {

			for (BAField tField : fieldList) {
				if ((tField != null) && (tField.getFieldId() == fieldId)) {
					baField = tField;
					break;
				}
			}
		}

		return baField;
	}

	public static BAField getBAFieldByname(String name,
			ArrayList<BAField> fieldList) {
		BAField baField = null;

		if (fieldList != null) {

			for (BAField tField : fieldList) {
				if ((tField != null) && (tField.getName() == name)) {
					baField = tField;
					break;
				}
			}
		}

		return baField;
	}

	@SuppressWarnings("unchecked")
	public void updateRequestListWithDefaultVlaues() {

		HashMap<Integer, TbitsTreeRequestData> requests = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
				.getData().get("mapOfRequests");

		ArrayList<TbitsModelData> attachmentSelectionColumns = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList");

		ArrayList<BAField> BAFieldList = (ArrayList<BAField>) getDataObject()
				.getData().get("BAFields");

		for (TbitsTreeRequestData trd : requests.values()) {
			for (TbitsModelData tmd : attachmentSelectionColumns) {
				Integer fieldId = (Integer) tmd
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {
					int dataTypeId = (Integer) tmd
							.get(AttachmentSelectionPage.DATA_TYPE_ID);
					boolean isEditable = (Boolean) tmd
							.get(AttachmentSelectionPage.IS_EDITABLE);
					String defValue = (String) tmd
							.get(AttachmentSelectionPage.DEFAULT_VALUE);

					BAField baField;

					baField = Utils.getBAFieldById(fieldId, BAFieldList);

					if (isEditable && (baField != null)) {
						String property = baField.getName();
						if (dataTypeId != TransmittalConstants.ATTACHMENTS) {

							if (dataTypeId == TransmittalConstants.TYPE) {
								if ((defValue != null)
										&& (!defValue.trim().equals(""))) {
									TypeClient typeClient = ((BAFieldCombo) baField)
											.getModelForName(defValue);
									if (typeClient != null)
										trd.set(property, defValue);
									else
										Window
												.alert("No defualt type found for field: "
														+ baField
																.getDisplayName());
								}
							} else {
								if ((defValue == null)
										|| (defValue.trim().equals("")))
									defValue = "-";
								trd.set(property, defValue);
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void updateDrawingListOfRequest(
			HashMap<String, ArrayList<DrawinglistModel>> hm_drawing) {

		ArrayList<TrnEditableColumns> EditableColumnList = (ArrayList<TrnEditableColumns>) getDataObject()
				.getData().get("editableColumnsList");

		HashMap<Integer, TbitsTreeRequestData> requests = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
				.getData().get("mapOfRequests");

		for (Object reqId : hm_drawing.keySet()) {
			String reqID = (String) reqId;
			int reqid = Integer.parseInt(reqID);
			ArrayList<DrawinglistModel> arr = hm_drawing.get(reqId);

			for (DrawinglistModel eachfield : arr) {
				String value = eachfield.getFIELD_VALUE();
				int field_id = Integer.parseInt((String) eachfield
						.getFIELD_ID());

				for (TrnEditableColumns eachColumn : EditableColumnList) {
					if (eachColumn.getFIELDID() == field_id) {
						String property = eachColumn.getPROPERTY();
						requests.get(reqid).set(property, value);
					}
				}

			}

		}

	}

	@SuppressWarnings("unchecked")
	public void updateAttchmentListOfRequest(
			ArrayList<AttachmentModel> attchementList)
			throws TbitsExceptionClient {

		ArrayList<BAField> BAFieldList = (ArrayList<BAField>) getDataObject()
				.getData().get("BAFields");

		ArrayList<TbitsModelData> attachmentSelectionColumns = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList");

		HashMap<Integer, TbitsTreeRequestData> requests = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
		.getData().get("mapOfRequests");

		
		List<FileClient> newAttachemnts = null;

		String errorString = "";

		ArrayList<String> DeliverableFieldsNames = new ArrayList<String>();
		BAField baField;
		String deliverableAttachmentProperties = "";
		for (TbitsModelData md : attachmentSelectionColumns) {
			if (((Integer) md.get("data_type_id")) == TransmittalConstants.ATTACHMENTS
					&& ((Boolean) md.get(TransmittalConstants.IS_ACTIVE_COLUMN))) {

				Integer fieldId = (Integer) md
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId, BAFieldList);

					DeliverableFieldsNames.add(baField.getName());
					deliverableAttachmentProperties = ((deliverableAttachmentProperties
							.equals("")) ? (String) baField.get("name")
							: deliverableAttachmentProperties + ","
									+ (String) baField.get("name"));
				}
			}
		}

	
	
		for (TbitsTreeRequestData trd : requests.values()) {
			if (attchementList.size() > 0) {
				for (AttachmentModel eachRow : attchementList) {
					if (((Integer) trd.getRequestId()).toString().equals(
							eachRow.getREQUEST_ID())) {
						HashMap<String, List<Attachmentinfo>> hm = eachRow
								.getAttachmentDetails();
						for (String eachProperty : hm.keySet()) {
							POJO obj = trd.getAsPOJO(eachProperty);
							if (obj == null || !(obj instanceof POJOAttachment)) {
								obj = new POJOAttachment(
										new ArrayList<FileClient>());
							}
							List<FileClient> delAttachments = (List<FileClient>) obj
									.getValue();

							List<Attachmentinfo> attachments = hm
									.get(eachProperty);
							newAttachemnts = new ArrayList<FileClient>();
							for (Attachmentinfo attachment : attachments) {
								String filename = attachment.getFILE_NAME();
								int requestFileId = attachment
										.getREQUEST_FILE_ID();

								Boolean idDocumentPresent = false;
								for (FileClient eachAttachment : delAttachments) {

									if ((eachAttachment.getRequestFileId() == requestFileId)
											&& (eachAttachment.getFileName()
													.equals(filename))) {
										eachAttachment.set(
												"IS_CHECKED_IN_TRANSIENT_DATA",
												String.valueOf(true));
										idDocumentPresent = true;
										break;

									}

								}
								if (!idDocumentPresent) {
									errorString = errorString
											+ filename
											+ "Document is not present anymore for Request having requestId "
											+ trd.getRequestId() + "" + "\n";
									// Window.alert("One or more selected documents  are not present anymore");

								}
							}
							for (FileClient eachAttachment : delAttachments) {
								if (eachAttachment
										.get("IS_CHECKED_IN_TRANSIENT_DATA") == null) {
									eachAttachment.set(
											"IS_CHECKED_IN_TRANSIENT_DATA",
											String.valueOf(false));
								}
							}
							newAttachemnts.addAll(delAttachments);
							trd.set(eachProperty, newAttachemnts);

						}

					}
				}
			}
			
		}

		if (!errorString.equals("")) {
			errorString = errorString
					+ "Please Consider using the wizard for transmittal" + "\n";
			Window.alert(errorString);
			throw new TbitsExceptionClient();
		}
		
		for (TbitsTreeRequestData trd : requests.values()) 
		{
			
			for(String attFieldName:DeliverableFieldsNames)
			{
				POJO obj = trd.getAsPOJO(attFieldName);
				if (obj == null	|| !(obj instanceof POJOAttachment)) 
				{
					
					obj = new POJOAttachment(
							new ArrayList<FileClient>());
				}
				
				List<FileClient> delAttachments = (List<FileClient>) obj
				.getValue();
				List<FileClient> freshAttachments = new ArrayList<FileClient>();
				if(delAttachments.size()==0)
				{
					continue;
				}
				else
				{
					for(FileClient fc:delAttachments)
					{
						if(Boolean.valueOf((String)fc.get("IS_CHECKED_IN_TRANSIENT_DATA")))
						{
							freshAttachments.add(fc);
						}
						
					}
				}
				
				trd.set(attFieldName, freshAttachments);
			}
		}		
		
		
		


	}
}
