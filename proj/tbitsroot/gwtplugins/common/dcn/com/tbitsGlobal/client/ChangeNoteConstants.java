/**
 * 
 */
package dcn.com.tbitsGlobal.client;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;

import dcn.com.tbitsGlobal.client.service.ChangeNoteService;
import dcn.com.tbitsGlobal.client.service.ChangeNoteServiceAsync;
import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;


/**
 * @author Lokesh
 *
 */
public class ChangeNoteConstants {
	public static final ChangeNoteServiceAsync dcnService = GWT.create(ChangeNoteService.class);
	public static ArrayList<ChangeNoteConfig> changeNoteConfigList = new ArrayList<ChangeNoteConfig>();
}
