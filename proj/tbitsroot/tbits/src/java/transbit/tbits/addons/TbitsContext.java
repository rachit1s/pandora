/**
 * 
 */
package transbit.tbits.addons;

import java.util.HashMap;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * A concrete impl of Context interface which can be extended further
 */
public class TbitsContext implements Context
{
	HashMap<Object,Object> objectMap = new HashMap<Object,Object>();

	/* (non-Javadoc)
	 * @see com.tbitsglobal.addon.Context#get(java.lang.Object)
	 */
	@Override
	public Object get(Object key) {
		return this.objectMap.get(key);
	}

	/* (non-Javadoc)
	 * @see com.tbitsglobal.addon.Context#set(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void set(Object key, Object value) {
		this.objectMap.put(key, value);
	}
}
