package commons.com.tbitsGlobal.utils.client;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import commons.com.tbitsGlobal.utils.client.Events.KeyboardShortcutEvent;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class TbitsNativePreviewHandler implements NativePreviewHandler{

	public static final int baseASCII = 64;
	
	private static TbitsNativePreviewHandler handler;
	
	public static TbitsNativePreviewHandler getInstance(){
		if(handler == null)
			handler = new TbitsNativePreviewHandler();
		return handler;
	}
	
	private TbitsNativePreviewHandler() {
		
	}
	
	
	public void onPreviewNativeEvent(NativePreviewEvent event) {
		if((event.getTypeInt() & Event.ONKEYDOWN) != 0){
			NativeEvent nativeEvent = event.getNativeEvent();
			
			// We have eaten up all the CTRL + SHIFT + akey
			if(nativeEvent.getCtrlKey() && nativeEvent.getShiftKey()){
				Log.info("Key Code : " + nativeEvent.getKeyCode());
				TbitsEventRegister.getInstance().fireEvent(new KeyboardShortcutEvent(nativeEvent.getKeyCode(), nativeEvent));
				event.cancel();
			}
		}
	}
}
