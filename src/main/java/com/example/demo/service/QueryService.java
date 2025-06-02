package com.example.demo.service;

import com.example.demo.domain.ApiKey;
import com.example.demo.domain.FieldPermission;
import com.example.demo.domain.PermissionSet;
import com.example.demo.domain.QueryLog;
import com.example.demo.domain.RowPermission;
import com.example.demo.dto.QueryResponse;
import com.example.demo.repository.ApiKeyRepository;
import com.example.demo.repository.FieldPermissionRepository;
import com.example.demo.repository.DbObjectRepository;
import com.example.demo.repository.PermissionSetRepository;
import com.example.demo.repository.QueryLogRepository;
import com.example.demo.repository.RowPermissionRepository;
import com.example.demo.repository.OperationPermissionRepository;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import com.example.demo.service.SqlRewriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import com.example.demo.service.SimpleSqlParserService;
import net.sf.jsqlparser.JSQLParserException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

@Service
public class QueryService {
    private final ApiKeyRepository apiKeyRepository;
    private final FieldPermissionRepository fieldPermissionRepository;
    private final RowPermissionRepository rowPermissionRepository;
    private final DbObjectRepository dbObjectRepository;
    private final PermissionSetRepository permissionSetRepository;
    private final JdbcTemplate defaultJdbcTemplate;
    private final QueryLogRepository queryLogRepository;
    private final DataSourceService dataSourceService;
    private final OperationPermissionRepository operationPermissionRepository;
    private final SqlRewriter sqlRewriter;
    private final SimpleSqlParserService simpleSqlParserService;
    // TODO: integrate AST-based SQL rewriter for joins

    public QueryService(ApiKeyRepository apiKeyRepository,
                        FieldPermissionRepository fieldPermissionRepository,
                        RowPermissionRepository rowPermissionRepository,
                        PermissionSetRepository permissionSetRepository,
                        DbObjectRepository dbObjectRepository,
                        DataSourceService dataSourceService,
                        JdbcTemplate jdbcTemplate,
                        QueryLogRepository queryLogRepository,
                        OperationPermissionRepository operationPermissionRepository,
                        SqlRewriter sqlRewriter,
                        SimpleSqlParserService simpleSqlParserService) {
        this.apiKeyRepository = apiKeyRepository;
        this.fieldPermissionRepository = fieldPermissionRepository;
        this.rowPermissionRepository = rowPermissionRepository;
        this.permissionSetRepository = permissionSetRepository;
        this.dbObjectRepository = dbObjectRepository;
        this.dataSourceService = dataSourceService;
        this.defaultJdbcTemplate = jdbcTemplate;
        this.queryLogRepository = queryLogRepository;
        this.operationPermissionRepository = operationPermissionRepository;
        this.sqlRewriter = sqlRewriter;
        this.simpleSqlParserService = simpleSqlParserService;
    }

