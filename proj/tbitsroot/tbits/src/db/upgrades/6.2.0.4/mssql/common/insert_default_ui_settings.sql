if not exists (select * from tbits_properties where name = 'transbit.tbits.isclassicui')
insert into tbits_properties (name, value) values ('transbit.tbits.isclassicui', 'false')
