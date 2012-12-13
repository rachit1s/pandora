/*
* 1. Insert msg_format 
* 2. Add post process rules for each ba user
* 3.For each BA
*	3.1. Add SMS_ID
*	3.2. Add permissions for SMS_ID
*	3.3. Update SendSMS field's permissions.
*/

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET XACT_ABORT ON
GO
declare @maxBA INT;
select @maxBA = max(sys_id) from business_areas
declare @i INT
declare @sendsmsfieldid int
declare @baUserSysId int
declare @ba_user_id int
declare @max int
declare @new_field_id int
BEGIN TRAN
INSERT INTO msg_format
(
	msg_format_id,
	msg_template,
	sys_id
)
VALUES
(
	0,
	'$sys_id$#$request_id$#$max_action_id$:$subject$ - $user_id$.',
	1
)
INSERT INTO msg_format
(
	msg_format_id,
	msg_template,
	sys_id
)
VALUES
(
	1,
	'Added $request_id$:$subject$ by  $user_id$.',
	1
)
INSERT INTO msg_format
(
	msg_format_id,
	msg_template,
	sys_id
)
VALUES
(
	2,
	'Updated $request_id$:$subject$  by $user_id$.',
	1
)

declare ba_users_cursor CURSOR FOR
select sys_id, user_id from business_area_users 

open ba_users_cursor

fetch next from ba_users_cursor into @baUserSysId, @ba_user_id

while @@FETCH_STATUS = 0
BEGIN
	print 'Addeding post process rules: sys_id = ';
	print cast(@baUserSysId as varchar(10)) + ', role_id = 12, user_id = '
			+ cast(@ba_user_id as varchar(10));
	select @max = isnull(max(rule_id),0) from post_process_rules;
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
		@ba_user_id,
		@baUserSysId,
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
		@ba_user_id,
		@baUserSysId,
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
	fetch next from ba_users_cursor into @baUserSysId, @ba_user_id
END
close ba_users_cursor 
deallocate ba_users_cursor

set @i = 1
while @i <= @maxBA 
BEGIN
	/* Add SMS_Id and set role permissions to 0*/
	exec stp_field_insert @i, 1, 'sms_id', 'Send Format Id', 'Send Format Id', 5, 1, 1, 0, 0, 0, '', 0, 0, 0
	select @new_field_id = max(field_id) from fields where sys_id = @i
	update roles_permissions set gpermissions = 0 where field_id = @new_field_id and sys_id = @i

	/* update the permissions of SendSMS*/
	select @sendsmsfieldid = field_id from fields where name = 'SendSMS' and sys_id = @i;
	update roles_permissions set gpermissions = 7 where role_id = 1 and field_id = @sendsmsfieldid and sys_id = @i
	set @i = @i + 1
END
COMMIT TRAN

/** Stp Upgrade **/
