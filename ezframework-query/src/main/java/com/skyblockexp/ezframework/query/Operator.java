package com.skyblockexp.ezframework.query;

/**
 * Comparison operators supported by {@link Condition}.
 */
public enum Operator {
    /** Equality */
    EQ,
    /** Not equal */
    NEQ,
    /** Greater than */
    GT,
    /** Less than */
    LT,
    /** Substring match */
    LIKE,
    /** Existence check */
    EXISTS,
    /** Collection membership */
    IN,
    /** Between (inclusive) */
    BETWEEN
}
