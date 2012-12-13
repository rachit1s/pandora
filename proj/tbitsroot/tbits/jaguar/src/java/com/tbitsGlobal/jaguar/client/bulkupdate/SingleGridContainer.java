package com.tbitsGlobal.jaguar.client.bulkupdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractSingleBulkGridContainer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

/**
 * 
 * @author sourabh
 * 
 * Container for Individual Bulk grid
 */
public class SingleGridContainer extends AbstractSingleBulkGridContainer<TbitsTreeRequestData>{
	
	public SingleGridContainer(BulkUpdateGrid bulkGrid) {
		super(bulkGrid);
	}
	
	@Override
	protected void beforeAdd(TbitsTreeRequestData model) {
		super.beforeAdd(model);
		
		FieldCache cache = CacheRepository.getInstance().getCache(FieldCache.class);
		if(cache.isInitialized()){
			List<ColPrefs> prefs = ((BulkUpdateGrid)bulkGrid).getPrefs();
			if(prefs != null){
				for(ColPrefs pref : prefs){
					BAField baField = cache.getObject(pref.getName());
					if(baField != null && !baField.isSetEnabled()){
						model.remove(baField.getName());
					}
				}
			}
		}
	}

	public void showStatus(HashMap<Integer, TbitsTreeRequestData> statusMap){
		for(int rowNo : statusMap.keySet()){
			TbitsTreeRequestData model = statusMap.get(rowNo);
			TbitsExceptionClient e = model.getError();
			if(e != null){
				model.set(IBulkUpdateConstants.RESPONSE_STATUS, "Error : " + e.getMessage());
			}else{
				model.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
			}
			bulkGrid.getStore().remove(rowNo - 1);
			bulkGrid.getStore().insert(model, rowNo - 1);
		}
	}
	
	public void setColumnValue(String property, Object value){
		List<TbitsTreeRequestData> models = bulkGrid.getSelectionModel().getSelectedItems();
		if(models == null || models.size() == 0){
			models = bulkGrid.getStore().getModels();
		}
		
		for(TbitsTreeRequestData model : models){
			if(value instanceof POJO){
				model.set(property, ((POJO) value).clone());
			}else
				model.set(property, value);
			
			int index = bulkGrid.getStore().indexOf(model);
			bulkGrid.getTbitsGridView().refreshRow(index);
			
		}
	}
	
	/**
	 * Adds the given files to all the models in the bulkgrid
	 * @param field
	 * @param atts
	 */
	public void addFilesToAll(BAFieldAttachment field, List<FileClient> atts){
		String fieldName = field.getName();
		
		List<TbitsTreeRequestData> models = bulkGrid.getSelectionModel().getSelectedItems();
		if(models == null || models.size() == 0){
			models = bulkGrid.getStore().getModels();
		}
		
		for(TbitsTreeRequestData model : models){
			POJO obj = model.getAsPOJO(fieldName);
			
			if(obj == null){
				obj = new POJOAttachment(new ArrayList<FileClient>());
			}
			
			if(obj instanceof POJOAttachment){
				POJOAttachment o = (POJOAttachment) obj;
				if(o.getValue() == null)
					o.setValue(new ArrayList<FileClient>());
				List<FileClient> values = o.getValue();
				for(FileClient att : atts){
					if(att.getStatus().equals(FileClient.STATUS_UPLOADED)){
						if(values.contains(att))
							values.remove(att);
						values.add(att);
					}
				}
			}
		}
		
		bulkGrid.getView().refresh(false);
	}
	
	/**
	 * Updates the requests which have been added or updated
	 */
	public void updateModels(){
		List<TbitsTreeRequestData> gridModels = bulkGrid.getStore().getModels();
		for(TbitsTreeRequestData model : gridModels){
			Object o = model.get(IBulkUpdateConstants.RESPONSE_STATUS);
			if(o != null && o instanceof String){
				String statusString = (String) o;
				if(statusString.startsWith("Updated")){
					TbitsTreeRequestData oldModel = BulkUpdateConstants.models.findModel(REQUEST, model.getRequestId());
					if(oldModel != null)
						BulkUpdateConstants.models.remove(oldModel);
					
					BulkUpdateConstants.models.add(model.clone());
				}else if(statusString.startsWith("Added")){
					BulkUpdateConstants.models.add(model.clone());
				}
			}
		}
	}
	
