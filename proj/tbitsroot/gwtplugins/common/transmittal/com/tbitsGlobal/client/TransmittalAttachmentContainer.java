/**
 * 
 */
package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLink;

/**
 * Container to put the attachments in the request where they can be
 * selected/deselected
 * 
 * @author lokesh
 * 
 */
public class TransmittalAttachmentContainer extends LayoutContainer {

	private static final String CHECK_BOX_ATTACHMENT_INFO = "attachmentInfo";
	List<CheckBox> checkBoxList = new ArrayList<CheckBox>();
	List<String> fileNameList = new ArrayList<String>();

	// List<TbitsHyperLink> tbitsHyperLinkList = new
	// ArrayList<TbitsHyperLink>();

	// public TransmittalAttachmentContainer(TableLayout tableLayout, int
	// requestId, List<FileClient> attachments, HashMap<Integer,
	// TransmittalAttachmentContainer> attachmentsContainerList) {
	// super();
	// this.setLayout(tableLayout);
	// for(FileClient aic : attachments){
	// CheckBox checkBox = new CheckBox();
	// checkBox.setData(CHECK_BOX_ATTACHMENT_INFO, aic);
	// TbitsHyperLink tbitsHyperLink = new TbitsHyperLink(aic.getFileName());
	// this.add(checkBox);
	// this.add(tbitsHyperLink);
	// this.checkBoxList.add(checkBox);
	// }
	// attachmentsContainerList.put(requestId,this);
	// }

	public TransmittalAttachmentContainer(ColumnLayout columnLayout) {
		super();
		this.setLayout(columnLayout);
	}

	/**
	 * Build the attachment container to keep the list of attachments for a
	 * single row.
	 * 
	 * @param tableLayout
	 * @param aicList
	 *            - list of attachments
	 * @param isTrue
	 */
	public TransmittalAttachmentContainer(TableLayout tableLayout,
			List<FileClient> aicList, boolean isTrue) {

		super();
		this.setLayout(tableLayout);
		for (FileClient aic : aicList) {
			CheckBox checkBox = new CheckBox();
			checkBox.setValue(isTrue);
			checkBox.setData(CHECK_BOX_ATTACHMENT_INFO, aic);

			checkBox.addListener(Events.OnClick, new Listener<FieldEvent>() {
				public void handleEvent(FieldEvent be) {
					if (be.getField() instanceof CheckBox)
						if (!Boolean.parseBoolean(be.getField().getRawValue()))
							be.setValue(false);
				}
			});

			fileNameList.add(aic.getFileName());
			TbitsHyperLink tbitsHyperLink = new TbitsHyperLink(aic
					.getFileName());
			this.add(checkBox);
			this.add(tbitsHyperLink);
			this.checkBoxList.add(checkBox);
		}
	}

	public TransmittalAttachmentContainer(TableLayout tableLayout,
			List<FileClient> attachments) {

		super();
		this.setLayout(tableLayout);
		for (FileClient aic : attachments) {
			boolean isTrue = Boolean.valueOf((String) aic
					.get("IS_CHECKED_IN_TRANSIENT_DATA"));
			CheckBox checkBox = new CheckBox();
			checkBox.setValue(isTrue);
			checkBox.setData(CHECK_BOX_ATTACHMENT_INFO, aic);

			checkBox.addListener(Events.OnClick, new Listener<FieldEvent>() {
				public void handleEvent(FieldEvent be) {
					if (be.getField() instanceof CheckBox)
						if (!Boolean.parseBoolean(be.getField().getRawValue()))
							be.setValue(false);
				}
			});

			fileNameList.add(aic.getFileName());
			TbitsHyperLink tbitsHyperLink = new TbitsHyperLink(aic
					.getFileName());
			this.add(checkBox);
			this.add(tbitsHyperLink);
			this.checkBoxList.add(checkBox);
		}
	}

	public List<String> getFileNames() {
		return this.fileNameList;
	}
}
