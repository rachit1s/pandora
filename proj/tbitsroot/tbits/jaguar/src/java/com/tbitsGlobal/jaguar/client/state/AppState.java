package com.tbitsGlobal.jaguar.client.state;

import com.tbitsGlobal.jaguar.client.events.OnAppStateChanged;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;

/**
 * 
 * @author sourabh
 *
 * An enum that holds the current state of application.
 * The Application moves from one state to a higher state one by one. 
 * All the component that are dependent on a particular state listen for {@link OnAppStateChanged} event.
 * The stated have been ordered in a logical manner. 
 * <b>It is a very important component of application. If you don't like it and wish to change it, please discuss first</b>
 */
public enum AppState{
	/*
	 * Define the possible states of the application
	 */
	CurrentUserReceived(1 << 0),									// When the captions have been received				
	CaptionReceived(AppState.CurrentUserReceived.value << 1),	// when the logged in user's info has been retrieved
	BAMapReceived(AppState.CaptionReceived.value << 1),		// when the applicable BAs have been received
	BAChanged(AppState.BAMapReceived.value << 1),				// when A BA has been loaded
	DisplayGroupsReceived(AppState.BAChanged.value << 1),		// When display groups for the BA have been recieved
	FieldsReceived(AppState.DisplayGroupsReceived.value << 1);	// When fields for the BA have been received
	
	private int value;
	
	AppState(int value){
		this.value = value;
	}
	
	public int getVal(){
		return value;
	}
	
	private static TbitsObservable observable = new BaseTbitsObservable();
	static{
		observable.attach();
	}
	
	/**
	 * if (the current state is not more than one level below the given app state){
	 * 		Set the given app state
	 * }else{
	 * 		Wait for the current state to reach one level below the given app state
	 * }
	 * @param state
	 */
	public static void setAppStateAfterCheckState(final AppState state){
		if(checkAppStateIsBefore(state))
			setAppState(state);
		else{
			observable.subscribe(OnAppStateChanged.class, new ITbitsEventHandle<OnAppStateChanged>(){
				public void handleEvent(OnAppStateChanged event) {
					if(checkAppStateIsBefore(state)){
						observable.unSubscribe(OnAppStateChanged.class, this);
						setAppState(state);
					}
				}});
		}
	}

	/**
	 * @param state
	 * @return true if the current app state is not more than one level below given app state
	 */
	public static boolean checkAppStateIsBefore(AppState state){
		if(state.getVal() == 1)
			return true;
		
		int appState = getSummedValue(state);
		return GlobalConstants.appState >= appState/2;
	}

	/**
	 * 
	 * @param state
	 * @return true if the current app state is equal or more than given app state
	 */
	public static boolean checkAppStateIsTill(AppState state){
		int appState = getSummedValue(state);
		return GlobalConstants.appState >= appState;
	}
	
	/**
	 * Sets app state of the application
	 * @param state
	 */
	public static void setAppState(AppState state){
		GlobalConstants.appState = getSummedValue(state);
		TbitsEventRegister.getInstance().fireEvent(new OnAppStateChanged());
	}
	
	/**
	 * @param state
	 * @return The summed up value for the given state
	 */
	private static int getSummedValue(AppState state){
		int appState = state.getVal();
		AppState[] appStates = AppState.values();
		for(AppState s : appStates){
			if(state.getVal() > s.getVal()){
				appState += s.getVal();
			}
		}
		
		return appState;
	}
	
	/**
	 * Delays a event till current app state reach one level below given app state
	 * @param <T>
	 * @param state
	 * @param delayedEvent
	 */
	public static <T extends TbitsBaseEvent> void delayTillAppStateIsBefore(final AppState state, final T delayedEvent){
		ITbitsEventHandle<OnAppStateChanged> handle = new ITbitsEventHandle<OnAppStateChanged>(){
			public void handleEvent(OnAppStateChanged event) {
				if(AppState.checkAppStateIsBefore(state) && observable.unSubscribe(OnAppStateChanged.class, this)){
					TbitsEventRegister.getInstance().fireEvent(delayedEvent);
				}
			}};
			observable.subscribe(OnAppStateChanged.class, handle);
	}
}