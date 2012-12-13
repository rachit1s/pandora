package com.tbitsGlobal.jaguar.client.widgets.forms;

import java.util.ArrayList;

import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IUpdateRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IViewRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IAddRequestForm;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IUpdateRequestForm;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IViewRequestForm;

public class RequestFormFactory 
{
	/**
	 * set this variable in UIContext if you want to create
	 * the form for the business_area other then the one globally selected
	 */
	final public static String SYS_PREFIX = "sys_prefix";
	
	private static RequestFormFactory factory  = null ;
	private RequestFormFactory()
	{
	}
	
	public synchronized  static RequestFormFactory getInstance()
	{
		if( null == factory )
		{
			factory = new RequestFormFactory() ;		
		}
		
		return factory ;
	}
	
	public IAddRequestForm getAddRequestForm(UIContext uiContext)
	{
		// RequestPanelSlot 
		IAddRequestForm adf = null ;
		String sysPrefix = uiContext.getValue(SYS_PREFIX, String.class);
		if( null == sysPrefix )
			sysPrefix = ClientUtils.getSysPrefix() ;
		
		// run the plugins
		ArrayList<IAddRequestFormPlugin> myPlugins = GWTPluginRegister.getInstance().getPlugins(RequestPanelSlot.class,IAddRequestFormPlugin.class);
		if( null != myPlugins )
		{
			for( IAddRequestFormPlugin afp : myPlugins )
			{
				if( afp.shouldExecute(sysPrefix))
				{
					adf = afp.getWidget(uiContext);
				}
			}
		}
		
		// get the default instance.
		if( null == adf )
			adf = new AddRequestForm(uiContext);
		
		return adf ;
	}
	
	public IUpdateRequestForm getUpdateRequestForm(UIContext uiContext)
	{
		// RequestPanelSlot 
		IUpdateRequestForm udf = null ;
		
		String sysPrefix = uiContext.getValue(SYS_PREFIX, String.class);
		if( null == sysPrefix )
			sysPrefix = ClientUtils.getSysPrefix() ;
		
		// run the plugins
		ArrayList<IUpdateRequestFormPlugin> myPlugins = GWTPluginRegister.getInstance().getPlugins(RequestPanelSlot.class,IUpdateRequestFormPlugin.class);
		if( null != myPlugins )
		{
			for( IUpdateRequestFormPlugin afp : myPlugins )
			{
				if( afp.shouldExecute(sysPrefix))
				{
					udf = afp.getWidget(uiContext);
				}
			}
		}
		
		// get the default instance.
		if( null == udf )
			udf = new UpdateRequestForm(uiContext);
		
		return udf ;
	}

	public IViewRequestForm getViewRequestForm(UIContext uiContext)
	{
		// RequestPanelSlot 
		IViewRequestForm vf = null ;
		
		String sysPrefix = uiContext.getValue(SYS_PREFIX, String.class);
		if( null == sysPrefix )
			sysPrefix = ClientUtils.getSysPrefix() ;
		
		// run the plugins
		ArrayList<IViewRequestFormPlugin> myPlugins = GWTPluginRegister.getInstance().getPlugins(RequestPanelSlot.class,IViewRequestFormPlugin.class);
		if( null != myPlugins )
		{
			for( IViewRequestFormPlugin afp : myPlugins )
			{
				if( afp.shouldExecute(sysPrefix))
				{
					vf = afp.getWidget(uiContext);
				}
			}
		}
		
		// get the default instance.
		if( null == vf )
			vf = new RequestView(uiContext);
		
		return vf ;
	}
}
