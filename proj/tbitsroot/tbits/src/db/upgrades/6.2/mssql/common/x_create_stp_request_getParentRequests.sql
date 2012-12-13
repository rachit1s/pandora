SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

create procedure stp_request_getParentRequests
(
	@systemId int,
	@requestId int
)
AS
--get parent_requests
select sys_id,request_id,subject from requests 
WHERE sys_id=@systemId AND
request_id= (select parent_request_id from requests where sys_id=@systemId and request_id=@requestId )
and request_id <> 0
