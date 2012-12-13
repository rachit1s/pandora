Documentation of CorrGeneric V 1.0 – The Generic Correspondence Module



Chapter 1 : Introduction And Aim

1.1 Aim :
a) to allow only few users to create correspondence.
b) to allow some specified users to create correspondence on-behalf of certain other users
c) to allow a user to create correspondence by selecting only certain configured values of dropdown fields.

1.2 -- Introduction :

The idea of this module is to make the correspondence module as generic as possible by taking the configurations from the database instead of hardcoding them in the code.

How It works:
When the user opens a new request / update request of the BA which is configured for correspondence the some of the configured drop-down fields are preselected based on configuration. This user can only selected certain users as the logger of the request, which are configured from the db.
Then based on values in the form the Assignee, Subscribers, Ccs etc are set from the db.
The user can select appropriate value in GenerateCorrespondence field and see the preview
and submit the request.

Every step will be explained in detail in the next few chapters.


Chapter 2 : Database Structure

There are 9 tables associate with this module as listed under. Each table name is prefixed with 'corr' to make it distinguishable.

1.	corr_field_name_map
-- to map the fields constants used in this module to actuall fields in the ba
2.	corr_onbehalf_map
-- used to set which user can log on behalf of which other users based on certain values in drop-down fields.
3.	corr_user_map 
-- used to set the values in user-type fields (assignee, cc, subscribers or any other user-type field ) based on values in some dropdown fields
4.	corr_properties
-- used to set some generic options which are used for running correspondence module
5.	corr_protocol_options
-- used to set some protocol specific options
6.	corr_ba_field_map
-- used to map the fields which needs to be copied from one ba to another
7.	corr_report_map
-- used to decide which template to use for creating the correspondence pdf
8.	corr_report_name_map
-- used to map the report-id of corr_report_map to actual rptdesign in the tbitsreports folder
9.	corr_report_params_map
-- used to set how the parameters are going to be supplied to the reports in the corr_report_map

Following sections will describe these tables in details.

2.1:	corr_field_name_map
Creation Script 


create table  corr_field_name_map
(
	corr_field_name varchar(128) not null,
	sys_prefix varchar(32) not null,
	field_name varchar(128) not null	
)

Description of table-columns :
There are a number of fields that are required for the proper functioning of correspondence module eg. Logger, Assignee, GenerateCorrespondence dropdown etc.
This table is used to tell the generic plugin actual fields that correspond to these fields in a particular ba.

The column 'corr_field_name' are the constants that correspond to the field names as used in the plugin. 
The column 'sys_prefix' tell for which ba this mapping is valid.
The column 'field_name' contains the actuall field_name for this ba to be used in the plugin.

Ex. lets say we want to set the logger field for correspondence. 
Now lets say in BA1 it should be mapped to logger_ids field but in BA2 it should be mapped to extended user-type field named 'CorrLogger' then the 2 entries will look like this in db 

(onbehalf_of_login, BA1, logger_ids)
(onbehalf_of_login, BA2, CorrLogger)

where onbehalf_of_login is the corr_field_name constant corresponding to the Logger field.

Following is the list of all the constants that can be configured for a BA which follows correspondence module.

1.	onbehalf_type1
2.	onbehalf_type2
3.	onbehalf_type3

4.	user_map_type1
5.	user_map_type2
6.	user_map_type3

7.	report_type1
8.	report_type2
9.	report_type3
10.	report_type4
11.	report_type5
	
12.	onbehalf_of_login
13.	originator_agency
14.	recepient_agency
15.	generation_agency
16.	generate_correspondence
17.	correspondence_number
18.	correspondence_file
19.	recepient_usertype
20.	other_attachment
21.	disable_protocol

The disable_protocol is a special optional corr field
which should be a type field and can have only
two values 'true' and 'false' as its types.
If the selected value in this field is true, then
the whole protocol will be disabled no constraint will
be followed. Including the Corr No. Generation. Rule.


2.2 corr_onbehalf_map

Creation script 

create table corr_onbehalf_map
(
	sys_prefix varchar(32) not null,
	user_login varchar(255) not null,
	onbehalf_type1 varchar(255),
	onbehalf_type2 varchar(255),
	onbehalf_type3 varchar(255),
	onbehalf_of_login varchar(255) not null
)

Description of table-colums

sys_prefix : the sys_prefix of the ba for which this configuration is valid

