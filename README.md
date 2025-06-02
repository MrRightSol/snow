# snow

## Overview

Snow is a Spring Boot–based proxy/middleware that centralizes:
- DataSource management (SQL Server, Oracle, DB2, Teradata)
- Metadata discovery: tables, views, stored procedures, columns, parameters
- Fine-grained permissions:
  - Table level (SELECT, INSERT, UPDATE, DELETE)
  - Field level (restrict visible columns)
  - Row level (RLS boolean expressions)
- API key authentication and auditing
- Extensible to other backends (e.g. SFTP)

All internal tables are prefixed with `GW_`, and referential integrity is enforced in code (no DB-level foreign keys).

## Configuration

By default, Snow uses an H2 in-memory database (`application.properties`). To switch to SQL Server, run:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=sqlserver
```

The `application-sqlserver.properties` file contains the external DB credentials, URL (with `encrypt=true;trustServerCertificate=true`), and sets the SQL Server dialect. You can override any datasource property via environment variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, etc.

## Seed Data

On startup (default H2 or SQL Server profile), the app auto-seeds:
- A table `test_table(id INT PRIMARY KEY, name VARCHAR(100))` with rows Alice and Bob
- A permission set `TestSet` in `GW_permission_set`
- A mapping to `user_id=1` in `GW_permission_set_user`
- A field permission in `GW_field_permission` allowing `id,name`
- An API key `test-key` (expires in 1 day) in `GW_api_key`

## DataSource API

Manage external database connections:

- **List DataSources**  
  GET `/api/datasources`

- **Create DataSource**  
  POST `/api/datasources`  
  ```json
  {
    "name":"TestSQLServer",
    "url":"jdbc:sqlserver://...;databaseName=...;encrypt=true;trustServerCertificate=true",
    "username":"sa",
    "password":"YourStrong!Passw0rd",
    "driverClassName":"com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "instanceName":""
  }
  ```

- **Test Connection**  
  GET `/api/datasources/{id}/test` → `{ "ok": true }`

(Additional endpoints exist for GET by ID, PUT, DELETE.)

## Metadata Import API

Import tables/views and columns for a DataSource:

- **Import single DataSource**  
  POST `/api/datasources/{id}/import`  
  → Returns list of `{ id, objectName, objectType, fieldCount }`

- **List imported objects**  
  GET `/api/datasources/{id}/objects`

- **List fields for an object**  
  GET `/api/datasources/{id}/objects/{objectId}/fields`

## Batch Metadata Import API

Import across multiple DataSources and object types:

POST `/api/metadata/import`  
```json
{
  "dataSourceIds":[1,2],
  "objectTypes":["TABLE","VIEW","SP"]
}
```
→ `{ "1":[ {...}, ... ], "2":[...] }`

Supported types:
- `TABLE` (BASE TABLE)
- `VIEW`
- `SP` (stored procedures)

Columns/parameters import into `GW_db_field` with:
- `columnName`, `dataType`, `ordinalPosition`
- `length` (CHARACTER_MAXIMUM_LENGTH)
- `columnConstraints` (NOT NULL/NULL, DEFAULT, or parameter mode)

## Permission API

Manage PermissionSets and grant permissions (fields, rows, operations, SFTP):

- **List PermissionSets**  
  GET `/api/permissions/sets`

- **Create PermissionSet**  
  POST `/api/permissions/sets`  
  ```json
  { "name":"AdminSet", "userIds":[1] }
  ```

- **Delete PermissionSet**  
  DELETE `/api/permissions/sets/{id}`

- **Grant field permission**  
  POST `/api/permissions/sets/{id}/fields`  
  ```json
  { "objectName":"test_table", "fields":["id","name"] }
  ```

- **Revoke field permission**  
  DELETE `/api/permissions/fields/{fieldId}`

- **Grant row permission**
  POST `/api/permissions/sets/{id}/rows`  
  ```json
  { "objectName":"test_table", "fieldName":"id", "operator":"=", "value":"1" }
  ```

- **Revoke row permission**  
  DELETE `/api/permissions/rows/{rowId}`

 - **List operation permissions for a set**  
  GET `/api/permissions/sets/{setId}/operations`

 - **Grant operation permission**  
  POST `/api/permissions/sets/{setId}/operations`  
  ```json
  [
  { "objectName":"test_table", "operations":["C","R","U","D"] }
  ]
  ```

 - **Revoke operation permission**  
  DELETE `/api/permissions/sets/{setId}/operations/{permId}`

### SFTP Permissions

Grant or revoke access to SFTP servers and path patterns:

- **List SFTP permissions for a set**  
  GET `/api/permissions/sets/{setId}/sftp`

- **Grant SFTP permission**  
  POST `/api/permissions/sets/{setId}/sftp`  
  ```json
  {
    "serverId":1,
    "pathPattern":"/main/invoices/**",
    "operations":["READ","WRITE"]
  }
  ```

- **Revoke SFTP permission**  
  DELETE `/api/permissions/sets/{setId}/sftp/{permId}`
  DELETE `/api/permissions/rows/{rowId}`

## Query API

Execute SQL with dynamic rewrite per user permissions:

POST `/api/query`  
Headers:
```
X-API-KEY: test-key
X-USER-ID: 1
```
Body:
```json
{ "sql":"SELECT * FROM test_table", "serverId":1 }
```

Processing steps:
1. Validate API key and expiry
2. Lookup all PermissionSets for `userId`
3. Union allowed fields from `GW_field_permission`
4. OR-combine RLS expressions from `GW_row_permission`
5. Build/execute SQL: `SELECT id,name FROM test_table WHERE id=1`
6. Audit to `GW_query_log`

Response:
```json
{
  "originalSql":"SELECT * FROM test_table",
  "adjustedSql":"SELECT id,name FROM test_table WHERE id=1",
  "rows":[...],
  "rowsReturned":1
}
```

## SFTP Proxy Support

Snow now includes an embedded SFTP proxy (Apache MINA SSHD) that listens on the port configured via `sftp.proxy.port` (default 2222). Connections authenticate using credentials stored in `GW_sftp_credential` and enforce directory permissions defined in `GW_sftp_permission`.

### Configuration

- Default listening port: `2222` (override with `sftp.proxy.port` in application properties)
- **Servers** (`GW_sftp_server`):
  - GET    `/api/sftp-servers`
  - POST   `/api/sftp-servers`
  - PUT    `/api/sftp-servers/{id}`
  - DELETE `/api/sftp-servers/{id}`
  - GET    `/api/sftp-servers/{id}/test`  (test connectivity)

- **Credentials** (`GW_sftp_credential`):
  - GET    `/api/sftp-servers/{serverId}/credentials`
  - POST   `/api/sftp-servers/{serverId}/credentials`  (generate username/password tied to a PermissionSet)
  - DELETE `/api/sftp-servers/{serverId}/credentials/{credId}`

- **Permissions** (`GW_sftp_permission`):
  - GET    `/api/permissions/sets/{setId}/sftp`
  - POST   `/api/permissions/sets/{setId}/sftp`
  - DELETE `/api/permissions/sets/{setId}/sftp/{permId}`

### File Metadata

- **Import metadata**:
  - POST `/api/sftp-servers/{serverId}/objects/import?path=/remote/path`

- **List imported objects**:
  - GET  `/api/sftp-servers/{serverId}/objects?path=/remote/path`

### File Download & Upload

All file transfer endpoints require an API key (`X-API-KEY`) and a user ID (`X-USER-ID`) in headers:

- **Download (READ permission)**:
  GET `/api/sftp-servers/{serverId}/download?path=/file.txt`

- **Upload (WRITE permission)**:
  POST `/api/sftp-servers/{serverId}/upload?path=/file.txt`
    - Request body: raw file bytes

Example usage:
```bash
# SFTP client (default port 2222):
sftp -P 2222 <username>@localhost

# Download via REST:
curl -H "X-API-KEY: test-key" -H "X-USER-ID: 1" \
  "http://localhost:8080/api/sftp-servers/1/download?path=/test.txt" > test.txt
``` 

## API Documentation (OpenAPI/Swagger)

Once the application is running, you can access the interactive API docs at:
  http://localhost:8080/swagger-ui.html  (aliases: `/swagger-ui/index.html`)

The OpenAPI spec is served at:
  http://localhost:8080/v3/api-docs