package commons.com.tbitsGlobal.utils.client.Uploader;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ModalPanel;
import com.extjs.gxt.ui.client.widget.Window;

import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;

/**
 * 
 * @author sourabh
 *
 * TODO : remove use of modal panel as we had to change the source of gxt for this
 */
public class UploadWindow extends Window{
	
	private AttachmentFieldContainer fieldContainer;
	
	public UploadWindow(String sysPrefix, TbitsTreeRequestData model, BAFieldAttachment field) {
		super();
		
		this.setHeading("Upload Files");
		this.setWidth(550);
		this.setHeight(400);
		this.setScrollMode(Scroll.AUTO);
		this.setModal(true);
		this.setDraggable(false);
		
		fieldContainer = new AttachmentFieldContainer(Mode.EDIT, sysPrefix, model, field);
		this.add(fieldContainer);
		
		LayoutContainer bottomMessage = new LayoutContainer();
		bottomMessage.setStyleAttribute("background", "#F6BC5D");
		bottomMessage.addText("<div style='padding:5px;'>Closing the window won't effect your uploads. The process will continue in the background</div>");
		this.setBottomComponent(bottomMessage);
	}
	
	@Override
	public void hide() {
		if(this.getFieldContainer().getInProgressUploads() + this.getFieldContainer().getQueuedUploads() > 0){
			this.setPagePosition(-1000, -1000);
			if (isModal()) {
				if(modalPanel != null)
					ModalPanel.push(modalPanel);
			    modalPanel = null;
			}
		}else
			super.hide();
	}
	
	public void hide(boolean force){
		if(force)
			super.hide();
		else hide();
	}
	
	@Override
	public void show() {
		super.show();
		
		if(modalPanel == null){
			modalPanel = ModalPanel.pop();
			modalPanel.show(this);
		}
		
		this.center();
	}

	public AttachmentFieldContainer getFieldContainer() {
		return fieldContainer;
	}
	
	public int getInProgressUploads(){
		return this.fieldContainer.getInProgressUploads();
	}
	
	public int getQueuedUploads(){
		return this.fieldContainer.getQueuedUploads();
	}
}
