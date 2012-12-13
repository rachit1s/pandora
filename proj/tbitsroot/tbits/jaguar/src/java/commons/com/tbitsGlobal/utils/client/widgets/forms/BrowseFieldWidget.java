package commons.com.tbitsGlobal.utils.client.widgets.forms;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.search.SearchWindow;
import commons.com.tbitsGlobal.utils.client.search.SearchWindow.ISubmitHandler;

public class BrowseFieldWidget extends ContentPanel{
	
	private TextField<String> textField;

	public BrowseFieldWidget() {
		super();
		
		this.setBodyBorder(false);
		this.setHeaderVisible(false);
		this.setLayout(new FitLayout());
		
		textField = new TextField<String>();
		
		this.add(textField, new FitData());
		
		this.addButton(new Button("Add", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				SearchWindow window = new SearchWindow();
				window.show();
				
				window.setSubmitHandler(new ISubmitHandler(){
					public void onSubmit(List<TbitsTreeRequestData> models) {
						if(models != null && models.size() > 0){
							String str = "";
							for(TbitsTreeRequestData model : models){
								int sysId = model.getSystemId();
								int requestId = model.getRequestId();
								
								BusinessAreaClient ba = ClientUtils.getBAbySysId(sysId);
								if(ba != null){
									if(!str.equals(""))
										str += ",";
									str += ba.getSystemPrefix() + "#" + requestId;
								}
							}
							String value = getStringValue();
							if(value == null)
								value = "";
							if(!value.equals("")){
								value += ",";
							}
							value += str;
							setStringValue(value);
						}
					}});
			}}));
	}

	public void setStringValue(String value) {
		this.textField.setValue(value);
	}

	public String getStringValue() {
		return this.textField.getValue();
	}
}
