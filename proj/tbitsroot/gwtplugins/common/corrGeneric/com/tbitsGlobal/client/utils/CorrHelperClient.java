package corrGeneric.com.tbitsGlobal.client.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
//import com.tbitsGlobal.jaguar.client.UserPicker;
import com.tbitsGlobal.jaguar.client.cache.UserCache;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.CheckBoxFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.DateFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.NumberFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.TextAreaFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.TextFieldFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.TypeFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.UserPickerFieldConfig;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJOBoolean;
import commons.com.tbitsGlobal.utils.client.pojo.POJOInt;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;
import commons.com.tbitsGlobal.utils.client.widgets.TypeFieldControl;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;
//import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;
import corrGeneric.com.tbitsGlobal.client.objects.UserNotFoundException;
import corrGeneric.com.tbitsGlobal.shared.CorrConst;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.*;

public class CorrHelperClient 
{	
	private static HashMap<String,HashMap<String,List<TypeClient>>> typeModels = new HashMap<String,HashMap<String,List<TypeClient>>>() ;
	private static HashMap<String,HashMap<String, IFieldConfig>> fieldsMap = new HashMap<String,HashMap<String, IFieldConfig>>();
	private static HashMap<String,HashMap<String,BAField>> baFieldMap = new HashMap<String,HashMap<String,BAField>>();
//	private static HashMap<String,UserClient> userMap = null;
	static UserCache userCache = null;
	
	public static void registerBAFields( String sysPrefix , List<BAField> bafields )
	{
		HashMap<String,BAField> map = new HashMap<String,BAField>(bafields.size());
		for( BAField baf : bafields )
		{
			map.put(baf.getName(), baf);
		}
		baFieldMap.put(sysPrefix, map);
	}
	
	public static void setDefaultType(String sysPrefix, String typeFieldName)
	{
		HashMap<String,IFieldConfig> fieldConfigs = fieldsMap.get(sysPrefix);
		
		IFieldConfig fc = fieldConfigs.get(typeFieldName);
		if( null == fc || !(fc instanceof TypeFieldConfig))
		{
			System.out.println("Cannot find the type " + typeFieldName + " for ba : " + sysPrefix);
			return; 
		}
		
		TypeFieldConfig tfc = (TypeFieldConfig) fc ;
		TypeFieldControl tfcont = tfc.getWidget() ;
		if( null != typeModels.get(sysPrefix) && null != typeModels.get(sysPrefix).get(typeFieldName) )
		{
			tfcont.getStore().removeAll();
			tfcont.getStore().add(typeModels.get(sysPrefix).get(typeFieldName));
		}
	}
	
	public static void registerType( String sysPrefix, String typeFieldName )
	{
		HashMap<String,IFieldConfig> fieldConfigs = fieldsMap.get(sysPrefix) ;
		if( null == fieldConfigs )
			throw new IllegalArgumentException("The ba : " + sysPrefix + " is not registered.");
		
		IFieldConfig fc = fieldConfigs.get(typeFieldName);
		Widget w = fc.getWidget() ;
		
		if( w instanceof TypeFieldControl )
		{
			TypeFieldControl tfc = (TypeFieldControl) w ;
			List<TypeClient> models = tfc.getStore().getModels() ;
//			List<TbitsModelData> myModels = new ArrayList<TbitsModelData>() ;
//			myModels.addAll(models) ;
			
			HashMap<String,List<TypeClient>> tm = typeModels.get(sysPrefix);
			if( null == tm )
				tm = new HashMap<String,List<TypeClient>>() ;
			
			tm.put(typeFieldName, models);
			typeModels.put(sysPrefix,tm);
		}
	}

	public static void registerFields( String sysPrefix, HashMap<String,IFieldConfig> fieldConfigs )
	{
		fieldsMap.put(sysPrefix, fieldConfigs);
	}
	
