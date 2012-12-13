package commons.com.tbitsGlobal.utils.client.log;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.google.gwt.logging.client.HtmlLogFormatter;

public class ContainerLogHandler extends Handler{
	
	private TbitsWidgetLogger widgetContainer;
	
	public ContainerLogHandler(TbitsWidgetLogger container) {
		this.widgetContainer = container;
	    setFormatter(new HtmlLogFormatter(true));
	    setLevel(Level.ALL);
	}

	@Override
	public void close(){
		widgetContainer.hide();
	}

	@Override
	public void flush() {}

	@Override
	public void publish(LogRecord record) {
		if (!isLoggable(record)) {
			return;
		}
		
		Formatter formatter = getFormatter();
	    String msg = formatter.format(record);
		
		widgetContainer.log(record, msg);
	}

}
