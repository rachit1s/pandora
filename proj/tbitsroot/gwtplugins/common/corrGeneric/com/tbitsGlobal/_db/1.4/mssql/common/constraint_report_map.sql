IF NOT EXISTS (select * from sys.objects where name = 'uq_corr_report_entry')
BEGIN
alter table corr_report_map
add constraint uq_corr_report_entry unique ( sys_prefix, report_type1, report_type2, report_type3, report_type4, report_type5, report_id)
END
