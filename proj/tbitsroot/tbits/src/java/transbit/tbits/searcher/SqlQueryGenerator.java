package transbit.tbits.searcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.dql.treecomponents.Constraint;
import transbit.tbits.dql.treecomponents.DqlConstants;
import transbit.tbits.dql.treecomponents.Parameter;
import transbit.tbits.dql.treecomponents.Value;
import transbit.tbits.dql.treecomponents.DqlConstants.ParamType;

/**
 * <b>BEWARNED : HEAVY USAGE OF MS SQL!</b>
 * <br>
 * This class generates SQL queries for the searcher. 
 * The queries to be generated can be on any of the following sets :<br>
 * 	* requests, request_users, requests_ex<br>
 * 	* actions, action_users, actions_ex<br>
 * <br>
 * The generator instance contains the sqls on the basic, users and ex tables.<br>
 * <br>
 * The class also contains various static functions that modify a given sql query for certain cases.
 * 
 * @author Karan Gupta
 *
 */
public class SqlQueryGenerator {
	
	//====================================================================================
	
	// Generator specific variables
	private SqlConditionEncapsulator basic;
//	private SqlConditionEncapsulator ex;
	
	private String queryOnEx;
	private DqlConstants.Operator operatorOnEx;
	private String queryOnUsers;
	private DqlConstants.Operator operatorOnUsers;
	private String queryOnTags;
	private DqlConstants.Operator operatorOnTags;
	private String queryOnRelatedRequests;
	private DqlConstants.Operator operatorOnRelatedRequests;
	private String queryOnVersions;
	private DqlConstants.Operator operatorOnVersions;
	private String queryOnReadUnread;
	private DqlConstants.Operator operatorOnReadUnread;
	private int sysId;
	private int userId;
	private int type;
	// Types of generators
	public static final int QUERY_REQUESTS = 1;
	public static final int QUERY_ACTIONS = 2;
	
	//====================================================================================

	/**
	 * Constructor
	 * 
	 * @param type
	 */
	public SqlQueryGenerator(int sysId, int userId, int type){
		
		this.type = type;
		this.userId = userId;
		this.sysId = sysId;
		basic = new SqlConditionEncapsulator(getDefaultQueryForBasic(sysId));
		queryOnEx = null;
		queryOnTags = null;
		queryOnUsers = null;
		queryOnVersions = null;
		queryOnRelatedRequests = null;
		queryOnReadUnread = null;
	}

	//====================================================================================

	/**
	 * Append the condition for the given constraint on the relevant table
	 * 
	 * @param f
	 * @param c
	 * @throws Exception 
	 */
	public void appendCondition(Field f, Constraint c) throws Exception {

		if(f == null){
			if(c.getField().equals(Searcher.PUBLIC_TAGS_FIELD_FILTER) || c.getField().equals(Searcher.PRIVATE_TAGS_FIELD_FILTER)){
				addToTagsQuery(c);
			}
			else if(c.getField().equals(Searcher.READ_UNREAD_FIELD_FILTER)){
				
				addToReadUnreadQuery(c);
			}
			else
				throw new Exception("Unable to handle null field to append condition!");
		}
		else{
			if(f.getName().equals(Field.RELATED_REQUESTS)){
				addToLinkedRequestsQuery(c);
			}
			else if(f.getDataTypeId()==DataType.ATTACHMENTS){
				addToVersionQuery(c);
			}
			// Append a user condition
			else if(f.getDataTypeId()==DataType.USERTYPE){
				// HACK for last updated by
				if(f.getName().equals("user_id"))
					addToBasicQuery(f, c);
				else
					addToUserQuery(f, c);
			}
			else if(f.getIsExtended()){
				addToExtendedFieldQuery(f, c);
			}
			else {
				addToBasicQuery(f, c);
			}
		}
	}
	
	private void addToBasicQuery(Field f, Constraint c) throws Exception{
		String conditions = "";
		conditions = getFixedFieldConditions(f, c);
		basic.appendCondition(conditions, c.getOperator());
		basic.addConstraint(c);
	}
	
	private void addToReadUnreadQuery(Constraint c) throws Exception{
		
		String queryOnReadUnread = "";
		
		if(c.getValues().size() > 1)
			throw new Exception("Valueset not valid for Read/Unread.");
		
		Value val = c.getValues().get(0);
			
		if(val.getParams().size() > 1)
			throw new Exception("Paramset not valid for Read/Unread.");
		Parameter parameter = val.getParams().get(0);
		if(!parameter.type.equals(ParamType.BOOLEAN))
			throw new Exception("Only Boolean values valid for Read/Unread.");
			
		queryOnReadUnread += 	getDefaultQueryForReadUnread(sysId, userId) + " and ura.action_id=r.max_action_id ";
		
		if(parameter.param.equals("0")){
			// except for unread
			queryOnReadUnread = "(select sys_id, request_id from requests where sys_id="+sysId+")\n" + 
								"except\n" + 
								"("+queryOnReadUnread+")";
		}
		
		if(this.queryOnReadUnread == null){
			this.queryOnReadUnread = queryOnReadUnread;
			this.operatorOnReadUnread = c.getOperator();
		}
		else{
			if(c.getOperator().equals(DqlConstants.Operator.AND)){
				this.queryOnReadUnread = 	"select rur1.* from \n(" + this.queryOnReadUnread + ")rur1\njoin (" + queryOnReadUnread +")rur2\n" +
											"on rur1.sys_id=rur2.sys_id and rur1.request_id=rur2.request_id";
			}
			else if(c.getOperator().equals(DqlConstants.Operator.OR)){
				this.queryOnReadUnread = "(" + this.queryOnReadUnread + ")\nunion\n(" + queryOnReadUnread +")";
			}
		}
	}

	private void addToExtendedFieldQuery(Field f, Constraint c) throws Exception {
		
		String conditions = getExtendedFieldConditions(f, c);
		String queryOnEx = getDefaultQueryForEx(sysId) + " and " + conditions;
		
		if(this.queryOnEx == null){
			this.queryOnEx = queryOnEx;
			this.operatorOnEx = c.getOperator();
		}
		else{
			if(c.getOperator().equals(DqlConstants.Operator.AND)){
				this.queryOnEx = 	"select ex1.* from \n(" + this.queryOnEx + ")ex1\njoin (" + queryOnEx +")ex2\n" +
									"on ex1.sys_id=ex2.sys_id and ex1.request_id=ex2.request_id" +
									((this.type == QUERY_ACTIONS)?" and ex1.action_id=ex2.action_id":"");
			}
			else if(c.getOperator().equals(DqlConstants.Operator.OR)){
				this.queryOnEx = "(" + this.queryOnEx + ")\nunion\n(" + queryOnEx +")";
			}
		}
		
	}

