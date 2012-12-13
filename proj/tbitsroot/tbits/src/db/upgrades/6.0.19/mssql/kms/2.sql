set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[stp_business_area_users_insert] 
(
	@sys_id 	INT,
	@user_id 	INT,
	@is_active 	BIT
)
AS
declare @max INT;
INSERT INTO business_area_users 
(
	sys_id,
	user_id,
	is_active
) 
VALUES 
(
	@sys_id,
	@user_id,
	@is_active
)
select @max = isnull(max(rule_id), 0) from post_process_rules;
INSERT INTO post_process_rules
(
	rule_id,
	user_id,
    sys_id,
    priority,
    xml_string,
    description,
    enabled
)
VALUES
(
	@max + 1,
	@user_id,
	@sys_id,
	1,
	'<?xml version="1.0" encoding="utf-8" ?>   
<Rule id="1">   
	<expressions>
		<expression id="1">
			<name>max_action_id</name> 
			<op>EQ</op> 
			<value>1</value> 
		</expression>
	</expressions>
	<actions>    
		<action>     
			<name>sms_id</name>      
			<op>SET</op>      
			<value>1</value>    
		</action>   
	</actions>  
</Rule>',
	'Add Request',
	1
)
INSERT INTO post_process_rules
(
	rule_id,
	user_id,
    sys_id,
    priority,
    xml_string,
    description,
    enabled
)
VALUES
(
	@max + 2,
	@user_id,
	@sys_id,
	1,
	'<?xml version="1.0" encoding="utf-8" ?>   
<Rule id="1">   
	<expressions>
		<expression id="1">
			<name>max_action_id</name> 
			<op>NE</op> 
			<value>1</value> 
		</expression>
	</expressions>
	<actions>    
		<action>     
			<name>sms_id</name>      
			<op>SET</op>      
			<value>2</value>    
		</action>   
	</actions>  
</Rule>',
	'Update Request',
	1
)

exec stp_admin_insert_roles_users @sys_id, 12, @user_id, 1
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[stp_tbits_createBusinessArea] 
(
	@sysName	VARCHAR(128),
	@sysPrefix	VARCHAR(128)
)
AS
DECLARE @systemId INT
DECLARE @userId INT

SELECT 
	@userId = user_id 
FROM 
	users 
WHERE user_login = 'root'

SELECT 
	@systemId = ISNULL(max(sys_id), 0) 
FROM 
	business_areas

SELECT @systemId = @systemId + 1

-- Insert the Business Area Record.
INSERT INTO business_areas 
(
	sys_id, 
	name, 
	display_name, 
	email, 
	sys_prefix, 
	description, 
	type, 
	location, 
	max_request_id, 
	max_email_actions, 
	is_email_active, 
	is_active, 
	date_created, 
	is_private, 
	sys_config, 
	field_config
)
VALUES
(
	@systemId, 
	@sysName, 
	@sysName, 
	@sysprefix+'@localhost', 
	@sysPrefix, 
	@sysName, 
	'Development', 
	'hyd', 
	0, 
	10, 
	1, 
	1, 
	getUTCDate(), 
	0, 
	'
	<SysConfig>
	    <!-- Default CSS to be used for this business area. -->
	    <Stylesheet web="tbits.css" email="null" />
		<!-- Datetime related options. -->
	    <DefaultDueDate allowNull="true" duration="0" />
	    <DateFormat list="6" email="6" />
		<!-- Mailing options. -->
	    <Notify request="1" action="1" />
	    <NotifyLogger request="true" action="true" />
	    <MailFormat format="1" />
	    <!-- Severity related options. -->
	    <Severity>
	        <Incoming highValue="critical" lowValue="low" />
	        <Outgoing highValue="critical" lowValue="low" />
		</Severity>
		<!-- Other options.-->
		<Assign all="false" volunteer="0" />
		<LegacyPrefixes list="" />
		<!-- Custom links if any. -->
		<CustomLinks>
		</CustomLinks>
	</SysConfig>
	', 
	null
)

