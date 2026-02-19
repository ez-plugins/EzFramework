package com.skyblockexp.ezframework.query;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionTest {

    @Test
    public void exists_and_eq_and_neq() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", "Alice");
        Condition c1 = new Condition(Operator.EXISTS, null);
        assertTrue(c1.matches(m, "name"));
        assertFalse(c1.matches(m, "missing"));

        Condition c2 = new Condition(Operator.EQ, "Alice");
        assertTrue(c2.matches(m, "name"));
        Condition c3 = new Condition(Operator.NEQ, "Bob");
        assertTrue(c3.matches(m, "name"));
    }

    @Test
    public void like_in_between_and_comparisons() {
        Map<String, Object> m = new HashMap<>();
        m.put("desc", "hello world");
        m.put("age", 30);

        Condition like = new Condition(Operator.LIKE, "lo wo");
        assertTrue(like.matches(m, "desc"));

        Condition in = new Condition(Operator.IN, Arrays.asList(10,20,30));
        assertTrue(in.matches(m, "age"));

        Condition between = new Condition(Operator.BETWEEN, Arrays.asList(20,40));
        assertTrue(between.matches(m, "age"));

        Condition gt = new Condition(Operator.GT, 25);
        assertTrue(gt.matches(m, "age"));
        Condition lt = new Condition(Operator.LT, 40);
        assertTrue(lt.matches(m, "age"));
    }

    @Test
    public void null_and_type_mismatches() {
        Map<String, Object> m = new HashMap<>();
        m.put("x", null);

        Condition eqNull = new Condition(Operator.EQ, null);
        assertTrue(eqNull.matches(m, "x"));

        Condition inNotCollection = new Condition(Operator.IN, "not-a-collection");
        assertFalse(inNotCollection.matches(m, "x"));

        Condition betweenShort = new Condition(Operator.BETWEEN, Collections.singletonList(1));
        assertFalse(betweenShort.matches(m, "x"));
    }
}

