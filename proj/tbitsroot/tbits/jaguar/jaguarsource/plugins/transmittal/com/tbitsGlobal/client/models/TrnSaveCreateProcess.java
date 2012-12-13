package transmittal.com.tbitsGlobal.client.models;

import java.util.HashMap;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

/**
 * Pojo used for saving the values for a new Transmittal process created from the wizard.
 * All the values are populated in a single data structure and sent to db.
 * @author devashish
 *
 */
public class TrnSaveCreateProcess extends TbitsModelData {
	
	public static String VALUES_PAGE_1	= "valuesPage1";
	public static String VALUES_PAGE_2 	= "valuesPage2";
	public static String VALUES_PAGE_3	= "valuesPage3";
	public static String VALUES_PAGE_4	= "valuesPage4";
	public static String VALUES_PAGE_5	= "valuesPage5";
	public static String VALUES_PAGE_6	= "valuesPage6";
	public static String VALUES_PAGE_7	= "valuesPage7";
	public static String VALUES_PAGE_8	= "valuesPage8";
	
	public TrnSaveCreateProcess(){}
	
	//--------------getter/setter for page1 values---------------//
	public HashMap<String, String> getValuesPage1(){
		return (HashMap<String, String>) this.get(VALUES_PAGE_1);
	}
	
	public void setValuesPage1(HashMap<String, String> valuesPage1){
		this.set(VALUES_PAGE_1, valuesPage1);
	}
	
	//--------------getter/setter for page2 values---------------//
	public List<TrnProcessParam> getValuesPage2(){
		return (List<TrnProcessParam>) this.get(VALUES_PAGE_2);
	}
	
	public void setValuesPage2(List<TrnProcessParam> valuesPage2){
		this.set(VALUES_PAGE_2, valuesPage2);
	}
	
	//--------------getter/setter for page3 values---------------//
	public List<TrnPostProcessValue> getValuesPage3(){
		return (List<TrnPostProcessValue>) this.get(VALUES_PAGE_3);
	}
	
	public void setValuesPage3(List<TrnPostProcessValue> valuesPage3){
		this.set(VALUES_PAGE_3, valuesPage3);
	}
	
	//--------------getter/setter for page4 values---------------//
	public List<TrnFieldMapping> getValuesPage4(){
		return (List<TrnFieldMapping>) this.get(VALUES_PAGE_4);
	}
	
	public void setValuesPage4(List<TrnFieldMapping> valuesPage4){
		this.set(VALUES_PAGE_4, valuesPage4);
	}
	
	//---------------getter/setter for page5 values---------------//
	public List<TrnAttachmentList> getValuesPage5(){
		return (List<TrnAttachmentList>) this.get(VALUES_PAGE_5);
	}
	
	public void setValuesPage5(List<TrnAttachmentList> valuesPage5){
		this.set(VALUES_PAGE_5, valuesPage5);
	}
	
	//---------------getter/setter for page6 values----------------//
	public List<TrnDistList> getValuesPage6(){
		return (List<TrnDistList>) this.get(VALUES_PAGE_6);
	}
	
	public void setValuesPage6(List<TrnDistList> valuesPage6){
		this.set(VALUES_PAGE_6, valuesPage6);
	}
	
	//----------------getter/setter for page 7 values--------------//
	public List<TrnDrawingNumber> getValuesPage7(){
		return (List<TrnDrawingNumber>) this.get(VALUES_PAGE_7);
	}
	
	public void setValuesPage7(List<TrnDrawingNumber> valuesPage7){
		this.set(VALUES_PAGE_7, valuesPage7);
	}
	
	//-----------------getter/setter for page 8 values-------------//
	public List<TrnValidationRule> getValuesPage8(){
		return (List<TrnValidationRule>) this.get(VALUES_PAGE_8);
	}
	
	public void setValuesPage8(List<TrnValidationRule> valuesPage8){
		this.set(VALUES_PAGE_8,  valuesPage8);
	}
}
