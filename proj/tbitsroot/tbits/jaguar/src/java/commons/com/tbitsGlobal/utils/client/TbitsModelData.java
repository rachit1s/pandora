package commons.com.tbitsGlobal.utils.client;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.RpcMap;

/**
 * 
 * @author sourabh
 * 
 * An implementation of {@link BaseModelData} that can be extended to create new objects
 */
public class TbitsModelData extends BaseModelData implements Comparable<TbitsModelData>{
	private static final long serialVersionUID = 1L;
	
	public TbitsModelData() {
		super();
		
		map = new RpcMap();
		allowNestedValues = false;
	}
	
	public <T extends TbitsModelData> T clone(T model) {
		for(String property : this.getPropertyNames()){
			model.set(property, this.get(property));
		}
		return model;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		// TODO Auto-generated method stub
		return super.getProperties();
	}

	@Override
	public int compareTo(TbitsModelData o) {
		return 0;
	}
}
