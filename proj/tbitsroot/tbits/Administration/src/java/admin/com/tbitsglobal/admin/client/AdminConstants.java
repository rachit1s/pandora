package admin.com.tbitsglobal.admin.client;

import com.google.gwt.core.client.GWT;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public interface AdminConstants {
	public static final AdminDBServiceAsync dbService = GWT
			.create(AdminDBService.class);

	public static UserClient currentUser = null;

	public static String TRN_PROCESS_ID_DB	=	"trn_process_id";
}
