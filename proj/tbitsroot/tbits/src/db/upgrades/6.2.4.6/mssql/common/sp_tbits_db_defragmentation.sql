CREATE PROCEDURE sp_tbits_db_defragmentation
AS
BEGIN

SELECT
    RowNum = ROW_NUMBER() OVER(ORDER BY t.TABLE_NAME)
    ,TableName = t.TABLE_SCHEMA + '.' + t.TABLE_NAME
    ,AlterMe = 'ALTER INDEX ALL ON [' + t.TABLE_SCHEMA + '].[' + t.TABLE_NAME + '] REBUILD WITH (FILLFACTOR = 80, SORT_IN_TEMPDB = ON,
              STATISTICS_NORECOMPUTE = ON);'
INTO #Reindex_Tables
FROM INFORMATION_SCHEMA.TABLES t
WHERE TABLE_TYPE = 'BASE TABLE'
 
DECLARE @Iter INT
DECLARE @MaxIndex INT
DECLARE @ExecMe VARCHAR(MAX)
 
SET @Iter = 1
SET @MaxIndex =
(
    SELECT COUNT(1)
    FROM #Reindex_Tables
)
 
WHILE @Iter < @MaxIndex
BEGIN
    SET @ExecMe =
    (
        SELECT AlterMe
        FROM #Reindex_Tables
        WHERE RowNum = @Iter
    )
 
    EXEC (@ExecMe)
    PRINT @ExecMe + ' Executed'
 
    SET @Iter = @Iter + 1
END



drop table #Reindex_Tables


END
GO