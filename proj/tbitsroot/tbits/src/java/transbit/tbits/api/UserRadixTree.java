package transbit.tbits.api;

import java.util.Date;
import java.util.List;
import java.util.Random;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.User;
import ds.tree.DuplicateKeyException;
import ds.tree.RadixTreeImpl;

/**
 * 
 * @author sourabh
 * 
 * The radix tree to store all the active users.
 * 
 * The following keys are maintained : 
 * 1. email
 * 2. user login
 * 3. display name
 * 4. first name
 * 5. last name
 * 
 * All the matches are case insensitive
 * 
 * In case of 3,4,5 mutiple keys are maintained when containing white spaces
 * e.g. display_name = "Vinod M Srinivas" will match for prefixes "v", "m", "s", etc...
 */
public class UserRadixTree extends RadixTreeImpl<Integer>{
	
	public static TBitsLogger LOG	= TBitsLogger.getLogger("commons.com.tbitsGlobal.utils.server");
	
	private static UserRadixTree tree;
	
	private UserRadixTree() {
		super();
		
		this.refresh();
	}
	
	public static UserRadixTree getInstance(){
		if(tree == null)
			tree = new UserRadixTree();
		return tree;
	}
	
	public static void createNewInstance(){
		tree = new UserRadixTree();
	}
	
	protected void refresh(){
		Date start = new Date();
		
		List<User> users = User.getActiveUsers();
		for(User user : users){
			
			String key = user.getEmail();
			if(key != null)
				this.insert(key.toLowerCase(), user.getUserId());		// search for email
			
			key = user.getUserLogin();
			if(key != null)
				this.insert(key.toLowerCase(), user.getUserId());	// search for login
			
			String displayName = user.getDisplayName();				// search for display name
			if(displayName != null)
			{
				String[] tokens = displayName.toLowerCase().split(" ");
				for (String token : tokens) {
					this.insert(token, user.getUserId());
				}
			}
			
			String firstName = user.getFirstName();					// search for first name
			if(firstName != null)
			{
				String[] tokens = firstName.toLowerCase().split(" ");
				for(String token : tokens){
					this.insert(token, user.getUserId());
				}
			}
			
			String lastName = user.getLastName();					// search for last name
			if(lastName != null)
			{
				String[] tokens = lastName.toLowerCase().split(" ");
				for(String token : tokens){
					this.insert(token, user.getUserId());
				}
			}
		}
		
		Date end = new Date();
		
		LOG.info("Time taken to make radix tree : " + (end.getTime() - start.getTime()) + " milliseconds");
	}
	
	@Override
	public void insert(String key, Integer value) {
		try{
			if(key.equals("") || key.equals(" "))
				return;
			super.insert(key, value);
		}catch(DuplicateKeyException e){
//			LOG.info("",(e));
			
			Random r = new Random();
			int n = r.nextInt();
			
			key += n;
			insert(key, value);
		}
	}
}
