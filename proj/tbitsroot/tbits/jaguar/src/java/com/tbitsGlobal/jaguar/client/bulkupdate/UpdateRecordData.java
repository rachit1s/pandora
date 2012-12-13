package com.tbitsGlobal.jaguar.client.bulkupdate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;

/**
 * {@link Serializable} data carrier used for Bulk Operations.
 * 
 * @author sourabh
 *
 */
public class UpdateRecordData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private TbitsTreeRequestData updateModel;
	
	public UpdateRecordData() {
		updateModel = new TbitsTreeRequestData();
	}

	public UpdateRecordData(
			HashMap<String, List<AttachmentInfoClient>> updateFiles,
			TbitsTreeRequestData updateModel) {
		this();
		this.updateModel = updateModel;
	}

	public TbitsTreeRequestData getUpdateModel() {
		return updateModel;
	}

	public void setUpdateModel(TbitsTreeRequestData updateModel) {
		this.updateModel = updateModel;
	}
}
