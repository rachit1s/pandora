package com.tbitsGlobal.jaguar.client;

import com.google.gwt.i18n.client.Constants;

public interface JaguarProps extends Constants{
	
//----------- Remote Service --------------
	/**
	 * URL of the app
	 * @return
	 */
	//SANDEEP::These urls are wrong. Because these should be relative urls
	@DefaultStringValue("/jaguar/db")
	String url();
	
	/**
	 * Url of proxy servlet
	 * @return
	 */
	@DefaultStringValue("/jaguar/proxy")
	String proxy();
	
//----------- Application ---------------
	@DefaultStringValue("<link rel=\"stylesheet\" type=\"text/css\" href=\"resources/css/gxt-all.css\" />")
	String style();
}
