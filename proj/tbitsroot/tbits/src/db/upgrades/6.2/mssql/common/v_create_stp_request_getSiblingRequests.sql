SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

create procedure stp_request_getSiblingRequests
(
	@systemId int,
	@requestId int
)
AS
--get siblings
select sys_id,request_id,subject from requests WHERE sys_id=@systemId AND
	 parent_request_id=
	(select  parent_request_id from requests WHERE sys_id=@systemId and request_id=@requestId) 	
	 AND
	 request_id <> @requestId and parent_request_id <>0
