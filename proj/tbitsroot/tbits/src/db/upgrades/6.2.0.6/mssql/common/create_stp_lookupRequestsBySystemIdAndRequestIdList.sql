set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go

-- @author Nitiraj
-- This stored procedure can be used to retrive
-- a set of requests for a particular ba.
-- Ex. retriving request data after the search
-- there is limit on the length of the string that can be passed as the comma separated requestId = 7999

create procedure [dbo].[stp_request_lookupBySystemIdAndRequestIdList]
(
	@systemId 	INT,
	@requestId	varchar(7999)
)
AS
declare @sql1  varchar(4000)
-- I am using multiple nvarchars to create the sql to be executed so that the maximum limit of a particular varchar
-- does not exceed.
set @sql1 = 'SELECT sys_id,
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
	memo,
	append_interface,
	notify,
	notify_loggers,
	replied_to_action,
	office_id
 FROM requests 
WHERE
	sys_id 		= ' + convert(varchar(10),@systemId) + ' AND
	request_id in ( ' 

declare @sql2 varchar( 4000 )
set @sql2  = ' )

SELECT * FROM request_users 
WHERE
	sys_id 		= ' + convert( varchar(10),@systemId) + ' AND
	request_id in ( ' 

declare @sql3 varchar(4000)
set @sql3 = ')
order by request_id,field_id, ordering

SELECT 
	sys_id,
	request_id,
	field_id,
	bit_value,
	datetime_value,
	int_value,
	convert(varchar(1024), real_value) real_value,
	varchar_value,
	text_value,
	text_content_type,
	type_value 
FROM 
	requests_ex
WHERE
	sys_id 	= ' + convert(varchar(10),@systemId) + ' AND
	request_id in(' 
	
declare @sql4 varchar(4000)

set @sql4 = ')
	
select * from related_requests
where primary_sys_id= ' + convert(varchar(10),@systemId) + ' and primary_request_id in ( '

declare @sql5 varchar(40)
set @sql5 = ' )'

--print @sql1 + @requestId + @sql2 + @requestId + @sql3 + @requestId + @sql4 + @requestId + @sql5

exec(@sql1 + @requestId + @sql2 + @requestId + @sql3 + @requestId + @sql4 + @requestId + @sql5)

