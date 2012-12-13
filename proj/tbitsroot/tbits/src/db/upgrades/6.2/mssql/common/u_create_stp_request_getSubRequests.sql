SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

create procedure stp_request_getSubRequests
(
	@systemId int,
	@requestId int
)
AS
--get sub_requests
select sys_id,request_id,subject from requests WHERE sys_id=@systemId AND
	 parent_request_id= @requestId and @requestId <>0