	private void addToUserQuery(Field f, Constraint c) throws Exception {
		
		String conditions = "";
		ArrayList<Value> nullVals = new ArrayList<Value>();
		for(Value val : c.getValues()){
			if(val.getParams().size() == 1 && val.getParams().get(0).type.equals(ParamType.NULL))
				nullVals.add(val);
			else{
				if(!conditions.equals("")){
					if(val.getOperator().equals(DqlConstants.Operator.OR))
						conditions += " or ";
					else if(val.getOperator().equals(DqlConstants.Operator.AND))
						conditions += " and ";
				}
				String parameters = "";
				for(Parameter parameter : val.getParams()){
					if(parameter.type.equals(ParamType.NULL))
						throw new Exception("NULL parameter encountered in parameter set!");
					if(!parameters.equals("") )
						parameters += " or ";
					// Escape ms sql specific special characters
					int searchFrom = 0;
					int specialIndex = 0;
					while((specialIndex = parameter.param.indexOf("'", searchFrom)) >= 0){
						parameter.param  = parameter.param.substring(0, specialIndex) + "'" + parameter.param.substring(specialIndex);
						searchFrom = specialIndex + 2;
					}
					
					parameters += "user_id " + parameter.getComparator() + " (select user_id from users where user_login = '" + parameter.param + "')";
				}
				conditions += "(" + parameters + ")";
			}
		}
		String queryOnUsers = "";
		if(!conditions.equals("")){
			conditions = "field_id = " + f.getFieldId() + " and (" + conditions + ")";
			queryOnUsers = getDefaultQueryForUsers(sysId) + " and " + conditions;
		}
		
		for(Value val : nullVals){
			String nullQuery = 	getDefaultQueryForBasic(sysId) + "\nexcept\n" +
								getDefaultQueryForUsers(sysId) + " and field_id = " + f.getFieldId();
			if(!queryOnUsers.equals("")){
				if(val.getOperator().equals(DqlConstants.Operator.OR))
					queryOnUsers = "(" + queryOnUsers + ")\nunion\n(" + nullQuery +")";
				else
					queryOnUsers =	"select u1.* from \n(" + queryOnUsers + ")u1\njoin (" + nullQuery +")u2\n" +
									"on u1.sys_id=u2.sys_id and u1.request_id=u2.request_id" +
									((this.type == QUERY_ACTIONS)?" and u1.action_id=u2.action_id":"");
			}
			else
				queryOnUsers = nullQuery;
		}
		
		if(this.queryOnUsers == null){
			this.queryOnUsers = queryOnUsers;
			this.operatorOnUsers = c.getOperator();
		}
		else{
			if(c.getOperator().equals(DqlConstants.Operator.AND)){
				this.queryOnUsers = 	"select u1.* from \n(" + this.queryOnUsers + ")u1\njoin (" + queryOnUsers +")u2\n" +
									"on u1.sys_id=u2.sys_id and u1.request_id=u2.request_id" +
									((this.type == QUERY_ACTIONS)?" and u1.action_id=u2.action_id":"");
			}
			else if(c.getOperator().equals(DqlConstants.Operator.OR)){
				this.queryOnUsers = "(" + this.queryOnUsers + ")\nunion\n(" + queryOnUsers +")";
			}
		}
		
	}

	private void addToTagsQuery(Constraint c) throws Exception {
		
		String conditions = "";
		for(Value val : c.getValues()){
			if(!conditions.equals("")){
				if(val.getOperator().equals(DqlConstants.Operator.OR))
					conditions += " or ";
				else if(val.getOperator().equals(DqlConstants.Operator.AND))
					conditions += " and ";
			}
			String parameters = "";
			for(Parameter parameter : val.getParams()){
				if(parameter.type.equals(ParamType.NULL))
					throw new Exception("Null value not valid for tags");
				if(!parameters.equals(""))
					parameters += " or ";
				// Escape ms sql specific special characters
				int searchFrom = 0;
				int specialIndex = 0;
				while((specialIndex = parameter.param.indexOf("'", searchFrom)) >= 0){
					parameter.param  = parameter.param.substring(0, specialIndex) + "'" + parameter.param.substring(specialIndex);
					searchFrom = specialIndex + 2;
				}
				
				parameters += 	"tag_id " + parameter.getComparator() + " (select tag_id from tags_definitions where" +
								" user_id = " + ((c.getField().equals(Searcher.PUBLIC_TAGS_FIELD_FILTER))?-1:userId) + 
								" and name = '" + parameter.param + "')";
			}
			conditions += "(" + parameters + ")";
		}
		
		if(this.queryOnTags == null){
			this.queryOnTags = getDefaultQueryForTags(sysId) + " and " + conditions;
			this.operatorOnTags = c.getOperator();
		}
		else{
			if(c.getOperator().equals(DqlConstants.Operator.AND)){
				this.queryOnTags += " and " + conditions;
			}
			else if(c.getOperator().equals(DqlConstants.Operator.OR)){
				this.queryOnTags += " or " + conditions;
			}
		}
	}
	
	private void addToLinkedRequestsQuery(Constraint c) throws Exception {
		
		String conditions = "";
		for(Value val : c.getValues()){
			if(!conditions.equals("")){
				if(val.getOperator().equals(DqlConstants.Operator.OR))
					conditions += " or ";
				else if(val.getOperator().equals(DqlConstants.Operator.AND))
					conditions += " and ";
			}
			String parameters = "";
			for(Parameter parameter : val.getParams()){
				if(parameter.type.equals(ParamType.NULL))
					throw new Exception("Null value not valid for related requests");
				if(!parameters.equals(""))
					parameters += " or ";
				// Escape ms sql specific special characters
				int searchFrom = 0;
				int specialIndex = 0;
				while((specialIndex = parameter.param.indexOf("'", searchFrom)) >= 0){
					parameter.param  = parameter.param.substring(0, specialIndex) + "'" + parameter.param.substring(specialIndex);
					searchFrom = specialIndex + 2;
				}
				
				//Check for # for first kind of sys_prefix#request_id kind of input
				String relatedSysPrefix = null;
				int relatedRequestId = 0;
				try{
					if(parameter.param.indexOf("#") >= 0){
						StringTokenizer st = new StringTokenizer(parameter.param, "#");
						relatedSysPrefix = st.nextToken();
						relatedRequestId = Integer.parseInt(st.nextToken());
						if(st.hasMoreTokens())
							throw new Exception("Invalid related request format! Correct usage \"sys_prefix#request_id\" or request_id");
					}
					else{
						relatedRequestId = Integer.parseInt(parameter.param);
					}
				}
				catch(NumberFormatException e){
					throw new Exception("Invalid related request format! Correct usage \"sys_prefix#request_id\" or request_id");
				}
					
				
				parameters += 	((relatedSysPrefix == null)?"":
								"(related_sys_id = (select sys_id from business_areas where sys_prefix='"+relatedSysPrefix+"') and ")
								+"related_request_id = " + relatedRequestId + ((relatedSysPrefix == null)?"":")");
			}
			conditions += "(" + parameters + ")";
		}
		
		if(this.queryOnRelatedRequests == null){
			this.queryOnRelatedRequests = getDefaultQueryForRelatedRequests(sysId) + " and " + conditions;
			this.operatorOnRelatedRequests = c.getOperator();
		}
		else{
			if(c.getOperator().equals(DqlConstants.Operator.AND)){
				this.queryOnRelatedRequests += " and " + conditions;
			}
			else if(c.getOperator().equals(DqlConstants.Operator.OR)){
				this.queryOnRelatedRequests += " or " + conditions;
			}
		}
	}

