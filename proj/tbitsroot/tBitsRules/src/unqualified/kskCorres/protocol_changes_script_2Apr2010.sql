declare @fsqid int
declare @dqwid int 
declare @ybyid int 
declare @whid int 
declare @gprid int 
declare @sslid int 
declare @xlid int 
declare @smjid int 

select @xlid = user_id from users where user_login='xueli'
select @smjid = user_id from users where user_login='shen.mj'
select @gprid = user_id from users where user_login='gprao'
select @sslid = user_id from users where user_login='lakshmanan.s'
select @whid = user_id from users where user_login='wangheng'
select @ybyid = user_id from users where user_login='yby'
select @fsqid = user_id from users where user_login='fsq'
select @dqwid = user_id from users where user_login='duqiwei'

--select * from ksk_user_map

update ksk_user_map
set recepient_id = @dqwid
where sub_corres in ( 'CM','CN','CO','HS' )
and recepient_type = 1
and recepient_id = @fsqid

update ksk_user_map
set recepient_id = @dqwid
where sub_corres in ( 'EN','PR','OT' )
and recepient_type = 3
and recepient_id=@fsqid

update ksk_user_map
set user_id = @dqwid
where sub_corres in ( 'CM','CN','PR','OT' )
and user_id=@fsqid

update ksk_user_map
set user_id = @dqwid
where sub_corres in ( 'CO','EN' )
and user_id=@ybyid

delete from ksk_user_map
where sub_corres = 'EN'
and recepient_type=2
and recepient_id=@whid

insert into ksk_user_map
values ( @dqwid,'HS',NULL,NULL )

insert into ksk_user_map
values ( @dqwid,'HS',1,@gprid )

insert into ksk_user_map
values ( @dqwid,'HS',3,@sslid )

insert into ksk_user_map
values ( @dqwid,'HS',2,@xlid )

insert into ksk_user_map
values ( @dqwid,'HS',2,@smjid )