user_login : the user_login of the user who will be allowed to log on behalf of user in column 'onbehalf_of_login' based on the values in type fields configured.

You can configure 3 type (dropdown) fields viz onbehalf_type1, onbehalf_type2, onbehalf_type3 in the corr_field_name_map to be used in this table.
For example you can configure the category_id to be used as onbehalf_type1 for the ba with sysPrefix 'tbits' in the table corr_field_name_map by creating an entry like ( onbehalf_type1, tbits, category_id ).

After this you can set the type-names of category_id field as the values in onbehalf_type1 column of corr_onbehalf_map table.

You can configure atmost 3 Type fields for this purpose in ba with corr_field_name as 
1.	onbehalf_type1
2.	onbehalf_type2
3.	onbehalf_type3

If none of these are configured then it is assumed that there is direct mapping between user_login and onbehalf_of_login of table corr_onbehalf_map for this ba.
And the three correspondencing columns must be NULL for each entry.

Note that it is an error if a type is configured in corr_field_name_map but NULL is set in the corr_onbehalf_map for that type-entry. 

Ex. Lets say we want to configure the category_id as the onbehalf_type1 and severity_id as onbehalf_type2. But don't want to set the onbehalf_type3 for the ba 'tbits'. Then the entries in corr_field_name_map will be like

(onbehalf_type1, tbits, category_id)
(onbehalf_type2, tbits, category_id)

-- Note no entry corresponding to onbehalf_type3.

Now lets say category_id field contains types as (cat1, cat2, cat3) and severity_id contains types as (sev1, sev2) then the following entries are valid in corr_onbehalf_map

(tbits,user1,cat1,sev1,null,user2)
(tbits,user1,cat1,sev2,null,user2)
(tbits,user1,cat2,sev2,null,user3)
(tbits,user1,cat2,sev2,null,user4)

while some of the invalid entries are
(tbits,user1,null,sev2,null,user4)
(tbits,user1,cat2,null,null,user4)
(tbits,user1,cat2,sev1,sta2,user4)

Each entry of this table say ( sysPrefix1, userLogin1, onbehalfType1, onbehalfType2, onbehalfType3, userLoginValue1 ) means that for the ba with sys_prefix= 'sysPrefix1' if the login user is 'userLogin1' then this user will be able to select the value onbehalfType1 in the dropdown configured for the
corr_field_name 'onbehalf_type1'. After selecting onbehalfType1 user   can select onbehalfType2 and onbehalfType3 in respective type field. After selecting all these three fields the Logger field will contain the value of userLoginValue1 in its user-picker field.

For convenience of user only allowed values will appear in type dropdown onbehalf_type1. And only those values will appear in onbehalf_type2 which are allowed for this user and on the selected value on onbehalf_type1. 
Similarly only those values will appear in onbehalf_type3 which are allowed for this user and on the selected value on onbehalf_type2 and onbehalf_type1.

So you can create a cascading hide/show model for the dropdowns from this table along with the permissioning.

For example with the following entries for ba tbits
(tbits,user1,cat1,sev1,null,user2)
(tbits,user1,cat1,sev2,null,user2)
(tbits,user1,cat2,sev2,null,user3)
(tbits,user1,cat2,sev2,null,user4)

if the login user is user1 then he can select cat1 and cat2 in category_id field but not cat3 as there is no entry corresponding to it. After selecting cat1 the user1 will see both sev1 and sev2 in severity_id field. Byt after selecting cat2 he will see only sev2 in severity_id field. Now if the user1 has selected cat1,sev1 then he can only fill user2 in the logger_ids field. But if he selects cat2,sev2 then he can set user3 OR user4 in the logger_ids field.

2.3	corr_user_map

Creation Script
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

Description of table-columns

1.	sys_prefix : the ba for which this mapping is valid

2.	user_login : this field is mapped to the corr_field_name 'onbehalf_of_login' user-type field in corr_field_name_map 
(Note corr_onbehalf_map's onbehalf_of_login and this table's user_login share the same mapping of corr_field_name_map which is 'onbehalf_of_login')
 
3.	user_map_type1 : type values of a drop-down field configured in corr_field_name_map against the name 'user_map_type1'.
 
4.	user_map_type2 : similar to user_map_type1 
 
5.	user_map_type3 : similar to user_map_type2
 
6.	user_type_field_name : the field into which the values are to be filled.
 
7.	user_login_value : this user_login will be filled in the above user-type-field
 