	private void addToVersionQuery(Constraint c) throws Exception {
		
		String queryOnVersions = null;
		
		for(Value v : c.getValues()){
			String parameters = "";
			String toBeAdded = "";
			
			// handle null query
			if(v.getParams().size() == 1 && v.getParams().get(0).type.equals(ParamType.NULL)){
				toBeAdded = "select distinct sys_id, request_id, action_id from actions\n" + 
							"except\n" + 
							"select distinct sys_id, request_id, action_id from versions where file_id<>0 and file_action<>'D'";
			}
			else{
				for(Parameter parameter : v.getParams()){
					String paramStr = "";
					if(parameter.type.equals(ParamType.NULL)){
						// TODO maybe an exception should be thrown in case of NULL in paramList
						paramStr = "''";
					}
					else if(parameter.type.equals(ParamType.STRING)){
						// Escape ms sql specific special characters
						int searchFrom = 0;
						int specialIndex = 0;
						while((specialIndex = parameter.param.indexOf("'", searchFrom)) >= 0){
							parameter.param  = parameter.param.substring(0, specialIndex) + "'" + parameter.param.substring(specialIndex);
							searchFrom = specialIndex + 2;
						}
						
						paramStr = "'" + parameter.param + "'";
					}
					else
						throw new Exception("Attachment field can take only string values!");
					if(!parameters.equals(""))
						parameters += " or ";
					parameters += "attachment" + parameter.getComparator() + paramStr;
				}
				
				toBeAdded = 	"select distinct v.sys_id, v.request_id, v.action_id from versions v\n"+
								"join (select sys_id, request_id, action_id " +
								"from versions where file_id<>0 and file_action<>'D' and (" + parameters + ")) as aa\n"+
								"on v.sys_id = aa.sys_id and v.request_id = aa.request_id\n"+
								"and v.action_id = (select max(action_id) from versions where sys_id=v.sys_id and request_id=v.request_id)";
			}
			
			if(queryOnVersions == null){
				queryOnVersions = toBeAdded;
			}
			else{
				if(v.getOperator() == null || v.getOperator().equals(DqlConstants.Operator.AND)){
					queryOnVersions = 	"select v1.* from (" + queryOnVersions + ")v1\njoin (" + toBeAdded +")v2\n" +
										"on v1.sys_id=v2.sys_id and v1.request_id=v2.request_id and v1.action_id=v2.action_id";
				}
				else if(v.getOperator().equals(DqlConstants.Operator.OR)){
					queryOnVersions = "(" + queryOnVersions + ")\nunion\n(" + toBeAdded +")";
				}
			}
		}
		
		if(this.queryOnVersions == null){
			this.queryOnVersions = queryOnVersions;
			this.operatorOnVersions = c.getOperator();
		}
		else{
			if(c.getOperator().equals(DqlConstants.Operator.AND)){
				this.queryOnVersions = 	"select v1.* from (" + this.queryOnVersions + ")v1\njoin (" + queryOnVersions +")v2\n" +
									"on v1.sys_id=v2.sys_id and v1.request_id=v2.request_id and v1.action_id=v2.action_id";
			}
			else if(c.getOperator().equals(DqlConstants.Operator.OR)){
				this.queryOnVersions = "(" + this.queryOnVersions + ")\nunion\n(" + queryOnVersions +")";
			}
		}
			
	}

	//====================================================================================

