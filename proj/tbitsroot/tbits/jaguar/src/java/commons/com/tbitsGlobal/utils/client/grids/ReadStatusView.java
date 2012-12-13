package commons.com.tbitsGlobal.utils.client.grids;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridView;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.Joint;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

/**
 * 
 * @author sourabh
 * 
 * A tree grid view that displays request id red and bold.
 */
public class ReadStatusView extends TreeGridView{
	
	@Override
	protected void onColumnWidthChange(int column, int width) {
		super.onColumnWidthChange(column, width);
		this.refresh(false);
	}
	
	@Override
	public String getTemplate(ModelData m, String id, String text,
			AbstractImagePrototype icon, boolean checkable,
			Joint joint, int level) {
		 StringBuffer sb = new StringBuffer();
		    sb.append("<div id=\"");
		    sb.append(id);
		    sb.append("\" class=\"x-tree3-node\">");

		    sb.append("<div class=\"x-tree3-el\">");

		    String h = "";
		    switch (joint) {
		      case COLLAPSED:
		        h = GXT.IMAGES.tree_collapsed().getHTML();;
		        break;
		      case EXPANDED:
		        h = GXT.IMAGES.tree_expanded().getHTML();;
		        break;
		      default:
		        h = "<img src=\"" + GXT.BLANK_IMAGE_URL + "\" style='width: 16px'>";
		    }

		    sb.append("<img src=\"");
		    sb.append(GXT.BLANK_IMAGE_URL);
		    sb.append("\" style=\"height: 18px; width: ");
		    sb.append(level * 18);
		    sb.append("px;\" />");
		    sb.append(h);
		    if (checkable) {
		      sb.append(GXT.IMAGES.unchecked().getHTML());
		    } else {
		      sb.append("<span></span>");
		    }
		    if (icon != null) {
		      sb.append(icon.getHTML());
		    } else {
		      sb.append("<span></span>");
		    }
		    sb.append("<span class=\"x-tree3-node-text\">");
		    //sb.append("<span class=\" color : red; background : yellow;\"");
		    if(!((TbitsTreeRequestData)m).getRead())
		    	sb.append("<span style='font-weight:bold; color:#f00;'>");
		    else
		    	sb.append("<span>");
		    sb.append(text);
		    sb.append("</span>");
		    sb.append("</span>");

		    sb.append("</div>");
		    sb.append("</div>");

		    return sb.toString();
	}
}
