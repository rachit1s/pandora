package com.tbitsGlobal.jaguar.client.bulkupdate;


import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.core.client.GWT;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;

/**
 * 
 * @author sourabh
 * 
 * For all the static constants required for the module
 *
 */
public class BulkUpdateConstants {
	
	public static ListStore<TbitsTreeRequestData> models = new ListStore<TbitsTreeRequestData>();
	
	public static POJO clipboard;
	
	public static final BulkUpdateServiceAsync bulkUpdateService = GWT.create(BulkUpdateService.class);
}