	/**
	 * Form and return the query related to this generator.
	 * @return
	 */
	public String getQuery() {
		
		String sql = "";
		
		// Basic query to search for the relevant request ids.
		sql += "\nfrom (\n\t" + basic.getAndSql() + ((basic.orConstraints > 0)?"\nunion \n\t" + basic.getOrSql():"") + ") basic ";
		
		if(queryOnEx != null){
			if(operatorOnEx == null || operatorOnEx.equals(DqlConstants.Operator.AND))
				sql += 	"\njoin (\n\t" + queryOnEx + ")ex \n" +
						"on basic.sys_id=ex.sys_id and basic.request_id=ex.request_id " + ((type==QUERY_ACTIONS)?"and basic.action_id=ex.action_id":"");
			else if(operatorOnEx.equals(DqlConstants.Operator.OR))
				sql += 	"\nleft join (\n\t" + queryOnEx + ")ex \n" +
						"on basic.sys_id=ex.sys_id and basic.request_id=ex.request_id " + ((type==QUERY_ACTIONS)?"and basic.action_id=ex.action_id":"");
		}
		
		if(queryOnUsers != null){
			if(operatorOnUsers == null || operatorOnUsers.equals(DqlConstants.Operator.AND))
				sql += 	"\njoin (\n\t" + queryOnUsers + ")u \n" +
						"on basic.sys_id=u.sys_id and basic.request_id=u.request_id " + ((type==QUERY_ACTIONS)?"and basic.action_id=u.action_id":"");
			else if(operatorOnUsers.equals(DqlConstants.Operator.OR))
				sql += 	"\nleft join (\n\t" + queryOnUsers + ")u \n" +
						"on basic.sys_id=u.sys_id and basic.request_id=u.request_id " + ((type==QUERY_ACTIONS)?"and basic.action_id=u.action_id":"");
		}
		
		if(queryOnReadUnread != null){
			if(operatorOnReadUnread == null || operatorOnReadUnread.equals(DqlConstants.Operator.AND))
				sql += 	"\njoin (\n\t" + queryOnReadUnread + ")rur \n" +
						"on basic.sys_id=rur.sys_id and basic.request_id=rur.request_id ";
			else if(operatorOnReadUnread.equals(DqlConstants.Operator.OR))
				sql += 	"\nleft join (\n\t" + queryOnReadUnread + ")rur \n" +
						"on basic.sys_id=rur.sys_id and basic.request_id=rur.request_id ";
		}
		
		if(queryOnTags != null){
			if(operatorOnTags == null || operatorOnTags.equals(DqlConstants.Operator.AND))
				sql += 	"\njoin (\n\t" + queryOnTags + ")t \n" +
						"on basic.sys_id=t.sys_id and basic.request_id=t.request_id ";
			else if(operatorOnTags.equals(DqlConstants.Operator.OR))
				sql += 	"\nleft join (\n\t" + queryOnTags + ")t \n" +
						"on basic.sys_id=t.sys_id and basic.request_id=t.request_id ";
		}
		
		if(queryOnVersions != null){
			if(operatorOnVersions == null || operatorOnVersions.equals(DqlConstants.Operator.AND))
				sql += 	"\njoin (\n\t" + queryOnVersions + ")v \n" +
						"on basic.sys_id=v.sys_id and basic.request_id=v.request_id " + ((type==QUERY_ACTIONS)?"and basic.action_id=v.action_id":"");
			else if(operatorOnVersions.equals(DqlConstants.Operator.OR))
				sql += 	"\nleft join (\n\t" + queryOnVersions + ")v \n" +
						"on basic.sys_id=v.sys_id and basic.request_id=v.request_id " + ((type==QUERY_ACTIONS)?"and basic.action_id=v.action_id":"");
		}
		
		if(queryOnRelatedRequests != null){
			if(operatorOnRelatedRequests == null || operatorOnRelatedRequests.equals(DqlConstants.Operator.AND))
				sql += 	"\njoin (\n\t" + queryOnRelatedRequests + ")rereq \n" +
						"on basic.sys_id=rereq.sys_id and basic.request_id=rereq.request_id ";
			else if(operatorOnRelatedRequests.equals(DqlConstants.Operator.OR))
				sql += 	"\nleft join (\n\t" + queryOnRelatedRequests + ")rereq \n" +
						"on basic.sys_id=rereq.sys_id and basic.request_id=rereq.request_id ";
		}
		return sql;
	}

	//====================================================================================

	// Default queries for the tables
	
	/**
	 * @param sysId
	 * @return default query for the basic table (requests/actions)
	 */
	public String getDefaultQueryForBasic(int sysId){
		
		String defaultQuery;
		if(type == QUERY_REQUESTS)
			defaultQuery = "select sys_id, request_id from requests";
		else 
			defaultQuery = "select sys_id, request_id, action_id from actions";
		if(sysId > 0){
			defaultQuery += " where " + Field.BUSINESS_AREA + " = " + sysId;
		}
		return defaultQuery;
	}
	
	/**
	 * @param sysId
	 * @return default query for the ex table (requests_ex/actions_ex)
	 */
	public String getDefaultQueryForEx(int sysId){
		
		String defaultQuery;
		if(type == QUERY_REQUESTS)
			defaultQuery = "select sys_id, request_id from requests_ex";
		else 
			defaultQuery = "select sys_id, request_id, action_id from actions_ex";
		if(sysId > 0){
			defaultQuery += " where " + Field.BUSINESS_AREA + " = " + sysId;
		}
		return defaultQuery;
	}
	
	/**
	 * @param sysId
	 * @return default query for the users table (request_users/action_users)
	 */
	public String getDefaultQueryForUsers(int sysId){
		
		String defaultQuery;
		if(type == QUERY_REQUESTS)
			defaultQuery = "select sys_id, request_id from request_users";
		else 
			defaultQuery = "select sys_id, request_id, action_id from action_users";
		if(sysId > 0){
			defaultQuery += " where " + Field.BUSINESS_AREA + " = " + sysId;
		}
		return defaultQuery;
	}
	
	/**
	 * 
	 * @param sysId
	 * @return default query for tags table (tags_requests)
	 */
	public String getDefaultQueryForTags(int sysId){
		String defaultQuery;
		defaultQuery = "select sys_id, request_id from tags_requests";
		if(sysId > 0){
			defaultQuery += " where " + Field.BUSINESS_AREA + " = " + sysId;
		}
		return defaultQuery;
	}
	
	/**
	 * 
	 * @param sysId
	 * @return default query for related requests table (related_requests)
	 */
	public String getDefaultQueryForRelatedRequests(int sysId){
		String defaultQuery;
		defaultQuery = "select primary_sys_id as sys_id, primary_request_id as request_id from related_requests";
		if(sysId > 0){
			defaultQuery += " where primary_sys_id = " + sysId;
		}
		return defaultQuery;
	}
	
	public String getDefaultQueryForReadUnread(int sysId, int userId){
		String defaultQuery;
		defaultQuery =	"select ura.sys_id, ura.request_id from user_read_actions ura\n" + 
						"join requests r on ura.sys_id=r.sys_id and ura.request_id=r.request_id\n" + 
						"and ura.user_id="+userId;
		if(sysId > 0){
			defaultQuery += " and ura.sys_id="+sysId;
		}
		return defaultQuery;
	}
	
	//====================================================================================

	// Utility functions to generate the queries
	
