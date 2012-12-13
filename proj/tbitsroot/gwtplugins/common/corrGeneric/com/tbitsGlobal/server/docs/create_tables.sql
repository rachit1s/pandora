create table corr_user_map
(
	sys_prefix varchar(32) not null,
	user_login varchar(255) not null,
	user_map_type1 varchar(255),
	user_map_type2 varchar(255),
	user_map_type3 varchar(255),
	user_type_field_name varchar(128) not null,
	user_login_value varchar(255) not null,
	strictness int not null
)

create table corr_report_params_map
(
	report_id varchar(255) not null,
	param_type varchar(32) not null,
	param_name varchar(511) not null,
	param_value_type varchar(32) not null,
	param_value varchar(1024) not null
)

create table corr_report_name_map
(
	report_id int not null,
	report_file_name varchar(255) not null
)

create table corr_report_map
(
	sys_prefix varchar(32) not null,
	report_type1 varchar(255),
	report_type2 varchar(255),
	report_type3 varchar(255),
	report_type4 varchar(255),
	report_type5 varchar(255),
	report_id int not null
)

  create table corr_protocol_options
  (
  		sys_prefix varchar(32) not null,
  		option_name varchar(255) not null,
  		option_value varchar(4000) not null,
  		option_description varchar(4000)
  )

 create table corr_properties
(
	property_name varchar(400) not null,
	property_value varchar(4000) not null,
	property_description varchar(4000),
	UNIQUE(property_name)
)


create table corr_onbehalf_map
(
	sys_prefix varchar(32) not null,
	user_login varchar(255) not null,
	onbehalf_type1 varchar(255),
	onbehalf_type2 varchar(255),
	onbehalf_type3 varchar(255),
	onbehalf_of_login varchar(255) not null
)

create table  corr_field_name_map
(
	corr_field_name varchar(128) not null,
	sys_prefix varchar(32) not null,
	field_name varchar(128) not null	
)
	

create table corr_ba_field_map
(
	from_sys_prefix varchar(32) not null,
	from_field_name varchar(128) not null,
	to_sys_prefix varchar(32) not null,
	to_field_name varchar(128) not null
)



