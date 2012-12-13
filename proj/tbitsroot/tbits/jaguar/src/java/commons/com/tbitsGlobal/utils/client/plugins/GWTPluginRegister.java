package commons.com.tbitsGlobal.utils.client.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Client side register for GWT plugins. 
 * It is a singleton class.
 * 
 * There are slots --> {@link GWTPluginSlotAbstract}
 * There are plugins --> {@link IGWTPlugin}
 * 
 * For every slot there can be plugins of more that one kind.
 * 
 * @author sourabh
 *
 */
@SuppressWarnings("unchecked")
public class GWTPluginRegister {
	private HashMap<Class<? extends GWTPluginSlotAbstract>, ListStore<TbitsModelData>> 	pluginmap;
	
	private static GWTPluginRegister register;
	
	/**
	 * @return The instance of register.
	 */
	public static GWTPluginRegister getInstance(){
		if(register == null)
			register = new GWTPluginRegister();
		return register;
	}
	
	/**
	 * Contructor.
	 */
	private GWTPluginRegister() {
		pluginmap = new HashMap<Class<? extends GWTPluginSlotAbstract>, ListStore<TbitsModelData>>();
	}
	
	/**
	 * Add a plugin to the register.
	 * @param <T> A interface that extends {@link IGWTPlugin}
	 * @param slotClass. Plugin slot.
	 * @param pluginClazz. The T.
	 * @param plugin. An instance of the plugin.
	 */
	public <T extends IGWTPlugin> void addPlugin(Class<? extends GWTPluginSlotAbstract> slotClass,Class<T> pluginClazz, T plugin){
		if(!pluginmap.containsKey(slotClass)){
			ListStore<TbitsModelData> store = new ListStore<TbitsModelData>();
			pluginmap.put(slotClass, store);
		}
		ListStore<TbitsModelData> store = pluginmap.get(slotClass);
		
		TbitsModelData model = new TbitsModelData();
		model.set("pluginClazz", pluginClazz);
		model.set("plugin", plugin);
		
		store.add(model);
		
		Log.info("Plugin : " + plugin.getClass().getName() + " initialized at slot : " + slotClass.getName());
	}
	
	/**
	 * Gets plugins for specified slot and class.
	 * @param <T>
	 * @param slotClass
	 * @param pluginClass
	 * @return. The List of plugins.
	 */
	public <T extends IGWTPlugin> ArrayList<T> getPlugins(Class<? extends GWTPluginSlotAbstract> slotClass, Class<T> pluginClass){
		ListStore<TbitsModelData> store = pluginmap.get(slotClass);
		if(store != null){
			List<TbitsModelData> models = store.findModels("pluginClazz", pluginClass);
			if(models != null){
				ArrayList<T> plugins = new ArrayList<T>();
				for(TbitsModelData model : models){
					plugins.add((T) model.get("plugin"));
				}
				return plugins;
			}
		}
		return null;
	}	
}
