package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import transmittal.com.tbitsGlobal.client.admin.pages.AttachmentListPanel;
import transmittal.com.tbitsGlobal.client.models.TrnAttachmentList;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

/**
 * Create a panel for page 5 of the wizard
 * @author devashish
 *
 */
public class CreateProcessPage5Panel extends AttachmentListPanel {
	
	@SuppressWarnings("unchecked")
	public CreateProcessPage5Panel(){
		super();
		
		this.setHeaderVisible(true);
		this.setHeading("Attachment Selection Table [Page 5]");
		
		canSave 			= false;
		ToolBarButton clearButton = new ToolBarButton("Clear All", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				clearGrid();
			}
		});
		
		toolbar.add(clearButton);
		
		((ComboBox<TrnProcess>)toolbar.getWidget(0)).setEmptyText("Choose a Transmittal Process for reference");
	}
	
	/***
	 * Clear the Grid 
	 */
	private void clearGrid(){
		if(this.singleGridContainer.getModels().size() > 0){
			this.singleGridContainer.removeAllModels();
		}
	}
	
	/**
	 * Return a list containing all the values for Attachment List table
	 * @return
	 */
	public List<TrnAttachmentList> getAttachemtList(){
		return this.singleGridContainer.getModels();
	}

}
