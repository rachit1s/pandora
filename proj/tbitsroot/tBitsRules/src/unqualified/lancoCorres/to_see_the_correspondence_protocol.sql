create table #protocol_tmp
(
	type int ,
	typeName varchar ( 10 ) 
)

insert into #protocol_tmp
values ( 1, 'TO' )

insert into #protocol_tmp
values ( 2, 'OUR_CC' )

insert into #protocol_tmp
values ( 3, 'YOUR_CC' )


select p.user_login,p.display_name, p.sub_corres, t.typeName ,q.user_login,q.display_name,q.first_name,q.last_name
from 
	(	select u.user_login,u.first_name,u.last_name,u.display_name,k.sub_corres,k.recepient_type,k.recepient_id 
		from ksk_user_map k 
			join users u on u.user_id=k.user_id
	) p left join users q on p.recepient_id = q.user_id join #protocol_tmp t on p.recepient_type=t.type
order by p.sub_corres,p.user_login,t.type 

drop table #protocol_tmp
