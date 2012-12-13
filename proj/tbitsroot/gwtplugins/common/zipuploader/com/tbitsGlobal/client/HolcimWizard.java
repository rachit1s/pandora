package zipuploader.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;

import transmittal.com.tbitsGlobal.client.ITransmittalWizardPage;
import transmittal.com.tbitsGlobal.client.TransmittalAbstractWizard;
import transmittal.com.tbitsGlobal.client.WizardData;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.wizards.IWizardPage;

public class HolcimWizard extends TransmittalAbstractWizard {

	public HolcimWizard() {
		super();
		this.addFinishButton();
		this.addBackButton();
		final WizardData dataObject = new WizardData();

		HolcimConstants.dbService.fetchConstants(ClientUtils.getCurrentBA()
				.getSystemId(), new AsyncCallback<HashMap<String, Object>>() {

			@Override
			public void onFailure(Throwable arg0) {
				Window.alert("some error occured");
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(HashMap<String, Object> arg0) {
				HashMap<String, String> hm1 = (HashMap<String, String>) arg0
						.get("config");
				ArrayList<TbitsModelData> tmd = (ArrayList<TbitsModelData>) arg0
						.get("fields");

				HolcimClientConstants.fields = tmd;

				for (String key : hm1.keySet()) {
					if (key.equals("deliverableFieldID")) {
						HolcimClientConstants.setDeliverableFieldID(Integer
								.parseInt(hm1.get(key)));
					} else if (key.equals("TRN_PROCESS_ID")) {
						HolcimClientConstants.setTRN_PROCESS_ID(Integer
								.parseInt(hm1.get(key)));

					} else if (key.equals("docNoField")) {
						HolcimClientConstants.setDocField(hm1.get(key));

					}

					HolcimWizardPage page1 = new HolcimWizardPage(context, HolcimWizard.this);
					HolcimGridWizardPage page2 = new HolcimGridWizardPage(context, HolcimWizard.this,
							dataObject);
					HolcimFinalGridWizardPage page3 = new HolcimFinalGridWizardPage(context);
					HolcimWizard.this.addPage(page1);
					HolcimWizard.this.addPage(page2);
					HolcimWizard.this.addPage(page3);
					HolcimWizard.this.show();

					activePage = page1;
					activePage.onDisplay();
				}
			}

		});
/*
		HolcimWizardPage page1 = new HolcimWizardPage(context, this);
		HolcimGridWizardPage page2 = new HolcimGridWizardPage(context, this,
				dataObject);
		HolcimFinalGridWizardPage page3 = new HolcimFinalGridWizardPage(context);
		HolcimWizard.this.addPage(page1);
		HolcimWizard.this.addPage(page2);
		this.addPage(page3);
		HolcimWizard.this.show();

		activePage = page1;
		activePage.onDisplay();*/

	}

	@Override
	protected void addFinishTransmittalButtonForPage1() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addFinishTransmittalButtonForPage2() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addBackButton() {
		backBtn = new Button("Validate", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				activePage.funcToBeCalledOnBack();
			}

		});
		this.addButton(backBtn);
	}

	@Override
	public void addFinishButton() {

	}

	public void addNextButton() {
		nextBtn = new Button("Add/Update Requests",
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						try {

							activePage.onLeave();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
		this.addButton(nextBtn);

	}

	@Override
	protected void addPreviewDOCButton() {
		previewDOCBtn = new Button("Create Transmital",
				new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						previewDOCBtn.disable();
						activePage.funcToBeCalledOnBack();

					}
				});
		this.addButton(previewDOCBtn);

	}

	@Override
	protected void addPreviewPDFButton() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onSubmit() {
		// TODO Auto-generated method stub

	}

	protected void callNextButton() {
		int current = activePage.getDisplayOrder();
		if (pages.containsKey(current + 1)) {
			if (activePage.onLeave()) {
				ITransmittalWizardPage<? extends LayoutContainer, ?> nextPage = activePage
						.getNext();
				layout.setActiveItem(nextPage.getWidget());
				activePage = nextPage;
				activePage.onDisplay();
			}
		}

	}

	protected void callNext1Button(WizardData wizardData) {
		int current = activePage.getDisplayOrder();
		activePage.getNext().buildPage(wizardData);

		ITransmittalWizardPage<? extends LayoutContainer, ?> nextPage = activePage
				.getNext();
		layout.setActiveItem(nextPage.getWidget());
		activePage = nextPage;
		activePage.onDisplay();

	}

	/**
	 * This method adds a page to the wizard. Insert a page in the order and
	 * define the page before and the page after this page. (similar to
	 * inserting in a linked list)
	 * 
	 * @param a
	 *            {@link IWizardPage}
	 */
	protected void addPage(
			ITransmittalWizardPage<? extends LayoutContainer, ?> page) {
		this.add(page.getWidget());
		page.onInitialize();
		int displayOrder = page.getDisplayOrder();
		if (pages.containsKey(displayOrder - 1)) {
			ITransmittalWizardPage<? extends LayoutContainer, ?> prePage = pages
					.get(displayOrder - 1);
			prePage.setNext(page);
			page.setPrevious(prePage);
		}
		if (pages.containsKey(displayOrder + 1)) {
			ITransmittalWizardPage<? extends LayoutContainer, ?> nextPage = pages
					.get(displayOrder + 1);
			nextPage.setPrevious(page);
			page.setNext(nextPage);
		}
		pages.put(displayOrder, page);
	}

}
