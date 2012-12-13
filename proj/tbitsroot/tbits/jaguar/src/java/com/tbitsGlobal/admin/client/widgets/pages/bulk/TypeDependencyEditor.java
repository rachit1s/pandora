package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.event.CheckChangedEvent;
import com.extjs.gxt.ui.client.event.CheckChangedListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.TreeNode;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeDependency;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class TypeDependencyEditor<M> extends TreePanel<BaseTreeModel>{

	private TypeClient type;
	
	public TypeDependencyEditor(TypeClient type) {
		super(new TreeStore<BaseTreeModel>());
		
		this.type = type;
		
		this.setCheckable(true);  
	    this.setAutoLoad(true);  
	    this.setTrackMouseOver(true);
	   // this.setCheckStyle(CheckCascade.CHILDREN);
		this.setDisplayProperty(TypeClient.DISPLAY_NAME);
	
		
		APConstants.apService.getFields(ClientUtils.getSysPrefix(), new AsyncCallback<List<BAField>>() {
			@Override
			public void onSuccess(List<BAField> result) {
				if(result != null){
					final List<BAFieldCombo> typeFields = new ArrayList<BAFieldCombo>();
					for(BAField field : result){
						if(field.getFieldId() == TypeDependencyEditor.this.type.getFieldId())
							continue;
						if(field instanceof BAFieldCombo)
							typeFields.add((BAFieldCombo) field);
					}
					APConstants.apService.getTypeDependenciesForType(TypeDependencyEditor.this.type, new AsyncCallback<List<TypeDependency>>() {
						@Override
						public void onSuccess(List<TypeDependency> result) {
							if(result != null)
								build(result, typeFields);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							TbitsInfo.error("Could not retrieve type dependencies.. Please see logs for details..", caught);
							Log.error("Could not retrieve type dependencies.. Please see logs for details..", caught);
						}
					});
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not retrieve type dependencies.. Please see logs for details..", caught);
				Log.error("Could not retrieve type dependencies.. Please see logs for details..", caught);
			}
		});
	}
	
	private void build(List<TypeDependency> dependencies, List<BAFieldCombo> typeFields){
		HashMap<Integer, List<Integer>> dependencyMap = new HashMap<Integer, List<Integer>>();
		for(TypeDependency dependency : dependencies){
			int destFieldId = dependency.getDestFieldId();
			if(!dependencyMap.containsKey(destFieldId))
				dependencyMap.put(destFieldId, new ArrayList<Integer>());
			dependencyMap.get(destFieldId).add(dependency.getDestTypeId());
		}
		
		List<BaseTreeModel> checkedItems = new ArrayList<BaseTreeModel>();
		for(BAFieldCombo typeField : typeFields){
			if(typeField.getFieldId() == type.getFieldId())
				continue;
			BaseTreeModel model = new BaseTreeModel();
			model.set(TypeClient.DISPLAY_NAME, typeField.getDisplayName());
			model.set("id", typeField.getFieldId());
			for(TypeClient type : typeField.getTypes()){
				BaseTreeModel childModel = new BaseTreeModel();
				childModel.set(TypeClient.DISPLAY_NAME, type.getDisplayName());
				childModel.set("id", type.getTypeId());
				model.add(childModel);
				
				if(dependencyMap.get(typeField.getFieldId()) != null && dependencyMap.get(typeField.getFieldId()).contains(type.getTypeId()))
					checkedItems.add(childModel);
			}
			
			this.store.add(model, true);
		}
		
		for(BaseTreeModel model : checkedItems){
			this.setChecked(model, true);
		}
	}
	
	public List<TypeDependency> getDependencies(){
		List<TypeDependency> dependencies = new ArrayList<TypeDependency>();
		List<BaseTreeModel> checkedNodes = this.getCheckedSelection();
		for(BaseTreeModel model : checkedNodes){
			if(this.isLeaf(model)){
				int fieldId = (Integer)model.getParent().get("id");
				int typeId = (Integer)model.get("id");
				
				TypeDependency dependency  = new TypeDependency();
				dependency.setSysId(type.getSystemId());
				dependency.setSrcFieldId(type.getFieldId());
				dependency.setSrcTypeId(type.getTypeId());
				dependency.setDestFieldId(fieldId);
				dependency.setDestTypeId(typeId);
				dependencies.add(dependency);
			}
		}
		return dependencies;
	}
	
	
	@Override
	protected void onClick(TreePanelEvent tpe) {
		
	    TreeNode node = tpe.getNode();
	    if (node != null) {
	      Element jointEl = view.getJointElement(node);
	      if (jointEl != null && tpe.within(jointEl)) {
	        toggle((BaseTreeModel) tpe.getItem());
	      } else if (GXT.isHighContrastMode) {
	        Rectangle r = El.fly(jointEl).getBounds();
	        if (r.contains(tpe.getClientX(), tpe.getClientY())) {
	          toggle((BaseTreeModel) tpe.getItem());
	        }
	      }
	      Element checkEl = view.getCheckElement(node);
	      if (isCheckable() && checkEl != null && tpe.within(checkEl)) {
	        onCheckClick(tpe, node);
	      }
	    }
		this.setCheckStyle(CheckCascade.CHILDREN); 
	}
	
}
