package commons.com.tbitsGlobal.utils.client.Events;

import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister.IContextHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister.IPostFireHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister.IPreFireHandle;

public class DefaultContextHandle implements IContextHandle{

	private HashMap<Class<? extends TbitsBaseEvent>, IPreFireHandle<? extends TbitsBaseEvent>> preFireHandleMap;
	private HashMap<Class<? extends TbitsBaseEvent>, IPostFireHandle<? extends TbitsBaseEvent>> postFireHandleMap;
	
	public DefaultContextHandle() {
		preFireHandleMap = new HashMap<Class<? extends TbitsBaseEvent>, IPreFireHandle<? extends TbitsBaseEvent>>();
		postFireHandleMap = new HashMap<Class<? extends TbitsBaseEvent>, IPostFireHandle<? extends TbitsBaseEvent>>();
	}
	
	public <T extends TbitsBaseEvent> void setPreFireHandle(Class<T> eventClazz, IPreFireHandle<T> handle){
		preFireHandleMap.put(eventClazz, handle);
	}
	
	public <T extends TbitsBaseEvent> void setPostFireHandle(Class<T> eventClazz, IPostFireHandle<T> handle){
		postFireHandleMap.put(eventClazz, handle);
	}
	
	public <T extends TbitsBaseEvent> IPostFireHandle getPostFireHandle(Class<T> eventClazz) {
		return postFireHandleMap.get(eventClazz);
	}

	public <T extends TbitsBaseEvent> IPreFireHandle getPreFireHandle(Class<T> eventClazz) {
		// TODO Auto-generated method stub
		return preFireHandleMap.get(eventClazz);
	}

}
