-----------------------------------------------------
If Exists (select *  from roles_users where user_id not in ( select user_id from users ))
	BEGIN
	delete  from roles_users where user_id not in ( select user_id from users )
	END
Else
	Print 'No Orphan roles users here' 
------------------------------------------------------

