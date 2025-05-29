
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gw_db_field]') AND type in (N'U'))
DROP TABLE [dbo].[gw_db_field]
GO
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gw_db_object]') AND type in (N'U'))
DROP TABLE [dbo].[gw_db_object]
GO
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gw_field_permission]') AND type in (N'U'))
DROP TABLE [dbo].[gw_field_permission]
GO
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gw_query_log]') AND type in (N'U'))
DROP TABLE [dbo].[gw_query_log]
GO
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gw_row_permission]') AND type in (N'U'))
DROP TABLE [dbo].[gw_row_permission]
GO
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gw_api_key]') AND type in (N'U'))
DROP TABLE [dbo].[gw_api_key]
GO
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gw_datasource_config]') AND type in (N'U'))
DROP TABLE [dbo].[gw_datasource_config]
GO
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gw_permission_set_user]') AND type in (N'U'))
DROP TABLE [dbo].[gw_permission_set_user]
GO
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gw_permission_set]') AND type in (N'U'))
DROP TABLE [dbo].[gw_permission_set]
GO

