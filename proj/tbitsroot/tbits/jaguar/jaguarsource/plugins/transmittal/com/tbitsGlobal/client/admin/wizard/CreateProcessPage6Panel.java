package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import transmittal.com.tbitsGlobal.client.admin.pages.DistListPanel;
import transmittal.com.tbitsGlobal.client.models.TrnDistList;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

/**
 * Create a panel to hold the grid for page 6 of wizard
 * @author devashish
 *
 */
public class CreateProcessPage6Panel extends DistListPanel {
	
	@SuppressWarnings("unchecked")
	public CreateProcessPage6Panel(){
		super();
		
		this.setHeaderVisible(true);
		this.setHeading("Distribution Table [Page 6]");
		
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
	 * Clear the grid and remove all the data
	 */
	private void clearGrid(){
		if(this.singleGridContainer.getModels().size() > 0){
			this.singleGridContainer.removeAllModels();
		}
	}
	
	/**
	 * Get the values from the grid for Distribution List table
	 * @return
	 */
	public List<TrnDistList> getDistributionList(){
		return this.singleGridContainer.getModels();
	}

}
