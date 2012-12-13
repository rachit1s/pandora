package commons.com.tbitsGlobal.utils.client.Uploader;

import gwtupload.client.HasProgress;
import gwtupload.client.IUploader;

import com.extjs.gxt.ui.client.widget.ProgressBar;

public class TbitsProgressBar extends ProgressBar implements HasProgress{
	
	private long startTime = System.currentTimeMillis();
	
	public TbitsProgressBar() {
		super();
		
		this.setIncrement(100);
		this.setInterval(1500);
		this.setWidth(150);
		
		this.setStyleAttribute("margin", "2px");
	}
	
	//"{0}% {1}/{2} KB. ({3} KB/s)"
	public void setProgress(int done, int total) {
		int percent = IUploader.Utils.getPercent(done, total);
		
		double d = new Double(percent);
		
		long soFar = System.currentTimeMillis() - startTime;
		long velocity = soFar > 0 ? ((done * 1000) / soFar) : 0;
		
		String text = percent + "% " + "(" + velocity + " KB/s)";
		this.updateProgress(d/100, text);
	}
}
