package com.skyblockexp.ezframework.query;

/**
 * Fluent builder for small queries.
 */
public class QueryBuilder {
    private final Query q = new Query();

    /**
     * Create a new fluent {@link QueryBuilder}.
     */
    public QueryBuilder() {}

    /**
     * Add an equality condition.
     * @param key field name
     * @param value value to compare
     * @return this builder
     */
    public QueryBuilder whereEquals(String key, Object value) {
        q.addCondition(key, new Condition(Operator.EQ, value));
        return this;
    }

    /**
     * Add a substring match condition.
     * @param key field name
     * @param substr substring to match
     * @return this builder
     */
    public QueryBuilder whereLike(String key, String substr) {
        q.addCondition(key, new Condition(Operator.LIKE, substr));
        return this;
    }

    /**
     * Add an existence check for the field.
     * @param key field name
     * @return this builder
     */
    public QueryBuilder whereExists(String key) {
        q.addCondition(key, new Condition(Operator.EXISTS, null));
        return this;
    }

    /**
     * Add an IN condition using a collection of values.
     * @param key field name
     * @param values collection of values
     * @return this builder
     */
    public QueryBuilder whereIn(String key, java.util.Collection<?> values) {
        q.addCondition(key, new Condition(Operator.IN, values));
        return this;
    }

    /**
     * Add a BETWEEN condition (inclusive).
     * @param key field name
     * @param a lower bound
     * @param b upper bound
     * @return this builder
     */
    public QueryBuilder whereBetween(String key, Object a, Object b) {
        java.util.List<Object> pair = new java.util.ArrayList<>(); pair.add(a); pair.add(b);
        q.addCondition(key, new Condition(Operator.BETWEEN, pair));
        return this;
    }

    /**
     * Add group by columns.
     * @param cols column names
     * @return this builder
     */
    public QueryBuilder groupBy(String... cols) {
        if (cols != null) for (String c : cols) if (c != null) q.getGroupBy().add(c);
        return this;
    }

    /**
     * Add an order by expression.
     * @param col column name
     * @param asc true for ascending, false for descending
     * @return this builder
     */
    public QueryBuilder orderBy(String col, boolean asc) {
        if (col != null) q.getOrderBy().add(col + (asc ? " ASC" : " DESC"));
        return this;
    }

    /**
     * Set the maximum number of rows to return for the built query.
     *
     * @param l limit
     * @return this builder
     */
    public QueryBuilder limit(int l) { q.setLimit(l); return this; }

    /**
     * Set the offset into the result set for the built query.
     *
     * @param o offset
     * @return this builder
     */
    public QueryBuilder offset(int o) { q.setOffset(o); return this; }

    /**
     * Build the configured {@link Query}.
     *
     * @return built {@link Query}
     */
    public Query build() { return q; }
}
