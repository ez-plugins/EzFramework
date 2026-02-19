package com.skyblockexp.ezframework.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueryTest {

    @Test
    public void addConditionAndAccessors() {
        Query q = new Query();
        q.addCondition("a", new Condition(Operator.EQ, 1));
        assertTrue(q.getConditions().containsKey("a"));
        q.setLimit(10);
        q.setOffset(3);
        assertEquals(10, q.getLimit());
        assertEquals(3, q.getOffset());
        q.getGroupBy().add("grp");
        q.getOrderBy().add("col DESC");
        assertEquals("grp", q.getGroupBy().get(0));
        assertEquals("col DESC", q.getOrderBy().get(0));
    }
}
