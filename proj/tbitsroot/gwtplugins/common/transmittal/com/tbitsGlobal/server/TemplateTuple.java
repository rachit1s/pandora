package transmittal.com.tbitsGlobal.server;

public class TemplateTuple {
	
	String docname;
	public String getDocname() {
		return docname;
	}

	public void setDocname(String docname) {
		this.docname = docname;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	String format;
	
	public TemplateTuple(String docname,String format) {
		this.docname=docname;
		this.format=format;
	}

	public TemplateTuple() {
		// TODO Auto-generated constructor stub
	}

}