--- Insert the Field Records.
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 1, 'sys_id', 'Business Area', 'BA', 5, 0, 0, 1, '', 6, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 2, 'request_id', 'Article', 'Request', 5, 0, 0, 1, '', 47, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 3, 'category_id', 'Category', 'Category', 9, 0, 3, 1, '', 254, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 4, 'status_id', 'Status', 'Status', 9, 0, 3, 1, '', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 5, 'severity_id', 'Priority', 'Severity', 9, 0, 3, 1, '', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 6, 'request_type_id', 'Article Type', 'Article Type', 9, 0, 3, 1, '', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 7, 'logger_ids', 'Author', 'Authors', 10, 0, 3, 1, '', 191, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 8, 'assignee_ids', 'Reviewer', 'Reviewers', 10, 0, 3, 1, '', 191, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 9, 'subscriber_ids', 'Subscribers', 'Subscribers', 10, 0, 5, 1, '', 63, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private)
 VALUES(@systemId, 10, 'to_ids', 'To', 'To', 10, 0, 2, 1, '', 125, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 11, 'cc_ids', 'Cc', 'CC', 10, 0, 2, 1, '', 125, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 12, 'subject', 'Subject', 'Subject', 7, 0, 3, 1, '', 127, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 13, 'description', 'Description', 'Description', 8, 0, 0, 1, '', 103, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 14, 'is_private', 'Private', 'Private', 1, 0, 1, 1, '', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 15, 'parent_request_id', 'Parent', 'Parent', 5, 0, 3, 1, '', 127, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 16, 'user_id', 'Last Update By.', 'User', 10, 0, 0, 1, '', 44, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 17, 'max_action_id', '# U', 'Action', 5, 0, 0, 1, '', 44, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 18, 'due_datetime', 'Review Date', 'Due', 4, 0, 3, 1, '', 127, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 19, 'logged_datetime', 'Submitted Date', 'Logged', 4, 0, 0, 1, '', 44, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 20, 'lastupdated_datetime', 'Last Updated', 'Last Updated', 4, 0, 0, 1, '', 44, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 21, 'header_description', 'Header Description', 'Header', 8, 0, 0, 1, '', 4, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 22, 'attachments', 'Attachments', 'Attach', 8, 0, 0, 1, '', 103, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 23, 'summary', 'Summary', 'Summary', 8, 0, 1, 1, '', 103, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 24, 'memo', 'Memo', 'Memo', 8, 0, 1, 1, '', 7, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 25, 'append_interface', 'append_interface', 'append_interface', 5, 0, 0, 1, '',0, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 26, 'notify', 'Notify', 'Notify', 1, 0, 1, 1, '', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 27, 'notify_loggers', 'Notify Author', 'Notify Logger', 1, 0, 1, 1, '', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 28, 'replied_to_action', 'replied_to_action', 'replied_to_action', 5, 0, 1, 1, '', 70, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 29, 'related_requests', 'Linked Articles', 'Linked Requests', 7, 0, 4, 1, '', 23, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 30, 'office_id', 'Office', 'Office', 9, 0, 3, 1, '', 126, 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent, display_order, display_group) 
VALUES(@systemId,  31, 'SendSMS', 'Send SMS', 'Sends SMS', 1, 1, 1, 0, 0, 47, '', 0, 0, 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent, display_order, display_group) 
VALUES(@systemId,  32, 'sms_id', 'SMS Id', 'The Id of SMS format', 5, 1, 1, 0, 0, 0, '', 0, 0, 0)


