
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='reset_password')							   									         
BEGIN
create table reset_password
(
user_login varchar(200),
reset_key varchar(512),
creation_date datetime
)

END