	/**
	 * Get the formatted condition for query on the fixed fields in the requests table.
	 * 
	 * @param f
	 * @param c
	 * @return Condition to be apended to the query
	 * @throws Exception 
	 */
	private String getFixedFieldConditions(Field f, Constraint c) throws Exception {
		
		String conditions = "";
		for(Value val : c.getValues()){
			
			if(!conditions.equals("")){
				if(val.getOperator().equals(DqlConstants.Operator.OR))
					conditions += " or ";
				else if(val.getOperator().equals(DqlConstants.Operator.AND))
					conditions += " and ";
			}
			
			if(f.getDataTypeId() == DataType.USERTYPE){
				String parameters = "";
				for(Parameter parameter : val.getParams()){
					if(parameter.type.equals(ParamType.NULL))
						throw new Exception("NULL parameter encountered in parameter set!");
					if(!parameters.equals("") )
						parameters += " or ";
					// Escape ms sql specific special characters
					int searchFrom = 0;
					int specialIndex = 0;
					while((specialIndex = parameter.param.indexOf("'", searchFrom)) >= 0){
						parameter.param  = parameter.param.substring(0, specialIndex) + "'" + parameter.param.substring(specialIndex);
						searchFrom = specialIndex + 2;
					}
					
					parameters += f.getName() + parameter.getComparator() + " (select user_id from users where user_login = '" + parameter.param + "')";
				}
				conditions += "(" + parameters + ")";
			}
			else if(f.getDataTypeId() == DataType.TYPE){
				String parameters = "";
				for(Parameter parameter : val.getParams()){
					if(!parameters.equals(""))
						parameters += " or ";
					
					if(parameter.type.equals(ParamType.NULL)){
						throw new Exception("Null value not valid for field : " + f.getName());
					}
					try {
						Type t = Type.lookupAllBySystemIdAndFieldNameAndTypeName(f.getSystemId(), f.getName(), parameter.param);
						if(t == null)
							throw new Exception("No such type value exists! Please check. " + f.getName() + " : \"" + parameter.param + "\"");
							
						parameters += f.getName() + parameter.getComparator() + t.getTypeId();
					} 
					catch (DatabaseException e) {
						e.printStackTrace();
					}
				}
				conditions += "(" + parameters + ")";
			}
			else if(f.getDataTypeId() == DataType.STRING){
				String parameters = "";
				for(Parameter parameter : val.getParams()){
					if(!parameters.equals(""))
						parameters += " or ";
					
					if(parameter.type.equals(ParamType.NULL)){
						throw new Exception("Null value not valid for field : " + f.getName());
					}
					
					// Escape ms sql specific special characters
					int searchFrom = 0;
					int specialIndex = 0;
					while((specialIndex = parameter.param.indexOf("'", searchFrom)) >= 0){
						parameter.param  = parameter.param.substring(0, specialIndex) + "'" + parameter.param.substring(specialIndex);
						searchFrom = specialIndex + 2;
					}
					searchFrom = 0;
					specialIndex = 0;
					ArrayList<String> specials = new ArrayList<String>();
					specials.add("%");
					specials.add("_");
					specials.add("[");
					for(String special : specials){
						while((specialIndex = parameter.param.indexOf(special, searchFrom)) >= 0){
							parameter.param  = parameter.param.substring(0, specialIndex) + "\\" + parameter.param.substring(specialIndex);
							searchFrom = specialIndex + 1 + special.length();
						}
					}
					
					parameters += f.getName() + " like '%"+parameter.param+"%' {escape '\\'}";
				}
				
				conditions += "(" + parameters + ")";
			}
			else{
				String parameters = "";
				for(Parameter parameter : val.getParams()){
					if(!parameters.equals(""))
						parameters += " or ";
					
					if(parameter.type.equals(ParamType.NULL)){
						parameters += f.getName() + " IS NULL";
					}
					else if(parameter.type.equals(ParamType.DATETIME) || parameter.type.equals(ParamType.STRING)){
						String valStr = parameter.param;
						
						if(parameter.type.equals(ParamType.DATETIME)){
							valStr = Parameter.getDateTimeValue(valStr);
						}
						
						// Escape ms sql specific special characters
						int searchFrom = 0;
						int specialIndex = 0;
						while((specialIndex = valStr.indexOf("'", searchFrom)) >= 0){
							valStr  = valStr.substring(0, specialIndex) + "'" + valStr.substring(specialIndex);
							searchFrom = specialIndex + 2;
						}	
						valStr = "'"+valStr+"'";
						parameters += f.getName() + parameter.getComparator() + valStr;
					}
					else{
						String valStr = parameter.param;
						
						Parameter toEvaluate = Parameter.determineTypeAndValue(valStr);
						if(toEvaluate == null)
							toEvaluate = parameter;
						
						if(toEvaluate.type == ParamType.DATETIME || toEvaluate.type == ParamType.STRING){
							// Escape ms sql specific special characters
							int searchFrom = 0;
							int specialIndex = 0;
							while((specialIndex = toEvaluate.param.indexOf("'", searchFrom)) >= 0){
								toEvaluate.param  = toEvaluate.param.substring(0, specialIndex) + "'" + toEvaluate.param.substring(specialIndex);
								searchFrom = specialIndex + 2;
							}
							
							valStr = "'"+toEvaluate.param+"'";
						}
						parameters += f.getName() + parameter.getComparator() + valStr;
					}
				}
				
				conditions += "(" + parameters + ")";
			}
		}
		
		return conditions;
	}

