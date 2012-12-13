package transmittal.com.tbitsGlobal.client;

import java.util.*;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.tbitsGlobal.jaguar.client.cache.UserCache;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

public class WizardData {

	private HashMap<String, Object> hm;

	public WizardData() {
		hm = new HashMap<String, Object>();
	}

	public void setData(HashMap<String, Object> hm) {
		this.hm.putAll(hm);
		this.hm.putAll(getDataForPage2());
		this.hm.putAll(getDataForPage3());
	}

	@SuppressWarnings("unchecked")
	HashMap<String, String> getDataForPage2() {
		HashMap<String, String> valuesMap = new HashMap<String, String>();
		HashMap<Integer, TbitsTreeRequestData> requestMap = (HashMap<Integer, TbitsTreeRequestData>) hm
		.get("mapOfRequests");
		ArrayList<TbitsTreeRequestData> models =  new ArrayList<TbitsTreeRequestData>();
		for(TbitsTreeRequestData trd:requestMap.values()){
			models.add(trd);
		}
		BAField baField;
		
		ArrayList<TbitsModelData>listOfColumns= (ArrayList<TbitsModelData>) hm.get("attachmentTableColumnList");
		
		Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
			public int compare(TbitsModelData o1, TbitsModelData o2) {
				if ((o1 != null) && (o2 != null)) {
					int s1 = (Integer) o1
							.get(TransmittalConstants.COLUMN_ORDER);
					int s2 = (Integer) o2
							.get(TransmittalConstants.COLUMN_ORDER);
					if (s1 > s2)
						return 1;
					else if (s1 == s2)
						return 0;
					else if (s1 < s2)
						return -1;
				}
				return 0;
			}
		};
		// Sort the column info, before creating column configs out of them. So,
		// that they maintain the sort order and
		// hence the column order in the table.
		Collections.sort(listOfColumns, comp);

