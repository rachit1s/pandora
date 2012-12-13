ALTER TABLE action_users add field_id int
GO

ALTER TABLE request_users add field_id int
GO

UPDATE action_users
set field_id=0
GO

UPDATE action_users
set field_id=16
where user_type_id = 1

UPDATE action_users
set field_id=7
where user_type_id = 2

UPDATE action_users
set field_id=8
where user_type_id = 3


UPDATE action_users
set field_id=9
where user_type_id = 4


UPDATE action_users
set field_id=11
where user_type_id = 6


UPDATE action_users
set field_id=10
where user_type_id = 5






UPDATE request_users
set field_id=0
GO

UPDATE request_users
set field_id=16
where user_type_id = 1

UPDATE request_users
set field_id=7
where user_type_id = 2

UPDATE request_users
set field_id=8
where user_type_id = 3


UPDATE request_users
set field_id=9
where user_type_id = 4


UPDATE request_users
set field_id=11
where user_type_id = 6


UPDATE request_users
set field_id=10
where user_type_id = 5
GO
