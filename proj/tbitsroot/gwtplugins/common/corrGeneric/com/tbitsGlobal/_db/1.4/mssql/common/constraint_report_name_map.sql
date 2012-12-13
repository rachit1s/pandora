IF NOT EXISTS (select * from sys.objects where name = 'uq_corr_report_name_entry')
BEGIN
alter table corr_report_name_map
add constraint uq_corr_report_name_entry unique ( report_id, report_file_name)
END
