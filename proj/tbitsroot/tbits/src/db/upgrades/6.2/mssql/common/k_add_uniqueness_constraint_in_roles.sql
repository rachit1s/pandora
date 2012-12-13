alter table roles
add constraint unique_sys_id_and_rolename
unique(sys_id,rolename)
GO
