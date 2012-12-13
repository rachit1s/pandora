package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import transmittal.com.tbitsGlobal.client.admin.pages.ValidationRulesPanel;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnValidationRule;

/**
 * Create a panel for page 8 of the wizard
 * @author devashish
 *
 */
public class CreateProcessPage8Panel extends ValidationRulesPanel {
	
	@SuppressWarnings("unchecked")
	public CreateProcessPage8Panel(){
		super();
		
		this.setHeaderVisible(true);
		this.setHeading("Validation Rules [Page 8]");
		
		canSave 			= false;
		ToolBarButton clearButton = new ToolBarButton("Clear All", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				clearGrid();
			}
		});
		
		toolbar.add(clearButton);
		((ComboBox<TrnProcess>)toolbar.getWidget(0)).setEmptyText("Choose a Transmittal Process for reference");
	}
	
	/**
	 * Clear the grid and remove all data
	 */
	private void clearGrid(){
		if(this.singleGridContainer.getModels().size() > 0){
			this.singleGridContainer.removeAllModels();
		}
	}
	
	/**
	 * Get the data from table for Validation Rules table
	 * @return
	 */
	public List<TrnValidationRule> getValidationRules(){
		return this.singleGridContainer.getModels();
	}
	
}
