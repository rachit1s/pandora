declare @cvkid int
declare @gprid int 
declare @avgid int 
declare @fsqid int
declare @ybyid int
declare @xlid int
declare @smjid int 
declare @gkbid int
declare @sslid int

select @cvkid=user_id from users where user_login='prasad.c'
select @gprid=user_id from users where user_login='gprao'
select @gkbid=user_id from users where user_login='ganesh.b'
select @sslid=user_id from users where user_login='lakshmanan.s'
select @avgid=user_id from users where user_login='venugopalrao.a'

select @fsqid=user_id from users where user_login='fsq'
select @smjid=user_id from users where user_login='shen.mj'
select @xlid=user_id from users where user_login='xueli'
select @ybyid=user_id from users where user_login='yby'

--select @cvkid,  @gprid, @gkbid ,@sslid, @avgid, @fsqid ,@smjid, @xlid ,@ybyid

insert into ksk_user_map
values ( @cvkid,'CO', NULL, NULL ) 

insert into ksk_user_map
values (@cvkid,'CO', 1, @fsqid )

insert into ksk_user_map
values (@cvkid , 'CO', 2, @gprid ) 

insert into ksk_user_map
values (@cvkid , 'CO', 2, @avgid )

insert into ksk_user_map
values (@cvkid , 'CO', 3, @xlid )

insert into ksk_user_map
values (@cvkid , 'CO', 3, @smjid ) 


insert into ksk_user_map
values( @xlid , 'QL', 3, @cvkid ) 

insert into ksk_user_map
values( @fsqid , 'QL', 3, @cvkid ) 

insert into ksk_user_map
values( @xlid , 'PL', 3, @cvkid ) 

insert into ksk_user_map
values( @fsqid , 'PL', 3, @cvkid ) 

insert into ksk_user_map
values( @xlid , 'HS', 3, @cvkid ) 

insert into ksk_user_map
values( @fsqid , 'HS', 3, @cvkid ) 

insert into ksk_user_map
values( @xlid , 'OT', 3, @cvkid ) 

insert into ksk_user_map
values( @fsqid , 'OT', 3, @cvkid ) 

insert into ksk_user_map
values( @fsqid , 'CO', 3, @cvkid ) 

insert into ksk_user_map
values( @ybyid , 'CO', 3, @cvkid ) 

insert into ksk_user_map
values( @sslid , 'QL', 2, @cvkid ) 

insert into ksk_user_map
values( @gkbid , 'PL', 2, @cvkid ) 

insert into ksk_user_map
values( @sslid , 'HS', 2, @cvkid ) 

insert into ksk_user_map
values( @gprid , 'OT', 2, @cvkid ) 
 
 
