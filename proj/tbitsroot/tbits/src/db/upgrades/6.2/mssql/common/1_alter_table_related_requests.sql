-- this script will change the related_requests table to have sys_ids instead of sys_prefixes
create table dbo.Tmp_related_requests
(
	primary_sys_id int,
	primary_request_id int,
	primary_action_id int,
	related_sys_id int,
	related_request_id int,
	related_action_id int
)

insert into dbo.Tmp_related_requests select a.sys_id 'primary_sys_id' ,r.primary_request_id 'primary_request_id', r.primary_action_id 'primary_action_id',
	b.sys_id 'related_sys_id',r.related_request_id 'related_request_id', r.related_action_id 'related_action_id' 
from related_requests r
join business_areas a on r.primary_sys_prefix=a.sys_prefix
join business_areas b on r.related_sys_prefix=b.sys_prefix
GO

DROP TABLE dbo.related_requests
GO

EXECUTE sp_rename N'dbo.Tmp_related_requests', N'related_requests', 'OBJECT'
GO