--- Insert into field_descriptor table
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 1, 'ba', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 2, 'req', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 2, 'request', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 3, 'cat', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 3, 'category', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 4, 'stat', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 4, 'status', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 5, 'sev', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 5, 'severity', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 6, 'reqtype', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 6, 'requesttype', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 6, 'type', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 7, 'log', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 7, 'logger', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 7, 'loggers', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 8, 'ass', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 8, 'assignee', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 8, 'assignees', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 8, 'assign', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 9, 'sub', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 9, 'subscriber', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 9, 'subscribers', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 10, 'to', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 11, 'cc', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 12, 'subj', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 13, 'desc', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 13, 'alltext', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 14, 'conf', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 15, 'par', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 15, 'parent', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 16, 'user', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 17, 'action', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, 'ddate', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, 'due', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, 'dueby', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, 'duedate', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 19, 'ldate', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 19, 'loggeddate', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 20, 'udate', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 20, 'updateddate', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 21, 'hdr', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 22, 'att', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 23, 'sum', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 23, 'summary', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 24, 'memo', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 25, 'actfrm', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 26, 'notify', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 26, 'mail', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 27, 'notlog', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 28, 'reAction', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 29, 'relReq', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 29, 'link', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 29, 'linked', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 30, 'off', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 31, 'sendsms', 1)

---- Insert the default Types.

---- Categories
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 3, 1, 'pending', 'Others', 'Pending - Not specified or Misc', 1, 1, 1, 1, 0, 0)

---- Statuses
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 4,1, 'Active', 'Active', 'Active', 1, 1, 1, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 4,2, 'Approved', 'Approved', 'Approved', 2, 1, 0, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 4,3, 'Expired', 'Expired', 'Expired', 3, 1, 0, 0, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 4,4, 'Rejected', 'Rejected', 'Rejected', 4, 1, 0, 0, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 4,5, 'Suspended', 'Suspended', 'Suspended', 5, 1, 0, 0, 0, 0)

---- Severities
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 5, 1, 'low', 'Low', 'Low', 1, 1, 0, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 5, 2, 'medium', 'Medium', 'Medium', 2, 1, 1, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 5, 3, 'high', 'High', 'High', 3, 1, 0, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 5, 4, 'critical', 'Critical', 'Critical', 4, 1, 0, 1, 0, 0)

---- Request Types.
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 6, 1, 'others', 'Others', 'Others', 1, 1, 1, 1, 0, 0)
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
--VALUES(@systemId, 6, 2, 'question', 'Question', 'Question', 2, 1, 0, 1, 0, 0)
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
--VALUES(@systemId, 6, 3, 'countermeasure', 'Counter Measure', 'Counter Measure', 3, 1, 0, 1, 0, 0)
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
--VALUES(@systemId, 6, 4, 'manual', 'Manual', 'Manual', 4, 1, 0, 1, 0, 0)


----Insert default locations
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
VALUES(@systemId, 30, 1, 'default', '-', 'default', 1, 1, 1, 1, 0, 0)
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 2, 'Gurgaon Plant', 'Gurgaon Plant', 'Houston', 2, 1, 0, 0, 0, 0)
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 3, 'Hyderabad', 'HYD', 'Hyderabad', 3, 1, 0, 0, 0, 0) 
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 4, 'Kansas City', 'KC', 'Kansas City', 4, 1, 0, 0, 0, 0) 
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 5, 'London', 'LON', 'London', 5, 1, 0, 0, 0, 0 )
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 6, 'New York', 'NYC', 'New York', 6, 1, 0, 0, 0, 0) 
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES ( @systemId, 30, 7, 'San Fransisco', 'SF', 'San Fransisco', 7, 1, 0, 0, 0, 0) 
--
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 8, 'Silicon Valley', 'SV', 'Silicon Valley', 8, 1, 0, 0, 0, 0) 


---- INSERT INTO roles.
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 1, 'User', 'User')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 2, 'Logger', 'Logger')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 3, 'Assignee', 'Assignee')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 4, 'Subscriber', 'Subscriber')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 5, 'To', 'To')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 6, 'Cc', 'Cc')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 7, 'Analyst', 'Analyst')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 8, 'Manager', 'Manager')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 9, 'Admin', 'Admin')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 10, 'PermissionAdmin', 'PermissionAdmin')

