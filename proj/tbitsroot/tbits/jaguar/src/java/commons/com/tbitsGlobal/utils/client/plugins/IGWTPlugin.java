package commons.com.tbitsGlobal.utils.client.plugins;

import com.google.gwt.user.client.ui.Widget;

/**
 * The interface for plugin.
 * 
 * @author sourabh
 *
 * @param <T> The type of widget the plugin returns.
 * @param <V> The type of parameter required for the widget.
 */
public interface IGWTPlugin<T extends Widget, V extends Object> {
	/**
	 * @param param
	 * @return The {@link Widget} to be added to the UI
	 */
	public T getWidget(V param);
	
	/**
	 * @param sysPrefix
	 * @return True if the plugin has to be executed for given BA
	 */
	boolean shouldExecute(String sysPrefix);
}
