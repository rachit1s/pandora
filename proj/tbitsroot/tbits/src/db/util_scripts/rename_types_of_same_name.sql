update types set name = name + '1'
from types t3 join ( select t.sys_id,t.field_id,t.type_id from types t
join types t1 on t.sys_id = t1.sys_id and t.field_id = t1.field_id and t.name = t1.name and t.type_id > t1.type_id ) as t2
on t3.sys_id = t2.sys_id and t3.field_Id = t2.field_id and t3.type_id = t2.type_Id
