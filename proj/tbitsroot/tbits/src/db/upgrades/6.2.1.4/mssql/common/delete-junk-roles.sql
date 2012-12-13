delete rp from
roles_permissions rp
LEFT outer join roles  r on r.sys_id = rp.sys_id and r.role_id = rp.role_id
where r.role_id is null 
