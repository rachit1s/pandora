IF NOT EXISTS (select * from sys.objects where name = 'uq_corr_user_entry')
BEGIN
alter table corr_user_map
add constraint uq_corr_user_entry unique ( sys_prefix, user_login, user_map_type1, user_map_type2,user_map_type3,user_type_field_name, user_login_value)
END
