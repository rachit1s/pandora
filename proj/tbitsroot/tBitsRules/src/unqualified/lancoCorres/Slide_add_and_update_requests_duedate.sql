declare @sysId int
declare @sysPrefix varchar (50)
set @sysPrefix='CORR'

select @sysId=ba.sys_id from business_areas ba where ba.sys_prefix=@sysPrefix

update requests
set due_datetime=dateadd(day,14,logged_datetime)
where sys_id=@sysId and max_action_id=1

update requests
set due_datetime=dateadd(day,10,logged_datetime)
where sys_id=@sysId and max_action_id>1