	/**
	 * Get the formatted condition for query on the extended fields in the requests_ex table.
	 * 
	 * @param f
	 * @param c
	 * @return Condition to be apended to the query
	 * @throws Exception 
	 */
	private String getExtendedFieldConditions(Field f, Constraint c) throws Exception {

		String conditions = "";
		for(Value val : c.getValues()){
			if(!conditions.equals("")){
				if(val.getOperator().equals(DqlConstants.Operator.OR))
					conditions += " or ";
				else if(val.getOperator().equals(DqlConstants.Operator.AND))
					conditions += " and ";
			}
			
			if(f.getDataTypeId() == DataType.TYPE){
				String parameters = "";
				for(Parameter parameter : val.getParams()){
					if(!parameters.equals(""))
						parameters += " or ";
					
					if(parameter.type.equals(ParamType.NULL)){
						throw new Exception("Null value not valid for field : " + f.getName());
					}
					try {
						Type t = Type.lookupAllBySystemIdAndFieldNameAndTypeName(f.getSystemId(), f.getName(), parameter.param);
						if(t == null)
							throw new Exception("No such type value exists! Please check. " + f.getName() + " : \"" + parameter.param + "\"");
							
						parameters += "type_value" + parameter.getComparator() + t.getTypeId();
					} 
					catch (DatabaseException e) {
						e.printStackTrace();
					}
				}
				
				conditions += "(" + parameters + ")";
			}
			else if(f.getDataTypeId() == DataType.STRING){
				String parameters = "";
				for(Parameter parameter : val.getParams()){
					if(!parameters.equals(""))
						parameters += " or ";
					
					if(parameter.type.equals(ParamType.NULL)){
						throw new Exception("Null value not valid for field : " + f.getName());
					}
					
					// Escape ms sql specific special characters
					int searchFrom = 0;
					int specialIndex = 0;
					while((specialIndex = parameter.param.indexOf("'", searchFrom)) >= 0){
						parameter.param  = parameter.param.substring(0, specialIndex) + "'" + parameter.param.substring(specialIndex);
						searchFrom = specialIndex + 2;
					}
					searchFrom = 0;
					specialIndex = 0;
					ArrayList<String> specials = new ArrayList<String>();
					specials.add("%");
					specials.add("_");
					specials.add("[");
					for(String special : specials){
						while((specialIndex = parameter.param.indexOf(special, searchFrom)) >= 0){
							parameter.param  = parameter.param.substring(0, specialIndex) + "\\" + parameter.param.substring(specialIndex);
							searchFrom = specialIndex + 1 + special.length();
						}
					}
					
					parameters += "varchar_value like '%"+parameter.param+"%' {escape '\\'}";
				}
				
				conditions += "(" + parameters + ")";
			}
			else{
				String colName = "";
				switch(f.getDataTypeId()){
					case DataType.REAL : 		colName = "real_value";
												break;
					
					case DataType.DATE :
					case DataType.TIME :
					case DataType.DATETIME : 	colName = "datetime_value";
												break;
					
					case DataType.INT : 		colName = "int_value";
												break;
												
					case DataType.TEXT : 		colName = "text_value";
												break;
												
					case DataType.BOOLEAN : 	colName = "bit_value";
												break;
				}
				
				String parameters = "";
				for(Parameter parameter : val.getParams()){
					if(!parameters.equals(""))
						parameters += " or ";
					
					if(parameter.type.equals(ParamType.NULL)){
						parameters += colName + " IS NULL";
					}
					else if(parameter.type.equals(ParamType.DATETIME) || parameter.type.equals(ParamType.STRING)){
						String valStr = parameter.param;
						
						if(parameter.type.equals(ParamType.DATETIME)){
							valStr = Parameter.getDateTimeValue(valStr);
						}
						
						// Escape ms sql specific special characters
						int searchFrom = 0;
						int specialIndex = 0;
						while((specialIndex = valStr.indexOf("'", searchFrom)) >= 0){
							valStr  = valStr.substring(0, specialIndex) + "'" + valStr.substring(specialIndex);
							searchFrom = specialIndex + 2;
						}	
						valStr = "'"+valStr+"'";
						parameters += colName + parameter.getComparator() + valStr;
					}
					else{
						String valStr = parameter.param;
						Parameter toEvaluate = Parameter.determineTypeAndValue(valStr);
						if(toEvaluate == null)
							toEvaluate = parameter;
						
						if(toEvaluate.type == ParamType.DATETIME || toEvaluate.type == ParamType.STRING){
							// Escape ms sql specific special characters
							int searchFrom = 0;
							int specialIndex = 0;
							while((specialIndex = toEvaluate.param.indexOf("'", searchFrom)) >= 0){
								toEvaluate.param  = toEvaluate.param.substring(0, specialIndex) + "'" + toEvaluate.param.substring(specialIndex);
								searchFrom = specialIndex + 2;
							}
							
							valStr = "'"+toEvaluate.param+"'";
						}
						parameters += colName + parameter.getComparator() + valStr;
					}
				}
				
				conditions += "(" + parameters + ")";
			}
			
			
		}
		conditions = "field_id = " + f.getFieldId() + " and (" + conditions + ")";
		
		return conditions;
	}

	//====================================================================================
	
	// Static functions for miscellaneous sql queries
	
	/**
	 * Modify the query to add paging constraints
	 * 
	 * @param sql
	 * @param isPagingEnabled 
	 * @param pageSize
	 * @param pageNumber
	 * @param pagingColumn
	 * @param ordering 
	 * @return modified query with paging constraints
	 */
	public static String addPagingAndOrderingTo(String sql, boolean isPagingEnabled, int pageSize, int pageNumber, HashMap<String,String> ordering){
		
		String orderBy = "";
		String reverseOrderBy = "";

		Collection<String> orderCols = ordering.keySet();
		for(String col : orderCols){
			if(orderBy.contains(" " + col + " "))
				continue;
			
			if(!orderBy.equals(""))
				orderBy += " , ";
			if(!reverseOrderBy.equals(""))
				reverseOrderBy += " , ";
			
			orderBy += col + " " + ordering.get(col);
			reverseOrderBy += col + " " + ((ordering.get(col).equals(DqlConstants.ASC_ORDER))?DqlConstants.DESC_ORDER:DqlConstants.ASC_ORDER);
		}
		
		if(orderBy.equals("")){
			orderBy = "request_id DESC";
			reverseOrderBy = "request_id ASC";
		}
			
		if(isPagingEnabled)
			sql = 	"select * from ( \n" + 
				"select top " + pageSize + " * from ( \n" +
				"select top " + pageSize*pageNumber + " * from (\n" +
				sql + " \n" +
				") as newtbl0 order by " + orderBy + " \n" +
				") as newtbl1 order by " + reverseOrderBy + " \n" +
				") as newtbl2 order by " + orderBy;
		else
			sql = 	"select * from (\n" + sql + " \n" +
					") as newtbl0 order by " + orderBy;
		
		return sql;
	}
	
	//====================================================================================

	/**
	 * Add the extra queries for forming the request object. 
	 * The sql should have all the relevant sys_id, request_id selected into a #relevantReqs
	 * 
	 * @param sql
	 * @return modified query for retrieving all information related to request object
	 */
	public static String addQueriesForRequestObject(){
		
		String sql = "";
		
		// Join #relevantReqs with requests to get the basic info for building the request objects
		sql += "\n\nselect r.sys_id, r.request_id, r.category_id, r.status_id, r.severity_id, \n" +
				"r.request_type_id, r.subject, r.description, r.description_content_type, \n" +
				"r.is_private, r.parent_request_id, r.user_id, r.max_action_id, r.due_datetime, \n" +
				"r.logged_datetime, r.lastupdated_datetime, r.header_description, r.attachments, \n" +
				"r.summary, r.summary_content_type, '' as \"memo\", r.append_interface, r.notify, \n" +
				"r.notify_loggers, r.replied_to_action, r.office_id \n" +
				"from requests r \n" +
				"join #relevantReqs rr on r.sys_id = rr.sys_id and r.request_id = rr.request_id";
		
		// Join #relevantReqs with the request_users table to get the user info for the relevant requests
		sql += 	"\n\n select ru.sys_id, ru.request_id, ru.user_type_id, ru.user_id, ru.ordering, ru.is_primary, ru.field_id \n" +
				"from request_users ru \n" +
				"join #relevantReqs rr on ru.sys_id=rr.sys_id and ru.request_id = rr.request_id \n" + 
				"order by ru.sys_id, ru.request_id, ru.user_type_id, ru.ordering";
		
		// Join #relevantReqs with the requests_ex table to get the RequestEx object
		sql += 	"\n\n select re.sys_id, re.request_id, re.field_id, re.bit_value, re.datetime_value, re.int_value, \n" +
				"convert(varchar(1024), re.real_value) 'real_value', re.varchar_value, re.text_value, re.text_content_type, \n" +
				"type_value \n" +
				"from requests_ex re \n" +
				"join #relevantReqs rr on re.sys_id=rr.sys_id and re.request_id=rr.request_id";
		
		// Join #relevantReqs with the related_requests table to get the related requests
		sql +=  "\n\n select rere.primary_sys_id, rere.primary_request_id, rere.primary_action_id, \n" +
				"rere.related_sys_id, rere.related_request_id, rere.related_action_id \n" +
				"from related_requests rere \n" +
				"join #relevantReqs rr on rere.primary_sys_id=rr.sys_id and rere.primary_request_id=rr.request_id";
		
		// Join #relevantReqs with requests again for the child count
		sql += "\n\n select r.sys_id, r.request_id, rr.request_id as parent_request_id \n" +
				"from requests r \n" +
				"join #relevantReqs rr on rr.sys_id=r.sys_id and rr.request_id=r.parent_request_id";
		
		return sql;
	}
	
