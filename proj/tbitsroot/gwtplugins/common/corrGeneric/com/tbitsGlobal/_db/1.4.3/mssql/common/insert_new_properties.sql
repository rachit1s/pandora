if not exists (select * from corr_properties where property_name = 'protocol_options_name_cache_size' )
 insert into corr_properties values ('protocol_options_name_cache_size','40','new property' )


if not exists ( select * from corr_properties where property_name = 'protocol_options_name_cache_window_size' ) 
 insert into corr_properties values ('protocol_options_name_cache_window_size','40','new property' )
