package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import transmittal.com.tbitsGlobal.client.admin.pages.ProcessParamsPanel;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnProcessParam;

/**
 * Panel which holds the grid for Page 2 of wizard
 * @author devashish
 *
 */
public class CreateProcessPage2Panel extends ProcessParamsPanel {
	
	@SuppressWarnings("unchecked")
	public CreateProcessPage2Panel(){
		super();
		
		this.setHeaderVisible(true);
		this.setHeading("Transmittal Process Parameters [Page 2]");
		
		canSave = false;
		
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
	 * Get the Transmittal Process Parameters from the grid
	 * @return
	 */
	public List<TrnProcessParam> getProcessParams(){
		return this.singleGridContainer.getModels();
	}
}
