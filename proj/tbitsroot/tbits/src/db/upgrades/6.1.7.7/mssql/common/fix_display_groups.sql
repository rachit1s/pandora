update fields
set display_group = 1 
where display_group != 0 and display_group not in (select id from display_groups)
