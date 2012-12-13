package commons.com.tbitsGlobal.utils.client.requestFormInterfaces;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;

/**
 * 
 * @author sourabh
 * 
 * Interface to be extended by forms that intend to add/update request
 */
public interface IEditRequestForm extends IRequestForm 
{
	/**
	 * called when submit button is clicked or when form has to be submitted by other means
	 */
	public void onSubmit();
	
	/**
	 * Create {@link TbitsTreeRequestData} by retrieving values from {@link IFieldConfig}
	 * @return
	 */
	public TbitsTreeRequestData createRequestModel();
	
	/**
	 * Deletes the current draft
	 */
	public void deleteDraft();
	
	/**
	 * Saves the current state of form as draft
	 */
	public void saveDraft();
}
