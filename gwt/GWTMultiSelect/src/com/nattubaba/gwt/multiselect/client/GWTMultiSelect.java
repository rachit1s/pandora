package com.nattubaba.gwt.multiselect.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;

public class GWTMultiSelect implements EntryPoint {
	ListBox comboBox1 = new ListBox(true);

	public void onModuleLoad() {
    
//    comboBox1.setVisibleItemCount(10);
////    list.setMultipleSelect(true);
//
//    comboBox1.addItem("A");
//    comboBox1.addItem("B");
//    comboBox1.addItem("C");
//    comboBox1.addItem("D");
//    
//    comboBox1.addChangeHandler(new ChangeHandler() {
//		
//		@Override
//		public void onChange(ChangeEvent event) {
//			int i = ((ListBox)event.getSource()).getSelectedIndex();
//			System.out.println("Item selected : " + i);
//		}
//	}) ;
//    
//    ComboBox<ComboBoxModelData> comboBox2 = new ComboBox<ComboBoxModelData>();
//   
//    ComboBoxModelData a = new ComboBoxModelData("a", "A");
//    ComboBoxModelData b = new ComboBoxModelData("b", "B");
//    ComboBoxModelData c = new ComboBoxModelData("c", "C");
//    ComboBoxModelData d = new ComboBoxModelData("d", "D");
// 
//    ListStore<ComboBoxModelData> listStore = new ListStore<ComboBoxModelData>();
//    listStore.add(a);
//    listStore.add(b);
//    listStore.add(c);
//    listStore.add(d);
//    comboBox2.setFieldLabel("Select Letter");  
//    comboBox2.setStore(listStore);
//    comboBox2.setEmptyText("Select...");
//    comboBox2.setWidth(150);
//    comboBox2.setTypeAhead(true);
//    comboBox2.setTriggerAction(TriggerAction.ALL);
//    comboBox2.setTemplate(getTemplateString(ComboBoxModelData.DISPLAY_NAME, ComboBoxModelData.NAME));
//    comboBox2.setValueField("name");
//    
//    SimpleComboBox<String> simpleComboBox = new SimpleComboBox<String>();
//    simpleComboBox.add(Arrays.asList( new String[]{ "X","Y","Z" }));
//    simpleComboBox.setTitle("SimpleComboBox");
//    RootPanel.get("comboBox1").add(comboBox1);
//    RootPanel.get("comboBox2").add(comboBox2);
//    RootPanel.get("comboBox3").add(simpleComboBox);
    
    List<MultiSelectModelData> mdList = new ArrayList<MultiSelectModelData>();
    mdList.add( new MultiSelectModelData("P"));
    mdList.add( new MultiSelectModelData("Q"));
    mdList.add( new MultiSelectModelData("R"));
    mdList.add( new MultiSelectModelData("S"));
    final MultiSelectComboBox mcb = new MultiSelectComboBox(mdList);
    
    RootPanel.get("comboBox2").add(mcb);
    RootPanel.get("button").add( new Button("GetValue", new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			String value = mcb.getRawValue();
			com.google.gwt.user.client.Window.alert("The raw value is : " + value);
		}
	}));
    
  }

	private native String getTemplateString(String displayName, String name) /*-{
		return [ '<tpl for=".">', '<div class="x-combo-list-item">{',
				displayName, '}<{', name, '}></div>', '</tpl>' ].join("");
	}-*/;

}

class ComboBoxModelData extends BaseModelData {
	public static String NAME = "name";
	public static String DISPLAY_NAME = "display_name";

	public ComboBoxModelData(String name, String displayName) {
		this.setName(name);
		this.setDisplayName(displayName);
	}

	public String getName() {
		return (String) this.get(NAME);
	}

	public void setName(String name) {
		this.set(NAME, name);
	}

	public String getDisplayName() {
		return (String) this.get(DISPLAY_NAME);
	}

	public void setDisplayName(String displayName) {
		this.set(DISPLAY_NAME, displayName);
	}
}