--Insert one BAUser
--INSERT INTO business_area_users(sys_id,user_id) VALUES(@systemId, @userId)
exec stp_business_area_users_insert @systemId, @userId, 1

/**
Permission Values.
===================
|   Type | Value. |
=================== 
|    Add |  1     |
| Change |  2     |
|   View |  4     |
=================== 
*/
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 1, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 2, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 3, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 4, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 5, 6, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 6, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 7, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 8, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 9, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 10, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 11, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 12, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 13, 5, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 14, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 15, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 16, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 18, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 19, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 20, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 21, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 22, 5, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 23, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 24, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 26, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 27, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 29, 7, 0)
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 30, 4, 0)  

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 1, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 2, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 3, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 4, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 5, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 6, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 7, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 8, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 9, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 10, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 11, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 12, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 13, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 14, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 15, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 16, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 18, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 19, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 20, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 21, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 22, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 23, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 24, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 26, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 27, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 29, 0, 0)
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 30, 0, 0)  

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 1, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 2, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 3, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 4, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 5, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 6, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 7, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 8, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 9, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 10, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 11, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 12, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 13, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 14, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 15, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 16, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 18, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 19, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 20, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 21, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 22, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 23, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 24, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 26, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 27, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 29, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 30, 7, 0) 

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 1, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 2, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 3, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 4, 0, 0) 

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 5, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 6, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 7, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 8, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 9, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 10, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 11, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 12, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 13, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 14, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 15, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 16, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 18, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 19, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 20, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 21, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 22, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 23, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 24, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 26, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 27, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 29, 0, 0)
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 30, 0, 0)  

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 1, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 2, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 3, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 4, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 5, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 6, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 7, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 8, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 9, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 10, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 11, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 12, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 13, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 14, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 15, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 16, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 18, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 19, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 20, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 21, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 22, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 23, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 24, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 26, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 27, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 29, 0, 0)
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 30, 3, 0)  

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 1, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 2, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 3, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 4, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 5, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 6, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 7, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 8, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 9, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 10, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 11, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 12, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 13, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 14, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 15, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 16, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 18, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 19, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 20, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 21, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 22, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 23, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 24, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 26, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 27, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 29, 0, 0)
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 30, 0, 0)

EXEC stp_tbits_insertAuthorizationDefaults @systemId

GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[stp_tbits_insertAuthorizationDefaults]
(
    @systemId INT
)
AS

/*
 * Delete standard roles.
 */
DELETE roles WHERE sys_id = @systemId

INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 1, 'User', 'User')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 2, 'Logger', 'Logger')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 3, 'Assignee', 'Assignee')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 4, 'Subscriber', 'Subscriber')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 5, 'Cc', 'Cc')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 6, 'To', 'To')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 7, 'Analyst', 'Analyst')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 8, 'Manager', 'Manager')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 9, 'Admin', 'Admin')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 10, 'PermissionAdmin', 'Permission Admin')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 11, 'Customer', 'Customer')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 12, 'BAUsers', 'BAUsers')
/*
 * Delete permissions standard roles have on standard fields.
 */
