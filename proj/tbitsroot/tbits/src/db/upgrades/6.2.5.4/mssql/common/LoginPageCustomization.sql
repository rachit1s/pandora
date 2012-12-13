if NOT EXISTS(select * from tbits_properties where name='transbit.tbits.auth.tip')
insert into tbits_properties (name,value) values ('transbit.tbits.auth.tip','You can press CTRL+U to update a request on request details page.$In the search results putting the mouse cursor displays a menu for View or Update orAdd Sub request menu$You can create hierarchy of requests of any depth$Instead of opening a browser you can directly reply to a mail to take an action on requests');
if NOT EXISTS(select * from tbits_properties where name='transbit.tbits.address.contact')
insert into tbits_properties(name,value) values ('transbit.tbits.address.contact',' ');
if NOT EXISTS(select * from tbits_properties where name='transbit.tbits.auth.emailto')
insert into tbits_properties (name,value) values ('transbit.tbits.auth.emailto',' ');
