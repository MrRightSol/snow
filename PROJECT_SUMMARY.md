# Database Proxy / Middleware Web App

## Project Goals
- Build a Spring Boot-based proxy/middleware that controls access to multiple databases (initially MS SQL Server, later Oracle, DB2, Teradata).
- Centralize connection management, object discovery, and metadata storage in application tables.
- Allow administrators to define fine-grained permissions:
  - Table-level: SELECT, INSERT, UPDATE, DELETE, CREATE, DROP
  - Field-level: restrict visible columns per table
  - Row-level (RLS): boolean expressions (e.g. `Status = 'Active'`, `DeptId = current_user_dept`)
- Issue time-limited API keys (UUID + api-access-key) tied to a PermissionSet.
- Expose a single `/api/query` endpoint:
  - Accept raw SQL in the body + `X-API-KEY` header
  - Parse & rewrite the SQL to enforce allowed fields and inject RLS predicates
  - Execute against the configured target DB
  - Log every request (raw SQL, adjusted SQL, rows returned, status, timestamp)
  - Return JSON payload with both original and adjusted SQL plus result rows

## Authentication & Authorization
- Basic authentication stub in this prototype (to be replaced by Azure AD + Spring Security).
- Users and roles managed via existing tables (users, roles, pages, permission_list, access_list) omitted for now.
- API keys stored in `api_key` table with expiration.

## Data Model (JPA Entities)
| Entity           | Table Name      | Purpose                                     |
|------------------|-----------------|---------------------------------------------|
| PermissionSet    | permission_set  | Named set of actions + object permissions   |
| FieldPermission  | field_permission| Allowed columns (comma-delimited) per table |
| RowPermission    | row_permission  | RLS expressions per table                   |
| ApiKey           | api_key         | Key string + expiry + link to PermissionSet |
| QueryLog         | query_log       | Audit log of all queries executed          |

## Package Structure
```
com.example.demo
├─ config          # Beans & test data initializer
├─ controller      # REST endpoints (`/api/query`)
├─ domain          # JPA entity classes
├─ repository      # Spring Data JPA interfaces
├─ service         # Core logic: validate API key, rewrite & execute SQL, log
└─ dto             # Request/response DTOs
```

## Prototype Implementation (First Slice)
1. **Schema & Seed Data**
   - H2 in-memory datasource configured in `application.properties`.
   - `TestDataInitializer` creates:
     - `test_table(id, name)` with 2 rows
     - A `PermissionSet` named **TestSet**
     - A `FieldPermission` for **test_table** allowing `id,name`
     - An `ApiKey` value `test-key` expiring in 1 day
2. **QueryService**
   - Validates `X-API-KEY` and expiry
   - Only supports `SELECT * FROM <table>` pattern
   - Looks up allowed fields and optional RLS clause
   - Builds adjusted SQL: `SELECT field1, field2 FROM table [WHERE ...]`
   - Executes via `JdbcTemplate`, captures results
   - Persists a `QueryLog` entry for audit/KPI
3. **QueryController**
   - POST `/api/query`
   - Header: `X-API-KEY`  Body: `{ "sql": "SELECT * FROM test_table" }`
   - Returns:
     ```json
     {
       "originalSql": "SELECT * FROM test_table",
       "adjustedSql": "SELECT id, name FROM test_table",
       "rows": [{ "ID": 1, "NAME": "Alice" }, ...],
       "rowsReturned": 2
     }
     ```

## Next Steps
 - Admin UI in Angular:
   - DataSources page scaffolded (CRUD, test, import)
   - Remaining modules: Metadata (tables/fields), Permissions, API Keys, Query Logs, Dashboard
 - Expand SQL parsing & rewriting: support joins, nested queries, DML/DDL beyond simple SELECT
 - Integrate JSqlParser or AI-based risk analysis for query safety
 - Flesh out Angular UI: hook up to REST endpoints, secure with Azure AD + Spring Security
 - Add comprehensive tests, input validation, and error handling
 - Implement metrics/log purge and data retention policies

---
_Generated on project bootstrap to capture current prototype state._

## Implementation To Date
### Server-side (Spring Boot)
- Entities & Repositories:
  - Core permissions: PermissionSet, FieldPermission, RowPermission
  - API keys: ApiKey
  - Audit logs: QueryLog
  - Multi-DB config: DataSourceConfig
  - Metadata import: DbObject, DbField
  - SFTP proxy: SftpServerConfig, SftpCredential, SftpPermission, SftpObject
- Services:
  - QueryService: validate API key, simple SQL rewrite, execution, logging
  - DataSourceService: dynamic DataSourceConfig handling, Hikari pools, JdbcTemplate per server
  - MetadataService: import and list table/view and column metadata
  - SftpService: manages embedded SFTP proxy (SSHD), authentication, download/upload
  - SftpMetadataService: import and store directory listing metadata
- Controllers & DTOs:
  - QueryController (`/api/query`)
  - DataSourceController (`/api/datasources`)
  - MetadataController (`/api/datasources/{id}/import`, `/objects`, `/objects/{id}/fields`)
  - PermissionController (`/api/permissions`)
  - ApiKeyController (`/api/apikeys`)
  - QueryLogController (`/api/logs`)
  - SftpServerController (`/api/sftp-servers`)
  - SftpCredentialController (`/api/sftp-servers/{serverId}/credentials`)
  - SftpPermissionController (`/api/permissions/sets/{setId}/sftp`)
  - SftpMetadataController (`/api/sftp-servers/{serverId}/objects` and `/import`)
  - SftpFileController (`/api/sftp-servers/{serverId}/download`, `/upload`)
- Configuration & Init:
  - In-memory H2 (schema auto-update) with `TestDataInitializer` seed data
  - Microsoft SQL Server JDBC driver support (mssql-jdbc)

### Client-side (Angular)
- Angular CLI v19 standalone components scaffold
- Angular Material integrated
- Application shell: `AppComponent` with mat-sidenav, mat-toolbar, RouterOutlet
- Routing configured with redirect to `/datasources`
- `DatasourcesListComponent` (MatTable, import action)
- `DataSourceService` for REST API calls