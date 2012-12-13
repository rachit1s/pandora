package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.List;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.modelData.EscalationConditionDetailClient;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;


public class EscalationConditionMapPanel extends AbstractAdminBulkUpdatePanel<EscalationConditionDetailClient> {
	
	public EscalationConditionMapPanel()
	{
		super();
		
	}
	

	@Override
	protected void onSave(List<EscalationConditionDetailClient> models, Button btn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EscalationConditionDetailClient getEmptyModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected BulkUpdateGridAbstract<EscalationConditionDetailClient> getNewBulkGrid(
			BulkGridMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(int page) {
		// TODO Auto-generated method stub
		
	}

}
