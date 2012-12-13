
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

create procedure stp_request_getRelatedRequests
(
        @systemId int,
        @requestId int
)
AS

--get all related requests

select rr.related_sys_id, rr.related_request_id, rr.related_action_id,r.subject from related_requests rr
join requests r on r.sys_id=rr.related_sys_id and r.request_id=rr.related_request_id
where primary_sys_id=@systemId and primary_request_id=@requestId

