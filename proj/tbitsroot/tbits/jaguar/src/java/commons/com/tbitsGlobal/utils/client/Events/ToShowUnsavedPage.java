package commons.com.tbitsGlobal.utils.client.Events;

public class ToShowUnsavedPage extends TbitsBaseEvent{
	private String pageCaption;

	public String getPageCaption() {
		return pageCaption;
	}
	public ToShowUnsavedPage(String pageCaption) {
		this.pageCaption = pageCaption;
	}
}