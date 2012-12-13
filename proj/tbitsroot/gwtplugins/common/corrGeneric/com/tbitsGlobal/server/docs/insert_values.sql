insert into corr_properties
values ( 'fieldNameCache.size','4','The Corr Field to BA field name mapping cache size. Cache size 1 means field-name mapping for 1 BA will be saved' )

insert into corr_properties
values ( 'fieldNameCache.window.size','10','The window size used for LRU for the fieldName cache ' )

insert into corr_properties
values ( 'protocolOptionsCache.size','4','The size of the cache for protocols-options of BA. Cache size 1 means protocol-option mapping for 1 BA will be saved' )

insert into corr_properties
values ( 'protocolOptionsCache.window.size','10','The window size used for LRU window size for the protocol-option cache ' )

insert into corr_properties
values ( 'userMapCache.size','4','The user-map mapping cache size. Cache size 1 means mapping for 1 user 1 ba  will be cached' )

insert into corr_properties
values ( 'userMapCache.window.size','10','The window size used for LRU for user-map caching' )


insert into corr_properties
values ( 'onBehalfUserCache.size','4','The onbehalf-map mapping cache size. Cache size 1 means mapping for 1 user 1 ba  will be cached' )

insert into corr_properties
values ( 'onBehalfUserCache.window.size','10','The window size used for LRU for onbehalf-map caching' )

insert into corr_properties
values ( 'reportMapCache.size','4','The report-map mapping cache size. Cache size 1 means mapping report-mapping for 1 ba will be cached' )

insert into corr_properties
values ( 'reportMapCache.window.size','10','The window size used for LRU for report-map caching' )

insert into corr_properties
values ( 'reportNameMapCache.size','4','The report-map mapping cache size. Cache size 1 means mapping report-mapping for 1 ba will be cached' )

insert into corr_properties
values ( 'reportNameMapCache.window.size','10','The window size used for LRU for report-map caching' )

insert into corr_properties
values ( 'reportParamsCache.size','4','The report-map mapping cache size. Cache size 1 means mapping report-mapping for 1 ba will be cached' )

insert into corr_properties
values ( 'reportParamsCache.window.size','10','The window size used for LRU for report-map caching' )

insert into corr_properties
values ( 'commaSeparatedListOfApplicableBa', 'kdi_di' , ' comma separated BAs for which the protocol is applicable' )


insert into corr_field_name_map
values ( 'onbehalf_type1', 'kdi_id', 'category_id')

insert into corr_field_name_map
values ( 'onbehalf_type2', 'kdi_di', 'severity_id')

insert into corr_field_name_map
values ( 'onbehalf_of_login', 'kdi_id', 'CorrLogger')

insert into corr_field_name_map
values ( 'usermap_type1', 'kdi_id', 'category_id')




-- ba specific

insert into corr_field_name_map
values ( 'correspondence_file_field_name','kdi_di','CorrespondanceFile')

insert into corr_field_name_map
values ( 'generate_correspondence_field_name', 'kdi_di','GenerateCorrespondence')

insert into corr_report_map
values( 'kdi_di',null,null,null,null,null,1)






--insert into corr_field_name_map values ( 'OnBehalfType1','kdi_corr','category_id')
--insert into corr_field_name_map values ( 'OnBehalfType2','kdi_corr','severity_id')
--insert into corr_field_name_map values ( 'LoggerFieldName','kdi_corr','logger_ids')
--insert into corr_field_name_map values ( 'Originator','kdi_corr','status_id')
--insert into corr_field_name_map values ( 'GenerationAgencyFieldName','kdi_corr','CorrGenerationAgency')
--insert into corr_field_name_map values ( 'GenerateCorrespondenceFieldName','kdi_corr','GenerateCorrespondence')
--insert into corr_field_name_map values ( 'CorrespondenceNumberFieldName','kdi_corr','CorrespondanceNumber')
--insert into corr_field_name_map values ( 'RepientUserTypeFieldName','kdi_corr','assignee_ids')
--insert into corr_field_name_map values ( 'UserMapType1','kdi_corr','category_id')
--insert into corr_field_name_map values ( 'UserMapType2','kdi_corr','severity_id')
--
--
--insert into corr_onbehalf_map values ( 'kdi_corr', 'root', 'LTHOLTSO', 'CM', null , 'dc_desein' )





