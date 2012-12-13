ALTER PROCEDURE [dbo].[stp_user_update]
(
	@user_id		int,
	@user_login		nvarchar(255),
	@first_name		nvarchar(255),
	@last_name		nvarchar(255),
	@display_name		nvarchar(255),
	@email			nvarchar(255),
	@is_active		bit,
	@user_type_id		int,
	@web_config		text,
	@windows_config	text,
	@is_on_vacation	bit,
	@is_display		bit,
	@cn			varchar(255),
	@distinguished_name	varchar(255),
	@name			varchar(255),
	@member_of		text,
	@member		text,
	@mail_nickname	varchar(255),
	@location		nvarchar(64),
	@extension		varchar(64),
	@mobile		varchar(64),
	@home_phone		varchar(64),
	@firm_code		 varchar(64),
	@designation	nvarchar(128),
	@firm_address	nvarchar(512),
	@sex			varchar(1),
	@full_firm_name nvarchar(512)
)

AS
UPDATE users
SET 
	user_login 		= @user_login,
	first_name 		= @first_name,
	last_name  		= @last_name,
	display_name  		= @display_name,
	email  			= @email,
	is_active  		= @is_active,
	user_type_id  		= @user_type_id,
	web_config  		= @web_config,
	windows_config  	= @windows_config,
	is_on_vacation  	= @is_on_vacation,
	is_display  		= @is_display,
	cn  			= @cn,
	distinguished_name  	= @distinguished_name,
	name  			= @name,
	member_of  		= @member_of,
	member  		= @member,
	mail_nickname 		= @mail_nickname,
	location			= @location,
	extension		= @extension,
	mobile			= @mobile,
	home_phone		= @home_phone,
	firm_code		= @firm_code,
	designation		= @designation,
	firm_address	= @firm_address,
	sex				= @sex,
	full_firm_name	= @full_firm_name
WHERE
	user_id = @user_id

