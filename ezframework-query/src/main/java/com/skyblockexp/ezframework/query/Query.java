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
    /**
     * Create an empty Query.
     */
    public Query() {}
    /**
     * Add a condition for a field.
     * @param key field name
     * @param c condition
     */
    public void addCondition(String key, Condition c) { conditions.put(key, c); }

    /**
     * Get the currently configured conditions keyed by field name.
     *
     * @return map of field -> condition
     */
    public Map<String, Condition> getConditions() { return conditions; }

    /**
     * Get the configured maximum number of rows to return.
     *
     * @return configured limit (may be null)
     */
    public Integer getLimit() { return limit; }

    /**
     * Set the maximum number of rows to return.
     *
     * @param limit max rows to return
     */
    public void setLimit(Integer limit) { this.limit = limit; }

    /**
     * Get the configured offset into the result set.
     *
     * @return configured offset (may be null)
     */
    public Integer getOffset() { return offset; }

    /**
     * Set the offset into the result set.
     *
     * @param offset offset into result set
     */
    public void setOffset(Integer offset) { this.offset = offset; }

    /**
     * Get the list of columns to group results by.
     *
     * @return group by columns list
     */
    public java.util.List<String> getGroupBy() { return groupBy; }

    /**
     * Get the list of order-by expressions to apply to results.
     *
     * @return order by expressions list
     */
    public java.util.List<String> getOrderBy() { return orderBy; }
}
