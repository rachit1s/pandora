/**
 * 
 */
package com.tbitsGlobal.admin.client.state;

import com.tbitsGlobal.admin.client.events.OnAPAppStateChanged;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;

/**
 * @author dheeru
 * 
 */
public enum AppState{

	UserReceived(1 << 0), 
	BAMapReceived(AppState.UserReceived.value << 1),
	BAChanged(AppState.BAMapReceived.value << 1);

	private int value;

	AppState(int value) {
		this.value = value;
	}

	public int getVal() {
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
			observable.subscribe(OnAPAppStateChanged.class, new ITbitsEventHandle<OnAPAppStateChanged>(){
				public void handleEvent(OnAPAppStateChanged event) {
					if(checkAppStateIsBefore(state)){
						observable.unSubscribe(OnAPAppStateChanged.class, this);
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
		TbitsEventRegister.getInstance().fireEvent(new OnAPAppStateChanged());
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
		ITbitsEventHandle<OnAPAppStateChanged> handle = new ITbitsEventHandle<OnAPAppStateChanged>(){
			public void handleEvent(OnAPAppStateChanged event) {
				if(AppState.checkAppStateIsBefore(state) && observable.unSubscribe(OnAPAppStateChanged.class, this)){
					TbitsEventRegister.getInstance().fireEvent(delayedEvent);
				}
			}};
			observable.subscribe(OnAPAppStateChanged.class, handle);
	}
	
	/**
	 * Delays a event till current app state reach to given app state
	 * @param <T>
	 * @param state
	 * @param delayedEvent
	 */
	public static <T extends TbitsBaseEvent> void delayTillAppStateIsTill(final AppState state, final T delayedEvent){
		ITbitsEventHandle<OnAPAppStateChanged> handle = new ITbitsEventHandle<OnAPAppStateChanged>(){
			public void handleEvent(OnAPAppStateChanged event) {
				if(AppState.checkAppStateIsTill(state) && observable.unSubscribe(OnAPAppStateChanged.class, this)){
					TbitsEventRegister.getInstance().fireEvent(delayedEvent);
				}
			}};
			observable.subscribe(OnAPAppStateChanged.class, handle);
	}

}
