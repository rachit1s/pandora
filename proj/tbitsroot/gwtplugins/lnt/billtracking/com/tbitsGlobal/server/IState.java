package billtracking.com.tbitsGlobal.server;

public interface IState {	
	//state attributes
	public static final String state_duration="state_duration";
	public static final String next_state_id = "next_state_id";
	public static final String prev_state_id = "prev_state_id";
	public static final String reject_state_id = "rej_state_id";
	public static final String is_state_decision = "is_decision_state";	

	//Field Names per State
	public static final String state_target_date = "state_fieldname_targetdate";
	public static final String state_pending_with="state_typevalue_pendingwithdep";
	public static final String state_Dep_Receipt_Date="state_fieldname_receiptdate";
	public static final String state_Dep_Acknowledge_Date="state_fieldname_acknowledgedate";
	public static final String state_Decision_Field_Name="state_fieldname_decision";
	public static final String state_attachment_ids = "state_attachment_ids";

	//TypeFields to be prefilled to reach this state
	public static final String state_type_field_names="state_fieldname_types";
	public static final String state_type_field_values="state_fieldvalues_types";

	//UserTypeFields to be prefilled to reach this state
	public static final String state_usertype_field_values = "state_fieldvalue_usertypes";
	public static final String state_usertype_field_names = "state_fieldname_usertypes";

}
