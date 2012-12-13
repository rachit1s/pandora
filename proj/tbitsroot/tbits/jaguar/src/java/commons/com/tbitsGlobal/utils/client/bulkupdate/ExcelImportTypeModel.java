package commons.com.tbitsGlobal.utils.client.bulkupdate;

import com.extjs.gxt.ui.client.util.Format;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;

/**
 * @author sourabh
 *
 * Enum to carry data types supported by excel import in Bulk Gridss
 */
public class ExcelImportTypeModel extends TbitsModelData{
	
	public enum ExcelImportDataType{
		
		Text("Text"),
		Boolean("Boolean"),
		Number("Number"),
		Date("Date"),
		Other("Other");
		
		private String displayName;
		
		private ExcelImportDataType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
	
	public static String ID				= "id";
	public static String DISPLAY_NAME 	= "display_name";
	public static String DATA_TYPE		= "data_type";
	
	public String getID() {
		return (String) this.get(ID);
	}

	public void setID(String myID) {
		this.set(ID, myID);
	}
	
	public String getDisplayName() {
		return Format.htmlEncode((String) this.get(DISPLAY_NAME));
	}

	public void setDisplayName(String myDisplayName) {
		this.set(DISPLAY_NAME, myDisplayName);
	}
	
	public ExcelImportDataType getDataType(){
		return (ExcelImportDataType)this.get(DATA_TYPE);
	}
	
	public void setDataType(ExcelImportDataType dataType){
		this.set(DATA_TYPE, dataType);
	}
}