	//====================================================================================

	/**
	 * Add queries to select the requested columns. 
	 * Modifies the ordering HashMap if an extended field is present in requested columns and ordering.
	 * 
	 * @param sql
	 * @param requestedColumns
	 * @param ordering 
	 * @param fields
	 * @param ordering 
	 * @return
	 * @throws Exception
	 */
	public static String addQueriesForCustomRequestColumns(String sql, ArrayList<String> requestedColumns, Hashtable<String,Field> fields, HashMap<String,String> ordering) throws Exception {

		sql = "(" + sql + ") q \n";
		String customSelect = "select q.*";
		boolean joinRequests = false;
		
		int count = 0;
		for(String rc : requestedColumns){
			if(rc.equals("sys_id") || rc.equals("request_id") || rc.equals("action_id"))
				continue;
			if(!fields.containsKey(rc))
				throw new Exception("The field name " + rc + " does not exist in the given Business Area!");
			Field f = fields.get(rc);
			 if(f.getIsExtended()){
			
				
				String colName = "";
				switch(f.getDataTypeId()){
				case DataType.USERTYPE:         colName = "user_login";
												break;
					case DataType.REAL : 		colName = "real_value";
												break;
					
					case DataType.DATE :
					case DataType.TIME :
					case DataType.DATETIME : 	colName = "datetime_value";
												break;
					
					case DataType.INT : 		colName = "int_value";
												break;
												
					case DataType.TEXT : 		colName = "text_value";
												break;
												
					case DataType.TYPE : 		colName = "type_value";
												break;
												
					case DataType.BOOLEAN : 	colName = "bit_value";
												break;
												
					case DataType.STRING : 		colName = "varchar_value";
												break;
				}
				if(colName.equalsIgnoreCase("user_login"))
				{
					customSelect += ", re"+count+"."+ colName +" as __"+rc;
					sql += 	"join request_users re"+count+" \n" +
							"on q.sys_id=re"+count+".sys_id and q.request_id=re"+count+".request_id " +
							"and re"+count+".field_id=" + f.getFieldId()+" join users u on u.user_id = re.user_id\n";
					
				}
				else{
					
				customSelect += ", re"+count+"."+ colName +" as __"+rc;
				
				
				// change the column name to __rc for extended fields to avoid problems with restricted names
				if(ordering.containsKey(rc)){
					ordering.put("__"+rc, ordering.remove(rc));
				}
				requestedColumns.remove(rc);
				requestedColumns.add("__" + rc);
				
				sql += 	"join requests_ex re"+count+" \n" +
						"on q.sys_id=re"+count+".sys_id and q.request_id=re"+count+".request_id " +
						"and re"+count+".field_id=" + f.getFieldId()+" \n";
			}
				}
			else{
				if(f.getDataTypeId()==DataType.USERTYPE)
				{
					 
					customSelect += ", u.user_login as " + rc;
					sql += "join request_users r on q.sys_id=r.sys_id and q.request_id=r.request_id  and r.field_id=" + f.getFieldId()+" join users u  on u.user_id = r.user_id\n";
					
				}else{
					customSelect += ", r."+ rc;
				if(!joinRequests){
					sql += "join requests r on q.sys_id=r.sys_id and q.request_id=r.request_id \n";
					joinRequests = true;
				}
				}
			}
				
			count++;
		}
		
		return (customSelect + " from \n" + sql);
	}

	//====================================================================================

	/**
	 * Generate the query for getting the requests that the given list of users can view 
	 * due to their association with static and dynamic roles
	 *
	 * @param user_ids
	 * @return query
	 */
	public static String getQueryForRoles(int sys_id, ArrayList<Integer> user_ids){
		
		int requestFieldId = 2;
		try {
			requestFieldId = Field.lookupBySystemIdAndFieldName(sys_id, Field.REQUEST).getFieldId();
		}
		catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		String query = 	"select distinct ro.sys_id, ro.request_id \n" +
						"from roles_permissions rop \n" +
						"join( \n" + getQueryForRolesRequests(sys_id, user_ids) + "\n" +
						") ro \n" +
						"on rop.sys_id=ro.sys_id and rop.role_id = ro.role_id and rop.field_id="+requestFieldId+" \n" +
						"join permissions p on rop.gpermissions=p.permission and p.pview=1 ";
		
		return query;
	}
	
	//====================================================================================

	/**
	 * Gives the requests associated with all the roles of the given users.
	 * 
	 * @param sys_id
	 * @param user_ids
	 * @return
	 */
	public static String getQueryForRolesRequests(int sys_id, ArrayList<Integer> user_ids){
		
		String query = 	"(" + getQueryForUserRole(sys_id) + ")\n" +
						"union\n" +
						"(" + getQueryForStaticRoles(sys_id, user_ids) + ")\n" +
						"union\n" +
						"(" + getQueryForDynamicRoles(sys_id, user_ids) + ")";
		return query;
	}
	
	//====================================================================================

	/**
	 * Generate the query for getting the hardcoded 'User' role that every user belongs to
	 * TODO Bad Way. This basically returns the entire requests table! 
	 * Joins on the unions after this is a very heavy process. Consumes too much time.
	 * 
	 * @return query
	 */
	private static String getQueryForUserRole(int sys_id) {
		
		String query = 	"select distinct r.sys_id, r.request_id, rou.role_id \n" +
						"from roles rou \n"+
						"join requests r on r.sys_id=rou.sys_id and rou.rolename='User'";
		query += (sys_id > 0)?" and r.sys_id="+sys_id:"";
		return query;
	}
	
	//====================================================================================

