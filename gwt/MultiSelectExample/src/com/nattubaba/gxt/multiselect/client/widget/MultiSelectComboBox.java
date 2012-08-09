package com.nattubaba.gxt.multiselect.client.widget;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Element;
/**
 * copied from http://bhat86.blogspot.in/2012/02/gxt-comobobox-with-multi-select-feature.html
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 * @param <D>
 */
public class MultiSelectComboBox<D extends ModelData> extends
		TriggerField<String> {

	private Dialog checkBoxListHolder;
	private CheckBoxListView<D> listView;
	private ListStore<D> store;

	private String delimiter = ",";
	private boolean readOnly;

	public MultiSelectComboBox() {
		store = new ListStore<D>();
		listView = new CheckBoxListView<D>();
	}

	@Override
	protected void onTriggerClick(ComponentEvent ce) {
		super.onTriggerClick(ce);
		checkBoxListHolder.setSize(getWidth(), 200);
		listView.setWidth(getWidth());
		checkBoxListHolder.setPosition(getAbsoluteLeft(), getAbsoluteTop()
				+ getHeight());
		if (checkBoxListHolder.isVisible()) {
			checkBoxListHolder.hide();
		} else {
			checkBoxListHolder.show();
		}
	}

	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);

		checkBoxListHolder = new Dialog();
		checkBoxListHolder.setClosable(false);
		checkBoxListHolder.setHeaderVisible(false);
		checkBoxListHolder.setFooter(false);
		checkBoxListHolder.setFrame(false);
		checkBoxListHolder.setResizable(false);
		checkBoxListHolder.setAutoHide(false);
		checkBoxListHolder.getButtonBar().setVisible(false);
		checkBoxListHolder.setLayout(new FillLayout());
		checkBoxListHolder.add(listView);
		listView.setStore(store);

		checkBoxListHolder.addWindowListener(new WindowListener() {

			@Override
			public void windowHide(WindowEvent we) {
				setValue(parseCheckedValues(listView));
			}

		});

	}

	private String parseCheckedValues(CheckBoxListView<D> checkBoxView) {
		StringBuffer buf = new StringBuffer();
		if (checkBoxView != null) {
			List<D> selected = checkBoxView.getChecked();
			int index = 1, len = selected.size();
			for (D c : selected) {
				buf.append(c.get(listView.getDisplayProperty()));
				if (index < len) {
					buf.append(delimiter);
				}
				index++;
			}
		}
		return buf.toString();
	}

	public CheckBoxListView<D> getListView() {
		return listView;
	}

	public void setListView(CheckBoxListView<D> listView) {
		this.listView = listView;
	}

	public ListStore<D> getStore() {
		return store;
	}

	public void setStore(ListStore<D> store) {
		this.store = store;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
}
