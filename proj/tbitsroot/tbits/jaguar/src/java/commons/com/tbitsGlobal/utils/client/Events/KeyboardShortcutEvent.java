package commons.com.tbitsGlobal.utils.client.Events;

import com.google.gwt.dom.client.NativeEvent;


public class KeyboardShortcutEvent extends TbitsBaseEvent{
	private int keyCode;
	private NativeEvent nativeEvent;
	
	public KeyboardShortcutEvent(int keyCode, NativeEvent nativeEvent) {
		super();
		this.keyCode = keyCode;
		this.nativeEvent = nativeEvent;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public NativeEvent getNativeEvent() {
		return nativeEvent;
	}

	public void setNativeEvent(NativeEvent nativeEvent) {
		this.nativeEvent = nativeEvent;
	}
	
}
