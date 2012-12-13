-------add Unique constraints in sys_id and Field name should be Unique

IF NOT EXISTS( select * from sys.objects where name = 'Uk_sys_field_name')
	BEGIN
    ALTER TABLE fields
    ADD CONSTRAINT Uk_sys_field_name
    UNIQUE(sys_id,name)
    END
ELSE 
   print 'Uk_sys_field_name already exists'
GO

-----------------------user_login should be unique
IF NOT EXISTS( select * from sys.objects where name = 'Uk_user_login') 
   IF NOT Exists(select  Count(user_login) from users group by user_login having count(user_login)>1)
     BEGIN
        ALTER TABLE users
        ADD CONSTRAINT Uk_user_login
        UNIQUE(user_login)
        END
    ELSE 
       print 'duplicate User Login'
  ElSE
     print 'Uk_user_login already exists'
GO
-------------------------------------- type unique key on sys_id,field_id,name

IF NOT EXISTS( select * from sys.objects where name = 'Uk_type_field_name')
	BEGIN
    ALTER TABLE types
    ADD CONSTRAINT Uk_type_field_name
    UNIQUE(sys_id,field_id,name)
    END
ELSE 
   print 'Uk_type_field_name'
GO
-------------------------------------------------business areas BA sys_prefix
IF NOT EXISTS( select * from sys.objects where name = 'Uk_ba_sys_prefix')
	BEGIN
    ALTER TABLE business_areas
    ADD CONSTRAINT Uk_ba_sys_prefix
    UNIQUE(sys_prefix)
    END
ELSE 
   print 'Uk_ba_sys_prefix already exists'
GO

-----------------------------------role name should be unique
IF NOT EXISTS( select * from sys.objects where name = 'Uk_role_field_name')
	BEGIN
    ALTER TABLE roles
    ADD CONSTRAINT Uk_role_field_name
    UNIQUE(sys_id,rolename)
    END
ELSE 
   print 'Uk_role_field_name'
GO



-------------
