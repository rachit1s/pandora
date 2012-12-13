package commons.com.tbitsGlobal.utils.client.log;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.HTML;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;

public class TbitsWidgetLogger extends Window{
	private static final Level[] levels = {
	      Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG,
	      Level.FINE, Level.FINER, Level.FINEST, Level.OFF, Level.ALL};
	
	private Button[] levelButtons;
	
	private LayoutContainer container = new LayoutContainer(){
		@Override
		public Html addText(String text) {
			Html html = super.addText(text);
			this.layout();
			return html;
		}
	};
	
	public TbitsWidgetLogger() {
		super();
		
		this.setSize(800, 400);
		this.setHeading("Logger Console");
		this.setMaximizable(true);
		this.setLayout(new FitLayout());
		this.setScrollMode(Scroll.AUTO);
		
		container.setStyleAttribute("background", "#fff");
		container.setStyleAttribute("padding", "2px;");
		container.setScrollMode(Scroll.AUTO);
		container.setLayoutOnChange(true);
		
		this.add(container, new FitData());
		
		levelButtons = new Button[levels.length];
	    for (int i = 0; i < levels.length; i++) {
	      final Level level = levels[i];
	      levelButtons[i] = new Button(level.getName());
	      this.addButton(levelButtons[i]);
	      levelButtons[i].addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				Log.setCurrentLogLevel(level);
				TbitsInfo.info("Log Level set to : " + level.getName());
			}});
	    }
	    
	    this.addButton(new Button("Clear", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				clear();
			}}));
	}
	
	public void clear() {
		this.container.removeAll();
		this.layout();
	}

	public boolean isSupported() {
		return true;
	}
	
	public void log(LogRecord record, String formattedMessage) {
	    String title = makeTitle(record);
	    addLogText("<div style='color: " + getColor(record.getLevel()) + "' title='" + title + "'>" + formattedMessage + "</div>");
	}
	
	private String makeTitle(LogRecord record) {
	    String message = record.getMessage();
	    Throwable throwable = record.getThrown();
	    if (throwable != null) {
	      if (throwable.getMessage() == null) {
	        message = throwable.getClass().getName();
	      } else {
	        message = throwable.getMessage().replaceAll(
	            throwable.getClass().getName().replaceAll("^(.+\\.).+$", "$1"), "");
	      }
	    }
	    return message.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "\"");
	}
	
	private void addLogText(String logText) {
	    container.add(new HTML(logText));
	}
	
	private String getColor(Level logLevel) {
	    if (logLevel == Level.OFF) {
	      return "#000"; // black
	    }
	    
	    if (logLevel == Level.SEVERE) {
	      return "#C11B17"; // dark red
	    }
	    if (logLevel == Level.WARNING) {
	      return "#E56717"; // dark orange
	    }
	    if (logLevel == Level.INFO) {
	      return "#2B60DE"; // blue
	    }
	    
	    return "#20b000"; // green
	}
}
