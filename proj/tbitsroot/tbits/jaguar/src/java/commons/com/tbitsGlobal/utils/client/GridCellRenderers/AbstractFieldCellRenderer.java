package commons.com.tbitsGlobal.utils.client.GridCellRenderers;

import com.extjs.gxt.ui.client.data.ModelData;

import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

/**
 * 
 * @author sourabh
 *
 * @param <M>
 * @param <T>
 * 
 * Abstract renderer for {@link BAField}s
 */
public abstract class AbstractFieldCellRenderer<M extends ModelData, T extends BAField> extends LinkCellRenderer<M> {
	protected Mode mode ;
	protected T field;
	
	protected AbstractFieldCellRenderer(Mode mode, T field) {
		super();
		this.mode = mode;
		this.field = field;
	}
}
