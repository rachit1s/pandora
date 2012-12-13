package mom.com.tbitsGlobal.client.Extensions;

import java.util.List;

import mom.com.tbitsGlobal.client.MeetingHeader;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.tbitsGlobal.jaguar.client.bulkupdate.BulkUpdatePanel;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class MOMGridTab extends TabItem {
	public static String CONTEXT_MEETING = "meeting";
	public static String CONTEXT_CAPTION = "caption";

	private UIContext myContext;
	private MeetingHeader meetingHeader;
	private TbitsMOMToolBar momToolBar;
	private BulkUpdatePanel bulkUpdatePanel;

	private boolean submitDone = false;
	
	public MOMGridTab(UIContext parentContext) {
		super();
		this.myContext = parentContext;
		final String caption = (String) this.myContext.getValue(CONTEXT_CAPTION, String.class);
		this.setText("MOM - " + caption);
		this.setClosable(true);
		this.setBorders(false);
		this.setLayout(new FitLayout());

		this.addListener(Events.BeforeClose, new Listener<TabPanelEvent>(){
			Listener<TabPanelEvent> l = this;
			
			public void handleEvent(TabPanelEvent be) {
				if(!submitDone){
					be.setCancelled(true);
					MessageBox confirm = new MessageBox();
					confirm.setButtons(MessageBox.YESNO);
					confirm.setTitle("Warning!!!");
					confirm.setMessage("Your " + caption + " has not been saved. Are you sure you want to close this " + caption);
					confirm.addCallback(new Listener<MessageBoxEvent>(){
						public void handleEvent(MessageBoxEvent be) {
							if(be.getButtonClicked().getText().toLowerCase().equals("yes")){
								MOMGridTab.this.removeListener(Events.BeforeClose, l);
								MOMGridTab.this.close();
							}
						}});
					confirm.show();
				}
			}});
		
		ContentPanel container = new ContentPanel();
		container.setLayout(new BorderLayout());
		container.setBodyBorder(false);
		container.setHeaderVisible(false);

		DefaultUIContext context = new DefaultUIContext();
		context = new DefaultUIContext(myContext);
		context.setValue(TbitsMOMToolBar.CONTEXT_PARENT_TAB, this);
		momToolBar = new TbitsMOMToolBar(ClientUtils.getSysPrefix(), context);
		container.setTopComponent(momToolBar);

		this.bulkUpdatePanel = new BulkUpdatePanel(ClientUtils.getSysPrefix(), 0);
//		this.bulkUpdatePanel.setToolBarPosition(ToolBarPosition.BOTTOM);
		container.add(this.bulkUpdatePanel, new BorderLayoutData(LayoutRegion.CENTER));

		DefaultUIContext headerContext = new DefaultUIContext(this.myContext);
		headerContext.setValue(MeetingHeader.CONTEXT_CAPTION, caption);
		headerContext.setValue(MeetingHeader.CONTEXT_PARENT_TAB, this);
		meetingHeader = new MeetingHeader(headerContext);
		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH);
		northData.setCollapsible(true);
		northData.setFloatable(true);
		northData.setSplit(true);
		northData.setMargins(new Margins(2));
		container.add(meetingHeader, northData);

		this.add(container, new FitData());
	}
	
	public void close(boolean submitDone) {
		this.submitDone = submitDone;
		super.close();
	}

	public void fillHeader(TbitsTreeRequestData model) {
		meetingHeader.setHeaderModel(model);
	}

	public void fillActions(List<TbitsTreeRequestData> actions) {
		bulkUpdatePanel.getSingleGridContainer().addModel(actions);
	}

	public MeetingHeader getMeetingHeader() {
		return meetingHeader;
	}

	public TbitsMOMToolBar getMomToolBar() {
		return momToolBar;
	}

	public BulkUpdatePanel getBulkUpdatePanel() {
		return bulkUpdatePanel;
	}
}
