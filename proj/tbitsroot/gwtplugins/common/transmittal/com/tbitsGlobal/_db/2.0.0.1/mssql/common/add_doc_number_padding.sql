if NOT exists (select * from tbits_properties where name = 'lntdcr.docNumberPadding')
BEGIN
insert into tbits_properties (name, value) values ('lntdcr.docNumberPadding', '3');

END

