package commons.com.tbitsGlobal.utils.client.requestFormInterfaces;

import com.extjs.gxt.ui.client.store.ListStore;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;

public interface IRequestFormData {
	public static String CONTEXT_MODEL			=	"model";
	public static String CONTEXT_DRAFT			=	"draft";
	public static String CONTEXT_REQUEST_DATA	=	"request_data";
	
	/**
	 * @return The sysprefix
	 */
	public String getSysPrefix();
	
	/**
	 * @return The list of {@link BAField}s that are used to create the form
	 */
	public ListStore<BAField> getBAFields();
	
	/**
	 * @return The {@link TbitsTreeRequestData} which has been used to fill the form
	 */
	public TbitsTreeRequestData getRequestModel();
	
	public void setRequestModel(TbitsTreeRequestData requestModel);
	
	/**
	 * @return The Display Groups in the form
	 */
	public ListStore<DisplayGroupClient> getDisplayGroups();
}
