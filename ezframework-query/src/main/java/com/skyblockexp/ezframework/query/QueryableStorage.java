package com.skyblockexp.ezframework.query;

import java.util.List;

/**
 * Implement this on storage providers to offer basic querying capabilities.
 * Implementations return storage paths that match the query.
 */
public interface QueryableStorage {
    java.util.List<String> query(Query q) throws Exception;
}
