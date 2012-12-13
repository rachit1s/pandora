package transbit.tbits.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

/**
 * 
 * @author nitiraj
 *
 */
/**
 * Plugins can implement this interface if they want to be executed before composing mail for any user 
 */
public interface IMailPreProcessor 
{
	/**
	 * 
	 * @param user : the cloned user to whome this mail will be sent. Be careful. Don't change. Changing this will not affect the original User to whome the mail will be sent.
	 * 
	 * @param request : the cloned request for which this mail will be sent. ( Although Discouraged ) Any changes in 
	 * 					this Request object will appear in the Header values of the email for this user.
	 * 
	 * @param actionList : the cloned ArrayList of all the action for which this mail will be sent. ( Although Discouraged ) Any changes in 
	 * 					   these Actions will appear in the Action Details values of the email for this user.
	 * 
	 * @param actionFileHash : the cloned files ( attachments ) that will be sent with this mail. Plugin can change
	 * the priority field of each ActionFileInfo. 
	 * Rule for files corresponding to the maxActionId : any file with negative priority will always be sent as link.
	 * Rest files will be arraged in decreasing order of their priority and the system will try to attach each file
	 * to the email. If attaching the files is not possible a link will be sent instead. 
	 * Rule for files of previous Actions : They will all appear as link.
	 * 
	 * (Discouraged)Attachements will be sent to this user according to this Hashtable, so if a ActionFileInfo object is changed / removed
	 * then it will affect the email.
	 *  
	 * @param permissions : cloned permissions of the User for this request. Plugin can change the permission of a field to affect email format. Permissions.EMAIL_VIEW permission is 
	 * used for deciding whether the field should be visible in email or not. Applicable for Attachement types too.
	 */
	public void executeMailPreProcessor(User user, Request request, ArrayList<Action> actionList, Hashtable<Integer,Collection<ActionFileInfo>> actionFileHash, Hashtable<String,Integer>permissions );
	public String getMailPreProcessorName();
	public double getMailPreProcessorOrder();
}