	public static void disableField( String sysPrefix, String fieldName )
	{
		HashMap< String,IFieldConfig> fcMap = fieldsMap.get(sysPrefix);
		if( null == fcMap )
			throw new IllegalArgumentException("The ba : " + sysPrefix + " is not registered.");
		
		IFieldConfig fc = fcMap.get(fieldName);
		
		Widget w = fc.getWidget() ;
		
		if( w instanceof UserPicker )
		{
			UserPicker up = (UserPicker) w ;
			up.disable() ;
		}
		else if( w instanceof TypeFieldControl )
		{
			TypeFieldControl tfc = (TypeFieldControl) w ;
			tfc.disable() ;
		}
		else if( w instanceof TextField )
		{
			TextField tf = (TextField) w ;
			tf.disable() ;
		}
		else if( w instanceof TextArea )
		{
			TextArea ta = (TextArea) w ;
			ta.disable() ;
		}
		else if( w instanceof NumberField )
		{
			NumberField nf = ( NumberField ) w ;
			nf.disable() ;
		}
		else if( w instanceof DateField )
		{
			DateField df = (DateField)w ;
			df.disable() ;
		}
		else if( w instanceof CheckBox )
		{
			CheckBox cb = (CheckBox) w ;
			cb.disable() ;
		}
		else
		{
			System.out.println("Could not find widget type of : " + fieldName);
			com.google.gwt.user.client.Element e = w.getElement() ;
			e.setAttribute("disabled", "true");
		}		
	}
	
	public static boolean setUserPickerFieldConfig(UserPickerFieldConfig upfc, String value)
	{
		upfc.setPOJO(new POJOString(""));
		upfc.setPOJO(new POJOString(value));
		return true ;
	}
	
	public static boolean setTypeFieldConfig( String sysPrefix, TypeFieldConfig tfc, String value)
	{
		TypeClient current = tfc.getWidget().getValue() ;
		
		ListStore<TypeClient> ls = tfc.getWidget().getStore() ;
		ls.removeAll() ;
		
		ArrayList<TypeClient> myModels = getModelData( sysPrefix, tfc, value ) ;
		ls.add(myModels);

		tfc.getWidget().setStore(ls);
		if( null != current && contains( myModels, current))
		{
			tfc.getWidget().setValue(current);
		}
		else if( myModels.size() != 0 )
		{
			tfc.getWidget().setValue(myModels.get(0));
		}
		
		if( myModels.size() == 0 )
		{
			Log.error("Cannot find '" + value + "' in field '" + tfc.getName() + "'" );
			return false ;
		}
		
		return true ;
	}
	
	private static boolean contains(ArrayList<TypeClient> myModels, TypeClient current) 
	{
		if( null == current || null == myModels) 
			return false ;
		
		for( TypeClient tmd : myModels )
		{
			if( tmd.getName().equals(current.getName()))
				return true ;
		}
		
		return false;
	}

	private static ArrayList<TypeClient> getModelData(String sysPrefix, TypeFieldConfig tfc, String value) 
	{
		String [] myTypes = value.split(",");
		String fieldName = tfc.getBaField().getName() ;
		HashMap<String,List<TypeClient>> tm = typeModels.get(sysPrefix);
		List<TypeClient> allModels = tm.get(fieldName);
		ArrayList<TypeClient> myModels = new ArrayList<TypeClient>() ;
		for( String name : myTypes )
		{
			for( TypeClient tmd : allModels )
			{
				if( tmd.getName().equals(name))
				{
					myModels.add(tmd) ;
					break ;
				}
			}
		}
		return myModels;
	}

