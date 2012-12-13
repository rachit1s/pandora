update roles_permissions
set gpermissions = gpermissions + 2 -- for the change perm to be included
from
roles_permissions rp
join fields f on f.sys_id=rp.sys_id and f.field_id = rp.field_id
where (f.name = 'description' or f.name = 'summary')
and ( rp.gpermissions & 1 != 0 ) and ( rp.gpermissions & 2 = 0 )
