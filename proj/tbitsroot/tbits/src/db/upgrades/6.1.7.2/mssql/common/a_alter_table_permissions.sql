if not exists (select * from syscolumns
  where id=object_id('permissions') and name='pEmailView')
begin
alter table permissions
add pEmailView int
end
GO

delete permissions


insert into permissions values (0 ,0 ,0 ,0 ,0)
insert into permissions values (1 ,1 ,0 ,0 ,0)
insert into permissions values (2 ,0 ,1 ,0 ,0)
insert into permissions values (3 ,1 ,1 ,0 ,0)
insert into permissions values (4 ,0 ,0 ,1 ,0)
insert into permissions values (5 ,1 ,0 ,1 ,0)
insert into permissions values (6 ,0 ,1 ,1 ,0)
insert into permissions values (7 ,1 ,1 ,1 ,0)


insert into permissions values (8 ,0 ,0 ,0 ,1)
insert into permissions values (9 ,1 ,0 ,0 ,1)
insert into permissions values (10 ,0 ,1 ,0 ,1)
insert into permissions values (11 ,1 ,1 ,0 ,1)
insert into permissions values (12 ,0 ,0 ,1 ,1)
insert into permissions values (13 ,1 ,0 ,1 ,1)
insert into permissions values (14 ,0 ,1 ,1 ,1)
insert into permissions values (15 ,1 ,1 ,1 ,1)
