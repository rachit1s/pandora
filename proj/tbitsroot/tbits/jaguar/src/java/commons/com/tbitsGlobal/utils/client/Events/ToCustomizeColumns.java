package commons.com.tbitsGlobal.utils.client.Events;

import com.extjs.gxt.ui.client.widget.grid.Grid;

public class ToCustomizeColumns extends TbitsBaseEvent {
	private Grid source;

	public ToCustomizeColumns(Grid source) {
		super();
		
		this.source = source;
	}

	public void setSource(Grid source) {
		this.source = source;
	}

	public Grid getSource() {
		return source;
	}
	
}
