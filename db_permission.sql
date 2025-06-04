-- DB Servers
SELECT 'gw_datasource_config', * FROM gw_datasource_config
SELECT 'gw_db_object', * FROM gw_db_object
SELECT 'gw_db_field', * FROM gw_db_field

-- Permissions Set
SELECT 'gw_permission_set', * FROM gw_permission_set where id=1
SELECT 'gw_permission_set_user', * FROM gw_permission_set_user where permission_set_id = 1
SELECT 'gw_operation_permission', * FROM gw_operation_permission where permission_set_id = 1
SELECT 'gw_field_permission', * FROM gw_field_permission where permission_set_id = 1
SELECT 'gw_row_permission', * FROM gw_row_permission where permission_set_id = 1
SELECT 'gw_api_key', * FROM gw_api_key where permission_set_id = 1

-- SFTP servers
SELECT 'gw_sftp_server', * FROM gw_sftp_server 

--SFTP Permissions
SELECT 'gw_sftp_credential', * FROM gw_sftp_credential where permission_set_id in (1, 2,3)
SELECT 'gw_sftp_object', * FROM gw_sftp_object --where permission_set_id = 1
SELECT 'gw_sftp_permission', * FROM gw_sftp_permission where permission_set_id = 1
