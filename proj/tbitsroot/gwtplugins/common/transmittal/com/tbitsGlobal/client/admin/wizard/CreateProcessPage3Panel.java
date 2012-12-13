package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import transmittal.com.tbitsGlobal.client.admin.pages.PostTrnFieldMapPanel;
import transmittal.com.tbitsGlobal.client.models.TrnPostProcessValue;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

/**
 * Panel to hold the grid for Page 3 of wizard
 * @author devashish
 *
 */
public class CreateProcessPage3Panel extends PostTrnFieldMapPanel {
	
	@SuppressWarnings("unchecked")
	public CreateProcessPage3Panel(){
		super();
		
		this.setHeaderVisible(true);
		this.setHeading("Post Transmittal Field Map [Page 3]");
		
		canSave 			= false;
		commonGridDisabled 	= false;
		ToolBarButton clearButton = new ToolBarButton("Clear All", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				clearGrid();
			}
		});
		
		toolbar.add(clearButton);
		((ComboBox<TrnProcess>)toolbar.getWidget(0)).setEmptyText("Choose a Transmittal Process for reference");
	}
	
	/**
	 * Clear all the values from grid
	 */
	private void clearGrid(){
		if(this.singleGridContainer.getModels().size() > 0){
			this.singleGridContainer.removeAllModels();
		}
	}
	
	/**
	 * Get all the values from post trn field map grid
	 * @return
	 */
	public List<TrnPostProcessValue> getPostTrnFieldMap(){
		return this.singleGridContainer.getModels();
	}
}
