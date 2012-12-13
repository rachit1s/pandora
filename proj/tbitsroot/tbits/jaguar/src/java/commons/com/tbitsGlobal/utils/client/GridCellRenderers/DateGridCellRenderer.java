package commons.com.tbitsGlobal.utils.client.GridCellRenderers;

import java.util.Date;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.i18n.client.DateTimeFormat;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldDate;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJODate;

/**
 * 
 * @author sourabh
 * 
 * Cell renderer for date fields
 */
public class DateGridCellRenderer extends AbstractFieldCellRenderer<TbitsTreeRequestData, BAFieldDate> {

	public DateGridCellRenderer(BAFieldDate field) {
		super(Mode.VIEW, field);
	}

	@Override
	public Object render(TbitsTreeRequestData model, String property,
			ColumnData config, int rowIndex, int colIndex,
			ListStore<TbitsTreeRequestData> store,
			Grid<TbitsTreeRequestData> grid) {
		POJO pojo = model.getAsPOJO(property);
		if(pojo != null && pojo instanceof POJODate){
			Date date = ((POJODate)pojo).getValue();
			if(date != null){
				String format = ClientUtils.getCurrentUser().getWebDateFormat();
				if(format == null)
					format = GlobalConstants.API_DATE_FORMAT;
				return DateTimeFormat.getFormat(format).format(date);
			}
		}
		return "";
	}

}
