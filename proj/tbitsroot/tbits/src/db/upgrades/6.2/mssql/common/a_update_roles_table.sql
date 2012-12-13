UPDATE roles 
set field_id=0,can_be_deleted = 1
GO

UPDATE roles
set field_id=7,can_be_deleted = 0
where rolename = 'Logger'

UPDATE roles
set field_id=8,can_be_deleted = 0
where rolename = 'Assignee'


UPDATE roles
set field_id=9,can_be_deleted = 0
where rolename = 'Subscriber'


UPDATE roles
set field_id=11,can_be_deleted = 0
where rolename = 'Cc'


UPDATE roles
set field_id=10,can_be_deleted = 0
where rolename = 'To'

update roles set can_be_deleted=0 where rolename='Analyst'
update roles set can_be_deleted=0 where rolename='Manager'
update roles set can_be_deleted=0 where rolename='Admin'
update roles set can_be_deleted=0 where rolename='PermissionAdmin'
update roles set can_be_deleted=0 where rolename='BAUsers'
update roles set can_be_deleted=0 where rolename='User'
GO
