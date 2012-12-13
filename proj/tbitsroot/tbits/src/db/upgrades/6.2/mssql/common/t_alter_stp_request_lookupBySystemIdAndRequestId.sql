set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go




ALTER procedure [dbo].[stp_request_lookupBySystemIdAndRequestId]
(
	@systemId 	INT,
	@requestId	INT
)
AS
SELECT sys_id,
	request_id,
	category_id,
	status_id,
	severity_id,
	request_type_id,
	subject,
	description,
	description_content_type,
	is_private,
	parent_request_id,
	user_id,
	max_action_id,
	due_datetime,
	logged_datetime,
	lastupdated_datetime,
	header_description,
	attachments,
	summary,
	summary_content_type,
	'' as "memo",
	append_interface,
	notify,
	notify_loggers,
	replied_to_action,
	office_id
 FROM requests 
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId 
SELECT * FROM request_users 
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId 
order by user_type_id, ordering

SELECT 
	sys_id,
	request_id,
	field_id,
	bit_value,
	datetime_value,
	int_value,
	convert(varchar(1024), real_value) 'real_value',
	varchar_value,
	text_value,
	text_content_type,
	type_value 
FROM 
	requests_ex
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId

--get all related requests
select * from related_requests
where primary_sys_id=@systemId and primary_request_id=@requestId