DELETE roles_permissions 
WHERE 
    sys_id = @systemId AND 
    field_id IN 
    (
        SELECT 
            field_id 
        FROM 
            fields 
        WHERE 
            sys_id = @systemId AND 
            is_extended = 0
    )

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,2,6,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,3,4,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,4,4,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,5,4,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,6,4,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,7,4,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,8,4,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,9,6,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,10,7,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,11,7,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,12,4,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,13,5,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,15,7,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,16,4,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,17,4,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,18,4,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,19,4,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,20,4,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,21,4,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,22,5,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,23,4,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,24,7,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,26,4,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,27,4,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,28,4,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,29,4,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,2,1,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,7,2,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,9,1,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,11,1,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,12,3,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,14,6,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,23,3,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,3,2,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,4,2,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,5,2,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,6,2,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,8,2,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,9,2,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,12,2,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,14,6,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,18,2,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,23,2,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,26,2,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,27,2,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,29,2,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,14,6,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,2,7,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,3,4,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,4,4,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,5,7,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,6,4,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,7,4,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,8,4,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,9,7,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,10,7,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,11,7,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,12,4,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,13,5,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,15,7,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,16,4,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,17,4,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,18,4,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,19,4,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,20,4,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,21,4,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,22,5,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,23,4,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,24,7,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,26,4,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,27,4,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,28,4,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,29,7,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,2,7,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,3,4,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,4,4,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,5,7,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,6,4,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,7,4,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,8,4,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,9,7,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,10,7,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,11,7,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,12,4,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,13,5,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,15,7,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,16,4,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,17,4,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,18,4,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,19,4,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,20,4,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,21,4,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,22,5,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,23,4,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,24,7,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,26,4,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,27,4,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,28,4,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,29,7,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,3,2,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,5,2,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,6,2,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,8,3,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,9,2,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,12,2,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,14,6,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,18,2,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,23,2,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,26,2,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,27,2,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,29,2,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,2,7,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,3,7,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,4,7,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,5,7,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,6,7,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,7,7,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,8,7,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,9,7,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,10,7,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,11,7,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,12,7,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,13,5,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,14,6,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,15,7,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,16,4,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,17,4,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,18,7,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,19,4,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,20,4,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,21,4,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,22,5,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,23,7,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,24,7,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,26,7,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,27,7,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,28,4,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,29,7,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,30,7,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,32,0,0)	--SMS_ID

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,32,0,0)	--SMS_ID
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO


ALTER PROCEDURE [dbo].[stp_business_area_users_delete] 
(
	@sys_id INT,
	@user_id INT,
	@is_active BIT
)
AS
DELETE FROM post_process_rules 
WHERE 
	sys_id = @sys_id AND
	user_id = @user_id

DELETE FROM roles_users
WHERE
	sys_id 	= @sys_id AND
	user_id = @user_id

DELETE FROM type_users
WHERE
	sys_id 	= @sys_id AND
	user_id = @user_id

DELETE FROM business_area_users 
WHERE
	sys_id 	= @sys_id AND
	user_id = @user_id



GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO


ALTER PROCEDURE [dbo].[stp_tbits_deleteBusinessArea]
(  
 @systemId  int  
)  
as  

PRINT 'Deleting Exclusion List:'
delete exclusion_list where sys_id = @systemId  
PRINT 'Deleting Request Extended Fields:'
delete requests_ex where sys_id = @systemId  
PRINT 'Deleting Request Users:'
delete request_users where sys_id = @systemId  
PRINT 'Deleting Requests:'
delete requests where sys_id = @systemId  
PRINT 'Deleting Action Extended Fields:'
delete actions_ex where sys_id = @systemId  
PRINT 'Deleting Action Users:'
delete action_users where sys_id = @systemId  
PRINT 'Deleting Actions:'
delete actions where sys_id = @systemId  
PRINT 'Deleting Role-Users:'
delete roles_users where sys_id = @systemId  
PRINT 'Deleting Role-Permissions:'
delete roles_permissions where sys_id = @systemId  
PRINT 'Deleting Roles:'
delete roles where sys_id = @systemId  
PRINT 'Deleting BA Users:'
delete business_area_users where sys_id = @systemId
PRINT 'Deleting Type Users:'
delete type_users where sys_id = @systemId  
PRINT 'Deleting Types:'
delete types where sys_id = @systemId  
PRINT 'Deleting Field Descriptors:'
delete field_descriptors where sys_id = @systemId  
PRINT 'Deleting Fields:'
delete fields where sys_id = @systemId  
PRINT 'Deleting BA Record:'
delete business_areas where sys_id = @systemId
PRINT 'Deleting post process rules '
delete post_process_rules where sys_id = @systemId


GO
