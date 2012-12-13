package commons.com.tbitsGlobal.utils.client.widgets;

import com.extjs.gxt.ui.client.store.ListStore;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public class UserComboPlusPlus extends ComboPlusPlus<UserClient>{
	public UserComboPlusPlus(BAFieldMultiValue baField) {
		super();
		
		this.setDisplayField(UserClient.USER_LOGIN);
		
		this.setTriggerAction(TriggerAction.QUERY);
		this.setMinChars(1);
		this.setMinListWidth(100);
		
		this.setStore(new ListStore<UserClient>(UserPickerListLoader.getloader(baField)));
	}
}
