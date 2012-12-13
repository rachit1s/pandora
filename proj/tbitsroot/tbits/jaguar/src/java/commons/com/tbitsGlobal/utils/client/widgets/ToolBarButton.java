package commons.com.tbitsGlobal.utils.client.widgets;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;

/**
 * 
 * @author sourabh
 *
 * Button to be showed on a tool bar. Displays the shim surface without hover
 */
public class ToolBarButton extends Button{
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	public ToolBarButton() {
		super();
		
		observable = new BaseTbitsObservable();
		observable.attach();
	}
	
	public ToolBarButton(String text){
		super(text);
		
		observable = new BaseTbitsObservable();
		observable.attach();
	}
	
	public ToolBarButton(String text, SelectionListener<ButtonEvent> listener){
		super(text, listener);
		
		observable = new BaseTbitsObservable();
		observable.attach();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		setStyleName("x-btn  x-btn-noicon x-btn-over");
		setStyleAttribute("marginLeft", "3px");
	}
	@Override
	protected void onMouseOut(ComponentEvent ce) {
		super.onMouseOut(ce);
		if(this.isEnabled())
			setStyleName("x-btn  x-btn-noicon x-btn-over");
	}
	
	@Override
	protected void onEnable() {
		super.onEnable();
		setStyleName("x-btn  x-btn-noicon x-btn-over");
	}
	
	public <T extends TbitsBaseEvent> void enableOn(Class<T> clazz){
		ITbitsEventHandle<T> handle = new ITbitsEventHandle<T>(){
			public void handleEvent(T event) {
				enable();
			}};
		observable.subscribe(clazz, handle);
	}
	
	public <T extends TbitsBaseEvent> void disableOn(Class<T> clazz){
		ITbitsEventHandle<T> handle = new ITbitsEventHandle<T>(){
			public void handleEvent(T event) {
				disable();
			}};
		observable.subscribe(clazz, handle);
	}
}