8.	strictness : this is the value associated with the particular user_type_field and takes one of the 3 values ( 1 = any user is allowed in this field, 2 = all the users present in the mapping should be present and additional users are allowed, 3 = exactly these users must be present in the given field.)

the user_map_typeX can be configured as we had configured onbehalf_typeX to some type fields. 

The difference is that instead of considering the login user we are now considering the first user in the onbehalf_of_login field. We are not following the hide/show of dropdowns in case of user_map_type's. We can set any number of user-type fields with values set in the user_login_values and multiple users in the same field by mapping the same entries with different values in user_login_value.

Lets say the status_id field has type as (open,closed,suspended,reopened)
For example lets say we map status_id field to user_map_type1 for the ba 'tbits'
now I have following entries in user_map table

(tbits,user1,open,null,null,assignee_ids,user2,1)
(tbits,user1,open,null,null,assignee_ids,user3,3)
(tbits,user1,open,null,null,subscriber_ids,user4,1)
(tbits,user1,open,null,null,subscriber_ids,user5,1)

(tbits,user1,closed,null,null,assignee_ids,user4,1)
(tbits,user1,closed,null,null,assignee_ids,user5,2)


(tbits,user2,open,null,null,assignee_ids,user6,1)

from the above entries : 
if onbehalf_of_login field contians user1 then user is allowed to select open and closed in the status_id field. Rest are invalid. 

If the onbehalf_of_login field contains user2 then only open can be selected in status_id field.

If user1 is selected in onbehalf_of_login field and open is selected in user_map_type1 then the fields assignee_ids will be automatically filled with user2,user3 and subscriber_ids will be automatically filled with values user4,user5 . Now the user cannot change the values as he likes as the maximum strictness for the assignee_ids field is 3. If he adds or deletes any user from assignee field then he will get an error on submition or preview. But he can add / delete any user from subscriber_ids as the maximum strictness is 1 for subscriber_ids field entries.

The table entries will be illegal if the user_map_type's are not configured in corr_field_name_map
but values are filled in the corr_user_map table. Also those entries will be illegal which are null in corr_user_map but are configured in corr_field_name_map.


2.4	: corr_properties table
Creation Scripts 

 create table corr_properties
(
	property_name varchar(400) not null,
	property_value varchar(4000) not null,
	property_description varchar(4000),
	UNIQUE(property_name)
)

Description of table-column
property_name : name of the property
property_value : value of the property
property_description : description of the property

This table contains properties which are relate to correspondence module as whole and not to any particular BA which follows the module.
It must contain entries for following property_name

1.	onbehalf_user_cache_size
2.	onbehalf_user_cache_window_size
3.	field_name_cache_size
4.	field_name_cache_window_size
5.	ba_field_cache_size
6.	ba_field_cache_window_size
7.	user_map_cache_size
8.	user_map_cache_window_size
9.	report_map_cache_size
10.	report_map_cache_window_size
11.	report_name_map_cache_size
12.	report_name_map_cache_window_size
13.	report_params_cache_size
14.	report_params_cache_window_size
15.	protocol_options_cache_size
16.	protocol_options_cache_window_size
17.	comma_separated_list_of_applicable_bas

the properties from 1 - 16 are for internal cache management of tables by corr-module. Each of these entries accept integer values (although note that the table column is of varchar type) .
 
The most interesting property is the 17th viz comma_separated_list_of_applicable_bas.
Its value should be the comma separated list of sys_prefixes for which the correspondence module is to be run. This has been kept separately as some one might want to stop Corr. Protocol from one ba but may not want to delete all the other configuration from db. So all the plugin will only run for a particular BA if this entry contains its sys_prefix.


2.5	: corr_protocol_options

Creation Script

  create table corr_protocol_options
  (
  		sys_prefix varchar(32) not null,
  		option_name varchar(255) not null,
  		option_value varchar(4000) not null,
  		option_description varchar(4000)
  )

Description of table-columns
sys_prefix : the ba for which this entry is valid
option_name : the name of the option
option_value : the value of the option
option_description : any description for this option

These options are specific to a particular BA. 
Options which are currently supported by this module are
1.	"protocol_follow_on_behalf";
	permitted values : "yes" // default
				"no"

if set to no then the constraint derived from on_behalf_map will not be followed
for that BA
	
2."more_than_one_logger_allowed";
	permitted values : "yes";
				 "no"; // default

