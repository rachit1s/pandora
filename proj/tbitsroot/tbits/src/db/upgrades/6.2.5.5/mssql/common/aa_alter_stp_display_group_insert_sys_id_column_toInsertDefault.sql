
/****** Object:  StoredProcedure [dbo].[stp_display_group_insert_sys_id_column]    Script Date: 01/17/2012 16:16:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER procedure [dbo].[stp_display_group_insert_sys_id_column]
AS
Declare @sys_id int
Declare @display_group int
Declare @NewDisplay_group int

update fields set display_group = 1 where display_name = 'SMS Id'
update fields set display_group = 1 where display_name = 'send SMS'

update fields set display_order = (select max(display_order) from fields where display_group = 1) where display_name = 'SMS Id'
update fields set display_order = (select max(display_order) from fields where display_group = 1) where display_name = 'Send SMS'


----change in the display_group add the sys_id and 

alter table display_groups add  sys_id int

---
begin

--------------------------------

select id,display_name,display_order,is_active,is_default  
into #tmp 
from display_groups 
----------------------------------
select distinct f.sys_id,display_group ,dg.display_name
into #tmp1
from fields f 
join display_groups dg on dg.id = f.display_group
where is_extended = 1 and display_group not in ( 0,1)
----------------------------------

delete from display_groups where id in (select display_group from #tmp1)

insert into display_groups (display_name,display_order,is_active,is_default, sys_id )
( select t.display_name,t.display_order,t.is_active,t.is_default,t1.sys_id 
from #tmp t 
join #tmp1 t1 on t.id = t1.display_group )
----------------------------------
--- now update fields with new display_id
select dg.id,dg.sys_id,t1.display_group
into #tmp2
from display_groups dg 
join #tmp1 t1 on dg.sys_id = t1.sys_id and dg.display_name = t1.display_name
------------------------------------
while(exists(select * from #tmp2))
begin
select 
   @NewDisplay_group = id,
   @sys_id = sys_id,
   @display_group = display_group
from #tmp2
--update field
update fields 
 set display_group = @NewDisplay_group
 where sys_id = @sys_id 
 and display_group = @display_group
--delete from #tmp
delete from #tmp2
 where sys_id = @sys_id
 and id = @NewDisplay_group
 and display_group = @display_group

select * from #tmp2
END

-------------------
drop table #tmp
drop table #tmp1
drop table #tmp2
END
