package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeDependency;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class TypeDependencyEditorWindow extends Window{
	public TypeDependencyEditorWindow(final TypeClient type) {
		super();
		
		this.setClosable(true);
		this.setModal(true);
		this.setLayout(new FitLayout());
		this.setSize(400, 400);
		this.setHeading("Dependencies");
		this.setStyleAttribute("background", "#fff");
		
		final TypeDependencyEditor editor = new TypeDependencyEditor(type);
		this.add(editor, new FitData());
		
		this.addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<TypeDependency> dependencies = editor.getDependencies();
				APConstants.apService.updateTypeDependencies(type, dependencies, new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if(result)
							TbitsInfo.info("Type Dependencies successfully updated..");
					}
					
					@Override
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable to update type dependencies.. Please see logs for details..", caught);
						Log.error("Unable to update type dependencies.. Please see logs for details..", caught);
					}
				});
			}
		}));
	}
}
