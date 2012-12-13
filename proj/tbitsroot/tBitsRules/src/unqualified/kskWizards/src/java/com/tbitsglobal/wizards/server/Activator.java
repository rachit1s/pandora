package com.tbitsglobal.wizards.server;

import invitationLetterWizard.com.tbitsGlobal.client.ILService;
import invitationLetterWizard.com.tbitsGlobal.server.InvitationDBServiceImpl;
import transbit.tbits.plugin.GWTProxyServletManager;


@SuppressWarnings("serial")
public class Activator extends InvitationDBServiceImpl{
	static{
		GWTProxyServletManager.getInstance().subscribe(ILService.class.getName(), Activator.class);
		System.out.println("Subscribed " + Activator.class.getName());
	}
}