		ArrayList<String> baFieldNames = new ArrayList<String>();
		ArrayList<BAField> baFields = (ArrayList<BAField>) hm.get("BAFields");
		for (TbitsModelData md : listOfColumns) {
			int dataTypeId = (Integer) md
					.get(TransmittalConstants.DATA_TYPE_ID_COLUMN);
			if (dataTypeId != TransmittalConstants.ATTACHMENTS) {
				Integer fieldId = (Integer) md
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId, baFields);

					if (baField != null) {
						baFieldNames.add(baField.getName());
					}
				}
			}
		}

		String deliverableAttachmentProperties = "";
		for (TbitsModelData md : listOfColumns) {
			if (((Integer) md.get("data_type_id")) == TransmittalConstants.ATTACHMENTS&&((Boolean)md.get(TransmittalConstants.IS_ACTIVE_COLUMN))&&((Boolean)md.get(TransmittalConstants.IS_EDITABLE))) {
				
				Integer fieldId = (Integer) md
				.get(TransmittalConstants.FIELD_ID_COLUMN);
				 if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId, baFields);

			
				deliverableAttachmentProperties = ((deliverableAttachmentProperties
						.equals("")) ? (String) baField.get("name")
						: deliverableAttachmentProperties + ","
								+ (String) baField.get("name"));
			}
		}
		}
		JSONArray tableJson = getDrawingListsJsonString(models, baFieldNames);
		valuesMap.put(TransmittalConstants.DRAWING_TABLE_KEY_WORD, tableJson
				.toString());
		valuesMap.put(TransmittalConstants.DELIVERABLE_FIELD_NAME,
				deliverableAttachmentProperties);

		valuesMap.put("SelectedAttachmentsTable", getSelectedAttachmentslist()
				.toString());

		return valuesMap;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, String> getDataForPage3() {
		HashMap<String, String> valuesMap = new HashMap<String, String>();

		
		Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {		
			public int compare(TbitsModelData o1, TbitsModelData o2) {
				
				if ((o1 != null) && (o2 != null)){
					int s1 = (Integer)o1.get(TransmittalConstants.COLUMN_ORDER);
					int s2 = (Integer)o2.get(TransmittalConstants.COLUMN_ORDER);
					if (s1 > s2)
						return 1;
					else if (s1 == s2)
						return 0;
					else if (s1 < s2)
						return -1;
				}
				return 0;
			}
		};
		//Sort the column info, before creating column configs out of them. So, that they maintain the sort order and 
		//hence the column order in the table.
		ArrayList<TbitsModelData> distributionDataColumns = (ArrayList<TbitsModelData>) hm.get("distributionTableColumnsList");
		Collections.sort(distributionDataColumns, comp);
		
		UserCache userCache = CacheRepository.getInstance().getCache(
				UserCache.class);
		ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();
		
		TbitsModelData tmd = (TbitsModelData) hm.get("transmittalProcessParams");
		String cclist=tmd.get("ccList");
		HashSet<String> userSet = new HashSet<String>();
		for (String userLogin : cclist.trim().split(",")) {
			if (!userLogin.trim().equals("")) {
				userSet.add(userLogin);
			}
		}
		for (String userLogin1 : userSet) {
			if (!userLogin1.trim().equals("")) {
				UserClient uc = userCache.getObject(userLogin1);// getUserBasedOnLogin(userMap,
				// userLogin);
				if (uc != null) {
					TbitsModelData tempData2 = new TbitsModelData();
					for (TbitsModelData distColumnTmd : distributionDataColumns) {
						String property = (String) distColumnTmd
								.get("name");
						if (property != null) {
							// Check if its a user property if not,
							// fetch
							// default values from db.
							String value = (String) uc
									.get(property);
							if ((value != null)
									&& (!value.trim().equals("")))
								tempData2.set(property, value);
							else {
								String tempValue = distColumnTmd
										.get(TransmittalConstants.FIELD_CONFIG);
								if ((Integer) distColumnTmd
										.get("data_type_id") != 9) {
									if ((tempValue != null)
											&& (!tempValue.trim()
													.equals("")))
										tempData2.set(property,
												tempValue);
									else
										tempData2.set(property, "-");
								} else {
									HashMap<String, String> typesMap = new HashMap<String, String>();
									DistributionTableColumnsConfig
											.fetchKeyValuePairsfromJsonString(
													typesMap,
													tempValue);
									tempValue = typesMap.keySet()
											.iterator().next();
									String valueOfkeyValuePair=typesMap.get(tempValue);
									if (tempValue != null)
										tempData2.set(property,
												tempValue);
									else
										tempData2.set(property, "-");
								}
							}
						}
					}
					
					distributionListModelData.add(tempData2);
				}
			}
		}
		
		ArrayList<String> propertyList = new ArrayList<String>();
	

		for (TbitsModelData md : distributionDataColumns) {
			propertyList.add((String) md.get("name"));
		}

		try {
			JSONArray tableJson = getDistributionListJsonString(distributionListModelData,
					propertyList);
			valuesMap.put(TransmittalConstants.DISTRIBUTION_TABLE, tableJson
					.toString());
		} catch (JSONException je) {
			Window.alert(je.getMessage());
		}
		return valuesMap;
	}

	/**
	 * 
	 * @param isMergeAllAttachments
	 * @return
	 */
	private JSONArray getSelectedAttachmentslist() {

		JSONArray attachmentModel = new JSONArray();
		int count = 0;

		HashMap<Integer, TbitsTreeRequestData> map = (HashMap<Integer, TbitsTreeRequestData>) hm
				.get("mapOfRequests");
		ArrayList<Integer> arr = new ArrayList<Integer>();
		for(Integer i:map.keySet()){
			arr.add(i);
		}
			
		for (Integer requestId : arr) {
			TbitsTreeRequestData trd = map.get(requestId);

			JSONArray temp = setJsonFieldValue1(trd);
			attachmentModel.set(count, temp);
			count++;

		}
		return attachmentModel;
	}

	@SuppressWarnings("unchecked")
	private JSONArray setJsonFieldValue1(TbitsTreeRequestData trd) {
		BAField baField;
		JSONArray finalArr = new JSONArray();
		JSONArray tempArr1 = new JSONArray();
		JSONArray tempArr = new JSONArray();
		JSONArray tempArr2 = new JSONArray();
		finalArr.set(0, new JSONString(trd.getRequestId() + ""));
		finalArr.set(1, new JSONString((String) trd.get("subject")));
		int count1 = 2;
		ArrayList<BAField> baFields = (ArrayList<BAField>) hm.get("BAFields");
		ArrayList<TbitsModelData> arr = (ArrayList<TbitsModelData>) hm
				.get("attachmentTableColumnList");
		if (arr != null) {
			for (TbitsModelData tmd : arr) {
				int dataTypeId = (Integer) tmd.get("data_type_id");
				boolean isEditable = (Boolean) tmd.get("is_editable");
				Integer fieldId = (Integer) tmd
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {
					baField = Utils.getBAFieldById(fieldId, baFields);

					if (baField != null) {
						if (trd != null) {
							if (TransmittalConstants.ATTACHMENTS == baField
									.getDataTypeId()
									&& isEditable) {

								String property = baField.getName();
								tempArr = new JSONArray();
								tempArr.set(0, new JSONString(property));

								POJO obj = trd.getAsPOJO(property);

								if (obj == null
										|| !(obj instanceof POJOAttachment)) {
									obj = new POJOAttachment(
											new ArrayList<FileClient>());
								}

								List<FileClient> delAttachments = ((POJOAttachment) obj)
										.getValue();

								if (delAttachments.size() == 0) {
									tempArr.set(1, new JSONString(""));
									finalArr.set(count1, tempArr);
									++count1;
								} else {
									tempArr2 = new JSONArray();
									int count = 0;
									for (FileClient eachAttachMent : delAttachments) {
										tempArr1 = new JSONArray();
										tempArr1.set(0, new JSONString(
												eachAttachMent.getFileName()));
										tempArr1.set(1, new JSONString(
												eachAttachMent
														.getRequestFileId()
														+ ""));
										tempArr2.set(count, tempArr1);
										count++;
									}
									tempArr.set(1, tempArr2);
									finalArr.set(count1, tempArr);
									++count1;
								}

							}
						}
					}
				}
			}
		}

		return finalArr;

	}

	private JSONArray getDrawingListsJsonString(
			List<TbitsTreeRequestData> models, ArrayList<String> baFieldNames) {
		JSONArray tableJson = new JSONArray();
		int count = 0;
		ArrayList<BAField> baFields = (ArrayList<BAField>) hm.get("BAFields");
	
		for (TbitsTreeRequestData model : models) {
			int reqId = model.getRequestId();
			TbitsTreeRequestData trd = ((HashMap<Integer, TbitsTreeRequestData>) hm
					.get("mapOfRequests")).get(reqId);
			if (trd != null) {

				JSONArray drawingListValues = new JSONArray();
				for (int i = 0; i < baFieldNames.size(); i++) {
					String fValue = String
							.valueOf(trd.get(baFieldNames.get(i)));

					BAField baField = Utils.getBAFieldByname(baFieldNames
							.get(i), baFields);
					String field_id = baField.getFieldId() + "";
					if (baField instanceof BAFieldCombo) {
						fValue = getTypeValueBasedOnConfig(
								(BAFieldCombo) baField, fValue);
					}

					drawingListValues.set(i + 1, new JSONString(field_id + ","
							+ fValue));
				}
				drawingListValues.set(0, new JSONString(model.getRequestId()
						+ ""));
				tableJson.set(count, drawingListValues);
				count++;
			}
		}
		return tableJson;
	}

	protected String getTypeValueBasedOnConfig(BAFieldCombo baField,
			String currentTypeName) {
		String fValue = currentTypeName;
		int typeValueSrc = 0;
		ArrayList<TbitsModelData> arr = (ArrayList<TbitsModelData>) hm
				.get("attachmentTableColumnList");
		for (TbitsModelData md : arr) {
			Integer fieldId = (Integer) md
					.get(TransmittalConstants.FIELD_ID_COLUMN);
			if (fieldId == baField.getFieldId()) {
				typeValueSrc = (Integer) md
						.get(TransmittalConstants.TYPE_VALUE_SOURCE_COLUMN);
				if (typeValueSrc < 2)
					return fValue;
				break;
			}
		}

		List<TypeClient> types = ((BAFieldCombo) baField).getTypes();
		for (TypeClient type : types) {
			String typeName = type.getName();
			if (typeName.equals(fValue)) {
				switch (typeValueSrc) {
				case 0:
				case 1:
					fValue = type.getName();
					break;
				case 2:
					fValue = type.getDisplayName();
					break;
				case 3:
					fValue = type.getDescription();
					break;
				}
				return fValue;
			}
		}
		return fValue;
	}

	private JSONArray getDistributionListJsonString(
			List<TbitsModelData> models, ArrayList<String> fieldNames)
			throws JSONException {
		JSONArray tableJson = new JSONArray();
		int count = 0;
		for (TbitsModelData model : models) {
			JSONArray drawingListValues = new JSONArray();
			for (int i = 0; i < fieldNames.size(); i++) {
				String fValue = String.valueOf(model.get(fieldNames.get(i)));
				String prop = fieldNames.get(i);
				fValue = prop + "," + fValue;
				drawingListValues.set(i + 1, new JSONString(fValue));
			}
			tableJson.set(count, drawingListValues);
			count++;
		}
		return tableJson;
	}

	public HashMap<String, Object> getData() {
		return hm;

	}

}
