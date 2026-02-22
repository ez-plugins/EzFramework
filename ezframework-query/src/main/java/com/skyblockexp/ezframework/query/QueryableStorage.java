package com.skyblockexp.ezframework.query;

import java.util.List;

/**
 * Implement this on storage providers to offer basic querying capabilities.
 * Implementations return storage paths that match the query.
 */
public interface QueryableStorage {
    /**
     * Query storage and return matching storage paths.
     * @param q query to execute
     * @return list of matching storage paths
     * @throws Exception on query errors
     */
    java.util.List<String> query(Query q) throws Exception;
}
