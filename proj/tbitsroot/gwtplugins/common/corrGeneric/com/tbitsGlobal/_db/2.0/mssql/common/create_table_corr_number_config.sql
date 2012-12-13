IF NOT EXISTS ( SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[corr_number_config]') AND type in (N'U'))
BEGIN
 
create table corr_number_config
(
	id int identity,
	sys_prefix varchar(32),
	num_type1 varchar(255),
	num_type2 varchar(255),
	num_type3 varchar(255),
	num_format varchar(1023),
	num_fields varchar(1023),
	max_id_format varchar(1023),
	max_id_fields varchar(1023)

	UNIQUE(sys_prefix,num_type1,num_type2,num_type3) 
)

END
