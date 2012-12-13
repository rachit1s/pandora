package commons.com.tbitsGlobal.utils.client.pojo;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;

public class POJODate extends POJO<Date>{
	private static final long serialVersionUID = 1L;
	
	private String format = GlobalConstants.API_DATE_FORMAT;
	
	public POJODate() {
		super();
	}
	
	public POJODate(Date value) {
		super(value);
	}

	@Override
	public POJO<Date> clone() {
		return new POJODate(new Date(this.value.getTime()));
	}

	@Override
	public String toString() {
		return DateTimeFormat.getFormat(this.format).format(this.value);
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getFormat() {
		return format;
	}

	@Override
	public int compareTo(POJO<Date> o) {
		return o.getValue().compareTo(this.value);
	}

}
