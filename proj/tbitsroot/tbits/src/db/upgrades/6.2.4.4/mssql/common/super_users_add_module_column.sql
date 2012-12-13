--add a new column called module_name to super_users
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.Columns WHERE TABLE_NAME = 'super_users' AND COLUMN_NAME = 'module_name')
BEGIN
    alter table super_users
	add module_name varchar(50) null
--Create a temp table and add all the values currently in super_users table into it and then delete the table
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'temptable'))
BEGIN
    drop table temptable
END
create table temptable(
	user_id	  int,
	is_active int,
    module_name varchar(50)
)
delete from temptable
insert into temptable select * from super_users
update temptable set module_name = 'tbits'
drop table super_users
create table super_users(
	user_id	  int,
	is_active int,
    module_name varchar(50)
	PRIMARY KEY (user_id, module_name)
)
delete from super_users
insert into super_users select * from temptable
drop table temptable
END