	/**
	 * Generate the query for getting the static roles that the user belongs to
	 * 
	 * @param user_ids
	 * @return query
	 */
	public static String getQueryForStaticRoles(int sys_id, ArrayList<Integer> user_ids) {
		
		String query = 	"select distinct r.sys_id, r.request_id, rou.role_id \n" +
						"from roles_users rou \n" +
						"join requests r on r.sys_id=rou.sys_id \n";
		
		query += (sys_id > 0)?" and r.sys_id="+sys_id:"";
		
		String userCondition = "";
		for(int uid: user_ids){
			if(!userCondition.equals(""))
				userCondition += " or";
			userCondition += " rou.user_id=" + uid;
		}
		if(!userCondition.equals(""))
			userCondition = " and (" + userCondition + ")\n";
		
		return (query + userCondition);
	}
	
	//====================================================================================

	/**
	 * Generate the query for getting the roles of the list of users by virtue of them being in the request users list 
	 * 
	 * @param sys_id
	 * @param user_ids
	 * @return query
	 */
	public static String getQueryForDynamicRoles(int sys_id, ArrayList<Integer> user_ids){
		
		String query = 	"select distinct ru.sys_id, ru.request_id, r.role_id \n" + 
						"from request_users ru \n" +
						"join roles r on ru.field_id=r.field_id and ru.sys_id=r.sys_id \n";
		
		query += (sys_id > 0)?" and r.sys_id="+sys_id:"";
		
		String userCondition = "";
		for(int uid: user_ids){
			if(!userCondition.equals(""))
				userCondition += " or";
			userCondition += " ru.user_id=" + uid;
		}
		if(!userCondition.equals(""))
			userCondition = " and (" + userCondition + ")\n";
		
		return (query + userCondition);
	}
	
	//====================================================================================

	/**
	 * Generate the query for getting the requests which are is_private.
	 * 
	 * @param sys_id
	 * @return
	 */
	public static String getQueryForPrivate(int sys_id) {
		String query = "select distinct sys_id, request_id from requests where is_private=1 and sys_id="+sys_id;
		return query;
	}
	
	//====================================================================================

	/**
	 * Generate the query for getting the requests which are is_private and can be viewed by the given set of users 
	 * The requests should also have a view permission from any role.
	 * 
	 * @param sys_id
	 * @param user_ids
	 * @return query
	 */
	public static String getQueryForPermittedPrivate(int sys_id, ArrayList<Integer> user_ids){
		
		int isPrivateFieldId = 14;
		int requestFieldId = 2;
		try {
			isPrivateFieldId = Field.lookupBySystemIdAndFieldName(sys_id, Field.IS_PRIVATE).getFieldId();
			requestFieldId = Field.lookupBySystemIdAndFieldName(sys_id, Field.REQUEST).getFieldId();
		}
		catch (DatabaseException e) {
			e.printStackTrace();
		}
		
//		select proles.sys_id, proles.request_id from
//		(select ro.sys_id, ro.request_id from roles_permissions rop
//		join 
//		(getQueryForRolesRequests)ro
//		on rop.sys_id=ro.sys_id and rop.role_id=ro.role_id and rop.field_id=requestFieldId 
//		join permissions p on rop.gpermissions=p.permission and p.pview=1
//		)proles
//		join
//		(
//		select ro.sys_id, ro.request_id from roles_permissions rop
//		join 
//		(getQueryForRolesRequests)ro
//		on ro.sys_id=rop.sys_id and ro.role_id=rop.role_id and rop.field_id=isPrivateFieldId 
//		join permissions p on rop.gpermissions=p.permission and p.pview=1
//		join requests r on ro1.sys_id=r.sys_id and ro.request_id=r.request_id and r.is_private=1
//		)pprivate
//		on proles.sys_id=pprivate.sys_id and proles.request_id=pprivate.request_id
		
		String query = "select proles.sys_id, proles.request_id from \n" + 
						"( select ro.sys_id, ro.request_id from roles_permissions rop \n" +
						"join  \n (" + getQueryForRolesRequests(sys_id, user_ids) + "\n) ro \n" +
						"on rop.sys_id=ro.sys_id and rop.role_id=ro.role_id and rop.field_id="+requestFieldId+" \n" +
						"join permissions p on rop.gpermissions=p.permission and p.pview=1 \n" +
						") proles \n join \n " +
						"( select ro.sys_id, ro.request_id from roles_permissions rop \n" +
						"join \n (" + getQueryForRolesRequests(sys_id, user_ids) + "\n) ro \n" +
						"on ro.sys_id=rop.sys_id and ro.role_id=rop.role_id and rop.field_id="+isPrivateFieldId+" \n"+
						"join permissions p on rop.gpermissions=p.permission and p.pview=1 \n" +
						"join requests r on ro.sys_id=r.sys_id and ro.request_id=r.request_id and r.is_private=1 \n" +
						")pprivate \n" +
						"on proles.sys_id=pprivate.sys_id and proles.request_id=pprivate.request_id \n";

//		query += (sys_id > 0)?" and r.sys_id="+sys_id:"";
//		
//		String userCondition = "";
//		for(int uid: user_ids){
//			if(!userCondition.equals(""))
//				userCondition += " or";
//			userCondition += " rou.user_id=" + uid;
//		}
//		if(!userCondition.equals(""))
//			userCondition = " and (" + userCondition + ")\n";
//		
//		query = (query + userCondition)+
//				"join roles_permissions rop1 on 1=1\n" +
//				"join( \n" +
//				"(" + getQueryForStaticRoles(sys_id, user_ids) + ")\n" +
//				"union\n" +
//				"(" + getQueryForDynamicRoles(sys_id, user_ids) + ")\n" +
//				") ro \n" +
//				"on rop1.sys_id=ro.sys_id and rop1.role_id = ro.role_id and rop1.field_id="+requestFieldId+" and ro.sys_id=r.sys_id and ro.request_id=r.request_id \n" +
//				"join permissions p1 on rop1.gpermissions=p1.permission and p1.pview=1 ";
		
		return query;
	}

	//====================================================================================

	public static String getQueryForPermissions(int sysId, int userId) {
		
		if(userId == -1){
			return null;
		}
		
		// Make a list of all the relevant UserIds
		ArrayList<User> mailingLists = MailListUser.getMailListsByRecursiveMembership(userId);
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		userIds.add(userId);
		if(mailingLists != null){
			for(User ml : mailingLists)
				userIds.add(ml.getUserId());
		}
		
		// Form the relevant query to get the list of sys_ids and request_ids which are permitted for the user
		String sql =  	"--permitted requests by roles\n("+
						SqlQueryGenerator.getQueryForRoles(sysId, userIds) + 
						")\n--removing private requests\nexcept\n("+
						SqlQueryGenerator.getQueryForPrivate(sysId)+
						")\n--adding private requests with is_private permission\nunion\n("+
						SqlQueryGenerator.getQueryForPermittedPrivate(sysId, userIds)+")\n";
		
		return sql;
	}

	//====================================================================================

}