if set to yes then then more than one user will be allowed in the onbehalf_of_login user-type field.

3. "transfer_to_with_update";
	permitted values : sys_prefix of any BA.

4.	"transfer_to_without_update" ;
	permitted values : sys_prefix of any BA.

The properties 3 and 4 are used for Coping the contents of a request from one ba to another ba.

Ex. for an entry like
(BA1,transfer_to_with_update,BA2)

then when we are viewing the any request in of BA1 there will appear a button in the upper right corner of the Tab like 'Transfer to BA2' when clicked on this button a new / update request form will open with the contents of this request copied from BA1 to BA2 based on the entries in table 'corr_ba_field_map'

The difference between transfer_to_with_update and tranfer_to_without_update : 
lets say we have 2 request RBA1 of BA1 and RBA2 of BA2. If we are transfering RBA1 from BA1 to BA2 then in case of trasfer_to_without_update the request will always open as New Request of  BA2.
Now lets say we are trasfering with transfer_to_with_update option. Then there are two possibilities.
1.	there is no related request in RBA1 which is of BA2. (related requests of other Bas does not matter )
2.	RBA1 contains a related request of BA2 say RBA2.

In case of 1. the request will open as New Request in BA2 as was the case with transfer_to_without_update
In case of 2. the request will open as Update Request for RBA2. 

If there are more than one request of BA2 in BA1 then the results are unpredictable.



2.6 corr_ba_field_map

This table defines the mapping of fields which needs to be copied from one BA to another BA during the Transfer_To options.

Creation Script :

create table corr_ba_field_map
(
	from_sys_prefix varchar(32) not null,
	from_field_name varchar(128) not null,
	to_sys_prefix varchar(32) not null,
	to_field_name varchar(128) not null
)

from_sys_prefix : the source ba sys_prefix
from_field_name : the source field name
to_sys_prefix : the destination ba sys_prefix 
to_field_name : the destination field name

	
2.7.	corr_report_map

This table defines which report id to be used on what values of 5 report_types configured for each ba.

Creation Script

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

You can configure maximum of 5 and minimum of 0 Type fields namely report_typeX for each ba in the corr_field_name_map table.
After that you can configure that on what values of which type values combinations which report_id (the report identifier integer ) to be used for report generation.


2.8 corr_report_name_map

This table maps the report idetifier integers used in the corr_report_map to actual report_files.

Creation Script

create table corr_report_name_map
(
	report_id int not null,
	report_file_name varchar(255) not null
)

report_id : the report identifier
report_file_name : the name of the report file to be used. This file must be present in the tbitsreports folder of the installation.


2.9 corr_report_params_map
This table is used to find the parameter values to be fed to the report for generation.

There are 2 kinds of parameter types supported now.
And 3 kinds of parameter value types.

Creation Script

create table corr_report_params_map
(
	report_id varchar(255) not null,
	param_type varchar(32) not null,
	param_name varchar(511) not null,
	param_value_type varchar(32) not null,
	param_value varchar(1024) not null
)

report_id : the report identifier integer used in corr_report_name_map and corr_report_map.
param_type : the type of parameter.
		Two values namely report_param or variable. It should be specified by the report author which parameter is to be marked as what param_type depending on the conventions of Birt.

param_value_type : it defines the type of the value supplied in the param_value 			column of this table.
			As of now three values are supported in this column.

1.	constant : it says that the param_value is a simple string and should be directl passed to the report as is.

2.	java_class : it says that the param_value is the fully qualified name of a Java Class that is present in the plugins folder of tbits.
This class must implement the IreportParamPlugin interface.
The system will create an instance of this plugin and call its 'execute' method 
to get the value that will be passed to the report.

3.	java_object : it says that the param_value is the fully qualified name of the java class present in the tbits plugin folder and implements the IReportJavaObject Interface.

Module will create an object of this class call its initialize method and pass this object directly to the report for its consumption.
Note the interface IreportJavaObject has a method called initialize(CorrObject coob ) which will be passed the CorrObject in context and can be used to initialize itself. 


NOTE : if you have updated the correspondence configurations directly into the database then you must refresh the cache by accessing following url

http://server:port/proxy/corr_clear_cache

This plugin runs in conjugation with corrGeneric classic tbits plugin which contains the contraints that needs to be implemented.
Also for numbering system and report generation a customer specific plugins is required usually named like ORGCorr

