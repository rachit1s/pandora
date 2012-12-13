update fields
set permission=127 where sys_id in(select sys_id from business_areas where sys_prefix='Bill')
