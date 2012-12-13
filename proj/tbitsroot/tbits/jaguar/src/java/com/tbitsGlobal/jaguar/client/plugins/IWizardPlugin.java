package com.tbitsGlobal.jaguar.client.plugins;

import java.util.ArrayList;

import commons.com.tbitsGlobal.utils.client.plugins.IGWTPlugin;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;

public interface IWizardPlugin extends IGWTPlugin<AbstractWizard, ArrayList<Integer>>{
	public String getButtonCaption();
}
