package com.skyblockexp.ezframework.query;

import java.util.Map;

/**
 * A single field condition used by {@link Query}.
 */
public class Condition {
    private final Operator op;
    private final Object value;

    /**
     * Create a condition.
     * @param op operator
     * @param value comparison value (may be null for some operators)
     */
    public Condition(Operator op, Object value) {
        this.op = op;
        this.value = value;
    }

    /**
     * Return the operator for this condition.
     *
     * @return the operator
     */
    public Operator getOperator() { return op; }

    /**
     * Return the comparison value for this condition.
     *
     * @return the value used for comparison (may be null)
     */
    public Object getValue() { return value; }

    /**
     * Evaluate whether the given map's value for `key` satisfies this condition.
     *
     * @param map source attributes
     * @param key field name to check
     * @return true if the condition matches
     */
    @SuppressWarnings("unchecked")
    public boolean matches(Map<String, Object> map, String key) {
        boolean exists = map.containsKey(key);
        Object v = map.get(key);
        switch (op) {
            case EXISTS:
                return exists;
            case EQ:
                if (v == null) return value == null;
                return v.equals(value);
            case NEQ:
                if (v == null) return value != null;
                return !v.equals(value);
            case LIKE:
                if (v == null || value == null) return false;
                return v.toString().contains(value.toString());
            case IN:
                if (value == null) return false;
                if (!(value instanceof java.util.Collection)) return false;
                java.util.Collection<?> col = (java.util.Collection<?>) value;
                if (v == null) return col.contains(null);
                return col.contains(v);
            case BETWEEN:
                if (v == null || value == null) return false;
                if (!(value instanceof java.util.List)) return false;
                java.util.List<?> pair = (java.util.List<?>) value;
                if (pair.size() < 2) return false;
                Object a = pair.get(0);
                Object b = pair.get(1);
                if (v instanceof Comparable && a instanceof Comparable && b instanceof Comparable) {
                    try {
                        Comparable vc = (Comparable) v;
                        return vc.compareTo(a) >= 0 && vc.compareTo(b) <= 0;
                    } catch (Exception ignored) {}
                }
                return false;
            case GT:
                if (v instanceof Comparable && value instanceof Comparable) {
                    try { return ((Comparable) v).compareTo(value) > 0; } catch (Exception ignored) {}
                }
                return false;
            case LT:
                if (v instanceof Comparable && value instanceof Comparable) {
                    try { return ((Comparable) v).compareTo(value) < 0; } catch (Exception ignored) {}
                }
                return false;
            default:
                return false;
        }
    }
}