	public static boolean setValue( String sysPrefix, String fieldName , String fieldValue)
	{
		HashMap<String,IFieldConfig> fieldConfigs = fieldsMap.get(sysPrefix);
		
		IFieldConfig fc = fieldConfigs.get(fieldName);
		
		if( fc instanceof UserPickerFieldConfig )
		{
			UserPickerFieldConfig upfc = (UserPickerFieldConfig)fc;
			return setUserPickerFieldConfig(upfc, fieldValue);
		}
		else if( fc instanceof TypeFieldConfig )
		{
			TypeFieldConfig tfc = (TypeFieldConfig)fc ;
			return setTypeFieldConfig(sysPrefix, tfc, fieldValue);
		}
		else if( fc instanceof TextFieldFieldConfig )
		{
			TextFieldFieldConfig tffc = (TextFieldFieldConfig) fc ;
			return setTextFieldConfig(tffc, fieldValue);
		}
		else if( fc instanceof TextAreaFieldConfig )
		{
			TextAreaFieldConfig ta = (TextAreaFieldConfig) fc ;
			return setTextAreaFieldConfig( ta, fieldValue) ;
		}
		else if( fc instanceof NumberFieldConfig )
		{
			NumberFieldConfig nfc = ( NumberFieldConfig ) fc ;
			return setNumberFieldConfig( nfc, fieldValue );
		}
		else if( fc instanceof DateFieldConfig )
		{
			DateFieldConfig df = (DateFieldConfig)fc ;
			return setDateFieldConfig( df, fieldValue );
		}
		else if( fc instanceof CheckBoxFieldConfig )
		{
			CheckBoxFieldConfig cb = (CheckBoxFieldConfig) fc ;
			return setCheckBoxFieldConfig(cb, fieldValue);
		}
		else
		{
			Log.error("Could not find widget type of : " + fieldName + " for ba : " + sysPrefix);
			return false;
		}		
	}

	private static boolean setCheckBoxFieldConfig(CheckBoxFieldConfig cb,
			String fieldValue) 
	{
		if(fieldValue.equals("true"))
		{
			cb.setPOJO(new POJOBoolean(true));
		}
		else
		{
			cb.setPOJO(new POJOBoolean(false));
		}
		
		return true ;
	}

	private static boolean setDateFieldConfig(DateFieldConfig df, String fieldValue) 
	{
		// TODO : get date in apiformat
		return false ;
	}

	private static boolean setNumberFieldConfig(NumberFieldConfig nfc,	String fieldValue) 
	{
		try
		{
			Integer i = Integer.parseInt(fieldValue);
			nfc.setPOJO(new POJOInt(i));
			return true;
		}
		catch(NumberFormatException nfe)
		{
			nfe.printStackTrace();
			return false ;
		}
	}

	private static boolean setTextAreaFieldConfig(TextAreaFieldConfig ta,
			String fieldValue) 
	{
		ta.setPOJO(new POJOString(fieldValue));
		return true ;
	}

	private static boolean setTextFieldConfig(TextFieldFieldConfig tffc,
			String fieldValue) 
	{
		tffc.setPOJO(new POJOString(fieldValue));
		return true ;
	}
	
	public static String fdn( String sysPrefix, String fieldName )
	{
		if( null == sysPrefix || null == fieldName )
			return null;
		
		if( null != baFieldMap.get(sysPrefix) &&  null != baFieldMap.get(sysPrefix).get(fieldName) )
		{
			return baFieldMap.get(sysPrefix).get(fieldName).getDisplayName();
		}
		
		return null;
	}
	
	public static BAField lookupBAField( String sysPrefix, String fieldName )
	{
		if( null == baFieldMap )
			return null;
		if( null == baFieldMap.get(sysPrefix))
			return null;
		
		return baFieldMap.get(sysPrefix).get(fieldName);
	}
	public static void initializeUserMap()
	{
		if( null == userCache )
		{	
			userCache = CacheRepository.getInstance().getCache(UserCache.class);
		}
	}
	
	public static UserClient getUserClient(String ul)
	{
		initializeUserMap();
		return userCache.getObject(ul);
	}
	
	public static ArrayList<UserClient> getUserClientArray( Collection<String> userLogins )
	{
		initializeUserMap();
		ArrayList<UserClient> ucl = new ArrayList<UserClient>(userLogins.size());
		for( String ul : userLogins )
		{
			UserClient uc = getUserClient(ul);
			if( null != uc )
				ucl.add(uc);
		}
		
		return ucl;
	}
	
