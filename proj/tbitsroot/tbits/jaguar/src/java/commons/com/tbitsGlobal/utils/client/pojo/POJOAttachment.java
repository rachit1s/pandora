package commons.com.tbitsGlobal.utils.client.pojo;

import java.util.ArrayList;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

/**
 * 
 * @author sourabh
 * 
 * POJO to carry List of files
 */
public class POJOAttachment extends POJO<List<FileClient>> {

	public POJOAttachment() {
		super();
	}
	
	public POJOAttachment(List<FileClient> value) {
		super(value);
	}

	@Override
	public POJO<List<FileClient>> clone() {
		List<FileClient> attVal = new ArrayList<FileClient>();
		attVal.addAll(this.value);
		return new POJOAttachment(attVal);
	}

	@Override
	public String toString() {
		List<FileClient> atts = this.getValue();
		if(atts != null){
			String str = "";
			for(FileClient att : atts){
				str += att.toString() + "<br />";
			}
			return str;
		}
		return "";
	}
	
	public void addFile(FileClient att){
		this.value.add(att);
	}

	@Override
	public int compareTo(POJO<List<FileClient>> o) {
		if(o.equals(this))
			return 0;
		return 1;
	}
}
