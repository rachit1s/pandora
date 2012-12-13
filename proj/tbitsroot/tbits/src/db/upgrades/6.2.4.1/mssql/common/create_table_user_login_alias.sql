IF not EXISTS(SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'user_login_alias')
begin
	print 'creating the table user_login_alias'
	create table user_login_alias
	(
		ldap_user_login nvarchar(255),
		tbits_user_login nvarchar(255)
		CONSTRAINT ldap_tbits_user_constraint UNIQUE (ldap_user_login,tbits_user_login)
	)
end
else 
begin
	print 'the table user_login_alias  already exists'
end
