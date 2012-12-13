package browse.com.tbitsglobal.browse.client;

import java.util.List;

import com.extjs.gxt.ui.client.util.Util;

public class BrowseUtils {
	
	public static List<String> getFieldNameListFromParams(Params params){
		Object obj = params.get(Params.FIELD_NAMES);
		if(obj != null){
			String fieldNamesString = (String) obj;
			String[] fieldNames = fieldNamesString.split(",");
			List<String> fieldNameList = Util.createList(fieldNames);
			
			return fieldNameList;
		}
		return null;
	}
}
