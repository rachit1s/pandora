if not exists (select * from corr_properties where property_name = 'onbehalf_sys_cache_window_size' )
BEGIN
insert into corr_properties values ('onbehalf_sys_cache_window_size','5','new property' )
insert into corr_properties values ('onbehalf_sys_cache_size','5','new property' )
insert into corr_properties values ('protocol_options_name_cache_size','5','new property' )
insert into corr_properties values ('protocol_options_name_cache_window_size','5','new property' )
END
