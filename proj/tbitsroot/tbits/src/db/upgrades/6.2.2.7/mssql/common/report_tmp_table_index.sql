IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='report_tmp_table_index') 
create table report_tmp_table_index
(
 report_table_name varchar(200),
 last_actual_run_time  datetime,
 cache_expiry_duration_in_seconds bigint,

)
else 
print'Table already exist'
