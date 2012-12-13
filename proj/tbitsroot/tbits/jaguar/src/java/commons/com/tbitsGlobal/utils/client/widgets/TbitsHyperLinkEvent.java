package commons.com.tbitsGlobal.utils.client.widgets;

import com.extjs.gxt.ui.client.event.BoxComponentEvent;

public class TbitsHyperLinkEvent extends BoxComponentEvent {
	/**
	 * The source link.
	 */
	private TbitsHyperLink link;

	/**
	 * Creates a new TbitsHyperLink event.
	 * 
	 * @param button the source button
	 */
	public TbitsHyperLinkEvent(TbitsHyperLink button) {
	    super(button);
	    this.link = button;
	}

	public TbitsHyperLink getLink() {
		return link;
	}

}
