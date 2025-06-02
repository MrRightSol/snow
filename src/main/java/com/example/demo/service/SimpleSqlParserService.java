package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight fallback SQL parser for simple SELECT statements.
 */
@Service
public class SimpleSqlParserService {

    public static class SqlParts {
        public List<String> fields = new ArrayList<>();
        public List<String> tables = new ArrayList<>();
        public List<String> joins = new ArrayList<>();
        public Optional<String> whereClause = Optional.empty();
    }

    private static final Pattern SELECT_PATTERN =
        Pattern.compile("(?i)^\\s*SELECT\\s+(.*?)\\s+FROM\\s+", Pattern.DOTALL);
    private static final Pattern FROM_PATTERN =
        Pattern.compile("(?i)\\sFROM\\s+(.*?)(\\s+WHERE\\s+|$)", Pattern.DOTALL);
    private static final Pattern WHERE_PATTERN =
        Pattern.compile("(?i)\\sWHERE\\s+(.*)$", Pattern.DOTALL);
    private static final Pattern JOIN_PATTERN =
        Pattern.compile("(?i)(LEFT\\s+JOIN|RIGHT\\s+JOIN|INNER\\s+JOIN|JOIN)\\s+([^\\s]+)", Pattern.DOTALL);

    /**
     * Parse raw SQL into its component parts (fields, tables, joins, where).
     */
    public SqlParts parse(String rawSql) {
        String sql = normalizeWhitespace(rawSql);
        SqlParts parts = new SqlParts();
        // 1) Extract SELECT ... FROM
        Matcher mSelect = SELECT_PATTERN.matcher(sql);
        if (mSelect.find()) {
            String fieldsSegment = mSelect.group(1);
            parts.fields = splitAndClean(fieldsSegment, ",");
        }
        // 2) Extract FROM segment
        Matcher mFrom = FROM_PATTERN.matcher(sql);
        if (mFrom.find()) {
            String fromSegment = mFrom.group(1);
            parts.tables = splitAndClean(fromSegment, ",");
        }
        // 3) Extract JOINed tables
        Matcher mJoin = JOIN_PATTERN.matcher(sql);
        while (mJoin.find()) {
            parts.joins.add(mJoin.group(2));
        }
        // 4) Extract WHERE clause
        Matcher mWhere = WHERE_PATTERN.matcher(sql);
        if (mWhere.find()) {
            parts.whereClause = Optional.of(mWhere.group(1).trim());
        }
        return parts;
    }

    private String normalizeWhitespace(String s) {
        return s.replaceAll("\\s+", " ")
                .replaceAll("\\s*;\\s*$", "")
                .trim();
    }

    private List<String> splitAndClean(String segment, String delimiterRegex) {
        String[] tokens = segment.split(delimiterRegex);
        List<String> out = new ArrayList<>();
        for (String t : tokens) {
            String c = t.trim();
            if (!c.isEmpty()) out.add(c);
        }
        return out;
    }
}