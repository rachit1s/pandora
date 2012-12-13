IF NOT EXISTS (select * from sys.objects where name = 'uq_corr_field_name_entry')
BEGIN
alter table corr_field_name_map
add constraint uq_corr_field_name_entry unique( corr_field_name,sys_prefix,field_name)
END
