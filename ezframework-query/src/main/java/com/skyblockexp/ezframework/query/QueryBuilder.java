package com.skyblockexp.ezframework.query;

/**
 * Fluent builder for small queries.
 */
public class QueryBuilder {
    private final Query q = new Query();

    public QueryBuilder whereEquals(String key, Object value) {
        q.addCondition(key, new Condition(Operator.EQ, value));
        return this;
    }

    public QueryBuilder whereLike(String key, String substr) {
        q.addCondition(key, new Condition(Operator.LIKE, substr));
        return this;
    }

    public QueryBuilder whereExists(String key) {
        q.addCondition(key, new Condition(Operator.EXISTS, null));
        return this;
    }

    public QueryBuilder whereIn(String key, java.util.Collection<?> values) {
        q.addCondition(key, new Condition(Operator.IN, values));
        return this;
    }

    public QueryBuilder whereBetween(String key, Object a, Object b) {
        java.util.List<Object> pair = new java.util.ArrayList<>(); pair.add(a); pair.add(b);
        q.addCondition(key, new Condition(Operator.BETWEEN, pair));
        return this;
    }

    public QueryBuilder groupBy(String... cols) {
        if (cols != null) for (String c : cols) if (c != null) q.getGroupBy().add(c);
        return this;
    }

    public QueryBuilder orderBy(String col, boolean asc) {
        if (col != null) q.getOrderBy().add(col + (asc ? " ASC" : " DESC"));
        return this;
    }

    public QueryBuilder limit(int l) { q.setLimit(l); return this; }
    public QueryBuilder offset(int o) { q.setOffset(o); return this; }

    public Query build() { return q; }
}
