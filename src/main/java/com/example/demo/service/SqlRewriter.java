package com.example.demo.service;

import com.example.demo.domain.FieldPermission;
import com.example.demo.domain.RowPermission;
import com.example.demo.domain.PermissionSet;
import com.example.demo.repository.FieldPermissionRepository;
import com.example.demo.repository.RowPermissionRepository;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Rewrites simple SELECTs with explicit columns and injects row-level filters.
 */
@Component
public class SqlRewriter {
    private final FieldPermissionRepository fieldPermRepo;
    private final RowPermissionRepository rowPermRepo;

    public SqlRewriter(FieldPermissionRepository fieldPermRepo,
                       RowPermissionRepository rowPermRepo) {
        this.fieldPermRepo = fieldPermRepo;
        this.rowPermRepo = rowPermRepo;
    }

    /**
     * Rewrite a parsed Select to enforce field and row permissions.
     * Returns modified Select AST.
     */
    public Select rewrite(Select select, List<PermissionSet> sets) {
        SelectBody body = select.getSelectBody();
        if (!(body instanceof PlainSelect)) {
            throw new RuntimeException("Only simple SELECT supported");
        }
        PlainSelect ps = (PlainSelect) body;
        // Map aliases to real table names
        Map<String, String> aliasMap = new LinkedHashMap<>();
        addFrom(ps.getFromItem(), aliasMap);
        if (ps.getJoins() != null) {
            for (Join j : ps.getJoins()) {
                addFrom(j.getRightItem(), aliasMap);
            }
        }
        // Enforce read permission done by caller
        // Rewrite SELECT items (explicit columns only)
        List<SelectItem> newItems = new ArrayList<>();
        for (SelectItem si : ps.getSelectItems()) {
            if (si instanceof SelectExpressionItem) {
                Expression expr = ((SelectExpressionItem) si).getExpression();
                if (!(expr instanceof Column)) {
                    throw new RuntimeException("Unsupported select expression: " + expr);
                }
                Column c = (Column) expr;
                String alias = c.getTable() != null ? c.getTable().getName() : aliasMap.keySet().iterator().next();
                String tbl = aliasMap.get(alias);
                // field-level enforcement
                boolean ok = fieldPermRepo.findByPermissionSetInAndObjectName(sets, tbl)
                        .stream()
                        .flatMap(fp -> Arrays.stream(fp.getFields().split(",")))
                        .map(String::trim)
                        .anyMatch(col -> col.equalsIgnoreCase(c.getColumnName()));
                if (!ok) {
                    throw new RuntimeException("Field not permitted: " + c.getColumnName());
                }
                newItems.add(si);
            } else {
                throw new RuntimeException("Only explicit columns supported in parser");
            }
        }
        ps.setSelectItems(newItems);
        // Inject row filters
        Expression where = ps.getWhere();
        Expression combined = where;
        for (Map.Entry<String, String> e : aliasMap.entrySet()) {
            String alias = e.getKey(), tbl = e.getValue();
            List<RowPermission> rules = rowPermRepo.findByPermissionSetInAndObjectName(sets, tbl);
            Expression tableExp = null;
            for (RowPermission rp : rules) {
                Column col = new Column(alias + "." + rp.getFieldName());
                BinaryExpression cond;
                switch (rp.getOperator()) {
                    case ">": cond = new GreaterThan(); break;
                    case "<": cond = new MinorThan(); break;
                    case ">=": cond = new GreaterThanEquals(); break;
                    case "<=": cond = new MinorThanEquals(); break;
                    case "<>": cond = new NotEqualsTo(); break;
                    default: cond = new EqualsTo();
                }
                cond.setLeftExpression(col);
                String rv = rp.getValue();
                Expression valExpr = rv.matches("\\d+(\\.\\d+)?")
                        ? new LongValue(rv)
                        : new StringValue("'" + rv.replace("'", "''") + "'");
                cond.setRightExpression(valExpr);
                tableExp = tableExp == null ? cond : new AndExpression(tableExp, cond);
            }
            if (tableExp != null) {
                combined = combined == null ? tableExp : new AndExpression(combined, tableExp);
            }
        }
        ps.setWhere(combined);
        return select;
    }

    private void addFrom(FromItem fi, Map<String, String> aliasMap) {
        if (!(fi instanceof Table)) {
            throw new RuntimeException("Unsupported FROM item: " + fi);
        }
        Table t = (Table) fi;
        String alias = t.getAlias() != null ? t.getAlias().getName() : t.getName();
        aliasMap.put(alias, t.getName());
    }
}