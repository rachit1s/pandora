package com.tbitsGlobal.uploader.gears.client.interfaces;

import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author sourabh
 *
 * An {@link IUploadListener} that can return a {@link Widget}
 */
public interface IUploadListenerWidget extends IUploadListener{
	public Widget getWidget();
}
