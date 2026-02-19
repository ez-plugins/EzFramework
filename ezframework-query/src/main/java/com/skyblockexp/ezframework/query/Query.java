package com.skyblockexp.ezframework.query;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple query representation: equality / like conditions keyed by field name.
 */
public class Query {
    private final LinkedHashMap<String, Condition> conditions = new LinkedHashMap<>();
    private Integer limit;
    private Integer offset;
    private final java.util.List<String> groupBy = new java.util.ArrayList<>();
    private final java.util.List<String> orderBy = new java.util.ArrayList<>();

    public void addCondition(String key, Condition c) { conditions.put(key, c); }
    public Map<String, Condition> getConditions() { return conditions; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }

    public java.util.List<String> getGroupBy() { return groupBy; }
    public java.util.List<String> getOrderBy() { return orderBy; }
}
