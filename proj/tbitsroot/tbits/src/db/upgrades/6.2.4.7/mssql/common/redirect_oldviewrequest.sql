if NOT EXISTS(select * from tbits_properties where name = 'redirect.oldviewrequest')
insert into tbits_properties (name, value, displayName,Description, Category) VALUES ('transbit.tbits.redirect.oldviewrequest', 'true', 'Redirect Old Urls to Jaguar', 'If this is set true the old urls (emails) will be redirected to jaguar', 'Legacy');
