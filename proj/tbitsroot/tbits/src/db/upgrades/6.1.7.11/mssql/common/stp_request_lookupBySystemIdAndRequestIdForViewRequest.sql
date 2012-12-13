set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO


ALTER procedure [dbo].[stp_request_lookupBySystemIdAndRequestIdForViewRequest]
(
	@systemId 	INT,
	@requestId	INT
)
AS
declare @sysPrefix varchar(50)
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
	convert(varchar, real_value) 'real_value',
	varchar_value,
	text_value,
	text_content_type,
	type_value 
FROM 
	requests_ex
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId
--get sub_requests
select request_id,subject from requests WHERE sys_id=@systemId AND
	 parent_request_id= @requestId and @requestId <>0
--get siblings
select request_id,subject from requests WHERE sys_id=@systemId AND
	 parent_request_id=
	(select  parent_request_id from requests WHERE sys_id=@systemId and request_id=@requestId) 	
	 AND
	 request_id <> @requestId and parent_request_id <>0
--get related requests
select  @sysPrefix= sys_prefix  from business_areas where sys_id = @systemId
select distinct  related_sys_prefix "sys_prefix"  , related_request_id "request_id" , related_action_id "action_id" into #tmp1
	from related_requests where primary_sys_prefix =@sysPrefix
		and primary_request_id=@requestId
Insert into #tmp1
	 select distinct primary_sys_prefix "sys_prefix"   , primary_request_id   "request_id"  ,primary_action_id "action_id"
		from related_requests where related_sys_prefix =@sysPrefix
		and related_request_id=@requestId
-- #tmp1 contains all directly related request as of now
-- get transitive related requests into #tmp1 if any
 
IF ((SELECT COUNT(*) FROM #tmp1) > 0)
BEGIN
	select * into #tmp2 from #tmp1
while ((SELECT COUNT(*) FROM #tmp2) > 0)
BEGIN
	select distinct  related_sys_prefix "sys_prefix"  , related_request_id "request_id"  , related_action_id "action_id" into #tmp3
	from related_requests, #tmp2 where 
		primary_sys_prefix = #tmp2.sys_prefix and primary_request_id = #tmp2.request_id
		 and 
		 (NOT(related_sys_prefix = @sysPrefix and related_request_id = @requestId) )
		and
		(NOT( related_sys_prefix = #tmp2.sys_prefix and related_request_id = #tmp2.request_id))
Insert into #tmp3
 	select distinct primary_sys_prefix "sys_prefix"   , primary_request_id   "request_id"  ,primary_action_id "action_id"
		from related_requests, #tmp2  where
		 related_sys_prefix = #tmp2.sys_prefix and related_request_id = #tmp2.request_id
		 and
		(NOT(primary_sys_prefix = @sysPrefix and primary_request_id = @requestId))
		and
		(NOT(primary_sys_prefix = #tmp2.sys_prefix and primary_request_id = #tmp2.request_id))
	
IF ((SELECT COUNT(*) FROM #tmp3) = 0)
	BEGIN
		BREAK
	END
	DELETE #tmp2
	INSERT INTO #tmp2
	
	SELECT * FROM #tmp3 
 	where (sys_prefix +'#' +  convert(varchar(50),request_id))
			 not in 
				(select sys_prefix +'#' +  convert(varchar(50),request_id) from #tmp1)
	INSERT INTO #tmp1
	SELECT *  FROM #tmp3
	
	DROP TABLE #tmp3
END
DROP TABLE #tmp2
END
-- return all direct and transitive related requests
select distinct  
	case
	when #tmp1.action_id > 0 then #tmp1.sys_prefix + '#' + convert(varchar(50),#tmp1.request_id)  + '#' +  convert(varchar(50),#tmp1.action_id)
	else
	#tmp1.sys_prefix + '#' + convert(varchar(50),#tmp1.request_id)
	end
	 "request_id",  isNull(requests.subject,' ') "subject"
	 from
	 #tmp1 LEFT JOIN  business_areas ON
		#tmp1.sys_prefix = business_areas.sys_prefix
	LEFT JOIN requests ON
	 	#tmp1.request_id = requests.request_id  AND
		 business_areas.sys_id  =  requests.sys_id
	
DROP TABLE #tmp1

DECLARE @parentRequestId int
SELECT @parentRequestId = parent_request_id from requests where request_id = @requestId and sys_id = @systemId
SELECT @requestId "request_id",subject from requests where request_id = @parentRequestId and sys_id = @systemId





