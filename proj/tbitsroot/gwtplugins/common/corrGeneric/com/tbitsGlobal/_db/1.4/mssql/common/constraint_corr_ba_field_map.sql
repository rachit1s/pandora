IF NOT EXISTS (select * from sys.objects where name = 'uq_corr_ba_field_entry')
BEGIN

alter table corr_ba_field_map
add constraint uq_corr_ba_field_entry unique(from_sys_prefix,from_field_name,to_sys_prefix,to_field_name)

END
