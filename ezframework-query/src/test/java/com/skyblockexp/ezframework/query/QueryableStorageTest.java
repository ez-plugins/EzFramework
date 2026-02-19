package com.skyblockexp.ezframework.query;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class QueryableStorageTest {

    @Test
    public void implementationIsCalled() throws Exception {
        QueryableStorage impl = new QueryableStorage() {
            @Override
            public java.util.List<String> query(Query q) {
                // return a path list reflecting limit if set
                if (q.getLimit() != null && q.getLimit() == 1) return Arrays.asList("one");
                return Arrays.asList("a","b");
            }
        };

        Query q = new Query(); q.setLimit(1);
        java.util.List<String> out = impl.query(q);
        assertEquals(1, out.size());
        assertEquals("one", out.get(0));
    }
}
