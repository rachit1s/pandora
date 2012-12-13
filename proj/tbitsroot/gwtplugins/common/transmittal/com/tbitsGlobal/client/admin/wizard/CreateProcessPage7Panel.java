package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import transmittal.com.tbitsGlobal.client.admin.pages.DrawingNumberFieldPanel;
import transmittal.com.tbitsGlobal.client.models.TrnDrawingNumber;

/**
 * Create a panel for page 7 of the wizard
 * @author devashish
 *
 */
public class CreateProcessPage7Panel extends DrawingNumberFieldPanel {
	
	public CreateProcessPage7Panel(){
		super();
		
		this.setHeaderVisible(true);
		this.setHeading("Drawing Number Table [Page 7]");
		
		canSave 			= false;
		ToolBarButton clearButton = new ToolBarButton("Clear All", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				clearGrid();
			}
		});
		
		toolbar.add(clearButton);
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
	 * Get the values from the grid for the Distribution List table
	 * @return
	 */
	public List<TrnDrawingNumber> getDistributionList(){
		return this.singleGridContainer.getModels();
	}

}
