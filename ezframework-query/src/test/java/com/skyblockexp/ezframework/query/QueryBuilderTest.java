package com.skyblockexp.ezframework.query;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class QueryBuilderTest {

    @Test
    public void testBuildBasic() {
        Query q = new QueryBuilder()
                .whereEquals("id", 10)
                .whereLike("name", "bob")
                .whereExists("alive")
                .whereIn("tags", Arrays.asList("x","y"))
                .whereBetween("score", 1, 5)
                .groupBy("g1", null, "g2")
                .orderBy("col1", true)
                .orderBy("col2", false)
                .limit(50)
                .offset(5)
                .build();

        assertEquals(5, q.getConditions().size());
        assertTrue(q.getConditions().get("id").getOperator() == Operator.EQ);
        assertTrue(q.getConditions().get("name").getOperator() == Operator.LIKE);
        assertTrue(q.getConditions().get("alive").getOperator() == Operator.EXISTS);
        assertTrue(q.getConditions().get("tags").getOperator() == Operator.IN);
        assertTrue(q.getConditions().get("score").getOperator() == Operator.BETWEEN);

        assertEquals(2, q.getGroupBy().size());
        assertTrue(q.getOrderBy().get(0).endsWith("ASC"));
        assertTrue(q.getOrderBy().get(1).endsWith("DESC"));
        assertEquals(50, q.getLimit());
        assertEquals(5, q.getOffset());
    }
}