	public static ListStore<UserClient> getListStore( List<UserClient> users )
	{
		ListStore<UserClient> store = new ListStore<UserClient>();
		store.add(users);
		return store;
	}
	
	public static ListStore<UserClient> getListStore( Collection<String> userLogins )
	{
		ListStore<UserClient> store = new ListStore<UserClient>();
		store.add(getUserClientArray(userLogins));
		return store;
	}
	
	public static List<UserClient> getDefaultUserList(List<UserClient> currUserList)
	{
		initializeUserMap();
		
		Collection<UserClient> users = userCache.getValues();
		List<UserClient> allUsers = new ArrayList<UserClient>(users);
		return allUsers;
	}
	
	public static void initializeCorrApplicableBAs( final boolean showError)
	{
		CorrConst.corrDBService.getApplicableBas(new AsyncCallback<HashMap<String,? extends Serializable>>() 
		{
			public void onSuccess(HashMap<String,? extends Serializable> result) 
			{
				CorrConst.applicableBas = (ArrayList<String>) result.get(ApplicableBas);
				if( null == CorrConst.applicableBas )
					CorrConst.applicableBas = new ArrayList<String>(); // just add empty array so that we don't do async call everytime
				
//				showConfirmationValues = (HashMap<String,String>) result.get(ShowConfirmation);
//				if( null == showConfirmationValues )
//					showConfirmationValues = new HashMap<String,String>();
//				
//				genCorrFieldNamesMap = (HashMap<String,String>) result.get(GenerateCorrFieldNames);
//				if( null == genCorrFieldNamesMap )
//					genCorrFieldNamesMap = new HashMap<String,String>();
			}
			
			public void onFailure(Throwable caught) 
			{
				if(showError == true)
					TbitsInfo.write("Following exception Occured while loading Correspondence Module." + caught.getMessage() + "\nPlease try Refreshing.", TbitsInfo.ERROR);
				return;
			}
		});
	}
	
	/*
	public static void initializeViewRequestOptions( final boolean showError)
	{
		CorrConst.corrDBService.getViewRequestParams(new AsyncCallback<HashMap<String,? extends Serializable>>() 
		{
			public void onSuccess(HashMap<String,? extends Serializable> result) 
			{
				CorrConst.transferToAppBas = new ArrayList<String>();
				CorrConst.transferToOptions = (ArrayList<ProtocolOptionEntry>) result.get(CorrConst.TransferToOptions);
				if( null != CorrConst.transferToOptions )
				{
					for( ProtocolOptionEntry ops : CorrConst.transferToOptions )
					{
						CorrConst.transferToAppBas.add(ops.getSysPrefix());
					}
				}
				
				CorrConst.statusFieldName = (HashMap<String, String>) result.get(StatusFieldNames);
				if( null == statusFieldName )
					statusFieldName = new HashMap<String, String>();
			
				CorrConst.sendMeEmail = (HashMap<String, String>) result.get(SendMeEmailBas);
				if( null == sendMeEmail )
					sendMeEmail = new HashMap<String, String>();
				
			}
			
			public void onFailure(Throwable caught) 
			{
				if( showError )
					TbitsInfo.write("Following exception Occured while loading Correspondence Module." + caught.getMessage() + "\nPlease try Refreshing.", TbitsInfo.ERROR);
				return;
			}
		});
	}
	*/

	public static ArrayList<UserClient> getUserClients(String sysPrefix, UserPicker up) throws UserNotFoundException 
	{
		initializeUserMap(); 
		String value = up.getStringValue();
		if( null == value )
			return null;
		
		ArrayList<UserClient> uls = new ArrayList<UserClient>();
		String [] userLogins = value.split("[;, ]");
		for( String ul : userLogins )
		{
			ul = ul.trim();
			UserClient uc = userCache.getObject(ul);
			if( null == uc )
				throw new UserNotFoundException("No user found with user-login = " + ul + " in the field : " + fdn(sysPrefix, up.getName()));
			
			uls.add(uc);
		}
		
		return uls;
	}
}
