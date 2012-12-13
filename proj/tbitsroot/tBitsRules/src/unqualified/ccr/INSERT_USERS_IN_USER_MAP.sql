
declare @firstId int 
declare @epmId int 
declare @ccId int 

select @firstId=user_id from users where user_login='uday'
select @epmId=user_id from users where user_login='nitiraj'
select @ccId=user_id from users where user_login='sandeep'

if( (@firstId is null ) OR ( @epmId  is null ) OR ( @ccId is null ) )
	begin
		print 'first or epm or cc do not exists. Please check'
	end

insert into ccr_user_map
values (@firstId, 'CCR_MAP', 1, @epmId)

insert into ccr_user_map
values (@epmId, 'CC_MAP', 1, @ccId )

select * from ccr_user_map