package com.skyblockexp.ezframework.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OperatorTest {
    @Test
    public void enumContainsExpected() {
        Operator[] ops = Operator.values();
        assertTrue(ops.length >= 8);
        assertEquals(Operator.EQ, Operator.valueOf("EQ"));
        assertEquals(Operator.BETWEEN, Operator.valueOf("BETWEEN"));
    }
}
