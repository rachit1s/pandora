/****** Object:  Table [dbo].[tvn_folder_structure]    Script Date: 02/07/2011 18:25:16 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[tvn_folder_structure](
	[sys_id] [int] NULL,
	[_order] [real] NULL,
	[identifier] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF

declare sys_ids cursor for
	select distinct sys_id from folder_structure

declare @sId int
declare @count real
declare @id varchar(50)
		
open sys_ids
fetch next from sys_ids into @sId
while(@@fetch_status <> -1)
	begin
		if(@@fetch_status <> -2)
		declare identifiers cursor for
			select identifier from folder_structure where sys_id=@sId
		set @count=1.0
		open identifiers
		fetch next from identifiers into @id
		while(@@fetch_status <> -1)
			begin
				if(@@fetch_status <> -2)
					insert into tvn_folder_structure values (@sId, @count, @id)
					set @count = @count+1
					fetch next from identifiers into @id
			end
		close identifiers
		deallocate identifiers
		fetch next from sys_ids into @sId
	end
close sys_ids
deallocate sys_ids

drop table folder_structure