	/**
	 * Calculates the changes in the {@link BulkUpdateGrid}.
	 * 
	 * @return Map of changes.
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	private HashMap<Integer, UpdateRecordData> calculateChanges(){
		HashMap<Integer, UpdateRecordData> updateData = new HashMap<Integer, UpdateRecordData>();
		
		List<TbitsTreeRequestData> oldValues = BulkUpdateConstants.models.getModels();
		for(TbitsTreeRequestData oldModel : oldValues){
			int requestId = oldModel.getRequestId();
			if(requestId == 0)
				continue;
			TbitsTreeRequestData newModel = this.bulkGrid.getStore().findModel(REQUEST, oldModel.get(REQUEST));
			if(newModel != null){
				UpdateRecordData updateRecord = null;
				for(String name : newModel.getPropertyNames()){
					POJO newObj = newModel.getAsPOJO(name);
					if(newObj != null && newObj.getValue() != null && !newObj.toString().equals("null")) {
						POJO oldObj = oldModel.getAsPOJO(name);
						if(oldObj == null || !newObj.equals(oldObj)){
							if(updateRecord == null)
								updateRecord = new UpdateRecordData();
							if(!updateData.containsKey(requestId))
								updateData.put(requestId, updateRecord);
							if(newObj instanceof POJOAttachment){
								TbitsTreeRequestData updateModel = updateRecord.getUpdateModel();
								List<FileClient> attachments = ((POJOAttachment)newObj).getValue();
								List<FileClient> files = new ArrayList<FileClient>();
								for(FileClient attachment : attachments){
									if(attachment.getStatus().equals(FileClient.STATUS_UPLOADED))
										files.add(attachment);
								}
								updateModel.set(name, new POJOAttachment(files));
							}else{
								TbitsTreeRequestData updateModel = updateRecord.getUpdateModel();
								updateModel.set(name, newObj);
							}
						}
					}
				}
				if(updateRecord != null){
					updateRecord.getUpdateModel().setRequestId(newModel.getRequestId());
					updateRecord.getUpdateModel().setMaxActionId(newModel.getMaxActionId());
				}
			}
		}
		
		return updateData;
	}
	
	/**
	 * Retrieves new records added to the {@link BulkUpdateGrid}.
	 * 
	 * @return List of {@link UpdateRecordData}
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	private ArrayList<UpdateRecordData> getNewRecords(){
		ArrayList<UpdateRecordData> addData = new ArrayList<UpdateRecordData>();
		
		List<TbitsTreeRequestData> models = bulkGrid.getStore().getModels();
		for(TbitsTreeRequestData model : models){
			if(model.getRequestId() == 0){
				UpdateRecordData addRecord = null;
				for(String name : model.getPropertyNames()){
					POJO obj = model.getAsPOJO(name);
					if(obj != null && obj.getValue() != null 
							&& obj.toString() != null && !obj.toString().equals("") && !obj.toString().equals("null")){
						if(addRecord == null)
							addRecord = new UpdateRecordData();
						if(obj instanceof POJOAttachment){
							TbitsTreeRequestData updateModel = addRecord.getUpdateModel();
							List<FileClient> attachments = ((POJOAttachment)obj).getValue();
							ArrayList<FileClient> files = new ArrayList<FileClient>();
							for(FileClient attachment : attachments){
								if(attachment.getStatus().equals(FileClient.STATUS_UPLOADED))
									files.add(attachment);
							}
							updateModel.set(name, new POJOAttachment(files));
						}else{
							TbitsTreeRequestData addModel = addRecord.getUpdateModel();
							addModel.set(name, obj);
						}
					}
				}
				if(addRecord != null)
					addData.add(addRecord);
			}
		}
		return addData;
	}
}
