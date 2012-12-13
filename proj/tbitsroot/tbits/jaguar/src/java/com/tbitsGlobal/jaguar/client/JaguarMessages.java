package com.tbitsGlobal.jaguar.client;

import com.google.gwt.i18n.client.Messages;

public interface JaguarMessages extends Messages{
//----------- Application --------------
	@DefaultMessage("Internet Explorer 6 is not officially supported. Please Upgrade your browser") 
	String ie6_found();
	
	@DefaultMessage("All your unsaved changes would be lost")
	String on_window_leave();
}