    // Query prefix strategy: no physical prefix for external tables
    @Transactional
    public QueryResponse executeQuery(String apiKeyValue,
                                      Long userId,
                                      String rawSql,
                                      Long serverId) {
        // Validate API key
        ApiKey apiKey = apiKeyRepository.findByKey(apiKeyValue)
                .orElseThrow(() -> new RuntimeException("Invalid API key"));
        if (apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("API key expired");
        }
        // Determine all permission sets for this user
        java.util.List<PermissionSet> sets =
                permissionSetRepository.findByUserIdsContains(userId);
        if (sets.isEmpty()) {
            throw new RuntimeException("No permission sets for user: " + userId);
        }
        try {
            // Normalize and parse SQL (strip comments, drop trailing semicolon)
            String cleanedSql = normalizeSql(rawSql);
            Statement stmt;
            try {
                stmt = CCJSqlParserUtil.parse(cleanedSql);
            } catch (JSQLParserException e) {
                // Fallback to simple parser on parse errors
                SimpleSqlParserService.SqlParts parts = simpleSqlParserService.parse(rawSql);
                // TODO: integrate parts into permission logic
                throw new RuntimeException(
                    "Fallback parser extracted tables=" + parts.tables +
                    ", fields=" + parts.fields,
                    e
                );
            }
            String tableName;
            int rowsReturned;
            String adjustedSql;
            JdbcTemplate jt = (serverId != null)
                    ? dataSourceService.getJdbcTemplate(serverId)
                    : defaultJdbcTemplate;
            // Handle basic SELECT * FROM <table> via AST
            if (stmt instanceof Select) {
                Select selectStmt = (Select) stmt;
                if (!(selectStmt.getSelectBody() instanceof PlainSelect)) {
                    throw new RuntimeException("Only simple SELECT * FROM <table> supported");
                }
                PlainSelect plain = (PlainSelect) selectStmt.getSelectBody();
                // Collect all tables: main FROM and any JOINs
                List<Table> tables = new ArrayList<>();
                tables.add((Table) plain.getFromItem());
                if (plain.getJoins() != null) {
                    for (Join join : plain.getJoins()) {
                        FromItem joinItem = join.getRightItem();
                        if (!(joinItem instanceof Table)) {
                            throw new RuntimeException("Only simple JOINs on tables supported");
                        }
                        tables.add((Table) joinItem);
                    }
                }
                // Determine if original SELECT uses wildcard (* or alias.*)
                List<SelectItem> origItems = plain.getSelectItems();
                boolean hasWildcard = origItems.stream().anyMatch(si ->
                        si instanceof AllColumns || si instanceof AllTableColumns);
                if (hasWildcard) {
                    // Expand SELECT * into per-table allowed columns
                    List<SelectItem> newSelectItems = new ArrayList<>();
                    for (Table tbl : tables) {
                        String tblName = tbl.getName();
                        String alias = tbl.getAlias() != null ? tbl.getAlias().getName() : tblName;
                        List<FieldPermission> fpsList = fieldPermissionRepository.findByPermissionSetInAndObjectName(sets, tblName);
                        if (fpsList.isEmpty()) {
                            throw new RuntimeException("No field permissions for table: " + tblName);
                        }
                        Set<String> colNames = new LinkedHashSet<>();
                        for (FieldPermission fp : fpsList) {
                            for (String fld : fp.getFields().split(",")) {
                                String f = fld.trim();
                                if (!f.isEmpty()) colNames.add(f);
                            }
                        }
                        for (String f : colNames) {
                            newSelectItems.add(new SelectExpressionItem(new Column(alias + "." + f)));
                        }
                    }
                    plain.setSelectItems(newSelectItems);
                } else {
                    // Validate explicit column list against permissions
                    // Build alias -> table name map
                    Map<String, String> aliasToTable = new HashMap<>();
                    for (Table tbl : tables) {
                        String real = tbl.getName();
                        String alias = tbl.getAlias() != null ? tbl.getAlias().getName() : real;
                        aliasToTable.put(alias, real);
                    }
                    List<SelectItem> validated = new ArrayList<>();
                    for (SelectItem si : origItems) {
                        if (!(si instanceof SelectExpressionItem)) {
                            throw new RuntimeException("Unsupported select item: " + si);
                        }
                        Expression expr = ((SelectExpressionItem) si).getExpression();
                        if (!(expr instanceof Column)) {
                            throw new RuntimeException("Only simple column references allowed: " + expr);
                        }
                        Column col = (Column) expr;
                        String prefix = col.getTable() != null ? col.getTable().getName() : null;
                        String realTable = prefix != null && aliasToTable.containsKey(prefix)
                                ? aliasToTable.get(prefix)
                                : (tables.size() == 1 ? tables.get(0).getName() : null);
                        if (realTable == null) {
                            throw new RuntimeException("Cannot resolve table for column: " + col);
                        }
                        // check field permission
                        List<FieldPermission> fpsList = fieldPermissionRepository.findByPermissionSetInAndObjectName(sets, realTable);
                        boolean ok = fpsList.stream().anyMatch(fp -> Arrays.stream(fp.getFields().split(","))
                                .map(String::trim).anyMatch(fld -> fld.equals(col.getColumnName())));
                        if (!ok) {
                            throw new RuntimeException("No permission for column: " + col);
                        }
                        validated.add(si);
                    }
                    plain.setSelectItems(validated);
                }
                // Build row-level predicate expression
                Expression rowPredicate = null;
                for (Table tbl : tables) {
                    String tblName = tbl.getName();
                    String alias = (tbl.getAlias() != null) ? tbl.getAlias().getName() : tblName;
                    List<RowPermission> rpList = rowPermissionRepository.findByPermissionSetInAndObjectName(sets, tblName);
                    for (RowPermission rp : rpList) {
                        String rawVal = rp.getValue();
                        String val = rawVal.matches("\\d+(\\.\\d+)?") ? rawVal : ("'" + rawVal.replace("'","''") + "'");
                        String exprStr = alias + "." + rp.getFieldName() + " " + rp.getOperator() + " " + val;
                        Expression rpExpr;
                        try {
                            rpExpr = CCJSqlParserUtil.parseExpression(exprStr);
                        } catch (Exception e) {
                            throw new RuntimeException("Invalid row permission expression: " + exprStr, e);
                        }
                        rowPredicate = (rowPredicate == null) ? rpExpr : new AndExpression(rowPredicate, rpExpr);
                    }
                }
                // Combine with existing WHERE if present
                Expression existingWhere = plain.getWhere();
                if (rowPredicate != null) {
                    Expression newWhere = (existingWhere != null)
                        ? new AndExpression(existingWhere, rowPredicate)
                        : rowPredicate;
                    plain.setWhere(newWhere);
                }
                // Serialize AST back to SQL
                adjustedSql = selectStmt.toString();
                List<Map<String, Object>> results = jt.queryForList(adjustedSql);
                int count = results.size();
                logAndSave(apiKey, rawSql, adjustedSql, count);
                return new QueryResponse(rawSql, adjustedSql, results, count);
            }
            // Handle INSERT
            if (stmt instanceof net.sf.jsqlparser.statement.insert.Insert) {
                var ins = (net.sf.jsqlparser.statement.insert.Insert) stmt;
                tableName = ins.getTable().getName();
                enforceOp(sets, tableName, "C");
                adjustedSql = rawSql;
                rowsReturned = jt.update(adjustedSql);
                logAndSave(apiKey, rawSql, adjustedSql, rowsReturned);
                return new QueryResponse(rawSql, adjustedSql, List.of(), rowsReturned);
            }
            // Handle UPDATE
            if (stmt instanceof net.sf.jsqlparser.statement.update.Update) {
                var upd = (net.sf.jsqlparser.statement.update.Update) stmt;
                tableName = upd.getTable().getName();
                enforceOp(sets, tableName, "U");
                adjustedSql = rawSql;
                rowsReturned = jt.update(adjustedSql);
                logAndSave(apiKey, rawSql, adjustedSql, rowsReturned);
                return new QueryResponse(rawSql, adjustedSql, List.of(), rowsReturned);
            }
            // Handle DELETE
            if (stmt instanceof net.sf.jsqlparser.statement.delete.Delete) {
                var del = (net.sf.jsqlparser.statement.delete.Delete) stmt;
                tableName = del.getTable().getName();
                enforceOp(sets, tableName, "D");
                adjustedSql = rawSql;
                rowsReturned = jt.update(adjustedSql);
                logAndSave(apiKey, rawSql, adjustedSql, rowsReturned);
                return new QueryResponse(rawSql, adjustedSql, List.of(), rowsReturned);
            }
            // Unsupported operation
            throw new RuntimeException("Unsupported SQL operation");
        } catch (Exception e) {
            throw new RuntimeException("Error processing query: " + e.getMessage(), e);
        }
    }

    private void enforceOp(List<PermissionSet> sets, String tableName, String code) {
        boolean allowed = operationPermissionRepository
                .findByPermissionSetInAndObjectName(sets, tableName)
                .stream().anyMatch(op -> op.getOperations().contains(code));
        if (!allowed) {
            throw new RuntimeException(code + " not allowed on table: " + tableName);
        }
    }

    private void logAndSave(ApiKey apiKey, String rawSql, String adjustedSql, int rowsReturned) {
        QueryLog log = new QueryLog();
        log.setApiKey(apiKey);
        log.setRawSql(rawSql);
        log.setAdjustedSql(adjustedSql);
        log.setRowsReturned(rowsReturned);
        log.setExecutedAt(LocalDateTime.now());
        log.setStatus("SUCCESS");
        queryLogRepository.save(log);
    }
    /**
     * Remove SQL comments and trailing semicolon before parsing.
     */
    private static String normalizeSql(String sql) {
        // strip block comments
        String noBlock = sql.replaceAll("(?s)/\\*.*?\\*/", " ");
        // strip line comments
        String noLine = noBlock.replaceAll("(?m)--.*?$", " ");
        String trimmed = noLine.trim();
        if (trimmed.endsWith(";")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}