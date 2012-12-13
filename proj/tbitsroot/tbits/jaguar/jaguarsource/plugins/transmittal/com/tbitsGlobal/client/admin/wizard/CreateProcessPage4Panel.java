package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import transmittal.com.tbitsGlobal.client.admin.pages.SrcTargetFieldMapPanel;
import transmittal.com.tbitsGlobal.client.models.TrnFieldMapping;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

/**
 * Create the panel to hold the grid for page 4 of wizard
 * @author devashish
 */
public class CreateProcessPage4Panel extends SrcTargetFieldMapPanel {

	@SuppressWarnings("unchecked")
	public CreateProcessPage4Panel(){
		super();
		
		this.setHeaderVisible(true);
		this.setHeading("Source Target Field Map [Page 4]");
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
	 * Clear all the values from the grid
	 */
	private void clearGrid(){
		if(this.singleGridContainer.getModels().size() > 0){
			this.singleGridContainer.removeAllModels();
		}
	}
	
	/**
	 * Get the Source Target Field Mapping stored in the grid
	 * @return
	 */
	public List<TrnFieldMapping> getSrcTargetFieldMapping(){
		return this.singleGridContainer.getModels();
	}
	
}
