IF NOT EXISTS (SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_field_name_map'))
BEGIN

select distinct sys_prefix,user_login,onbehalf_type1,onbehalf_type2,onbehalf_type3,onbehalf_of_login into #tmp_onbehalf_map from corr_onbehalf_map 

delete from corr_onbehalf_map

insert into corr_onbehalf_map select * from #tmp_onbehalf_map

drop table #tmp_onbehalf_map

select distinct sys_prefix,user_login,user_map_type1,user_map_type2,user_map_type3,user_type_field_name,user_login_value,strictness into #tmp_user_map from corr_user_map 

delete from corr_user_map

insert into corr_user_map select * from #tmp_user_map

drop table #tmp_user_map

select distinct from_sys_prefix,from_field_name,to_sys_prefix,to_field_name into #tmp_ba_field_map from corr_ba_field_map        

delete from corr_ba_field_map

insert into corr_ba_field_map select * from #tmp_ba_field_map

drop table #tmp_ba_field_map

select distinct corr_field_name,sys_prefix,field_name into #tmp_field_name_map from corr_field_name_map

delete from corr_field_name_map

insert into corr_field_name_map select * from #tmp_field_name_map

drop table #tmp_field_name_map

select distinct sys_prefix,report_type1,report_type2,report_type3,report_type4,report_type5,report_id into #tmp_report_map from corr_report_map

delete from corr_report_map

insert into corr_report_map select * from #tmp_report_map

drop table #tmp_report_map

select distinct report_id,report_file_name into #tmp_report_name_map from corr_report_name_map

delete from corr_report_name_map

insert into corr_report_name_map select * from #tmp_report_name_map

drop table #tmp_report_name_map


END

