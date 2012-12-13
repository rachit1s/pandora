package com.tbitsGlobal.admin.client.widgets;

import com.tbitsGlobal.admin.client.utils.LinkIdentifier;

public class BATabItem extends APTabItem{

	protected String sysPrefix;
	
	public BATabItem(String sysPrefix, LinkIdentifier linkId) {
		super(linkId);
		
		this.sysPrefix = sysPrefix;
	}

}
